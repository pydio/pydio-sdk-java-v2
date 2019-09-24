package com.pydio.sdk.core.model;

import java.util.Properties;

public class ObjectNode implements Node {

    protected String path;
    protected String label;
    private Properties properties;

    @Override
    public void setProperty(String key, String value) {
        if (properties == null) {
            properties = new Properties();
        }
        properties.setProperty(key, value);
    }

    @Override
    public void setProperties(Properties p) {

    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key, "");
    }

    @Override
    public void deleteProperty(String key) {
        if(properties != null && properties.contains(key)){
            properties.remove(key);
        }
    }

    @Override
    public int type() {
        return Node.TYPE_LOCAL_NODE;
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        try {
            return this == o || (o instanceof Node) && ((Node) o).type() == type() && label().equals(((Node) o).label()) && path().equals(((Node) o).path());
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public String getEncoded() {
        return null;
    }

    @Override
    public int compare(Node node) {
        return 0;
    }

    @Override
    public String getEncodedHash() {
        return null;
    }
}
