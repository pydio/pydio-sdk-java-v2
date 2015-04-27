package pydio.sdk.java.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by pydio on 01/04/2015.
 */
public abstract class PydioSecurityManager {
    public static String ENCRYPTED_PASSWORD_PREFIX = "enc:";

    private final int KEY_SIZE = 128;
    private final int PBKDF2_ITERATION_NUMBER = 20000;

    KeyStore ks = null;
    private String KEYSTORE_PASSWORD = "pydioks";
    Properties p;

    public PydioSecurityManager(){
        FileInputStream in = null;
        try {
            if(ks == null){
                ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(null, getKeystorePassword().toCharArray());
                this.load();
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCertificate(String alias, X509Certificate cert) throws KeyStoreException{
        if(ks == null) return;
        ks.setCertificateEntry(alias, cert);
        this.store();
    }

    public boolean checkCertificate(String alias, X509Certificate cert) throws KeyStoreException, CertificateEncodingException {
        if(ks == null) return false;
        try {
            cert.checkValidity();
        } catch (CertificateExpiredException e) {
            e.printStackTrace();
            return false;
        } catch (CertificateNotYetValidException e) {
            e.printStackTrace();
            return false;
        }

        X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);
        if(certificate == null) return false;
        try {
            MessageDigest hash = MessageDigest.getInstance("MD5");
            byte[] c1 = hash.digest(certificate.getEncoded());
            byte[] c2 = hash.digest(cert.getEncoded());
            return Arrays.equals(c1, c2);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return Arrays.equals(cert.getEncoded(), certificate.getEncoded());
        }
    }

    String getKeystorePassword(){
        return KEYSTORE_PASSWORD;
    }

    private void store(){
        try {
            ks.store(getKeystoreFileOutputStream(), getKeystorePassword().toCharArray());
        } catch (IOException e) {
        } catch (CertificateException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (KeyStoreException e) {
        }
    }

    private void load(){
        try {
            InputStream in = getKeystoreFileInputStream();
            ks.load(in, getKeystorePassword().toCharArray());
        } catch (IOException e) {
        } catch (CertificateException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }

    private Properties getProperties(){
        if(p == null){
            p = loadConfigs();
            if(p == null){
                p = new Properties();
            }
        }
        return p;
    }

    public String loadPassword(String masterPassword, String alias){
        String pass = getProperties().getProperty(alias, "");
        if(masterPassword == null || masterPassword.length() == 0 || pass.startsWith(ENCRYPTED_PASSWORD_PREFIX)){
            return pass;
        }
        byte[] encryptedPass = Base64.decodeBase64(pass.substring(pass.indexOf(ENCRYPTED_PASSWORD_PREFIX)));
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), alias.getBytes(), PBKDF2_ITERATION_NUMBER, KEY_SIZE);
            SecretKey key = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(key.getEncoded());
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            return new String(cipher.doFinal(encryptedPass));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean storePassword(String masterPassword, String alias, String password){

        if(masterPassword == null || masterPassword.length() == 0 ){
            getProperties().setProperty(alias, password);
        }else {
            byte[] bytes = password.getBytes();
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), alias.getBytes(), PBKDF2_ITERATION_NUMBER, KEY_SIZE);
                SecretKey key = factory.generateSecret(spec);
                SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                IvParameterSpec ivParameterSpec = new IvParameterSpec(key.getEncoded());
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
                String enc_password = new String(Base64.encodeBase64(cipher.doFinal(bytes)));
                getProperties().setProperty(alias, ENCRYPTED_PASSWORD_PREFIX+enc_password);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        saveConfigs(p);
        return true;
    }

    public Object[] getPasswordAliases(){
        Object[] keys = getProperties().keySet().toArray();
        String[] stringKeys = new String[keys.length];
        for(int i = 0; i < keys.length; i++){
            stringKeys[i] = (String) keys[i];
        }
        return stringKeys;
    }

    public String createMasterPasswordChecksum(String masterPassword){
        String rand = new BigInteger(130, new Random()).toString(32);
        int count = 0;
        for (int i = 0, len = rand.length(); i < len; i++) {
            if (Character.isDigit(rand.charAt(i))) {
                count++;
            }
        }
        String checksum = count + "pydio" + rand;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), "Pydio".getBytes(), PBKDF2_ITERATION_NUMBER, KEY_SIZE);
            SecretKey key = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(key.getEncoded());
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            String enc_checksum = new String(Base64.encodeBase64(cipher.doFinal(checksum.getBytes())));
            getProperties().setProperty("MASTER_PASSWORD_CHECKSUM", enc_checksum);
            return enc_checksum;
        } catch (Exception e) {}
        return null;
    }

    public boolean checkMasterPassword(String masterPassword, String b64Checksum){
        byte[] enc_checksum = Base64.decodeBase64(b64Checksum);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), "Pydio".getBytes(), PBKDF2_ITERATION_NUMBER, KEY_SIZE);
            SecretKey key = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(key.getEncoded());
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            String clearChecksum = new String(cipher.doFinal(enc_checksum));
            if(!clearChecksum.contains("pydio")){
                return false;
            }
            String first = clearChecksum.substring(0, clearChecksum.indexOf("pydio"));
            String last =  clearChecksum.substring(clearChecksum.indexOf("pydio") + 5);
            int count = 0;
            for (int i = 0, len = last.length(); i < len; i++) {
                if (Character.isDigit(last.charAt(i))) {
                    count++;
                }
            }
            return first.equals(""+count);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected abstract void saveConfigs(Properties p);
    protected abstract Properties loadConfigs();
    public abstract InputStream getKeystoreFileInputStream();
    public abstract OutputStream getKeystoreFileOutputStream();
}
