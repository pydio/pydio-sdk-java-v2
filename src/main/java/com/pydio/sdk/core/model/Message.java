package com.pydio.sdk.core.model;

import com.pydio.sdk.core.Pydio;

import org.w3c.dom.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Message implements Serializable{

	public final static String SUCCESS = "SUCCESS";
	public final static String ERROR 	= "ERROR";
	public final static String EMPTY 	= "EMPTY";

	public String message;
	private String type = EMPTY;

    public List<Node> deleted = new ArrayList<>();
    public List<Node> added = new ArrayList<>();
    public List<Node> updated = new ArrayList<>();


	public String type() {
		return type;
	}
	
	public String text() {
		return message;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public void setType(String type){
		this.type = type;
	}

	public boolean hasEvents() {
		return this.added.size() > 0 || this.deleted.size() > 0 || this.updated.size() > 0;
	}

	private static Message empty(){
		Message m = new Message();
		m.type = ERROR;
		return m;
	}
	/**
	 * create a message from the given XML Document and fire associated event
	 * @param doc An instance of XML Document
	 * @return An instance of a Message
	 */
	public static Message create(Document doc){
		if(doc == null){
			return empty();
		}

		org.w3c.dom.Node xml_message = doc.getElementsByTagName(Pydio.XML_MESSAGE).item(0);
		Message msg = new Message();

		if(xml_message != null){
			msg.setMessage(xml_message.getTextContent());
			msg.setType(xml_message.getAttributes().getNamedItem(Pydio.MESSAGE_PROPERTY_TYPE).getNodeValue());
		}

        org.w3c.dom.Node diff = doc.getElementsByTagName(Pydio.XML_NODES_DIFF).item(0);
        if(diff != null) {
            for (int i = 0; i < diff.getChildNodes().getLength(); i++) {
                org.w3c.dom.Node child = diff.getChildNodes().item(i);
                String tag = child.getNodeName();

                List<Node> list = null;

                if (Pydio.NODE_DIFF_REMOVE.equals(tag)) {
                    if (msg.deleted == null) {
                        msg.deleted = new ArrayList<>();
                    }
                    list = msg.deleted;
                } else if (Pydio.NODE_DIFF_ADD.equals(tag)) {
                    if (msg.added == null) {
                        msg.added = new ArrayList<>();
                    }
                    list = msg.added;
                } else if (Pydio.NODE_DIFF_UPDATE.equals(tag)) {
                    if (msg.updated == null) {
                        msg.updated = new ArrayList<>();
                    }
                    list = msg.updated;
                }

                for (int j = 0; list != null && j < child.getChildNodes().getLength(); j++) {
                    list.add(NodeFactory.createNode(child.getChildNodes().item(j)));
                }
            }
        }
        return msg;
	}
	
	public static Message create(String type, String message){
		Message m = new Message();
		m.type = type;
		m.message = message;
		return m;
	}
	
}