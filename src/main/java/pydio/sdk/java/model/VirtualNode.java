package pydio.sdk.java.model;

import org.json.JSONObject;

import java.io.File;
import java.util.Properties;

import pydio.sdk.java.utils.Pydio;

public class VirtualNode implements Node{

    protected String path;
    protected String label;
    Properties properties;

	public void initFromXml(org.w3c.dom.Node xml) {
		// TODO Auto-generated method stub		
	}

	public void initFromJson(JSONObject json) {
		// TODO Auto-generated method stub
		
	}

	public void initFromProperties(Properties prop) {
		// TODO Auto-generated method stub
	}

    public void initFromFile(File file) {
        properties = new Properties();
        path = file.getAbsolutePath();
        label = file.getName();

        properties.setProperty(Pydio.NODE_PROPERTY_AJXP_MODIFTIME, file.lastModified()+"");
        properties.setProperty(Pydio.NODE_PROPERTY_LABEL, file.getName());

        String[] list = file.getName().split(".");
        String ext = list.length > 1 ? list[list.length - 1].toLowerCase() : "";
        if(ext.length() > 0) {
            properties.setProperty(Pydio.NODE_PROPERTY_ICON, ext);
        }
        boolean is_image = ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg");

        properties.setProperty(Pydio.NODE_PROPERTY_IS_IMAGE, String.valueOf(is_image));
        if(is_image){
            properties.setProperty(Pydio.NODE_PROPERTY_ICON, "image");
        }else{
            if(file.isDirectory()){
                properties.setProperty(Pydio.NODE_PROPERTY_ICON, "directory");
            }
        }
        properties.setProperty(Pydio.NODE_PROPERTY_AJXP_MIME, "");
        properties.setProperty(Pydio.NODE_PROPERTY_MIMESTRING, "");
        properties.setProperty(Pydio.NODE_PROPERTY_DESCRIPTION, "");
        properties.setProperty(Pydio.NODE_PROPERTY_BYTESIZE, file.length()+"");
        properties.setProperty(Pydio.NODE_PROPERTY_FILE_SIZE, file.length()+"");
        properties.setProperty(Pydio.NODE_PROPERTY_IS_FILE, Boolean.toString(file.isFile()).toLowerCase());
    }
    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key, "");
    }

    public int type() {
		return Node.TYPE_VIRTUAL;
	}

	public String path() {
		return path;
	}

	public String label() {
		return label;
	}

}
