package pydio.sdk.java.auth;

import org.apache.http.auth.UsernamePasswordCredentials;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * A credentials provider that prompt user to get login and password 
 * @author pydio
 *
 */
public class CommandlineCredentialsProvider implements CredentialsProvider {
    Map<String, String> p = new HashMap<String , String>();
    @Override
    public void setAuthenticationChallengeResponse(String key, String response) {
        p.put(key, response);
        //challengeResponse = response;
    }
    @Override
    public String getAuthenticationChallengeResponse(String key) {
        return p.get(key);
    }
    public UsernamePasswordCredentials requestForLoginPassword() {
		System.out.println("\n\nXXXXXXXXXXXXXXXXXXXXXX- Authentication -XXXXXXXXXXXXXXXXXXXXXXXXXXX");
		String login , password;
		System.out.print("login    :");
		login = new Scanner(System.in).nextLine();
		System.out.print("password :");
		password = new Scanner(System.in).nextLine();
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		return new UsernamePasswordCredentials(login, password);
	}
	public X509Certificate requestForCertificate() {
		// TODO Auto-generated method stub
		return null;
	}

}
