package io.pyd.sdk.client.model;

import java.util.Properties;
import org.json.JSONObject;

public class RepositoryNode implements Node{
		
	private boolean allowCrossRepositoryCopy;
	private String accessType;
	private String slug;
	private String label;
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
	
	
	//**************************NODE implements***************************//	
	public NodeSpec getNodeSpec() {
		return null;
	}

	public void setNodeSpec(NodeSpec spec) {
		
	}
	
	public void initFromXml(org.w3c.dom.Node xml) {		
	}

	public void initFromJson(JSONObject json) {		
	}

	public void initFromProperties(Properties prop) {		
	}
}
