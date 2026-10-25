package com.expensesplit.handler;

import com.sun.net.httpserver.*;
import com.expensesplit.db.DatabaseManager;
import com.expensesplit.util.JsonUtil;
import com.expensesplit.util.RequestParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ExpenseHandler implements HttpHandler {
    private final String action;

    public ExpenseHandler(String action) {
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

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String[] parts = path.split("/");

        try {
            if ("GET".equals(method)) {
                if (parts.length == 3) {
                    List<Map<String, Object>> expenses = DatabaseManager.getUserExpenses(userId);
                    sendJson(exchange, Map.of("success", true, "expenses", expenses));
                } else if (parts.length == 5 && "expenses".equals(parts[3])) {
                    int groupId = Integer.parseInt(parts[4]);
                    List<Map<String, Object>> expenses = DatabaseManager.getGroupExpenses(groupId);
                    sendJson(exchange, Map.of("success", true, "expenses", expenses));
                }
            } else if ("POST".equals(method) && parts.length == 3) {
                Map<String, Object> body = RequestParser.parseJsonBody(exchange);
                Integer groupId = body.containsKey("groupId") && body.get("groupId") != null
                    ? JsonUtil.getInt(body, "groupId", 0) : null;
                String description = JsonUtil.getString(body, "description", "");
                double amount = JsonUtil.getDouble(body, "amount", 0);
                String category = JsonUtil.getString(body, "category", "General");
                String splitType = JsonUtil.getString(body, "splitType", "equal");
                int year = JsonUtil.getInt(body, "year", java.time.Year.now().getValue());
                int month = JsonUtil.getInt(body, "month", java.time.LocalDate.now().getMonthValue());

                if (description.isEmpty() || amount == 0) {
                    sendJson(exchange, Map.of("success", false, "message", "Description and amount required"));
                    return;
                }

                int expenseId = DatabaseManager.createExpense(groupId, userId, description, amount, category, splitType, year, month);

                if (expenseId > 0 && groupId != null) {
                    List<Map<String, Object>> members = DatabaseManager.getGroupMembers(groupId);
                    if (members.size() > 1) {
                        double splitAmount = amount / members.size();
                        int[] userIds = members.stream().mapToInt(m -> (int) m.get("id")).toArray();
                        double[] amounts = new double[userIds.length];
                        Arrays.fill(amounts, Math.round(splitAmount * 100.0) / 100.0);
                        DatabaseManager.createExpenseSplits(expenseId, userIds, amounts);

                        for (Map<String, Object> member : members) {
                            int memberId = (int) member.get("id");
                            if (memberId != userId) {
                                double memberShare = Math.round(splitAmount * 100.0) / 100.0;
                                DatabaseManager.createSettlement(memberId, userId, memberShare, groupId);
                            }
                        }
                    }
                }

                sendJson(exchange, Map.of("success", expenseId > 0, "message", expenseId > 0 ? "Expense created" : "Failed", "expenseId", expenseId));
            } else if ("DELETE".equals(method) && parts.length == 4) {
                int expenseId = Integer.parseInt(parts[3]);
                boolean deleted = DatabaseManager.deleteExpense(expenseId, userId);
                sendJson(exchange, Map.of("success", deleted, "message", deleted ? "Expense deleted" : "Not found or access denied"));
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
