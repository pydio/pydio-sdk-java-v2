package pydio.sdk.java.examples;

import java.io.IOException;

import pydio.sdk.java.core.PydioClient;
import pydio.sdk.java.core.utils.RegistryItemHandler;

/**
 * Created by jabar on 31/10/2016.
 */

public class FeatureEnabling {

    public static void main(String[] a) throws IOException {
        PydioClient c = new PydioClient("http://sandbox.pydio.com/enterprise", "jabar", "security");
        c.serverGeneralRegistry(new RegistryItemHandler() {
            @Override
            protected void onPref(String name, String value) {
                System.out.println(name + " = " + value);
            }
        });
    }
}
