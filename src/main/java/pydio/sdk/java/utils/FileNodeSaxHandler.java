package pydio.sdk.java.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Properties;

import pydio.sdk.java.model.Node;
import pydio.sdk.java.model.NodeFactory;
import pydio.sdk.java.model.NodeHandler;
import pydio.sdk.java.model.TreeNode;

/**
 * Created by pydio on 09/02/2015.
 */
public class FileNodeSaxHandler extends DefaultHandler {

    boolean mInsideTree = false;
    String mInnerElement = "";

    public int mParsedCount = 0;
    public boolean mPagination = false;
    public int mPaginationTotalItem;
    public int mPaginationTotalPage;
    public int mPaginationCurrentPage;
    public TreeNode mRootNode;

    NodeHandler mHandler;
    Properties p = null;

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if("pagination".equals(qName)){
            mPagination = true;
            mPaginationTotalItem = Integer.parseInt(attributes.getValue("count"));
            mPaginationTotalPage = Integer.parseInt(attributes.getValue("total"));
            mPaginationCurrentPage = Integer.parseInt(attributes.getValue("current"));
        }

        if("tree".equals(qName)){
            mInsideTree = true;
            mParsedCount++;
            p = new Properties();
            for(int i = 0; i < attributes.getLength(); i++){
                p.setProperty(attributes.getLocalName(i), attributes.getValue(i));
            }
            if(mParsedCount == 1){
                if("".equals(p.getProperty(Pydio.NODE_PROPERTY_FILENAME))){
                    p.setProperty(Pydio.NODE_PROPERTY_FILENAME, "/");
                }
                mRootNode = (TreeNode) NodeFactory.createNode(Node.TYPE_TREE, p);
                p = null;
            }
        }

        if(!mInsideTree) return;

        if("label".equals(qName.toLowerCase()) || "description".equals(qName.toLowerCase())){
            mInnerElement = qName;
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(mInsideTree && "tree".equals(qName)){
            if(p != null) {
                mHandler.processNode(NodeFactory.createNode(Node.TYPE_TREE, p));
                p = null;
            }
            mInsideTree = false;
            return;
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if(mInsideTree && p != null){
            p.setProperty(mInnerElement, new String(ch, start, length));
        }
    }

    public void endDocument() throws SAXException {}

    public FileNodeSaxHandler(NodeHandler nodeHandler){
        this.mHandler = nodeHandler;
    }
}
