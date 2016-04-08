package pydio.sdk.java;


import java.io.IOException;

import pydio.sdk.java.model.Node;
import pydio.sdk.java.utils.NodeHandler;
import pydio.sdk.java.utils.Log;

/**
 * Created by pydio on 11/02/2015.
 */
public class Test {


    public static void main(String[] arg) {

        final PydioClient client = new PydioClient("http://192.168.0.91", "pydio", "pydiopassword");
        NodeHandler handler = new NodeHandler() {
            @Override
            public void onNode(Node node) {
                Log.success(node.label());
            }
        };

        try {
            client.workspaceList(handler);
        } catch (IOException e) {
            Log.error("Failed to get the workspaces");
        }

        try {
            client.ls("1", "/", handler);
        } catch (IOException e) {
            Log.error("Failed to load the \"My Files\" workspace content");
        }
    }
}
