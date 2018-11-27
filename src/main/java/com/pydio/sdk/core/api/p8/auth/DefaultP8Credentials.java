package com.pydio.sdk.core.api.p8.auth;

import com.pydio.sdk.core.common.callback.PasswordLoader;
import com.pydio.sdk.core.security.Credentials;

public class DefaultP8Credentials implements Credentials {

    private PasswordLoader loader;
    private String url;
    private String user;
    private String captcha;

    public DefaultP8Credentials(String url, String user, PasswordLoader loader) {
        this.loader = loader;
        this.url = url;
        this.user = user;
    }

    @Override
    public String getLogin() {
        return user;
    }

    @Override
    public String getPassword() {
        return loader.loadPassword(url, user);
    }

    @Override
    public String getCaptcha() {
        return captcha;
    }

    @Override
    public String getSeed() {
        return null;
    }


    public void setCaptcha(String c) {
        this.captcha = c;
    }
}
