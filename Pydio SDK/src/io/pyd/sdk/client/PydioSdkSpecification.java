package io.pyd.sdk.client;

/*
*Message definition:
*
*
Message :
	type : message type int
	code : message code int
	content description : string description of the message String
	+ public fromXML();  : parameter : String or XMLDoc
	+ public fromJSON(); : parameter : String
*
*
*
*/

interface Message{
	public getType();
	public getCode();
	public getMessage();
}

interface statResult{
	
}

class PydioException extends Exception{}
class ServerException extends PydioException{}
class NetworkException extends PydioException{}
class AuthException extends PydioException{}

interface Node{
	
	public getPath();
	public getProperty();
	
	public Node[] loadChildren(NodeHandler handler, Map<String,Object> lsRecurseOptions, Map<String,Object> lsOrderOptions);
	
	public Message write(File source, ProgressHandler progress, boolean autoRename, String encodedFilename, String append);
	public Message write(InputStream data, ProgressHandler progress, boolean autoRename, String encodedFilename, String append);		
	
	public void read(OutputStream os, ProgressHandler progress);
	public void read(File target, ProgressHandler progress);

	public Message remove();
	public StatResult stat();
	public Message create();
	
	public Message rename(String newBaseName);
	public Message copy(Node destinationParent);
	public Message move(Node destinationParent);

		
}

interface NodeHandler{
	public handleNode(Node node);
}
interface TransferProgressHandler{
	
}

public interface PydioSdkSpecification{
	
	
	static String LS_RECURSE_KEY_FLAG = "recursive";
	static String LS_RECURSE_KEY_MAX_DEPTH = "max_depth";
	static String LS_RECURSE_KEY_MAX_NODES = "max_nodes";	
	
	static String LS_REMOTE_ORDER_KEY_FLAG = "remote_order";
	static String LS_REMOTE_ORDER_KEY_COLUMN = "order_column";
	static String LS_REMOTE_ORDER_KEY_DIR = "order_dir";
	
	void listNodes(NodeHandler handler, Node[] nodes, Map<String,Object> lsRecurseOptions, Map<String,Object> lsOrderOptions) throws PydioException;		
	void listChildren(NodeHandler handler, Node node, Map<String,Object> lsRecurseOptions, Map<String,Object> lsOrderOptions) throws PydioException;

	Node[] listNodes(Node[] nodes, Map<String,Object> lsRecurseOptions, Map<String,Object> lsOrderOptions) throws PydioException;		
	Node[] listChildren(Node node, Map<String,Object> lsRecurseOptions, Map<String,Object> lsOrderOptions) throws PydioException;

	
	Message write(Node target, File source, ProgressHandler progress, boolean autoRename, String encodedFilename, String append) throws PydioException;
	Message write(Node target, InputStream data, ProgressHandler progress, boolean autoRename, String encodedFilename, String append) throws PydioException;		
	
	void read(Node[] nodes, OutputStream os, ProgressHandler progress) throws PydioException;
	void read(Node[] nodes, File target, ProgressHandler progress) throws PydioException;

	Message remove(Node[] nodes) throws PydioException;
	StatResult[] stat(Node[] nodes) throws PydioException;

	Message createResource(Node newNode) throws PydioException;
	
	Message rename(Node node, String newBaseName) throws PydioException;
	Message copy(Node[] nodes, Node destinationParent) throws PydioException;
	Message move(Node[] nodes, Node destinationParent) throws PydioException;

	Message lsync(Node from, Node to, boolean copy) throws PydioException;
	Message applyCheck(Node node) throws PydioException;
	Message compress(Node[] nodes, String archiveName, boolean compressflat) throws PydioException;

	Message prepareChunkDownload(Node node, int chunkCount) throws PydioException;
	Message downloadChunk(String fileID, int chunkIndex) throws PydioException;
	
}