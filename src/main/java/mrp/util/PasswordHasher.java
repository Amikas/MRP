package mrp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for securely hashing passwords with salt
 * Uses SHA-256 with a random salt to prevent rainbow table attacks
 */
public class PasswordHasher {

    /**
     * Hashes a password with a random salt using SHA-256
     * Combines the salt and hash together for storage
     * @param password The plain text password to hash
     * @return A Base64 encoded string containing both salt and hash
     */
    public static String hash(String password) {
        try {

            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies a password against a stored hash
     * Extracts the salt from the stored hash and compares with the provided password
     * @param password The plain text password to verify
     * @param storedHash The stored hash containing both salt and hashed password
     * @return true if the password matches the stored hash, false otherwise
     */
    public static boolean verify(String password, String storedHash) {
        try {

            byte[] combined = Base64.getDecoder().decode(storedHash);

            byte[] salt = new byte[16];
            byte[] storedPasswordHash = new byte[combined.length - salt.length];
            System.arraycopy(combined, 0, salt, 0, salt.length);
            System.arraycopy(combined, salt.length, storedPasswordHash, 0, storedPasswordHash.length);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            return MessageDigest.isEqual(storedPasswordHash, hashedPassword);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}