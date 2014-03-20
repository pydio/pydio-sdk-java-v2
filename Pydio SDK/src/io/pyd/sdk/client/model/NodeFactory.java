package io.pyd.sdk.client.model;

import java.util.Properties;

import org.json.JSONObject;

public class NodeFactory {
	
	public static Node createNode(int type, org.w3c.dom.Node xml, String specName) throws Message{
		NodeSpec spec = NodeSpecContainer.getSpecInstance(specName);
		Node node = instantiateNode(type);
		node.initFromXml(xml);
		node.setNodeSpec(spec);
		return node;
	}
	
	public static Node createNode(int type, JSONObject json, String specName) throws Message{
		NodeSpec spec = NodeSpecContainer.getSpecInstance(specName);
		Node node = instantiateNode(type);
		node.initFromJson(json);
		node.setNodeSpec(spec);
		return node;
	}

	public static Node createNode(int type, Properties prop, String specName) throws Message{
		NodeSpec spec = NodeSpecContainer.getSpecInstance(specName);
		Node node = instantiateNode(type);
		node.initFromProperties(prop);
		node.setNodeSpec(spec);
		return node;
	}
	
	private static Node instantiateNode(int type) throws Message{
		switch (type) {		
		case Node.TYPE_FILE:
			return new FileNode();
			
		case Node.TYPE_REPOSITORY:
			return new RepositoryNode();
			
		case Node.TYPE_SERVER:			
			return new ServerNode();
			
		case Node.TYPE_VIRTUAL:
			return new VirtualNode();
		default:
			throw new Message();	
		}
	}
}
