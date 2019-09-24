package com.pydio.sdk.core;


/**
 * Class that defines the Pydio Protocol Constant
 *
 * @author pydio
 */
public class Pydio {
    public final static String XML_MESSAGE = "message";

    public final static String NODE_PROPERTY_UUID = "Uuid";
    public final static String NODE_PROPERTY_ENCODED = "encoded";
    public final static String NODE_PROPERTY_ENCODING = "encoding";
    public final static String NODE_PROPERTY_ENCODED_HASH = "encoded_hash";
    public final static String NODE_PROPERTY_BOOKMARK = "bookmark";
    public final static String NODE_PROPERTY_TYPE = "Type";
    public final static String XML_NODES_DIFF = "nodes_diff";
    public final static String NODE_PROPERTY_ID = "id";
    public final static String NODE_PROPERTY_AJXP_MIME = "ajxp_mime";
    public final static String NODE_PROPERTY_AJXP_SHARED = "ajxp_shared";
    public final static String NODE_PROPERTY_SHARE_JSON_INFO = "share_json_info";
    public final static String NODE_PROPERTY_SHARE_UUID = "share_Uuid";
    public final static String NODE_PROPERTY_META_JSON_ENCODED = "meta_encoded";
    public final static String NODE_PROPERTY_JSON_ENCODED = "encoded";
    public final static String NODE_PROPERTY_AJXP_MODIFTIME = "ajxp_modiftime";
    public final static String NODE_PROPERTY_BYTESIZE = "bytesize";
    public final static String NODE_PROPERTY_FILE_GROUP = "file_group";
    public final static String NODE_PROPERTY_FILE_OWNER = "file_owner";
    public final static String NODE_PROPERTY_FILE_PERMS = "file_perms";
    public final static String NODE_PROPERTY_FILE_SIZE = "filesize";
    public final static String NODE_PROPERTY_FILENAME = "filename";
    public final static String NODE_PROPERTY_ORIGINAL_PATH = "original_path";
    public final static String NODE_PROPERTY_ICON = "icon";
    public final static String NODE_PROPERTY_IS_FILE = "is_file";
    public final static String NODE_PROPERTY_IS_IMAGE = "is_image";
    public final static String NODE_PROPERTY_SHARE_LINK = "share_link";
    public final static String NODE_PROPERTY_SHARE_LINK_DESCRIPTION = "share_link_description";
    public final static String NODE_PROPERTY_SHARE_LINK_EXPIRED = "share_link_expired";
    public final static String NODE_PROPERTY_META_FIELDS = "meta_fields";
    public final static String NODE_PROPERTY_META_LABELS = "meta_labels";
    public final static String NODE_PROPERTY_META_TYPES = "meta_types";
    public final static String NODE_PROPERTY_MIMESTRING = "mimestring";
    public final static String NODE_PROPERTY_MIMESTRING_ID = "mimestring_id";
    public final static String NODE_PROPERTY_READABLE_DIMENSION = "readable_dimension";
    public final static String NODE_PROPERTY_OPEN_ICON = "openicon";
    public final static String NODE_PROPERTY_REPO_HAS_RECYCLE_BIN = "repo_has_recycle";
    public final static String NODE_PROPERTY_TEXT = "text";
    public final static String NODE_PROPERTY_IMAGE_HEIGHT = "image_height";
    public final static String NODE_PROPERTY_IMAGE_WIDTH = "image_width";
    public final static String NODE_PROPERTY_IMAGE_TYPE = "image_type";
    public final static String NODE_PROPERTY_IMAGE_THUMB_PATHS = "image_thumb_url_prefix";
    public final static String NODE_PROPERTY_LABEL = "label";
    public final static String NODE_PROPERTY_WORKSPACE_ID = "workspace_id";
    public final static String NODE_PROPERTY_WORKSPACE_UUID = "workspace_id";
    public final static String NODE_PROPERTY_WORKSPACE_SLUG = "workspace_slug";

    public final static String NODE_PROPERTY_DESCRIPTION = "description";

    public final static String MESSAGE_PROPERTY_TYPE = "type";
    public final static String WORKSPACE_PROPERTY_OWNER = "owner";
    public final static String WORKSPACE_PROPERTY_ID = "id";
    public final static String WORKSPACE_PROPERTY_ACCESS_TYPE = "access_type";
    public final static String WORKSPACE_PROPERTY_CROSS_COPY = "allowCrossRepositoryCopy";
    public final static String WORKSPACE_PROPERTY_META_SYNC = "meta_syncable_REPO_SYNCABLE";
    public final static String WORKSPACE_PROPERTY_SLUG = "repositorySlug";
    public final static String WORKSPACE_PROPERTY_ACL = "acl";
    public final static String WORKSPACE_DESCRIPTION = "description";
    public final static String WORKSPACE_IS_PUBLIC = "is_public";


    public final static String WORKSPACE_ACCESS_TYPE_CONF = "ajxp_conf";
    public final static String WORKSPACE_ACCESS_TYPE_SHARED = "ajxp_shared";
    public final static String WORKSPACE_ACCESS_TYPE_MYSQL = "mysql";
    public final static String WORKSPACE_ACCESS_TYPE_IMAP = "imap";
    public final static String WORKSPACE_ACCESS_TYPE_JSAPI = "jsapi";
    public final static String WORKSPACE_ACCESS_TYPE_USER = "ajxp_user";
    public final static String WORKSPACE_ACCESS_TYPE_HOME = "ajxp_home";
    public final static String WORKSPACE_ACCESS_TYPE_HOMEPAGE = "homepage";
    public final static String WORKSPACE_ACCESS_TYPE_ADMIN = "ajxp_admin";
    public final static String WORKSPACE_ACCESS_TYPE_FS = "fs";
    public final static String WORKSPACE_ACCESS_TYPE_GATEWAY = "gateway";
    public final static String WORKSPACE_ACCESS_TYPE_SETTINGS = "settings";
    public final static String WORKSPACE_ACCESS_TYPE_INBOX = "inbox";

    public final static String NODE_DIFF_ADD = "add";
    public final static String NODE_DIFF_UPDATE = "update";
    public final static String NODE_DIFF_REMOVE = "remove";
    public final static String NODE_PROPERTY_PATH = "path";
    public final static String NODE_WORKSPACE = "workspace";

    public final static String REMOTE_CONFIG_UPLOAD_SIZE = "UPLOAD_MAX_SIZE";
    public final static String LOCAL_CONFIG_BUFFER_SIZE = "buffer_size";
    public final static int LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE = 2048;


    public static final String COOKIES_USER = "user";
    public static final String COOKIES_PASSWORD = "password";
    public static final String WORKSPACE_ID = "workspace_id";
    public static final String NODE_PATH = "path";
    public static final String FILE_PATH = "file_path";
    public static final String OFFLINE_NODE_HASH = "hash";
    public static final String OFFLINE_NODE_SIZE = "size";
    public static final String OFFLINE_NODE_ENCRYPTED = "encrypted";
    public static final String BOOKMARKS_PATH = "path";
    public static final String PROPERTIES_NAME = "name";
    public static final String PROPERTIES_VALUE = "value";
    public static final String ALIAS = "alias";
    public static final String CERTIFICATE = "certificate";
    public static final String SERVER = "server";
    public static final String SESSION_ID = "session_id";
    public static final String TASK_ID = "task_id";
    public static final String FOLDER_NAME = "folder_name";
    public static final String ADDRESS = "address";
    public static final String NODE_DATA = "node";
    public static final String SERVER_ID = "server_id";
    public static final String LOGIN = "user";
    public static final String DISPLAYED_NAME = "user_display_name";
    public static final String LOGO = "logo";
    public static final String SESSION_NAME = "session_name";
    public static final String REMEMBER_PASSWORD = "remember_password";
    public static final String CHANGE_LOCATION = "location";


    public final static String CHANGE_NODE_ID = "node_id";
    public final static String CHANGE_SEQ = "seq";
    public final static String CHANGE_TYPE = "type";
    public final static String CHANGE_SOURCE = "source";
    public final static String CHANGE_TARGET = "target";
    public final static String CHANGE_NODE_BYTESIZE = "bytesize";
    public final static String CHANGE_NODE_MD5 = "md5";
    public final static String CHANGE_NODE_MTIME = "mtime";
    public final static String CHANGE_NODE_PATH = "node_path";
    public final static String CHANGE_NODE_WORKSPACE = "repository_identifier";
    public final static String CHANGE_NODE = "node";


    public final static int CHANGE_INDEX_SEQ = 0;
    public final static int CHANGE_INDEX_NODE_ID = 1;
    public final static int CHANGE_INDEX_TYPE = 2;
    public final static int CHANGE_INDEX_SOURCE = 3;
    public final static int CHANGE_INDEX_TARGET = 4;
    public final static int CHANGE_INDEX_NODE_BYTESIZE = 5;
    public final static int CHANGE_INDEX_NODE_MD5 = 6;
    public final static int CHANGE_INDEX_NODE_MTIME = 7;
    public final static int CHANGE_INDEX_NODE_PATH = 8;
    public final static int CHANGE_INDEX_NODE_WORKSPACE = 9;
}
