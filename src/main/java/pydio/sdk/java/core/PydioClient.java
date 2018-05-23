package pydio.sdk.java.core;


import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.Configuration;
import io.swagger.client.api.ShareServiceApi;
import io.swagger.client.model.RestDeleteShareLinkResponse;
import io.swagger.client.model.RestPutShareLinkRequest;
import io.swagger.client.model.RestShareLink;
import io.swagger.client.model.RestShareLinkAccessType;
import io.swagger.client.model.TreeNode;
import pydio.sdk.java.core.http.ContentBody;
import pydio.sdk.java.core.http.HttpResponse;
import pydio.sdk.java.core.model.Change;
import pydio.sdk.java.core.model.FileNode;
import pydio.sdk.java.core.model.Node;
import pydio.sdk.java.core.model.NodeDiff;
import pydio.sdk.java.core.model.NodeFactory;
import pydio.sdk.java.core.model.PydioMessage;
import pydio.sdk.java.core.model.ServerNode;
import pydio.sdk.java.core.model.WorkspaceNode;
import pydio.sdk.java.core.security.CertificateTrust;
import pydio.sdk.java.core.security.Passwords;
import pydio.sdk.java.core.transport.SessionTransport;
import pydio.sdk.java.core.transport.Transport;
import pydio.sdk.java.core.transport.TransportFactory;
import pydio.sdk.java.core.utils.BucketUploadListener;
import pydio.sdk.java.core.utils.ChangeHandler;
import pydio.sdk.java.core.utils.ChangeProcessor;
import pydio.sdk.java.core.utils.Filter;
import pydio.sdk.java.core.utils.HttpResponseParser;
import pydio.sdk.java.core.utils.MessageHandler;
import pydio.sdk.java.core.utils.NodeDiffHandler;
import pydio.sdk.java.core.utils.NodeHandler;
import pydio.sdk.java.core.utils.PasswordLoader;
import pydio.sdk.java.core.utils.ProgressListener;
import pydio.sdk.java.core.utils.Pydio;
import pydio.sdk.java.core.utils.RegistryItemHandler;
import pydio.sdk.java.core.utils.ServerGeneralRegistrySaxHandler;
import pydio.sdk.java.core.utils.TreeNodeSaxHandler;
import pydio.sdk.java.core.utils.UnexpectedResponseException;
import pydio.sdk.java.core.utils.UploadStopNotifierProgressListener;
import pydio.sdk.java.core.utils.WorkspaceNodeSaxHandler;

/**
 * @author pydio
 */
public class PydioClient implements Serializable {

    public SessionTransport sessionTransport;

    public ServerNode server;
    protected WorkspaceNode mWorkspace;
    private JSONObject bootConf;
    private String JWT;
    private long JWTExpirationTime = -1;
    private ApiClient apiClient;
    private SSLContext mApiSSLContext;
    public boolean isPydioCells;

    //*****************************************
    //         INITIALIZATION METHODS
    //*****************************************
    public PydioClient(String url) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        server = (ServerNode) NodeFactory.createNode(Node.TYPE_SERVER);
        server.init(url);
        sessionTransport = (SessionTransport) TransportFactory.getInstance(Transport.MODE_SESSION, server);
    }

    public PydioClient(String url, final String login, final String password) {

        if (!url.endsWith("/")) {
            url += "/";
        }
        final String normalizedUrl = url;

        server = (ServerNode) NodeFactory.createNode(Node.TYPE_SERVER);
        server.init(url, new CertificateTrust.Helper() {
            @Override
            public boolean isServerTrusted(X509Certificate[] chain) {
                return true;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        });
        Passwords.Loader = new PasswordLoader() {
            @Override
            public String loadPassword(String server, String user) {
                if (normalizedUrl.equals(server) && login.equals(user)) {
                    return password;
                }
                return null;
            }
        };
        sessionTransport = (SessionTransport) TransportFactory.getInstance(Transport.MODE_SESSION, server);
        sessionTransport.mUser = login;
    }

    public PydioClient(ServerNode node) {
        server = node;
        sessionTransport = (SessionTransport) TransportFactory.getInstance(Transport.MODE_SESSION, node);
    }

    public void setUser(String user) {
        sessionTransport.mUser = user;
    }

    public void setCookieManager(CookieManager cm) {
        sessionTransport.setCookieManager(cm);
    }

    private SSLContext sslContext() throws IOException {
        if(mApiSSLContext != null) return mApiSSLContext;
        try {
            mApiSSLContext = SSLContext.getInstance("TLS");
            CertificateTrust.Helper helper = server.setSSLUnverified(true).getTrustHelper();
            mApiSSLContext.init(null, new TrustManager[]{new pydio.sdk.java.core.security.CertificateTrustManager(helper)}, null);
            return mApiSSLContext;
        } catch (NoSuchAlgorithmException | KeyManagementException ignored) {}
        throw new IOException("NO TLS");
    }

    private void initApiClient() throws IOException {
        if (apiClient == null) {
            apiClient = new ApiClient();
            apiClient.setBasePath(server.url() + "a");
            Configuration.setDefaultApiClient(apiClient);
        }

        if (server.SSLUnverified()) {
            OkHttpClient c = apiClient.getHttpClient();
            c.setSslSocketFactory(sslContext().getSocketFactory());
            c.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return server.url().contains(s);
                }
            });
        }
    }

    //*****************************************
    //         REMOTE ACTION METHODS
    //*****************************************

    /**
     * Authenticates the user
     *
     * @return true if the authentication succeeded. false if not
     * @see #responseStatus()#logout()
     */
    public boolean login() throws IOException {
        getBootConfig();
        sessionTransport.login();
        return sessionTransport.requestStatus() == Pydio.OK;
    }

    public void loginIfNecessary() throws IOException {
        sessionTransport.getResponse("blablacar", null);
    }

    /**
     * UnAuthenticates the user
     *
     * @return true if the login() succeeded or false if the authen
     * @see #responseStatus()#login()
     */
    public boolean logout() throws IOException {
        String action = Pydio.ACTION_LOGOUT;
        String response = sessionTransport.getStringContent(action, null);
        boolean result = response != null && response.contains("logging_result value=\"2\"");

        if (result) {
            sessionTransport.mSecureToken = "";
        }
        return result;
    }

    /**
     * Retrieve the workspaces
     *
     * @see #responseStatus()#logout()
     */
    public void workspaceList(final NodeHandler handler) throws IOException {
        getBootConfig();
        Map<String, String> params = new HashMap<String, String>();
        String action = Pydio.ACTION_GET_REGISTRY;
        params.put(Pydio.PARAM_XPATH, Pydio.XPATH_USER_WORKSPACES);

        DefaultHandler saxHandler = new WorkspaceNodeSaxHandler(handler, 0, -1);

        //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
        HttpResponse r = sessionTransport.getResponse(action, params);
        try {
            SAXParserFactory
                    .newInstance()
                    .newSAXParser()
                    .parse(r.getEntity().getContent(), saxHandler);
        } catch (Exception e) {
            //Log.e("Pydio", e.getMessage());
            throw new IOException();
        }
    }

    /**
     * Set the default workspace. After this method is called with success, the temporary workspace parameter is not necessary if sending request on workspace with with the same ID
     *
     * @param id the target workspace ID
     * @see #switchWorkspace(String)
     */
    public boolean selectWorkspace(final String id) throws IOException {
        getBootConfig();
        Map<String, String> params = new HashMap<String, String>();
        params.put(Pydio.PARAM_WORKSPACE, id);
        //Log.i("Request", "[action=" + Pydio.ACTION_SWITCH_REPO + //Log.paramString(params) + "]");
        sessionTransport.getResponse(Pydio.ACTION_SWITCH_REPO, params);
        int status = sessionTransport.requestStatus();
        if (status == Pydio.OK) {
            params.clear();
            String action = Pydio.ACTION_GET_REGISTRY;
            params.put(Pydio.PARAM_XPATH, Pydio.XPATH_USER_ACTIVE_WORKSPACE);

            //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
            String content = sessionTransport.getStringContent(action, params);
            return content != null && content.contains("<active_repo id=\"" + id + "\"");
        }
        return false;
    }

    /**
     * Set the default workspace. After this method is called with success, the temporary workspace parameter is not necessary if sending request on workspace with with the same ID
     *
     * @param id the target workspace ID
     * @deprecated use {@link #selectWorkspace(String)}
     */
    public void switchWorkspace(final String id) throws IOException {
        getBootConfig();
        Map<String, String> params = new HashMap<String, String>();
        params.put(Pydio.PARAM_WORKSPACE, id);
        //Log.i("Request", "[action=" + Pydio.ACTION_SWITCH_REPO + //Log.paramString(params) + "]");
        sessionTransport.getResponse(Pydio.ACTION_SWITCH_REPO, params);
        int status = sessionTransport.requestStatus();
        if (status != Pydio.OK) {
            throw new IOException();
        }
    }

    /**
     * Downloads the registry file. The registry content can be different whether the request is authenticated ot nor.
     *
     * @param tempWorkspace the target workspace ID
     * @param out           Stream to write downloaded content in
     * @param workspace     Stream to write downloaded content in
     * @deprecated use {@link #selectWorkspace(String)}
     */
    public void downloadRegistry(String tempWorkspace, OutputStream out, boolean workspace) throws IOException {
        getBootConfig();

        Map<String, String> params = new HashMap<String, String>();
        if (workspace) {
            selectWorkspace(tempWorkspace);
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        } else {
            params.put(Pydio.PARAM_XPATH, "user");
        }
        try {
            //Log.i("Request", "[action=" + Pydio.ACTION_GET_REGISTRY + //Log.paramString(params) + "]");
            InputStream in = sessionTransport.getResponseStream(Pydio.ACTION_GET_REGISTRY, params);
            byte[] buffer = new byte[Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
        } catch (NullPointerException e) {
            throw new IOException("empty response");
        }
    }

    public void serverGeneralRegistry(RegistryItemHandler itemHandler) throws IOException {
        getBootConfig();
        //Log.i("Request", "[action=" + Pydio.ACTION_GET_REGISTRY + "]");
        InputStream in = sessionTransport.getResponseStream(Pydio.ACTION_GET_REGISTRY, null);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        /*FileOutputStream stream = new FileOutputStream("C:\\Users\\jabar\\Desktop\\Putty\\registry.xml");

        byte[] bytes = new byte[16384];
        for(int read; (read = in.read(bytes)) != -1;){
            stream.write(bytes, 0, read);
        }
        stream.close();
        in.close();*/

        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, new ServerGeneralRegistrySaxHandler(itemHandler));

            in.close();
        } catch (Exception e) {
            //Log.e("Pydio", e.getMessage());
            in.close();
            throw new IOException();
        }
    }

    public void userServerRegistry(OutputStream out) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Pydio.PARAM_XPATH, "user");
        try {
            //Log.i("Request", "[action=" + Pydio.ACTION_GET_REGISTRY + //Log.paramString(params) + "]");
            InputStream in = sessionTransport.getResponseStream(Pydio.ACTION_GET_REGISTRY, params);
            byte[] buffer = new byte[Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
        } catch (NullPointerException e) {
            throw new IOException("empty response");
        }
    }

    public void userWorkspaceRegistry(String workspace, OutputStream out) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        selectWorkspace(workspace);
        params.put(Pydio.PARAM_TEMP_WORKSPACE, workspace);

        try {
            //Log.i("Request", "[action=" + Pydio.ACTION_GET_REGISTRY + //Log.paramString(params) + "]");
            InputStream in = sessionTransport.getResponseStream(Pydio.ACTION_GET_REGISTRY, params);
            byte[] buffer = new byte[Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
        } catch (NullPointerException e) {
            throw new IOException("empty response");
        }
    }

    /**
     * Loads the properties of the node located at the specified path
     *
     * @param tempWorkspace The target workspace ID
     * @param path          The path of the node to load
     * @param handler       A delegate to handle the result
     */
    public void loadNodeData(String tempWorkspace, String path, NodeHandler handler) throws IOException {
        DefaultHandler saxHandler = null;
        String action;

        Map<String, String> params = new HashMap<String, String>();

        action = Pydio.ACTION_LIST;
        params.put(Pydio.PARAM_OPTIONS, "al");
        params.put(Pydio.PARAM_FILE, path);
        saxHandler = new TreeNodeSaxHandler(handler);


        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        try {
            //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
            HttpResponse r = sessionTransport.getResponse(action, params);
            InputStream in = r.getEntity().getContent();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, saxHandler);
            return;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (Exception e) {
        }
    }

    /**
     * List content of a directory node.
     *
     * @param tempWorkspace the target workspace ID
     * @param path          The directory which content is to be listed
     * @param handler       A delegate to handle parsed nodes
     * @return The node at the specified path
     */
    public FileNode ls(String tempWorkspace, String path, final NodeHandler handler) throws IOException {
        DefaultHandler saxHandler = null;
        String action;

        Map<String, String> params = new HashMap<String, String>();

        if (path == null) {
            action = Pydio.ACTION_GET_REGISTRY;
            params.put(Pydio.PARAM_XPATH, Pydio.XPATH_USER_WORKSPACES);
            saxHandler = new WorkspaceNodeSaxHandler(handler, 0, -1);
        } else {
            action = Pydio.ACTION_LIST;
            params.put(Pydio.PARAM_OPTIONS, "al");
            params.put(Pydio.PARAM_DIR, path);
            if (tempWorkspace == null) {
                tempWorkspace = mWorkspace.getId();
            }
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
            saxHandler = new TreeNodeSaxHandler(handler);
        }

        while (true) {
            //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
            HttpResponse r = sessionTransport.getResponse(action, params);
            InputStream in = r.getEntity().getContent();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = null;

            try {
                parser = factory.newSAXParser();
                parser.parse(in, saxHandler);
            } catch (Exception e) {
                //Log.e("Pydio", e.getMessage());
                throw new IOException();
            }

            if (saxHandler instanceof TreeNodeSaxHandler) {
                TreeNodeSaxHandler fileHandler = (TreeNodeSaxHandler) saxHandler;
                if (fileHandler.mPagination) {
                    if (!(fileHandler.mPaginationTotalPage == fileHandler.mPaginationCurrentPage)) {
                        params.put(Pydio.PARAM_DIR, path + "%23" + (fileHandler.mPaginationCurrentPage + 1));
                    } else {
                        return fileHandler.mRootNode;
                    }
                } else {
                    if (fileHandler.mRootNode == null) {
                        throw new IOException();
                    }
                    return fileHandler.mRootNode;
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Upload a file on the pydio server
     *
     * @param path             The directory to upload in
     * @param source           The file to be uploaded
     * @param name             The name of the uploaded file
     * @param autoRename       if set to true the file will be renamed if there is a file with the same name
     * @param progressListener A delegate to listen to progress
     * @param handler          A delegate to handle the response message
     */
    public PydioMessage upload(String tempWorkspace, String path, File source, String name, boolean autoRename, final UploadStopNotifierProgressListener progressListener, final NodeDiffHandler handler) throws IOException {
        loginIfNecessary();
        String action;
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        action = Pydio.ACTION_UPLOAD;
        params.put(Pydio.PARAM_DIR, path);
        params.put(Pydio.PARAM_XHR_UPLOADER, "true");

        if (name == null) {
            name = source.getName();
        }
        try {
            String urlEncodedName = java.net.URLEncoder.encode(name, "utf-8");
            params.put(Pydio.PARAM_URL_ENCODED_FILENAME, urlEncodedName);
        } catch (UnsupportedEncodingException e) {
        }

        if (autoRename) {
            params.put(Pydio.PARAM_AUTO_RENAME, "true");
        }

        //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
        if (server.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE) == null) {
            serverGeneralRegistry(new RegistryItemHandler() {
                @Override
                protected void onPref(String name, String value) {
                    super.onPref(name, value);
                    server.setProperty(name, value);
                }
            });
        }
        ContentBody cb = new ContentBody(source, name, Long.parseLong(server.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE)));
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

        HttpResponse response = sessionTransport.putContent(action, params, cb);
        if (response == null) {
            return PydioMessage.create(PydioMessage.EMPTY, "");
        }

        while (response.code() == 200 && !cb.allChunksWritten()) {
            try {
                NodeDiff diff = NodeDiff.create(HttpResponseParser.getXML(response));
                if (diff.added != null) {
                    Node node = diff.added.get(0);
                    String label = node.label();
                    if (!label.equals(cb.getFilename())) {
                        cb.setFilename(label);
                    }
                }
                if (handler != null) {
                    handler.onNodeDiff(diff);
                }
            } catch (NullPointerException ignored) {
            }
            params.put(Pydio.PARAM_APPEND_TO_URLENCODED_PART, cb.getFilename());
            response = sessionTransport.putContent(action, params, cb);
            if (response == null) {
                return PydioMessage.create(PydioMessage.EMPTY, "");
            }
        }
        if (response.code() != 200 || !cb.allChunksWritten()) {
            return PydioMessage.create(PydioMessage.ERROR, "");
        }
        return PydioMessage.create(PydioMessage.SUCCESS, "");
    }

    /**
     * Upload a file on the pydio server
     *
     * @param path      The directory to upload in
     * @param files     The list of paths where are located files to upload
     * @param totalSize The total size of the bulk
     * @param listener  A delegate to listen to progress
     * @param handler   A delegate to handle the response message
     */
    public PydioMessage uploadBucket(String tempWorkspace, String path, final ArrayList<String> files, Long totalSize, Filter<File> filter, final BucketUploadListener listener, final NodeDiffHandler handler) throws IOException {
        loginIfNecessary();
        String action;
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }

        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        action = Pydio.ACTION_UPLOAD;
        params.put(Pydio.PARAM_DIR, path);
        params.put(Pydio.PARAM_XHR_UPLOADER, "true");
        params.put(Pydio.PARAM_AUTO_RENAME, "true");

        if (totalSize == 0) {
            for (int i = 0; i < files.size(); i++) {
                totalSize += new File(files.get(i)).length();
            }
        }


        final int size = files.size();
        for (int i = 0; i < size; i++) {
            if (listener != null) {
                listener.onNext(i, size);
            }

            final File file = new File(files.get(i));
            if (filter != null && filter.isExcluded(file)) {
                continue;
            }

            final String name = file.getName();
            try {
                String urlEncodedName = java.net.URLEncoder.encode(name, "utf-8");
                params.put(Pydio.PARAM_URL_ENCODED_FILENAME, urlEncodedName);
            } catch (UnsupportedEncodingException e) {
            }
            //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");

            if (server.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE) == null) {
                serverGeneralRegistry(new RegistryItemHandler() {
                    @Override
                    protected void onPref(String name, String value) {
                        server.setProperty(name, value);
                    }
                });
            }

            ContentBody cb = new ContentBody(file, name, Long.parseLong(server.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE)));
            final int finalI = i;
            cb.setListener(new ContentBody.ProgressListener() {
                @Override
                public void transferred(long num) throws IOException {
                    if (listener != null && listener.onProgress(name, num, file.length())) {
                        throw new IOException("");
                    }
                }

                @Override
                public void partTransferred(int part, int currentTotal) throws IOException {
                }
            });

            HttpResponse response = sessionTransport.putContent(action, params, cb);
            if (response == null) {
                return PydioMessage.create(PydioMessage.EMPTY, "");
            }

            while (response.code() == 200 && !cb.allChunksWritten()) {
                try {
                    NodeDiff diff = NodeDiff.create(HttpResponseParser.getXML(response));
                    if (diff.added != null) {
                        Node node = diff.added.get(0);
                        String label = node.label();
                        if (!label.equals(cb.getFilename())) {
                            cb.setFilename(label);
                        }
                    }
                    if (handler != null) {
                        handler.onNodeDiff(diff);
                    }
                } catch (NullPointerException ignored) {
                }
                params.put(Pydio.PARAM_APPEND_TO_URLENCODED_PART, cb.getFilename());
                response = sessionTransport.putContent(action, params, cb);
                if (response == null) {
                    return PydioMessage.create(PydioMessage.EMPTY, "");
                }
            }
            if (response.code() != 200 || !cb.allChunksWritten()) {
                return PydioMessage.create(PydioMessage.ERROR, "");
            }
        }
        return PydioMessage.create(PydioMessage.SUCCESS, "");
    }


    public PydioMessage uploadTree(String tempWorkspace, String remoteRoot, String localRoot, ArrayList<String> files, long totalSize, Filter<File> filter, final BucketUploadListener listener, final NodeDiffHandler handler) throws IOException {
        loginIfNecessary();
        String action;

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }

        if (totalSize == 0) {
            for (int i = 0; i < files.size(); i++) {
                totalSize += new File(files.get(i)).length();
            }
        }

        int size = files.size();
        for (int i = 0; i < size; i++) {

            if (listener != null) {
                listener.onNext(i, size);
            }
            String filePath = files.get(i);
            final File file = new File(filePath);
            if (filter != null && filter.isExcluded(file)) {
                continue;
            }

            String currentRemoteRoot = file.getParentFile().getAbsolutePath().replace(localRoot, remoteRoot);

            if (file.isDirectory()) {
                try {
                    mkdir(tempWorkspace, currentRemoteRoot, file.getName(), null);
                } catch (IOException ignored) {
                }

            } else {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
                action = Pydio.ACTION_UPLOAD;
                params.put(Pydio.PARAM_XHR_UPLOADER, "true");
                params.put(Pydio.PARAM_DIR, currentRemoteRoot);
                final String name = file.getName();

                try {
                    String urlEncodedName = java.net.URLEncoder.encode(name, "utf-8");
                    params.put(Pydio.PARAM_URL_ENCODED_FILENAME, urlEncodedName);
                } catch (UnsupportedEncodingException ignored) {
                }

                if (server.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE) == null) {
                    serverGeneralRegistry(new RegistryItemHandler() {
                        @Override
                        protected void onPref(String name, String value) {
                            server.setProperty(name, value);
                        }
                    });
                }

                //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
                if (server.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE) == null) {
                    serverGeneralRegistry(new RegistryItemHandler() {
                        @Override
                        protected void onPref(String name, String value) {
                            super.onPref(name, value);
                            server.setProperty(name, value);
                        }
                    });
                }
                ContentBody cb = new ContentBody(file, name, Long.parseLong(server.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE)));

                if (listener != null) {
                    cb.setListener(new ContentBody.ProgressListener() {
                        @Override
                        public void transferred(long num) throws IOException {
                            if (listener.onProgress(name, num, file.length())) {
                                throw new IOException("");
                            }
                        }

                        @Override
                        public void partTransferred(int part, int currentTotal) throws IOException {
                        }
                    });
                }

                HttpResponse response = sessionTransport.putContent(action, params, cb);
                if (response == null) {
                    return PydioMessage.create(PydioMessage.EMPTY, "");
                }

                while (response.code() == 200 && !cb.allChunksWritten()) {
                    try {
                        NodeDiff diff = NodeDiff.create(HttpResponseParser.getXML(response));
                        if (diff.added != null) {
                            Node node = diff.added.get(0);
                            String label = node.label();
                            if (!label.equals(cb.getFilename())) {
                                cb.setFilename(label);
                            }
                        }
                        if (handler != null) {
                            handler.onNodeDiff(diff);
                        }
                    } catch (NullPointerException ignored) {
                    }
                    params.put(Pydio.PARAM_APPEND_TO_URLENCODED_PART, cb.getFilename());
                    response = sessionTransport.putContent(action, params, cb);
                    if (response == null) {
                        return PydioMessage.create(PydioMessage.EMPTY, "");
                    }
                }
                if (response.code() != 200 || !cb.allChunksWritten()) {
                    return PydioMessage.create(PydioMessage.ERROR, "");
                }
            }
        }
        return PydioMessage.create(PydioMessage.SUCCESS, "");
    }

    /**
     * Upload data in a file at the specified directory
     *
     * @param path             The directory to upload in
     * @param source           The data stream source
     * @param length           The size of the data stream
     * @param name             The name of the uploaded file
     * @param autoRename       if set to true the file will be renamed if there is a file with the same name
     * @param progressListener A delegate to listen to progress
     * @param handler          A delegate to handle the response message
     */
    public PydioMessage upload(String tempWorkspace, String path, InputStream source, long length, String name, boolean autoRename, final UploadStopNotifierProgressListener progressListener, final NodeDiffHandler handler) throws IOException {
        loginIfNecessary();
        String action;
        try {
            JSONObject stats = stats(tempWorkspace, path, false);
            if (stats == null || stats.length() == 0) {
                throw new IOException();
            }
        } catch (UnexpectedResponseException e) {
            //Log.e("Pydio", e.getMessage());
            throw new IOException(e);
        }

        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        action = Pydio.ACTION_UPLOAD;
        params.put(Pydio.PARAM_DIR, path);
        params.put(Pydio.PARAM_XHR_UPLOADER, "true");

        try {
            String urlEncodedName = java.net.URLEncoder.encode(name, "utf-8");
            params.put(Pydio.PARAM_URL_ENCODED_FILENAME, urlEncodedName);
        } catch (UnsupportedEncodingException e) {
        }

        if (autoRename) {
            params.put(Pydio.PARAM_AUTO_RENAME, "true");
        }

        //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
        if (server.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE) == null) {
            serverGeneralRegistry(new RegistryItemHandler() {
                @Override
                protected void onPref(String name, String value) {
                    super.onPref(name, value);
                    server.setProperty(name, value);
                }
            });
        }
        long size = Long.parseLong(server.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE));
        ContentBody cb = new ContentBody(source, name, length, size);
        //ContentBody cb = new ContentBody(source, name, length, Long.parseLong(server.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE)));

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

        HttpResponse response = sessionTransport.putContent(action, params, cb);
        if (response == null) {
            return PydioMessage.create(PydioMessage.EMPTY, "");
        }

        while (response.code() == 200 && !cb.allChunksWritten()) {
            try {
                NodeDiff diff = NodeDiff.create(HttpResponseParser.getXML(response));
                if (diff.added != null) {
                    Node node = diff.added.get(0);
                    String label = node.label();
                    if (!label.equals(cb.getFilename())) {
                        cb.setFilename(label);
                    }
                }
                if (handler != null) {
                    handler.onNodeDiff(diff);
                }
            } catch (NullPointerException ignored) {
            }
            params.put(Pydio.PARAM_APPEND_TO_URLENCODED_PART, cb.getFilename());
            response = sessionTransport.putContent(action, params, cb);
            if (response == null) {
                return PydioMessage.create(PydioMessage.EMPTY, "");
            }
        }

        if (response.code() != 200 || !cb.allChunksWritten()) {
            return PydioMessage.create(PydioMessage.ERROR, "");
        }

        return PydioMessage.create(PydioMessage.SUCCESS, "");
    }

    /**
     * Upload data in a file at the specified directory
     *
     * @param path             The directory to upload in
     * @param source           The byte array to be uploaded
     * @param name             The name of the uploaded file
     * @param autoRename       if set to true the file will be renamed if there is a file with the same name
     * @param progressListener A delegate to listen to progress
     * @param handler          A delegate to handle the response message
     */
    public PydioMessage upload(String tempWorkspace, String path, byte[] source, String name, boolean autoRename, final UploadStopNotifierProgressListener progressListener, final NodeDiffHandler handler) throws IOException {
        loginIfNecessary();
        String action;
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        action = Pydio.ACTION_UPLOAD;
        params.put(Pydio.PARAM_DIR, path);
        params.put(Pydio.PARAM_XHR_UPLOADER, "true");

        try {
            String urlEncodedName = java.net.URLEncoder.encode(name, "utf-8");
            params.put(Pydio.PARAM_URL_ENCODED_FILENAME, urlEncodedName);
        } catch (UnsupportedEncodingException e) {
            //Log.e("URL Encoding", e.getMessage());
            return PydioMessage.create(PydioMessage.ERROR, e.getMessage());
        }

        if (autoRename) {
            params.put(Pydio.PARAM_AUTO_RENAME, "true");
        }

        //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
        if (server.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE) == null) {
            serverGeneralRegistry(new RegistryItemHandler() {
                @Override
                protected void onPref(String name, String value) {
                    super.onPref(name, value);
                    server.setProperty(name, value);
                }
            });
        }
        ContentBody cb = new ContentBody(source, name, Long.parseLong(server.getProperty(Pydio.REMOTE_CONFIG_UPLOAD_SIZE)));
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

        HttpResponse response = sessionTransport.putContent(action, params, cb);
        if (response == null) {
            return PydioMessage.create(PydioMessage.EMPTY, "");
        }

        while (response.code() == 200 && !cb.allChunksWritten()) {
            try {
                NodeDiff diff = NodeDiff.create(HttpResponseParser.getXML(response));
                if (diff.added != null) {
                    Node node = diff.added.get(0);
                    String label = node.label();
                    if (!label.equals(cb.getFilename())) {
                        cb.setFilename(label);
                    }
                }
                if (handler != null) {
                    handler.onNodeDiff(diff);
                }
            } catch (NullPointerException ignored) {
            }
            params.put(Pydio.PARAM_APPEND_TO_URLENCODED_PART, cb.getFilename());
            response = sessionTransport.putContent(action, params, cb);
            if (response == null) {
                return PydioMessage.create(PydioMessage.EMPTY, "");
            }
        }

        if (response.code() != 200 || !cb.allChunksWritten()) {
            return PydioMessage.create(PydioMessage.ERROR, "");
        }

        return PydioMessage.create(PydioMessage.SUCCESS, "");
    }

    /**
     * Downloads content from the server.
     *
     * @param paths            Paths of remote nodes to be downloaded
     * @param target           Where to write downloaded data
     * @param progressListener A delegate to read download progress
     * @return The size of downloaded data
     */
    public long download(String tempWorkspace, String[] paths, OutputStream target, ProgressListener progressListener) throws IOException {

        Map<String, String> params = new HashMap<String, String>();
        fillParams(params, paths);

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        //Log.i("Request", "[action=" + Pydio.ACTION_DOWNLOAD + //Log.paramString(params) + "]");
        HttpResponse response = sessionTransport.getResponse(Pydio.ACTION_DOWNLOAD, params);

        InputStream stream = response.getEntity().getContent();

        if (responseStatus() != Pydio.OK) throw new IOException("failed to get stream");


        long total_read = 0;
        int read = 0, buffer_size = Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE;
        byte[] buffer = new byte[buffer_size];

        for (; ; ) {
            try {
                read = stream.read(buffer);
            } catch (IOException e) {
                throw new IOException("R");
            }
            if (read == -1) break;
            total_read += read;
            try {
                target.write(buffer, 0, read);
            } catch (IOException e) {
                //Log.e("Download", e.getMessage());
                throw new IOException("W");
            }

            if (progressListener != null) {
                progressListener.onProgress(total_read);
            }
        }

        try {
            stream.close();
        } catch (IOException e) {
            //Log.e("Download", e.getMessage());
            throw new IOException("C");
        }
        return total_read;
    }

    /**
     * Downloads content from the server
     *
     * @param paths            Paths of remote nodes to be downloaded
     * @param target           File that contains the downloaded data
     * @param progressListener A delegate to read download progress
     * @return The size of downloaded data
     * @throws IOException           on network error
     * @throws FileNotFoundException if target file does not exist
     */
    public long download(String tempWorkspace, String[] paths, File target, ProgressListener progressListener) throws IllegalStateException, IOException {
        OutputStream out = new FileOutputStream(target);
        long read = download(tempWorkspace, paths, out, progressListener);
        out.close();
        return read;
    }

    /**
     * Removes Nodes on the server
     *
     * @param tempWorkspace Target workspace
     * @param paths         Paths of node to remove
     */
    public void remove(String tempWorkspace, String[] paths, MessageHandler handler) throws IOException {
        remove(tempWorkspace, paths, null, handler);
    }

    /**
     * Removes nodes on the server
     *
     * @param tempWorkspace Target workspace
     * @param paths         Names of nodes to remove
     * @param dir           Directory that contains nodes to remove
     */
    public void remove(String tempWorkspace, String[] paths, String dir, MessageHandler handler) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        if (dir != null) {
            params.put(Pydio.PARAM_DIR, dir);
        }
        fillParams(params, paths);
        //Log.i("Request", "[action=" + Pydio.ACTION_DELETE + //Log.paramString(params) + "]");
        Document doc = sessionTransport.getXmlContent(Pydio.ACTION_DELETE, params);
        if (handler != null) {
            try {
                handler.onMessage(PydioMessage.create(doc));
            } catch (NullPointerException e) {
                handler.onMessage(PydioMessage.create(PydioMessage.ERROR, "Delete failed"));
            }
        }
    }

    /**
     * Rename the node specified by path with the newName
     *
     * @param tempWorkspace the target workspace ID
     * @param path          Path of the node to be renamed
     * @param newName       The new name
     */
    public void rename(String tempWorkspace, String path, String newName, MessageHandler handler) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_FILE, path);
        if (newName.contains("/")) {
            params.put(Pydio.PARAM_DEST, newName);
        } else {
            params.put(Pydio.PARAM_FILENAME_NEW, newName);
        }
        //Log.i("Request", "[action=" + Pydio.ACTION_RENAME + //Log.paramString(params) + "]");
        Document doc = sessionTransport.getXmlContent(Pydio.ACTION_RENAME, params);
        if (handler != null) {
            handler.onMessage(PydioMessage.create(doc));
        }
    }

    /**
     * Copy nodes at specified paths into a folder
     *
     * @param tempWorkspace the target workspace ID
     * @param paths         paths of nodes to be copied
     * @param targetFolder  Directory node to copy nodes in
     */
    public void copy(String tempWorkspace, String[] paths, String targetFolder, MessageHandler handler) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        fillParams(params, paths);
        params.put(Pydio.PARAM_DEST, targetFolder);
        //Log.i("Request", "[action=" + Pydio.ACTION_COPY + //Log.paramString(params) + "]");
        Document doc = sessionTransport.getXmlContent(Pydio.ACTION_COPY, params);
        if (handler != null) {
            handler.onMessage(PydioMessage.create(doc));
        }
    }

    /**
     * Move nodes at specified paths into a folder
     *
     * @param tempWorkspace the target workspace ID
     * @param paths         Paths of nodes to be moved
     * @param targetFolder  Directory node to move nodes in
     * @param handler       Delegate to process response
     */
    public void move(String tempWorkspace, String[] paths, String targetFolder, boolean force_del, MessageHandler handler) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_ACTION, Pydio.ACTION_MOVE);
        fillParams(params, paths);
        params.put(Pydio.PARAM_DEST, targetFolder);
        if (force_del) {
            params.put(Pydio.PARAM_FORCE_COPY_DELETE, "true");
        }
        //Log.i("Request", "[action=" + Pydio.ACTION_MOVE + //Log.paramString(params) + "]");
        Document doc = sessionTransport.getXmlContent(Pydio.ACTION_MOVE, params);
        if (handler != null) {
            handler.onMessage(PydioMessage.create(doc));
        }
    }

    /**
     * Creates a directory at specified path with specified name
     *
     * @param tempWorkspace the target workspace ID
     * @param targetFolder  Directory to create directory in
     * @param dirname       Directory name
     * @param handler       Delegate to process response
     */
    public void mkdir(String tempWorkspace, String targetFolder, String dirname, MessageHandler handler) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_DIR, targetFolder);
        params.put(Pydio.PARAM_DIRNAME, dirname);
        //Log.i("Request", "[action=" + Pydio.ACTION_MKDIR + //Log.paramString(params) + "]");
        Document doc = sessionTransport.getXmlContent(Pydio.ACTION_MKDIR, params);
        if (handler != null) {
            handler.onMessage(PydioMessage.create(doc));
        }
    }

    /**
     * Creates a file at specified path with specified name
     *
     * @param tempWorkspace the target workspace ID
     * @param path          File path
     * @param handler       Delegate to process response
     */
    public void mkfile(String tempWorkspace, String path, MessageHandler handler) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_NODE, path);
        //Log.i("Request", "[action=" + Pydio.ACTION_MKFILE + //Log.paramString(params) + "]");
        Document doc = sessionTransport.getXmlContent(Pydio.ACTION_MKFILE, params);
        handler.onMessage(PydioMessage.create(doc));
    }

    /**
     * Move a file from the recycle bin from its older place
     *
     * @param tempWorkspace the target workspace ID
     * @param path          Path of the file to restore
     * @param handler       Delegate to process response
     */
    public void restore(String tempWorkspace, String path, MessageHandler handler) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_FILE, path);
        params.put(Pydio.PARAM_DIR, "/recycle_bin");

        //Log.i("Request", "[action=" + Pydio.ACTION_RESTORE + //Log.paramString(params) + "]");
        Document doc = sessionTransport.getXmlContent(Pydio.ACTION_RESTORE, params);
        handler.onMessage(PydioMessage.create(doc));
    }

    /**
     * Creates a zip of nodes specified by paths
     *
     * @param tempWorkspace the target workspace ID
     * @param paths         Paths of nodes to be compressed
     * @param name          zip name
     * @param compressFlat  boolean
     * @param handler       Delegate to process response
     */
    public void compress(String tempWorkspace, String[] paths, String name, boolean compressFlat, MessageHandler handler) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_ARCHIVE_NAME, name);
        params.put(Pydio.PARAM_COMPRESS_FLAT, Boolean.toString(compressFlat).toLowerCase());
        fillParams(params, paths);
        //Log.i("Request", "[action=" + Pydio.ACTION_COMPRESS + //Log.paramString(params) + "]");
        handler.onMessage(PydioMessage.create(sessionTransport.getXmlContent(Pydio.ACTION_COMPRESS, params)));
    }

    public PydioMessage mkdir(String tempWorkspace, String path) throws IOException {

        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        File file = new File(path);
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_DIR, file.getParent());
        params.put(Pydio.PARAM_DIRNAME, file.getName());
        //Log.i("Request", "[action=" + Pydio.ACTION_MKDIR + //Log.paramString(params) + "]");
        return PydioMessage.create(sessionTransport.getXmlContent(Pydio.ACTION_MKDIR, params));
    }

    public PydioMessage move(String tempWorkspace, String[] paths, String targetFolder, boolean force_del) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_ACTION, Pydio.ACTION_MOVE);
        fillParams(params, paths);
        params.put(Pydio.PARAM_DEST, targetFolder);
        if (force_del) {
            params.put(Pydio.PARAM_FORCE_COPY_DELETE, "true");
        }
        //Log.i("Request", "[action=" + Pydio.ACTION_MOVE + //Log.paramString(params) + "]");
        Document doc = sessionTransport.getXmlContent(Pydio.ACTION_MOVE, params);
        return PydioMessage.create(doc);
    }

    public PydioMessage remove(String tempWorkspace, String[] paths) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        fillParams(params, paths);
        //Log.i("Request", "[action=" + Pydio.ACTION_DELETE + //Log.paramString(params) + "]");
        Document doc = sessionTransport.getXmlContent(Pydio.ACTION_DELETE, params);
        return PydioMessage.create(doc);
    }

    public PydioMessage copy(String tempWorkspace, String[] paths, String targetFolder) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        fillParams(params, paths);
        params.put(Pydio.PARAM_DEST, targetFolder);
        //Log.i("Request", "[action=" + Pydio.ACTION_COPY + //Log.paramString(params) + "]");
        Document doc = sessionTransport.getXmlContent(Pydio.ACTION_COPY, params);
        return PydioMessage.create(doc);
    }

    public InputStream previewData(String tempWorkspace, String path) throws IOException, UnexpectedResponseException {
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }

        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_FILE, path);
        params.put(Pydio.PARAM_GET_THUMB, "true");
        //params.put(Pydio.PARAM_DIMENSION, dim+"");

        String action;
        if (path.endsWith(".pdf")) {
            action = Pydio.ACTION_IMAGICK_DATA_PROXY;
        } else {
            action = Pydio.ACTION_PREVIEW_DATA_PROXY;
        }

        //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
        HttpResponse response = sessionTransport.getResponse(action, params);

        if (response != null) {
            String h = response.getHeaders("Content-Type").get(0);
            if (!h.toLowerCase().contains("image")) {
                throw new UnexpectedResponseException("");
            }
            try {
                return response.getEntity().getContent();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public void previewData(String tempWorkspace, String path, OutputStream out) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }

        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_FILE, path);
        params.put(Pydio.PARAM_GET_THUMB, "true");
        //params.put(Pydio.PARAM_DIMENSION, dim+"");

        String action = path.endsWith(".pdf") ? Pydio.ACTION_IMAGICK_DATA_PROXY : Pydio.ACTION_PREVIEW_DATA_PROXY;

        //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
        HttpResponse response = sessionTransport.getResponse(action, params);
        ////Log.info(HttpResponseParser.getString(response));

        if (response != null) {
            String h = response.getHeaders("Content-Type").get(0);
            if (h.toLowerCase().contains("image")) {
                InputStream in = response.getEntity().getContent();
                byte[] buffer = new byte[1024];
                int read;
                while (-1 != (read = in.read(buffer))) {
                    out.write(buffer, 0, read);
                }
                try {
                    in.close();
                } catch (IOException e) {
                }
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public String streamingVideoURL(String tempWorkspace, String path) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        loginIfNecessary();
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_SECURE_TOKEN, sessionTransport.mSecureToken);
        params.put(Pydio.PARAM_FILE, path);
        return sessionTransport.getGETUrl(Pydio.ACTION_READ_VIDEO_DATA, params);
    }

    public String streamingAudioURL(String tempWorkspace, String path) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        loginIfNecessary();
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_SECURE_TOKEN, sessionTransport.mSecureToken);
        params.put(Pydio.PARAM_FILE, path);
        params.put("rich_preview", "true");
        return sessionTransport.getGETUrl(Pydio.ACTION_AUDIO_PROXY, params);
    }

    public String listUsers() throws IOException {
        //Log.i("Request", "[action=" + Pydio.ACTION_LIST_USERS + "]");
        return sessionTransport.getStringContent(Pydio.ACTION_LIST_USERS, null);
    }

    public String createUser(String login, String password) throws IOException {
        //Log.i("Request", "[action=" + Pydio.ACTION_CREATE_USER + "]");
        return sessionTransport.getStringContent(Pydio.ACTION_CREATE_USER + login + "/" + password, null);
    }

    /**
     * Gets all the changes that occurred in a directory after a sequence number.
     *
     * @param tempWorkspace   the target workspace ID
     * @param seq             sequence number
     * @param filter          Directory name to search in
     * @param changeProcessor Delegate to process changes
     * @return An integer which is the sequence number of the most recent change
     */
    public int changes(String tempWorkspace, int seq, boolean flatten, String filter, ChangeProcessor changeProcessor) throws IOException, UnexpectedResponseException {
        Map<String, String> params = new HashMap<String, String>();
        int result_seq = 0;

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_CHANGE_SEQ_ID, seq + "");
        if (filter != null) {
            params.put(Pydio.PARAM_CHANGE_FILTER, filter);
        } else {
            params.put(Pydio.PARAM_CHANGE_FILTER, "/");
        }
        params.put(Pydio.PARAM_CHANGE_FLATTEN, String.valueOf(flatten));
        params.put(Pydio.PARAM_CHANGE_STREAM, "true");
        String action = Pydio.ACTION_CHANGES;
        try {
            //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
            HttpResponse r = sessionTransport.getResponse(action, params);
            String h = r.getHeaders("Content-Type").get(0);

            if (!h.toLowerCase().contains("application/json")) {
                throw new UnexpectedResponseException(HttpResponseParser.getString(r));
            }

            InputStream is = r.getEntity().getContent();
            Scanner sc = new Scanner(is, "UTF-8");
            sc.useDelimiter("\\n");
            String line = sc.nextLine();

            while (!line.toLowerCase().startsWith("last_seq")) {
                final String[] change = new String[11];
                while (!line.endsWith("}}")) {
                    line += sc.nextLine();
                }

                JSONObject json = new JSONObject(line);

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
                changeProcessor.process(change);
                line = sc.nextLine();
            }
            if (line.toLowerCase().startsWith("last_seq")) {
                result_seq = Integer.parseInt(line.split(":")[1]);
            }
        } catch (Exception e) {
        }
        return Math.max(seq, result_seq);
    }

    /**
     * Gets all the changes that occurred in a directory after a sequence number.
     *
     * @param tempWorkspace the target workspace ID
     * @param seq           sequence number
     * @param filter        Directory name to search in
     * @return An integer which is the sequence number of the most recent change
     */
    public long changes(String tempWorkspace, long seq, boolean flatten, String filter, ChangeHandler handler) throws IOException, UnexpectedResponseException {
        Map<String, String> params = new HashMap<String, String>();
        long result_seq = 0;

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }

        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_CHANGE_SEQ_ID, seq + "");
        if (filter != null) {
            params.put(Pydio.PARAM_CHANGE_FILTER, filter);
        } else {
            params.put(Pydio.PARAM_CHANGE_FILTER, "/");
        }

        params.put(Pydio.PARAM_CHANGE_FLATTEN, String.valueOf(flatten));
        params.put(Pydio.PARAM_CHANGE_STREAM, "true");
        String action = Pydio.ACTION_CHANGES;

        try {
            //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
            HttpResponse r = sessionTransport.getResponse(action, params);
            String h = r.getHeaders("Content-Type").get(0);

            if (!h.toLowerCase().contains("application/json")) {
                throw new UnexpectedResponseException(HttpResponseParser.getString(r));
            }

            InputStream is = r.getEntity().getContent();
            Scanner sc = new Scanner(is, "UTF-8");
            sc.useDelimiter("\\n");
            String line = sc.nextLine();

            while (!line.toLowerCase().startsWith("last_seq")) {
                final Change change = new Change();
                while (!line.endsWith("}}")) {
                    line += sc.nextLine();
                }

                JSONObject json = new JSONObject(line);

                change.seq = json.getLong(Pydio.CHANGE_SEQ);
                result_seq = Math.max(result_seq, change.seq);
                change.id = json.getLong(Pydio.CHANGE_NODE_ID);
                change.type = json.getString(Pydio.CHANGE_TYPE);
                change.source = json.getString(Pydio.CHANGE_SOURCE);
                change.target = json.getString(Pydio.CHANGE_TARGET);
                change.size = json.getJSONObject(Pydio.CHANGE_NODE).getLong(Pydio.CHANGE_NODE_BYTESIZE);
                change.md5 = json.getJSONObject(Pydio.CHANGE_NODE).getString(Pydio.CHANGE_NODE_MD5);
                change.mtime = json.getJSONObject(Pydio.CHANGE_NODE).getLong(Pydio.CHANGE_NODE_MTIME);
                change.path = json.getJSONObject(Pydio.CHANGE_NODE).getString(Pydio.CHANGE_NODE_PATH);
                change.ws = json.getJSONObject(Pydio.CHANGE_NODE).getString(Pydio.CHANGE_NODE_WORKSPACE);
                handler.onChange(change);
                line = sc.nextLine();
            }

            if (line.toLowerCase().startsWith("last_seq")) {
                result_seq = Integer.parseInt(line.split(":")[1]);
            }
        } catch (Exception e) {
        }

        return Math.max(seq, result_seq);
    }

    /**
     * Gets stats of a node
     *
     * @param tempWorkspace the target workspace ID
     * @param path          A String path of the node
     * @param with_hash     A boolean. If set to true a hash of the node is added to the result
     * @return A json object.
     */
    public JSONObject stats(String tempWorkspace, String path, boolean with_hash) throws UnexpectedResponseException, IOException {
        String text = "";
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        String action = Pydio.ACTION_STATS;
        if (with_hash) {
            action += "_hash";
        }

        params.put(Pydio.PARAM_FILE, path);
        //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
        HttpResponse r = sessionTransport.getResponse(action, params);
        if (r == null) return null;

        String h = r.getHeaders("Content-Type").get(0);
        if (!"application/json".equals(h.toLowerCase())) {
            throw new UnexpectedResponseException(HttpResponseParser.getString(r));
        }

        try {
            InputStream is = r.getEntity().getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            if (sb.length() == 0) return null;
            text = sb.toString();
            return new JSONObject(text);
        } catch (ParseException e) {
            throw new IOException(text);
        }
    }

    /**
     * Gets share info of a node
     *
     * @param tempWorkspace the target workspace ID
     * @param path          Path of the node to get the share info
     * @return null if the node is not shared. Or else A json object
     */
    public JSONObject shareInfo(String tempWorkspace, String path, String uuid) throws IOException {
        JSONObject bc = getBootConfig();
        if (bc == null) {
            throw new IOException("cannot get server info");
        }

        if (isPydioCells) {
            return shareInfo(uuid);

        } else {
            Map<String, String> params = new HashMap<String, String>();
            if (tempWorkspace == null) {
                tempWorkspace = mWorkspace.getId();
            }
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
            String action = Pydio.ACTION_LOAD_SHARED_ELEMENT_DATA;
            params.put(Pydio.PARAM_FILE, path);
            //params.put(Pydio.PARAM_SHARE_ELEMENT_TYPE, Pydio.SHARE_ELEMENT_TYPE_FILE);
            //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
            String res = sessionTransport.getStringContent(action, params);
            try {
                return new JSONObject(res);
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    private JSONObject shareInfo(String uuid) throws IOException {
        //ShareServiceApi shareApi = new ShareServiceApi();
        getJWT();
        if ("".equals(JWT)) {
            throw new IOException("failed to get the JWT");
        }
        ShareServiceApi api = new ShareServiceApi();
        try {
            RestShareLink link = api.getShareLink(uuid);
            Gson gs = new Gson();
            String jsonString = gs.toJson(link);
            return new JSONObject(jsonString);
        } catch (ApiException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a share link for node
     *
     * @param tempWorkspace  the target workspace ID
     * @param path           Path of the node the share link is requested
     * @param ws_label       A String label of the share link
     * @param ws_description A String description of the share link
     * @param password       A String password to access the content of the share link
     * @param expiration     An integer that tells the number of day the share link is active
     * @param canPreview     A boolean when set to true allow view when viewing shared node
     * @param canDownload    A boolean when set to true allow download when viewing shared node
     * @return the link as a string
     */
    public String minisiteShare(String tempWorkspace, String path, String uuid, String ws_label, boolean isFolder, String ws_description, String password, int expiration, int downloads, boolean canPreview, boolean canDownload) throws IOException {
        if (isPydioCells) {
            getJWT();
            if ("".equals(JWT)) {
                throw new IOException("failed to get the JWT");
            }
            ShareServiceApi api = new ShareServiceApi();
            try {
                RestPutShareLinkRequest body = new RestPutShareLinkRequest();
                body.setCreatePassword(password);
                body.setPasswordEnabled(Boolean.parseBoolean(password));
                RestShareLink sl = new RestShareLink();


                TreeNode n = new TreeNode();
                n.setUuid(uuid);

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

                body.setShareLink(sl);
                RestShareLink link = api.putShareLink(body);
                return link.getLinkUrl();
            } catch (ApiException e) {
                e.printStackTrace();
                return null;
            }
        }

        Map<String, String> params = new HashMap<String, String>();
        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        String action = Pydio.ACTION_SHARE;
        params.put(Pydio.PARAM_SUB_ACTION, Pydio.ACTION_CREATE_MINISITE);
        params.put(Pydio.PARAM_FILE, path);
        params.put(Pydio.PARAM_SHARE_ELEMENT_TYPE, isFolder ? "folder" : "file");
        params.put(Pydio.PARAM_CREATE_GUEST_USER, "true");

        //params.put("simple_right_download", canDownload ? "on" : "off");
        //params.put("simple_right_read", canPreview? "on" : "off");
        //params.put(Pydio.PARAM_SUB_ACTION, Pydio.ACTION_SHARE_NODE);
        //params.put(Pydio.PARAM_ENABLE_PUBLIC_LINK, "true");
        //params.put("share_type", "on");

        if (password != null && !"".equals(password)) {
            params.put(Pydio.PARAM_SHARE_GUEST_USER_PASSWORD, password);
        }
        if (canPreview) {
            params.put(Pydio.PARAM_RIGHT_PREVIEW, "on");
        } else {
            params.put(Pydio.PARAM_MINISITE_LAYOUT, "ajxp_unique_dl");
        }
        if (canDownload) {
            params.put(Pydio.PARAM_RIGHT_DOWNLOAD, "on");
        } else {
            params.put(Pydio.PARAM_MINISITE_LAYOUT, "ajxp_unique_strip");
        }
        params.put(Pydio.PARAM_SHARE_EXPIRATION, String.valueOf(expiration));
        params.put(Pydio.PARAM_SHARE_DOWNLOAD, String.valueOf(downloads));
        if (ws_description != null) {
            params.put(Pydio.PARAM_SHARE_WORKSPACE_DESCRIPTION, ws_description);
        }
        params.put(Pydio.PARAM_SHARE_WORKSPACE_LABEL, ws_label);
        //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
        return sessionTransport.getStringContent(action, params);
    }

    /**
     * Deleted generated share link for a node
     *
     * @param tempWorkspace the target workspace ID
     * @param path          Path of the node to unshare
     */
    public void unshareMinisite(String tempWorkspace, String path, String uuid) throws IOException {
        if (isPydioCells) {
            getJWT();
            if ("".equals(JWT)) {
                throw new IOException("failed to get the JWT");
            }

            ShareServiceApi api = new ShareServiceApi();
            try {
                RestDeleteShareLinkResponse rsp = api.deleteShareLink(uuid);
                if (!rsp.isSuccess()) {
                    throw new IOException(rsp.toString());
                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
            return;
        }

        Map<String, String> params = new HashMap<String, String>();
        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        String action = Pydio.ACTION_UNSHARE;
        params.put(Pydio.PARAM_FILE, path);
        //Log.i("Request", "[action=" + action + //Log.paramString(params) + "]");
        sessionTransport.getResponse(action, params);
    }

    /**
     * Search for nodes along the name
     *
     * @param tempWorkspace the target workspace ID
     * @param query         searched character sequence
     * @param handler       A delegate to handle result nodes
     */
    public void search(String tempWorkspace, String query, NodeHandler handler) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        if (tempWorkspace == null) {
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_SEARCH_QUERY, query);
        try {
            //Log.i("Request", "[action=" + Pydio.ACTION_SEARCH + //Log.paramString(params) + "]");
            HttpResponse response = sessionTransport.getResponse(Pydio.ACTION_SEARCH, params);
            InputStream in = response.getEntity().getContent();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, new TreeNodeSaxHandler(handler));
        } catch (SAXException e) {
        } catch (ParserConfigurationException e) {
        }
    }

    private void fillParams(Map<String, String> params, String[] paths) {
        if (paths != null) {
            if (paths.length == 1) {
                params.put(Pydio.PARAM_FILE, paths[0]);
                return;
            }
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i];
                params.put(Pydio.PARAM_FILE + "_" + i, path);
            }
        }
    }

    /**
     * Gets data relative to the user
     *
     * @param user   The name of the user
     * @param binary The id of the data field
     * @return handler Delegate to process response
     */
    public InputStream getUserData(String user, String binary) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Pydio.PARAM_USER_ID, user);
        params.put(Pydio.PARAM_BINARY_ID, binary);
        //Log.i("Request", "[action=" + Pydio.ACTION_GET_BINARY_PARAM + //Log.paramString(params) + "]");
        return sessionTransport.getResponseStream(Pydio.ACTION_GET_BINARY_PARAM, params);
    }

    public void setServer(ServerNode server) {
        this.server = server;
    }
    /**
     * @return An integer value of the last request status
     * @see Pydio
     */
    public int responseStatus() {
        return sessionTransport.requestStatus();
    }

    public void getCaptcha() throws IOException {
        sessionTransport.loadCaptcha();
    }

    public String action(String action, Map<String, String> params) {
        try {
            String content = sessionTransport.getStringContent(action, params);
            //Log.i("", content);
        } catch (Exception e) {
            //Log.e("ERROR", e.getMessage());
        }
        return action;
    }

    private JSONObject getBootConfig() throws IOException {
        if (bootConf == null) {
            String action = Pydio.ACTION_GET_BOOT_CONF;
            //params.put(Pydio.PARAM_SHARE_ELEMENT_TYPE, Pydio.SHARE_ELEMENT_TYPE_FILE);
            //Log.i("Request", "[action=" + action + "]");
            String res = sessionTransport.getStringContent(action, null);
            try {
                bootConf = new JSONObject(res);
            } catch (Exception ignored) {
            }
        }
        isPydioCells = bootConf.has("backend") && bootConf.getJSONObject("backend") != null;
        return bootConf;
    }

    private void getJWT() throws IOException {
        long cSecond = System.currentTimeMillis()/1000;
        if (null != JWT && !"".equals(JWT) && (JWTExpirationTime == -1 || JWTExpirationTime > cSecond)) {
            initApiClient();
            apiClient.addDefaultHeader("Authorization", "Bearer " + JWT);
        }

        login();
        Map<String, String> params = new HashMap<String, String>();
        params.put(Pydio.PARAM_CLIENT_TIME, "" + System.currentTimeMillis());
        String action = Pydio.ACTION_JWT;

        String sc = sessionTransport.getStringContent(action, params);
        try {
            JSONObject jsonObject = new JSONObject(sc);
            JWT = jsonObject.getString("jwt");
            if (jsonObject.has("expirationTime")) {
                JWTExpirationTime = jsonObject.getLong("expirationTime");
            }
            initApiClient();
            apiClient.addDefaultHeader("Authorization", "Bearer " + JWT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private ApiClient getApiClient() {
        try {
            initApiClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiClient;
    }
}
