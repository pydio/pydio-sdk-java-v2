package pydio.sdk.java.core.utils;


/**
 * Class that defines the Pydio Protocol Constant
 * @author pydio
 *
 */
public class Pydio {
	
	public final static String PARAM_GET_ACTION					        = "get_action";
	public final static String PARAM_SUB_ACTION					        = "sub_action";

	public final static String PARAM_ACTION					            = "action";
	public final static String PARAM_SHARE_ELEMENT_TYPE					= "element_type";
	public final static String PARAM_CAPTCHA_CODE				        = "captcha_code";
	public final static String PARAM_NODE 						        = "node";
	public final static String PARAM_NODES 					    	    = "nodes";
	public final static String PARAM_DIRNAME 					        = "dirname";
	public final static String PARAM_PATH 						        = "path";
	public final static String PARAM_URL_ENCODED_FILENAME               = "urlencoded_filename";
	public final static String PARAM_OPTIONS 					        = "options";
	public final static String PARAM_RECURSIVE 					        = "recursive";
	public final static String PARAM_CONTENT 					        = "content";
	public final static String PARAM_DIR 						        = "dir";
	public final static String PARAM_FILE 						        = "file";
	public final static String PARAM_REMOTE_ORDER 				        = "remote_order";
	public final static String PARAM_MAX_NODES 					        = "max_nodes";
	public final static String PARAM_MAX_DEPTH 					        = "max_depth";
	public final static String PARAM_ORDER_COLUMN 				        = "order_column";
	public final static String PARAM_ORDER_DIRECTION 			        = "order_direction";
	public final static String PARAM_INPUT_UPLOAD 				        = "input_upload";
	public final static String PARAM_AUTO_RENAME 				        = "auto_rename";
	public final static String PARAM_APPEND_TO_URLENCODED_PART          = "appendto_urlencoded_part";
	public final static String PARAM_ARCHIVE_NAME                       = "archive_name";
	public final static String PARAM_COMPRESS_FLAT 				        = "compress_flat";
	public final static String PARAM_FROM 						        = "from";
	public final static String PARAM_TO 						        = "to";
	public final static String PARAM_COPY 						        = "copy";
	public final static String PARAM_ENCODE 					        = "encode";
	public final static String PARAM_FILENAME_NEW 				        = "filename_new";
	public final static String PARAM_DEST 						        = "dest";
	public final static String PARAM_FORCE_COPY_DELETE 			        = "force_copy_delete";
	public final static String PARAM_CHMOD_VALUE 				        = "chmod_value";
	public final static String PARAM_FILE_ID 					        = "file_id";
	public final static String PARAM_CHUNK_INDEX 				        = "chunk_index";
	public final static String PARAM_CHUNK_COUNT 				        = "chunk_count";
	public final static String PARAM_XHR_UPLOADER				        = "xhr_uploader";
    public final static String PARAM_XPATH                              = "xPath";
    public static final String PARAM_TEMP_WORKSPACE                     = "tmp_repository_id";
    public static final String PARAM_WORKSPACE                          = "repository_id";
    public static final String PARAM_SECURE_TOKEN                       = "secure_token";
    public static final String PARAM_REMEMBER_ME                        = "remember_me";
    public static final String PARAM_DIMENSION                          = "dimension";
    public static final String PARAM_GET_THUMB                          = "get_thumb";
    public static final String PARAM_SHARE_GUEST_USER_PASSWORD          = "guest_user_pass";
    public static final String PARAM_SHARE_EXPIRATION                   = "expiration";
    public static final String PARAM_SHARE_DOWNLOAD                     = "downloadlimit";
    public static final String PARAM_SHARE_WORKSPACE_LABEL              = "repo_label";
    public static final String PARAM_SHARE_WORKSPACE_DESCRIPTION        = "repo_description";
    public static final String PARAM_SEARCH_QUERY                       = "query";
    public static final String PARAM_SEARCH_LIMIT                       = "limit";
    public static final String PARAM_BINARY_ID                          = "binary_id";
    public static final String PARAM_USER_ID                            = "user_id";

    public static final String PARAM_CHANGE_SEQ_ID                      = "seq_id";
    public static final String PARAM_CHANGE_FLATTEN                     = "flatten";
    public static final String PARAM_CHANGE_STREAM                      = "stream";
    public static final String PARAM_CHANGE_FILTER                      = "filter";





	public final static String ACTION_LIST 						        = "ls";
	public final static String ACTION_GET_BOOT_CONF 					= "get_boot_conf";
	public final static String ACTION_LOGOUT 						    = "logout";
	public final static String ACTION_UPLOAD 					        = "upload";
	public final static String ACTION_DOWNLOAD 					        = "download";
	public final static String ACTION_CAPTCHA                           = "get_captcha";
	public final static String ACTION_COMPRESS 					        = "compress";
	public final static String ACTION_LSYNC 					        = "lsync";
	public final static String ACTION_APPLY_CHECK_HOOK 			        = "apply_check_hook";
	public final static String ACTION_GET_CONTENT 				        = "get_content";
	public final static String ACTION_PUT_CONTENT 				        = "put_content";
	public final static String ACTION_RESTORE 					        = "restore";
	public final static String ACTION_MKDIR 					        = "mkdir";
	public final static String ACTION_MKFILE 					        = "mkfile";
	public final static String ACTION_RENAME 					        = "rename";
	public final static String ACTION_COPY 						        = "copy";
	public final static String ACTION_MOVE 						        = "move";
	public final static String ACTION_DELETE 					        = "delete";
	public final static String ACTION_CREATE_MINISITE 					= "create_minisite";
	public final static String ACTION_SHARE 					        = "share";
	public final static String ACTION_SEARCH 					        = "search";
	public final static String ACTION_UNSHARE 					        = "unshare";
    public final static String ACTION_CHMOD 					        = "chmod";
    public final static String ACTION_PREPARE_CHUNK_DL 			        = "prepare_chunk_dl";
    public final static String ACTION_DOWNLOAD_CHUNK 			        = "download_chunk";
    public final static String ACTION_PURGE 					        = "purge";
    public final static String ACTION_GET_TOKEN                         = "get_boot_conf";
    public final static String ACTION_GET_SEED                          = "get_seed";
    public final static String ACTION_LOGIN                             = "login";
    public final static String ACTION_GET_REGISTRY                      = "get_xml_registry";
    public final static String ACTION_LIST_USERS                        = "user_list_authorized_users";
    public final static String ACTION_CREATE_USER                       = "user_create_user";
    public final static String ACTION_CONF_PREFIX                       = "ajxp_conf/";
    public final static String ACTION_PREVIEW_DATA_PROXY                = "preview_data_proxy";
    public final static String ACTION_IMAGICK_DATA_PROXY                = "imagick_data_proxy";
    public final static String ACTION_STATS                             = "stat";
    public final static String ACTION_CHANGES                           = "changes";
    public final static String ACTION_LOAD_SHARED_ELEMENT_DATA          = "load_shared_element_data";
    public final static String ACTION_GET_BINARY_PARAM                  = "get_binary_param";



    public final static String PARAM_SHARE_ELEMENT_TYPE_FILE            = "file";
    public final static String PARAM_SHARED_ELEMENT_TYPE_MINISITE       = "minisite";
    public final static String PARAM_SHARED_ELEMENT_TYPE_WORKSPACE      = "repository";





    public final static String XPATH_USER_WORKSPACES                    = "user/repositories";
    public final static String XPATH_USER_ACTIVE_WORKSPACE              = "user/active_repo";
    public final static String XPATH_VALUE_PLUGINS				        = "plugins";
    public final static String XML_TREE 						        = "tree";
    public final static String XML_MESSAGE 						        = "message";


	public final static String XML_NODES_DIFF 					        = "nodes_diff";
    public final static String NODE_PROPERTY_AJXP_MIME 			        = "ajxp_mime";
    public final static String NODE_PROPERTY_AJXP_SHARED 			    = "ajxp_shared";
    public final static String NODE_PROPERTY_AJXP_MODIFTIME 	        = "ajxp_modiftime";
    public final static String NODE_PROPERTY_BYTESIZE 			        = "bytesize";
    public final static String NODE_PROPERTY_FILE_GROUP 		        = "file_group";
    public final static String NODE_PROPERTY_FILE_OWNER 		        = "file_owner";
    public final static String NODE_PROPERTY_FILE_PERMS 		        = "file_perms";
    public final static String NODE_PROPERTY_FILE_SIZE 			        = "filesize";
    public final static String NODE_PROPERTY_FILENAME 			        = "filename";
    public final static String NODE_PROPERTY_ORIGINAL_PATH		        = "original_path";
    public final static String NODE_PROPERTY_ICON 				        = "icon";
    public final static String NODE_PROPERTY_IS_FILE 			        = "is_file";
    public final static String NODE_PROPERTY_IS_IMAGE 			        = "is_image";
    public final static String NODE_PROPERTY_SHARE_LINK 			    = "share_link";
    public final static String NODE_PROPERTY_SHARE_LINK_DESCRIPTION     = "share_link_description";
    public final static String NODE_PROPERTY_SHARE_LINK_EXPIRED 		= "share_link_expired";
    public final static String NODE_PROPERTY_META_FIELDS 		        = "meta_fields";
    public final static String NODE_PROPERTY_META_LABELS 		        = "meta_labels";
    public final static String NODE_PROPERTY_META_TYPES 		        = "meta_types";
    public final static String NODE_PROPERTY_MIMESTRING 		        = "mimestring";
    public final static String NODE_PROPERTY_MIMESTRING_ID 		        = "mimestring_id";
    public final static String NODE_PROPERTY_READABLE_DIMENSION         = "readable_dimension";
    public final static String NODE_PROPERTY_OPEN_ICON 			        = "openicon";
    public final static String NODE_PROPERTY_REPO_HAS_RECYCLE_BIN       = "repo_has_recycle";
    public final static String NODE_PROPERTY_TEXT 				        = "text";
    public final static String NODE_PROPERTY_IMAGE_HEIGHT 		        = "image_height";
    public final static String NODE_PROPERTY_IMAGE_WIDTH 		        = "image_width";
    public final static String NODE_PROPERTY_IMAGE_TYPE 		        = "image_type";
    public final static String NODE_PROPERTY_LABEL 		                = "label";

	public final static String NODE_PROPERTY_DESCRIPTION 		        = "description";

	public final static String MESSAGE_PROPERTY_TYPE 			        = "type";
	public final static String WORKSPACE_PROPERTY_OWNER 			    = "owner";
    public final static String WORKSPACE_PROPERTY_ID                    = "id";
    public final static String WORKSPACE_PROPERTY_ACCESS_TYPE           = "access_type";
    public final static String WORKSPACE_PROPERTY_CROSS_COPY            = "allowCrossRepositoryCopy";
    public final static String WORKSPACE_PROPERTY_META_SYNC  	        = "meta_syncable_REPO_SYNCABLE";
    public final static String WORKSPACE_PROPERTY_SLUG                  = "repositorySlug";
    public final static String WORKSPACE_PROPERTY_ACL                   = "acl";
    public final static String WORKSPACE_DESCRIPTION                    = "description";


    public final static String WORKSPACE_ACCESS_TYPE_CONF               = "ajxp_conf";
    public final static String WORKSPACE_ACCESS_TYPE_SHARED             = "ajxp_shared";
    public final static String WORKSPACE_ACCESS_TYPE_MYSQL              = "mysql";
    public final static String WORKSPACE_ACCESS_TYPE_IMAP               = "imap";
    public final static String WORKSPACE_ACCESS_TYPE_JSAPI              = "jsapi";
    public final static String WORKSPACE_ACCESS_TYPE_USER               = "ajxp_user";
    public final static String WORKSPACE_ACCESS_TYPE_HOME               = "ajxp_home";
    public final static String WORKSPACE_ACCESS_TYPE_ADMIN               = "ajxp_admin";
    public final static String WORKSPACE_ACCESS_TYPE_FS                 = "fs";
    public final static String WORKSPACE_ACCESS_TYPE_INBOX              = "inbox";


    public static final String FEAT_DO_NOT_SAVE_PASS                    = "MOBILE_SECURITY_FORCE_DONTSAVEPASS";
    public static final String FEAT_FORCE_PIN_CODE                      = "MOBILE_SECURITY_FORCE_PIN_CODE";
    public static final String FEAT_RE_AUTH_INTER                       = "MOBILE_SECURITY_FORCE_REAUTH";
    public static final String FEAT_DISABLE_SHARE                       = "MOBILE_SECURITY_OUTSIDE_SHARE";
    public static final String FEAT_DISABLE_OFFLINE                     = "MOBILE_SECURITY_DISABLE_OFFLINE";
    public static final String FEAT_DISABLE_BACKUP                      = "MOBILE_SECURITY_DISABLE_BACKUP";



    public final static String WORKSPACE_LABEL                          = "label";
    public final static String NODE_DIFF_ADD 					        = "add";
    public final static String NODE_DIFF_UPDATE 				        = "update";

	public final static String NODE_DIFF_REMOVE 				        = "remove";
    public final static String SERVER_PROPERTY_HOST                     = "host";
    public final static String SERVER_PROPERTY_LABEL                    = "label";
    public final static String SERVER_PROPERTY_SCHEME                   = "protocol";
    public final static String SERVER_PROPERTY_SELF_SIGNED              = "sslSelfSigned";

	public final static String NODE_PROPERTY_PATH                       = "path";

	public final static String REMOTE_CONFIG_UPLOAD_SIZE                = "UPLOAD_MAX_SIZE";
    public final static String LOCAL_CONFIG_BUFFER_SIZE                 = "buffer_size";
	public final static int LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE      = 16384;


    public static final String COOKIES_USER                      = "user";
    public static final String COOKIES_PASSWORD                  = "password";
    public static final String WORKSPACE_ID          = "workspace_id";
    public static final String NODE_PATH             = "path";
    public static final String FILE_PATH             = "file_path";
    public static final String OFFLINE_NODE_HASH     = "hash";
    public static final String OFFLINE_NODE_SIZE     = "size";
    public static final String OFFLINE_NODE_ENCRYPTED= "encrypted";
    public static final String BOOKMARKS_PATH        = "path";
    public static final String PROPERTIES_NAME       = "name";
    public static final String PROPERTIES_VALUE      = "value";
    public static final String ALIAS                 = "alias";
    public static final String CERTIFICATE           = "certificate";
    public static final String SERVER                = "server";
    public static final String SESSION_ID            = "session_id";
    public static final String TASK_ID               = "task_id";
    public static final String FOLDER_NAME           = "folder_name";
    public static final String ADDRESS               = "address";
    public static final String NODE_DATA             = "node";
    public static final String SERVER_ID             = "server_id";
    public static final String LOGIN                 = "user";
    public static final String DISPLAYED_NAME        = "user_display_name";
    public static final String LOGO                  = "logo";
    public static final String SESSION_NAME          = "session_name";
    public static final String REMEMBER_PASSWORD     = "remember_password";
    public static final String CHANGE_LOCATION       = "location";


    public final static String CHANGE_NODE_ID                           = "node_id";
    public final static String CHANGE_SEQ                               = "seq";
    public final static String CHANGE_TYPE                              = "type";
    public final static String CHANGE_SOURCE                            = "source";
    public final static String CHANGE_TARGET                            = "target";
    public final static String CHANGE_NODE_BYTESIZE                     = "bytesize";
    public final static String CHANGE_NODE_MD5                          = "md5";
    public final static String CHANGE_NODE_MTIME                        = "mtime";
    public final static String CHANGE_NODE_PATH                         = "node_path";
    public final static String CHANGE_NODE_WORKSPACE                    = "repository_identifier";
    public final static String CHANGE_NODE                              = "node";


    public final static int CHANGE_INDEX_SEQ                            = 0;
    public final static int CHANGE_INDEX_NODE_ID                        = 1;
    public final static int CHANGE_INDEX_TYPE                           = 2;
    public final static int CHANGE_INDEX_SOURCE                         = 3;
    public final static int CHANGE_INDEX_TARGET                         = 4;
    public final static int CHANGE_INDEX_NODE_BYTESIZE                  = 5;
    public final static int CHANGE_INDEX_NODE_MD5                       = 6;
    public final static int CHANGE_INDEX_NODE_MTIME                     = 7;
    public final static int CHANGE_INDEX_NODE_PATH                      = 8;
    public final static int CHANGE_INDEX_NODE_WORKSPACE                 = 9;

    public final static String CHANGE_EVENT_CREATE                      = "create";
    public final static String CHANGE_EVENT_DELETE                      = "delete";
    public final static String CHANGE_EVENT_PATH                        = "path";
    public final static String CHANGE_EVENT_CONTENT                     = "content";



    public final static String REGISTRY_PREF_EMAIL                      = "email";
    public final static String REGISTRY_PREF_AVATAR                     = "avatar";
    public final static String REGISTRY_PREF_USER_DISPLAY_NAME          = "USER_DISPLAY_NAME";
    public final static String REGISTRY_PREF_DEFAULT_WS                 = "DEFAULT_START_REPOSITORY";


    public final static String AUTH_CHALLENGE_TYPE_CAPTCHA              = "CHALLENGE_CAPTCHA";
    public static final String ACTION_SWITCH_REPO                       = "switch_repository";
    public static String[] no_auth_required_actions                     = {Pydio.ACTION_CAPTCHA, Pydio.ACTION_GET_SEED, Pydio.ACTION_LOGIN};

    public final static int
            OK                                                          = 1,
            ERROR_NOT_A_SERVER                                          = 2,
            ERROR_WRONG_PATH                                            = 3,
            ERROR_CON_FAILED                                            = 4,
            ERROR_CON_FAILED_SSL                                        = 5,
            ERROR_UNVERIFIED_CERTIFICATE                                = 6,
            ERROR_IN_NAME_SYNTAX                                        = 7,
            ERROR_OTHER                                                 = 8,
            ERROR_SERVER_SLEEPING                                       = 9,
            ERROR_AUTHENTICATION                                        = 10,
            ERROR_AUTHENTICATION_WITH_CAPTCHA                           = 11,
            ERROR_BAD_METHOD                                            = 12,
            ERROR_NOT_FOUND                                             = 13,
            ERROR_USER_ALREADY_AUTH                                     = 14,
            ERROR_OLD_AUTHENTICATION_TOKEN                              = 15,
            ERROR_UNREACHABLE_HOST                                      = 16,
            ERROR_ACCESS_REFUSED                                        = 17,
            ERROR_CERTIFICATE_HOSTNAME_UNMATCH                          = 18;

}
