package pydio.sdk.java.model;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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
			return createNode(Node.TYPE_TREE, xml);
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
			case Node.TYPE_TREE:
				return new TreeNode();
				
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

    public static Node createNode(int type, File file){
        if(file == null){
            return null;
        }
        Node node = newNode(type);
        node.initFromFile(file);
        return node;
    }

    public static byte[] serialize(Node node){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(node);
            out.close();
            byte[] buf = bos.toByteArray();
            return buf;
        } catch(IOException ioe) {
            return null;
        }
    }

    public static Node deserialize(byte[] buffer){
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
            Object object = in.readObject();
            in.close();
            return (Node)object;
        } catch(ClassNotFoundException cnfe) {
            return null;
        } catch(IOException ioe) {
            return null;
        }
    }
}
