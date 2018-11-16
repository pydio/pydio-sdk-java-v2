package com.pydio.sdk.core.service;

import com.pydio.sdk.core.common.callback.ServerResolver;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

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

    public static String resolve(String url, boolean refresh) throws IOException {
        URI uri = URI.create(url);
        String scheme = uri.getScheme();
        String id = url.substring(scheme.length() + 3).replace("/", "");

        if(!resolvers.containsKey(scheme)){
            throw new IOException("Unable to resolve server " + url);
        }
        return resolvers.get(scheme).resolve(id, refresh);
    }
}
