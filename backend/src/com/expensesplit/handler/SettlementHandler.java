package com.expensesplit.handler;

import com.sun.net.httpserver.*;
import com.expensesplit.db.DatabaseManager;
import com.expensesplit.util.JsonUtil;
import com.expensesplit.util.RequestParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SettlementHandler implements HttpHandler {

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
            if ("GET".equals(method) && parts.length == 2) {
                List<Map<String, Object>> settlements = DatabaseManager.getUserSettlements(userId);
                sendJson(exchange, Map.of("success", true, "settlements", settlements));
            } else if ("POST".equals(method) && parts.length == 2) {
                Map<String, Object> body = RequestParser.parseJsonBody(exchange);
                int toUserId = JsonUtil.getInt(body, "toUserId", 0);
                double amount = JsonUtil.getDouble(body, "amount", 0);
                Integer groupId = body.containsKey("groupId") && body.get("groupId") != null
                    ? JsonUtil.getInt(body, "groupId", 0) : null;

                if (toUserId <= 0 || amount <= 0) {
                    sendJson(exchange, Map.of("success", false, "message", "Valid recipient and positive amount required"));
                    return;
                }

                int settlementId = DatabaseManager.createSettlement(userId, toUserId, amount, groupId);
                sendJson(exchange, Map.of(
                    "success", settlementId > 0,
                    "message", settlementId > 0 ? "Settlement created" : "Failed",
                    "settlementId", settlementId
                ));
            } else if ("PUT".equals(method) && parts.length == 4 && "complete".equals(parts[3])) {
                int settlementId = Integer.parseInt(parts[2]);
                boolean completed = DatabaseManager.completeSettlement(settlementId);
                sendJson(exchange, Map.of("success", completed, "message", completed ? "Settlement completed" : "Failed"));
            } else {
                sendJson(exchange, Map.of("success", false, "message", "Endpoint not found"), 404);
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
