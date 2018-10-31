package pydio.sdk.java.core.utils;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Properties;

import pydio.sdk.java.server.Plugin;

/**
 * Created by pydio on 03/06/2015.
 */
public class RegistrySaxHandler extends DefaultHandler {

    private RegistryItemHandler handler;

    private boolean inside_actions = false, inside_action = false;
    private boolean inside_plugins = false;
    private boolean inside_repositories = false;
    private boolean inside_repo = false;
    private boolean inside_preferences = false;

    private boolean insideAjxpPlugin = false;
    private boolean insidePluginConfigs = false;

    private Plugin currentPlugin;
    private String pluginProperty;


    public boolean hasUserElement = false;
    private String mUser = null;


    private String action = "", actionRead, actionWrite;

    private Properties p;
    private String inner_element;

    public RegistrySaxHandler(RegistryItemHandler handler){
        this.handler = handler;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if("user".equals(qName) && mUser == null){
            hasUserElement = true;
            mUser = attributes.getValue("id");
            return;
        }

        if("action".equals(qName)){
            inside_action = true;
            action = attributes.getValue("name");
            return;
        }

        if("property".equals(qName) && insidePluginConfigs) {
            pluginProperty = attributes.getValue(attributes.getIndex("name"));
        }

        if("rightsContext".equals(qName) && inside_action){
            actionRead = attributes.getValue("read");
            actionWrite = attributes.getValue("write");
            return;
        }


        if("action".equals(qName) && inside_actions){
            action = attributes.getValue(attributes.getIndex("name"));
            return;
        }

        if("pref".equals(qName) && inside_preferences){
            handler.onPref(attributes.getValue(attributes.getIndex("name")), attributes.getValue(attributes.getIndex("value")));
        }

        if("plugin_configs".equals(qName) && insideAjxpPlugin) {
            insidePluginConfigs = true;
        }

        if("repo".equals(qName) && inside_repositories){
            inside_repo = true;
            p = new Properties();
            for(int i = 0; i < attributes.getLength(); i++){
                p.setProperty(attributes.getLocalName(i), attributes.getValue(i));
            }
            return;
        }

        if(inside_repo && "label".equals(qName.toLowerCase()) || "description".equals(qName.toLowerCase())){
            inner_element = qName;
            return;
        }

        if("actions".equals(qName)){
            inside_actions = true;
            return;
        }

        if("plugins".equals(qName)){
            inside_plugins = true;
            return;
        }

        if("repositories".equals(qName)){
            inside_repositories = true;
            return;
        }

        if("preferences".equals(qName)){
            inside_preferences = true;
            return;
        }

        if("ajxp_plugin".equals(qName) || "plugin".equals(qName)){
            insideAjxpPlugin = true;
            currentPlugin = new Plugin();
            currentPlugin.id = attributes.getValue("id");
            currentPlugin.name = attributes.getValue("name");
            currentPlugin.label = attributes.getValue("label");
            currentPlugin.description = attributes.getValue("description");
            currentPlugin.configs = new Properties();
        }

        if(inside_repositories && "repo".equals(qName)){
            inside_repo = true;
            return;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String content = new String(ch, start, length);
        if(inside_repo && inner_element != null && inner_element.length() != 0){
            p.setProperty(inner_element, content);
            return;
        }

        if(insidePluginConfigs && pluginProperty != null){
            if(currentPlugin != null) {
                if(currentPlugin.configs == null){
                    currentPlugin.configs = new Properties();
                }
                currentPlugin.configs.put(pluginProperty, content);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if(inside_action && "action".equals(qName)){
            inside_action = false;
            handler.onAction(action, actionRead, actionWrite);
            actionRead = actionWrite = null;
            return;
        }

        if(inside_plugins && "plugins".equals(qName)){
            inside_plugins = false;
            return;
        }

        if(inside_repositories && "repositories".equals(qName)){
            inside_repositories = false;
            return;
        }

        if(inside_actions && "actions".equals(qName)){
            inside_actions = false;
            return;
        }

        if(inside_preferences && "preferences".equals(qName)){
           inside_preferences = false;
            return;
        }

        if(inside_repo){
            if("label".equals(qName.toLowerCase()) || "description".equals(qName.toLowerCase())){
                inner_element = "";
                return;
            }

            if("repo".equals(qName)){
                inside_repo = false;
                handler.onWorkspace(p);
                p = null;
                return;
            }
        }

        if(insidePluginConfigs && "plugin_configs".equals(qName)) {
            insidePluginConfigs = false;
        }

        if ("ajxp_plugin".equals(qName) || "plugin".equals(qName)) {
            if(currentPlugin != null){
                handler.onPlugin(currentPlugin);
                currentPlugin = null;
            }
        }
    }
}
