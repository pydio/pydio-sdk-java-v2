package io.pyd.sdk.client.transport;
import java.io.InputStream;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.w3c.dom.Document;

public interface Transport{
	
	public static int TRANSPORT_MODE_SESSION = 1;
	public static int TRANSPORT_MODE_RESTFUL = 2;
	public static int TRANSPORT_MODE_MOCK = 3;
	
	public final static String PARAM_NODE 						= "node";
	public final static String PARAM_NODES 						= "nodes";
	public final static String PARAM_DIRNAME 					= "dirname";
	public final static String PARAM_PATH 						= "path";
	public final static String PARAM_URL_ENCODED 				= "url_encoded";
	public final static String PARAM_OPTIONS 					= "options";
	public final static String PARAM_RECURSIVE 					= "recursive";
	public final static String PARAM_DIR 						= "dir";
	public final static String PARAM_FILE 						= "file";
	public final static String PARAM_REMOTE_ORDER 				= "remote_order";
	public final static String PARAM_MAX_NODES 					= "max_nodes";
	public final static String PARAM_MAX_DEPTH 					= "max_depth";
	public final static String PARAM_ORDER_COLUMN 				= "order_column";
	public final static String PARAM_ORDER_DIRECTION 			= "order_direction";
	public final static String PARAM_INPUT_UPLOAD 				= "input_upload";
	public final static String PARAM_AUTO_RENAME 				= "auto_rename";
	public final static String PARAM_APPENDTO_URLENCODED_PART 	= "appendto_urlencoded_part";
	public final static String PARAM_ARCHIVE 					= "archive";
	public final static String PARAM_COMPRESS_FLAT 				= "compress_flat";
	public final static String PARAM_FROM 						= "from";
	public final static String PARAM_TO 						= "to";
	public final static String PARAM_COPY 						= "copy";
	public final static String PARAM_ENCODE 					= "encode";
	public final static String PARAM_FILENAME_NEW 				= "filename_new";
	public final static String PARAM_DEST 						= "dest";
	public final static String PARAM_FORCE_COPY_DELETE 			= "force_copy_delete";
	public final static String PARAM_CHMOD_VALUE 				= "chmod_value";
	public final static String PARAM_FILE_ID 					= "file_id";
	public final static String PARAM_CHUNK_INDEX 				= "chunk_index";
	public final static String PARAM_CHUNK_COUNT 				= "chunk_count";
	
	
	public final static String ACTION_LIST 						= "ls";
	public final static String ACTION_UPLOAD 					= "upload";
	public final static String ACTION_DOWNLOAD 					= "downloas";
	public final static String ACTION_COMPRESS 					= "compress";
	public final static String ACTION_LSYNC 					= "lsync";
	public final static String ACTION_APPLY_CHECK_HOOK 			= "apply_check_hook";
	public final static String ACTION_GET_CONTENT 				= "get_content";
	public final static String ACTION_PUT_CONTENT 				= "put_content";
	public final static String ACTION_RESTORE 					= "retore";
	public final static String ACTION_MKDIR 					= "mkdir";
	public final static String ACTION_MKFILE 					= "mkfile";
	public final static String ACTION_RENAME 					= "rename";
	public final static String ACTION_COPY 						= "copy";
	public final static String ACTION_MOVE 						= "move";
	public final static String ACTION_DELETE 					= "delete";
	public final static String ACTION_CHMOD 					= "chmod";
	public final static String ACTION_PREPARE_CHUNK_DL 			= "prepare_chunk_dl";
	public final static String ACTION_DOWNLOAD_CHUNK 			= "download_chunk";
	public final static String ACTION_PURGE 					= "purge";
	
	
	public HttpResponse getResponse(String action, Map<String, String> params);
	public String getStringContent(String action, Map<String, String> params);
	public Document getXmlContent(String action, Map<String, String> params);
	public JSONObject getJsonContent(String action, Map<String, String> params);
	public InputStream getResponseStream(String action, Map<String, String> params);	
}