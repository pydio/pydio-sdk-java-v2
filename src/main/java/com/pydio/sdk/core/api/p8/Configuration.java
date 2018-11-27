package com.pydio.sdk.core.api.p8;

import com.pydio.sdk.core.common.callback.ServerResolver;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

public class Configuration {
    public String endpoint;
    public String userAgent;
    public boolean selfSigned;
    public SSLContext sslContext;
    public HostnameVerifier hostnameVerifier;
    public ServerResolver resolver;
}
