package com.expensesplit.auth;

import com.expensesplit.db.DatabaseManager;
import com.expensesplit.model.User;
import com.expensesplit.util.StringUtils;
import java.util.Map;

public class AuthService {

    public static Map<String, Object> register(String username, String email, String password, String fullName, String phone) {
        if (StringUtils.isNullOrEmpty(username) || StringUtils.isNullOrEmpty(email) ||
            StringUtils.isNullOrEmpty(password) || StringUtils.isNullOrEmpty(fullName)) {
            return Map.of("success", false, "message", "All fields are required");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return Map.of("success", false, "message", "Invalid email format");
        }

        PasswordValidator.ValidationResult validation = PasswordValidator.validate(password);
        if (!validation.isValid()) {
            return Map.of("success", false, "message", validation.getMessage());
        }

        if (DatabaseManager.findUserByUsername(username) != null) {
            return Map.of("success", false, "message", "Username already exists");
        }
        if (DatabaseManager.findUserByEmail(email) != null) {
            return Map.of("success", false, "message", "Email already registered");
        }

        String hashedPassword = PasswordValidator.hashPassword(password);
        int userId = DatabaseManager.createUser(username, email, hashedPassword, fullName, phone);

        if (userId > 0) {
            String token = TokenManager.generateToken(userId);
            return Map.of("success", true, "message", "Registration successful", "token", token, "userId", userId);
        }

        return Map.of("success", false, "message", "Registration failed. Please try again.");
    }

    public static Map<String, Object> login(String username, String password) {
        if (StringUtils.isNullOrEmpty(username) || StringUtils.isNullOrEmpty(password)) {
            return Map.of("success", false, "message", "Username and password are required");
        }

        User user = DatabaseManager.findUserByUsername(username);
        if (user == null) {
            return Map.of("success", false, "message", "Invalid credentials");
        }

        if (!PasswordValidator.verifyPassword(password, user.getPasswordHash())) {
            return Map.of("success", false, "message", "Invalid credentials");
        }

        String token = TokenManager.generateToken(user.getId());
        return Map.of(
            "success", true,
            "message", "Login successful",
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "balance", user.getBalance()
            )
        );
    }

    public static Map<String, Object> logout(String token) {
        TokenManager.invalidateToken(token);
        return Map.of("success", true, "message", "Logged out successfully");
    }
}
