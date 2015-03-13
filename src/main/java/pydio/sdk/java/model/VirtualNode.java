package pydio.sdk.java.model;

import org.json.JSONObject;

import java.util.Properties;

public class VirtualNode implements Node{

	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public void initFromXml(org.w3c.dom.Node xml) {
		// TODO Auto-generated method stub		
	}

	public void initFromJson(JSONObject json) {
		// TODO Auto-generated method stub
		
	}

	public void initFromProperties(Properties prop) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public Properties getProperties() {
        return null;
    }

    public int type() {
		// TODO Auto-generated method stub
		return Node.TYPE_VIRTUAL;
	}

	public String path() {
		// TODO Auto-generated method stub
		return null;
	}

	public String label() {
		// TODO Auto-generated method stub
		return null;
	}

}
