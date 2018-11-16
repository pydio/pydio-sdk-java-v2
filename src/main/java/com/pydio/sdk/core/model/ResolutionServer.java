package com.pydio.sdk.core.model;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

/**
 * Created by jabar on 27/07/2016.
 */
public class ResolutionServer extends ServerNode {

    public static final String EXPIRATION_DATE = "expiration";

    private String name;
    private String resolvedUrl;
    private String clientID;

    public Properties mVanity;
    public Properties mSupport;
    public Properties[] mEndpoints;
    public ByteArrayOutputStream mImage;

    public ResolutionServer(String id, String resolverName){
        clientID = id;
        name = resolverName;
    }

    @Override
    public String url() {
        return resolvedUrl;
    }

    public boolean urlExpired(){
        String expiration = getProperty("expiration");
        if(expiration == null){}
        return false;
    }

    public String resolverName(){
        return name;
    }

    public String clientID(){
        return clientID;
    }

    public void setResolvedUrl(String url){
        resolvedUrl = url;
    }
}
