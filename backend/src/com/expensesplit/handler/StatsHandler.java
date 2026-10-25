package com.expensesplit.handler;

import com.sun.net.httpserver.*;
import com.expensesplit.db.DatabaseManager;
import com.expensesplit.util.JsonUtil;
import com.expensesplit.util.RequestParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StatsHandler implements HttpHandler {
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
        try {
            if (path.contains("/stats/category")) {
                String query = exchange.getRequestURI().getQuery();
                int year = getParam(query, "year", java.time.Year.now().getValue());
                int month = getParam(query, "month", java.time.LocalDate.now().getMonthValue());
                List<Map<String, Object>> data = DatabaseManager.getCategorySummary(userId, year, month);
                sendJson(exchange, Map.of("success", true, "categories", data));
            } else if (path.contains("/stats/monthly")) {
                String query = exchange.getRequestURI().getQuery();
                int year = getParam(query, "year", java.time.Year.now().getValue());
                List<Map<String, Object>> data = DatabaseManager.getMonthlySummary(userId, year);
                sendJson(exchange, Map.of("success", true, "months", data));
            } else if (path.contains("/stats/summary")) {
                String query = exchange.getRequestURI().getQuery();
                int year = getParam(query, "year", java.time.Year.now().getValue());
                int month = getParam(query, "month", java.time.LocalDate.now().getMonthValue());
                Map<String, Object> data = DatabaseManager.getMonthlyTotals(userId, year, month);
                sendJson(exchange, Map.of("success", true, "summary", data));
            } else if (path.contains("/stats/groups")) {
                String query = exchange.getRequestURI().getQuery();
                int year = getParam(query, "year", java.time.Year.now().getValue());
                int month = getParam(query, "month", 0);
                List<Map<String, Object>> data = DatabaseManager.getGroupSummary(userId, year, month);
                sendJson(exchange, Map.of("success", true, "groups", data));
            } else if (path.contains("/stats/daily")) {
                String query = exchange.getRequestURI().getQuery();
                int year = getParam(query, "year", java.time.Year.now().getValue());
                int month = getParam(query, "month", java.time.LocalDate.now().getMonthValue());
                List<Map<String, Object>> data = DatabaseManager.getDailySummary(userId, year, month);
                sendJson(exchange, Map.of("success", true, "daily", data));
            } else if (path.contains("/user/profile")) {
                if ("PUT".equals(exchange.getRequestMethod())) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Map<String, Object> data = JsonUtil.parse(body);
                    String username = data.get("username") != null ? data.get("username").toString().trim() : "";
                    String fullName = data.get("fullName") != null ? data.get("fullName").toString().trim() : "";
                    String email = data.get("email") != null ? data.get("email").toString().trim() : "";
                    String phone = data.get("phone") != null ? data.get("phone").toString().trim() : "";
                    if (username.isEmpty()) {
                        sendJson(exchange, Map.of("success", false, "error", "Username is required"), 400);
                        return;
                    }
                    if (fullName.isEmpty()) {
                        sendJson(exchange, Map.of("success", false, "error", "Full name is required"), 400);
                        return;
                    }
                    if (email.isEmpty() || !email.contains("@")) {
                        sendJson(exchange, Map.of("success", false, "error", "Valid email is required"), 400);
                        return;
                    }
                    boolean updated = DatabaseManager.updateUserProfile(userId, username, fullName, email, phone);
                    if (updated) {
                        sendJson(exchange, Map.of("success", true, "message", "Profile updated"));
                    } else {
                        sendJson(exchange, Map.of("success", false, "error", "Username or email already exists"), 400);
                    }
                } else {
                    Map<String, Object> profile = DatabaseManager.getUserProfile(userId);
                    double income = DatabaseManager.getUserIncome(userId);
                    double spent = DatabaseManager.getUserTotalSpent(userId);
                    profile.put("totalIncome", income);
                    profile.put("totalSpent", spent);
                    sendJson(exchange, Map.of("success", true, "profile", profile));
                }
            } else {
                sendJson(exchange, Map.of("success", false, "message", "Not found"), 404);
            }
        } catch (Exception e) {
            sendJson(exchange, Map.of("success", false, "message", "Server error: " + e.getMessage()));
        }
    }

    private int getParam(String query, String key, int def) {
        if (query == null) return def;
        for (String p : query.split("&")) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals(key)) {
                try { return Integer.parseInt(kv[1]); } catch (Exception ignored) {}
            }
        }
        return def;
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
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
