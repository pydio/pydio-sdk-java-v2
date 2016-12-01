package pydio.sdk.java.examples;

import org.json.JSONObject;

import pydio.sdk.java.core.PydioClient;

/**
 * Created by jabar on 07/04/2016.
 */
public class MinisiteShare {
    public static void main(String[] args) throws Exception{

        final PydioClient client = new PydioClient("http://54.154.218.27", "jabar", "pydio@2015");
        client.login();
        client.upload("1", "/", "Text content of the shared file".getBytes(), "share_file.txt", false, null, null);

        JSONObject info = client.shareInfo("1", "/share_file.txt");

        if(info == null){
            client.minisiteShare("1", "/share_file.txt", "Share", "Example", "password", 1, 1, true, true);
            info = client.shareInfo("1", "/share_file.txt");
        }

        String link = info.getJSONObject("minisite").getString("public_link");
        System.out.println(link);
    }
}
