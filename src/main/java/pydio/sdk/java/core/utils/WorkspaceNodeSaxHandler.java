package pydio.sdk.java.core.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Properties;

import pydio.sdk.java.core.model.Node;
import pydio.sdk.java.core.model.NodeFactory;

/**
 * Created by pydio on 09/02/2015.
 */
public class WorkspaceNodeSaxHandler extends DefaultHandler {

    boolean inside_repo = false, inside_label = false, inside_description = false;
    String inner_element = "";
    NodeHandler handler;
    Properties p = null;
    //String tabs = "";

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        /*tabs += "\t";
        Log.info(tabs + qName);*/

        if("repo".equals(qName)){
            inside_repo = true;
            p = new Properties();
            for(int i = 0; i < attributes.getLength(); i++){
                p.setProperty(attributes.getLocalName(i), attributes.getValue(i));
            }
            return;
        }

        if(!inside_repo) return;

        inside_label = "label".equals(qName);
        inside_description = "description".equals(qName);
        if(inside_label || inside_description){
            inner_element = qName;
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        /*Log.info(tabs + qName);
        tabs = tabs.substring(0, tabs.length() - 1);*/

        if(inside_repo && (inside_label || inside_description)){
            if(inside_label){
                inside_label = false;
            } else if(inside_description){
                inside_description = false;
            }
            return;
        }

        if(inside_repo && "repo".equals(qName)){
            handler.onNode(NodeFactory.createNode(Node.TYPE_WORKSPACE, p));
            p = null;
            inside_repo = false;
            return;
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if(inside_repo && (inside_label || inside_description)){
            String content = new String(ch, start, length);
            if (!p.containsKey(inner_element)) {
                p.setProperty(inner_element, content);
            }
        }
    }


    public WorkspaceNodeSaxHandler(NodeHandler nodeHandler, int offset, int max){
        this.handler = nodeHandler;
    }
}
