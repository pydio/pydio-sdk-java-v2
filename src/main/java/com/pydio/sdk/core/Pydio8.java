package com.pydio.sdk.core;

import com.pydio.sdk.core.api.p8.Configuration;
import com.pydio.sdk.core.api.p8.P8Client;
import com.pydio.sdk.core.api.p8.P8Request;
import com.pydio.sdk.core.api.p8.P8RequestBuilder;
import com.pydio.sdk.core.api.p8.P8Response;
import com.pydio.sdk.core.api.p8.consts.Action;
import com.pydio.sdk.core.api.p8.consts.Param;
import com.pydio.sdk.core.common.callback.ChangeProcessor;
import com.pydio.sdk.core.common.callback.NodeHandler;
import com.pydio.sdk.core.common.callback.RegistryItemHandler;
import com.pydio.sdk.core.common.callback.TransferProgressListener;
import com.pydio.sdk.core.common.errors.Code;
import com.pydio.sdk.core.common.errors.SDKException;
import com.pydio.sdk.core.common.http.ContentBody;
import com.pydio.sdk.core.common.parser.RegistrySaxHandler;
import com.pydio.sdk.core.common.parser.ServerGeneralRegistrySaxHandler;
import com.pydio.sdk.core.common.parser.TreeNodeSaxHandler;
import com.pydio.sdk.core.common.parser.WorkspaceNodeSaxHandler;
import com.pydio.sdk.core.model.FileNode;
import com.pydio.sdk.core.model.Message;
import com.pydio.sdk.core.model.Node;
import com.pydio.sdk.core.model.NodeDiff;
import com.pydio.sdk.core.model.ServerNode;
import com.pydio.sdk.core.model.Token;
import com.pydio.sdk.core.security.Credentials;
import com.pydio.sdk.core.utils.io;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ProtocolException;
import java.rmi.UnknownHostException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;

public class Pydio8 implements Client {

    private ServerNode serverNode;
    private P8Client p8;
    private Credentials credentials;
    private String secureToken;
    private int loginFailure;

    public Pydio8(ServerNode node) {
        this.serverNode = node;
        loginFailure = 0;

        Configuration config = new Configuration();
        config.endpoint = serverNode.url();
        config.resolver = serverNode.getServerResolver();
        if (config.selfSigned = serverNode.unVerifiedSSL()) {
            config.sslContext = serverNode.getSslContext();
            config.hostnameVerifier = serverNode.getHostnameVerifier();
        }
        p8 = new P8Client(config);
    }

    private void loginIfRequire() {
    }

    private P8Request refreshSecureToken(P8Request req) {
        try {
            JSONObject info = authenticationInfo();
            if (!info.has(Param.captchaCode)) {
                login();
                return P8RequestBuilder.update(req).setSecureToken(secureToken).getRequest();
            }
        } catch (SDKException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadSecureToken() throws SDKException {
        if (null == secureToken || "".equals(secureToken)) {
            login();
        }
    }

    @Override
    public void setCredentials(Credentials c) {
        this.credentials = c;
    }

    @Override
    public void setTokenProvider(Token.Provider p) {

    }

    @Override
    public void setTokenStore(Token.Store s) {

    }

    @Override
    public String getUser() {
        return this.credentials.getLogin();
    }

    @Override
    public InputStream getUserData(String binary) throws SDKException {
        P8Request req = P8RequestBuilder.getUserData(credentials.getLogin(), binary).setSecureToken(secureToken).getRequest();
        P8Response rsp = p8.execute(req);

        final int code = rsp.code();
        if (code != Code.ok) {
            if (code == Code.authentication_required && credentials != null && loginFailure == 0) {
                loginFailure++;
                login();
                return getUserData(binary);
            }
            throw SDKException.fromP8Code(code);
        }
        return rsp.getContent();
    }

    @Override
    public void login() throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.login(credentials);
        P8Request req = builder.getRequest();
        P8Response rsp = p8.execute(req);
        final int code = rsp.code();
        if (code != Code.ok) {
            throw SDKException.fromP8Code(code);
        }

        Document doc = rsp.toXMLDocument();
        if (doc != null) {
            if (doc.getElementsByTagName("logging_result").getLength() > 0) {
                String result = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem("value").getNodeValue();
                if (result.equals("1")) {
                    secureToken = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem(Param.secureToken).getNodeValue();
                    loginFailure = 0;
                } else {
                    loginFailure++;
                    if (result.equals("-4")) {
                        throw SDKException.fromP8Code(Code.authentication_with_captcha_required);
                    }
                    throw SDKException.fromP8Code(Code.authentication_required);
                }
            } else {
                throw SDKException.unexpectedContent(new IOException(doc.toString()));
            }
        } else {
            throw SDKException.fromP8Code(Code.authentication_required);
        }
    }

    @Override
    public void logout() {
        p8.execute(P8RequestBuilder.logout().setSecureToken(secureToken).getRequest());
    }

    @Override
    public X509Certificate[] remoteCertificateChain() {
        return new X509Certificate[0];
    }

    @Override
    public void downloadServerRegistry(RegistryItemHandler itemHandler) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.serverRegistry();
        P8Response rsp = p8.execute(builder.getRequest());
        final int code = rsp.saxParse(new ServerGeneralRegistrySaxHandler(itemHandler));
        if (code != Code.ok) {
            throw SDKException.fromP8Code(code);
        }
    }

    @Override
    public void downloadWorkspaceRegistry(String ws, RegistryItemHandler itemHandler) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.workspaceRegistry(ws).setSecureToken(secureToken);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        final int code = rsp.saxParse(new RegistrySaxHandler(itemHandler));
        if (code != Code.ok) {
            throw SDKException.fromP8Code(code);
        }
    }

    @Override
    public void workspaceList(NodeHandler handler) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.workspaceList().setSecureToken(secureToken);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }
        final int code = rsp.saxParse(new WorkspaceNodeSaxHandler(handler, 0, -1));
        if (code != Code.ok) {
            throw SDKException.fromP8Code(code);
        }
    }

    @Override
    public FileNode nodeInfo(String ws, String path) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.nodeInfo(ws, path).setSecureToken(secureToken);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }

        final FileNode[] node = new FileNode[1];
        final int resultCode = rsp.saxParse(new TreeNodeSaxHandler((n) -> node[0] = (FileNode) n));
        if (resultCode != Code.ok) {
            throw SDKException.fromP8Code(resultCode);
        }
        return node[0];
    }

    @Override
    public FileNode ls(String ws, String folder, NodeHandler handler) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.ls(ws, folder).setSecureToken(secureToken);
        while (true) {
            P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
            int code = rsp.code();
            if (code != Code.ok) {
                throw SDKException.fromP8Code(code);
            }

            TreeNodeSaxHandler treeHandler = new TreeNodeSaxHandler(handler);
            final int resultCode = rsp.saxParse(treeHandler);
            if (resultCode != Code.ok) {
                throw SDKException.fromP8Code(resultCode);
            }

            if (treeHandler.mPagination) {
                if (!(treeHandler.mPaginationTotalPage == treeHandler.mPaginationCurrentPage)) {
                    builder.setParam(Param.dir, folder + "%23" + (treeHandler.mPaginationCurrentPage + 1));
                } else {
                    return treeHandler.mRootNode;
                }
            } else {
                return treeHandler.mRootNode;
            }
        }
    }

    @Override
    public void search(String ws, String pattern, NodeHandler h) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.search(ws, pattern).setSecureToken(secureToken);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        int code = rsp.code();
        if (code != Code.ok) {
            throw SDKException.fromP8Code(code);
        }

        final int resultCode = rsp.saxParse(new TreeNodeSaxHandler(h));
        if (resultCode != Code.ok) {
            throw SDKException.fromP8Code(resultCode);
        }
    }

    @Override
    public Message upload(InputStream source, long length, String ws, String path, String name, boolean autoRename, TransferProgressListener progressListener) throws SDKException {
        stats(ws, path, false);

        //TODO: get size from server configs
        long maxChunkSize = 2 * 1024 * 1204;
        ContentBody cb = new ContentBody(source, name, length, maxChunkSize);
        if (progressListener != null) {
            cb.setListener(new ContentBody.ProgressListener() {
                @Override
                public void transferred(long num) throws IOException {
                    if (progressListener.onProgress(num)) {
                        throw new IOException("");
                    }
                }
                @Override
                public void partTransferred(int part, int total) throws IOException {
                    if (total == 0) total = 1;
                    if (progressListener.onProgress(part * 100 / total)) {
                        throw new IOException("");
                    }
                }
            });
        }

        P8RequestBuilder builder;
        try {
            builder = P8RequestBuilder.upload(ws, path, name, autoRename, cb).setSecureToken(secureToken);
        } catch (IOException e) {
            e.printStackTrace();
            throw SDKException.encoding(e);
        }

        P8Request req = builder.getRequest();
        P8Response rsp = p8.execute(req, this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }

        while (rsp.code() == Code.ok && !cb.allChunksWritten()) {
            NodeDiff diff = NodeDiff.create(rsp.toXMLDocument());
            if (diff.updated != null && diff.updated.size() > 0) {
                name = diff.updated.get(0).label();
            } else if (diff.added != null && diff.added.size() > 0) {
                name = diff.added.get(0).label();
            } else {
                //todo: stats "name" to get info
            }
            builder.setParam(Param.appendToUrlencodedPart, name);
            rsp = p8.execute(builder.getRequest());
        }

        if (rsp.code() != Code.ok) {
            return Message.create(Message.ERROR, rsp.toString());
        }

        NodeDiff diff;
        Document doc = rsp.toXMLDocument();
        if (doc != null) {
            diff = NodeDiff.create(doc);
            if (diff.added != null) {
                Node node = diff.added.get(0);
                String label = node.label();
                if (!label.equals(cb.getFilename())) {
                    cb.setFilename(label);
                }

                Message msg = Message.create(Message.SUCCESS, "");
                msg.added = diff.added;
                msg.updated = diff.updated;
                msg.deleted = diff.deleted;
                return msg;
            }
        }


        FileNode info = nodeInfo(ws, path + "/" + name);

        Message msg = Message.create(Message.SUCCESS, "");
        if (msg.added == null) {
            msg.added = new ArrayList<>();
        }
        msg.added.add(info);
        return msg;
    }

    @Override
    public Message upload(File source, String ws, String path, String name, boolean autoRename, TransferProgressListener progressListener) throws SDKException {
        FileInputStream in;
        try {
            in = new FileInputStream(source);
        } catch (FileNotFoundException e) {
            throw SDKException.notFound(e);
        }
        return upload(in, source.length(), ws, path, name, autoRename, progressListener);
    }

    @Override
    public String uploadURL(String ws, String folder, String name, boolean autoRename) throws SDKException {
        loadSecureToken();
        try {
            P8RequestBuilder builder = null;
            try {
                builder = P8RequestBuilder.upload(ws, folder, name, autoRename, null);
            } catch (IOException e) {
                e.printStackTrace();
                throw SDKException.encoding(e);
            }
            return p8.getURL(builder.getRequest());
        } catch (ProtocolException | UnknownHostException e) {
            throw SDKException.malFormURI(e);
        } catch (UnsupportedEncodingException e) {
            throw SDKException.encoding(e);
        }
    }

    @Override
    public long download(String ws, String path, OutputStream target, TransferProgressListener progressListener) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.download(ws, path).setSecureToken(secureToken);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);

        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }


        try {
            return rsp.write(target, progressListener);
        } catch (IOException e) {
            e.printStackTrace();
            throw SDKException.conReadFailed(e);
        }
    }

    @Override
    public long download(String ws, String file, File target, TransferProgressListener progressListener) throws SDKException {
        OutputStream out;
        try {
            out = new FileOutputStream(target);
        } catch (FileNotFoundException e) {
            throw SDKException.notFound(e);
        }

        try {
            return download(ws, file, out, progressListener);
        } finally {
            io.close(out);
        }
    }

    @Override
    public String downloadURL(String ws, String file) throws SDKException {
        loadSecureToken();

        P8RequestBuilder builder = P8RequestBuilder.download(ws, file).setSecureToken(secureToken);
        try {
            return p8.getURL(builder.getRequest());
        } catch (ProtocolException | UnknownHostException e) {
            throw SDKException.malFormURI(e);
        } catch (UnsupportedEncodingException e) {
            throw SDKException.encoding(e);
        }
    }

    @Override
    public Message delete(String ws, String[] files) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.delete(ws, files).setSecureToken(secureToken);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }
        Document xml = rsp.toXMLDocument();
        return Message.create(xml);
    }

    @Override
    public Message restore(String ws, String[] files) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.restore(ws, files).setSecureToken(secureToken);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }
        Document xml = rsp.toXMLDocument();
        return Message.create(xml);
    }

    @Override
    public Message move(String ws, String[] files, String dstFolder) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.move(ws, files, dstFolder).setSecureToken(secureToken);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }
        Document xml = rsp.toXMLDocument();
        return Message.create(xml);
    }

    @Override
    public Message rename(String ws, String srcFile, String dstFile) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.rename(ws, srcFile, new File(dstFile).getName()).setSecureToken(secureToken);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }
        Document xml = rsp.toXMLDocument();
        return Message.create(xml);
    }

    @Override
    public Message copy(String ws, String[] files, String folder) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.copy(ws, files, folder).setSecureToken(secureToken);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }
        Document xml = rsp.toXMLDocument();
        return Message.create(xml);
    }

    @Override
    public Message mkdir(String ws, String parent, String name) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.mkdir(ws, parent, name).setSecureToken(secureToken);
        P8Request req = builder.getRequest();
        P8Response rsp = p8.execute(req, this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }
        Document xml = rsp.toXMLDocument();
        return Message.create(xml);
    }

    @Override
    public InputStream previewData(String ws, String file, int dim) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.previewImage(ws, file, dim).setSecureToken(secureToken);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }
        return rsp.getContent();
    }

    @Override
    public String streamingAudioURL(String ws, String file) throws SDKException {
        loadSecureToken();

        P8RequestBuilder builder = P8RequestBuilder.streamingAudio(ws, file).setSecureToken(secureToken);
        try {
            return p8.getURL(builder.getRequest());
        } catch (ProtocolException | UnknownHostException e) {
            throw SDKException.malFormURI(e);
        } catch (UnsupportedEncodingException e) {
            throw SDKException.encoding(e);
        }
    }

    @Override
    public String streamingVideoURL(String ws, String file) throws SDKException {
        loadSecureToken();

        P8RequestBuilder builder = P8RequestBuilder.streamingVideo(ws, file).setSecureToken(secureToken);
        try {
            return p8.getURL(builder.getRequest());
        } catch (ProtocolException | UnknownHostException e) {
            throw SDKException.malFormURI(e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw SDKException.encoding(e);
        }
    }

    @Override
    public JSONObject stats(String ws, String file, boolean withHash) throws SDKException {
        loadSecureToken();

        P8RequestBuilder builder = P8RequestBuilder.stats(ws, file, withHash).setSecureToken(secureToken);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }
        String h = rsp.getHeaders("Content-Type").get(0);
        if (!"application/json".equals(h.toLowerCase())) {
            throw SDKException.unexpectedContent(new IOException(String.format("wrong response content type: %s", h)));
        }

        try {
            return new JSONObject(rsp.toString());
        } catch (Exception e) {
            throw SDKException.unexpectedContent(e);
        }
    }

    @Override
    public long changes(String ws, String filter, int seq, boolean flatten, ChangeProcessor processor) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.changes(ws, filter, seq, flatten).setSecureToken(secureToken);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);

        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }

        String h = rsp.getHeaders("Content-Type").get(0);
        if (!h.toLowerCase().contains("application/json")) {
            throw SDKException.unexpectedContent(new IOException(rsp.toString()));
        }

        final long[] lastSeq = new long[1];
        final boolean[] readLastLine = {false};

        rsp.lineByLine("UTF-8", "\\n", (line) -> {
            if (readLastLine[0]) {
                return;
            }

            if (line.toLowerCase().startsWith("last_seq")) {
                readLastLine[0] = true;
                lastSeq[0] = Integer.parseInt(line.split(":")[1]);
                return;
            }

            final String[] change = new String[11];
            JSONObject json;
            try {
                json = new JSONObject(line);
            } catch (ParseException e) {
                return;
            }
            change[Pydio.CHANGE_INDEX_SEQ] = json.getString(Pydio.CHANGE_SEQ);
            lastSeq[0] = Math.max(lastSeq[0], Integer.parseInt(change[Pydio.CHANGE_INDEX_SEQ]));
            change[Pydio.CHANGE_INDEX_NODE_ID] = json.getString(Pydio.CHANGE_NODE_ID);
            change[Pydio.CHANGE_INDEX_TYPE] = json.getString(Pydio.CHANGE_TYPE);
            change[Pydio.CHANGE_INDEX_SOURCE] = json.getString(Pydio.CHANGE_SOURCE);
            change[Pydio.CHANGE_INDEX_TARGET] = json.getString(Pydio.CHANGE_TARGET);
            change[Pydio.CHANGE_INDEX_NODE_BYTESIZE] = json.getJSONObject(Pydio.CHANGE_NODE).getString(Pydio.CHANGE_NODE_BYTESIZE);
            change[Pydio.CHANGE_INDEX_NODE_MD5] = json.getJSONObject(Pydio.CHANGE_NODE).getString(Pydio.CHANGE_NODE_MD5);
            change[Pydio.CHANGE_INDEX_NODE_MTIME] = json.getJSONObject(Pydio.CHANGE_NODE).getString(Pydio.CHANGE_NODE_MTIME);
            change[Pydio.CHANGE_INDEX_NODE_PATH] = json.getJSONObject(Pydio.CHANGE_NODE).getString(Pydio.CHANGE_NODE_PATH);
            change[Pydio.CHANGE_INDEX_NODE_WORKSPACE] = json.getJSONObject(Pydio.CHANGE_NODE).getString(Pydio.CHANGE_NODE_WORKSPACE);
            change[10] = "remote";
            processor.process(change);
        });
        return Math.max(seq, lastSeq[0]);
    }

    @Override
    public String share(String ws, String file, String ws_label, boolean isFolder, String ws_description, String password, int expiration, int download, boolean canPreview, boolean canDownload) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.share(ws, file, ws_description);
        if (password != null && !"".equals(password)) {
            builder.setParam(Param.shareGuestUserPassword, password);
        }

        if (!canPreview) {
            builder.setParam(Param.miniSiteLayout, "ajxp_unique_dl");
        }

        if (!canDownload) {
            builder.setParam(Param.miniSiteLayout, "ajxp_unique_strip");
        }

        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }
        return rsp.toString();
    }

    @Override
    public void unshare(String ws, String file) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.shareInfo(ws, file);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }
    }

    @Override
    public JSONObject shareInfo(String ws, String file) throws SDKException {
        P8RequestBuilder builder = P8RequestBuilder.shareInfo(ws, file);
        P8Response rsp = p8.execute(builder.getRequest(), this::refreshSecureToken, Code.authentication_required);
        if (rsp.code() != Code.ok) {
            throw SDKException.fromP8Code(rsp.code());
        }
        try {
            return new JSONObject(rsp.toString());
        } catch (Exception e) {
            throw SDKException.unexpectedContent(e);
        }
    }

    @Override
    public InputStream getCaptcha() {
        P8Request request = new P8RequestBuilder().setAction(Action.getCaptcha).getRequest();
        return p8.execute(request).getContent();
    }

    @Override
    public JSONObject authenticationInfo() throws SDKException {
        P8Response seedResponse = p8.execute(P8RequestBuilder.getSeed().getRequest());
        String seed = seedResponse.toString();
        JSONObject o = new JSONObject();

        boolean withCaptcha = false;

        if (!"-1".equals(seed)) {
            seed = seed.trim();
            if (seed.contains("\"seed\":-1") || seed.contains("\"seed\": -1")) {
                withCaptcha = seed.contains("\"captcha\": true") || seed.contains("\"captcha\":true");
                o.put(Param.seed, "-1");

            } else {
                String contentType = seedResponse.getHeaders("Content-Type").get(0);
                boolean seemsToBePydio = (contentType != null) && (
                        (contentType.toLowerCase().contains("text/plain"))
                                | (contentType.toLowerCase().contains("text/xml"))
                                | (contentType.toLowerCase().contains("text/json"))
                                | (contentType.toLowerCase().contains("application/json")));

                if (!seemsToBePydio) {
                    throw SDKException.unexpectedContent(new IOException(seed));
                }
                o.put(Param.seed, "-1");
            }
        }

        if (withCaptcha) {
            o.put(Param.captchaCode, true);
        }
        return o;
    }
}
