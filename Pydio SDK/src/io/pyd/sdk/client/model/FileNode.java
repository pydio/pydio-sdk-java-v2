package io.pyd.sdk.client.model;

import java.util.Properties;

import org.json.JSONObject;

public class FileNode implements Node{
	
	private boolean isLeaf;
	private boolean isImage;
	private boolean isFolder;
	private int fileGroup;
	private int fileOwner;	
	private long modifTime;
	private long size;
	private int filePermissions;
	private String metaFields;
	private String metaTypes;
	private String metaLabels;
	private String icon;
	private String openIcon;
	private String path;
	private String mimeString;
	private int mimeStringId;
	private int imageHeight;
	private int imageWidth;
	private String imageType;
	private String text;
	//private String
	//private int 
	
	

	

	public NodeSpec getNodeSpec() {
		return null;
	}

	public void setNodeSpec(NodeSpec spec) {
		
	}

	public void initFromXml(org.w3c.dom.Node xml) {}

	public void initFromJson(JSONObject json) {}

	public void initFromProperties(Properties prop) {}

	
	public boolean isLeaf(){
		return isLeaf;
	}
	
	public boolean isImage(){
		return isImage;
	}
	
	public boolean isFolder(){
		return isFolder;
	}
	
	public long modifTime(){
		return modifTime;
	}
	
	public long size(){
		return size;
	}
	
	public int fileGroup(){
		return fileGroup;
	}
	
	public int fileOwner(){
		return fileOwner;
	}
	
	public int filePermissions(){
		return filePermissions;
	}
	
	public String metaFileds(){
		return metaFields;
	}
	
	public String metaTypes(){
		return metaTypes;
	}
	
	public String metaLabels(){
		return metaLabels();
	}
	
	public String icon(){
		return icon;
	}
	
	public String openIcon(){
		return openIcon;
	}
	
	public String mimeString(){
		return mimeString;
	}
	
	public String getPath(){
		return text;
	}
	
	public int mimeStringId(){
		return mimeStringId;
	}
	
	public String imageType(){
		return imageType;
	}
	
	public 	int image_height(){
		return imageHeight;
	}
	
	public int image_width(){
		return imageWidth;
	}
}
