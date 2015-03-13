package pydio.sdk.java;


import org.apache.http.auth.UsernamePasswordCredentials;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import pydio.sdk.java.auth.CredentialsProvider;
import pydio.sdk.java.model.FileNode;
import pydio.sdk.java.model.Node;
import pydio.sdk.java.model.NodeFactory;
import pydio.sdk.java.model.NodeHandler;
import pydio.sdk.java.model.PydioMessage;
import pydio.sdk.java.model.ServerNode;
import pydio.sdk.java.model.WorkspaceNode;
import pydio.sdk.java.transport.Transport;
import pydio.sdk.java.utils.MessageHandler;
import pydio.sdk.java.utils.ProgressListener;

/**
 * Created by pydio on 11/02/2015.
 */
public class Test {
    public static void main(String[] arg){

        final ArrayList<Node> nodes = new ArrayList<>();
        //Configuring a pydio server by creating a serverNode.
        ServerNode server = (ServerNode) NodeFactory.createNode(Node.TYPE_SERVER);
        server.setProtocol("http");
        server.setHost("54.154.218.27");
        server.setPath("/");
        server.setLegacy(false);

        //Instantiation of a Pydio Client for peforming operations
        PydioClient client = PydioClient.configure(server, Transport.MODE_SESSION, new CredentialsProvider() {
            @Override
            public UsernamePasswordCredentials requestForLoginPassword() {
                return new UsernamePasswordCredentials("jabar", "rabaj@2015");
            }
            @Override
            public X509Certificate requestForCertificate() {
                return null;
            }
        });
        //Load server remote configs useful for uploads
        client.getRemoteConfigs();
        //selecting a workspace by id
        if(client.selectWorkspace("1")){
            WorkspaceNode ws = client.workspace;
            //listing the selected workspace file list and for each file display the name
            System.out.println("Listing de [ws|"+ws.label()+"] :");
            client.listChildren(ws, new NodeHandler() {
                @Override
                public void processNode(Node node) {
                    //filter by name
                    if (node != null && !node.label().equals("Recycle Bin")) {
                        nodes.add((FileNode) node);
                        System.out.println("+ " + node.label());
                    }
                }
            }, 0, 1000);
            //downloading the hole workspace into a zip file
            try {
                client.read(nodes.toArray(new Node[nodes.size()]), new FileOutputStream("C:\\Users\\pydio\\Desktop\\download.zip"), new ProgressListener() {
                    @Override
                    public void onProgress(long progress) {
                        System.out.println("Progress :" + progress);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Uploading a file to a server
            client.write(ws, new File("C:\\Users\\pydio\\Downloads\\debian-7.6.0-i386-netinst.iso"), new ProgressListener() {
                @Override
                public void onProgress(long progress) {
                    System.out.println(progress);
                }
            }, true, null, new MessageHandler() {
                @Override
                public void onMessage(PydioMessage m) {
                    System.out.println(m.getContent());
                }
            });
            nodes.clear();
            //listing the selected workspace file list and for each file display the name
            System.out.println("Listing de [ws|"+ws.label()+"] :");
            client.listChildren(ws, new NodeHandler() {
                @Override
                public void processNode(Node node) {
                    if (node != null && !node.label().equals("Recycle Bin")) {
                        nodes.add((FileNode) node);
                        System.out.println("+ " + node.label());
                    }
                }
            }, 0, 1000);
        }
    }
}
