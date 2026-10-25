package com.expensesplit.auth;

import com.expensesplit.config.Config;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class PasswordValidator {

    public static ValidationResult validate(String password) {
        if (password == null) return new ValidationResult(false, "Password is required");
        if (password.length() < Config.MIN_PASSWORD_LENGTH)
            return new ValidationResult(false, "Password must be at least " + Config.MIN_PASSWORD_LENGTH + " characters");
        if (password.length() > Config.MAX_PASSWORD_LENGTH)
            return new ValidationResult(false, "Password must be under " + Config.MAX_PASSWORD_LENGTH + " characters");

        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        if (!hasUpper) return new ValidationResult(false, "Password must contain at least one uppercase letter");
        if (!hasLower) return new ValidationResult(false, "Password must contain at least one lowercase letter");
        if (!hasDigit) return new ValidationResult(false, "Password must contain at least one digit");
        if (!hasSpecial) return new ValidationResult(false, "Password must contain at least one special character (!@#$%^&*)");

        String commonPasswords = "password,12345678,qwerty,abc123,admin,letmein,monkey,dragon,login,princess,";
        if (commonPasswords.contains(password.toLowerCase() + ","))
            return new ValidationResult(false, "This password is too common. Choose a stronger one.");

        boolean hasRepeating = false;
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && password.charAt(i) == password.charAt(i + 2)) {
                hasRepeating = true;
                break;
            }
        }
        if (hasRepeating)
            return new ValidationResult(false, "Password must not contain 3+ repeating characters");

        return new ValidationResult(true, "Password meets all requirements");
    }

    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, Config.PASSWORD_ITERATIONS, Config.PASSWORD_KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Config.PASSWORD_ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public static boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, expectedHash.length * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] actualHash = factory.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return false;
        }
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}
