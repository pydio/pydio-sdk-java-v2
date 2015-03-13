package pydio.sdk.java.model;

import org.json.JSONObject;

import java.util.Properties;

public class NodeFactory {
	
	public static Node createNode(int type, org.w3c.dom.Node xml){

		Node node = newNode(type);
		if(xml != null) {
			node.initFromXml(xml);
		}

		return node;
	}
	
	public static Node createNode(int type, JSONObject json){
		Node node = newNode(type);
		
		if(json != null){
			node.initFromJson(json);
		}
		return node;
	}
	
	public static Node createNode(int type) {
		return newNode(type);
	}
	
	public static Node createNode(org.w3c.dom.Node xml){
		String nodename = xml.getNodeName(); 
		if("repo".equals(nodename)) {
			return createNode(Node.TYPE_WORKSPACE, xml);
		}else if("tree".equals(nodename)){
			return createNode(Node.TYPE_FILE, xml);
		}
		return null;
	}

	public static Node createNode(int type, Properties prop){
		Node node = newNode(type);
		node.initFromProperties(prop);
		return node;
	}

	private static Node newNode(int type){
		switch (type) {			
			case Node.TYPE_FILE:
				return new FileNode();
				
			case Node.TYPE_WORKSPACE:
				return new WorkspaceNode();
				
			case Node.TYPE_SERVER:
				return new ServerNode();
				
			case Node.TYPE_VIRTUAL:
				return new VirtualNode();
			default:
				return null;	
		}
	}
}
