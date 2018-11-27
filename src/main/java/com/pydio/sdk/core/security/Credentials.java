package com.pydio.sdk.core.security;

public interface Credentials {

    String getLogin();

    String getPassword();

    String getCaptcha();

    String getSeed();
}
