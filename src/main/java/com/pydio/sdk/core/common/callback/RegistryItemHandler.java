package com.pydio.sdk.core.common.callback;

import com.pydio.sdk.core.server.Plugin;

import java.util.Properties;

/**
 * Created by pydio on 04/06/2015.
 */
public abstract class RegistryItemHandler {
    //abstract void onNewItem(int type, Object o);
    public void onPref(String name, String value) {
    }

    public void onAction(String action, String read, String write) {
    }

    public void onWorkspace(Properties p) {
    }

    public void onPlugin(Plugin p) {
    }
}
