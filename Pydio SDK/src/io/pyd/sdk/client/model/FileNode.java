package io.pyd.sdk.client.model;

import io.pyd.sdk.client.utils.Pydio;

import java.util.Properties;

import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

public class FileNode implements Node{
	
	
	
	/*//private boolean isLeaf;
	private boolean isImage;
	private boolean isFolder;
	//private int fileGroup;
	//private int fileOwner;	
	private long modifTime;
	private long size;
	//private int filePermissions;
	//private String metaFields;
	//private String metaTypes;
	//private String metaLabels;
	private String icon;
	//private String openIcon;
	private String path;
	private String mimeString;
	//private int mimeStringId;
	//private int imageHeight;
	//private int imageWidth;
	//private String imageType;
	private String label;*/
	
	Properties properties = new Properties();
	
	public NodeSpec getNodeSpec() {
		return null;
	}

	public void setNodeSpec(NodeSpec spec) {
		
	}

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

	public void initFromProperties(Properties prop) {
		properties = (Properties) prop.clone();
	}	
	
	public String name(){
		return properties.getProperty(Pydio.NODE_PROPERTY_TEXT);
	}
	
	public boolean isImage(){
		return Boolean.parseBoolean(properties.getProperty(Pydio.NODE_PROPERTY_IS_IMAGE));
	}
	
	public boolean isFile(){
		return Boolean.parseBoolean(properties.getProperty(Pydio.NODE_PROPERTY_IS_FILE));
	}
	
	public long modifTime(){
		return Long.parseLong(properties.getProperty(Pydio.NODE_PROPERTY_AJXP_MODIFTIME));
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
