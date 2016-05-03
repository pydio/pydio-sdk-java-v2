package pydio.sdk.java.security;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by jabar on 29/03/2016.
 */
public class Crypto {

    public static final String HASH_MD5 = "MD5";
    public static final String HASH_SHA1 = "SHA-1";

    private static final String AES_ENCRYPTION_TYPE = "AES";
    private static final String DERIVATION_KEY_ALGO = "PBKDF2WithHmacSHA1";
    private static final String AES_CBC_PADDING = "AES/CBC/PKCS5Padding";

    private static final int DERIVED_KEY_SIZE = 128;
    private static final int DERIVATION_ITERATION = 20000;

    public static byte[] hash(final String algo,  byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algo);
        md.update(bytes);
        return md.digest();
    }
    public static String hexHash(final String algo, byte[] bytes) throws NoSuchAlgorithmException {
        return hexify(hash(algo, bytes));
    }

    public static String aesDerivedKeyDecrypt(String keyPassword, String salt, byte[] data){
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(DERIVATION_KEY_ALGO);
            KeySpec spec = new PBEKeySpec(keyPassword.toCharArray(), salt.getBytes(), DERIVATION_ITERATION, DERIVED_KEY_SIZE);
            SecretKey key = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), AES_ENCRYPTION_TYPE);

            Cipher cipher = Cipher.getInstance(AES_CBC_PADDING);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(key.getEncoded());
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            return new String(cipher.doFinal(data));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String aesDerivedKeyEncrypt(String keyPassword, String salt, byte[] data){
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(DERIVATION_KEY_ALGO);
            KeySpec spec = new PBEKeySpec(keyPassword.toCharArray(), salt.getBytes(), DERIVATION_ITERATION, DERIVED_KEY_SIZE);
            SecretKey key = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), AES_ENCRYPTION_TYPE);

            Cipher cipher = Cipher.getInstance(AES_CBC_PADDING);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(key.getEncoded());
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            String enc_password = new String(Base64.encode(cipher.doFinal(data)));
            return  enc_password;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String hexify(byte bytes[]) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuffer buf = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; ++i) {
            buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }
        return buf.toString();
    }
}
