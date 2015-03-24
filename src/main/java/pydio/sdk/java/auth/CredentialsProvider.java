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

public interface CredentialsProvider {
    public void setAuthenticationChallengeResponse(String key, String response);
    public String getAuthenticationChallengeResponse(String key);
	public UsernamePasswordCredentials requestForLoginPassword();
	public X509Certificate requestForCertificate();
}

