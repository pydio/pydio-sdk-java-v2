package pydio.sdk.java.examples;

import java.io.IOException;

/**
 * Created by jabar on 05/04/2016.
 */
public class SelfSignedCertificate {
    public static void main(String[] args) throws IOException{

        /*PydioClient client = new PydioClient("https://192.168.0.91", "pydio", "pydiopassword");
        final X509Certificate[] acceptedCertificate = new X509Certificate[1];

        CertificateTrust.setHelper(new CertificateTrust.Helper(){
            @Override
            public boolean isServerTrusted(X509Certificate[] chain) {
                boolean match = chain[0].equals(acceptedCertificate[0]);
                System.out.println("Certificate trust : " + String.valueOf(match));
                return match;
            }
        });

        while(true) {
            try {
                client.ls("1", "/", new NodeHandler() {
                    @Override
                    public void onNode(Node node) {
                        System.out.printf(node.label());
                    }
                });
                return;

            } catch (IOException e) {
                int status = client.responseStatus();
                if(status == Pydio.ERROR_UNVERIFIED_CERTIFICATE){
                    X509Certificate cert = CertificateTrustManager.mLastUnverifiedCertificateChain[0];
                    String thumbPrint = "none";

                    try {
                        thumbPrint = Crypto.hexHash(Crypto.HASH_SHA1, cert.getEncoded());
                    } catch (NoSuchAlgorithmException e1) {
                        e1.printStackTrace();
                    } catch (CertificateEncodingException e1) {
                        e1.printStackTrace();
                    }

                    System.out.println("Server certificate thumbprint : " + thumbPrint);
                    System.out.print("Type \"Y\" to trust or another key to revoke : ");
                    String line = new Scanner(System.in).nextLine().trim();

                    if("y".equalsIgnoreCase(line)){
                        acceptedCertificate[0] = cert;
                        System.out.println("Certificated trusted. New attempt");
                        continue;
                    } else {
                        System.out.println("You revoked the server certificate");
                        return;
                    }

                } else{
                    e.printStackTrace();
                    System.out.println("Unable to verify the server certificate");
                    return;
                }
            }
        }*/
    }
}
