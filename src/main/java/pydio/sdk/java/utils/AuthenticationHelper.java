/**
 * 
 */
package pydio.sdk.java.utils;

/**
 * Interface describing callbacks to provide authentication items
 * @author pydio
 *
 */

public abstract class AuthenticationHelper {
    protected String challenge;
    public abstract String[] getCredentials();
    public void setChallengeResponse(String response){
        challenge = response;
    }
    public String getChallengeResponse(){
        return challenge;
    }
}

