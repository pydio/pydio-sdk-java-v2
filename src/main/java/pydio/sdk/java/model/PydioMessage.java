package pydio.sdk.java.model;

import org.w3c.dom.Document;

import java.io.Serializable;
import java.util.ArrayList;

import pydio.sdk.java.utils.Pydio;

public class PydioMessage implements Serializable{

	
	/**
	 * Message result SUCCESS
	 */
	public final static String SUCCESS = "SUCCESS";
	/**
	 * Message result ERROR
	 */
	public final static String ERROR 	= "ERROR";
	private String message;
	private String type;

    public ArrayList<Node> deleted = null;
    public ArrayList<Node> added = null;
    public ArrayList<Node> updated = null;


	public String getType() {
		return type;
	}
	
	public String getContent() {
		return message;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public void setType(String type){
		this.type = type;
	}
	/**
	 * create a message from the given XML Document and fire associated event
	 * @param doc An instance of XML Document
	 * @return An instance of a Message
	 */
	public static PydioMessage create(Document doc){
		org.w3c.dom.Node xml_message = doc.getElementsByTagName(Pydio.XML_MESSAGE).item(0);
		PydioMessage msg = new PydioMessage();
		if(xml_message != null){
			msg.setMessage(xml_message.getTextContent());
			msg.setType(xml_message.getAttributes().getNamedItem(Pydio.MESSAGE_PROPERTY_TYPE).getNodeValue());
		}
        org.w3c.dom.Node diff = doc.getElementsByTagName(Pydio.XML_NODES_DIFF).item(0);
        if(diff != null) {
            for (int i = 0; i < diff.getChildNodes().getLength(); i++) {
                org.w3c.dom.Node child = diff.getChildNodes().item(i);
                String tag = child.getNodeName();

                ArrayList<Node> list = null;

                if (Pydio.NODE_DIFF_REMOVE.equals(tag)) {
                    if (msg.deleted == null) {
                        msg.deleted = new ArrayList<Node>();
                    }
                    list = msg.deleted;
                } else if (Pydio.NODE_DIFF_ADD.equals(tag)) {
                    if (msg.added == null) {
                        msg.added = new ArrayList<Node>();
                    }
                    list = msg.added;
                } else if (Pydio.NODE_DIFF_UPDATE.equals(tag)) {
                    if (msg.updated == null) {
                        msg.updated = new ArrayList<Node>();
                    }
                    list = msg.updated;
                }

                for (int j = 0; j < child.getChildNodes().getLength(); j++) {
                    list.add(NodeFactory.createNode(child.getChildNodes().item(j)));
                }
            }
        }
        return msg;
	}
	
	public static PydioMessage create(String type, String message){
		PydioMessage m = new PydioMessage();
		m.type = type;
		m.message = message;
		return m;
	}
	
}