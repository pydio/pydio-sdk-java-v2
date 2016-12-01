package pydio.sdk.java.core.security;

import pydio.sdk.java.core.utils.PasswordLoader;

/**
 * Created by jabar on 03/05/2016.
 */
public class Passwords {
    public static PasswordLoader Loader;

    public static String load(String url, String login){
        if(Loader == null) return null;
        return Loader.loadPassword(url, login);
    }
}
