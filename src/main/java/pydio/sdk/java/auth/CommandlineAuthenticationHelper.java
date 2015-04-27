package pydio.sdk.java.auth;

import org.apache.http.auth.UsernamePasswordCredentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyException;
import java.security.KeyStoreException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.Scanner;

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
            public String getMasterPassword() {
                System.out.print("XXXXXXXX MASTER PASSWORD : ");
                return  new Scanner(System.in).nextLine();
            }

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

            protected void saveConfigs(Properties p){
                try {
                    p.storeToXML(new FileOutputStream("configs.xml"), "Keystore password generation parameters");
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
            }

            protected Properties loadConfigs(){
                try {
                    Properties p = new Properties();
                    p.loadFromXML(new FileInputStream(new File("configs.xml")));
                    return p;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        return w;
    }

    @Override
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
    @Override
    public boolean isTrusted(X509Certificate cert) {
        certificate = cert;
        try {
            if(w.checkCertificate(cert.getSubjectDN().toString(), cert)){
                certificate = null;
                return true;
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }
}
