package com.ahaviss.utilities;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.nio.ByteBuffer;
public class SecurityUtils {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 16;
    private static final int ITERATIONS = 65000;
    private static final int KEY_LENGTH = 256;
    public static SecretKey getAESKeyFromPassword(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        spec.clearPassword();
        return secret;
    }
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
        byte[] cipherText = cipher.doFinal(plainText.getBytes());

        // 5. Combine [IV] + [Salt] + [CipherText] into one package
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + salt.length + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(salt);
        byteBuffer.put(cipherText);

        // 6. Return as a Base64 string so it can be saved in a text file
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }
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
        return new String(plainText);
    }
    public static byte[] generateSalt() {
        //Allocate memory for salt
        byte[] salt = new byte[SALT_LENGTH_BYTE];
        //Generate and return salt
        new SecureRandom().nextBytes(salt);
        return salt;
    }
    public static String hashPassword(String password, byte[] salt) {
        try {
            // Get an instance of the SHA-256 algorithm
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            // Convert the password string into bytes and hash it
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            //Put it into one byte buffer
            ByteBuffer byteBuffer = ByteBuffer.allocate(encodedHash.length + salt.length);
            byteBuffer.put(salt);
            byteBuffer.put(encodedHash);
            // Convert the byte array into a Base64 string so it can be stored as text
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: Hashing algorithm not found!");
        }
    }
    public static boolean verifyPassword(String inputPassword, String storedBase64) {
        //Decode base 64
        byte[] decoded = Base64.getDecoder().decode(storedBase64);
        ByteBuffer buffer = ByteBuffer.wrap(decoded);

        // 1. Pull the original salt out of the front
        byte[] salt = new byte[SALT_LENGTH_BYTE];
        buffer.get(salt);

        // 2. Hash the user's input using that EXACT salt
        String attempt = hashPassword(inputPassword, salt);

        // 3. Compare the two Base64 strings
        return attempt.equals(storedBase64);
    }
}
