package io.pyd.sdk.client.model;

import java.util.Properties;
import org.json.JSONObject;

public class ServerNode implements Node{
		
	private boolean legacy = false;
	private boolean SSLselfSigned = false;
	private long uploadLimit;
	private String protocol;
	private String host;


	public void initFromProperties(Properties spec) {		
	}
	
	public void initFromXml(org.w3c.dom.Node xml) {
	}

	public void initFromJson(JSONObject json) {
	}
	
	public boolean isLegacy(){
		return legacy;
	}
	
	public boolean isSSLselfSigned(){
		return SSLselfSigned;
	}
	
	public long getUploadLimit(){
		return uploadLimit;
	}
	
	public String getHost(){
		return host;
	}
	
	public String getProtocol(){
		return protocol;
	}

	public NodeSpec getNodeSpec() {
		return null;
	}

	public void setNodeSpec(NodeSpec spec) {		
	}

}
