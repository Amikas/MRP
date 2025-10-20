package mrp.service;

import com.sun.net.httpserver.HttpExchange;
import mrp.model.User;
import mrp.util.JsonUtil;
import mrp.database.DatabaseConnection;
import mrp.util.PasswordHasher;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.UUID;

public class UserService {
    private DatabaseConnection dbConnection = new DatabaseConnection();

    public void register(HttpExchange exchange) throws IOException {
        Connection conn = null;

        try {
            // Parse request body to User object
            String body = new String(exchange.getRequestBody().readAllBytes());
            User user = JsonUtil.fromJson(body, User.class);

            // Validate input
            if (user.getUsername() == null || user.getUsername().trim().isEmpty() ||
                    user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                sendError(exchange, 400, "Username and password are required");
                return;
            }

            conn = dbConnection.getConnection();

            // Check if username already exists
            String checkSql = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, user.getUsername());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    sendError(exchange, 409, "Username already exists");
                    return;
                }
            }

            // Insert new user WITH PASSWORD HASHING
            String insertSql = "INSERT INTO users (id, username, password) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                String userId = UUID.randomUUID().toString();
                String hashedPassword = PasswordHasher.hash(user.getPassword()); // Hash the password

                insertStmt.setString(1, userId);
                insertStmt.setString(2, user.getUsername());
                insertStmt.setString(3, hashedPassword); // Store the hashed password

                int rowsAffected = insertStmt.executeUpdate();

                if (rowsAffected > 0) {
                    String response = "User registered successfully";
                    sendSuccess(exchange, 201, response);
                } else {
                    sendError(exchange, 500, "Failed to register user");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void login(HttpExchange exchange) throws IOException {
        Connection conn = null;

        try {
            String body = new String(exchange.getRequestBody().readAllBytes());
            User user = JsonUtil.fromJson(body, User.class);

            // Validate input
            if (user.getUsername() == null || user.getPassword() == null) {
                sendError(exchange, 400, "Username and password are required");
                return;
            }

            conn = dbConnection.getConnection();

            // Get stored hash from database
            String sql = "SELECT id, password FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getUsername());

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String providedPassword = user.getPassword();

                    // Verify the password against the stored hash
                    if (PasswordHasher.verify(providedPassword, storedHash)) {
                        // Valid credentials - generate token
                        String token = user.getUsername() + "-mrpToken";
                        sendSuccess(exchange, 200, token);
                    } else {
                        sendError(exchange, 401, "Invalid username or password");
                    }
                } else {
                    sendError(exchange, 401, "Invalid username or password");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendSuccess(HttpExchange exchange, int code, String message) throws IOException {
        exchange.sendResponseHeaders(code, message.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(message.getBytes());
        }
    }

    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        exchange.sendResponseHeaders(code, message.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(message.getBytes());
        }
    }
}