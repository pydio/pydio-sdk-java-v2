package com.pydio.sdk.core.auth;

import org.json.JSONObject;

public class OauthConfig {
    public String state;
    public String baseURL;
    public String clientID;
    public String audience;
    public String clientSecret;
    public String redirectURI;
    public String authorizeEndpoint;
    public String tokenEndpoint;
    public String revokeEndpoint;
    public String scope;
    public String code;
    public String jwksUriEndpoint;
    public String refreshEndpoint;


    public static OauthConfig fromServer(JSONObject o) {
        OauthConfig cfg = new OauthConfig();
        cfg.clientID = "cells-mobile";
        cfg.clientSecret = "";
        cfg.redirectURI = "cellsauth://callback";
        cfg.tokenEndpoint = o.getString("token_endpoint");
        cfg.authorizeEndpoint = o.getString("authorization_endpoint");
        cfg.revokeEndpoint = o.getString("revocation_endpoint");
        return cfg;
    }
}
