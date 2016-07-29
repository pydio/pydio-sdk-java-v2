package pydio.sdk.java.core.utils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by pydio on 21/05/2015.
 */
public class CustomCertificateException extends CertificateException {
    public X509Certificate cert;
}
