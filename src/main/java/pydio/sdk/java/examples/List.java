package pydio.sdk.java.examples;

import java.io.IOException;

import pydio.sdk.java.core.PydioClient;
import pydio.sdk.java.core.model.Node;
import pydio.sdk.java.core.utils.NodeHandler;

/**
 * Created by jabar on 30/03/2016.
 */
public class List {
    public static void main(String[] arg) throws IOException {
        PydioClient c = new PydioClient("https://mon-nuage.iut-tlse3.fr/", "supportpydio", "PydioTran");
        c.workspaceList(new NodeHandler() {
            @Override
            public void onNode(Node node) {
                System.out.println(node.label());
            }
        });
    }
}
