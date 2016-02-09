package pydio.sdk.java;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
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
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Calendar;
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

import pydio.sdk.java.auth.AuthenticationHelper;
import pydio.sdk.java.http.HttpResponseParser;
import pydio.sdk.java.model.ChangeProcessor;
import pydio.sdk.java.model.Node;
import pydio.sdk.java.model.NodeHandler;
import pydio.sdk.java.model.PydioMessage;
import pydio.sdk.java.model.ServerNode;
import pydio.sdk.java.model.TreeNode;
import pydio.sdk.java.model.UnexpectedResponseException;
import pydio.sdk.java.model.WorkspaceNode;
import pydio.sdk.java.transport.SessionTransport;
import pydio.sdk.java.transport.Transport;
import pydio.sdk.java.transport.TransportFactory;
import pydio.sdk.java.utils.FileNodeSaxHandler;
import pydio.sdk.java.utils.MessageHandler;
import pydio.sdk.java.utils.ProgressListener;
import pydio.sdk.java.utils.Pydio;
import pydio.sdk.java.utils.RegistryItemHandler;
import pydio.sdk.java.utils.RegistrySaxHandler;
import pydio.sdk.java.utils.UploadStopNotifierProgressListener;
import pydio.sdk.java.utils.WorkspaceNodeSaxHandler;
/**
 * 
 * @author pydio
 *
 */

public class PydioClient {
	
	SessionTransport transport;
    public ServerNode server;
    Properties localConfigs = new Properties();
    AuthenticationHelper helper;


    //*****************************************
    //         INITIALIZATION METHODS
    //*****************************************

    public static PydioClient configure(ServerNode node, int mode){
        PydioClient client = new PydioClient(node, mode);
        return client;
    }
    public static PydioClient configure(ServerNode node){
        PydioClient client = new PydioClient(node, Transport.MODE_SESSION);
        return client;
    }
    public PydioClient(ServerNode server, int mode){
        transport = (SessionTransport) TransportFactory.getInstance(mode, server);
        this.server = server;
        localConfigs = new Properties();
        localConfigs.setProperty(Pydio.LOCAL_CONFIG_BUFFER_SIZE, "" + Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE);
    }

    public void setAuthenticationHelper(AuthenticationHelper h){
        helper = h;
        transport.setAuthenticationHelper(h);
    }
    public AuthenticationHelper authenticationHelper(){
        return helper;
    }

    //*****************************************
    //         REMOTE ACTION METHODS
    //*****************************************

    public boolean login() throws IOException {
        transport.login();
        return transport.requestStatus() == Pydio.NO_ERROR;
    }
    public boolean logout() throws IOException {
        String action = Pydio.ACTION_LOGOUT;
        String response = transport.getStringContent(action, null);

        boolean result = response != null && response.contains("logging_result value=\"2\"");

        if(result){
            transport.secure_token = "";
        }
        return result;
    }


    public boolean selectWorkspace(final String id) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Pydio.PARAM_WORKSPACE, id);
        transport.getResponse(Pydio.ACTION_SWITCH_REPO, params);

        int status = transport.requestStatus();
        if(status == Pydio.NO_ERROR){
            params.clear();
            String action = Pydio.ACTION_GET_REGISTRY;
            params.put(Pydio.PARAM_XPATH, Pydio.XPATH_USER_ACTIVE_WORKSPACE);
            String content = transport.getStringContent(action, params);
            return content != null && content.contains("<active_repo id=\"" + id + "\"");
        }
        return false;
    }
    public void downloadRegistry(OutputStream out, boolean workspace) throws IOException {
        Map<String, String> params = null;
        if(workspace){
            params = new HashMap<String, String>();
            params.put(Pydio.PARAM_XPATH, "actions");
        }else{
            params = new HashMap<String, String>();
            params.put(Pydio.PARAM_XPATH, "user");
        }
        try {
            InputStream in = transport.getResponseStream(Pydio.ACTION_GET_REGISTRY, params);
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
    public void parseRegistry(InputStream in, RegistryItemHandler handler){
        if (in == null) return;
        try {
            SAXParser parser = null;
            SAXParserFactory factory = SAXParserFactory.newInstance();
            parser = factory.newSAXParser();
            parser.parse(in, new RegistrySaxHandler(handler));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public void loadNodeData(String tempWorkspace, String path, NodeHandler handler)throws IOException{
        DefaultHandler saxHandler = null;
        String action;

        Map<String, String> params = new HashMap<String , String>();

        action = Pydio.ACTION_LIST;
        params.put(Pydio.PARAM_OPTIONS, "al");
        params.put(Pydio.PARAM_FILE, path);
        saxHandler = new FileNodeSaxHandler(handler, 0, -1);

        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
        try {
            HttpResponse r = transport.getResponse(action, params);
            InputStream in = r.getEntity().getContent();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, saxHandler);
            return;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void listChildren(String tempWorkspace, String path, final NodeHandler handler, int offset, int max) throws IOException {
        DefaultHandler saxHandler = null;
        String action;

        Map<String, String> params = new HashMap<String , String>();

        if(path == null){
            action = Pydio.ACTION_GET_REGISTRY;
            params.put(Pydio.PARAM_XPATH, Pydio.XPATH_USER_WORKSPACES);
            saxHandler = new WorkspaceNodeSaxHandler(handler, offset, max);
        }else{
            action = Pydio.ACTION_LIST;
            params.put(Pydio.PARAM_OPTIONS, "al");
            params.put(Pydio.PARAM_DIR, path);
            if(tempWorkspace != null) {
                params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
            }
            saxHandler = new FileNodeSaxHandler(handler, offset, max);
        }

        HttpResponse r = transport.getResponse(action , params);
        InputStream in  = r.getEntity().getContent();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = null;
        try {
            parser = factory.newSAXParser();
            parser.parse(in, saxHandler);
        } catch (Exception e) {
            throw new IOException();
        }
    }
	/**
	 * Upload a file on the pydio server
	 * @param path the directory to upload the file in
	 * @param source the file to be uploaded
	 * @param progressListener Listener to handle upload progress
	 * @param autoRename if set to true the file will be automatically rename if exists on the remote server
	 * @param name the name on the remote server
	 * @return a SUCCESS or ERROR Message
	 */
    public void write(String tempWorkspace, String path, File source, String name, boolean autoRename, UploadStopNotifierProgressListener progressListener, final MessageHandler handler)throws IOException {
        String action;
		Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null){
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }

        action =  Pydio.ACTION_UPLOAD;
        params.put(Pydio.PARAM_DIR, path);
		params.put(Pydio.PARAM_XHR_UPLOADER, "true");

        if(name == null){
            name = source.getName();
        }
        try {
            params.put(Pydio.PARAM_URL_ENCODED, java.net.URLEncoder.encode(name, "utf-8"));
        } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

        if(autoRename) {
            params.put(Pydio.PARAM_AUTO_RENAME, "true");
        }

        final Document doc = transport.putContent(action, params, source, name, progressListener);

        if(handler != null) {
            handler.onMessage(PydioMessage.create(doc));
        }
	}
	/**
	 * Download content from the remote server.
	 * @param paths remote nodes to read content
	 * @param outputStream Outputstream on the local target file
	 * @param progressListener
	 */
    public long read(String tempWorkspace, String[] paths, OutputStream outputStream, ProgressListener progressListener) throws IOException{

        Map<String, String> params = new HashMap<String , String>();
		fillParams(params, paths);

        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }

        InputStream stream = transport.getResponseStream(Pydio.ACTION_DOWNLOAD, params);
        if(requestStatus() != Pydio.NO_ERROR) throw new IOException("failed to get stream");


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
                outputStream.write(buffer, 0, read);
            } catch (IOException e){
                throw  new IOException("W");
            }

            if(progressListener != null){
                progressListener.onProgress(total_read);
            }
            i++;
        }
        try { stream.close(); }
        catch (IOException e){
            throw  new IOException("C");
        }
        return total_read;
	}
	/** 
	 * 
	 * Downlaod content ffrom the server
	 * @param paths Remotes nodes to read content from
	 * @param target local file to put read content in
	 * @param progressListener
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws IllegalStateException 
	 */
    public long read(String tempWorkspace, String[] paths, File target, ProgressListener progressListener) throws IllegalStateException, IOException {
        OutputStream out = new FileOutputStream(target);
        long read = read(tempWorkspace, paths, out, progressListener);
        out.close();
        return read;
    }
    /**
     * Remove node on the server
     * @param paths
     * @return
     */
    public void remove(String tempWorkspace, String[] paths, MessageHandler handler) throws IOException{
        remove(tempWorkspace, paths, null, handler);
    }

    public void remove(String tempWorkspace, String[] paths, String dir, MessageHandler handler)throws IOException{
        Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }

        if(dir != null){
            params.put(Pydio.PARAM_DIR, dir);
        }
        fillParams(params, paths);
        Document doc = transport.getXmlContent(Pydio.ACTION_DELETE, params);
        try {
            handler.onMessage(PydioMessage.create(doc));
        }catch (NullPointerException e){
            handler.onMessage(PydioMessage.create(PydioMessage.ERROR, "Delete failed"));
        }
    }
	/**
	 * 
	 * @param path
	 * @param newBaseName
	 * @return
	 */
    public void rename(String tempWorkspace, String path, String newBaseName, MessageHandler handler)throws IOException {
        Map<String, String> params = new HashMap<String , String>();

        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
        params.put(Pydio.PARAM_FILE, path);
        if(newBaseName.contains("/")){
            params.put(Pydio.PARAM_DEST, newBaseName);
        }else{
            params.put(Pydio.PARAM_FILENAME_NEW, newBaseName);
        }
        Document doc = transport.getXmlContent(Pydio.ACTION_RENAME, params);
        handler.onMessage(PydioMessage.create(doc));
	}
	/**
	 * 
	 * @param paths
	 * @param destinationParent
	 * @return
	 */
    public void copy(String tempWorkspace, String[] paths, String destinationParent, MessageHandler handler)throws IOException {
		Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
        fillParams(params, paths);
		params.put(Pydio.PARAM_DEST, destinationParent);
		Document doc = transport.getXmlContent(Pydio.ACTION_COPY, params);
        handler.onMessage(PydioMessage.create(doc));
	}
	/**
	 * 
	 * @param paths
	 * @param destinationParent
	 * @return
	 */
    public void move(String tempWorkspace, String[] paths, String destinationParent, boolean force_del, MessageHandler handler) throws IOException{
        Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
        params.put(Pydio.PARAM_ACTION, Pydio.ACTION_MOVE);
		fillParams(params, paths);
		params.put(Pydio.PARAM_DEST, destinationParent);
		if(force_del){
			params.put(Pydio.PARAM_FORCE_COPY_DELETE, "true");
		}
		Document doc = transport.getXmlContent(Pydio.ACTION_MOVE, params);
        handler.onMessage(PydioMessage.create(doc));
	}
	/**
	 * 
	 * @param path
	 * @param dirname
	 * @return
	 */
    public void createFolder(String tempWorkspace, String path, String dirname, MessageHandler handler)throws IOException {
        Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
        params.put(Pydio.PARAM_DIR, path);
		params.put(Pydio.PARAM_DIRNAME, dirname);
		Document doc = transport.getXmlContent(Pydio.ACTION_MKDIR, params);
        handler.onMessage(PydioMessage.create(doc));
	}
	/** 
	 * @param path
	 * @return
	 */
    public void createFile(String tempWorkspace, String path, MessageHandler handler) throws IOException{
        Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
		params.put(Pydio.PARAM_NODE, path);
		Document doc = transport.getXmlContent(Pydio.ACTION_MKFILE, params);
        handler.onMessage(PydioMessage.create(doc));
	}

    public void restore(String tempWorkspace, String path, MessageHandler handler)throws IOException{
        Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
        params.put(Pydio.PARAM_FILE, path);
        params.put(Pydio.PARAM_DIR, "/recycle_bin");

        Document doc = transport.getXmlContent(Pydio.ACTION_RESTORE, params);
        handler.onMessage(PydioMessage.create(doc));
    }

    public void compress(String tempWorkspace, String[] paths, String name, boolean compressFlat, MessageHandler handler)throws IOException{
        Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
        params.put(Pydio.PARAM_ARCHIVE_NAME, name);
        params.put(Pydio.PARAM_COMPRESS_FLAT, Boolean.toString(compressFlat).toLowerCase());
        fillParams(params, paths);
        handler.onMessage(PydioMessage.create(transport.getXmlContent(Pydio.ACTION_COMPRESS, params)));
    }


    /**
     * Load the remote config registry of the current server.
     */
    public void getRemoteConfigs() throws IOException {
        Map<String, String> params = new HashMap<String , String>();
        params.put(Pydio.PARAM_XPATH, Pydio.XPATH_VALUE_PLUGINS);
        Document doc = transport.getXmlContent(Pydio.ACTION_GET_REGISTRY , params);
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        try {
            XPathExpression expr = xpath.compile(Pydio.REMOTE_CONFIG_UPLOAD_SIZE);
            org.w3c.dom.Node result = (org.w3c.dom.Node)expr.evaluate(doc, XPathConstants.NODE);
            server.addConfig(Pydio.REMOTE_CONFIG_UPLOAD_SIZE, result.getFirstChild().getNodeValue().replace("\"", ""));
        } catch (XPathExpressionException e1) {
        }
    }

    public InputStream previewData(String tempWorkspace, String path, boolean force_redim, int dim)throws IOException{
        Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
        params.put(Pydio.PARAM_FILE, path);
        if(force_redim) {
            params.put(Pydio.PARAM_GET_THUMB, "true");
            params.put(Pydio.PARAM_DIMENSION, dim+"");
        }
        HttpResponse response = transport.getResponse(Pydio.ACTION_PREVIEW_DATA_PROXY, params);
        try{
            return response.getEntity().getContent();
        }catch(Exception e){}
        return null;
    }

    public String listUsers()throws IOException{
        return transport.getStringContent(Pydio.ACTION_LIST_USERS, null);
    }

    public String newUser(String login, String password)throws IOException{
        return transport.getStringContent(Pydio.ACTION_CREATE_USER + login + "/" + password, null);
    }

    public InputStream getAuthenticationChallenge(String type)throws IOException{
        //return transport.getAuthenticationChallenge();
        if(Pydio.AUTH_CHALLENGE_TYPE_CAPTCHA.equals(type)) {
            boolean image = false;
            HttpResponse resp = transport.getResponse(Pydio.ACTION_CAPTCHA, null);
            Header[] heads = resp.getHeaders("Content-type");
            for (int i = 0; i < heads.length; i++) {
                if (heads[i].getValue().contains("image/png")) {
                    image = true;
                    break;
                }
            }
            if (!image) return null;
            HttpEntity entity = resp.getEntity();
            try {
                return entity.getContent();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }

    public final int checkServer(String tempWorkspace)throws IOException{
        Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
        params.put(Pydio.PARAM_ACTION, Pydio.ACTION_GET_SEED);
        HttpResponse response = transport.getResponse(Pydio.ACTION_MKDIR, null);

        Header[] heads = response.getHeaders("Content-type");
        for(int i=0;i<heads.length;i++){
            if(!heads[i].getValue().contains("text/plain") && !heads[i].getValue().contains("application/json")) {
                boolean containsHTML = false;
                try {
                    String content = EntityUtils.toString(response.getEntity());
                    containsHTML = content.toLowerCase().contains("<html");
                } catch (Exception e) {}

                if(heads[i].getValue().contains("text/html") || containsHTML) {
                    return Pydio.ERROR_WRONG_PATH;
                }else {
                    return Pydio.ERROR_NOT_A_SERVER;
                }
            }
        }
        return Pydio.NO_ERROR;
    }

    public int changes(String tempWorkspace, int seq, boolean flatten, String filter, ChangeProcessor p)throws IOException, UnexpectedResponseException{
        Map<String, String> params = new HashMap<String , String>();
        int result_seq = 0;
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
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
            HttpResponse r = transport.getResponse(action, params);
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
                p.process(change);
                line = sc.nextLine();
            }
            if(line.toLowerCase().startsWith("last_seq")) {
                result_seq = Integer.parseInt(line.split(":")[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Math.max(seq, result_seq);
    }

    public JSONObject stats(String tempWorkspace, String[] paths, boolean with_hash) throws UnexpectedResponseException, IOException {
        String text = "";

        Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }

        String action = Pydio.ACTION_STATS;
        if(with_hash){
            action += "_hash";
        }

        fillParams(params, paths);
        HttpResponse r = transport.getResponse(action, params);
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
            e.printStackTrace();
            throw  new IOException(text);
        }
    }

    public JSONObject shareInfo(String tempWorkspace, String path)throws IOException{
        Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null){
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
        String action = Pydio.ACTION_LOAD_SHARED_ELEMENT_DATA;
        params.put(Pydio.PARAM_FILE, path);
        //params.put(Pydio.PARAM_SHARE_ELEMENT_TYPE, Pydio.SHARE_ELEMENT_TYPE_FILE);
        String res = transport.getStringContent(action, params);
        try {
            return new JSONObject(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public InputStream getAvatar(String user, String binary) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Pydio.PARAM_USER_ID, user);
        params.put(Pydio.PARAM_BINARY_ID, binary);
        return transport.getResponseStream(Pydio.ACTION_GET_BINARY_PARAM, params);
    }

    public JSONObject bootConf(){
        try {
            return new JSONObject(transport.getStringContent(Pydio.ACTION_GET_BOOT_CONF, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String minisiteShare(String tempWorkspace, String[] paths, String ws_label, String ws_description, String password, int expiration, int downloads, boolean canRead, boolean canDownload)throws IOException{

        Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }

        String action = Pydio.ACTION_SHARE;
        fillParams(params, paths);

        params.put(Pydio.PARAM_SUB_ACTION, Pydio.ACTION_CREATE_MINISITE);

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

        return transport.getStringContent(action, params);
    }

    public void unshareMinisite(String tempWorkspace, String path)throws IOException{
        Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
        String action = Pydio.ACTION_UNSHARE;
        params.put(Pydio.PARAM_FILE, path);
        transport.getResponse(action, params);
    }

    public void search(String tempWorkspace, String query, NodeHandler handler)throws IOException{
        Map<String, String> params = new HashMap<String , String>();
        if(tempWorkspace != null) {
            params.put(Pydio.PARAM_TEMP_WORKSPACE, tempWorkspace);
        }
        params.put(Pydio.PARAM_SEARCH_QUERY, query);
        try {
            HttpResponse response = transport.getResponse(Pydio.ACTION_SEARCH, params);
            InputStream in  = response.getEntity().getContent();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, new FileNodeSaxHandler(handler, 0, -1));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
    //*********************************************
    //            OTHERS
    //*********************************************

    /**
     * @param params
     * @param paths
     */
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

    public void setLocalConfig(String key, String value){
        localConfigs.setProperty(key, value);
    }

    public int requestStatus(){
        return transport.requestStatus();
    }

}
