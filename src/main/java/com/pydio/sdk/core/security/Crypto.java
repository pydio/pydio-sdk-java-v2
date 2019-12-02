package com.pydio.sdk.core.security;

import com.pydio.sdk.core.common.codec.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        return Hex.toString(hash(algo, bytes));
    }

    /*public static String aesDerivedKeyDecrypt(String keyPassword, String salt, byte[] data){
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
            String enc_password = new String(Base64.getEncoder().encodeToString(cipher.doFinal(data)));
            return  enc_password;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

}
