package com.expensesplit.handler;

import com.sun.net.httpserver.*;
import com.expensesplit.db.DatabaseManager;
import com.expensesplit.model.Group;
import com.expensesplit.model.User;
import com.expensesplit.util.JsonUtil;
import com.expensesplit.util.RequestParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GroupHandler implements HttpHandler {

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
            if ("GET".equals(method) && parts.length == 3) {
                List<Map<String, Object>> groups = DatabaseManager.getUserGroups(userId);
                sendJson(exchange, Map.of("success", true, "groups", groups));
            } else if ("POST".equals(method) && parts.length == 3) {
                Map<String, Object> body = RequestParser.parseJsonBody(exchange);
                String name = JsonUtil.getString(body, "name", "");
                String description = JsonUtil.getString(body, "description", "");
                int groupId = DatabaseManager.createGroup(name, description, userId);
                if (groupId > 0) {
                    sendJson(exchange, Map.of("success", true, "message", "Group created", "groupId", groupId));
                } else {
                    sendJson(exchange, Map.of("success", false, "message", "Failed to create group"));
                }
            } else if ("GET".equals(method) && parts.length == 4) {
                int groupId = Integer.parseInt(parts[3]);
                Group group = DatabaseManager.findGroupById(groupId);
                if (group != null && DatabaseManager.isGroupMember(groupId, userId)) {
                    List<Map<String, Object>> members = DatabaseManager.getGroupMembers(groupId);
                    sendJson(exchange, Map.of("success", true, "group", Map.of(
                        "id", group.getId(),
                        "name", group.getName(),
                        "description", group.getDescription(),
                        "walletBalance", group.getWalletBalance(),
                        "createdBy", group.getCreatedBy(),
                        "members", members
                    )));
                } else {
                    sendJson(exchange, Map.of("success", false, "message", "Group not found or access denied"), 404);
                }
            } else if ("PUT".equals(method) && parts.length == 4) {
                int groupId = Integer.parseInt(parts[3]);
                Group group = DatabaseManager.findGroupById(groupId);
                if (group == null) {
                    sendJson(exchange, Map.of("success", false, "message", "Group not found"), 404);
                    return;
                }
                if (group.getCreatedBy() != userId) {
                    sendJson(exchange, Map.of("success", false, "message", "Only the creator can edit this group"), 403);
                    return;
                }
                Map<String, Object> body = RequestParser.parseJsonBody(exchange);
                String name = JsonUtil.getString(body, "name", group.getName());
                String description = JsonUtil.getString(body, "description", group.getDescription());
                if (name.trim().isEmpty()) {
                    sendJson(exchange, Map.of("success", false, "message", "Group name is required"), 400);
                    return;
                }
                boolean updated = DatabaseManager.updateGroup(groupId, name.trim(), description.trim());
                sendJson(exchange, Map.of("success", updated, "message", updated ? "Group updated" : "Failed to update"));
            } else if ("POST".equals(method) && parts.length == 5 && "members".equals(parts[4])) {
                int groupId = Integer.parseInt(parts[3]);
                if (!DatabaseManager.isGroupMember(groupId, userId)) {
                    sendJson(exchange, Map.of("success", false, "message", "Access denied"), 403);
                    return;
                }
                Map<String, Object> body = RequestParser.parseJsonBody(exchange);
                String username = JsonUtil.getString(body, "username", "");
                User member = DatabaseManager.findUserByUsername(username);
                if (member != null) {
                    boolean added = DatabaseManager.addGroupMember(groupId, member.getId(), "member");
                    sendJson(exchange, Map.of("success", added, "message", added ? "Member added" : "Failed to add member"));
                } else {
                    sendJson(exchange, Map.of("success", false, "message", "User not found"));
                }
            } else if ("GET".equals(method) && parts.length == 5 && "members".equals(parts[4])) {
                int groupId = Integer.parseInt(parts[3]);
                List<Map<String, Object>> members = DatabaseManager.getGroupMembers(groupId);
                sendJson(exchange, Map.of("success", true, "members", members));
            } else if ("GET".equals(method) && parts.length == 5 && "balances".equals(parts[4])) {
                int groupId = Integer.parseInt(parts[3]);
                if (!DatabaseManager.isGroupMember(groupId, userId)) {
                    sendJson(exchange, Map.of("success", false, "message", "Access denied"), 403);
                    return;
                }
                List<Map<String, Object>> balances = DatabaseManager.getGroupBalances(groupId);
                sendJson(exchange, Map.of("success", true, "balances", balances));
            } else if ("GET".equals(method) && parts.length == 5 && "splits".equals(parts[4])) {
                int groupId = Integer.parseInt(parts[3]);
                if (!DatabaseManager.isGroupMember(groupId, userId)) {
                    sendJson(exchange, Map.of("success", false, "message", "Access denied"), 403);
                    return;
                }
                Map<String, Object> splits = DatabaseManager.getGroupSplits(groupId);
                sendJson(exchange, Map.of("success", true, "splits", splits));
            } else if ("DELETE".equals(method) && parts.length == 6 && "members".equals(parts[4])) {
                int groupId = Integer.parseInt(parts[3]);
                int memberId = Integer.parseInt(parts[5]);
                boolean removed = DatabaseManager.removeGroupMember(groupId, memberId);
                sendJson(exchange, Map.of("success", removed, "message", removed ? "Member removed" : "Failed"));
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
