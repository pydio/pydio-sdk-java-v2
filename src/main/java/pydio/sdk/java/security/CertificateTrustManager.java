
package pydio.sdk.java.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;


public class CertificateTrustManager implements X509TrustManager {

    protected X509Certificate[] mAcceptedIssuers = new X509Certificate[]{};
    public static X509Certificate[] mLastUnverifiedCertificateChain;

    public CertificateTrustManager() {
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        mLastUnverifiedCertificateChain = chain;
        CertificateTrust.Helper helper = CertificateTrust.helper();
        if(helper == null || !helper.isServerTrusted(chain)){
            throw new CertificateException();
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return mAcceptedIssuers;
    }
}