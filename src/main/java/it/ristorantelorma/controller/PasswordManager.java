package it.ristorantelorma.controller;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Handles password hashing and comparison.
 */
public final class PasswordManager {

    private static final String CLASS_NAME = PasswordManager.class.getName();
    private static final Logger LOGGER = SimpleLogger.getLogger(CLASS_NAME);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LENGTH = 18; // when base64-encoded: 24 chars
    private static final int KEY_LENGTH = 600; // when base64-encoded: 100 chars

    private PasswordManager() {
        throw new UnsupportedOperationException("Utility class and cannot be instantiated");
    }

    /**
     * Generate a 18byte-long random salt.
     *
     * @return the generated salt
     */
    public static byte[] generateSalt() {
        final byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Hash and salt the given password, using PBKDF2-HMAC-SHA256.
     *
     * @param password
     * @param salt random bytes
     * @return the hashed password
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static byte[] hashPassword(final String password, final byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65_535, KEY_LENGTH);
        final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }

    /**
     * Hash and salt the given password, return Base64-encoded salt:hash.
     *
     * 18byte salt, 600bit key, both Base64-encoded: the returned "salt:hash" is long 125 chars
     *
     * @param password
     * @param salt
     * @return base64 encoded salt:hash, null if an error is encountered
     */
    @CheckForNull
    public static String encodePassword(final String password, final byte[] salt) {
        final Encoder encoder = Base64.getEncoder();
        try {
            final byte[] hash = hashPassword(password, salt);
            return encoder.encodeToString(salt) + ":" + encoder.encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.log(Level.SEVERE, "Error while hashing password", e);
            return null;
        }
    }

    /**
     * Hash and salt the given password, generating internaly the salt.
     *
     * @see encodePassword()
     * @param password
     * @return base64 encoded salt:hash, null if an error is encountered
     */
    @CheckForNull
    public static String newEncodedPassword(final String password) {
        final byte[] salt = generateSalt();
        return encodePassword(password, salt);
    }

    /**
     * Compare the given password with the given hashed password. The salt will be extracted from
     * hashedPassword.
     *
     * @param password
     * @param hashedPassword
     * @return true if the password match, false otherwise
     * @throws IllegalArgumentException if hashedPassword is invalid
     * @throws IllegalStateException if an exception is found while hasing password
     */
    public static boolean checkPassword(final String password, final String hashedPassword) {
        final String[] slice = hashedPassword.split(":");
        if (slice.length != 2) {
            throw new IllegalArgumentException(
                    "hashedPassword is invalid (not in the form salt:hash)");
        }
        if (slice[0].length() != SALT_LENGTH * 4 / 3 || slice[1].length() != KEY_LENGTH / 8 * 4 / 3) {
            throw new IllegalArgumentException(
                    "hashedPassword: hash or salt have an invalid length");
        }
        final byte[] salt = Base64.getDecoder().decode(slice[0]);
        final String newHash = encodePassword(password, salt);
        if (newHash == null) {
            throw new IllegalStateException("Error while hashing password");
        }
        return newHash.equals(hashedPassword);
    }
}
