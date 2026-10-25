package com.expensesplit.handler;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import com.expensesplit.config.Config;

public class EmailService {
    private static final String SMTP_HOST = Config.SMTP_HOST;
    private static final int SMTP_PORT = Config.SMTP_PORT;
    private static final String SMTP_USER = Config.SMTP_USER;
    private static final String SMTP_PASS = Config.SMTP_PASS;

    public static boolean sendOtpEmail(String toEmail, String otp, String fullName) {
        if (SMTP_USER.isEmpty() || SMTP_PASS.isEmpty()) {
            printOtpToConsole(toEmail, otp, fullName);
            return false;
        }

        Socket socket = null;
        try {
            socket = new Socket(SMTP_HOST, SMTP_PORT);
            socket.setSoTimeout(10000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            readResponse(reader);
            sendCommand(writer, "EHLO splitease", reader);

            // Gmail requires STARTTLS; after the server accepts it we MUST perform a
            // real TLS handshake before sending credentials, otherwise AUTH is refused.
            sendCommand(writer, "STARTTLS", reader);
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket = (SSLSocket) factory.createSocket(socket, SMTP_HOST, SMTP_PORT, true);
            sslSocket.startHandshake();
            socket = sslSocket;

            reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream(), StandardCharsets.UTF_8));

            sendCommand(writer, "EHLO splitease", reader);
            sendCommand(writer, "AUTH LOGIN", reader);
            sendCommand(writer, Base64.getEncoder().encodeToString(SMTP_USER.getBytes(StandardCharsets.UTF_8)), reader);
            sendCommand(writer, Base64.getEncoder().encodeToString(SMTP_PASS.getBytes(StandardCharsets.UTF_8)), reader);
            sendCommand(writer, "MAIL FROM:<" + SMTP_USER + ">", reader);
            sendCommand(writer, "RCPT TO:<" + toEmail + ">", reader);
            sendCommand(writer, "DATA", reader);

            String subject = "SplitEase - Your OTP Code";
            String body = "Hi " + fullName + ",\n\n"
                + "Your One-Time Password (OTP) for SplitEase login is:\n\n"
                + "         " + otp + "\n\n"
                + "This OTP expires in 5 minutes.\n"
                + "If you didn't request this, please ignore.\n\n"
                + "Thanks,\nSplitEase Team";

            writer.write("From: SplitEase <" + SMTP_USER + ">\r\n");
            writer.write("To: " + toEmail + "\r\n");
            writer.write("Subject: " + subject + "\r\n");
            writer.write("\r\n");
            writer.write(body.replace("\n", "\r\n") + "\r\n");
            writer.write(".\r\n");
            writer.flush();
            readResponse(reader);

            sendCommand(writer, "QUIT", reader);
            return true;
        } catch (Exception e) {
            System.out.println("[EmailService] SMTP failed: " + e.getMessage());
            printOtpToConsole(toEmail, otp, fullName);
            return false;
        } finally {
            if (socket != null) {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }
    }

    private static void printOtpToConsole(String toEmail, String otp, String fullName) {
        System.out.println("========================================");
        System.out.println("  OTP for " + toEmail + ": " + otp);
        System.out.println("  User: " + fullName);
        System.out.println("========================================");
    }

    private static String readResponse(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
            if (line.length() >= 4 && line.charAt(3) == ' ') break;
        }
        return sb.toString();
    }

    private static void sendCommand(BufferedWriter writer, String cmd, BufferedReader reader) throws IOException {
        writer.write(cmd + "\r\n");
        writer.flush();
        readResponse(reader);
    }
}
