package com.pydio.sdk.core.model;

import java.util.Properties;

public class BookmarkNode implements Node {

    private String label;
    public BookmarkNode(String label) {
        this.label = label;
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
        return null;
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public void setProperty(String key, String value) {

    }

    @Override
    public void deleteProperty(String key) {

    }

    @Override
    public void setProperties(Properties p) {

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
