package com.jongsoft.finance.security;

import com.jongsoft.finance.configuration.SecuritySettings;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public class Encryption {

    private final static String ALGORITHM_NAME = "AES/GCM/NoPadding";
    private final static int ALGORITHM_NONCE_SIZE = 12;
    private final static int ALGORITHM_TAG_SIZE = 128;
    private final static int ALGORITHM_KEY_SIZE = 128;
    private final static String PBKDF2_NAME = "PBKDF2WithHmacSHA256";
    private final static int PBKDF2_SALT_SIZE = 16;
    private final static int PBKDF2_ITERATIONS = 32767;

    public Encryption(SecuritySettings securitySettings) {
    }

    public byte[] encrypt(byte[] data, String password) {
        SecureRandom rand = new SecureRandom();
        byte[] securitySalt = new byte[PBKDF2_SALT_SIZE];
        rand.nextBytes(securitySalt);

        try {
            // Create an instance of PBKDF2 and derive a key.
            var pwSpec = new PBEKeySpec(password.toCharArray(), securitySalt, PBKDF2_ITERATIONS, ALGORITHM_KEY_SIZE);
            var keyFactory = SecretKeyFactory.getInstance(PBKDF2_NAME);
            var key = keyFactory.generateSecret(pwSpec).getEncoded();

            // Encrypt and prepend salt.
            var cipherTextAndNonce = encrypt(data, key);
            var cipherTextAndNonceAndSalt = new byte[securitySalt.length + cipherTextAndNonce.length];
            System.arraycopy(
                    securitySalt,
                    0,
                    cipherTextAndNonceAndSalt,
                    0,
                    securitySalt.length);
            System.arraycopy(
                    cipherTextAndNonce,
                    0,
                    cipherTextAndNonceAndSalt,
                    securitySalt.length,
                    cipherTextAndNonce.length);

            // Return as base64 string.
            return Base64.getEncoder().encode(cipherTextAndNonceAndSalt);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to encrypt data", e);
        }
    }

    public byte[] decrypt(byte[] base64CiphertextAndNonceAndSalt, String password) {
        try {
            // Decode the base64.
            var cipherTextAndNonceAndSalt = Base64.getDecoder().decode(base64CiphertextAndNonceAndSalt);

            // Retrieve the salt and cipherTextAndNonce.
            var salt = new byte[PBKDF2_SALT_SIZE];
            var cipherTextAndNonce = new byte[cipherTextAndNonceAndSalt.length - PBKDF2_SALT_SIZE];
            System.arraycopy(
                    cipherTextAndNonceAndSalt,
                    0,
                    salt,
                    0,
                    salt.length);
            System.arraycopy(
                    cipherTextAndNonceAndSalt,
                    salt.length,
                    cipherTextAndNonce,
                    0,
                    cipherTextAndNonce.length);

            // Create an instance of PBKDF2 and derive the key.
            PBEKeySpec pwSpec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    PBKDF2_ITERATIONS,
                    ALGORITHM_KEY_SIZE);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF2_NAME);
            byte[] key = keyFactory.generateSecret(pwSpec).getEncoded();

            // Decrypt and return result.
            return decrypt(cipherTextAndNonce, key);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to decrypt data", e);
        }
    }

    private byte[] encrypt(byte[] plaintext, byte[] key) throws GeneralSecurityException {
        // Generate a 96-bit nonce using a CSPRNG.
        var rand = new SecureRandom();
        var nonce = new byte[ALGORITHM_NONCE_SIZE];
        rand.nextBytes(nonce);

        // Create the cipher instance and initialize.
        var cipher = Cipher.getInstance(ALGORITHM_NAME);
        cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(key, "AES"),
                new GCMParameterSpec(ALGORITHM_TAG_SIZE, nonce));

        // Encrypt and prepend nonce.
        var cipherText = cipher.doFinal(plaintext);
        var cipherTextAndNonce = new byte[nonce.length + cipherText.length];
        System.arraycopy(nonce, 0, cipherTextAndNonce, 0, nonce.length);
        System.arraycopy(cipherText, 0, cipherTextAndNonce, nonce.length, cipherText.length);

        return cipherTextAndNonce;
    }

    private byte[] decrypt(byte[] cipherTextAndNonce, byte[] key) throws GeneralSecurityException {
        var nonce = new byte[ALGORITHM_NONCE_SIZE];
        var ciphertext = new byte[cipherTextAndNonce.length - ALGORITHM_NONCE_SIZE];
        System.arraycopy(
                cipherTextAndNonce,
                0,
                nonce,
                0,
                nonce.length);
        System.arraycopy(
                cipherTextAndNonce,
                nonce.length,
                ciphertext,
                0,
                ciphertext.length);

        // Create the cipher instance and initialize.
        var cipher = Cipher.getInstance(ALGORITHM_NAME);
        cipher.init(
                Cipher.DECRYPT_MODE,
                new SecretKeySpec(key, "AES"),
                new GCMParameterSpec(ALGORITHM_TAG_SIZE, nonce));

        // Decrypt and return result.
        return cipher.doFinal(ciphertext);
    }
}
