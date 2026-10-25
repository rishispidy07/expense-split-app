package com.expensesplit.handler;

import com.sun.net.httpserver.*;
import com.expensesplit.db.DatabaseManager;
import com.expensesplit.util.JsonUtil;
import com.expensesplit.util.RequestParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OtpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.contains("/otp/send") && "POST".equals(method)) {
                handleSendOtp(exchange);
            } else if (path.contains("/otp/verify") && "POST".equals(method)) {
                handleVerifyOtp(exchange);
            } else {
                sendJson(exchange, Map.of("success", false, "message", "Not found"), 404);
            }
        } catch (Exception e) {
            sendJson(exchange, Map.of("success", false, "message", "Server error: " + e.getMessage()));
        }
    }

    private void handleSendOtp(HttpExchange exchange) throws IOException {
        Map<String, Object> body = RequestParser.parseJsonBody(exchange);
        String username = JsonUtil.getString(body, "username", "");

        if (username.isEmpty()) {
            sendJson(exchange, Map.of("success", false, "message", "Username required"));
            return;
        }

        var user = DatabaseManager.findUserByUsername(username);
        if (user == null) {
            sendJson(exchange, Map.of("success", false, "message", "User not found"));
            return;
        }

        String otp = generateOtp();
        DatabaseManager.saveOtp(user.getEmail(), otp, 300);

        String email = user.getEmail();
        String maskedEmail = maskEmail(email);

        boolean sent = EmailService.sendOtpEmail(email, otp, user.getFullName());

        sendJson(exchange, Map.of(
            "success", true,
            "message", "OTP sent to " + maskedEmail,
            "maskedEmail", maskedEmail,
            "emailSent", sent
        ));
    }

    private void handleVerifyOtp(HttpExchange exchange) throws IOException {
        Map<String, Object> body = RequestParser.parseJsonBody(exchange);
        String username = JsonUtil.getString(body, "username", "");
        String otpCode = JsonUtil.getString(body, "otp", "");

        if (username.isEmpty() || otpCode.isEmpty()) {
            sendJson(exchange, Map.of("success", false, "message", "Username and OTP required"));
            return;
        }

        var user = DatabaseManager.findUserByUsername(username);
        if (user == null) {
            sendJson(exchange, Map.of("success", false, "message", "User not found"));
            return;
        }

        boolean valid = DatabaseManager.verifyOtp(user.getEmail(), otpCode);
        if (!valid) {
            sendJson(exchange, Map.of("success", false, "message", "Invalid or expired OTP"));
            return;
        }

        String token = com.expensesplit.auth.TokenManager.generateToken(user.getId());
        DatabaseManager.saveToken(user.getId(), token, 7);

        sendJson(exchange, Map.of(
            "success", true,
            "message", "OTP verified",
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "balance", user.getBalance()
            )
        ));
    }

    private String generateOtp() {
        Random rand = new Random();
        int num = rand.nextInt(900000) + 100000;
        return String.valueOf(num);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "****";
        int at = email.indexOf('@');
        if (at <= 2) return email.charAt(0) + "***" + email.substring(at);
        return email.substring(0, 2) + "***" + email.substring(at);
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private void sendJson(HttpExchange exchange, Object data) throws IOException {
        sendJson(exchange, data, 200);
    }

    private void sendJson(HttpExchange exchange, Object data, int status) throws IOException {
        String json = JsonUtil.toJsonValue(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
