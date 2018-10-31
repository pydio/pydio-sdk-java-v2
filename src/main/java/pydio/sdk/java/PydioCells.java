package pydio.sdk.java;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.ChangeServiceApi;
import io.swagger.client.api.FrontendServiceApi;
import io.swagger.client.api.JobsServiceApi;
import io.swagger.client.api.ShareServiceApi;
import io.swagger.client.api.TreeServiceApi;
import io.swagger.client.model.RestBulkMetaResponse;
import io.swagger.client.model.RestChangeCollection;
import io.swagger.client.model.RestChangeRequest;
import io.swagger.client.model.RestCreateNodesRequest;
import io.swagger.client.model.RestDeleteNodesRequest;
import io.swagger.client.model.RestFrontSessionRequest;
import io.swagger.client.model.RestFrontSessionResponse;
import io.swagger.client.model.RestGetBulkMetaRequest;
import io.swagger.client.model.RestNodesCollection;
import io.swagger.client.model.RestPutShareLinkRequest;
import io.swagger.client.model.RestShareLink;
import io.swagger.client.model.RestShareLinkAccessType;
import io.swagger.client.model.RestUserJobRequest;
import io.swagger.client.model.TreeNode;
import io.swagger.client.model.TreeNodeType;
import io.swagger.client.model.TreeSyncChange;
import pydio.sdk.java.core.errors.SDKException;
import pydio.sdk.java.core.model.FileNode;
import pydio.sdk.java.core.model.Message;
import pydio.sdk.java.core.model.ServerNode;
import pydio.sdk.java.core.security.Passwords;
import pydio.sdk.java.core.utils.ChangeProcessor;
import pydio.sdk.java.core.utils.NodeHandler;
import pydio.sdk.java.core.utils.Pydio;
import pydio.sdk.java.core.utils.RegistryItemHandler;
import pydio.sdk.java.core.utils.RegistrySaxHandler;
import pydio.sdk.java.core.utils.ServerGeneralRegistrySaxHandler;
import pydio.sdk.java.core.utils.Token;
import pydio.sdk.java.core.utils.TransferProgressListener;
import pydio.sdk.java.core.utils.WorkspaceNodeSaxHandler;
import pydio.sdk.java.utils.io;

public class PydioCells implements Client {

    public String URL;
    private String apiURL;
    protected String JWT;
    private String user;
    private ServerNode serverNode;

    private Token.Provider tokenProvider;
    private Token.Store tokenStore;

    public PydioCells(ServerNode node) {
        this.serverNode = node;
        this.URL = node.url();
        this.apiURL = node.apiURL();
    }

    protected void getJWT() throws SDKException {
        ApiClient apiClient = getApiClient();

        Token t = null;
        String subject = String.format("%s@%s", user, serverNode.url());
        if (tokenProvider != null) {
            t = tokenProvider.get(subject);
        }

        if (t == null || t.isNotValid()) {
            String password = Passwords.load(serverNode.url(), user);
            if (password == null) {
                throw new SDKException(400, "no password provided", null);
            }

            RestFrontSessionRequest request = new RestFrontSessionRequest();
            request.setClientTime((int) System.currentTimeMillis());

            Map<String, String> authInfo = new HashMap<>();
            authInfo.put("login", this.user);
            authInfo.put("password", password);
            authInfo.put("type", "credentials");
            request.authInfo(authInfo);

            FrontendServiceApi api = new FrontendServiceApi(apiClient);
            RestFrontSessionResponse response;
            try {
                response = api.frontSession(request);
            } catch (ApiException e) {
                throw new SDKException(e.getCode(), e.getResponseBody(), e);
            }

            t = new Token();
            t.subject = subject;
            t.value = response.getJWT();
            long expireIn = (long) response.getExpireTime();
            t.expiry = System.currentTimeMillis() + expireIn * 1000;

            if (this.tokenStore != null) {
                this.tokenStore.set(t);
            }
        }
        this.JWT = t.value;
    }

    private ApiClient getApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(apiURL);
        if (this.serverNode.HttpSecureContext != null) {
            OkHttpClient c = apiClient.getHttpClient();
            c.setSslSocketFactory(this.serverNode.HttpSecureContext.getSocketFactory());
            c.setHostnameVerifier((s, sslSession) -> URL.contains(s));
        }
        return apiClient;
    }

    protected String fullPath(String ws, String file) {
        String fullPath = "";
        fullPath += ws;
        return (fullPath + file).replace("//", "/");
    }

    private FileNode toFileNode(String ws, TreeNode node) {
        FileNode result = new FileNode();

        Map<String, String> meta = node.getMetaStore();
        result.setProperty(Pydio.NODE_PROPERTY_META_JSON_ENCODED, new JSONObject(meta).toString());
        boolean isFile = node.getType() == TreeNodeType.LEAF;
        String isImage = meta.get("is_image") == null ? "false" : meta.get("is_image");
        String ws_shares = meta.get("workspaces_shares");
        if (ws_shares != null) {
            result.setProperty(Pydio.NODE_PROPERTY_AJXP_SHARED, "true");
            result.setProperty(Pydio.NODE_PROPERTY_SHARE_JSON_INFO, ws_shares);
            try {
                JSONArray shareWorkspaces = new JSONArray(ws_shares);
                JSONObject shareWs = (JSONObject) shareWorkspaces.get(0);
                String shareUUID = shareWs.getString("UUID");
                result.setProperty(Pydio.NODE_PROPERTY_SHARE_UUID, shareUUID);
            } catch (ParseException ignored) {
            }
        }
        String uuid = node.getUuid();
        if (uuid == null){
            return null;
        }
        result.setProperty(Pydio.NODE_PROPERTY_UUID, node.getUuid());
        result.setProperty(Pydio.NODE_PROPERTY_TEXT, new File(node.getPath()).getName());
        result.setProperty(Pydio.NODE_PROPERTY_LABEL, new File(node.getPath()).getName());
        String nodeSize = node.getSize();
        if (nodeSize == null) {
            if (!isFile) {
                result.setProperty(Pydio.NODE_PROPERTY_BYTESIZE, "4096");
            }
        } else {
            result.setProperty(Pydio.NODE_PROPERTY_BYTESIZE, node.getSize());
        }
        result.setProperty(Pydio.NODE_PROPERTY_PATH, node.getPath().replaceFirst(ws, ""));
        result.setProperty(Pydio.NODE_PROPERTY_FILENAME, node.getPath().replaceFirst(ws, ""));
        result.setProperty(Pydio.NODE_PROPERTY_IS_FILE, String.valueOf(isFile));
        result.setProperty(Pydio.NODE_PROPERTY_IS_IMAGE, isImage);
        result.setProperty(Pydio.NODE_PROPERTY_FILE_PERMS, String.valueOf(node.getMode()));
        result.setProperty(Pydio.NODE_PROPERTY_AJXP_MODIFTIME, node.getMtime());
        if (isImage.equals("true")) {
            result.setProperty(Pydio.NODE_PROPERTY_IMAGE_HEIGHT, meta.get("image_height"));
            result.setProperty(Pydio.NODE_PROPERTY_IMAGE_WIDTH, meta.get("image_height"));
            try {
                JSONObject thumb = new JSONObject(meta.get("ImageThumbnails"));
                boolean processing = thumb.getBoolean("Processing");
                if (!processing) {
                    JSONArray details = thumb.getJSONArray("thumbnails");
                    JSONObject thumbObject = new JSONObject();
                    for (int i = 0; i < details.length(); i++) {
                        JSONObject item = (JSONObject) details.get(i);
                        int size = item.getInt("size");
                        String format = item.getString("format");
                        String url = item.getString("url");
                        String thumbPath = !"".equals(url) ? url : "/" + node.getUuid() + "-" + size + "." + format;
                        thumbObject.put("" + size, thumbPath);
                    }
                    result.setProperty(Pydio.NODE_PROPERTY_IMAGE_THUMB_PATHS, thumbObject.toString());
                }
            } catch (Exception ignored) {
            }
        }

        String encoded = new Gson().toJson(node);
        result.setProperty(Pydio.NODE_PROPERTY_ENCODED, encoded);
        result.setProperty(Pydio.NODE_PROPERTY_ENCODING, "gson");
        return result;
    }

    @Override
    public String getURLString() {
        return serverNode.url();
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public void setTokenProvider(Token.Provider p) {
        this.tokenProvider = p;
    }

    @Override
    public void setTokenStore(Token.Store s) {
        this.tokenStore = s;
    }

    @Override
    public void setServerNode(ServerNode node) {
        this.serverNode = node;
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public InputStream getUserData(String binary) throws SDKException {
        return null;
    }

    @Override
    public void login() throws SDKException {
        if ("".equals(user)) {
            throw new SDKException(Pydio.ERROR_AUTHENTICATION, "no user provided", null);
        }
        this.getJWT();
    }

    @Override
    public void logout() throws SDKException {
        RestFrontSessionRequest request = new RestFrontSessionRequest();
        request.setLogout(true);
        try {
            new FrontendServiceApi(getApiClient()).frontSession(request);
        } catch (ApiException e) {
            throw new SDKException(e);
        }
    }

    @Override
    public X509Certificate[] remoteCertificateChain() {
        return new X509Certificate[0];
    }

    @Override
    public void downloadServerRegistry(RegistryItemHandler itemHandler) throws SDKException {
        String fullURI = this.URL + "a/frontend/state/?ws=login";
        URL url = null;
        try {
            url = new URL(fullURI);
        } catch (MalformedURLException e) {
            throw SDKException.malFormURI(fullURI, e);
        }

        this.getJWT();
        HttpURLConnection con;
        InputStream in;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer " + this.JWT);
            in = con.getInputStream();
        } catch (IOException e) {
            throw SDKException.conFailed(e);
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, new ServerGeneralRegistrySaxHandler(itemHandler));
        } catch (Exception e) {
            throw SDKException.unexpectedContent(e);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }

    @Override
    public void downloadWorkspaceRegistry(String ws, RegistryItemHandler itemHandler) throws SDKException {

        String fullURI = this.URL + "/a/frontend/state/?ws=" + ws;
        URL url;
        try {
            url = new URL(fullURI);
        } catch (MalformedURLException e) {
            throw SDKException.malFormURI(fullURI, e);
        }

        this.getJWT();

        HttpURLConnection con;
        InputStream in;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer " + this.JWT);
            in = con.getInputStream();

        } catch (IOException e) {
            throw SDKException.conFailed(e);
        }


        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, new RegistrySaxHandler(itemHandler));
        } catch (Exception e) {
            e.printStackTrace();
            throw SDKException.unexpectedContent(e);
        } finally {
            io.close(in);
        }
    }

    @Override
    public void workspaceList(NodeHandler handler) throws SDKException {
        URL url = null;
        try {
            url = new URL(this.URL + "/a/frontend/state");
        } catch (MalformedURLException e) {
            throw SDKException.malFormURI(this.URL + "/a/frontend/state", e);
        }

        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
        } catch (IOException e) {
            throw SDKException.conFailed(e);
        }

        this.getJWT();
        con.setRequestProperty("Authorization", "Bearer " + this.JWT);

        InputStream in;
        try {
            in = con.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw SDKException.conFailed(e);
        }

        try {
            DefaultHandler saxHandler = new WorkspaceNodeSaxHandler(handler, 0, -1);
            SAXParserFactory.newInstance().newSAXParser().parse(in, saxHandler);
        } catch (Exception e) {
            throw SDKException.unexpectedContent(e);
        }
    }

    @Override
    public FileNode nodeInfo(String ws, String path) throws SDKException {
        RestGetBulkMetaRequest request = new RestGetBulkMetaRequest();
        request.setAllMetaProviders(true);
        request.addNodePathsItem(fullPath(ws, path));
        RestBulkMetaResponse response;

        this.getJWT();
        ApiClient client = getApiClient();
        client.addDefaultHeader("Authorization", "Bearer " + this.JWT);
        TreeServiceApi api = new TreeServiceApi(client);
        try {
            response = api.bulkStatNodes(request);
        } catch (ApiException e) {
            throw new SDKException(e);
        }

        TreeNode node = response.getNodes().get(0);
        return toFileNode(ws, node);
    }

    @Override
    public FileNode ls(String ws, String folder, NodeHandler handler) throws SDKException {
        RestGetBulkMetaRequest request = new RestGetBulkMetaRequest();
        //request.addNodePathsItem(fullPath(ws, folder));
        if("/".equals(folder)) {
            request.addNodePathsItem(ws + "/*");
        } else {
            request.addNodePathsItem(fullPath(ws, folder + "/*"));
        }

        request.setAllMetaProviders(true);

        this.getJWT();
        ApiClient client = getApiClient();
        client.addDefaultHeader("Authorization", "Bearer " + this.JWT);
        TreeServiceApi api = new TreeServiceApi(client);
        RestBulkMetaResponse response;
        try {
            response = api.bulkStatNodes(request);
        } catch (ApiException e) {
            throw new SDKException(e);
        }

        FileNode result = null;
        List<TreeNode> nodes = response.getNodes();
        if(nodes != null){
            for (TreeNode node : response.getNodes()) {
                FileNode fileNode;
                try {
                     fileNode = toFileNode(ws, node);
                } catch (NullPointerException ignored){
                    continue;
                }

                if(fileNode != null){
                    String nodePath = ("/" + node.getPath()).replace("//", "/");
                    if (nodePath.equals(fullPath(ws, folder))) {
                        result = fileNode;
                    } else if (!fileNode.label().startsWith(".")) {
                        handler.onNode(fileNode);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void search(String ws, String pattern, NodeHandler h) throws SDKException {

    }

    @Override
    public Message upload(InputStream source, long length, String ws, String path, String name, boolean autoRename, TransferProgressListener progressListener) throws SDKException {
        return null;
    }

    @Override
    public Message upload(File source, String ws, String path, String name, boolean autoRename, TransferProgressListener progressListener) throws SDKException {
        return null;
    }

    @Override
    public String uploadURL(String ws, String folder, String name, boolean autoRename) throws SDKException{
        return null;
    }

    @Override
    public long download(String ws, String file, OutputStream target, TransferProgressListener progressListener) throws SDKException {
        return 0;
    }

    @Override
    public long download(String ws, String file, File target, TransferProgressListener progressListener) throws SDKException {
        OutputStream out;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw SDKException.notFound(e);
        }

        long downloaded = download(ws, file, out, progressListener);
        io.close(out);
        return downloaded;
    }


    @Override
    public Message delete(String ws, String[] files) throws SDKException {
        List<TreeNode> nodes = new ArrayList<>();
        for (String file: files){
            TreeNode node = new TreeNode();
            node.setPath(fullPath(ws, file));
            nodes.add(node);
        }

        RestDeleteNodesRequest request = new RestDeleteNodesRequest();
        request.setNodes(nodes);

        this.getJWT();

        ApiClient client = getApiClient();
        client.addDefaultHeader("Authorization", "Bearer " + this.JWT);
        TreeServiceApi api = new TreeServiceApi(client);

        try {
            api.deleteNodes(request);
        } catch (ApiException e) {
            e.printStackTrace();
            throw new SDKException(e);
        }
        return null;
    }

    @Override
    public Message restore(String ws, String[] files) throws SDKException {
        return null;
    }

    @Override
    public Message rename(String ws, String srcFile, String newName) throws SDKException {
        RestUserJobRequest request = new RestUserJobRequest();

        JSONArray nodes = new JSONArray();
        String path = fullPath(ws, srcFile);
        nodes.put(path);

        String parent = new File(srcFile).getParentFile().getPath();
        String dstFile;
        if("/".equals(parent)) {
            dstFile = parent + newName;
        } else {
            dstFile = parent + "/" + newName;
        }

        JSONObject o = new JSONObject();
        o.put("nodes", nodes);
        o.put("target", fullPath(ws, dstFile));
        o.put("targetParent", false);

        request.setJobName("move");
        request.setJsonParameters(o.toString());

        this.getJWT();
        ApiClient client = getApiClient();
        client.addDefaultHeader("Authorization", "Bearer " + this.JWT);
        JobsServiceApi api = new JobsServiceApi(client);
        try {
            api.userCreateJob("move", request);
        } catch (ApiException e) {
            e.printStackTrace();
            throw new SDKException(e);
        }
        return null;
    }

    @Override
    public Message move(String ws, String[] files, String dstFolder) throws SDKException {
        JSONArray nodes = new JSONArray();
        for (String file : files) {
            String path = "/" + ws + file;
            nodes.put(path);
        }

        JSONObject o = new JSONObject();
        o.put("nodes", nodes);
        o.put("target", "/" + ws + dstFolder);
        o.put("targetParent", true);

        RestUserJobRequest request = new RestUserJobRequest();
        request.setJobName("move");
        request.setJsonParameters(o.toString());

        this.getJWT();
        ApiClient client = getApiClient();
        client.addDefaultHeader("Authorization", "Bearer " + this.JWT);
        JobsServiceApi api = new JobsServiceApi(client);
        try {
            api.userCreateJob("move", request);
        } catch (ApiException e) {
            e.printStackTrace();
            throw new SDKException(e);
        }
        return null;
    }

    @Override
    public Message copy(String ws, String[] files, String folder) throws SDKException {
        JSONArray nodes = new JSONArray();
        for (String file : files) {
            String path = "/" + ws + file;
            nodes.put(path);
        }

        JSONObject o = new JSONObject();
        o.put("nodes", nodes);
        o.put("target", "/" + ws + folder);
        o.put("targetParent", true);

        RestUserJobRequest request = new RestUserJobRequest();
        request.setJobName("copy");
        request.setJsonParameters(o.toString());


        this.getJWT();
        ApiClient client = getApiClient();
        client.addDefaultHeader("Authorization", "Bearer " + this.JWT);
        JobsServiceApi api = new JobsServiceApi(client);
        try {
            api.userCreateJob("copy", request);
        } catch (ApiException e) {
            e.printStackTrace();
            throw new SDKException(e);
        }
        return null;
    }

    @Override
    public Message mkdir(String ws, String parent, String name) throws SDKException {
        TreeNode node = new TreeNode();
        node.setPath((ws + parent + "/" + name).replace("//", "/"));
        node.setType(TreeNodeType.COLLECTION);

        RestCreateNodesRequest request = new RestCreateNodesRequest();
        request.recursive(false);
        request.addNodesItem(node);


        this.getJWT();
        ApiClient client = getApiClient();
        client.addDefaultHeader("Authorization", "Bearer " + this.JWT);
        TreeServiceApi api = new TreeServiceApi(client);

        RestNodesCollection response;
        try {
            response = api.createNodes(request);
        } catch (ApiException e) {
            e.printStackTrace();
            throw new SDKException(e);
        }

        Message msg = new Message();
        msg.added = new ArrayList<>();

        List<TreeNode> nodes = response.getChildren();
        node = nodes.get(0);

        FileNode fileNode = toFileNode(ws, node);
        msg.added.add(fileNode);
        return msg;
    }

    //@Override
    public Message mkfile(String ws, String name, String folder) throws SDKException {

        TreeNode node = new TreeNode();
        node.setPath("/" + ws + folder + "/" + name);
        node.setType(TreeNodeType.LEAF);

        RestCreateNodesRequest request = new RestCreateNodesRequest();
        request.recursive(false);
        request.addNodesItem(node);

        this.getJWT();
        ApiClient client = getApiClient();
        client.addDefaultHeader("Authorization", "Bearer " + this.JWT);
        TreeServiceApi api = new TreeServiceApi(client);

        RestNodesCollection response;
        try {
            response = api.createNodes(request);
        } catch (ApiException e) {
            e.printStackTrace();
            throw new SDKException(e);
        }

        Message msg = new Message();
        msg.added = new ArrayList<>();

        List<TreeNode> nodes = response.getChildren();
        node = nodes.get(0);

        FileNode fileNode = toFileNode(ws, node);
        msg.added.add(fileNode);
        return msg;
    }

    @Override
    public InputStream previewData(String ws, String file) throws SDKException {
        return null;
    }

    @Override
    public String streamingAudioURL(String ws, String file) throws SDKException {
        return null;
    }

    @Override
    public String streamingVideoURL(String ws, String file) throws SDKException {
        return null;
    }

    @Override
    public String downloadURL(String ws, String file) throws SDKException {
        return null;
    }

    @Override
    public JSONObject stats(String ws, String file, boolean withHash) throws SDKException {
        RestGetBulkMetaRequest request = new RestGetBulkMetaRequest();
        request.addNodePathsItem(fullPath(ws, file));
        request.setAllMetaProviders(true);

        this.getJWT();
        ApiClient client = getApiClient();
        client.addDefaultHeader("Authorization", "Bearer " + this.JWT);
        TreeServiceApi api = new TreeServiceApi(client);
        RestBulkMetaResponse response;
        try {
            response = api.bulkStatNodes(request);
        } catch (ApiException e) {
            e.printStackTrace();
            throw new SDKException(e);
        }

        JSONObject stat = null;
        if (response.getNodes() != null) {
            TreeNode node = response.getNodes().get(0);
            stat = new JSONObject();
            stat.put("hash", node.getEtag());
            stat.put("size", Long.parseLong(node.getSize()));
            stat.put("mtime", Long.parseLong(node.getMtime()));
        }
        return stat;
    }

    @Override
    public long changes(String ws, String folder, int seq, boolean flatten, ChangeProcessor cp) throws SDKException {
        RestChangeRequest request = new RestChangeRequest();
        request.setFlatten(flatten);
        request.setSeqID(String.valueOf(seq));
        request.setFilter("/" + ws + folder);


        this.getJWT();
        ApiClient client = getApiClient();
        client.addDefaultHeader("Authorization", "Bearer " + this.JWT);
        ChangeServiceApi api = new ChangeServiceApi(client);
        RestChangeCollection response;

        try {
            response = api.getChanges(String.valueOf(seq), request);
        } catch (ApiException e) {
            throw new SDKException(e);
        }
        for (TreeSyncChange c : response.getChanges()) {
            final String[] change = new String[11];
            change[Pydio.CHANGE_INDEX_SEQ] = c.getSeq();
            change[Pydio.CHANGE_INDEX_NODE_ID] = c.getNodeId();
            change[Pydio.CHANGE_INDEX_TYPE] = c.getType().toString();
            change[Pydio.CHANGE_INDEX_SOURCE] = c.getSource();
            change[Pydio.CHANGE_INDEX_TARGET] = c.getTarget();
            change[Pydio.CHANGE_INDEX_NODE_BYTESIZE] = c.getNode().getBytesize();
            change[Pydio.CHANGE_INDEX_NODE_MD5] = c.getNode().getMd5();
            change[Pydio.CHANGE_INDEX_NODE_MTIME] = c.getNode().getMtime();
            change[Pydio.CHANGE_INDEX_NODE_PATH] = c.getNode().getNodePath().replaceFirst("/" + ws, "");
            change[Pydio.CHANGE_INDEX_NODE_WORKSPACE] = ws;
            change[10] = "remote";
            cp.process(change);
        }
        return Long.parseLong(response.getLastSeqId());
    }

    @Override
    public String share(String ws, String file, String ws_label, boolean isFolder, String ws_description, String password, int expiration, int download, boolean canPreview, boolean canDownload) throws SDKException {
        RestPutShareLinkRequest request = new RestPutShareLinkRequest();
        request.createPassword(password);
        request.setCreatePassword(password);
        request.setPasswordEnabled(Boolean.parseBoolean(password));
        RestShareLink sl = new RestShareLink();

        TreeNode n = new TreeNode();
        n.setUuid(file);

        List<RestShareLinkAccessType> permissions = new ArrayList<>();
        if (canPreview) {
            permissions.add(RestShareLinkAccessType.PREVIEW);
        }
        if (canDownload) {
            permissions.add(RestShareLinkAccessType.DOWNLOAD);
        }

        List<TreeNode> rootNodes = new ArrayList<>();
        rootNodes.add(n);
        sl.setPoliciesContextEditable(true);

        sl.setPermissions(permissions);
        sl.setRootNodes(rootNodes);
        sl.setPoliciesContextEditable(true);
        sl.setDescription(ws_description);
        sl.setLabel(ws_label);
        sl.setViewTemplateName("pydio_unique_strip");
        request.setShareLink(sl);

        this.getJWT();
        ApiClient client = getApiClient();
        client.addDefaultHeader("Authorization", "Bearer " + this.JWT);
        ShareServiceApi api = new ShareServiceApi(client);

        try {
            RestShareLink link = api.putShareLink(request);
            return link.getLinkUrl();
        } catch (ApiException e) {
            throw new SDKException(e);
        }
    }

    @Override
    public void unshare(String ws, String file) throws SDKException {
        this.getJWT();
        ApiClient client = getApiClient();
        client.addDefaultHeader("Authorization", "Bearer " + this.JWT);
        ShareServiceApi api = new ShareServiceApi(client);
        try {
            api.deleteShareLink(file);
        } catch (ApiException e) {
            throw new SDKException(e);
        }
    }

    @Override
    public JSONObject shareInfo(String ws, String shareID) throws SDKException {
        ApiClient client = getApiClient();
        client.addDefaultHeader("Authorization", "Bearer " + this.JWT);
        ShareServiceApi api = new ShareServiceApi(client);
        try {
            RestShareLink link = api.getShareLink(shareID);
            Gson gs = new Gson();
            String jsonString = gs.toJson(link);
            return new JSONObject(jsonString);
        } catch (ApiException e) {
            throw new SDKException(e);
        } catch (ParseException e) {
            throw SDKException.unexpectedContent(e);
        }
    }

    @Override
    public InputStream getCaptcha() throws SDKException {
        return null;
    }

    public interface Factory {
        PydioCells get(ServerNode node);
    }

    private static Map<String, Factory> registry = new HashMap<>();

    public static void registerFactory(String name, Factory f) {
        registry.put(name, f);
    }

    public static void registerFactory(Factory f) {
        registry.put("default", f);
    }

    public static Factory getFactory(String name) {
        if (registry.containsKey(name)) {
            return registry.get(name);
        }
        return registry.get("default");
    }

    public static Factory getFactory() {
        return registry.get("default");
    }

}
