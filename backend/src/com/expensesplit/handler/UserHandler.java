package com.expensesplit.handler;

import com.sun.net.httpserver.*;
import com.expensesplit.db.DatabaseManager;
import com.expensesplit.model.User;
import com.expensesplit.util.JsonUtil;
import com.expensesplit.util.RequestParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UserHandler implements HttpHandler {
    private final String action;

    public UserHandler(String action) {
        this.action = action;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        int userId = RequestParser.extractUserId(exchange);
        if (userId == -1) {
            sendJson(exchange, Map.of("success", false, "message", "Unauthorized"), 401);
            return;
        }

        try {
            switch (action) {
                case "me":
                    User user = DatabaseManager.findUserById(userId);
                    if (user != null) {
                        sendJson(exchange, Map.of("success", true, "user", Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "fullName", user.getFullName(),
                            "phone", user.getPhone() != null ? user.getPhone() : "",
                            "balance", user.getBalance()
                        )));
                    } else {
                        sendJson(exchange, Map.of("success", false, "message", "User not found"), 404);
                    }
                    break;
                case "list":
                    List<User> users = DatabaseManager.getAllUsers();
                    List<Map<String, Object>> userList = new ArrayList<>();
                    for (User u : users) {
                        userList.add(Map.of(
                            "id", u.getId(),
                            "username", u.getUsername(),
                            "fullName", u.getFullName(),
                            "email", u.getEmail()
                        ));
                    }
                    sendJson(exchange, Map.of("success", true, "users", userList));
                    break;
                default:
                    sendJson(exchange, Map.of("success", false, "message", "Invalid action"));
            }
        } catch (Exception e) {
            sendJson(exchange, Map.of("success", false, "message", "Server error: " + e.getMessage()));
        }
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
