package com.pydio.sdk.core.model;


import com.pydio.sdk.core.Pydio;

import java.util.Properties;

/**
 * 
 * @author pydio
 *
 */
public class FileNode implements Node {

	public Properties properties = new Properties();


	public boolean isImage(){
		return Boolean.parseBoolean(properties.getProperty(Pydio.NODE_PROPERTY_IS_IMAGE)) || "1".equals(properties.getProperty(Pydio.NODE_PROPERTY_IS_IMAGE));
	}

	public boolean isFile(){
		return Boolean.parseBoolean(properties.getProperty(Pydio.NODE_PROPERTY_IS_FILE));
	}

	public boolean isFolder(){
		String mime = getProperty(Pydio.NODE_PROPERTY_IS_FILE);
		return "false".equals(mime);
	}

	public long lastModified(){
        try {
            return Long.parseLong(properties.getProperty(Pydio.NODE_PROPERTY_AJXP_MODIFTIME));
        }catch (Exception e){
            return 0;
        }
	}

	public long size(){
		String strSize = properties.getProperty(Pydio.NODE_PROPERTY_BYTESIZE);
		if(strSize == null || "".equals(strSize)) return 0;
		return Long.parseLong(strSize);
	}

	public String icon(){
		return properties.getProperty(Pydio.NODE_PROPERTY_ICON);
	}

	public String mimeString(){
		return properties.getProperty(Pydio.NODE_PROPERTY_MIMESTRING);
	}

    public String shareID() {
        return properties.getProperty(Pydio.NODE_PROPERTY_SHARE_UUID);
    }

    public static String mimeType(String label){
        if (label.endsWith(".doc") || label.endsWith(".docx")) {
            // Word document
            return "application/msword";
        } else if(label.endsWith(".pdf")) {
            // PDF file
            return "application/pdf";
        } else if(label.endsWith(".ppt") || label.endsWith(".pptx")) {
            // Powerpoint file
            return "application/vnd.ms-powerpoint";
        } else if(label.endsWith(".xls") || label.endsWith(".xlsx")) {
            // Excel file
            return "application/vnd.ms-excel";
        } else if(label.endsWith(".zip") || label.endsWith(".rar")) {
            // WAV audio file
            return "application/x-wav";
        } else if (label.endsWith(".rtf")) {
            // RTF file
            return "application/rtf";
        } else if(label.endsWith(".wav") || label.endsWith(".mp3")) {
            // WAV audio file
            return "audio/x-wav";
        } else if(label.endsWith(".gif")) {
            // GIF file
            return "image/gif";
        } else if(label.endsWith(".jpg") || label.endsWith(".jpeg") || label.endsWith(".png")) {
            // JPG file
            return "image/jpeg";
        } else if(label.endsWith(".txt")) {
            // Text file
            return "text/plain";
        } else if(label.endsWith(".3gp") || label.endsWith(".mpg") || label.endsWith(".mpeg") || label.endsWith(".mpe") || label.endsWith(".mp4") || label.endsWith(".avi")) {
            // Video files
            return "video/*";
        } else {
            return "*/*";
        }
    }

    public boolean isEditable(){
        String mime = mimeString();
        return mime.equals("application/msword") || mime.equals("application/vnd.ms-powerpoint") || mime.equals("application/vnd.ms-excel") || mime.equals("application/rtf")
                || mime.equals("text/plain");
    }

    public boolean isReadable(){
        String mime = mimeString();
        return mime.equals("video/*") || mime.equals("image/jpeg") || mime.equals("image/gif") || mime.equals("audio/x-wav") || mime.equals("image/jpeg") || mime.equals("image/jpeg") || mime.equals("text/plain") || mime.equals("application/rtf");
    }

    public void unSetProperty(String key){
        properties.remove(key);
    }

    public boolean equals(Object o){
	    boolean instanceOfNode = o instanceof Node;
        return instanceOfNode && !(compare((Node) o) == different);
    }

    //*********************************************************************************************
    //                  Super class: NODE METHODS
    //*********************************************************************************************
    public String label(){
        return properties.getProperty(Pydio.NODE_PROPERTY_TEXT);
    }

    public String path(){
		return properties.getProperty(Pydio.NODE_PROPERTY_FILENAME);
	}

	public String getProperty(String name){
		return properties.getProperty(name);
	}

	public int type() {
		return Node.TYPE_REMOTE_NODE;
	}
	@Override
	public String id() {
		return properties.getProperty(Pydio.NODE_PROPERTY_UUID);
	}

    public void setProperty(String key, String value){
        if (properties == null) {
            properties = new Properties();
        }
        properties.put(key, value);
    }
    @Override
    public void deleteProperty(String key) {
        if(properties != null && properties.contains(key)){
            properties.remove(key);
        }
    }
    @Override
    public void setProperties(Properties p) {
        this.properties = p;
    }
	@Override
	public int compare(Node node) {
        if(node == null) {
            return different;
        }

        String encoded = getEncoded();
        if (encoded == null) {
            return different;
        }

        if (encoded.equals(node.getEncoded())) {
            return same;
        }

        String path = path();
        String label = path();

        String nPath = node.path();
        String nLabel = node.path();

        if(nPath.equals(path) && nLabel.equals(label)) {
            return content;
        }

		return different;
	}
    @Override
    public String getEncodedHash() {
        if(properties == null) {
            return null;
        }
        return properties.getProperty(Pydio.NODE_PROPERTY_ENCODED_HASH);
    }
    @Override
    public String getEncoded() {
        if(properties == null) {
            return null;
        }
        return properties.getProperty(Pydio.NODE_PROPERTY_ENCODED);
    }

}
