package com.pydio.sdk.core.model;

import com.pydio.sdk.core.Pydio;
import com.pydio.sdk.core.server.Plugin;

import java.util.List;
import java.util.Properties;

public class WorkspaceNode implements Node {
    private Properties properties;
    private Properties preferences;
    private List<Plugin> plugins;
    private List<String> actions;

    public boolean allowsCrossCopy() {
        return "true".equals(properties.getProperty(Pydio.WORKSPACE_PROPERTY_CROSS_COPY));
    }

    public String slug() {
        return properties.getProperty(Pydio.WORKSPACE_PROPERTY_SLUG);
    }

    public String getDescription() {
        return properties.getProperty(Pydio.NODE_PROPERTY_DESCRIPTION);
    }

    public String getAccessType() {
        return properties.getProperty(Pydio.WORKSPACE_PROPERTY_ACCESS_TYPE);
    }

    public boolean syncable() {
        return "true".equals(getProperty(Pydio.WORKSPACE_PROPERTY_META_SYNC));
    }

    public String label() {
        return properties.getProperty(Pydio.NODE_PROPERTY_LABEL);
    }

    public String getId() {
        return properties.getProperty(Pydio.WORKSPACE_PROPERTY_SLUG);
    }

    public String acl() {
        return properties.getProperty(Pydio.WORKSPACE_PROPERTY_ACL);
    }

    public String owner() {
        return properties.getProperty(Pydio.WORKSPACE_PROPERTY_OWNER);
    }

    public boolean isPublic() {
        String pub = properties.getProperty(Pydio.WORKSPACE_IS_PUBLIC);
        return "true".equals(pub);
    }

    public boolean isReadOnly() {
        return "r".equals(acl());
    }

    public boolean readableWritable() {
        return "rw".equals(acl());
    }

    public boolean isSyncable() {
        return "true".equals(getProperty(Pydio.WORKSPACE_PROPERTY_META_SYNC));
    }

    public boolean isActionDisabled(String action) {
        if (actions == null || actions.size() == 0) {
            return false;
        }

        for (String a : actions) {
            String[] items = a.split(":");
            String actionName = items[0];

            if (action.equals(actionName)) {
                String[] rights = items[1].split(":");
                boolean readChecked = true, writeChecked = true;
                try {
                    if ("false".equals(rights[0])) {
                        readChecked = acl().contains("r");
                    }
                    if ("false".equals(rights[1])) {
                        writeChecked = acl().contains("w");
                    }
                    return readChecked && writeChecked;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean isShared() {
        return "true".equals(properties.getProperty(Pydio.NODE_PROPERTY_AJXP_SHARED)) || "shared".equals(properties.getProperty(Pydio.WORKSPACE_PROPERTY_OWNER));
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public void setPreferences(Properties prefs) {
        this.preferences = prefs;
    }

    public void setPlugins(List<Plugin> plugins){
        this.plugins = plugins;
    }

    public Plugin getPlugin(String id){
        if (this.plugins == null){
            return null;
        }

        for(Plugin p: this.plugins){
            if(p.id.equals(id)){
                return p;
            }
        }

        return null;
    }

    public boolean isLoaded() {
        return this.actions != null && this.preferences != null && this.plugins != null;
    }
    //********************************************************************************************
    //                  Super class: NODE METHODS
    //********************************************************************************************
    @Override
    public void setProperties(Properties p) {
        properties = p;
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key, "");
    }

    @Override
    public void setProperty(String key, String value) {
        if (properties == null) {
            properties = new Properties();
        }
        properties.setProperty(key, value);
    }

    @Override
    public void deleteProperty(String key) {
        if(properties != null && properties.contains(key)){
            properties.remove(key);
        }
    }

    @Override
    public int type() {
        return Node.TYPE_WORKSPACE;
    }

    @Override
    public String id() {
        return properties.getProperty(Pydio.WORKSPACE_PROPERTY_SLUG);
    }

    @Override
    public String path() {
        return "/";
    }

    @Override
    public boolean equals(Object o) {
        try {
            return this == o || (o instanceof WorkspaceNode) && ((WorkspaceNode) o).getId() == getId() && ((WorkspaceNode) o).label() == label();
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
