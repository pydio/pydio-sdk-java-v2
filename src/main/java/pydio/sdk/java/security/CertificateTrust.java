package pydio.sdk.java.security;

import java.security.cert.X509Certificate;

/**
 * Created by jabar on 29/03/2016.
 */
public class CertificateTrust {
    public interface Helper {
        boolean isServerTrusted(X509Certificate[] chain);
    }
    private static Helper helper;

    public static void setHelper(Helper h){
        helper = h;
    }
    public static void revokeAll(){
        helper = new Helper() {
            @Override
            public boolean isServerTrusted(X509Certificate[] chain) {
                return false;
            }
        };
    }
    public static void acceptAll(){
        helper = new Helper() {
            @Override
            public boolean isServerTrusted(X509Certificate[] chain) {
                return true;
            }
        };
    }
    public static Helper helper(){
        return helper;
    }
}
