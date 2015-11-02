package pydio.sdk.java.auth;

import java.security.KeyException;
import java.security.cert.X509Certificate;

import pydio.sdk.java.utils.PydioSecureStore;

/**
 * A credentials provider that prompt user to get login and password 
 * @author pydio
 *
 */
public class CommandlineAuthenticationHelper extends AuthenticationHelper {
    public PydioSecureStore w;

    public PydioSecureStore wallet() throws KeyException {
        if(w == null)
        w = new PydioSecureStore() {

            @Override
            public void addCertificate(String alias, X509Certificate cert) throws Exception {

            }

            @Override
            public boolean checkCertificate(String alias, X509Certificate cert) throws Exception {
                return false;
            }
        };
        return w;
    }

    @Override
    public String[] requestForLoginPassword() {
		return new String[]{"jabar", "pydio@2015"};
	}
}
