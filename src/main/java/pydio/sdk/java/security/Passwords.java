package pydio.sdk.java.security;

import pydio.sdk.java.utils.PasswordLoader;

/**
 * Created by jabar on 03/05/2016.
 */
public class Passwords {
    public static PasswordLoader Loader;

    public static String load(String url, String login){
        if(Loader == null) return null;
        String password = Loader.loadPassword(url, login);
        return password;
    }
}
