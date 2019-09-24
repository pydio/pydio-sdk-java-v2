package com.pydio.sdk.core.model;

import com.pydio.sdk.core.Pydio;

import java.util.Properties;

/**
 * Created by jabar on 09/11/2015.
 */
public class SearchNode implements Node {

    private String label;
    private Properties properties;

    public SearchNode(String workspace, String label){
        this.label = label;
        properties = new Properties();
        if(workspace != null) {
            properties.setProperty(Pydio.NODE_PROPERTY_WORKSPACE_SLUG, workspace);
        }
    }

    @Override
    public int type() {
        return Node.TYPE_SEARCH;
    }

    @Override
    public String id() {
        return "search://" + label;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String path() {
        return label;
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    @Override
    public void deleteProperty(String key) {
        properties.remove(key);
    }

    @Override
    public void setProperties(Properties p) {
        properties = p;
    }

    @Override
    public String getEncoded() {
        return null;
    }

    @Override
    public int compare(Node node) {
        if (this.label.equals(node.label())) {
            return 0;
        }
        return 1;
    }

    @Override
    public String getEncodedHash() {
        return null;
    }
}
