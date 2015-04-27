package pydio.sdk.java.model;


import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

import java.util.Properties;

import pydio.sdk.java.utils.Pydio;

/**
 * 
 * @author pydio
 *
 */
public class FileNode implements Node{

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
    public Properties getProperties() {
        return properties;
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
		return properties.getProperty(name); 
	}

	public int type() {
		return Node.TYPE_FILE;
	}
	
}
