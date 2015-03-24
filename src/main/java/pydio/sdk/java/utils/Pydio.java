package pydio.sdk.java.utils;


/**
 * Class that defines the Pydio Protocol Constant
 * @author pydio
 *
 */
public class Pydio {
	
	public final static String PARAM_GET_ACTION					= "get_action";
	public final static String PARAM_ACTION					    = "action";
	public final static String PARAM_CAPTCHA_CODE				= "captcha_code";
	public final static String PARAM_NODE 						= "node";
	public final static String PARAM_NODES 						= "nodes";
	public final static String PARAM_DIRNAME 					= "dirname";
	public final static String PARAM_PATH 						= "path";
	public final static String PARAM_URL_ENCODED 				= "url_encoded";
	public final static String PARAM_OPTIONS 					= "options";
	public final static String PARAM_RECURSIVE 					= "recursive";
	public final static String PARAM_CONTENT 					= "content";
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
	public final static String PARAM_XHR_UPLOADER				= "xhr_uploader";
    public final static String PARAM_XPATH                      = "xPath";
    public static final String PARAM_WORKSPACE                  = "tmp_repository_id";
    public static final String PARAM_SECURE_TOKEN               = "secure_token";
    public static final String PARAM_REMEMBER_ME                = "remember_me";

	public final static String ACTION_LIST 						= "ls";
	public final static String ACTION_UPLOAD 					= "upload";
	public final static String ACTION_DOWNLOAD 					= "download";
	public final static String ACTION_CAPTACHA 					= "get_captcha";
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
    public final static String ACTION_GET_REGISTRY              = "get_xml_registry";

    public final static String XPATH_VALUE_USER_REPO			= "user/repositories";
    public final static String XPATH_VALUE_PLUGINS				= "plugins";
	
	public final static String AUTH_GET_TOKEN					= "get_boot_conf";
	public final static String AUTH_GET_SEED					= "get_seed";
	
	public final static String XML_TREE 						= "tree";
	public final static String XML_MESSAGE 						= "message";
	public final static String XML_NODES_DIFF 					= "nodes_diff";
	
	
	public final static String NODE_PROPERTY_AJXP_MIME 			= "ajxp_mime";
	public final static String NODE_PROPERTY_AJXP_MODIFTIME 	= "ajxp_modiftime";
	public final static String NODE_PROPERTY_BYTESIZE 			= "bytesize";
	public final static String NODE_PROPERTY_FILE_GROUP 		= "file_group";
	public final static String NODE_PROPERTY_FILE_OWNER 		= "file_owner";
	public final static String NODE_PROPERTY_FILE_PERMS 		= "file_perms";
	public final static String NODE_PROPERTY_FILE_SIZE 			= "filesize";
	public final static String NODE_PROPERTY_FILENAME 			= "filename";
	public final static String NODE_PROPERTY_ICON 				= "icon";
	public final static String NODE_PROPERTY_IS_FILE 			= "is_file";
	public final static String NODE_PROPERTY_IS_IMAGE 			= "is_image";
	public final static String NODE_PROPERTY_META_FIELDS 		= "meta_fields";
	public final static String NODE_PROPERTY_META_LABELS 		= "meta_labels";
	public final static String NODE_PROPERTY_META_TYPES 		= "meta_types";
	public final static String NODE_PROPERTY_MIMESTRING 		= "mimestring";
	public final static String NODE_PROPERTY_MIMESTRING_ID 		= "mimestring_id";
	public final static String NODE_PROPERTY_READABLE_DIMENSION = "readable_dimension";
	public final static String NODE_PROPERTY_OPEN_ICON 			= "openicon";
	public final static String NODE_PROPERTY_REPO_HAS_RECYCLE_BIN = "repo_has_recycle";
	public final static String NODE_PROPERTY_TEXT 				= "text";
	public final static String NODE_PROPERTY_IMAGE_HEIGHT 		= "image_height";
	public final static String NODE_PROPERTY_IMAGE_WIDTH 		= "image_width";
	public final static String NODE_PROPERTY_IMAGE_TYPE 		= "image_type";
	public final static String NODE_PROPERTY_LABEL 		        = "label";
	public final static String NODE_PROPERTY_DESCRIPTION 		= "description";

	public final static String MESSAGE_PROPERTY_TYPE 			= "type";
	
	public final static String REPO_PROPERTY_ID					= "id";
	public final static String REPO_PROPERTY_ACCESS_TYPE		= "access_type";
	public final static String REPO_PROPERTY_CROSS_COPY 		= "allowCrossRepositoryCopy";
	public final static String REPO_PROPERTY_SLUG 				= "repositorySlug";
	public final static String REPO_DESCRIPTION 				= "description";
	public final static String REPO_LABEL 						= "label";	
	
	public final static String NODE_DIFF_ADD 					= "add";
	public final static String NODE_DIFF_UPDATE 				= "update";
	public final static String NODE_DIFF_REMOVE 				= "remove";
	
	public final static String SERVER_PROPERTY_HOST             = "host";
	public final static String SERVER_PROPERTY_LABEL            = "label";
	public final static String SERVER_PROPERTY_SCHEME           = "protocol";
	public final static String SERVER_PROPERTY_SELF_SIGNED      = "sslSelfSigned";
	public final static String SERVER_PROPERTY_PATH             = "path";

	public final static String REMOTE_CONFIG_UPLOAD_SIZE        = "//property[@name='UPLOAD_MAX_SIZE']";
	
	public final static String LOCAL_CONFIG_BUFFER_SIZE         = "buffer_size";
	public final static int LCONFIG_BUFFER_SIZE_DVALUE  		= 16384;

    public final static int SESSION_STATE_NONE                  = 0;
    public final static int SESSION_STATE_AUTH_REQUIRED         = 1;
    public final static int SESSION_STATE_AUTH_REQUIRED_WITH_CAPTCHA = 2;
    public final static int SESSION_STATE_OK                    = 3;


    public final static String AUTH_CHALLENGE_TYPE_CAPTCHA      = "CHALLENGE_CAPTCHA";

    public static String[] no_auth_required_actions = {Pydio.ACTION_CAPTACHA};

}
