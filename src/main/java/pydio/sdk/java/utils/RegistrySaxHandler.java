package pydio.sdk.java.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Properties;

/**
 * Created by pydio on 03/06/2015.
 */
public class RegistrySaxHandler extends DefaultHandler {

    RegistryItemHandler handler;

    boolean inside_actions = false, inside_action = false;
    boolean inside_plugins = false;
    boolean inside_repositories = false;
    boolean inside_repo = false;
    boolean inside_preferences = false;


    public boolean mHasUserElement = false;
    public String mUser = null;


    String action = "", actionRead, actionWrite;
    Properties p;
    String inner_element;

    public RegistrySaxHandler(RegistryItemHandler handler){
        this.handler = handler;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if("user".equals(qName) && mUser == null){
            mHasUserElement = true;
            mUser = attributes.getValue("id");
            return;
        }

        if("action".equals(qName)){
            inside_action = true;
            action = attributes.getValue("name");
            return;
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

        if("plugin".equals(qName) && inside_plugins){
            /*String action = attributes.getValue(attributes.getIndex("name"));
            handler.onNewItem(Pydio.REGISTRY_ITEM_PLUGIN, action);*/
            return;
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

        if(inside_repositories && "repo".equals(qName)){
            inside_repo = true;
            return;
        }
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(inside_repo && inner_element.length() != 0){
            p.setProperty(inner_element, new String(ch, start, length));
        }
    }
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
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
    }
}
