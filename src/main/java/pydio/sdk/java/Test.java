package pydio.sdk.java;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pydio.sdk.java.model.Node;
import pydio.sdk.java.utils.NodeHandler;
import pydio.sdk.java.utils.Log;
import pydio.sdk.java.utils.ProgressListener;
import pydio.sdk.java.utils.UnexpectedResponseException;

/**
 * Created by pydio on 11/02/2015.
 */
public class Test {
    public static void main(String[] arg) {

        final PydioClient client = new PydioClient("http://sandbox.pydio.com/enterprise/", "jabar", "security");
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

            OutputStream out2 = new FileOutputStream("C:\\Users\\jabar\\Downloads\\Pydio\\pyd.png");
            client.download("1", new String[]{"/pydio.png"}, out2, new ProgressListener() {
                @Override
                public void onProgress(long progress) {
                    System.out.flush();
                    System.out.println("downloaded : " + progress + " octets");
                }
            });
            out2.close();

            File original = new File("C:\\Users\\jabar\\Downloads\\Pydio\\pydio.png");
            File copy = new File("C:\\Users\\jabar\\Downloads\\Pydio\\pyd.png");


            InputStream in = new FileInputStream(original);
            InputStream in2 = new FileInputStream(copy);

            System.out.println("Length original : " + original.length());
            System.out.println("Length copy : " + copy.length());

            int r1 = 0, r2 = 0;
            int mismatch_count = 0;
            for(int i = 0; i <= Math.max(original.length(), copy.length()); i++){
                r1 = in.read();
                r2 = in2.read();
                if(r1 != r2){
                    System.out.println(i + " eme bytes different " + r1 + " <> " + r2);
                    mismatch_count ++;
                    if(mismatch_count > 10){
                        break;
                    }
                }
            }

            in.close();
            in2.close();

        } catch (IOException e) {
            Log.error("Failed to load the \"My Files\" workspace content");
        }
    }
}
