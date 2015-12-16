package pydio.sdk.java.utils;

import java.util.Properties;

import pydio.sdk.java.model.WorkspaceNode;

/**
 * Created by pydio on 04/06/2015.
 */
public abstract class RegistryItemHandler {
    //abstract void onNewItem(int type, Object o);
    protected void onPref(String name, String value){}
    protected void onAction(String action){}
    protected void onWorkspace(Properties p){}
}
