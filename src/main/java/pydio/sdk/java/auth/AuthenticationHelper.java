/**
 * 
 */
package pydio.sdk.java.auth;
import java.security.cert.X509Certificate;


/**
 * Interface describing callbacks to provide authentication items
 * @author pydio
 *
 */

public abstract class AuthenticationHelper {
    protected X509Certificate certificate;
    protected String challenge;

    public abstract String[] requestForLoginPassword();

    public void setChallengeResponse(String response){
        challenge = response;
    }
    public String getChallengeResponse(){ return challenge; }

    public X509Certificate getCertificate(){
        return certificate;
    }
    public void setCertificate(X509Certificate cert){
        certificate = cert;
    }

}

