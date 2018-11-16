package com.pydio.sdk.core.common.callback;

/**
 * Created by jabar on 03/05/2016.
 */
public interface PasswordLoader {
    String loadPassword(String url, String login);
}
