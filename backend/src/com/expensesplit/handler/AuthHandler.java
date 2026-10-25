package com.expensesplit.handler;

import com.sun.net.httpserver.*;
import com.expensesplit.auth.AuthService;
import com.expensesplit.db.DatabaseManager;
import com.expensesplit.util.JsonUtil;
import com.expensesplit.util.RequestParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AuthHandler implements HttpHandler {
    private final String action;

    public AuthHandler(String action) {
        this.action = action;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            Map<String, Object> body = RequestParser.parseJsonBody(exchange);
            Map<String, Object> result;

            switch (action) {
                case "register":
                    result = AuthService.register(
                        JsonUtil.getString(body, "username", ""),
                        JsonUtil.getString(body, "email", ""),
                        JsonUtil.getString(body, "password", ""),
                        JsonUtil.getString(body, "fullName", ""),
                        JsonUtil.getString(body, "phone", "")
                    );
                    break;
                case "login":
                    String username = JsonUtil.getString(body, "username", "");
                    String password = JsonUtil.getString(body, "password", "");

                    // AuthService.login already generates a signed token and user payload.
                    result = AuthService.login(username, password);

                    if (Boolean.TRUE.equals(result.get("success"))) {
                        Object tokenObj = result.get("token");
                        Object userObj = result.get("user");
                        if (tokenObj != null && userObj instanceof Map) {
                            int userId = ((Number) ((Map<?, ?>) userObj).get("id")).intValue();
                            DatabaseManager.saveToken(userId, tokenObj.toString(), 7);
                        }
                    }
                    break;
                case "logout":
                    String token = RequestParser.extractToken(exchange);
                    result = AuthService.logout(token);
                    break;
                default:
                    result = Map.of("success", false, "message", "Invalid action");
            }

            sendJson(exchange, result);
        } catch (Exception e) {
            sendJson(exchange, Map.of("success", false, "message", "Server error: " + e.getMessage()));
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "****";
        int at = email.indexOf('@');
        if (at <= 2) return email.charAt(0) + "***" + email.substring(at);
        return email.substring(0, 2) + "***" + email.substring(at);
    }

    protected void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    protected void sendJson(HttpExchange exchange, Object data) throws IOException {
        String json = JsonUtil.toJsonValue(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
