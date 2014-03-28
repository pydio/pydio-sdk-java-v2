package io.pyd.sdk.client.model;

import java.util.Map;
import java.util.Properties;
import org.json.JSONObject;

public class ServerNode implements Node{
		
	private boolean legacy = false;
	private boolean SSLselfSigned = false;
	private long uploadLimit = 2097152;
	private String protocol;
	private String host;
	private String path;
	private Map<String, String> remoteCapacities;

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

	public int type() {
		return Node.TYPE_SERVER;
	}
	
	public void setLegacy(boolean leg){
		legacy = leg;
	}
	
	public void setHost(String h){
		host = h;
	}
	
	public void setProtocol(String prot){
		protocol = prot;
	}
	
	public void setSelSigned(boolean ssl){
		SSLselfSigned = ssl;
	}
	
	public void setUploadLimit(long limit){
		uploadLimit = limit;
	}
	
	public void setPath(String p){
		path = p;
	}
	
	public String getUrl(){
		return protocol+"://"+host+path+"/";
	}
	
	public String path(){
		return path;
	}
	
	
	/*public Map<String,String> getRemoteCapacities(RestRequest rest){
		if(this.remoteCapacities != null) return this.remoteCapacities;
		// Load XML Registry and get values
		remoteCapacities = new HashMap<String, String>();
		try {
			Document doc = rest.getDocumentContent(AjxpAPI.getInstance().getXmlPluginsRegistryUri());
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile(capacity_UPLOAD_LIMIT);
			org.w3c.dom.Node result = (org.w3c.dom.Node)expr.evaluate(doc, XPathConstants.NODE);
			remoteCapacities.put(capacity_UPLOAD_LIMIT, result.getFirstChild().getNodeValue().replace("\"", ""));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return this.remoteCapacities;
	}*/
	
}
