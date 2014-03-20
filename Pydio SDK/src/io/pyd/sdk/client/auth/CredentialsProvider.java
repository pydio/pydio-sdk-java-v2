/**
 * 
 */
package io.pyd.sdk.client.auth;

import java.security.cert.X509Certificate;
import java.util.Map.Entry;

public interface CredentialsProvider {
	
	public Entry<String, String> requestForLoginPassword();
	public X509Certificate requestForCertificate();
	public String requestForSharedSecret();
	
}

