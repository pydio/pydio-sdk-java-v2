package com.pydio.sdk.core.common.parser;

import com.pydio.sdk.core.common.callback.NodeHandler;
import com.pydio.sdk.core.model.Node;
import com.pydio.sdk.core.model.NodeFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Properties;

/**
 * Created by pydio on 09/02/2015.
 */
public class WorkspaceNodeSaxHandler extends DefaultHandler {

    private boolean inside_repo = false, inside_label = false, inside_description = false;
    private String inner_element = "";
    private NodeHandler handler;
    private Properties p = null;
    //String tabs = "";

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        /*tabs += "\t";
        //Log.info(tabs + qName);*/

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

    public void endElement(String uri, String localName, String qName) {
        /*//Log.info(tabs + qName);
        tabs = tabs.substring(0, tabs.length() - 1);*/

        if(inside_repo && (inside_label || inside_description)){
            if(inside_label){
                inside_label = false;
            } else {
                inside_description = false;
            }
            return;
        }

        if(inside_repo && "repo".equals(qName)){
            handler.onNode(NodeFactory.createNode(Node.TYPE_WORKSPACE, p));
            p = null;
            inside_repo = false;
        }
    }

    public void characters(char ch[], int start, int length) {
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
