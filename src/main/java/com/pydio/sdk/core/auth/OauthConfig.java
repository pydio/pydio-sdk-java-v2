package com.pydio.sdk.core.auth;

import com.pydio.sdk.core.common.codec.Hex;

import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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


    public static OauthConfig fromJSON(JSONObject o, String scope) {
        OauthConfig cfg = new OauthConfig();
        cfg.clientID = "cells-mobile";
        cfg.clientSecret = "";
        cfg.redirectURI = "cellsauth://callback";
        cfg.tokenEndpoint = o.getString("token_endpoint");
        cfg.authorizeEndpoint = o.getString("authorization_endpoint");
        cfg.revokeEndpoint = o.getString("revocation_endpoint");
        if ("".equals(scope)) {
            cfg.scope = "openid email offline profile pydio";
        } else {
            cfg.scope = scope;
        }


        byte[] bytes = new byte[8];
        new SecureRandom().nextBytes(bytes);
        cfg.state = Hex.toString(bytes);
        return cfg;
    }
}
