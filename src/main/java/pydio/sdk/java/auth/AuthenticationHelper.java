/**
 * 
 */
package pydio.sdk.java.auth;
import org.apache.http.auth.UsernamePasswordCredentials;
import java.security.cert.X509Certificate;


/**
 * Interface describing callbacks to provide authentication items
 * @author pydio
 *
 */

public abstract class AuthenticationHelper {
    protected X509Certificate certificate;
    protected String challenge;

    public abstract UsernamePasswordCredentials requestForLoginPassword();
    public abstract boolean isTrusted(X509Certificate cert);

    public void setChallengeResponse(String response){
        challenge = response;
    }
    public String getChallengeResponse(){ return challenge; }
    public X509Certificate lastCertificate(){
        return certificate;
    }
    public void setCertificate(X509Certificate cert){
        certificate = cert;
    }

    public static AuthenticationHelper instance = null;
}

