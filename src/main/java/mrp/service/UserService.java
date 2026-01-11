package mrp.service;

import com.sun.net.httpserver.HttpExchange;
import mrp.model.User;
import mrp.util.JsonUtil;
import mrp.repository.UserRepository;
import mrp.util.PasswordHasher;
import mrp.util.TokenUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserService {
    private UserRepository userRepository;

    public UserService() {
        try {
            this.userRepository = new UserRepository();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize UserRepository", e);
        }
    }

    public void register(HttpExchange exchange) throws IOException {
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

            // Check if username already exists
            if (userRepository.usernameExists(user.getUsername())) {
                sendError(exchange, 409, "Username already exists");
                return;
            }

            // Create new user
            User newUser = new User();
            newUser.setId(UUID.randomUUID().toString());
            newUser.setUsername(user.getUsername());
            newUser.setPassword(PasswordHasher.hash(user.getPassword()));
            // Token will be null initially

            // Save user
            userRepository.save(newUser);

            sendSuccess(exchange, 201, "User registered successfully");

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    public void login(HttpExchange exchange) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes());
            User user = JsonUtil.fromJson(body, User.class);

            if (user.getUsername() == null || user.getPassword() == null) {
                sendError(exchange, 400, "Username and password are required");
                return;
            }

            // Find user by username
            var userOpt = userRepository.findByUsername(user.getUsername());

            if (userOpt.isPresent()) {
                User dbUser = userOpt.get();
                String storedHash = dbUser.getPassword();
                String providedPassword = user.getPassword();

                if (PasswordHasher.verify(providedPassword, storedHash)) {
                    // Generate UUID v7 token
                    String token = TokenUtil.generateToken();
                    String userId = dbUser.getId();

                    // Store token in database
                    userRepository.updateToken(userId, token);

                    // Return token in response
                    Map<String, String> response = new HashMap<>();
                    response.put("token", token);
                    response.put("userId", userId);
                    sendSuccess(exchange, 200, response);
                } else {
                    sendError(exchange, 401, "Invalid username or password");
                }
            } else {
                sendError(exchange, 401, "Invalid username or password");
            }
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    // Helper method for token validation (used by MediaService)
    public boolean validateToken(String token) {
        try {
            return userRepository.findByToken(token).isPresent();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to get user ID from token
    public String getUserIdFromToken(String token) {
        try {
            var userOpt = userRepository.findByToken(token);
            return userOpt.map(User::getId).orElse(null);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
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