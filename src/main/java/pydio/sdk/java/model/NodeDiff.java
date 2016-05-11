package pydio.sdk.java.model;

import org.w3c.dom.Document;

import java.util.ArrayList;

import pydio.sdk.java.utils.Pydio;

/**
 * Created by jabar on 10/05/2016.
 */
public class NodeDiff {

    public ArrayList<Node> deleted = null;
    public ArrayList<Node> added = null;
    public ArrayList<Node> updated = null;

    public static NodeDiff create(Document doc){
        NodeDiff nodeDiff = new NodeDiff();
        org.w3c.dom.Node diff = doc.getElementsByTagName(Pydio.XML_NODES_DIFF).item(0);
        if(diff != null) {
            for (int i = 0; i < diff.getChildNodes().getLength(); i++) {
                org.w3c.dom.Node child = diff.getChildNodes().item(i);
                String tag = child.getNodeName();

                ArrayList<Node> list = null;

                if (Pydio.NODE_DIFF_REMOVE.equals(tag)) {
                    if (nodeDiff.deleted == null) {
                        nodeDiff.deleted = new ArrayList<Node>();
                    }
                    list = nodeDiff.deleted;
                } else if (Pydio.NODE_DIFF_ADD.equals(tag)) {
                    if (nodeDiff.added == null) {
                        nodeDiff.added = new ArrayList<Node>();
                    }
                    list = nodeDiff.added;
                } else if (Pydio.NODE_DIFF_UPDATE.equals(tag)) {
                    if (nodeDiff.updated == null) {
                        nodeDiff.updated = new ArrayList<Node>();
                    }
                    list = nodeDiff.updated;
                }

                for (int j = 0; j < child.getChildNodes().getLength(); j++) {
                    list.add(NodeFactory.createNode(child.getChildNodes().item(j)));
                }
            }
        }
        return nodeDiff;
    }
}
