package com.voting.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Salted SHA-256 password hashing.
 *
 * This is NOT bcrypt/argon2 - those aren't in the plain JDK - but salting
 * every password individually means two identical passwords never produce
 * the same hash, and a stolen database can't be cracked with a plain
 * rainbow table. Good enough for a college project; swap in a proper
 * password-hashing library for production use.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /** Generates a new random 16-byte salt, Base64 encoded. */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** Hashes plainText together with the given salt. */
    public static String hash(String plainText, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashed = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static boolean verify(String plainText, String salt, String expectedHash) {
        if (plainText == null || salt == null || expectedHash == null) return false;
        return hash(plainText, salt).equals(expectedHash);
    }

    /**
     * Minimum bar for a new password: at least 8 characters, at least one
     * letter and one digit. Feel free to tighten this.
     */
    public static boolean isStrong(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasDigit = false;
        boolean hasLetter = false;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            if (Character.isLetter(c)) hasLetter = true;
        }
        return hasDigit && hasLetter;
    }
}
