package examples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Scanner;

import pydio.sdk.java.PydioClient;
import pydio.sdk.java.model.Node;
import pydio.sdk.java.model.PydioMessage;
import pydio.sdk.java.utils.AuthenticationHelper;
import pydio.sdk.java.utils.MessageHandler;
import pydio.sdk.java.utils.NodeHandler;

/**
 * Created by jabar on 30/03/2016.
 */
public class List {
    public static void main(String[] arg) {

        System.out.println("JAVA VERSION " + System.getProperty("java.version"));

        //Setting up the client
        PydioClient client = new PydioClient("http://54.154.218.27");
        client.setAuthenticationHelper(new AuthenticationHelper() {
            public String[] getCredentials() {
                String[] credentials = new String[2];
                System.out.println("AUTHENTICATION:");
                System.out.print("username : ");
                credentials[0] = new Scanner(System.in).nextLine();
                System.out.print("password : ");
                credentials[1] = new Scanner(System.in).nextLine();
                return credentials;
            }
        });
        // the ID of the workspace you work on
        String workspaceID = "1";

        //Create the MessageHandler to process the creation resposne
        final MessageHandler messageHandler = new MessageHandler(){
            public void onMessage(PydioMessage m){
                System.out.println(m.text());
            }
        };

        //Create the NodeHandler to process every parsed node
        final NodeHandler nodeHandler = new NodeHandler(){
            public void onNode(Node node){
                System.out.println(node.label());
            }
        };


        try {
            //create "Test" directory at the root of the workspace
            client.mkdir(workspaceID, "/", "Test", messageHandler);

            //create text file inside "Test" directory and set its content
            client.upload("1", "/Test", "Content of the file".getBytes(), "text.txt", true, null, null);

            //list "/Test" folder
            client.ls(workspaceID, "/Test", nodeHandler);

            //download the text.txt file content
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            client.download(workspaceID, new String[]{"/Test/text.txt"}, out, null);
            System.out.println("Downloaded content : " + new String(out.toByteArray()));

        } catch (IOException e) {
            e.printStackTrace();
            client.responseStatus();
        }
    }
}
