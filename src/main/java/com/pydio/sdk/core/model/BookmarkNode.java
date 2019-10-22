package com.pydio.sdk.core.model;

import java.util.Properties;

public class BookmarkNode implements Node {

    private String label;
    private Properties properties;

    public BookmarkNode(String label) {
        this.label = label;
        this.properties = new Properties();
    }

    @Override
    public int type() {
        return TYPE_BOOKMARKS;
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String path() {
        return "/";
    }

    @Override
    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    @Override
    public void setProperty(String key, String value) {
        this.properties.put(key, value);
    }

    @Override
    public void deleteProperty(String key) {
        this.properties.remove(key);
    }

    @Override
    public void setProperties(Properties p) {
        this.properties = p;
    }

    @Override
    public String getEncoded() {
        return "";
    }

    @Override
    public int compare(Node node) {
        if (node == null) {
            return Node.different;
        }
        if (!this.label.equals(node.label())) {
            return Node.different;
        }
        return Node.same;
    }

    @Override
    public String getEncodedHash() {
        return null;
    }
}
