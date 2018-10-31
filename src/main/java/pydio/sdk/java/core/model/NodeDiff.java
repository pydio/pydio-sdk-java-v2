package pydio.sdk.java.core.model;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

import pydio.sdk.java.core.utils.Pydio;

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
                    list = nodeDiff.deleted;
                } else if (Pydio.NODE_DIFF_ADD.equals(tag)) {
                    list = nodeDiff.added;
                } else if (Pydio.NODE_DIFF_UPDATE.equals(tag)) {
                    list = nodeDiff.updated;
                }

                if(list == null){
                    list = new ArrayList<>();
                }

                for (int j = 0; j < child.getChildNodes().getLength(); j++) {
                    org.w3c.dom.Node c = child.getChildNodes().item(j);
                    Node pydNode = pydio.sdk.java.core.model.NodeFactory.createNode(c);
                    if( pydNode != null){
                        list.add(pydNode);
                    }
                }
            }
        }

        return nodeDiff;
    }
}
