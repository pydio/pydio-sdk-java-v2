package com.pydio.sdk.core.security;

public class Credentials {
    private String user;
    private String password;
    private String captcha;

    public static Credentials basic(String login, String password) {
        Credentials c = new Credentials();
        c.user = login;
        c.password = password;
        return c;
    }

    public static Credentials withCaptcha(String login, String password, String captcha) {
        Credentials c = new Credentials();
        c.user = login;
        c.password = password;
        c.captcha = captcha;
        return c;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public boolean captchaSet() {
        return captcha != null & captcha.length() > 0;
    }
}
