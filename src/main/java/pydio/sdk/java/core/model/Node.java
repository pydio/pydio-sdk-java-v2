package pydio.sdk.java.core.model;

import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.Properties;


public interface Node extends Serializable {
	
	int TYPE_TREE = 1;
	int TYPE_WORKSPACE = 2;
	int TYPE_SERVER = 3;
	int TYPE_VIRTUAL = 4;
	int TYPE_SEARCH = 5;

	int type();

	String label();
	/**
	 * 
	 * @return 
	 */	
	String path();
	/**
	 * initalizes the calling Node using an XML object
	 * @param xml is an XMl represation of a Node
	 */
	void initFromXml(org.w3c.dom.Node xml);
	/**
	 * initializes the calling Node properties using a json object
	 * @param json representation of a Node
	 * 
	 */
	void initFromJson(JSONObject json);
	/**
	 * initializes the calling Node properties using a Property object
	 * @param prop Property object containing all Node properties
	 */
	void initFromProperties(Properties prop);

    void initFromFile(File file);

    String getProperty(String key);

    void setProperty(String key, String value);

	boolean equals(Object o);
}
