package pydio.sdk.java.core.utils;

import java.io.IOException;
import java.util.HashMap;

import pydio.sdk.java.core.model.ResolutionServer;

/**
 * Created by pydio on 12/03/2015.
 */

public class ServerResolution {

    private static HashMap<String, ServerResolver> resolvers = new HashMap<String, ServerResolver>();

    public static void register(String name, ServerResolver resolver){
        resolvers.put(name, resolver);
    }

    public static void unregister(String name){
        resolvers.remove(name);
    }

    public static void resolve(ResolutionServer server) throws IOException {
        if(resolvers.containsKey(server.host())){
            resolvers.get(server.host()).resolve(server);
        }
    }
}
