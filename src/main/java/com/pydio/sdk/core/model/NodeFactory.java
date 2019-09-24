package com.pydio.sdk.core.model;

import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Properties;

public class NodeFactory {

    public static Node createNode(int type, org.w3c.dom.Node xml) {
        Node node = newNode(type);
		Properties p = new Properties();
		if (xml.hasAttributes()) {
			NamedNodeMap map = xml.getAttributes();
			for (int i = 0; i < map.getLength(); i++) {
				Attr at = (Attr) map.item(i);
				String attrName = at.getNodeName();
				String attrValue = at.getNodeValue();
				p.setProperty(attrName, attrValue);
			}
		}
		if(node != null){
			node.setProperties(p);
		}
		return node;
	}

    public static Node createNode(int type, JSONObject json) {
        Node node = newNode(type);
		/*if(json != null){
			node.initFromJson(json);
		}*/
		return node;
	}

    public static Node createNode(int type) {
		return newNode(type);
	}

    public static Node createNode(org.w3c.dom.Node xml) {
		String nodename = xml.getNodeName(); 
		if("repo".equals(nodename)) {
            return createNode(Node.TYPE_WORKSPACE, xml);
        } else if ("tree".equals(nodename)) {
            return createNode(Node.TYPE_REMOTE_NODE, xml);
		}
		return null;
	}

    public static Node createNode(int type, Properties prop) {
        Node node = newNode(type);
		if(node == null) {
			return null;
		}
		node.setProperties(prop);
		return node;
	}

    private static Node newNode(int type) {
		switch (type) {
            case Node.TYPE_REMOTE_NODE:
				return new FileNode();

            case Node.TYPE_WORKSPACE:
				return new WorkspaceNode();

            case Node.TYPE_SERVER:
                return new ServerNode();

            case Node.TYPE_LOCAL_NODE:
				return new ObjectNode();
			default:
				return null;	
		}
	}

    public static Node createNode(int type, File file) {
        if(file == null){
            return null;
        }
        Node node = newNode(type);
        //node.initFromFile(file);
        return node;
    }

    public static byte[] serialize(Node node) {
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

    public static Node deserialize(byte[] buffer) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
            Object object = in.readObject();
            in.close();
            return (Node) object;
        } catch(ClassNotFoundException cnfe) {
            return null;
        } catch(IOException ioe) {
            return null;
        }
    }
}
