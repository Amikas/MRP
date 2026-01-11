package mrp.service;

import com.sun.net.httpserver.HttpExchange;
import mrp.model.User;
import mrp.repository.interfaces.IUserRepository;
import mrp.service.interfaces.IUserService;
import mrp.util.JsonUtil;
import mrp.util.PasswordHasher;
import mrp.util.TokenUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.*;

public class UserService implements IUserService {
    private IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserService() {
        try {
            this.userRepository = (IUserRepository) new mrp.repository.UserRepository();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize UserRepository", e);
        }
    }

    private String getTokenFromExchange(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7); 
    }

    public String getUserIdFromExchange(HttpExchange exchange) {
        String token = getTokenFromExchange(exchange);
        if (token == null) {
            return null;
        }
        return getUserIdFromToken(token);
    }

    public boolean isAuthenticated(HttpExchange exchange) {
        return getUserIdFromExchange(exchange) != null;
    }

    public void getUserProfile(HttpExchange exchange) throws IOException {
        try {
            
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            String requestedUsername = pathParts[pathParts.length - 2]; 

            String currentUserId = getUserIdFromExchange(exchange);
            if (currentUserId == null) {
                sendError(exchange, 401, "Unauthorized");
                return;
            }

            var userOpt = userRepository.findByUsername(requestedUsername);
            if (userOpt.isEmpty()) {
                sendError(exchange, 404, "User not found");
                return;
            }

            User user = userOpt.get();

            Map<String, Object> userStats = userRepository.getUserStats(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("username", user.getUsername());
            response.put("userId", user.getId());
            response.put("statistics", userStats);

            if (currentUserId.equals(user.getId())) {
                
                response.put("email", ""); 
                response.put("joinedAt", ""); 
                response.put("isOwnProfile", true);
            } else {
                response.put("isOwnProfile", false);
            }

            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    public void updateUserProfile(HttpExchange exchange) throws IOException {
        
        String currentUserId = getUserIdFromExchange(exchange);
        if (currentUserId == null) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        try {
            
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            String requestedUsername = pathParts[pathParts.length - 2];

            var userOpt = userRepository.findByUsername(requestedUsername);
            if (userOpt.isEmpty()) {
                sendError(exchange, 404, "User not found");
                return;
            }

            User user = userOpt.get();
            if (!user.getId().equals(currentUserId)) {
                sendError(exchange, 403, "You can only update your own profile");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> updateData = JsonUtil.fromJson(body, Map.class);

            boolean updated = false;

            if (updateData.containsKey("password")) {
                String newPassword = (String) updateData.get("password");
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    user.setPassword(PasswordHasher.hash(newPassword));
                    updated = true;
                }
            }

            if (updateData.containsKey("email")) {

            }

            if (updated) {
                userRepository.update(user);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("username", user.getUsername());

            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    public void getLeaderboard(HttpExchange exchange) throws IOException {
        try {
            
            List<Map<String, Object>> leaderboard = userRepository.getLeaderboard();

            Map<String, Object> response = new HashMap<>();
            response.put("leaderboard", leaderboard);
            response.put("totalUsers", leaderboard.size());
            response.put("timestamp", new Date().toString());

            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * Creates a new user account after validating input and checking for duplicate usernames
     * Hashes the password before storing in the database
     */
    public void register(HttpExchange exchange) throws IOException {
        try {

            String body = new String(exchange.getRequestBody().readAllBytes());
            User user = JsonUtil.fromJson(body, User.class);

            if (user.getUsername() == null || user.getUsername().trim().isEmpty() ||
                    user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                sendError(exchange, 400, "Username and password are required");
                return;
            }

            // Check if username is already taken
            if (userRepository.usernameExists(user.getUsername())) {
                sendError(exchange, 409, "Username already exists");
                return;
            }

            // Create new user with unique ID and hashed password
            User newUser = new User();
            newUser.setId(UUID.randomUUID().toString());
            newUser.setUsername(user.getUsername());
            newUser.setPassword(PasswordHasher.hash(user.getPassword()));

            userRepository.save(newUser);

            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("username", newUser.getUsername());
            response.put("userId", newUser.getId());

            sendSuccess(exchange, 201, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user by verifying their credentials against the database
     * Generates a new authentication token upon successful login
     */
    public void login(HttpExchange exchange) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes());
            User user = JsonUtil.fromJson(body, User.class);

            if (user.getUsername() == null || user.getPassword() == null) {
                sendError(exchange, 400, "Username and password are required");
                return;
            }

            // Look up user in database by username
            var userOpt = userRepository.findByUsername(user.getUsername());

            if (userOpt.isPresent()) {
                User dbUser = userOpt.get();
                String storedHash = dbUser.getPassword();
                String providedPassword = user.getPassword();

                // Verify the provided password against the stored hash
                if (PasswordHasher.verify(providedPassword, storedHash)) {
                    // Generate new authentication token and update database
                    String token = TokenUtil.generateToken();
                    String userId = dbUser.getId();

                    userRepository.updateToken(userId, token);

                    Map<String, String> response = new HashMap<>();
                    response.put("token", token);
                    response.put("userId", userId);
                    response.put("username", dbUser.getUsername());
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

    public boolean validateToken(String token) {
        try {
            return userRepository.findByToken(token).isPresent();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        try {
            var userOpt = userRepository.findByToken(token);
            return userOpt.map(User::getId).orElse(null);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Optional<User> getUserFromToken(String token) {
        try {
            return userRepository.findByToken(token);
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
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