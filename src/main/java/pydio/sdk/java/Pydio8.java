package pydio.sdk.java;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import pydio.sdk.java.core.errors.SDKException;
import pydio.sdk.java.core.http.ContentBody;
import pydio.sdk.java.core.http.HttpResponse;
import pydio.sdk.java.core.model.FileNode;
import pydio.sdk.java.core.model.Message;
import pydio.sdk.java.core.model.Node;
import pydio.sdk.java.core.model.NodeDiff;
import pydio.sdk.java.core.model.ServerNode;
import pydio.sdk.java.core.model.WorkspaceNode;
import pydio.sdk.java.core.transport.SessionTransport;
import pydio.sdk.java.core.transport.Transport;
import pydio.sdk.java.core.transport.TransportFactory;
import pydio.sdk.java.core.utils.ChangeProcessor;
import pydio.sdk.java.core.utils.HttpResponseParser;
import pydio.sdk.java.core.utils.NodeHandler;
import pydio.sdk.java.core.utils.Params;
import pydio.sdk.java.core.utils.Pydio;
import pydio.sdk.java.core.utils.RegistryItemHandler;
import pydio.sdk.java.core.utils.RegistrySaxHandler;
import pydio.sdk.java.core.utils.ServerGeneralRegistrySaxHandler;
import pydio.sdk.java.core.utils.Token;
import pydio.sdk.java.core.utils.TransferProgressListener;
import pydio.sdk.java.core.utils.TreeNodeSaxHandler;
import pydio.sdk.java.core.utils.UnexpectedResponseException;
import pydio.sdk.java.core.utils.WorkspaceNodeSaxHandler;
import pydio.sdk.java.utils.io;

public class Pydio8 implements Client {

    private ServerNode serverNode;
    private WorkspaceNode workspace;
    private SessionTransport transport;
    private String user;

    public Pydio8(ServerNode node) {
        this.serverNode = node;
        transport = (SessionTransport) TransportFactory.getInstance(Transport.MODE_SESSION, node);
    }

    private void loginIfRequire() throws SDKException {
        try {
            transport.getResponse("piwaidi", null);
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "login failed", e);
        }
    }

    @Override
    public String getURLString() {
        return serverNode.url();
    }

    @Override
    public void setUser(String user) {
        this.user = user;
        this.transport.mUser = user;
    }

    @Override
    public void setTokenProvider(Token.Provider p) {

    }

    @Override
    public void setTokenStore(Token.Store s) {

    }

    @Override
    public void setServerNode(ServerNode node) {
        serverNode = node;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public InputStream getUserData(String binary) throws SDKException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Pydio.PARAM_USER_ID, user);
        params.put(Pydio.PARAM_BINARY_ID, binary);
        try {
            return transport.getResponseStream(Pydio.ACTION_GET_BINARY_PARAM, params);
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "to load user data", e);
        }
    }

    @Override
    public void login() throws SDKException {
        try {
            transport.login();
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "login failed", e);
        }
    }

    @Override
    public void logout() throws SDKException {
        String action = Pydio.ACTION_LOGOUT;
        String response = null;
        try {
            response = transport.getStringContent(action, null);
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "logout failed", e);
        }

        boolean result = response != null && response.contains("logging_result value=\"2\"");
        if (result) {
            transport.mSecureToken = "";
        }
    }

    @Override
    public X509Certificate[] remoteCertificateChain() {
        return new X509Certificate[0];
    }

    @Override
    public void downloadServerRegistry(RegistryItemHandler itemHandler) throws SDKException {
        InputStream in;
        try {
            in = transport.getResponseStream(Pydio.ACTION_GET_REGISTRY, null);
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to load registry", e);
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, new ServerGeneralRegistrySaxHandler(itemHandler));
        } catch (Exception e) {
            throw SDKException.unexpectedContent(e);
        } finally {
            io.close(in);
        }
    }

    @Override
    public void downloadWorkspaceRegistry(String ws, RegistryItemHandler itemHandler) throws SDKException {
        Map<String, String> params = new HashMap<>();
        params.put(Pydio.PARAM_TEMP_WORKSPACE, ws);

        InputStream in;
        try {
            in = transport.getResponseStream(Pydio.ACTION_GET_REGISTRY, params);
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to load registry", e);
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, new RegistrySaxHandler(itemHandler));
        } catch (Exception e) {
            throw SDKException.unexpectedContent(e);
        } finally {
            io.close(in);
        }
    }

    @Override
    public void workspaceList(NodeHandler handler) throws SDKException {
        Map<String, String> params = new HashMap<String, String>();
        String action = Pydio.ACTION_GET_REGISTRY;
        params.put(Pydio.PARAM_XPATH, Pydio.XPATH_USER_WORKSPACES);

        DefaultHandler saxHandler = new WorkspaceNodeSaxHandler(handler, 0, -1);
        HttpResponse r;
        try {
            r = transport.getResponse(action, params);
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to download registry", e);
        }

        try {
            SAXParserFactory
                    .newInstance()
                    .newSAXParser()
                    .parse(r.getEntity().getContent(), saxHandler);
        } catch (Exception e) {
            throw SDKException.unexpectedContent(e);
        }
    }

    @Override
    public FileNode nodeInfo(String ws, String path) throws SDKException {
        Map<String, String> params = new HashMap<String, String>();

        String action = Pydio.ACTION_LIST;
        params.put(Pydio.PARAM_OPTIONS, "al");
        params.put(Pydio.PARAM_FILE, path);
        params.put(Pydio.PARAM_TEMP_WORKSPACE, ws);


        final FileNode[] node = new FileNode[1];
        DefaultHandler saxHandler = new TreeNodeSaxHandler((n) -> {
            node[0] = (FileNode) n;
        });

        HttpResponse r;
        InputStream in;
        try {
            r = transport.getResponse(action, params);
            in = r.getEntity().getContent();
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to download node data", e);
        }

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, saxHandler);
            return node[0];
        } catch (Exception e) {
            throw SDKException.unexpectedContent(e);
        }
    }

    @Override
    public FileNode ls(String ws, String folder, NodeHandler handler) throws SDKException {

        String action;
        action = Pydio.ACTION_LIST;
        Map<String, String> params = Params.create(Pydio.PARAM_OPTIONS, "al").
                set(Pydio.PARAM_DIR, folder).
                set(Pydio.PARAM_TEMP_WORKSPACE, ws).
                get();

        while (true) {
            HttpResponse r;
            InputStream in;
            try {
                r = transport.getResponse(action, params);
                in = r.getEntity().getContent();
            } catch (IOException e) {
                throw new SDKException(transport.requestStatus(), "failed to download folder content", e);
            }

            SAXParserFactory factory = SAXParserFactory.newInstance();
            TreeNodeSaxHandler saxHandler = new TreeNodeSaxHandler(handler);
            try {
                SAXParser parser = factory.newSAXParser();
                parser.parse(in, saxHandler);
            } catch (Exception e) {
                throw SDKException.unexpectedContent(e);
            }

            if (saxHandler.mPagination) {
                if (!(saxHandler.mPaginationTotalPage == saxHandler.mPaginationCurrentPage)) {
                    params.put(Pydio.PARAM_DIR, folder + "%23" + (saxHandler.mPaginationCurrentPage + 1));
                } else {
                    return saxHandler.mRootNode;
                }
            } else {
                return saxHandler.mRootNode;
            }
        }
    }

    @Override
    public void search(String ws, String pattern, NodeHandler h) throws SDKException {
        Map<String, String> params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws).
                set(Pydio.PARAM_SEARCH_QUERY, pattern).get();

        HttpResponse response;
        InputStream in;
        try {
            response = transport.getResponse(Pydio.ACTION_SEARCH, params);
            in = response.getEntity().getContent();
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to download search result", e);
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, new TreeNodeSaxHandler(h));
        } catch (Exception e) {
            throw SDKException.unexpectedContent(e);
        } finally {
            io.close(in);

        }

    }

    @Override
    public Message upload(InputStream source, long length, String ws, String path, String name, boolean autoRename, TransferProgressListener progressListener) throws SDKException {
        loginIfRequire();

        String action;

        JSONObject stats = stats(ws, path, false);
        if (stats == null || stats.length() == 0) {
            throw SDKException.notFound(new FileNotFoundException(path));
        }


        action = Pydio.ACTION_UPLOAD;
        Params params = Params.
                create(Pydio.PARAM_TEMP_WORKSPACE, ws).
                set(Pydio.PARAM_DIR, path).
                set(Pydio.PARAM_XHR_UPLOADER, "true");


        String urlEncodedName = null;
        try {
            urlEncodedName = java.net.URLEncoder.encode(name, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw SDKException.encoding("utf-8", name, e);
        }

        params.set(Pydio.PARAM_URL_ENCODED_FILENAME, urlEncodedName);
        if (autoRename) {
            params.set(Pydio.PARAM_AUTO_RENAME, "true");
        }

        //TODO: try fetch upload chunck size
        /*if (serverNode.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE) == null) {
            serverGeneralRegistry(new RegistryItemHandler() {
                @Override
                protected void onPref(String name, String value) {
                    super.onPref(name, value);
                    server.setProperty(name, value);
                }
            });
        }
        long size = Long.parseLong(serverNode.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE));
        */
        long size = 2 * 1024 * 1204;
        ContentBody cb = new ContentBody(source, name, length, size);

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

        HttpResponse response = null;
        try {
            response = transport.putContent(action, params.get(), cb);
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), String.format("failed to put %s content in %s", name, path), e);
        }

        if (response == null) {
            return Message.create(Message.EMPTY, "");
        }

        while (response.code() == 200 && !cb.allChunksWritten()) {
            params.set(Pydio.PARAM_APPEND_TO_URLENCODED_PART, cb.getFilename());
            try {
                response = transport.putContent(action, params.get(), cb);
            } catch (IOException e) {
                throw new SDKException(transport.requestStatus(), String.format("failed to put %s content in %s", name, path), e);
            }

            if (response == null) {
                return Message.create(Message.EMPTY, "");
            }
        }

        if (response.code() != 200) {
            return Message.create(Message.ERROR, response.toString());
        }

        NodeDiff diff;
        try {
            diff = NodeDiff.create(HttpResponseParser.getXML(response));
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
        } catch (IOException ignore) {
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
        loginIfRequire();

        Params params = Params.
                create(Pydio.PARAM_TEMP_WORKSPACE, ws).
                set(Pydio.PARAM_DIR, folder).
                set(Pydio.PARAM_XHR_UPLOADER, "true");

        String urlEncodedName = null;
        try {
            urlEncodedName = java.net.URLEncoder.encode(name, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw SDKException.encoding("utf-8", name, e);
        }

        params.set(Pydio.PARAM_URL_ENCODED_FILENAME, urlEncodedName);
        if (autoRename) {
            params.set(Pydio.PARAM_AUTO_RENAME, "true");
        }

        try {
            return transport.getGETUrl(Pydio.ACTION_UPLOAD, params.get());
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to get file download URL", e);
        }
    }

    @Override
    public long download(String ws, String path, OutputStream target, TransferProgressListener progressListener) throws SDKException {
        Map<String, String> params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws).
                set(Pydio.PARAM_FILE, path).get();

        HttpResponse response;
        InputStream stream;

        try {
            response = transport.getResponse(Pydio.ACTION_DOWNLOAD, params);
            stream = response.getEntity().getContent();
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), String.format("failed to download %s", path), e);
        }

        int status = transport.requestStatus();
        if (status != Pydio.OK) {
            throw new SDKException(transport.requestStatus(), String.format("failed to download %s", path), null);
        }

        long total_read = 0;
        int read, buffer_size = Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE;
        byte[] buffer = new byte[buffer_size];

        for (; ; ) {
            try {
                read = stream.read(buffer);
            } catch (IOException e) {
                throw SDKException.conReadFailed(e);
            } finally {
                io.close(stream);
            }

            if (read == -1) break;

            total_read += read;
            try {
                target.write(buffer, 0, read);
            } catch (IOException e) {
                throw SDKException.conWriteFailed(e);
            } finally {
                io.close(stream);
            }

            if (progressListener != null) {
                progressListener.onProgress(total_read);
            }
        }

        io.close(stream);
        return total_read;
    }

    @Override
    public long download(String ws, String file, File target, TransferProgressListener progressListener) throws SDKException {
        OutputStream out;
        try {
            out = new FileOutputStream(file);
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
        loginIfRequire();
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws).
                set(Pydio.PARAM_SECURE_TOKEN, transport.mSecureToken).
                set(Pydio.PARAM_FILE, file);
        try {
            return transport.getGETUrl(Pydio.ACTION_DOWNLOAD, params.get());
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to get file download URL", e);
        }
    }

    @Override
    public Message delete(String ws, String[] files) throws SDKException {
        Params params = Params
                .create(Pydio.PARAM_TEMP_WORKSPACE, ws);

        boolean foundRecycle = false;
        int count = 0;
        for (String file : files) {
            foundRecycle |= file.contains("/recycle_bin");
            params.set(Pydio.PARAM_FILE + "_" + count, file);
        }

        if (foundRecycle) {
            params.set(Pydio.PARAM_DIR, "/recycle_bin");
        }

        try {
            return Message.create(transport.getXmlContent(Pydio.ACTION_DELETE, params.get()));
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to delete files", e);
        }
    }

    @Override
    public Message restore(String ws, String[] files) throws SDKException {
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws).
                set(Pydio.PARAM_DIR, "/recycle_bin");

        int count = 0;
        for (String file : files) {
            params.set(Pydio.PARAM_FILE + "_" + count, file);
        }

        try {
            return Message.create(transport.getXmlContent(Pydio.ACTION_RESTORE, params.get()));
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to restore files", e);
        }
    }

    @Override
    public Message move(String ws, String[] files, String dstFolder) throws SDKException {
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws).
                set(Pydio.PARAM_ACTION, Pydio.ACTION_MOVE);

        int count = 0;
        for (String file : files) {
            params.set(Pydio.PARAM_FILE + "_" + count, file);
        }
        params.set(Pydio.PARAM_DEST, dstFolder).set(Pydio.PARAM_FORCE_COPY_DELETE, "true");

        try {
            return Message.create(transport.getXmlContent(Pydio.ACTION_MOVE, params.get()));
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to move files", e);
        }
    }

    @Override
    public Message rename(String ws, String srcFile, String dstFile) throws SDKException {
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws).
                set(Pydio.PARAM_FILE, srcFile);

        if (dstFile.contains("/")) {
            params.set(Pydio.PARAM_DEST, dstFile);
        } else {
            params.set(Pydio.PARAM_FILENAME_NEW, dstFile);
        }

        try {
            return Message.create(transport.getXmlContent(Pydio.ACTION_RENAME, params.get()));
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to rename file", e);
        }
    }

    @Override
    public Message copy(String ws, String[] files, String folder) throws SDKException {
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws);

        int count = 0;
        for (String file : files) {
            params.set(Pydio.PARAM_FILE + "_" + count, file);
        }
        params.set(Pydio.PARAM_DEST, folder);

        try {
            return Message.create(transport.getXmlContent(Pydio.ACTION_COPY, params.get()));
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to copy files", e);
        }
    }

    @Override
    public Message mkdir(String ws, String parent, String name) throws SDKException {
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws).
                set(Pydio.PARAM_DIR, parent).
                set(Pydio.PARAM_DIRNAME, name);

        try {
            return Message.create(transport.getXmlContent(Pydio.ACTION_MKDIR, params.get()));
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), String.format("failed to create dir %s", name), e);
        }
    }

    @Override
    public InputStream previewData(String ws, String file) throws SDKException {
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws)
                .set(Pydio.PARAM_FILE, file)
                .set(Pydio.PARAM_GET_THUMB, "true");

        String action = file.endsWith(".pdf") ? Pydio.ACTION_IMAGICK_DATA_PROXY : Pydio.ACTION_PREVIEW_DATA_PROXY;

        HttpResponse response;
        try {
            response = transport.getResponse(action, params.get());
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to get preview data stream", e);
        }

        if (response != null) {
            String h = response.getHeaders("Content-Type").get(0);
            if (h.toLowerCase().contains("image")) {
                try {
                    return response.getEntity().getContent();
                } catch (IOException e) {
                    throw new SDKException(transport.requestStatus(), "failed to get preview data stream", e);
                }
            }
        }
        throw new SDKException(transport.requestStatus(), "no response from server", null);
    }

    @Override
    public String streamingAudioURL(String ws, String file) throws SDKException {
        loginIfRequire();
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws).
                set(Pydio.PARAM_SECURE_TOKEN, transport.mSecureToken).
                set(Pydio.PARAM_FILE, file).
                set("rich_preview", "true");
        try {
            return transport.getGETUrl(Pydio.ACTION_AUDIO_PROXY, params.get());
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failde to get audio streaming URL", e);
        }
    }

    @Override
    public String streamingVideoURL(String ws, String file) throws SDKException {
        loginIfRequire();
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws).
                set(Pydio.PARAM_SECURE_TOKEN, transport.mSecureToken).
                set(Pydio.PARAM_FILE, file);
        try {
            return transport.getGETUrl(Pydio.ACTION_READ_VIDEO_DATA, params.get());
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failde to get audio streaming URL", e);
        }
    }


    @Override
    public JSONObject stats(String ws, String file, boolean withHash) throws SDKException {
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws);
        params.set(Pydio.PARAM_FILE, file);

        String action = Pydio.ACTION_STATS;
        if (withHash) {
            action += "_hash";
        }

        HttpResponse r = null;
        try {
            r = transport.getResponse(action, params.get());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (r == null) return null;
        String h = r.getHeaders("Content-Type").get(0);
        if (!"application/json".equals(h.toLowerCase())) {
            throw SDKException.unexpectedContent(new IOException(String.format("wrong response content type: %s", h)));
        }

        InputStream is;
        BufferedReader br;
        try {
            is = r.getEntity().getContent();
            br = new BufferedReader(new InputStreamReader(is, "utf-8"));
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to get file info", e);
        }

        try {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            if (sb.length() == 0) return null;
            String text = sb.toString();
            return new JSONObject(text);
        } catch (Exception e) {
            throw SDKException.unexpectedContent(e);
        }
    }

    @Override
    public long changes(String ws, String filter, int seq, boolean flatten, ChangeProcessor processor) throws SDKException {
        int result_seq = 0;
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws).set(Pydio.PARAM_CHANGE_SEQ_ID, seq + "");

        if (filter != null) {
            params.set(Pydio.PARAM_CHANGE_FILTER, filter);
        } else {
            params.set(Pydio.PARAM_CHANGE_FILTER, "/");
        }
        params.set(Pydio.PARAM_CHANGE_FLATTEN, String.valueOf(flatten)).
                set(Pydio.PARAM_CHANGE_STREAM, "true");

        String action = Pydio.ACTION_CHANGES;

        HttpResponse r;
        InputStream is;

        try {
            r = transport.getResponse(action, params.get());
            is = r.getEntity().getContent();

        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to get changes events", e);
        }

        String h = r.getHeaders("Content-Type").get(0);
        if (!h.toLowerCase().contains("application/json")) {
            io.close(is);
            throw SDKException.unexpectedContent(new IOException(r.toString()));
        }

        Scanner sc = new Scanner(is, "UTF-8");
        sc.useDelimiter("\\n");
        String line = sc.nextLine();

        while (!line.toLowerCase().startsWith("last_seq")) {
            final String[] change = new String[11];
            while (!line.endsWith("}}")) {
                line += sc.nextLine();
            }

            JSONObject json = null;
            try {
                json = new JSONObject(line);
            } catch (ParseException e) {
                throw SDKException.unexpectedContent(e);
            } finally {
                io.close(is);
            }

            change[Pydio.CHANGE_INDEX_SEQ] = json.getString(Pydio.CHANGE_SEQ);
            result_seq = Math.max(result_seq, Integer.parseInt(change[Pydio.CHANGE_INDEX_SEQ]));
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
            line = sc.nextLine();
        }

        if (line.toLowerCase().startsWith("last_seq")) {
            result_seq = Integer.parseInt(line.split(":")[1]);
        }
        return Math.max(seq, result_seq);
    }

    @Override
    public String share(String ws, String file, String ws_label, boolean isFolder, String ws_description, String password, int expiration, int download, boolean canPreview, boolean canDownload) throws SDKException {
        String action = Pydio.ACTION_SHARE;
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws)
                .set(Pydio.PARAM_SUB_ACTION, Pydio.ACTION_CREATE_MINISITE)
                .set(Pydio.PARAM_FILE, file)
                .set(Pydio.PARAM_SHARE_ELEMENT_TYPE, isFolder ? "folder" : "file")
                .set(Pydio.PARAM_CREATE_GUEST_USER, "true")
                .set(Pydio.PARAM_SHARE_EXPIRATION, String.valueOf(expiration))
                .set(Pydio.PARAM_SHARE_DOWNLOAD, String.valueOf(download))
                .set(Pydio.PARAM_SHARE_WORKSPACE_LABEL, ws_label);

        if (password != null && !"".equals(password)) {
            params.set(Pydio.PARAM_SHARE_GUEST_USER_PASSWORD, password);
        }

        if (canPreview) {
            params.set(Pydio.PARAM_RIGHT_PREVIEW, "on");
        } else {
            params.set(Pydio.PARAM_MINISITE_LAYOUT, "ajxp_unique_dl");
        }

        if (canDownload) {
            params.set(Pydio.PARAM_RIGHT_DOWNLOAD, "on");
        } else {
            params.set(Pydio.PARAM_MINISITE_LAYOUT, "ajxp_unique_strip");
        }


        if (ws_description != null) {
            params.set(Pydio.PARAM_SHARE_WORKSPACE_DESCRIPTION, ws_description);
        }

        try {
            return transport.getStringContent(action, params.get());
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to get share link", e);
        }
    }

    @Override
    public void unshare(String ws, String file) throws SDKException {
        String action = Pydio.ACTION_UNSHARE;
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws)
                .set(Pydio.PARAM_FILE, file);
        try {
            transport.getResponse(action, params.get());
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to delete share link", e);
        }
    }

    @Override
    public JSONObject shareInfo(String ws, String file) throws SDKException {
        String action = Pydio.ACTION_LOAD_SHARED_ELEMENT_DATA;
        Params params = Params.create(Pydio.PARAM_TEMP_WORKSPACE, ws)
                .set(Pydio.PARAM_FILE, file)
                .set(Pydio.PARAM_FILE, file);
        String res = null;
        try {
            res = transport.getStringContent(action, params.get());
        } catch (IOException e) {
            throw new SDKException(transport.requestStatus(), "failed to get sharing link info", e);
        }

        try {
            return new JSONObject(res);
        } catch (ParseException e) {
            throw SDKException.unexpectedContent(e);
        }
    }

    @Override
    public InputStream getCaptcha() throws SDKException {
        try {
            return transport.loadCaptcha();
        } catch (IOException e) {
            return null;
        }
    }
}
