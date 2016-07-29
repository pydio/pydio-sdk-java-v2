package pydio.sdk.java.core.model;

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
	
	public static pydio.sdk.java.core.model.Node createNode(int type, org.w3c.dom.Node xml){
		pydio.sdk.java.core.model.Node node = newNode(type);
		if(xml != null) {
			node.initFromXml(xml);
		}
		return node;
	}
	
	public static pydio.sdk.java.core.model.Node createNode(int type, JSONObject json){
		pydio.sdk.java.core.model.Node node = newNode(type);

		if(json != null){
			node.initFromJson(json);
		}
		return node;
	}
	
	public static pydio.sdk.java.core.model.Node createNode(int type) {
		return newNode(type);
	}
	
	public static pydio.sdk.java.core.model.Node createNode(org.w3c.dom.Node xml){
		String nodename = xml.getNodeName(); 
		if("repo".equals(nodename)) {
			return createNode(pydio.sdk.java.core.model.Node.TYPE_WORKSPACE, xml);
		}else if("tree".equals(nodename)){
			return createNode(pydio.sdk.java.core.model.Node.TYPE_TREE, xml);
		}
		return null;
	}

	public static pydio.sdk.java.core.model.Node createNode(int type, Properties prop){
		pydio.sdk.java.core.model.Node node = newNode(type);
		node.initFromProperties(prop);
		return node;
	}

	private static pydio.sdk.java.core.model.Node newNode(int type){
		switch (type) {			
			case pydio.sdk.java.core.model.Node.TYPE_TREE:
				return new FileNode();
				
			case pydio.sdk.java.core.model.Node.TYPE_WORKSPACE:
				return new WorkspaceNode();
				
			case pydio.sdk.java.core.model.Node.TYPE_SERVER:
				return new pydio.sdk.java.core.model.ServerNode();
				
			case pydio.sdk.java.core.model.Node.TYPE_VIRTUAL:
				return new VirtualNode();
			default:
				return null;	
		}
	}

    public static pydio.sdk.java.core.model.Node createNode(int type, File file){
        if(file == null){
            return null;
        }
        pydio.sdk.java.core.model.Node node = newNode(type);
        node.initFromFile(file);
        return node;
    }

    public static byte[] serialize(pydio.sdk.java.core.model.Node node){
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

    public static pydio.sdk.java.core.model.Node deserialize(byte[] buffer){
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
            Object object = in.readObject();
            in.close();
            return (pydio.sdk.java.core.model.Node)object;
        } catch(ClassNotFoundException cnfe) {
            return null;
        } catch(IOException ioe) {
            return null;
        }
    }
}
