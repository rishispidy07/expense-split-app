package com.expensesplit.auth;

import com.expensesplit.config.Config;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class TokenManager {

    private static final String SECRET = Config.JWT_SECRET;
    private static final Map<String, Integer> tokenStore = new ConcurrentHashMap<>();

    public static String generateToken(int userId) {
        try {
            String payload = userId + ":" + System.currentTimeMillis() + ":" + (System.currentTimeMillis() + Config.JWT_EXPIRY_HOURS * 3600000L);
            String signature = sign(payload);

            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(
                (payload + "." + signature).getBytes(StandardCharsets.UTF_8)
            );

            tokenStore.put(token, userId);
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    public static int validateToken(String token) {
        if (token == null || token.isEmpty()) return -1;

        Integer cachedUserId = tokenStore.get(token);
        if (cachedUserId != null) return cachedUserId;

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\.");
            if (parts.length != 2) return -1;

            String payload = parts[0];
            String signature = parts[1];

            String expectedSignature = sign(payload);
            if (!MessageDigest.isEqual(signature.getBytes(), expectedSignature.getBytes())) return -1;

            String[] payloadParts = payload.split(":");
            if (payloadParts.length != 3) return -1;

            int userId = Integer.parseInt(payloadParts[0]);
            long expiresAt = Long.parseLong(payloadParts[2]);

            if (System.currentTimeMillis() > expiresAt) return -1;

            tokenStore.put(token, userId);
            return userId;
        } catch (Exception e) {
            return -1;
        }
    }

    public static void invalidateToken(String token) {
        if (token != null) tokenStore.remove(token);
    }

    private static String sign(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }
}
