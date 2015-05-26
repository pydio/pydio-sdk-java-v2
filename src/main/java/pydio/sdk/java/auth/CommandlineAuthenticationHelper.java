package pydio.sdk.java.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyException;

import pydio.sdk.java.utils.PydioSecurityManager;

/**
 * A credentials provider that prompt user to get login and password 
 * @author pydio
 *
 */
public class CommandlineAuthenticationHelper extends AuthenticationHelper {
    public PydioSecurityManager w;

    public PydioSecurityManager wallet() throws KeyException {
        if(w == null)
        w = new PydioSecurityManager() {
            @Override
            public InputStream getKeystoreFileInputStream() {
                try {
                    return new FileInputStream(new File("pydio.keystore"));
                } catch (FileNotFoundException e) {
                    return null;
                }
            }

            @Override
            public OutputStream getKeystoreFileOutputStream() {
                try {
                    return new FileOutputStream("pydio.keystore");
                } catch (FileNotFoundException e) {
                    return null;
                }
            }

        };
        return w;
    }

    @Override
    public String[] requestForLoginPassword() {
		/*System.out.println("\n\nXXXXXXXXXXXXXXXXXXXXXX- Authentication -XXXXXXXXXXXXXXXXXXXXXXXXXXX");
		String login , password;
		System.out.print("login    :");
		login = new Scanner(System.in).nextLine();
		System.out.print("password :");
		password = new Scanner(System.in).nextLine();
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");*/
		return new String[]{"jabar", "pydio@2015"};
	}
}
