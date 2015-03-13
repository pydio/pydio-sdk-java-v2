package pydio.sdk.java.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Properties;

import pydio.sdk.java.model.Node;
import pydio.sdk.java.model.NodeFactory;
import pydio.sdk.java.model.NodeHandler;

/**
 * Created by pydio on 09/02/2015.
 */
public class FileNodeSaxHandler extends DefaultHandler {

    boolean inside_tree = false;
    String inner_element = "";
    int count = 0, offset, max;
    NodeHandler handler;

    Properties p = null;
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        count++;
        if(count < offset || count > max){
            return;
        }
        if("tree".equals(qName)){
            inside_tree = true;
            p = new Properties();
            for(int i = 0; i < attributes.getLength(); i++){
                p.setProperty(attributes.getLocalName(i), attributes.getValue(i));
            }
        }
        if(!inside_tree) return;
        if("label".equals(qName.toLowerCase()) || "description".equals(qName.toLowerCase())){
            inner_element = qName;
        }
    }
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(count < offset || count > max){
            return;
        }
        if(inside_tree && "tree".equals(qName)){
            handler.processNode(NodeFactory.createNode(Node.TYPE_FILE, p));
            p = null;
            inside_tree = false;
            return;
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if(count < offset || count > max){
            return;
        }
        if(inside_tree){
            p.setProperty(inner_element, new String(ch, start, length));
        }
    }

    public void endDocument() throws SAXException {
        handler.processNode(null);
    }

    public FileNodeSaxHandler(NodeHandler nodeHandler, int offset, int max){
        this.handler = nodeHandler;
        this.offset = offset;
        this.max = max;
    }
}
