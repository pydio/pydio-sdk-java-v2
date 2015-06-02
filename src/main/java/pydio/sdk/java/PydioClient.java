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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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

import pydio.sdk.java.auth.AuthenticationHelper;
import pydio.sdk.java.model.ChangeProcessor;
import pydio.sdk.java.model.Node;
import pydio.sdk.java.model.NodeHandler;
import pydio.sdk.java.model.PydioMessage;
import pydio.sdk.java.model.ServerNode;
import pydio.sdk.java.model.TreeNode;
import pydio.sdk.java.model.WorkspaceNode;
import pydio.sdk.java.transport.Transport;
import pydio.sdk.java.transport.TransportFactory;
import pydio.sdk.java.utils.FileNodeSaxHandler;
import pydio.sdk.java.utils.MessageHandler;
import pydio.sdk.java.utils.ProgressListener;
import pydio.sdk.java.utils.Pydio;
import pydio.sdk.java.utils.WorkspaceNodeSaxHandler;

/**
 * 
 * @author pydio
 *
 */
public class PydioClient {
	
	Transport transport;
    public WorkspaceNode workspace;
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
        transport = TransportFactory.getInstance(mode, server);
        this.server = server;
        localConfigs = new Properties();
        localConfigs.setProperty(Pydio.LOCAL_CONFIG_BUFFER_SIZE, "" + Pydio.LOCAL_CONFIG_BUFFER_SIZE_DVALUE);
    }

    public void setAuthenticationHelper(AuthenticationHelper h){
        helper = h;
        transport.setAuthenticationHelper(helper);
    }
    public AuthenticationHelper authenticationHelper(){
        return helper;
    }

    //*****************************************
    //         REMOTE ACTION METHODS
    //*****************************************
    public boolean selectWorkspace(final String id){
        final WorkspaceNode[] wn = new WorkspaceNode[1];
        listChildren(null, new NodeHandler() {
            boolean found = false;
            @Override
            public void processNode(Node node) {
                if(found) return;
                WorkspaceNode n = (WorkspaceNode) node;
                if(n!= null && n.getId().equals(id)){
                    found = true;
                    wn[0] = n;
                }
            }
        }, 0, 1000);
        workspace = wn[0];
        return wn[0] != null;
    }

    public void listChildren(String path, final NodeHandler handler, int offset, int max) {
        DefaultHandler saxHandler = null;
        String action;

        Map<String, String> params = new HashMap<String , String>();

        if(path == null){
            action = Pydio.ACTION_GET_REGISTRY;
            params.put(Pydio.PARAM_XPATH, Pydio.XPATH_VALUE_USER_REPO);
            saxHandler = new WorkspaceNodeSaxHandler(handler, offset, max);
        }else{
            action = Pydio.ACTION_LIST;
            params.put(Pydio.PARAM_OPTIONS, "al");
            params.put(Pydio.PARAM_DIR, path);
            if(workspace != null) {
                params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
            }
            saxHandler = new FileNodeSaxHandler(handler, offset, max);
        }

        try {
            HttpResponse r = transport.getResponse(action , params);
            InputStream in  = r.getEntity().getContent();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, saxHandler);
            return;
        } catch (IOException e) {
            //e.printStackTrace();
        } catch (ParserConfigurationException e) {
            //e.printStackTrace();
        } catch (SAXException e) {
            //e.printStackTrace();
        } catch(Exception e){
            //e.printStackTrace();
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
    public void write(String path, File source, String name, boolean autoRename, ProgressListener progressListener, final MessageHandler handler) {
        String action;
		Map<String, String> params = new HashMap<String , String>();
        action =  Pydio.ACTION_UPLOAD;
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
        params.put(Pydio.PARAM_NODE, path);
		params.put(Pydio.PARAM_XHR_UPLOADER, "true");

		String tmp_name = null;
		String fname = source.getName();


		if(!EncodingUtils.getAsciiString(EncodingUtils.getBytes(source.getName(), "US-ASCII")).equals(source.getName())){
			tmp_name = fname;
			fname = EncodingUtils.getAsciiString(EncodingUtils.getBytes(source.getName(), "US-ASCII")).replace("?", "") + ".tmp_upload";
		}

		try {
			if(name != null){
				params.put(Pydio.PARAM_APPEND_TO_URLENCODED_PART, name);
			}else{
				params.put(Pydio.PARAM_URL_ENCODED, java.net.URLEncoder.encode(fname, "UTF-8"));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if(autoRename){
			params.put(Pydio.PARAM_AUTO_RENAME, "true");
		}
        final Document doc = transport.putContent(action, params, source, fname, progressListener);
		if(tmp_name != null){
			rename(path, tmp_name, new MessageHandler() {
                @Override
                public void onMessage(PydioMessage m) {
                    if(PydioMessage.SUCCESS.equals(m.getType())) {
                        handler.onMessage(PydioMessage.create(doc));
                    }else{
                    }
                }
            });
		}else{
            handler.onMessage(PydioMessage.create(doc));
        }
		//return m;
	}
	/**
	 * Download content from the remote server.
	 * @param paths remote nodes to read content
	 * @param outputStream Outputstream on the local target file
	 * @param progressListener
	 */
    public void read(String[] paths, OutputStream outputStream, ProgressListener progressListener) {

        Map<String, String> params = new HashMap<String , String>();
		fillParams(params, paths);
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
		InputStream stream = null;
		try {
            HttpEntity entity = transport.getResponse(Pydio.ACTION_DOWNLOAD, params).getEntity();
			stream = entity.getContent();
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        long total_read = 0;
		int read = 0, buffer_size = Integer.parseInt(localConfigs.getProperty(Pydio.LOCAL_CONFIG_BUFFER_SIZE));
		byte[] buffer = new byte[buffer_size];
		int i = 0;
		try {
			for(;;){
				read = stream.read(buffer);
				if(read == -1) break;
                total_read += read;
				outputStream.write(buffer, 0, read);
				if(progressListener != null){
					progressListener.onProgress(total_read);
				}
				i++;
			}
			stream.close();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/** 
	 * 
	 * Downlaod content ffrom the server
	 * @param paths Remotes nodes to read content from
	 * @param target local file to put read content in
	 * @param progressListener
	 * @throws java.io.IOException
	 * @throws java.io.FileNotFoundException
	 * @throws IllegalStateException 
	 */
    public void read(String[] paths, File target, ProgressListener progressListener) throws IllegalStateException, FileNotFoundException, IOException {
        read(paths, new FileOutputStream(target), progressListener);
    }
    /**
     * Remove node on the server
     * @param paths
     * @return
     */
    public void remove(String[] paths, MessageHandler handler) {
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
        fillParams(params, paths);
        Document doc = transport.getXmlContent(Pydio.ACTION_DELETE, params);
        handler.onMessage(PydioMessage.create(doc));
    }
	/**
	 * 
	 * @param path
	 * @param newBaseName
	 * @return
	 */
    public void rename(String path, String newBaseName, MessageHandler handler) {
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
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
    public void copy(String[] paths, Node destinationParent, MessageHandler handler) {
		Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
        fillParams(params, paths);
		params.put(Pydio.PARAM_DEST, ((TreeNode)destinationParent).path());
		Document doc = transport.getXmlContent(Pydio.ACTION_COPY, params);
        handler.onMessage(PydioMessage.create(doc));
	}
	/**
	 * 
	 * @param paths
	 * @param destinationParent
	 * @return
	 */
    public void move(String[] paths, Node destinationParent, boolean force_del, MessageHandler handler) {
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
        params.put(Pydio.PARAM_ACTION, Pydio.ACTION_MOVE);
		fillParams(params, paths);
		params.put(Pydio.PARAM_DEST, ((TreeNode)destinationParent).path());
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
    public void createFolder(String path, String dirname, MessageHandler handler) {
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
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
    public void createFile(String path, MessageHandler handler) {
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
		params.put(Pydio.PARAM_NODE, path);
		Document doc = transport.getXmlContent(Pydio.ACTION_MKFILE, params);
        handler.onMessage(PydioMessage.create(doc));
	}

    public void restore(String path, MessageHandler handler){
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
        params.put(Pydio.PARAM_FILE, path);
        params.put(Pydio.PARAM_DIR, "/recycle_bin");

        Document doc = transport.getXmlContent(Pydio.ACTION_RESTORE, params);
        handler.onMessage(PydioMessage.create(doc));
    }

    public void compress(String[] paths, String name, boolean compressFlat, MessageHandler handler){
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
        params.put(Pydio.PARAM_ARCHIVE_NAME, name);
        params.put(Pydio.PARAM_COMPRESS_FLAT, Boolean.toString(compressFlat).toLowerCase());
        fillParams(params, paths);
        handler.onMessage(PydioMessage.create(transport.getXmlContent(Pydio.ACTION_COMPRESS, params)));
    }
    /**
     * Load the remote config registry of the current server.
     */
    public void getRemoteConfigs(){
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
            //publish error message
        }
    }

    public InputStream previewData(String path, boolean force_redim, int dim){
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
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

    public String listUsers(){
        return transport.getStringContent(Pydio.ACTION_LIST_USERS, null);
    }

    public String newUser(String login, String password){
        return transport.getStringContent(Pydio.ACTION_CREATE_USER + login + "/" + password, null);
    }

    public InputStream getAuthenticationChallenge(String type){
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

    public final int checkServer(){
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
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

    public int changes(int seq, boolean flatten, String filter, ChangeProcessor p){
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
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
            String charset = EntityUtils.getContentCharSet(r.getEntity());
            if(charset == null){
                charset = "utf-8";
            }
            InputStream is = r.getEntity().getContent();
            Scanner sc = new Scanner(is, charset);
            String line = sc.nextLine();
            while(!line.toLowerCase().startsWith("last_seq") && line.length() != 0){
                final String[] change = new String[10];
                while(!line.endsWith("}}")){
                    line += sc.nextLine();
                }
                JSONObject json =  new JSONObject(line);

                change[Pydio.CHANGE_INDEX_SEQ] = json.getString(Pydio.CHANGE_SEQ);
                change[Pydio.CHANGE_INDEX_NODE_ID] = json.getString(Pydio.CHANGE_NODE_ID);
                change[Pydio.CHANGE_INDEX_TYPE] = json.getString(Pydio.CHANGE_TYPE);
                change[Pydio.CHANGE_INDEX_SOURCE] = json.getString(Pydio.CHANGE_SOURCE);
                change[Pydio.CHANGE_INDEX_TARGET] = json.getString(Pydio.CHANGE_TARGET);
                change[Pydio.CHANGE_INDEX_NODE_BYTESIZE] = json.getJSONObject(Pydio.CHANGE_NODE).getString(Pydio.CHANGE_NODE_BYTESIZE);
                change[Pydio.CHANGE_INDEX_NODE_MD5] = json.getJSONObject(Pydio.CHANGE_NODE).getString(Pydio.CHANGE_NODE_MD5);
                change[Pydio.CHANGE_INDEX_NODE_MTIME] = json.getJSONObject(Pydio.CHANGE_NODE).getString(Pydio.CHANGE_NODE_MTIME);
                change[Pydio.CHANGE_INDEX_NODE_PATH] = json.getJSONObject(Pydio.CHANGE_NODE).getString(Pydio.CHANGE_NODE_PATH);
                change[Pydio.CHANGE_INDEX_NODE_WORKSPACE] = json.getJSONObject(Pydio.CHANGE_NODE).getString(Pydio.CHANGE_NODE_WORKSPACE);
                p.process(change);
                line = sc.nextLine();
            }
            if(line.toLowerCase().startsWith("last_seq")) {
                return Integer.parseInt(line.split(":")[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return seq;
    }

    public JSONObject stats(String[] paths, boolean with_hash, ChangeProcessor p){
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
        String action = Pydio.ACTION_STATS;
        if(with_hash){
            action += "_hash";
        }
        fillParams(params, paths);
        try {
            HttpResponse r = transport.getResponse(action, params);
            String charset = EntityUtils.getContentCharSet(r.getEntity());
            if(charset == null){
                charset = "UTF-8";
            }
            InputStream is = r.getEntity().getContent();
            Scanner sc = new Scanner(is, charset);
            return new JSONObject(sc.nextLine());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    //*********************************************
    //            OTHERS
    //*********************************************
    /**
     * @param params
     * @param paths
     */
    private void fillParams(Map<String, String> params, String[] paths){
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
    /**
     *
     * @param w
     */
    public void setWorkspace(WorkspaceNode w){
        this.workspace = w;
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
