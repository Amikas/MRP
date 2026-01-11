package mrp.service;

import com.sun.net.httpserver.HttpExchange;
import mrp.model.MediaEntry;
import mrp.repository.interfaces.IFavoriteRepository;
import mrp.repository.interfaces.IMediaRepository;
import mrp.repository.interfaces.IUserRepository;
import mrp.service.interfaces.IFavoriteService;
import mrp.util.JsonUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.*;

public class FavoriteService implements IFavoriteService {
    private IFavoriteRepository favoriteRepository;
    private IMediaRepository mediaRepository;
    private IUserRepository userRepository;

    public FavoriteService(IFavoriteRepository favoriteRepository, IMediaRepository mediaRepository, IUserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
    }

    public FavoriteService() {
        try {
            this.favoriteRepository = (IFavoriteRepository) new mrp.repository.FavoriteRepository();
            this.mediaRepository = (IMediaRepository) new mrp.repository.MediaRepository();
            this.userRepository = (IUserRepository) new mrp.repository.UserRepository();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize repositories", e);
        }
    }

    /**
     * Helper method to get user ID from token
     */
    private String getUserIdFromToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        try {
            var userOpt = userRepository.findByToken(token);
            return userOpt.map(mrp.model.User::getId).orElse(null);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to validate authentication
     */
    public boolean isAuthenticated(HttpExchange exchange) {
        return getUserIdFromToken(exchange) != null;
    }

    /**
     * Add a media entry to user's favorites
     * POST /api/favorites/{mediaId}
     */
    public void addFavorite(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String userId = getUserIdFromToken(exchange);
        String path = exchange.getRequestURI().getPath();
        String mediaId = path.substring(path.lastIndexOf("/") + 1);

        try {
            // Validate media ID is provided
            if (mediaId == null || mediaId.isEmpty()) {
                sendError(exchange, 400, "Media ID is required");
                return;
            }

            // Check if media exists
            var mediaOpt = mediaRepository.findById(mediaId);
            if (mediaOpt.isEmpty()) {
                sendError(exchange, 404, "Media not found");
                return;
            }

            // Check if already favorited
            if (favoriteRepository.isFavorite(userId, mediaId)) {
                sendError(exchange, 409, "This media is already in your favorites");
                return;
            }

            // Add to favorites
            favoriteRepository.addFavorite(userId, mediaId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Media added to favorites");
            response.put("mediaId", mediaId);
            response.put("mediaTitle", mediaOpt.get().getTitle());

            sendSuccess(exchange, 201, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    /**
     * Remove a media entry from user's favorites
     * DELETE /api/favorites/{mediaId}
     */
    public void removeFavorite(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String userId = getUserIdFromToken(exchange);
        String path = exchange.getRequestURI().getPath();
        String mediaId = path.substring(path.lastIndexOf("/") + 1);

        try {
            // Validate media ID is provided
            if (mediaId == null || mediaId.isEmpty()) {
                sendError(exchange, 400, "Media ID is required");
                return;
            }

            // Check if media exists
            var mediaOpt = mediaRepository.findById(mediaId);
            if (mediaOpt.isEmpty()) {
                sendError(exchange, 404, "Media not found");
                return;
            }

            // Check if it's actually in favorites
            if (!favoriteRepository.isFavorite(userId, mediaId)) {
                sendError(exchange, 404, "This media is not in your favorites");
                return;
            }

            // Remove from favorites
            favoriteRepository.removeFavorite(userId, mediaId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Media removed from favorites");
            response.put("mediaId", mediaId);

            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    /**
     * Get all of user's favorite media entries
     * GET /api/favorites
     */
    public void getUserFavorites(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String userId = getUserIdFromToken(exchange);

        try {
            List<MediaEntry> favorites = favoriteRepository.getUserFavorites(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("count", favorites.size());
            response.put("favorites", favorites);

            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    /**
     * Check if a specific media is in user's favorites
     * GET /api/favorites/{mediaId}/check
     */
    public void checkIsFavorite(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String userId = getUserIdFromToken(exchange);
        String path = exchange.getRequestURI().getPath();

        // Extract mediaId from path like /api/favorites/{mediaId}/check
        String[] pathParts = path.split("/");
        String mediaId = null;

        if (pathParts.length >= 2) {
            mediaId = pathParts[pathParts.length - 2];
        }

        try {
            if (mediaId == null || mediaId.isEmpty()) {
                sendError(exchange, 400, "Media ID is required");
                return;
            }

            // Check if media exists
            var mediaOpt = mediaRepository.findById(mediaId);
            if (mediaOpt.isEmpty()) {
                sendError(exchange, 404, "Media not found");
                return;
            }

            boolean isFavorite = favoriteRepository.isFavorite(userId, mediaId);

            Map<String, Object> response = new HashMap<>();
            response.put("mediaId", mediaId);
            response.put("isFavorite", isFavorite);
            response.put("favoriteCount", favoriteRepository.getFavoriteCount(mediaId));

            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    /**
     * Get favorite count for a media entry (public endpoint)
     * GET /api/media/{mediaId}/favorite-count
     */
    public void getMediaFavoriteCount(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String mediaId = path.substring(path.lastIndexOf("/") + 1);
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        try {
            if (mediaId == null || mediaId.isEmpty()) {
                sendError(exchange, 400, "Media ID is required");
                return;
            }

            // Check if media exists
            var mediaOpt = mediaRepository.findById(mediaId);
            if (mediaOpt.isEmpty()) {
                sendError(exchange, 404, "Media not found");
                return;
            }

            int favoriteCount = favoriteRepository.getFavoriteCount(mediaId);

            Map<String, Object> response = new HashMap<>();
            response.put("mediaId", mediaId);
            response.put("mediaTitle", mediaOpt.get().getTitle());
            response.put("favoriteCount", favoriteCount);

            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    /**
     * Get all users who favorited a specific media entry
     * GET /api/favorites/{mediaId}/users
     */
    public void getMediaFavorites(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String mediaId = path.substring(path.lastIndexOf("/") - 1); // Get mediaId from /api/favorites/{mediaId}/users

        try {
            if (mediaId == null || mediaId.isEmpty()) {
                sendError(exchange, 400, "Media ID is required");
                return;
            }

            // Check if media exists
            var mediaOpt = mediaRepository.findById(mediaId);
            if (mediaOpt.isEmpty()) {
                sendError(exchange, 404, "Media not found");
                return;
            }

            // Note: The current FavoriteRepository doesn't support getting users who favorited a media
            // This would require a different implementation or a new method in the repository
            // For now, returning the favorite count as a placeholder
            int favoriteCount = favoriteRepository.getFavoriteCount(mediaId);

            Map<String, Object> response = new HashMap<>();
            response.put("mediaId", mediaId);
            response.put("mediaTitle", mediaOpt.get().getTitle());
            response.put("favoriteCount", favoriteCount);
            response.put("message", "This endpoint is not fully implemented in the current repository structure");

            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    // Helper methods for sending responses
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