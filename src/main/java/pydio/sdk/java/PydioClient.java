package pydio.sdk.java;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import pydio.sdk.java.http.CountingMultipartRequestEntity;
import pydio.sdk.java.http.HttpContentBody;
import pydio.sdk.java.utils.AuthenticationHelper;
import pydio.sdk.java.utils.HttpResponseParser;
import pydio.sdk.java.utils.ChangeProcessor;
import pydio.sdk.java.model.Node;
import pydio.sdk.java.model.NodeFactory;
import pydio.sdk.java.utils.NodeHandler;
import pydio.sdk.java.model.PydioMessage;
import pydio.sdk.java.model.ServerNode;
import pydio.sdk.java.model.FileNode;
import pydio.sdk.java.utils.UnexpectedResponseException;
import pydio.sdk.java.model.WorkspaceNode;
import pydio.sdk.java.transport.SessionTransport;
import pydio.sdk.java.transport.Transport;
import pydio.sdk.java.transport.TransportFactory;
import pydio.sdk.java.utils.TreeNodeSaxHandler;
import pydio.sdk.java.utils.Log;
import pydio.sdk.java.utils.MessageHandler;
import pydio.sdk.java.utils.ProgressListener;
import pydio.sdk.java.utils.Pydio;
import pydio.sdk.java.utils.UploadStopNotifierProgressListener;
import pydio.sdk.java.utils.WorkspaceNodeSaxHandler;
/**
 *
 * @author pydio
 *
 */

public class PydioClient {

	public SessionTransport http;
    public ServerNode server;
    protected WorkspaceNode mWorkspace;
    Properties localConfigs = new Properties();
    AuthenticationHelper helper;


    //*****************************************
    //         INITIALIZATION METHODS
    //*****************************************
    public PydioClient(String url, int mode){
        URI uri = URI.create(url);
        server = (ServerNode) NodeFactory.createNode(Node.TYPE_SERVER);
        server.setHost(uri.getHost());
        server.scheme(uri.getScheme());
        server.setPath(uri.getPath());
        server.setPort(uri.getPort());
        http = (SessionTransport) TransportFactory.getInstance(mode, server);
        localConfigs = new Properties();
        localConfigs.setProperty(Pydio.LOCAL_CONFIG_BUFFER_SIZE, "" + Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE);
    }
    public PydioClient(String url, int mode, final String login, final String password){
        this(url, mode);
        this.setAuthenticationHelper(new AuthenticationHelper() {
            @Override
            public String[] getCredentials() {
                return new String[]{login, password};
            }
        });
    }
    public PydioClient(String url, final String login, final String password){
        this(url);
        this.setAuthenticationHelper(new AuthenticationHelper() {
            @Override
            public String[] getCredentials() {
                return new String[]{login, password};
            }
        });
    }
    public PydioClient (String url){
        URI uri = URI.create(url);
        server = (ServerNode) NodeFactory.createNode(Node.TYPE_SERVER);
        server.setHost(uri.getHost());
        server.scheme(uri.getScheme());
        server.setPath(uri.getPath());
        server.setPort(uri.getPort());
        http = (SessionTransport) TransportFactory.getInstance(Transport.MODE_SESSION, server);
        localConfigs = new Properties();
        localConfigs.setProperty(Pydio.LOCAL_CONFIG_BUFFER_SIZE, "" + Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE);
    }
    public void setAuthenticationHelper(AuthenticationHelper h){
        helper = h;
        http.setAuthenticationHelper(h);
    }


    //*****************************************
    //         REMOTE ACTION METHODS
    //*****************************************

    /**
     * Authenticates the user
     * @return      true if the authentication succeeded. false if not
     * @see         #responseStatus()#logout()
     */
    public boolean login() throws IOException {
        http.login();
        return http.requestStatus() == Pydio.OK;
    }

    /**
     * UnAuthenticates the user
     * @return      true if the login() succeeded or false if the authen
     * @see         #responseStatus()#login()
     */
    public boolean logout() throws IOException {
        String action = Pydio.ACTION_LOGOUT;
        String response = http.getStringContent(action, null);
        Log.info("PYDIO SDK : " + "[action=" + action + "]");
        boolean result = response != null && response.contains("logging_result value=\"2\"");

        if(result){
            http.mSecureToken = "";
        }
        return result;
    }

    /**
     * Retrieve the workspaces
     * @see         #responseStatus()#logout()
     */
    public void workspaceList(final NodeHandler handler) throws IOException {
        Map<String, String> params = new HashMap<String , String>();
        String action = Pydio.ACTION_GET_REGISTRY;
        params.put(Pydio.PARAM_XPATH, Pydio.XPATH_USER_WORKSPACES);

        DefaultHandler saxHandler = new WorkspaceNodeSaxHandler(handler, 0, -1);

        Log.info("PYDIO SDK : " + "[action=" + action + Log.paramString(params) + "]");
        HttpResponse r = http.getResponse(action, params);

        try {
            SAXParserFactory
                    .newInstance()
                    .newSAXParser()
                    .parse(r.getEntity().getContent(), saxHandler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException();
        }
    }

    /**
     * Set the default workspace. After this method is called with success, the temporary workspace parameter is not necessary if sending request on workspace with with the same ID
     * @param   id  the target workspace ID
     * @see         #switchWorkspace(String)
     */
    public boolean selectWorkspace(final String id) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Pydio.PARAM_WORKSPACE, id);
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_SWITCH_REPO + Log.paramString(params) + "]");
        http.getResponse(Pydio.ACTION_SWITCH_REPO, params);
        int status = http.requestStatus();
        if(status == Pydio.OK){
            params.clear();
            String action = Pydio.ACTION_GET_REGISTRY;
            params.put(Pydio.PARAM_XPATH, Pydio.XPATH_USER_ACTIVE_WORKSPACE);

            Log.info("PYDIO SDK : " + "[action=" + action + Log.paramString(params) + "]");
            String content = http.getStringContent(action, params);
            return content != null && content.contains("<active_repo id=\"" + id + "\"");
        }
        return false;
    }

    /**
     * Set the default workspace. After this method is called with success, the temporary workspace parameter is not necessary if sending request on workspace with with the same ID
     * @param   id  the target workspace ID
     * @deprecated  use {@link #selectWorkspace(String)}
     */
    public void switchWorkspace(final String id) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Pydio.PARAM_WORKSPACE, id);
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_SWITCH_REPO + Log.paramString(params) + "]");
        http.getResponse(Pydio.ACTION_SWITCH_REPO, params);
        int status = http.requestStatus();
        if(status != Pydio.OK){
            throw new IOException();
        }
    }

    /**
     * Downloads the registry file. The registry content can be different whether the request is authenticated ot nor.
     * @param   tempWorkspace  the target workspace ID
     * @param   out Stream to write downloaded content in
     * @param   workspace Stream to write downloaded content in
     * @deprecated  use {@link #selectWorkspace(String)}
     */
    public void downloadRegistry(String tempWorkspace, OutputStream out, boolean workspace) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        if(workspace){
            selectWorkspace(tempWorkspace);
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }else{
            params.put(Pydio.PARAM_XPATH, "user");
        }
        try {
            Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_GET_REGISTRY + Log.paramString(params) + "]");
            InputStream in = http.getResponseStream(Pydio.ACTION_GET_REGISTRY, params);
            byte[] buffer = new byte[Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
        }catch (NullPointerException e){
            throw new IOException("empty response");
        }
    }

    /**
     * Loads the properties of the node located at the specified path
     * @param   tempWorkspace   The target workspace ID
     * @param   path            The path of the node to load
     * @param   handler         A delegate to handle the result
     */
    public void loadNodeData(String tempWorkspace, String path, NodeHandler handler)throws IOException{
        DefaultHandler saxHandler = null;
        String action;

        Map<String, String> params = new HashMap<String , String>();

        action = Pydio.ACTION_LIST;
        params.put(Pydio.PARAM_OPTIONS, "al");
        params.put(Pydio.PARAM_FILE, path);
        saxHandler = new TreeNodeSaxHandler(handler);


        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        try {
            Log.info("PYDIO SDK : " + "[action=" + action + Log.paramString(params) + "]");
            HttpResponse r = http.getResponse(action, params);
            InputStream in = r.getEntity().getContent();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, saxHandler);
            return;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {} catch(Exception e){}
    }

    /**
     * List content of a directory node.
     * @param   tempWorkspace   the target workspace ID
     * @param   path            The directory which content is to be listed
     * @param   handler         A delegate to handle parsed nodes
     * @return  The node at the specified path
     */
    public FileNode ls(String tempWorkspace, String path, final NodeHandler handler) throws IOException {
        DefaultHandler saxHandler = null;
        String action;

        Map<String, String> params = new HashMap<String , String>();

        if(path == null){
            action = Pydio.ACTION_GET_REGISTRY;
            params.put(Pydio.PARAM_XPATH, Pydio.XPATH_USER_WORKSPACES);
            saxHandler = new WorkspaceNodeSaxHandler(handler, 0, -1);
        }else{
            action = Pydio.ACTION_LIST;
            params.put(Pydio.PARAM_OPTIONS, "al");
            params.put(Pydio.PARAM_DIR, path);
            if(tempWorkspace == null){
                tempWorkspace = mWorkspace.getId();
            }
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
            saxHandler = new TreeNodeSaxHandler(handler);
        }

        while(true) {
            Log.info("PYDIO SDK : " + "[action=" + action + Log.paramString(params) + "]");
            HttpResponse r = http.getResponse(action, params);
            InputStream in = r.getEntity().getContent();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = null;

            try {
                parser = factory.newSAXParser();
                parser.parse(in, saxHandler);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException();
            }

            if(saxHandler instanceof TreeNodeSaxHandler){
                TreeNodeSaxHandler fileHandler = (TreeNodeSaxHandler) saxHandler;
                if(fileHandler.mPagination){
                    if(!(fileHandler.mPaginationTotalPage == fileHandler.mPaginationCurrentPage)){
                        params.put(Pydio.PARAM_DIR, path+"%23"+(fileHandler.mPaginationCurrentPage+1));
                    } else {
                        return fileHandler.mRootNode;
                    }
                } else {
                    if(fileHandler.mRootNode == null) {
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
	 * @param   path    The directory to upload in
	 * @param   source  The file to be uploaded
     * @param   name    The name of the uploaded file
     * @param   autoRename  if set to true the file will be renamed if there is a file with the same name
     * @param   progressListener    A delegate to listen to progress
     * @param   handler A delegate to handle the response message
     */
    public void upload(String tempWorkspace, String path, File source, String name, boolean autoRename, final UploadStopNotifierProgressListener progressListener, final MessageHandler handler)throws IOException {
        String action;
		Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        action =  Pydio.ACTION_UPLOAD;
        params.put(Pydio.PARAM_DIR, path);
		params.put(Pydio.PARAM_XHR_UPLOADER, "true");

        if(name == null){
            name = source.getName();
        }
        try {
            String urlEncodedName = java.net.URLEncoder.encode(name, "utf-8");
            params.put(Pydio.PARAM_URL_ENCODED_FILENAME, urlEncodedName);
        } catch (UnsupportedEncodingException e) {}

        if(autoRename) {
            params.put(Pydio.PARAM_AUTO_RENAME, "true");
        }

        Log.info("PYDIO SDK : " + "[action=" + action + Log.paramString(params) + "]");
        if(server.getRemoteConfig(Pydio.REMOTE_CONFIG_UPLOAD_SIZE) == null){
            try {
                getRemoteConfigs();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HttpContentBody cb = new HttpContentBody(source, name, Long.parseLong(server.getRemoteConfig(Pydio.REMOTE_CONFIG_UPLOAD_SIZE)));
        if(progressListener != null) {
            cb.setListener(new CountingMultipartRequestEntity.ProgressListener() {
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
        final Document doc = http.putContent(action, params, cb);
        if(handler != null) {
            handler.onMessage(PydioMessage.create(doc));
        }
	}

    /**
     * Upload data in a file at the specified directory
     * @param   path    The directory to upload in
     * @param   source  The data stream source
     * @param   length  The size of the data stream
     * @param   name    The name of the uploaded file
     * @param   autoRename  if set to true the file will be renamed if there is a file with the same name
     * @param   progressListener    A delegate to listen to progress
     * @param   handler A delegate to handle the response message
     */
    public void upload(String tempWorkspace, String path, InputStream source, long length, String name, boolean autoRename, final UploadStopNotifierProgressListener progressListener, final MessageHandler handler) throws IOException {
        String action;
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        action =  Pydio.ACTION_UPLOAD;
        params.put(Pydio.PARAM_DIR, path);
        params.put(Pydio.PARAM_XHR_UPLOADER, "true");

        try {
            String urlEncodedName = java.net.URLEncoder.encode(name, "utf-8");
            params.put(Pydio.PARAM_URL_ENCODED_FILENAME, urlEncodedName);
        } catch (UnsupportedEncodingException e) {}

        if(autoRename) {
            params.put(Pydio.PARAM_AUTO_RENAME, "true");
        }

        if(server.getRemoteConfig(Pydio.REMOTE_CONFIG_UPLOAD_SIZE) == null){
            try {
                getRemoteConfigs();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.info("PYDIO SDK : " + "[action=" + action + Log.paramString(params) + "]");
        HttpContentBody cb = new HttpContentBody(source, name, length, Long.parseLong(server.getRemoteConfig(Pydio.REMOTE_CONFIG_UPLOAD_SIZE)));
        if(progressListener != null) {
            cb.setListener(new CountingMultipartRequestEntity.ProgressListener() {
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

        final Document doc = http.putContent(action, params, cb);
        if(handler != null) {
            handler.onMessage(PydioMessage.create(doc));
        }
    }

    /**
     * Upload data in a file at the specified directory
     * @param   path    The directory to upload in
     * @param   source  The byte array to be uploaded
     * @param   name    The name of the uploaded file
     * @param   autoRename  if set to true the file will be renamed if there is a file with the same name
     * @param   progressListener    A delegate to listen to progress
     * @param   handler A delegate to handle the response message
     */
    public void upload(String tempWorkspace, String path, byte[] source, String name, boolean autoRename, final UploadStopNotifierProgressListener progressListener, final MessageHandler handler) throws IOException {
        String action;
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        action =  Pydio.ACTION_UPLOAD;
        params.put(Pydio.PARAM_DIR, path);
        params.put(Pydio.PARAM_XHR_UPLOADER, "true");

        try {
            String urlEncodedName = java.net.URLEncoder.encode(name, "utf-8");
            params.put(Pydio.PARAM_URL_ENCODED_FILENAME, urlEncodedName);
        } catch (UnsupportedEncodingException e) {}

        if(autoRename) {
            params.put(Pydio.PARAM_AUTO_RENAME, "true");
        }

        if(server.getRemoteConfig(Pydio.REMOTE_CONFIG_UPLOAD_SIZE) == null){
            try {
                getRemoteConfigs();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.info("PYDIO SDK : " + "[action=" + action + Log.paramString(params) + "]");
        HttpContentBody cb = new HttpContentBody(source, name, Long.parseLong(server.getRemoteConfig(Pydio.REMOTE_CONFIG_UPLOAD_SIZE)));
        if(progressListener != null) {
            cb.setListener(new CountingMultipartRequestEntity.ProgressListener() {
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
        final Document doc = http.putContent(action, params, cb);
        if(handler != null) {
            handler.onMessage(PydioMessage.create(doc));
        }
    }

	/**
	 * Downloads content from the server.
	 * @param paths     Paths of remote nodes to be downloaded
	 * @param target    Where to write downloaded data
	 * @param progressListener A delegate to read download progress
	 * @return The size of downloaded data
	 */
    public long download(String tempWorkspace, String[] paths, OutputStream target, ProgressListener progressListener) throws IOException{

        Map<String, String> params = new HashMap<String , String>();
		fillParams(params, paths);

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_DOWNLOAD + Log.paramString(params) + "]");
        InputStream stream = http.getResponseStream(Pydio.ACTION_DOWNLOAD, params);
        if(responseStatus() != Pydio.OK) throw new IOException("failed to get stream");


        long total_read = 0;
		int read = 0, buffer_size = Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE;
		byte[] buffer = new byte[buffer_size];

		int i = 0;
        for(;;){
            try {
                read = stream.read(buffer);
            } catch (IOException e){
                throw  new IOException("R");
            }

            if(read == -1) break;
            total_read += read;
            try {
                target.write(buffer, 0, read);
            } catch (IOException e){
                Log.info("PYDIO SDK : " + "[error=" + e.getMessage() + "]");
                throw  new IOException("W");
            }

            if(progressListener != null){
                progressListener.onProgress(total_read);
            }
            i++;
        }
        try { stream.close(); }
        catch (IOException e){
            Log.error("PYDIO SDK : " + "[error=" + e.getMessage() + "]");
            throw  new IOException("C");
        }
        return total_read;
	}

	/**
	 * Downloads content from the server
	 * @param paths Paths of remote nodes to be downloaded
	 * @param target    File that contains the downloaded data
     * @param progressListener A delegate to read download progress
     * @return The size of downloaded data
	 * @throws IOException on network error
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
     * @param tempWorkspace Target workspace
     * @param paths Paths of node to remove
     */
    public void remove(String tempWorkspace, String[] paths, MessageHandler handler) throws IOException{
        remove(tempWorkspace, paths, null, handler);
    }

    /**
     * Removes nodes on the server
     * @param tempWorkspace Target workspace
     * @param paths Names of nodes to remove
     * @param dir Directory that contains nodes to remove
     */
    public void remove(String tempWorkspace, String[] paths, String dir, MessageHandler handler)throws IOException{
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        if(dir != null){
            params.put(Pydio.PARAM_DIR, dir);
        }
        fillParams(params, paths);
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_DELETE + Log.paramString(params) + "]");
        Document doc = http.getXmlContent(Pydio.ACTION_DELETE, params);
        if(handler != null) {
            try {
                handler.onMessage(PydioMessage.create(doc));
            }catch (NullPointerException e){
                handler.onMessage(PydioMessage.create(PydioMessage.ERROR, "Delete failed"));
            }
        }

    }

	/**
	 * Rename the node specified by path with the newName
     * @param tempWorkspace   the target workspace ID
	 * @param path Path of the node to be renamed
	 * @param newName The new name
	 */
    public void rename(String tempWorkspace, String path, String newName, MessageHandler handler)throws IOException {
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_FILE, path);
        if(newName.contains("/")){
            params.put(Pydio.PARAM_DEST, newName);
        }else{
            params.put(Pydio.PARAM_FILENAME_NEW, newName);
        }
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_RENAME + Log.paramString(params) + "]");
        Document doc = http.getXmlContent(Pydio.ACTION_RENAME, params);
        if(handler != null) {
            handler.onMessage(PydioMessage.create(doc));
        }
	}

	/**
	 * Copy nodes at specified paths into a folder
     * @param tempWorkspace   the target workspace ID
	 * @param paths paths of nodes to be copied
	 * @param targetFolder Directory node to copy nodes in
	 */
    public void copy(String tempWorkspace, String[] paths, String targetFolder, MessageHandler handler)throws IOException {
		Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        fillParams(params, paths);
		params.put(Pydio.PARAM_DEST, targetFolder);
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_COPY + Log.paramString(params) + "]");
		Document doc = http.getXmlContent(Pydio.ACTION_COPY, params);
        if(handler != null) {
            handler.onMessage(PydioMessage.create(doc));
        }
	}

    /**
     * Move nodes at specified paths into a folder
     * @param tempWorkspace   the target workspace ID
     * @param paths Paths of nodes to be moved
     * @param targetFolder Directory node to move nodes in
     * @param handler Delegate to process response
     */
    public void move(String tempWorkspace, String[] paths, String targetFolder, boolean force_del, MessageHandler handler) throws IOException{
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_ACTION, Pydio.ACTION_MOVE);
		fillParams(params, paths);
		params.put(Pydio.PARAM_DEST, targetFolder);
		if(force_del){
			params.put(Pydio.PARAM_FORCE_COPY_DELETE, "true");
		}
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_MOVE + Log.paramString(params) + "]");
		Document doc = http.getXmlContent(Pydio.ACTION_MOVE, params);
        if(handler != null) {
            handler.onMessage(PydioMessage.create(doc));
        }
	}

	/**
     * Creates a directory at specified path with specified name
     * @param tempWorkspace   the target workspace ID
	 * @param targetFolder Directory to create directory in
	 * @param dirname Directory name
     * @param handler Delegate to process response
	 */
    public void mkdir(String tempWorkspace, String targetFolder, String dirname, MessageHandler handler)throws IOException {
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_DIR, targetFolder);
		params.put(Pydio.PARAM_DIRNAME, dirname);
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_MKDIR + Log.paramString(params) + "]");
		Document doc = http.getXmlContent(Pydio.ACTION_MKDIR, params);
        if(handler != null) {
            handler.onMessage(PydioMessage.create(doc));
        }
	}

    /**
     * Creates a file at specified path with specified name
     * @param tempWorkspace   the target workspace ID
     * @param path File path
     * @param handler Delegate to process response
     */
    public void mkfile(String tempWorkspace, String path, MessageHandler handler) throws IOException{
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
		params.put(Pydio.PARAM_NODE, path);
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_MKFILE + Log.paramString(params) + "]");
		Document doc = http.getXmlContent(Pydio.ACTION_MKFILE, params);
        handler.onMessage(PydioMessage.create(doc));
	}

    /**
     * Move a file from the recycle bin from its older place
     * @param tempWorkspace   the target workspace ID
     * @param path Path of the file to restore
     * @param handler Delegate to process response
     */
    public void restore(String tempWorkspace, String path, MessageHandler handler)throws IOException{
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_FILE, path);
        params.put(Pydio.PARAM_DIR, "/recycle_bin");

        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_RESTORE + Log.paramString(params) + "]");
        Document doc = http.getXmlContent(Pydio.ACTION_RESTORE, params);
        handler.onMessage(PydioMessage.create(doc));
    }

    /**
     * Creates a zip of nodes specified by paths
     * @param tempWorkspace   the target workspace ID
     * @param paths Paths of nodes to be compressed
     * @param name zip name
     * @param compressFlat boolean
     * @param handler Delegate to process response
     */
    public void compress(String tempWorkspace, String[] paths, String name, boolean compressFlat, MessageHandler handler)throws IOException{
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_ARCHIVE_NAME, name);
        params.put(Pydio.PARAM_COMPRESS_FLAT, Boolean.toString(compressFlat).toLowerCase());
        fillParams(params, paths);
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_COMPRESS + Log.paramString(params) + "]");
        handler.onMessage(PydioMessage.create(http.getXmlContent(Pydio.ACTION_COMPRESS, params)));
    }

    public void getRemoteConfigs() throws IOException {
        Map<String, String> params = new HashMap<String , String>();
        params.put(Pydio.PARAM_XPATH, Pydio.XPATH_VALUE_PLUGINS);
        Log.info("PYDIO SDK : " + "[action=remote_conf, " + Log.paramString(params) + "]");
        Document doc = http.getXmlContent(Pydio.ACTION_GET_REGISTRY, params);
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        try {
            XPathExpression expr = xpath.compile(Pydio.REMOTE_CONFIG_UPLOAD_SIZE);
            org.w3c.dom.Node result = (org.w3c.dom.Node)expr.evaluate(doc, XPathConstants.NODE);
            server.addConfig(Pydio.REMOTE_CONFIG_UPLOAD_SIZE, result.getFirstChild().getNodeValue().replace("\"", ""));
        } catch (XPathExpressionException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Get the preview of a node
     * @param tempWorkspace   the target workspace ID
     * @param path Directory name
     * @param force_redim A boolean when value is true ask a resized preview
     * @param dim the preview dimension. Useless if force_redim is false
     */
    public InputStream previewData(String tempWorkspace, String path, boolean force_redim, int dim)throws IOException, UnexpectedResponseException{
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }

        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_FILE, path);
        if(force_redim) {
            params.put(Pydio.PARAM_GET_THUMB, "true");
            params.put(Pydio.PARAM_DIMENSION, dim+"");
        }

        //Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_PREVIEW_DATA_PROXY + Log.paramString(params) + "]");
        HttpResponse response = http.getResponse(Pydio.ACTION_PREVIEW_DATA_PROXY, params);
        if(response != null) {
            Header h = response.getHeaders("Content-Type")[0];
            if (!h.getValue().toLowerCase().contains("image")) {
                /*System.err.println("PYDIO - SDK : " + "Preview Data content-type:" + h.getValue());
                System.err.println("PYDIO - SDK : " + "Parameters are [path=" + path + ", " + Pydio.PARAM_GET_THUMB + "=" + String.valueOf(force_redim) + "," + Pydio.PARAM_DIMENSION + "=" + String.valueOf(dim) + "]");
                String content = HttpResponseParser.getString(response);
                System.err.println("PYDIO - SDK : " + "Preview Data content:" + content);*/
                throw new UnexpectedResponseException("");
            }
            try {
                return response.getEntity().getContent();
            } catch (Exception e) {}
        }
        return null;
    }

    public String listUsers()throws IOException{
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_LIST_USERS + "]");
        return http.getStringContent(Pydio.ACTION_LIST_USERS, null);
    }
    public String createUser(String login, String password)throws IOException{
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_CREATE_USER + "]");
        return http.getStringContent(Pydio.ACTION_CREATE_USER + login + "/" + password, null);
    }

    /**
     * Gets all the changes that occurred in a directory after a sequence number.
     * @param tempWorkspace   the target workspace ID
     * @param seq sequence number
     * @param filter Directory name to search in
     * @param changeProcessor Delegate to process changes
     * @return An integer which is the sequence number of the most recent change
     */
    public int changes(String tempWorkspace, int seq, boolean flatten, String filter, ChangeProcessor changeProcessor)throws IOException, UnexpectedResponseException{
        Map<String, String> params = new HashMap<String , String>();
        int result_seq = 0;

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_CHANGE_SEQ_ID, seq+"");
        if(filter != null) {
            params.put(Pydio.PARAM_CHANGE_FILTER, filter);
        }else{
            params.put(Pydio.PARAM_CHANGE_FILTER, "/");
        }
        params.put(Pydio.PARAM_CHANGE_FLATTEN, String.valueOf(flatten));
        params.put(Pydio.PARAM_CHANGE_STREAM, "true");
        String action = Pydio.ACTION_CHANGES;
        try {
            Log.info("PYDIO SDK : " + "[action=" + action + Log.paramString(params) + "]");
            HttpResponse r = http.getResponse(action, params);
            Header[] h = r.getHeaders("Content-Type");

            if(!h[0].getValue().toLowerCase().contains("application/json")){
                throw new UnexpectedResponseException(HttpResponseParser.getString(r));
            }

            String charset = EntityUtils.getContentCharSet(r.getEntity());
            if(charset == null){
                charset = "UTF-8";
            }
            InputStream is = r.getEntity().getContent();
            Scanner sc = new Scanner(is, charset);
            sc.useDelimiter("\\n");
            String line = sc.nextLine();

            while(!line.toLowerCase().startsWith("last_seq")){
                final String[] change = new String[11];
                while(!line.endsWith("}}")){
                    line += sc.nextLine();
                }
                JSONObject json =  new JSONObject(line);

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
            if(line.toLowerCase().startsWith("last_seq")) {
                result_seq = Integer.parseInt(line.split(":")[1]);
            }
        } catch (Exception e) {}
        return Math.max(seq, result_seq);
    }

    /**
     * Gets stats of a node
     * @param tempWorkspace   the target workspace ID
     * @param path A String path of the node
     * @param with_hash A boolean. If set to true a hash of the node is added to the result
     * @return A json object.
     */
    public JSONObject stats(String tempWorkspace, String path, boolean with_hash) throws UnexpectedResponseException, IOException {
        String text = "";

        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        String action = Pydio.ACTION_STATS;
        if(with_hash){
            action += "_hash";
        }

        params.put(Pydio.PARAM_FILE, path);
        Log.info("PYDIO SDK : " + "[action=" + action + Log.paramString(params) + "]");
        HttpResponse r = http.getResponse(action, params);
        if(r == null) return null;

        Header[] h = r.getHeaders("Content-Type");
        if(!"application/json".equals(h[0].getValue().toLowerCase())){
            throw new UnexpectedResponseException(HttpResponseParser.getString(r));
        }

        try {
            String charset = EntityUtils.getContentCharSet(r.getEntity());
            if(charset == null){
                charset = "utf-8";
            }
            InputStream is = r.getEntity().getContent();

            BufferedReader br = new BufferedReader(new InputStreamReader(is, charset));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            if(sb.length() == 0) return null;
            text  = sb.toString();
            return new JSONObject(text);

        }catch (ParseException e) {
            throw  new IOException(text);
        }
    }

    /**
     * Gets share info of a node
     * @param tempWorkspace   the target workspace ID
     * @param path Path of the node to get the share info
     * @return null if the node is not shared. Or else A json object
     */
    public JSONObject shareInfo(String tempWorkspace, String path)throws IOException{
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        String action = Pydio.ACTION_LOAD_SHARED_ELEMENT_DATA;
        params.put(Pydio.PARAM_FILE, path);
        //params.put(Pydio.PARAM_SHARE_ELEMENT_TYPE, Pydio.SHARE_ELEMENT_TYPE_FILE);
        Log.info("PYDIO SDK : " + "[action=" + action + Log.paramString(params) + "]");
        String res = http.getStringContent(action, params);
        try {
            return new JSONObject(res);
        } catch (Exception e) {}
        return null;
    }

    /**
     * Gets data relative to the user
     * @param user The name of the user
     * @param binary The id of the data field
     * @return handler Delegate to process response
     */
    public InputStream getUserData(String user, String binary) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Pydio.PARAM_USER_ID, user);
        params.put(Pydio.PARAM_BINARY_ID, binary);
        Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_GET_BINARY_PARAM + Log.paramString(params) + "]");
        return http.getResponseStream(Pydio.ACTION_GET_BINARY_PARAM, params);
    }

    /**
     * Generates a share link for node
     * @param tempWorkspace   the target workspace ID
     * @param path Path of the node the share link is requested
     * @param ws_label A String label of the share link
     * @param ws_description A String description of the share link
     * @param password A String password to access the content of the share link
     * @param expiration An integer that tells the number of day the share link is active
     * @param canRead A boolean when set to true allow view when viewing shared node
     * @param canDownload A boolean when set to true allow download when viewing shared node
     * @return the link as a string
     */
    public String minisiteShare(String tempWorkspace, String path, String ws_label, String ws_description, String password, int expiration, int downloads, boolean canRead, boolean canDownload)throws IOException{
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);

        String action = Pydio.ACTION_SHARE;
        params.put(Pydio.PARAM_SUB_ACTION, Pydio.ACTION_CREATE_MINISITE);
        params.put(Pydio.PARAM_FILE, path);

        if(password != null && !"".equals(password)) {
            params.put(Pydio.PARAM_SHARE_GUEST_USER_PASSWORD, password);
        }

        params.put("create_guest_user", "true");
        if(canRead) params.put("simple_right_read", "on");
        if(canDownload) params.put("simple_right_download", "on");
        params.put("share_type", "on");

        params.put(Pydio.PARAM_SHARE_EXPIRATION, String.valueOf(expiration));
        params.put(Pydio.PARAM_SHARE_DOWNLOAD, String.valueOf(downloads));
        params.put(Pydio.PARAM_SHARE_WORKSPACE_DESCRIPTION, ws_description);
        params.put(Pydio.PARAM_SHARE_WORKSPACE_LABEL, ws_label);

        Log.info("PYDIO SDK : " + "[action=" + action + Log.paramString(params) + "]");
        return http.getStringContent(action, params);
    }

    /**
     * Deleted generated share link for a node
     * @param tempWorkspace the target workspace ID
     * @param path Path of the node to unshare
     */
    public void unshareMinisite(String tempWorkspace, String path)throws IOException{
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        String action = Pydio.ACTION_UNSHARE;
        params.put(Pydio.PARAM_FILE, path);
        Log.info("PYDIO SDK : " + "[action=" + action + Log.paramString(params) + "]");
        http.getResponse(action, params);
    }

    /**
     * Search for nodes along the name
     * @param   tempWorkspace   the target workspace ID
     * @param   query           searched character sequence
     * @param   handler         A delegate to handle result nodes
     */
    public void search(String tempWorkspace, String query, NodeHandler handler)throws IOException{
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace == null){
            tempWorkspace = mWorkspace.getId();
        }
        params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        params.put(Pydio.PARAM_SEARCH_QUERY, query);
        try {
            Log.info("PYDIO SDK : " + "[action=" + Pydio.ACTION_SEARCH + Log.paramString(params) + "]");
            HttpResponse response = http.getResponse(Pydio.ACTION_SEARCH, params);
            InputStream in  = response.getEntity().getContent();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, new TreeNodeSaxHandler(handler));
        } catch (SAXException e) {} catch (ParserConfigurationException e) {}
    }

    /**
     * @return A ByteArrayOutputStream that contains the captcha bytes
     */
    public ByteArrayOutputStream captchaData() {
        return http.getCaptcha();
    }



    private void fillParams(Map<String, String> params, String[] paths) {
        if(paths != null){
            if(paths.length == 1){
                params.put(Pydio.PARAM_FILE, paths[0]);
                return;
            }
            for(int i = 0; i < paths.length; i++){
                String path = paths[i];
                params.put(Pydio.PARAM_FILE+"_"+i, path);
            }
        }
    }

    public void setServer(ServerNode server){
        this.server = server;
    }

    public void setConfig(String key, String value){
        localConfigs.setProperty(key, value);
    }

    /**
     * @return An integer value of the last request status
     * @see Pydio
     */
    public int responseStatus(){
        return http.requestStatus();
    }

}
