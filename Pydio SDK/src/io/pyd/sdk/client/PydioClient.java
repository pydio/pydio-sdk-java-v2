
package io.pyd.sdk.client;

import io.pyd.sdk.client.auth.CredentialsProvider;
import io.pyd.sdk.client.http.CountingMultipartRequestEntity;
import io.pyd.sdk.client.model.FileNode;
import io.pyd.sdk.client.model.Message;
import io.pyd.sdk.client.model.Node;
import io.pyd.sdk.client.model.NodeFactory;
import io.pyd.sdk.client.transport.Transport;
import io.pyd.sdk.client.transport.TransportFactory;
import io.pyd.sdk.client.utils.Pydio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.EncodingUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * 
 * @author pydio
 *
 */
public class PydioClient {
	
	Transport transport;
	

	
	/**
	 * 
	 * @param mode
	 * @param p
	 */
	public PydioClient(int mode, CredentialsProvider p){
		transport = TransportFactory.getInstance(mode, p);
	}
	
	
	
	
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
	 * @param node
	 * @param lsRecurseOptions
	 * @param lsOrderOptions
	 * @return
	 */
	ArrayList<Node> listChildren(Node node, Map<String, String> lsRecurseOptions, Map <String, String> lsOrderOptions){
		
		String action;
		Map<String, String> params = new HashMap<String , String>();
		
		if(node.type() == Node.TYPE_SERVER){
			action = Pydio.ACTION_LIST_REPOSITORIES;
		}else{			
			action = Pydio.ACTION_LIST;
			params.put(Pydio.PARAM_OPTIONS, "al");			
			if(lsRecurseOptions != null){
				params.putAll(lsRecurseOptions);
			}
			
			if(lsOrderOptions != null){
				params.putAll(lsOrderOptions);
			}
		}		
		
		Document doc = transport.getXmlContent(action , params);
		NodeList entries;
		if(node.type() != Node.TYPE_SERVER){
			entries = doc.getDocumentElement().getChildNodes();
		}else{
			entries = doc.getElementsByTagName("repo");
		} 
		
		ArrayList<Node> nodes = new ArrayList<Node>();		
		for(int i = 0; i < entries.getLength(); i++){
			org.w3c.dom.Node xmlNode = entries.item(i);			
			nodes.add(NodeFactory.createNode(xmlNode));
		}
		return nodes;
	}
	
		
	/** 
	 * @param target
	 * @param source
	 * @param ProgressHandler
	 * @param autoRename
	 * @param encodedFilename
	 * @param append
	 * @return
	 */
	Message write(Node node, File source, CountingMultipartRequestEntity.ProgressListener progressHandler , boolean autoRename, String name){
		
		String action = Pydio.ACTION_UPLOAD;
		Map<String, String> params = new HashMap<String , String>();
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
	
		transport.putContent(action, params, source, fname, progressHandler);
		
		if(tmp_name != null){
			rename(node, tmp_name);
		}
		return null;
	}
	

	/**
	 * 
	 * @param node
	 * @param data
	 * @param name
	 * @return
	 */
	/*Message write(Node node, byte[] data, String name){
		
		String action = Pydio.ACTION_PUT_CONTENT;
		Map<String, String> params = new HashMap<String , String>();
		
		params.put(Pydio.PARAM_NODE, node.path());
		params.put(Pydio.PARAM_CONTENT, );
		params.put(Pydio.PARAM_ENCODE, "base64");
		try {
			params.put(Pydio.PARAM_APPENDTO_URLENCODED_PART, java.net.URLEncoder.encode(name, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	
		transport.putContent(action, params, data, name, null);

		return null;
	}*/
	
	
	/**
	 * 
	 * @param nodes
	 * @param outputStream
	 * @param ProgressHandler
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	void read(Node[] nodes, OutputStream outputStream, ProgressHandler progressHandler){
		String action = Pydio.ACTION_DOWNLOAD;
		Map<String, String> params = new HashMap<String , String>();
		fillParams(params, nodes);		
		
		InputStream stream = null;
		try {
			stream = transport.getResponse(action, params).getEntity().getContent();
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
		
		// if(stream == null) // TODO
		int read = 0;
		byte[] buffer = new byte[1024];		
		int i = 0;
		try {
			for(;;){
				read = stream.read(buffer);
				if(read == -1) break;
				outputStream.write(buffer, 0, read);
				
				if(progressHandler != null){
					progressHandler.onProgress(i*1024 + read);
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
	 * @param nodes
	 * @param target
	 * @param ProgressHandler
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws IllegalStateException 
	 */
	void read(Node[] nodes, File target, ProgressHandler ProgressHandler ) throws IllegalStateException, FileNotFoundException, IOException{
		read(nodes, new FileOutputStream(target), null);		
	}

	/**
	 * 
	 * @param nodes
	 * @return
	 */
	Message remove(Node[] nodes){		
		String action = Pydio.ACTION_DELETE;
		Map<String, String> params = new HashMap<String , String>();		
		fillParams(params, nodes);
		Document doc = transport.getXmlContent(action, params);		
		return Message.create(doc);
	}
	
	/**
	 * 
	 * @param node
	 * @param newBaseName
	 * @return
	 */
	Message rename(Node node, String newBaseName){
		String action = Pydio.ACTION_RENAME;
		Map<String, String> params = new HashMap<String , String>();
		params.put(Pydio.PARAM_FILE, node.path());	
		if(newBaseName.contains("/")){
			params.put(Pydio.PARAM_DEST, newBaseName);
		}else{
			params.put(Pydio.PARAM_FILENAME_NEW, newBaseName);
		}
		Document doc = transport.getXmlContent(action, params);
		return Message.create(doc);
	}
	
	/**
	 * 
	 * @param nodes
	 * @param destinationParent
	 * @return
	 */
	Message copy(Node[] nodes, Node destinationParent){
		String action = Pydio.ACTION_COPY;
		Map<String, String> params = new HashMap<String , String>();		
		fillParams(params, nodes);
		params.put(Pydio.PARAM_DEST, ((FileNode)destinationParent).path());
		Document doc = transport.getXmlContent(action, params);		
		return Message.create(doc);
	}
	
	/**
	 * 
	 * @param nodes
	 * @param destinationParent
	 * @return
	 */
	Message move(Node[] nodes, Node destinationParent, boolean force_del){
		String action = Pydio.ACTION_MOVE;
		Map<String, String> params = new HashMap<String , String>();	
		fillParams(params, nodes);
		params.put(Pydio.PARAM_DEST, ((FileNode)destinationParent).path());
		if(force_del){
			params.put(Pydio.PARAM_FORCE_COPY_DELETE, "true");
		}
		Document doc = transport.getXmlContent(action, params);
		return Message.create(doc);
	}
		
	/**
	 * @param node
	 * @param dirname
	 * @return
	 */
	Message createFolder(Node node, String dirname){
		String action = Pydio.ACTION_MKDIR;
		
		Map<String, String> params = new HashMap<String , String>();
		params.put(Pydio.PARAM_DIR, node.path());
		params.put(Pydio.PARAM_DIRNAME, dirname);
		
		Document doc = transport.getXmlContent(action, params);
		return Message.create(doc);
	}
	
	/** 
	 * @param path
	 * @return
	 */
	Message createFile(String path){
		String action = Pydio.ACTION_MKFILE;
		Map<String, String> params = new HashMap<String , String>();
		params.put(Pydio.PARAM_NODE, path);
		
		Document doc = transport.getXmlContent(action, params);
		return Message.create(doc);
	}
	
	
	
	/*
	StatResult[] stat( nodes)
	Message lsync( from,  to, copy) 
	Message applyCheck(node);
	Message compress(nodes, archiveName, compressflat)
	Message prepareChunkDownload( node, chunkCount)
	Message downloadChunk(fileID,chunkIndex)
	*/
}
