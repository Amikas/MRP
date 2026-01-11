package mrp.service;

import com.sun.net.httpserver.HttpExchange;
import mrp.model.Rating;
import mrp.repository.interfaces.IRatingRepository;
import mrp.repository.interfaces.IMediaRepository;
import mrp.repository.interfaces.IUserRepository;
import mrp.service.interfaces.IRatingService;
import mrp.util.JsonUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.*;

public class RatingService implements IRatingService {
    private IRatingRepository ratingRepository;
    private IMediaRepository mediaRepository;
    private IUserRepository userRepository;

    public RatingService(IRatingRepository ratingRepository, IMediaRepository mediaRepository, IUserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
    }

    public RatingService() {
        try {
            this.ratingRepository = (IRatingRepository) new mrp.repository.RatingRepository();
            this.mediaRepository = (IMediaRepository) new mrp.repository.MediaRepository();
            this.userRepository = (IUserRepository) new mrp.repository.UserRepository();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize repositories", e);
        }
    }

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

    private boolean isAuthenticated(HttpExchange exchange) {
        return getUserIdFromToken(exchange) != null;
    }

    public void createRating(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String userId = getUserIdFromToken(exchange);
        try {
            String body = new String(exchange.getRequestBody().readAllBytes());

            Map<String, Object> ratingData = JsonUtil.fromJson(body, Map.class);

            String mediaId = (String) ratingData.get("mediaId");
            if (mediaId == null || mediaId.trim().isEmpty()) {
                sendError(exchange, 400, "Media ID is required");
                return;
            }

            Object scoreObj = ratingData.get("score");
            if (scoreObj == null) {
                sendError(exchange, 400, "Score is required");
                return;
            }

            int score;
            try {
                score = Integer.parseInt(scoreObj.toString());
            } catch (NumberFormatException e) {
                sendError(exchange, 400, "Score must be a number between 1 and 5");
                return;
            }

            if (score < 1 || score > 5) {
                sendError(exchange, 400, "Score must be between 1 and 5");
                return;
            }

            String comment = (String) ratingData.get("comment");
            Boolean commentPublic = (Boolean) ratingData.get("commentPublic");

            var mediaOpt = mediaRepository.findById(mediaId);
            if (mediaOpt.isEmpty()) {
                sendError(exchange, 404, "Media not found");
                return;
            }

            var existingRatingOpt = ratingRepository.findByUserAndMedia(userId, mediaId);
            if (existingRatingOpt.isPresent()) {
                sendError(exchange, 409, "You have already rated this media. Use PATCH to update your rating.");
                return;
            }

            Rating rating = new Rating();
            rating.setId(UUID.randomUUID().toString());
            rating.setMediaId(mediaId);
            rating.setUserId(userId);
            rating.setScore(score);
            rating.setComment(comment);
            rating.setCommentPublic(commentPublic != null ? commentPublic : false);

            ratingRepository.save(rating);

            var savedRatingOpt = ratingRepository.findById(rating.getId());
            if (savedRatingOpt.isPresent()) {
                Rating savedRating = savedRatingOpt.get();
                
                var userOpt = userRepository.findById(savedRating.getUserId());
                if (userOpt.isPresent()) {
                    savedRating.setUsername(userOpt.get().getUsername());
                }
                
                savedRating.setMediaTitle(mediaOpt.get().getTitle());

                sendSuccess(exchange, 201, savedRating);
            } else {
                sendSuccess(exchange, 201, rating);
            }

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    public void updateRating(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String userId = getUserIdFromToken(exchange);
        String path = exchange.getRequestURI().getPath();
        String ratingId = path.substring(path.lastIndexOf("/") + 1);

        try {
            
            var ratingOpt = ratingRepository.findById(ratingId);
            if (ratingOpt.isEmpty()) {
                sendError(exchange, 404, "Rating not found");
                return;
            }

            Rating existingRating = ratingOpt.get();
            if (!existingRating.getUserId().equals(userId)) {
                sendError(exchange, 403, "You can only update your own ratings");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> updateData = JsonUtil.fromJson(body, Map.class);

            if (updateData.containsKey("score")) {
                Object scoreObj = updateData.get("score");
                try {
                    int score = Integer.parseInt(scoreObj.toString());
                    if (score >= 1 && score <= 5) {
                        existingRating.setScore(score);
                    }
                } catch (NumberFormatException e) {
                    
                }
            }
            if (updateData.containsKey("comment")) {
                existingRating.setComment((String) updateData.get("comment"));
            }
            if (updateData.containsKey("commentPublic")) {
                Boolean commentPublic = (Boolean) updateData.get("commentPublic");
                if (commentPublic != null) {
                    existingRating.setCommentPublic(commentPublic);
                }
            }

            ratingRepository.update(existingRating);

            var updatedRatingOpt = ratingRepository.findById(ratingId);
            sendSuccess(exchange, 200, updatedRatingOpt.get());

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    public void deleteRating(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String userId = getUserIdFromToken(exchange);
        String path = exchange.getRequestURI().getPath();
        String ratingId = path.substring(path.lastIndexOf("/") + 1);

        try {
            
            var ratingOpt = ratingRepository.findById(ratingId);
            if (ratingOpt.isEmpty()) {
                sendError(exchange, 404, "Rating not found");
                return;
            }

            Rating rating = ratingOpt.get();
            if (!rating.getUserId().equals(userId)) {
                sendError(exchange, 403, "You can only delete your own ratings");
                return;
            }

            ratingRepository.delete(ratingId, userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Rating deleted successfully");
            response.put("ratingId", ratingId);
            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    public void getMediaRatings(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        try {
            
            String mediaId = extractMediaIdFromQuery(exchange);

            if (mediaId == null || mediaId.trim().isEmpty()) {
                sendError(exchange, 400, "mediaId query parameter is required");
                return;
            }

            var mediaOpt = mediaRepository.findById(mediaId);
            if (mediaOpt.isEmpty()) {
                sendError(exchange, 404, "Media not found");
                return;
            }

            List<Rating> ratings = ratingRepository.findByMediaId(mediaId);

            String userId = getUserIdFromToken(exchange);
            List<Rating> filteredRatings = new ArrayList<>();
            for (Rating rating : ratings) {
                if (rating.isCommentPublic() || rating.getUserId().equals(userId)) {
                    filteredRatings.add(rating);
                } else {
                    
                    Rating filtered = new Rating();
                    filtered.setId(rating.getId());
                    filtered.setMediaId(rating.getMediaId());
                    filtered.setUserId(rating.getUserId());
                    filtered.setScore(rating.getScore());
                    filtered.setCommentPublic(false);
                    filtered.setUsername(rating.getUsername());
                    filtered.setMediaTitle(rating.getMediaTitle());
                    filtered.setLikeCount(rating.getLikeCount());
                    filteredRatings.add(filtered);
                }
            }

            sendSuccess(exchange, 200, filteredRatings);
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    public void getUserRatings(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String userId = getUserIdFromToken(exchange);
        try {
            List<Rating> ratings = ratingRepository.findByUserId(userId);
            sendSuccess(exchange, 200, ratings);
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    public void getMediaRatingStats(HttpExchange exchange) throws IOException {
        
        if (!isAuthenticated(exchange)) {
           sendError(exchange, 401, "Unauthorized");
           return;
        }

        try {
            
            String mediaId = extractMediaIdFromQuery(exchange);

            if (mediaId == null || mediaId.trim().isEmpty()) {
                sendError(exchange, 400, "mediaId query parameter is required");
                return;
            }

            var mediaOpt = mediaRepository.findById(mediaId);
            if (mediaOpt.isEmpty()) {
                sendError(exchange, 404, "Media not found");
                return;
            }

            double averageRating = ratingRepository.getAverageRatingForMedia(mediaId);
            int ratingCount = ratingRepository.getRatingCountForMedia(mediaId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("mediaId", mediaId);
            stats.put("averageRating", Math.round(averageRating * 10.0) / 10.0); 
            stats.put("ratingCount", ratingCount);
            stats.put("mediaTitle", mediaOpt.get().getTitle());

            sendSuccess(exchange, 200, stats);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    public void confirmComment(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String userId = getUserIdFromToken(exchange);
        String path = exchange.getRequestURI().getPath();

        String[] pathParts = path.split("/");
        String ratingId = pathParts[pathParts.length - 2]; 

        try {
            
            var ratingOpt = ratingRepository.findById(ratingId);
            if (ratingOpt.isEmpty()) {
                sendError(exchange, 404, "Rating not found");
                return;
            }

            Rating rating = ratingOpt.get();

            if (!rating.getUserId().equals(userId)) {
                sendError(exchange, 403, "You can only confirm your own comments");
                return;
            }

            rating.setCommentPublic(true);
            ratingRepository.update(rating);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Comment confirmed and made public");
            response.put("ratingId", ratingId);
            response.put("commentPublic", "true");

            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private String extractMediaIdFromQuery(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && "mediaId".equals(pair[0])) {
                return pair[1];
            }
        }
        return null;
    }

    public void likeRating(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String userId = getUserIdFromToken(exchange);
        String path = exchange.getRequestURI().getPath();

        String[] pathParts = path.split("/");
        String ratingId = null;

        for (int i = 0; i < pathParts.length; i++) {
            if (i + 1 < pathParts.length && "like".equals(pathParts[i + 1])) {
                ratingId = pathParts[i];
                break;
            }
        }

        if (ratingId == null || ratingId.isEmpty()) {
            sendError(exchange, 400, "Rating ID not found in path");
            return;
        }

        try {
            
            var ratingOpt = ratingRepository.findById(ratingId);
            if (ratingOpt.isEmpty()) {
                sendError(exchange, 404, "Rating not found");
                return;
            }

            Rating rating = ratingOpt.get();

            if (rating.getUserId().equals(userId)) {
                sendError(exchange, 400, "You cannot like your own rating");
                return;
            }

            if (ratingRepository.hasUserLikedRating(userId, ratingId)) {
                sendError(exchange, 409, "You have already liked this rating");
                return;
            }

            ratingRepository.addLike(userId, ratingId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Rating liked successfully");
            response.put("ratingId", ratingId);
            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    public void unlikeRating(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String userId = getUserIdFromToken(exchange);
        String path = exchange.getRequestURI().getPath();

        String[] pathParts = path.split("/");
        String ratingId = null;

        for (int i = 0; i < pathParts.length; i++) {
            if (i + 1 < pathParts.length && "like".equals(pathParts[i + 1])) {
                ratingId = pathParts[i];
                break;
            }
        }

        if (ratingId == null || ratingId.isEmpty()) {
            sendError(exchange, 400, "Rating ID not found in path");
            return;
        }

        try {
            
            var ratingOpt = ratingRepository.findById(ratingId);
            if (ratingOpt.isEmpty()) {
                sendError(exchange, 404, "Rating not found");
                return;
            }

            if (!ratingRepository.hasUserLikedRating(userId, ratingId)) {
                sendError(exchange, 404, "You have not liked this rating");
                return;
            }

            ratingRepository.removeLike(userId, ratingId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Rating unliked successfully");
            response.put("ratingId", ratingId);
            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    public void getRating(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        
        String[] pathParts = path.split("/");
        String ratingId = pathParts[pathParts.length - 1];

        try {
            var ratingOpt = ratingRepository.findById(ratingId);
            if (ratingOpt.isPresent()) {
                sendSuccess(exchange, 200, ratingOpt.get());
            } else {
                sendError(exchange, 404, "Rating not found");
            }
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
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