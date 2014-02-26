/**
 * 
 */
package io.pyd.sdk.client.auth;

public interface CredentialsProvider {
	
	//public void requestForCertificate(onCertificateProvidedListener listener);

	public void requestCredentials(onCredentialsProvidedListener listener);
	
	
	interface onCredentialsProvidedListener{
		public void onCredentialProvided(String user, String password);
	}
	
	//interface onCertificateProvidedListener{
		//public vois onCertificateProvided(Certificate cert);
	//}
}

