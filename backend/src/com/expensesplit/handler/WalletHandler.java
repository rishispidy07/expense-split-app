package com.expensesplit.handler;

import com.sun.net.httpserver.*;
import com.expensesplit.db.DatabaseManager;
import com.expensesplit.util.JsonUtil;
import com.expensesplit.util.RequestParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WalletHandler implements HttpHandler {

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

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String[] parts = path.split("/");

        try {
            if (parts.length >= 4) {
                int groupId = Integer.parseInt(parts[3]);

                if (!DatabaseManager.isGroupMember(groupId, userId)) {
                    sendJson(exchange, Map.of("success", false, "message", "Access denied"), 403);
                    return;
                }

                if (parts.length == 5) {
                    String action = parts[4];

                    if ("deposit".equals(action) && "POST".equals(method)) {
                        Map<String, Object> body = RequestParser.parseJsonBody(exchange);
                        double amount = JsonUtil.getDouble(body, "amount", 0);
                        if (amount <= 0) {
                            sendJson(exchange, Map.of("success", false, "message", "Amount must be positive"));
                            return;
                        }
                        boolean updated = DatabaseManager.updateGroupWallet(groupId, amount);
                        DatabaseManager.addWalletTransaction(groupId, userId, "deposit", amount, "Wallet deposit");
                        sendJson(exchange, Map.of("success", updated, "message", updated ? "Deposit successful" : "Failed"));
                    } else if ("withdraw".equals(action) && "POST".equals(method)) {
                        Map<String, Object> body = RequestParser.parseJsonBody(exchange);
                        double amount = JsonUtil.getDouble(body, "amount", 0);
                        if (amount <= 0) {
                            sendJson(exchange, Map.of("success", false, "message", "Amount must be positive"));
                            return;
                        }
                        boolean updated = DatabaseManager.updateGroupWallet(groupId, -amount);
                        DatabaseManager.addWalletTransaction(groupId, userId, "withdrawal", amount, "Wallet withdrawal");
                        sendJson(exchange, Map.of("success", updated, "message", updated ? "Withdrawal successful" : "Failed"));
                    } else if ("transactions".equals(action) && "GET".equals(method)) {
                        List<Map<String, Object>> transactions = DatabaseManager.getWalletTransactions(groupId);
                        sendJson(exchange, Map.of("success", true, "transactions", transactions));
                    } else {
                        sendJson(exchange, Map.of("success", false, "message", "Invalid action"), 404);
                    }
                } else {
                    sendJson(exchange, Map.of("success", false, "message", "Endpoint not found"), 404);
                }
            } else {
                sendJson(exchange, Map.of("success", false, "message", "Group ID required"), 400);
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
