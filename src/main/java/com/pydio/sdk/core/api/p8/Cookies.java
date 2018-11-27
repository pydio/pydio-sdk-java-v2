package com.pydio.sdk.core.api.p8;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

public class Cookies {

    private static CookieManager cookies;

    private static CookieManager getCookies() {
        if (cookies == null) {
            cookies = new CookieManager();
        }
        return cookies;
    }

    public static List<HttpCookie> getCookies(String url) {
        return getCookies().getCookieStore().get(URI.create(url));
    }

    public static void set(String url, HttpCookie c) {
        URI uri = URI.create(url);
        getCookies().getCookieStore().add(uri, c);
    }
}
