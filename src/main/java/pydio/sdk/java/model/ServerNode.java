package pydio.sdk.java.model;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
/**
 * Class that wrap a server properties
 * @author pydio
 *
 */

public class ServerNode implements Node{
		
	private boolean legacy = false;
	private boolean SSLselfSigned = false;
	private String protocol;
	private String host;
	private String path;
	private int port;
	private Map<String, String> mConfigs;
    Properties properties;
	

	public void initFromProperties(Properties spec) {}
    @Override
    public void initFromFile(File file) {}
    @Override
    public String getProperty(String key) {return properties.getProperty(key, "");}

    public void initFromXml(org.w3c.dom.Node xml) {}

	public void initFromJson(JSONObject json) {}
	/**
	 * 
	 * @return
	 */
    public boolean isLegacy(){
        return legacy;
    }
	
	public boolean isSSLselfSigned(){
		return SSLselfSigned;
	}
	
	public String host(){
		return host;
	}
	
	public String protocol(){
		return protocol;
	}

	public int port(){
		return port;
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
	
	public void setPath(String p){
        if("".equals(p)){
            path = "/";
        }else{
            path = p;
        }
	}

    public void setPort(int port){
        this.port = port;
    }
	
	public String url(){
        String url = protocol+"://"+host;
        if(port > 0 && port != 80){
            url += ":"+port;
        }
        return url+path;
	}
	
	public String path(){
		return path;
	}
	
	public String getRemoteConfig(String name){
		if(mConfigs == null) return null;
		return mConfigs.get(name);
	}
	
	public void addConfig(String key, String value){
		if(mConfigs == null) mConfigs = new HashMap<String, String>();
		mConfigs.put(key, value);
       // properties.setProperty(key, value);
	}

	public String label() {
		return null;
	}

    public boolean equals(Object o){
        try{
            return this == o || (o instanceof Node) && ((Node)o).type() == type() && label().equals(((Node)o).label()) && path().equals(((Node)o).path());
        }catch(NullPointerException e){
            return false;
        }
    }
	
}
