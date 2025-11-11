package mrp.service;

import com.sun.net.httpserver.HttpExchange;
import mrp.model.User;
import mrp.util.JsonUtil;
import mrp.database.DatabaseConnection;
import mrp.util.PasswordHasher;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
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


            String insertSql = "INSERT INTO users (id, username, password) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                String userId = UUID.randomUUID().toString();
                String hashedPassword = PasswordHasher.hash(user.getPassword());

                insertStmt.setString(1, userId);
                insertStmt.setString(2, user.getUsername());
                insertStmt.setString(3, hashedPassword);

                int rowsAffected = insertStmt.executeUpdate();

                if (rowsAffected > 0) {
                    sendSuccess(exchange, 201, "User registered successfully");
                } else {
                    sendError(exchange, 400, "Invalid user data");
                }

            } catch (SQLException e) {
                if (e.getSQLState().equals("23505")) {
                    sendError(exchange, 409, "Username already exists");
                }else {
                    sendError(exchange, 400, "Bad request: " + e.getMessage());
                }
            } catch (Exception e) {
                sendError(exchange, 500, "Internal server error");
            }


        } catch (SQLException e) {
            e.printStackTrace();
            sendError(exchange, 500, "Database error: " + e.getMessage()); // change status code
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
                        user.setId(rs.getString("id"));
                        user.setToken(token);
                        user.setPassword("****");
                        sendSuccess(exchange, 200, user); 
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

    private void sendSuccess(HttpExchange exchange, int code, Object data) throws IOException {
        String response = JsonUtil.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("status", String.valueOf(code));

        String response = JsonUtil.toJson(errorResponse);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

}