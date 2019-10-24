package com.pydio.sdk.examples;

public class Credentials implements com.pydio.sdk.core.security.Credentials {
    private String login;
    private String password;

    public Credentials(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public String getLogin() {
        return this.login;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getCaptcha() {
        return null;
    }

    @Override
    public String getSeed() {
        return "-1";
    }
}
