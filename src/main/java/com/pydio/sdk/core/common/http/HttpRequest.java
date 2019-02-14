package com.pydio.sdk.core.common.http;

import com.pydio.sdk.core.common.callback.ServerResolver;
import com.pydio.sdk.core.utils.Params;

import java.net.CookieManager;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

public class HttpRequest {

    private String endpoint;
    private String userAgent;
    private boolean selfSigned;
    private SSLContext sslContext;
    private HostnameVerifier hostnameVerifier;
    private ServerResolver resolver;
    private String method;
    private Params params;
    private Params headers;
    private ContentBody rawData;
    private CookieManager cookies;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isSelfSigned() {
        return selfSigned;
    }

    public void setSelfSigned(boolean selfSigned) {
        this.selfSigned = selfSigned;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public ServerResolver getResolver() {
        return resolver;
    }

    public void setResolver(ServerResolver resolver) {
        this.resolver = resolver;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public Params getHeaders() {
        return headers;
    }

    public void setHeaders(Params headers) {
        this.headers = headers;
    }

    public ContentBody getContentBody() {
        return rawData;
    }

    public void setContentBody(ContentBody rawData) {
        this.rawData = rawData;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public CookieManager getCookies() {
        return cookies;
    }

    public void setCookies(CookieManager cookies) {
        this.cookies = cookies;
    }
}
