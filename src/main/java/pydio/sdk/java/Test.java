package pydio.sdk.java;


import org.json.JSONObject;

import java.security.KeyException;
import java.util.ArrayList;

import pydio.sdk.java.auth.CommandlineAuthenticationHelper;
import pydio.sdk.java.model.ChangeProcessor;
import pydio.sdk.java.model.Node;
import pydio.sdk.java.model.NodeFactory;
import pydio.sdk.java.model.NodeHandler;
import pydio.sdk.java.model.ServerNode;
import pydio.sdk.java.model.WorkspaceNode;
import pydio.sdk.java.transport.Transport;

/**
 * Created by pydio on 11/02/2015.
 */
public class Test {
    public static void main(String[] arg) throws KeyException {

        final ArrayList<Node> nodes = new ArrayList<>();
        //Configuring a pydio server by creating a serverNode.
        ServerNode server = (ServerNode) NodeFactory.createNode(Node.TYPE_SERVER);
        server.setProtocol("http");
        server.setHost("54.154.218.27");
        server.setPath("/");
        server.setLegacy(false);

        CommandlineAuthenticationHelper helper = new CommandlineAuthenticationHelper();
        PydioClient client = PydioClient.configure(server, Transport.MODE_SESSION);
        client.setAuthenticationHelper(helper);

        final Node[] recycle = new Node[1];

        if (client.selectWorkspace("1")) {
            WorkspaceNode ws = client.workspace;

            System.out.println("Listing de [ws|" + ws.label() + "] :");
            client.listChildren(ws, new NodeHandler() {
                @Override
                public void processNode(Node node) {
                    //filter by name
                    if (node != null && !node.label().equals("Recycle Bin")) {
                        nodes.add(node);
                        System.out.println("+ " + node.label());
                    }else if (node.label().equals("Recycle Bin")){
                        recycle[0] = node;
                    }
                }
            }, 0, 1000);
/*
            String name = "funta";
            client.createFile(name, new MessageHandler() {
                @Override
                public void onMessage(PydioMessage m) {
                    System.out.println(m.getContent());
                }
            });

            client.compress(new Node[]{nodes.get(1), nodes.get(2)}, "test2.zip", true, new MessageHandler() {
                @Override
                public void onMessage(PydioMessage m) {
                    System.out.println(m.getContent());
                }
            });

            client.createFolder(client.workspace, name, new MessageHandler() {
                @Override
                public void onMessage(PydioMessage m) {
                    System.out.println(m.getContent());
                }
            });

            nodes.clear();
            client.listChildren(recycle[0], new NodeHandler() {
                @Override
                public void processNode(Node node) {
                    nodes.add(node);
                    System.out.println("+ " + node.label());
                }
            }, 0, 1000);

            client.compress(new Node[]{nodes.get(0), nodes.get(1)}, "test.zip", true, new MessageHandler() {
                @Override
                public void onMessage(PydioMessage m) {
                    System.out.println(m.getContent());
                }
            });

            /*int index = new Scanner(System.in).nextInt();

            client.restore(nodes.get(index), new MessageHandler() {
                @Override
                public void onMessage(PydioMessage m) {
                    System.out.println(m.getContent());
                }
            });*/

            /*client.changes(0, true, "/moi", new ChangeProcessor() {
                @Override
                public void process(String[] change) {
                    System.out.println(change.toString());
                }
            });*/

            JSONObject object = client.stats(new Node[]{nodes.get(0), nodes.get(1), nodes.get(2)}, true, new ChangeProcessor() {
                @Override
                public void process(String[] change) {
                    System.out.println(change);
                }
            });

            System.out.println(object.toString());
        }
    }
}
