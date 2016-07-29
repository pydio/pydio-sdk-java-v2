
package pydio.sdk.java.core.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;


public class CertificateTrustManager implements X509TrustManager {

    CertificateTrust.Helper mHelper;

    public CertificateTrustManager(CertificateTrust.Helper helper) {
        mHelper = helper;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if(mHelper == null || !mHelper.isServerTrusted(chain)){
            throw new CertificateException();
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        if(mHelper != null){
            return mHelper.getAcceptedIssuers();
        }
        return CertificateTrust.revokeAllHelper().getAcceptedIssuers();
    }
}