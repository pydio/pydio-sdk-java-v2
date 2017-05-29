package pydio.sdk.java.examples;

import pydio.sdk.java.core.PydioClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by jabar on 01/04/2016.
 */
public class Upload {

    public static void main(String[] args) throws IOException {
        final PydioClient client = new PydioClient("http://54.154.218.27", "jabar", "pydio@2015");
        client.login();
        File file = new File("C:\\Users\\jabar\\Downloads\\file.txt");
        FileInputStream in = new FileInputStream(file);
        //client.upload("1", "/", in, file.length(), "file.txt", true, null, null);
        in.close();
    }
}
