package com.expensesplit.handler;

import com.sun.net.httpserver.*;
import com.expensesplit.db.DatabaseManager;
import com.expensesplit.util.JsonUtil;
import com.expensesplit.util.RequestParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PersonalBalanceHandler implements HttpHandler {

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

        String method = exchange.getRequestMethod();
        try {
            if ("POST".equals(method)) {
                Map<String, Object> body = RequestParser.parseJsonBody(exchange);
                String action = JsonUtil.getString(body, "action", "");
                double amount = JsonUtil.getDouble(body, "amount", 0);
                String description = JsonUtil.getString(body, "description", "");

                if (amount <= 0) {
                    sendJson(exchange, Map.of("success", false, "message", "Amount must be positive"));
                    return;
                }

                if ("deposit".equals(action)) {
                    DatabaseManager.updateUserBalance(userId, amount);
                    java.time.LocalDate now = java.time.LocalDate.now();
                    DatabaseManager.createExpense(null, userId, "Balance deposit: " + description, amount, "Income", "equal", now.getYear(), now.getMonthValue());
                    sendJson(exchange, Map.of("success", true, "message", "Deposit successful"));
                } else if ("withdraw".equals(action)) {
                    DatabaseManager.updateUserBalance(userId, -amount);
                    sendJson(exchange, Map.of("success", true, "message", "Withdrawal successful"));
                } else {
                    sendJson(exchange, Map.of("success", false, "message", "Invalid action"));
                }
            } else if ("GET".equals(method)) {
                var user = DatabaseManager.findUserById(userId);
                double balance = user != null ? user.getBalance() : 0;
                sendJson(exchange, Map.of("success", true, "balance", balance));
            } else {
                sendJson(exchange, Map.of("success", false, "message", "Method not allowed"), 405);
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
