package com.expensesplit;

import com.expensesplit.db.DatabaseManager;
import com.expensesplit.server.AppServer;

public class App {
    public static void main(String[] args) {
        System.out.println("[App] Initializing ExpenseSplit Application...");

        try {
            DatabaseManager.initialize();
            System.out.println("[App] Database connected successfully");
        } catch (Exception e) {
            System.err.println("[App] WARNING: Could not connect to database. Running in limited mode.");
            System.err.println("[App] Make sure MySQL is running and schema is imported.");
            System.err.println("[App] Run: mysql -u root -p < sql/schema.sql");
        }

        try {
            AppServer server = new AppServer();
            server.start();
        } catch (Exception e) {
            System.err.println("[App] Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
