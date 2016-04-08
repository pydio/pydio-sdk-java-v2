package examples;

import java.io.IOException;
import pydio.sdk.java.PydioClient;

/**
 * Created by jabar on 01/04/2016.
 */
public class Upload {

    public static void main(String[] args) throws IOException {
        final PydioClient client = new PydioClient("http://54.154.218.27", "jabar", "pydio@2015");
        client.upload("1", "/", "Content of the file to upload".getBytes(), "file.txt", true, null, null);
    }
}
