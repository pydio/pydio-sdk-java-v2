package pydio.sdk.java.model;


import org.json.JSONObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import pydio.sdk.java.utils.Pydio;

public class WorkspaceNode implements Node{
    Properties properties;
	
	public boolean isAllowedCrossRepositoryCopy(){
		return properties.getProperty(Pydio.WORKSPACE_PROPERTY_CROSS_COPY) == "true";
	}
	
	public String getSlug(){
		return properties.getProperty(Pydio.WORKSPACE_PROPERTY_SLUG);
	}
	
	public String getDescription(){
		return properties.getProperty(Pydio.NODE_PROPERTY_DESCRIPTION);
	}
	
	public String getAccessType(){
		return properties.getProperty(Pydio.WORKSPACE_PROPERTY_ACCESS_TYPE);
	}
	
	public String label() {
		return properties.getProperty(Pydio.NODE_PROPERTY_LABEL);
	}
	
	public String getId(){
		return properties.getProperty(Pydio.WORKSPACE_PROPERTY_ID);
	}

	public void initFromXml(org.w3c.dom.Node xml) {
        properties = new Properties();
		/*
		 * 
		 *Example of repository format 
		<repo access_type="fs" allowCrossRepositoryCopy="true" id="0" repositorySlug="default">
		<label>Fichiers Communs</label>
		<description>Fichiers partag�s par tous les utilisateurs</description>
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

            properties.setProperty(Pydio.WORKSPACE_PROPERTY_ACCESS_TYPE, map.getNamedItem(Pydio.WORKSPACE_PROPERTY_ACCESS_TYPE).getNodeValue());
            properties.setProperty(Pydio.WORKSPACE_PROPERTY_CROSS_COPY, map.getNamedItem(Pydio.WORKSPACE_PROPERTY_ACCESS_TYPE).getNodeValue());
            properties.setProperty(Pydio.WORKSPACE_PROPERTY_SLUG, map.getNamedItem(Pydio.WORKSPACE_PROPERTY_SLUG).getNodeValue());
            properties.setProperty(Pydio.WORKSPACE_PROPERTY_ID, map.getNamedItem(Pydio.WORKSPACE_PROPERTY_ID).getNodeValue());
		}

		if (xml.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
			if (children.getLength() > 0) {
				for (int i = 0; i < children.getLength(); i++) {
					org.w3c.dom.Node n = children.item(i);
					if(Pydio.WORKSPACE_LABEL.equals(n.getNodeName())){
						properties.setProperty(Pydio.NODE_PROPERTY_LABEL, n.getTextContent());
					}else if(Pydio.WORKSPACE_DESCRIPTION.equals(n.getNodeName())){
                        properties.setProperty(Pydio.NODE_PROPERTY_DESCRIPTION, n.getTextContent());
					}
				}
			}
		}
	}

	public void initFromJson(JSONObject json) {		
	}

	public void initFromProperties(Properties p) {
        properties = p;
	}
    @Override
    public void initFromFile(File file) {
        properties = new Properties();
        if(file.isDirectory()) {
            properties.setProperty(Pydio.NODE_PROPERTY_LABEL, file.getName());
            properties.setProperty(Pydio.NODE_PROPERTY_PATH, file.getAbsolutePath());
            properties.setProperty(Pydio.WORKSPACE_PROPERTY_ACCESS_TYPE, Pydio.WORKSPACE_ACCESS_TYPE_FS);
            properties.setProperty(Pydio.WORKSPACE_PROPERTY_ID, file.getAbsolutePath());
            properties.setProperty(Pydio.WORKSPACE_PROPERTY_CROSS_COPY, "true");
            properties.setProperty(Pydio.WORKSPACE_PROPERTY_META_SYNC, "true");
            properties.setProperty(Pydio.WORKSPACE_PROPERTY_SLUG, "true");
        }else{
            try {
                InputStream in = new FileInputStream(file);
                if(file.getName().endsWith(".xml")){
                    properties.loadFromXML(in);
                }else{
                    properties.load(in);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public Properties getProperties() {
        return properties;
    }
    @Override
    public String getProperty(String key) {
        return properties.getProperty(key, "");
    }

    public int type() {
		return Node.TYPE_WORKSPACE;
	}

	public String path() {
		return "/";
	}

	public boolean equals(Object o){
		try{
			return this == o || (o instanceof Node) && ((Node)o).type() == type() && label().equals(((Node)o).label()) && path().equals(((Node)o).path());
		}catch(NullPointerException e){
			return false;
		}
	}
}
