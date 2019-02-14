package com.pydio.sdk.core.model.parser;

import com.pydio.sdk.core.Pydio;
import com.pydio.sdk.core.common.callback.NodeHandler;
import com.pydio.sdk.core.model.FileNode;
import com.pydio.sdk.core.model.Node;
import com.pydio.sdk.core.model.NodeFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Properties;

/**
 * Created by pydio on 09/02/2015.
 */
public class TreeNodeSaxHandler extends DefaultHandler {

    boolean mInsideTree = false;
    String mInnerElement = "";

    public int mParsedCount = 0;
    public boolean mPagination = false;
    public int mPaginationTotalItem;
    public int mPaginationTotalPage;
    public int mPaginationCurrentPage;
    public FileNode mRootNode;

    NodeHandler mHandler;
    Properties p = null;

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if("pagination".equals(qName)){
            mPagination = true;
            mPaginationTotalItem = Integer.parseInt(attributes.getValue("count"));
            mPaginationTotalPage = Integer.parseInt(attributes.getValue("total"));
            mPaginationCurrentPage = Integer.parseInt(attributes.getValue("current"));
        }

        if ("tree".equals(qName)) {
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
                mRootNode = (FileNode) NodeFactory.createNode(Node.TYPE_REMOTE_FILE, p);
                p = null;
            }
        }

        if(!mInsideTree) return;

        if("label".equals(qName.toLowerCase()) || "description".equals(qName.toLowerCase())){
            mInnerElement = qName;
        }
    }

    public void endElement(String uri, String localName, String qName) {
        if (mInsideTree && "tree".equals(qName)) {
            if(p != null) {
                Node node = NodeFactory.createNode(Node.TYPE_REMOTE_FILE, p);
                if(node != null){
                    node.setProperty(Pydio.NODE_PROPERTY_UUID, node.path());
                    mHandler.onNode(node);
                }
                p = null;
            }
            mInsideTree = false;
            return;
        }
    }

    public void characters(char ch[], int start, int length) {
        if(mInsideTree && p != null){
            p.setProperty(mInnerElement, new String(ch, start, length));
        }
    }

    public void endDocument() {
    }

    public TreeNodeSaxHandler(NodeHandler nodeHandler){
        this.mHandler = nodeHandler;
    }
}
