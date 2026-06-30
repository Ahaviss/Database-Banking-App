/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.utilities;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.nio.ByteBuffer;
/**
 * Utility class providing secure cryptographic operations including AES-256-GCM
 * encryption/decryption and Argon2id password hashing.
 * @author Ahaviss
 */
public class SecurityUtils {
    private static final int ARGON2_ITERATIONS  = 3;
    private static final int ARGON2_MEMORY      = 131072;
    private static final int ARGON2_PARALLELISM = 4;
    private static final int ARGON2_HASH_LENGTH = 32;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 16;
    private static final int ITERATIONS = 650000;
    private static final int KEY_LENGTH = 256;
    /** 
     * Derives an AES key with a PBKDF2 algorithm from a password.
     * 
     * @param password the password in array format from which the AES key will be derived from
     * @param salt the salt which guarantees a unique AES key every method call
     * @return an AES key derived from a password and the PBKDF2 algorithm
     * @throws Exception for any cryptography errors
     * @see #encrypt(String, String) 
     * @see #decrypt(String, String) 
     * */
    public static SecretKey getAESKeyFromPassword(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        spec.clearPassword();
        return secret;
    }
    /**
     * Encrypts plaintext using AES-256-GCM with a PBKDF2-derived key.
     * Returns a Base64 string containing: [iv][salt][ciphertext+tag].
     * To be decrypted by {@link #decrypt(String, String)}
     * 
     * @param plainText  The string to encrypt
     * @param password   The password to derive the key from {@code char[]}  — zero after use
     * @return           Base64-encoded packed string
     * @throws Exception If any cryptographic operation fails
     */
    public static String encrypt(String plainText, String password) throws Exception {
        // 1. Generate random IV and Salt (different every single time you save!)
        byte[] iv = new byte[IV_LENGTH_BYTE];
        byte[] salt = new byte[SALT_LENGTH_BYTE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        random.nextBytes(salt);

        // 2. Derive the 256-bit key using the password and the new salt
        SecretKey aesKey = getAESKeyFromPassword(password.toCharArray(), salt);

        // 3. Initialize the Cipher for encryption
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);

        // 4. Encrypt the data
        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 5. Combine [IV] + [Salt] + [CipherText] into one package
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + salt.length + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(salt);
        byteBuffer.put(cipherText);
        Arrays.fill(aesKey.getEncoded(), (byte) 0);
        // 6. Return as a Base64 string so it can be saved in a text file
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }
    /**
     * Decrypts a Base-64 encoded String produced by {@link #encrypt(String, String)}.
     *
     * @param base64Package is the ciphertext packaged with a salt, IV (Initialization Vector), and ciphertext
     * @param password is the key inputted by user, and an AES key is derived from it using PBKDF2
     * @return A plain String
     * @throws AEADBadTagException if password is incorrect, or data is corrupted or tampered with
     * @throws Exception if any other operation fails
     * */
    public static String decrypt(String base64Package, String password) throws Exception {
        // 1. Decode the Base64 back into raw bytes
        byte[] decoded = Base64.getDecoder().decode(base64Package);
        ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

        // 2. Slice the IV and Salt off the front of the package
        byte[] iv = new byte[IV_LENGTH_BYTE];
        byteBuffer.get(iv);

        byte[] salt = new byte[SALT_LENGTH_BYTE];
        byteBuffer.get(salt);

        // 3. The remaining bytes are the actual encrypted message
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        // 4. Rebuild the exact same key using the password and the Salt we just found
        SecretKey aesKey = getAESKeyFromPassword(password.toCharArray(), salt);

        // 5. Initialize the Cipher for decryption
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);

        // 6. Decrypt. This will throw an exception if the password is wrong OR if the data was edited.
        byte[] plainText = cipher.doFinal(cipherText);
        Arrays.fill(aesKey.getEncoded(), (byte) 0);
        return new String(plainText, StandardCharsets.UTF_8);
    }
    private static byte[] generateSalt() {
        //Allocate memory for salt
        byte[] salt = new byte[SALT_LENGTH_BYTE];
        //Generate and return salt
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Hashes a String using Argon2id from BouncyCastle and packages hashed with salt.
     * Can be compared with a plaintext string using {@link #verifyPassword(String, String)}
     * 
     * @param password the string to hash securely
     * @return a Base-64 encoded string which is hashed securely and packaged with salt
     * @see #verifyPassword(String, String) 
     */
    public static String hashPassword(String password) {
        byte[] salt = generateSalt();
        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withSalt(salt)
                .withIterations(ARGON2_ITERATIONS)
                .withMemoryAsKB(ARGON2_MEMORY)
                .withParallelism(ARGON2_PARALLELISM)
                .build();
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);
        byte[] hash = new byte[ARGON2_HASH_LENGTH];
        generator.generateBytes(password.toCharArray(), hash);
        String saltBase64 = Base64.getEncoder().withoutPadding().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().withoutPadding().encodeToString(hash);
        return String.format(
                "$argon2id$v=19$m=%d,t=%d,p=%d$%s$%s",
                ARGON2_MEMORY, ARGON2_ITERATIONS, ARGON2_PARALLELISM, saltBase64, hashBase64
        );
    }
    /** 
     * Verifies that an inputted password is the same as a stored hash hashed by {@link #hashPassword(String)}.
     * 
     * @param inputPassword the inputted password which will be compared to the hash
     * @param storedHash the stored hash inside a data source will be compared to the inputted password
     * @return a boolean verifying if the two inputs are the same (after hashing input password) true = same, false = different
     * @see #hashPassword(String) 
     * */
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            int version = Integer.parseInt(parts[2].split("=")[1]);
            String[] paramParts = parts[3].split(",");
            int memory = Integer.parseInt(paramParts[0].split("=")[1]);
            int iterations = Integer.parseInt(paramParts[1].split("=")[1]);
            int parallelism = Integer.parseInt(paramParts[2].split("=")[1]);
            byte[] salt = Base64.getDecoder().decode(parts[4]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[5]);
            Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                    .withVersion(version == 19 ? Argon2Parameters.ARGON2_VERSION_13 : Argon2Parameters.ARGON2_VERSION_10)
                    .withSalt(salt)
                    .withIterations(iterations)
                    .withMemoryAsKB(memory)
                    .withParallelism(parallelism)
                    .build();
            Argon2BytesGenerator generator = new Argon2BytesGenerator();
            generator.init(params);
            byte[] actualHash = new byte[ARGON2_HASH_LENGTH];
            generator.generateBytes(inputPassword.toCharArray(), actualHash);
            return MessageDigest.isEqual(expectedHash, actualHash);
        }
        catch (Exception e) {
            return false;
        }
    }
}
