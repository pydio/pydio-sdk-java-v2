package com.pydio.sdk.core.auth.jwt;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;

public class JWT {
    public Header header;
    public Claims claims;
    public String signature;

    public static JWT parse(String strJwt) {
        String[] parts = strJwt.split("\\.");
        if (parts.length != 3) {
            return null;
        }

        Base64 base64 = new Base64();
        byte[] headerBytes = base64.decode(parts[0].getBytes());
        byte[] claimsBytes = base64.decode(parts[1].getBytes());

        JWT jwt = new JWT();
        jwt.claims = new Claims();
        jwt.header = new Header();
        jwt.signature = parts[2];

        jwt.header = new Gson().fromJson(new String(headerBytes), Header.class);
        jwt.claims = new Gson().fromJson(new String(claimsBytes), Claims.class);

        return jwt;
    }
}

