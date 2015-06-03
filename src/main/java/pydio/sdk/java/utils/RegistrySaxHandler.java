package pydio.sdk.java.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by pydio on 03/06/2015.
 */
public abstract class RegistrySaxHandler extends DefaultHandler {
    boolean started_actions = false;
    public abstract void onAction(String action);
    //public abstract void onPlugin(String plugin);
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if("action".equals(qName) && started_actions){
            String action = attributes.getValue(attributes.getIndex("name"));
            onAction(action);
            return;
        }
        if("actions".equals(qName)){
            started_actions = true;
        }
    }
}
