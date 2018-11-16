package com.pydio.sdk.core.security;

import java.io.Serializable;
import java.security.cert.X509Certificate;

/**
 * Created by jabar on 29/03/2016.
 */
public class CertificateTrust {

    public interface Helper extends Serializable {
        boolean isServerTrusted(X509Certificate[] chain);
        X509Certificate[] getAcceptedIssuers();
    }


    public static Helper revokeAllHelper(){
        return new Helper() {
            @Override
            public boolean isServerTrusted(X509Certificate[] chain) {
                return false;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    public static Helper acceptAllHelper(){
        return new Helper() {
            @Override
            public boolean isServerTrusted(X509Certificate[] chain) {
                return true;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

}
