package io.pyd.sdk.client.auth;

import java.security.cert.X509Certificate;
import java.util.Scanner;

import org.apache.http.auth.UsernamePasswordCredentials;

public class CommandlineCredentialsProvider implements CredentialsProvider {

	public UsernamePasswordCredentials requestForLoginPassword() {
		System.out.println("\n\nจจจจจจจจจจจจจจAuthenticationจจจจจจจจจจจจจจจจจจ");
		String login , password;
		System.out.print("login    :");
		login = new Scanner(System.in).nextLine();
		System.out.print("password :");
		password = new Scanner(System.in).nextLine();
		System.out.println("จจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจ");
		return new UsernamePasswordCredentials(login, password);
	}

	public X509Certificate requestForCertificate() {
		// TODO Auto-generated method stub
		return null;
	}

}
