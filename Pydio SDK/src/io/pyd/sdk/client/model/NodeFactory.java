package io.pyd.sdk.client.model;

import java.util.Properties;

import org.json.JSONObject;

public class NodeFactory {
	
	public static Node createNode(int type, org.w3c.dom.Node xml, String specName){

		Node node = instantiateNode(type);
		if(xml != null) {
			node.initFromXml(xml);
		}
		
		if(specName != null){
			NodeSpec spec = NodeSpecContainer.getSpecInstance(specName);
			node.setNodeSpec(spec);
		}
		return node;
	}
	
	public static Node createNode(int type, JSONObject json, String specName){
		NodeSpec spec = NodeSpecContainer.getSpecInstance(specName);
		Node node = instantiateNode(type);
		
		if(json != null){
			node.initFromJson(json);
		}
		
		if(spec != null){
			node.setNodeSpec(spec);
		}		
		return node;
	}
	
	public static Node createNode(int type) {
		return instantiateNode(type);
	}
	
	public static Node createNode(org.w3c.dom.Node xml){
		String nodename = xml.getNodeName(); 
		if("repo".equals(nodename)) {
			return createNode(Node.TYPE_REPOSITORY, xml, null);
		}else if("tree".equals(nodename)){
			return createNode(Node.TYPE_FILE, xml, null);
		}
		return null;
	}

	public static Node createNode(int type, Properties prop, String specName){
		NodeSpec spec = NodeSpecContainer.getSpecInstance(specName);
		Node node = instantiateNode(type);
		node.initFromProperties(prop);
		node.setNodeSpec(spec);
		return node;
	}
	
	
	private static Node instantiateNode(int type){
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
				return null;	
		}
	}
}
