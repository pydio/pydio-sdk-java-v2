package com.pydio.sdk.core.auth;

import com.google.gson.Gson;
import com.pydio.sdk.core.utils.Log;

import org.json.JSONObject;

import java.text.ParseException;

public class Token {

    public String subject;
    public String value;
    public long expiry;
    public String refreshToken;
    public String idToken;
    public String scope;
    public String tokenType;

    private long currentTimeInSeconds() {
        return System.currentTimeMillis() / 1000 ;
    }

    public boolean isExpired() {
        if (value == null) {
            return true;
        }
        if ("".equals(value)) {
            return true;
        }
        long elapsedTimeSinceExpiry = this.currentTimeInSeconds() - this.expiry;
        boolean expired = elapsedTimeSinceExpiry > 0;
        if(expired) {
            Log.i("JWT", String.format("Expired since %s seconds", elapsedTimeSinceExpiry));
        }
        return expired;
    }

    public static String encode(Token t) {
        Gson gson = new Gson();
        return gson.toJson(t);
    }

    public static Token decode(String json) {
        return new Gson().fromJson(json, Token.class);
    }

    public static Token decodeOauthJWT(String jwt) throws ParseException {
        Token t = new Token();
        JSONObject jo = new JSONObject(jwt);

        t.value = jo.getString("access_token");
        t.expiry = jo.getInt("expires_in");
        t.idToken = jo.getString("id_token");
        t.refreshToken = jo.getString("refresh_token");
        t.scope = jo.getString("scope");
        t.tokenType = jo.getString("token_type");
        return t;
    }

    public interface Store {
        void set(Token t);
    }

    public interface Provider {
        Token get(String subject);
    }
}
