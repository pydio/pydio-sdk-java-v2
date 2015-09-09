package pydio.sdk.java.model;


import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

import java.io.File;
import java.util.Properties;

import pydio.sdk.java.utils.Pydio;

/**
 * 
 * @author pydio
 *
 */
public class TreeNode implements Node{

	Properties properties = new Properties();

	public void initFromXml(org.w3c.dom.Node xml) {
		if (xml.hasAttributes()) {
			NamedNodeMap map = xml.getAttributes();			
			for (int i = 0; i < map.getLength(); i++) {
				Attr at = (Attr) map.item(i);
				String attrName = at.getNodeName();
				String attrValue = at.getNodeValue();
				properties.setProperty(attrName, attrValue);
			}			
		}		
	}

	public void initFromJson(JSONObject json) {
		
	}

	public void initFromProperties(Properties p) {
		properties = (Properties) p.clone();
	}
    @Override
    public void initFromFile(File file) {
        properties = new Properties();

        properties.setProperty(Pydio.NODE_PROPERTY_FILENAME, file.getAbsolutePath());
        properties.setProperty(Pydio.NODE_PROPERTY_TEXT, file.getName());

        properties.setProperty(Pydio.NODE_PROPERTY_AJXP_MODIFTIME, file.lastModified()+"");
        properties.setProperty(Pydio.NODE_PROPERTY_LABEL, file.getName());

        String[] list = file.getName().split(".");

        properties.setProperty(Pydio.NODE_PROPERTY_IS_IMAGE, String.valueOf(mimeType(file.getName()).startsWith("image")));

		if(file.isDirectory()){
			properties.setProperty(Pydio.NODE_PROPERTY_MIMESTRING, "Directory");
		}else{
			properties.setProperty(Pydio.NODE_PROPERTY_MIMESTRING, mimeType(file.getName()));
		}

        properties.setProperty(Pydio.NODE_PROPERTY_DESCRIPTION, "");
        properties.setProperty(Pydio.NODE_PROPERTY_BYTESIZE, file.length()+"");
        properties.setProperty(Pydio.NODE_PROPERTY_FILE_SIZE, file.length()+"");
        properties.setProperty(Pydio.NODE_PROPERTY_IS_FILE, Boolean.toString(file.isFile()).toLowerCase());
    }

    public String label(){
		return properties.getProperty(Pydio.NODE_PROPERTY_TEXT);
	}

	public boolean isImage(){
		return Boolean.parseBoolean(properties.getProperty(Pydio.NODE_PROPERTY_IS_IMAGE));
	}

	public boolean isFile(){
		return Boolean.parseBoolean(properties.getProperty(Pydio.NODE_PROPERTY_IS_FILE));
	}

	public boolean isFolder(){
		String mime = getProperty(Pydio.NODE_PROPERTY_MIMESTRING);
		if(mime != null){
			return "Directory".equals(mime);
		}
		return new File(path()).isDirectory();
	}

	public long lastModified(){
        try {
            return Long.parseLong(properties.getProperty(Pydio.NODE_PROPERTY_AJXP_MODIFTIME));
        }catch (Exception e){
            return 0;
        }
	}

	public long size(){
		return Long.parseLong(properties.getProperty(Pydio.NODE_PROPERTY_BYTESIZE));
	}

	public String icon(){
		return properties.getProperty(Pydio.NODE_PROPERTY_ICON);
	}

	public String mimeString(){
		return properties.getProperty(Pydio.NODE_PROPERTY_MIMESTRING);
	}

	public String path(){
		return properties.getProperty(Pydio.NODE_PROPERTY_FILENAME);
	}

	public String getProperty(String name){
		return properties.getProperty(name, "");
	}

	public int type() {
		return Node.TYPE_TREE;
	}

    public boolean equals(Object o){
        try{
            return this == o || (o instanceof Node) && ((Node)o).type() == type() && label().equals(((Node)o).label()) && path().equals(((Node)o).path());
        }catch(NullPointerException e){
            return false;
        }
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

	public void setProperty(String key, String value){
		properties.put(key, value);
	}

	public void unSetProperty(String key){
		properties.remove(key);
	}

}
