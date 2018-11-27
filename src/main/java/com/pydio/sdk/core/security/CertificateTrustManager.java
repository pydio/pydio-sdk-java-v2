
package com.pydio.sdk.core.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;


public class CertificateTrustManager implements X509TrustManager {

    private CertificateTrust.Helper helper;

    public CertificateTrustManager(CertificateTrust.Helper helper) {
        this.helper = helper;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) {

    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (helper == null || !helper.isServerTrusted(chain)) {
            throw new CertificateException();
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        if (helper != null) {
            return helper.getAcceptedIssuers();
        }
        return CertificateTrust.revokeAllHelper().getAcceptedIssuers();
    }
}