package com.expensesplit.config;

public class Config {
    public static final int SERVER_PORT = 8080;
    public static final String SERVER_HOST = "0.0.0.0";
    public static final String STATIC_FILES_DIR = "frontend";

    public static final String DB_URL = env("EXPENSE_DB_URL",
        "jdbc:mysql://localhost:3306/expense_split_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    public static final String DB_USER = env("EXPENSE_DB_USER", "root");
    public static final String DB_PASSWORD = env("EXPENSE_DB_PASSWORD", "mysql80");
    public static final int DB_POOL_SIZE = 10;

    public static final String JWT_SECRET = env("EXPENSE_JWT_SECRET", "ExpenseSplitApp-SuperSecretKey-2026-Capstone-Project");
    public static final int JWT_EXPIRY_HOURS = 24;

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 64;
    public static final int PASSWORD_ITERATIONS = 100000;
    public static final int PASSWORD_KEY_LENGTH = 256;

    public static final String SMTP_HOST = env("EXPENSE_SMTP_HOST", "smtp.gmail.com");
    public static final int SMTP_PORT = 587;
    public static final String SMTP_USER = env("EXPENSE_SMTP_USER", "rishikeshan013@gmail.com");
    public static final String SMTP_PASS = env("EXPENSE_SMTP_PASS", "");

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isEmpty()) ? fallback : value;
    }
}
