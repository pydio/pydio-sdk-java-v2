package com.pydio.sdk.core.model;

import com.google.gson.Gson;
import com.pydio.sdk.core.utils.Log;

public class Token {
    public String subject;
    public String value;
    public long expiry;
    public String refreshToken;

    public static String serialize(Token t) {
        Gson gson = new Gson();
        return gson.toJson(t);
    }

    public boolean isNotValid() {
        if (value == null) {
            return true;
        }

        if ("".equals(value)) {
            return true;
        }

        long currentTimeInSeconds = System.currentTimeMillis();
        long elapsedTimeSinceExpiry = currentTimeInSeconds - this.expiry;

        boolean expired = elapsedTimeSinceExpiry > 0;
        if(expired) {
            Log.i("JWT", String.format("Expired since %s seconds", elapsedTimeSinceExpiry));
        }

        return expired;
    }

    public static Token deserialize(String json){
        return new Gson().fromJson(json, Token.class);
    }

    public interface Store {
        void set(Token t);
    }

    public interface Provider {
        Token get(String subject);
    }
}
