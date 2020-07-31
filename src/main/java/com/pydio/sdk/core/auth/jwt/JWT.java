package com.pydio.sdk.core.auth.jwt;

import com.google.gson.Gson;
import com.pydio.sdk.core.encoding.B64;

import java.io.UnsupportedEncodingException;

public class JWT {
    public Header header;
    public Claims claims;
    public String signature;

    public static JWT parse(String strJwt) {
        System.out.println(strJwt);

        String[] parts = strJwt.split("\\.");
        if (parts.length != 3) {
            return null;
        }

        JWT jwt = new JWT();
        jwt.claims = new Claims();
        jwt.header = new Header();
        jwt.signature = parts[2];

        String headerStr = B64.get().decode(parts[0]);
        String claimsStr = B64.get().decode(parts[1]);

        jwt.header = new Gson().fromJson(headerStr, Header.class);
        jwt.claims = new Gson().fromJson(claimsStr, Claims.class);

        return jwt;
    }
}

