package com.pydio.sdk.examples;

import com.pydio.sdk.core.Client;
import com.pydio.sdk.core.common.errors.Error;
import com.pydio.sdk.core.common.errors.SDKException;
import com.pydio.sdk.core.model.Message;
import com.pydio.sdk.core.model.ServerNode;

import java.io.ByteArrayInputStream;

public class Upload {


    public static void main(String[] args) {
        ServerNode node = new ServerNode();
        Error error = node.resolve("https://server-address");
        if (error != null) {
            // handle code here
            System.out.println(error);
            return;
        }

        Client client = Client.get(node);
        client.setCredentials(new Credentials("login", "password"));


        String targetWorkspaces = "my-files";
        String targetDir = "/"; // root
        String name = "hello.txt";

        byte[] content = "Hello Pydio!".getBytes();
        ByteArrayInputStream source = new ByteArrayInputStream(content);

        // Message upload(InputStream source, long length, String ws, String path, String name, boolean autoRename, final TransferProgressListener progressListener) throws SDKException;
        try {
            Message msg = client.upload(source, content.length, targetWorkspaces, targetDir, name, true, (progress) -> {
                System.out.printf("\r%d bytes written\n", progress);
                return false;
            });
        } catch (SDKException e) {
            e.printStackTrace();
        }
    }
}
