package pydio.sdk.java.core.utils;

import java.util.Properties;

import pydio.sdk.java.server.Plugin;

/**
 * Created by pydio on 04/06/2015.
 */
public abstract class RegistryItemHandler {
    //abstract void onNewItem(int type, Object o);
    protected void onPref(String name, String value){}
    protected void onAction(String action, String read, String write){}
    protected void onWorkspace(Properties p){}
    protected void onPlugin(Plugin p){}
}
