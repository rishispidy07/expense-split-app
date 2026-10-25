package com.expensesplit.handler;

import com.sun.net.httpserver.*;
import com.expensesplit.config.Config;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class StaticFileHandler implements HttpHandler {

    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put(".html", "text/html");
        MIME_TYPES.put(".css", "text/css");
        MIME_TYPES.put(".js", "application/javascript");
        MIME_TYPES.put(".json", "application/json");
        MIME_TYPES.put(".png", "image/png");
        MIME_TYPES.put(".jpg", "image/jpeg");
        MIME_TYPES.put(".gif", "image/gif");
        MIME_TYPES.put(".svg", "image/svg+xml");
        MIME_TYPES.put(".ico", "image/x-icon");
        MIME_TYPES.put(".woff", "font/woff");
        MIME_TYPES.put(".woff2", "font/woff2");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";

        Path filePath = Paths.get(Config.STATIC_FILES_DIR, path);

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            // SPA fallback - serve index.html for unknown routes
            filePath = Paths.get(Config.STATIC_FILES_DIR, "index.html");
            if (!Files.exists(filePath)) {
                String notFound = "<html><body><h1>404 Not Found</h1></body></html>";
                byte[] bytes = notFound.getBytes();
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(404, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.getResponseBody().close();
                return;
            }
        }

        // Prevent directory traversal
        String canonicalPath = filePath.toRealPath().toString();
        String canonicalBase = Paths.get(Config.STATIC_FILES_DIR).toRealPath().toString();
        if (!canonicalPath.startsWith(canonicalBase)) {
            exchange.sendResponseHeaders(403, -1);
            exchange.getResponseBody().close();
            return;
        }

        byte[] fileBytes = Files.readAllBytes(filePath);
        String extension = "";
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx > 0) extension = path.substring(dotIdx);

        String contentType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, fileBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(fileBytes);
        }
    }
}
