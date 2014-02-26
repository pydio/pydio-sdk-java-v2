package io.pyd.sdk.client.model;

import org.json.JSONObject;




public abstract class Node{
	
	public final static int NODE_TYPE_FILE = 1;
	public final static int NODE_TYPE_REPOSITORY = 2;
	public final static int NODE_TYPE_SERVER = 3;
	public final static int NODE_TYPE_VIRTUAL = 4;	
		
	protected String path;
	protected String label;
	NodeSpec spec;
	
	public NodeSpec getNodeSpec(){
		return spec;
	}
	
	public void setNodeSpec(NodeSpec spec){
		this.spec = spec;
	}
	
	public String getPath(){
		return path;
	}
	
	public String getLabel(){
		return label;
	}
	
	public abstract void initFromXml(org.w3c.dom.Node xml);
	public abstract void initFromJson(JSONObject json);
}
