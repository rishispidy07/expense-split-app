package com.expensesplit.server;

import com.sun.net.httpserver.*;
import com.expensesplit.config.Config;
import com.expensesplit.handler.*;
import com.expensesplit.util.RequestParser;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class AppServer {
    private com.sun.net.httpserver.HttpServer server;
    private final Map<String, Map<String, HttpHandler>> routes = new LinkedHashMap<>();

    public void start() throws IOException {
        server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(Config.SERVER_HOST, Config.SERVER_PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(50));

        registerRoutes();
        server.start();

        System.out.println("========================================");
        System.out.println("  ExpenseSplit App - Capstone Project");
        System.out.println("========================================");
        System.out.println("  Server running on: http://" + getLocalIp() + ":" + Config.SERVER_PORT);
        System.out.println("  Local access:      http://localhost:" + Config.SERVER_PORT);
        System.out.println("  Bound to:          " + Config.SERVER_HOST + " (all interfaces)");
        System.out.println("========================================");
        System.out.println("  API Endpoints:");
        System.out.println("    POST   /api/auth/register");
        System.out.println("    POST   /api/auth/login");
        System.out.println("    POST   /api/auth/logout");
        System.out.println("    GET    /api/users/me");
        System.out.println("    GET    /api/users");
        System.out.println("    POST   /api/groups");
        System.out.println("    GET    /api/groups");
        System.out.println("    GET    /api/groups/:id");
        System.out.println("    POST   /api/groups/:id/members");
        System.out.println("    GET    /api/groups/:id/members");
        System.out.println("    DELETE /api/groups/:id/members/:uid");
        System.out.println("    POST   /api/expenses");
        System.out.println("    GET    /api/expenses");
        System.out.println("    GET    /api/groups/:id/expenses");
        System.out.println("    POST   /api/settlements");
        System.out.println("    PUT    /api/settlements/:id/complete");
        System.out.println("    GET    /api/settlements");
        System.out.println("    POST   /api/wallet/:groupId/deposit");
        System.out.println("    POST   /api/wallet/:groupId/withdraw");
        System.out.println("    GET    /api/wallet/:groupId/transactions");
        System.out.println("========================================");
    }

    private void registerRoutes() {
        // Auth
        server.createContext("/api/auth/register", new AuthHandler("register"));
        server.createContext("/api/auth/login", new AuthHandler("login"));
        server.createContext("/api/auth/logout", new AuthHandler("logout"));

        // Users
        server.createContext("/api/users/me", new UserHandler("me"));
        server.createContext("/api/users", new UserHandler("list"));

        // Groups
        server.createContext("/api/groups", new GroupHandler());

        // Expenses
        server.createContext("/api/expenses", new ExpenseHandler("list"));

        // Settlements
        server.createContext("/api/settlements", new SettlementHandler());

        // Wallet
        server.createContext("/api/wallet", new WalletHandler());

        // Personal Balance
        server.createContext("/api/balance", new PersonalBalanceHandler());

        // OTP
        server.createContext("/api/otp", new OtpHandler());

        // Stats (charts, reports, profile)
        server.createContext("/api/stats/category", new StatsHandler());
        server.createContext("/api/stats/monthly", new StatsHandler());
        server.createContext("/api/stats/summary", new StatsHandler());
        server.createContext("/api/stats/groups", new StatsHandler());
        server.createContext("/api/stats/daily", new StatsHandler());
        server.createContext("/api/user/profile", new StatsHandler());

        // Static files
        server.createContext("/", new StaticFileHandler());
    }

    private String getLocalIp() {
        try {
            var interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                var networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;
                var addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    var address = addresses.nextElement();
                    if (address instanceof java.net.Inet4Address) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {}
        return "0.0.0.0";
    }

    public void stop() {
        if (server != null) server.stop(0);
    }
}
