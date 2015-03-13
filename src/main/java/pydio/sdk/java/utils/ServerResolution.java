package pydio.sdk.java.utils;

import java.util.HashMap;

import pydio.sdk.java.model.ServerNode;

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

    public static void resolve(ServerNode server){
        if(resolvers.containsKey(server.host())){
            resolvers.get(server.host()).resolve(server);
        }
    }
}
