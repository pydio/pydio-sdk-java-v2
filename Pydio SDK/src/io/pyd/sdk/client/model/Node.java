package io.pyd.sdk.client.model;

import java.util.Properties;

import org.json.JSONObject;




public interface Node{
	
	public final static int TYPE_FILE = 1;
	public final static int TYPE_REPOSITORY = 2;
	public final static int TYPE_SERVER = 3;
	public final static int TYPE_VIRTUAL = 4;	
	
	public NodeSpec getNodeSpec();
	public void setNodeSpec(NodeSpec spec);
	
	public void initFromXml(org.w3c.dom.Node xml);
	public void initFromJson(JSONObject json);
	public void initFromProperties(Properties prop);
}
