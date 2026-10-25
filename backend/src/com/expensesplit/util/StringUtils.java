package com.expensesplit.util;

import java.util.regex.Pattern;

public class StringUtils {
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String sanitize(String s) {
        if (s == null) return "";
        return s.replaceAll("[<>\"'&]", "").trim();
    }

    public static String toLowerCase(String s) {
        return s == null ? "" : s.toLowerCase().trim();
    }
}
