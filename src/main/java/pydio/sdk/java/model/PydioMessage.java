package pydio.sdk.java.model;

import org.w3c.dom.Document;

import java.io.Serializable;

import pydio.sdk.java.utils.Pydio;

public class PydioMessage implements Serializable{

	
	/**
	 * Message result SUCCESS
	 */
	public final static String SUCCESS = "success";
	/**
	 * Message result ERROR
	 */
	public final static String ERROR 	= "error";
	private String message;
	private String type;
	
	
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
		PydioMessage pydioMessage = new PydioMessage();
		if(xml_message != null){
			pydioMessage.setMessage(xml_message.getTextContent());
			pydioMessage.setType(xml_message.getAttributes().getNamedItem(Pydio.MESSAGE_PROPERTY_TYPE).getNodeValue());
		}
		return pydioMessage;
	}
	
	public static PydioMessage create(String type, String message){
		PydioMessage m = new PydioMessage();
		m.type = type;
		m.message = message;
		return m;
	}
	
}