package io.pyd.sdk.client.model;

import java.util.ArrayList;

import io.pyd.sdk.client.utils.DefaultEventBus;
import io.pyd.sdk.client.utils.Pydio;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Message{

	
	
	public final static int SUCCESS = 1;
	public final static int ERROR 	= 2;
		
	
	private static final long serialVersionUID = 1L;
	
	
	private String message;
	private String type;
	
	
	public String getType() {
		return type;
	}

	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public void setType(String type){
		this.type = type;
	}

	public static Message create(Document doc){
		
		org.w3c.dom.Node xml_message = doc.getElementsByTagName(Pydio.XML_MESSAGE).item(0);
		
		Message message = new Message();
		message.setMessage(xml_message.getTextContent());
		message.setType(xml_message.getAttributes().getNamedItem(Pydio.MESSAGE_PROPERTY_TYPE).getNodeValue());
		
		
		NodeList removes = doc.getElementsByTagName(Pydio.NODE_DIFF_REMOVE).item(0).getChildNodes();
		ArrayList<String> pathes = new ArrayList<String>();
		for(int i = 0; i < removes.getLength(); i++){
			pathes.add(removes.item(i).getAttributes().getNamedItem(Pydio.NODE_PROPERTY_FILENAME).getNodeValue());
		}		
		DefaultEventBus.bus().publish(new TreeRemoveEvent(pathes));
		
		
		
		NodeList adds = doc.getElementsByTagName(Pydio.NODE_DIFF_ADD).item(0).getChildNodes();
		ArrayList<Node> nodes = new ArrayList<Node>();
		for(int i = 0; i < adds.getLength(); i++){
			nodes.add(NodeFactory.createNode(adds.item(i)));
			
		}
		DefaultEventBus.bus().publish(new TreeAddEvent(nodes));
		
		
		
		NodeList updates = doc.getElementsByTagName(Pydio.NODE_DIFF_UPDATE).item(0).getChildNodes();
		nodes = new ArrayList<Node>();
		for(int i = 0; i < updates.getLength(); i++){			
			nodes.add(NodeFactory.createNode(updates.item(i)));			
		}
		DefaultEventBus.bus().publish(new TreeUpdateEvent(nodes));
		
		
		return message;
	}
	
	public static Message create(String type, String message){
		Message m = new Message();
		m.type = type;
		m.message = message;
		return m;
	}
	
}