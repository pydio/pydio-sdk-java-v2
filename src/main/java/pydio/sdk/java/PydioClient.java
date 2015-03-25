
package pydio.sdk.java;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import pydio.sdk.java.auth.CredentialsProvider;
import pydio.sdk.java.model.FileNode;
import pydio.sdk.java.model.Node;
import pydio.sdk.java.model.NodeFactory;
import pydio.sdk.java.model.NodeHandler;
import pydio.sdk.java.model.PydioMessage;
import pydio.sdk.java.model.ServerNode;
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


    //*****************************************
    //         INITIALIZATION METHODS
    //*****************************************
    public static PydioClient configure(ServerNode node, int mode, CredentialsProvider cp){
        PydioClient client = new PydioClient(node, mode);
        client.transport.setCredentialsProvider(cp);
        return client;
    }
    public static PydioClient configure(ServerNode node, CredentialsProvider cp){
        PydioClient client = new PydioClient(node, Transport.MODE_SESSION);
        client.transport.setCredentialsProvider(cp);
        return client;
    }

    public PydioClient(ServerNode server, int mode){
        transport = TransportFactory.getInstance(mode, server);
        this.server = server;
        localConfigs = new Properties();
        localConfigs.setProperty(Pydio.LOCAL_CONFIG_BUFFER_SIZE, ""+Pydio.LCONFIG_BUFFER_SIZE_DVALUE);
    }
    public PydioClient(String scheme, String host, String path, int transportMode){
        server = (ServerNode) NodeFactory.createNode(Node.TYPE_SERVER);
        server.setPath(path);
        server.setLegacy(false);
        server.setProtocol(scheme);
        server.setHost(host);
        server.setSelSigned(true);
        transport = TransportFactory.getInstance(transportMode);
        transport.setServer(server);
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

    public void listChildren(Node node, final NodeHandler handler, int offset, int max) {
        DefaultHandler saxHandler = null;
        String action;

        Map<String, String> params = new HashMap<String , String>();

        if(node == null || node.type()==Node.TYPE_SERVER){
            action = Pydio.ACTION_GET_REGISTRY;
            params.put(Pydio.PARAM_XPATH, Pydio.XPATH_VALUE_USER_REPO);
            saxHandler = new WorkspaceNodeSaxHandler(handler, offset, max);
        }else{
            action = Pydio.ACTION_LIST;
            params.put(Pydio.PARAM_OPTIONS, "al");
            params.put(Pydio.PARAM_DIR, node.path());
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
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
	/**
	 * Upload a file on the pydio server
	 * @param node the directory to upload the file in
	 * @param source the file to be uploaded
	 * @param progressListener Listener to handle upload progress
	 * @param autoRename if set to true the file will be automatically rename if exists on the remote server
	 * @param name the name on the remote server
	 * @return a SUCCESS or ERROR Message
	 */
    public void write(Node node, File source, ProgressListener progressListener , boolean autoRename, String name, final MessageHandler handler) {
        String action;
		Map<String, String> params = new HashMap<String , String>();
        action =  Pydio.ACTION_UPLOAD;
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
        params.put(Pydio.PARAM_NODE, node.path());
		params.put(Pydio.PARAM_XHR_UPLOADER, "true");

		String tmp_name = null;
		String fname = source.getName();


		if(!EncodingUtils.getAsciiString(EncodingUtils.getBytes(source.getName(), "US-ASCII")).equals(source.getName())){
			tmp_name = fname;
			fname = EncodingUtils.getAsciiString(EncodingUtils.getBytes(source.getName(), "US-ASCII")).replace("?", "") + ".tmp_upload";
		}

		try {
			if(name != null){
				params.put(Pydio.PARAM_APPENDTO_URLENCODED_PART, name);
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
			rename(node, tmp_name, new MessageHandler() {
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
	 * @param nodes remote nodes to read content
	 * @param outputStream Outputstream on the local target file
	 * @param progressListener
	 */
    public void read(Node[] nodes, OutputStream outputStream, ProgressListener progressListener) {

        Map<String, String> params = new HashMap<String , String>();
		fillParams(params, nodes);
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
	 * @param nodes Remotes nodes to read content from
	 * @param target local file to put read content in
	 * @param progressListener
	 * @throws java.io.IOException
	 * @throws java.io.FileNotFoundException
	 * @throws IllegalStateException 
	 */
    public void read(Node[] nodes, File target, ProgressListener progressListener) throws IllegalStateException, FileNotFoundException, IOException {
        read(nodes, new FileOutputStream(target), progressListener);
    }
    /**
     * Remove node on the server
     * @param nodes
     * @return
     */
    public void remove(Node[] nodes, MessageHandler handler) {
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
        fillParams(params, nodes);
        Document doc = transport.getXmlContent(Pydio.ACTION_DELETE, params);
        handler.onMessage(PydioMessage.create(doc));
    }
	/**
	 * 
	 * @param node
	 * @param newBaseName
	 * @return
	 */
    public void rename(Node node, String newBaseName, MessageHandler handler) {
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
		params.put(Pydio.PARAM_FILE, node.path());
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
	 * @param nodes
	 * @param destinationParent
	 * @return
	 */
    public void copy(Node[] nodes, Node destinationParent, MessageHandler handler) {
		Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
        fillParams(params, nodes);
		params.put(Pydio.PARAM_DEST, ((FileNode)destinationParent).path());
		Document doc = transport.getXmlContent(Pydio.ACTION_COPY, params);
        handler.onMessage(PydioMessage.create(doc));
	}
	/**
	 * 
	 * @param nodes
	 * @param destinationParent
	 * @return
	 */
    public void move(Node[] nodes, Node destinationParent, boolean force_del, MessageHandler handler) {
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
        params.put(Pydio.PARAM_ACTION, Pydio.ACTION_MOVE);
		fillParams(params, nodes);
		params.put(Pydio.PARAM_DEST, ((FileNode)destinationParent).path());
		if(force_del){
			params.put(Pydio.PARAM_FORCE_COPY_DELETE, "true");
		}
		Document doc = transport.getXmlContent(Pydio.ACTION_MOVE, params);
        handler.onMessage(PydioMessage.create(doc));
	}
	/**
	 * 
	 * @param node
	 * @param dirname
	 * @return
	 */
    public void createFolder(Node node, String dirname, MessageHandler handler) {
        Map<String, String> params = new HashMap<String , String>();
        if(workspace != null) {
            params.put(Pydio.PARAM_WORKSPACE, workspace.getId());
        }
        params.put(Pydio.PARAM_ACTION, Pydio.ACTION_MKDIR);
        params.put(Pydio.PARAM_DIR, node.path());
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

    public InputStream getAuthenticationChallenge(String type){
        //return transport.getAuthenticationChallenge();
        if(Pydio.AUTH_CHALLENGE_TYPE_CAPTCHA.equals(type)) {
            boolean image = false;
            HttpResponse resp = transport.getResponse(Pydio.ACTION_CAPTACHA, null);
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
        return Pydio.SERVER_CHECKED_OK;
    }
    /*
        public void changes(Node node, ChangeProcessor processor){}
        public void crossCopy(Node node, Workspace workspace, MessageHandler
        Message lsync( from,  to, copy)
        Message applyCheck(node);
        Message compress(nodes, archiveName, compressflat)
        Message prepareChunkDownload( node, chunkCount)
        Message downloadChunk(fileID,chunkIndex)
	*/

    //*********************************************
    //            OTHERS
    //*********************************************
    /**
     * @param params
     * @param nodes
     */
    private void fillParams(Map<String, String> params, Node[] nodes){
        if(nodes != null){
            if(nodes.length == 1){
                params.put(Pydio.PARAM_FILE, ((FileNode)(nodes[0])).path());
                return;
            }
            for(int i = 0; i < nodes.length; i++){
                String path = ((FileNode)nodes[i]).path();
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

    public int authenticationStatus(){
        return transport.authenticationStatus();
    }

    public void setCredentialsProvider(CredentialsProvider cp){
        transport.setCredentialsProvider(cp);
    }

}
