package io.pyd.sdk.client.model;

import io.pyd.sdk.client.utils.Pydio;

import java.util.Properties;

import org.json.JSONObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class RepositoryNode implements Node{
		
	private boolean allowCrossRepositoryCopy = false;
	private String accessType;
	private String slug;
	private String label;
	private String id;
	private String description;	
	
	
	public boolean isAllowedCrossRepositoryCopy(){
		return allowCrossRepositoryCopy;
	}
	
	public String getSlug(){
		return slug;
	}
	
	public String getDescription(){
		return description;
	}
	
	public String  getAccesType(){
		return accessType;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getId(){
		return id;
	}
	
	//**************************NODE implements***************************//
	public NodeSpec getNodeSpec() {
		return null;
	}

	public void setNodeSpec(NodeSpec spec) {
		
	}
	
	public void initFromXml(org.w3c.dom.Node xml) {
		/*
		 * 
		 *Example of repository format 
		<repo access_type="fs" allowCrossRepositoryCopy="true" id="0" repositorySlug="default">
		<label>Fichiers Communs</label>
		<description>Fichiers partagés par tous les utilisateurs</description>
		<client_settings icon="plugins/access.fs/icon.png">
	        <resources>
	            <i18n namespace="access_fs" path="plugins/access.fs/i18n"/>
	        </resources>
	    </client_settings>
		</repo>*/		
		
		xml.normalize();
		NodeList children = xml.getChildNodes();

		if (xml.hasAttributes()) {
			NamedNodeMap map = xml.getAttributes();
			accessType = map.getNamedItem(Pydio.REPO_PROPERTY_ACCESS_TYPE).getNodeValue();
			try{allowCrossRepositoryCopy = Boolean.parseBoolean(map.getNamedItem(Pydio.REPO_PROPERTY_CROSS_COPY).getNodeValue());}catch(Exception e){}
			slug = map.getNamedItem(Pydio.REPO_PROPERTY_SLUG).getNodeValue();
			id = map.getNamedItem(Pydio.REPO_PROPERTY_ID).getNodeValue();
		}

		if (xml.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
			if (children.getLength() > 0) {
				for (int i = 0; i < children.getLength(); i++) {
					org.w3c.dom.Node n = children.item(i);
					if(Pydio.REPO_LABEL.equals(n.getNodeName())){
						label = n.getTextContent();
					}else if(Pydio.REPO_DESCRIPTION.equals(n.getNodeName())){
						description = n.getTextContent();
					}
				}
			}
		}
	}

	public void initFromJson(JSONObject json) {		
	}

	public void initFromProperties(Properties prop) {		
	}

	public int type() {
		return Node.TYPE_REPOSITORY;
	}

	public String path() {
		return "/";
	}
}
