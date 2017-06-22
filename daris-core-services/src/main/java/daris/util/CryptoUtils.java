package daris.util;

import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {

    private final static byte[] salt = new byte[] { -30, -91, -71, 33, 41, 115, -89, 34, 115, 30, -42, -5, 18, -72,
            -106, -30 };
    private final static IvParameterSpec iv = new IvParameterSpec(
            new byte[] { -63, 81, -122, 60, 78, 96, -101, -51, 19, -35, 57, 77, -90, 39, -80, -19 });

    private static byte[] generateKey(String password) throws Throwable {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128); // AES-128
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return f.generateSecret(spec).getEncoded();
    }

    public static byte[] encrypt(byte[] input, byte[] key) throws Throwable {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), iv);
        return cipher.doFinal(input);
    }

    public static byte[] decrypt(byte[] input, byte[] key) throws Throwable {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), iv);
        return cipher.doFinal(input);
    }

    public static String encrypt(String input, String password) throws Throwable {
        return Base64.getEncoder().encodeToString(encrypt(input.getBytes("UTF-8"), generateKey(password)));
    }

    public static String decrypt(String input, String password) throws Throwable {
        return new String(decrypt(Base64.getDecoder().decode(input), generateKey(password)));
    }

}
