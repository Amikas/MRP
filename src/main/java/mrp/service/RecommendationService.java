// Create: service/RecommendationService.java
package mrp.service;

import com.sun.net.httpserver.HttpExchange;
import mrp.model.MediaEntry;
import mrp.repository.interfaces.IMediaRepository;
import mrp.repository.interfaces.IRatingRepository;
import mrp.repository.interfaces.IUserRepository;
import mrp.service.interfaces.IRecommendationService;
import mrp.util.JsonUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService implements IRecommendationService {
    private IMediaRepository mediaRepository;
    private IRatingRepository ratingRepository;
    private IUserRepository userRepository;

    public RecommendationService(IMediaRepository mediaRepository, IRatingRepository ratingRepository, IUserRepository userRepository) {
        this.mediaRepository = mediaRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
    }

    public RecommendationService() {
        try {
            this.mediaRepository = (IMediaRepository) new mrp.repository.MediaRepository();
            this.ratingRepository = (IRatingRepository) new mrp.repository.RatingRepository();
            this.userRepository = (IUserRepository) new mrp.repository.UserRepository();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize repositories", e);
        }
    }

    /**
     * GET /api/recommendations
     * Get personalized recommendations for authenticated user
     */
    public void getRecommendations(HttpExchange exchange) throws IOException {
        // Authentication required
        String userId = getUserIdFromToken(exchange);
        if (userId == null) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        try {
            // Get user's ratings to understand preferences
            List<mrp.model.Rating> userRatings = ratingRepository.findByUserId(userId);

            // Separate highly rated media (score >= 4)
            List<String> highlyRatedMediaIds = userRatings.stream()
                    .filter(rating -> rating.getScore() >= 4)
                    .map(mrp.model.Rating::getMediaId)
                    .collect(Collectors.toList());

            List<MediaEntry> recommendations = new ArrayList<>();

            if (!highlyRatedMediaIds.isEmpty()) {
                // Get genres from highly rated media
                Set<String> preferredGenres = getPreferredGenres(highlyRatedMediaIds);

                // Get similar media based on preferred genres
                recommendations = getMediaByGenres(preferredGenres, userId);

                // Remove media user has already rated
                Set<String> ratedMediaIds = userRatings.stream()
                        .map(mrp.model.Rating::getMediaId)
                        .collect(Collectors.toSet());
                recommendations.removeIf(media -> ratedMediaIds.contains(media.getId()));

                // Limit results
                recommendations = recommendations.stream()
                        .limit(10)
                        .collect(Collectors.toList());
            } else {
                // If user has no ratings, recommend popular media
                recommendations = getPopularMedia();
            }

            // Add average ratings to each media
            for (MediaEntry media : recommendations) {
                double avgRating = ratingRepository.getAverageRatingForMedia(media.getId());
                // You could add this to a DTO or extend MediaEntry
            }

            Map<String, Object> response = new HashMap<>();
            response.put("recommendations", recommendations);
            response.put("count", recommendations.size());
            response.put("basedOn", highlyRatedMediaIds.isEmpty() ? "popularity" : "your preferences");

            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/media/{id}/similar
     * Get media similar to a specific media item
     */
    public void getSimilarMedia(HttpExchange exchange) throws IOException {
        try {
            // Extract mediaId from path like /api/media/{mediaId}/similar
            String path = exchange.getRequestURI().getPath();
            // Remove the trailing "/similar" to get the media ID
            if (path.endsWith("/similar")) {
                path = path.substring(0, path.length() - "/similar".length());
            }
            String mediaId = path.substring(path.lastIndexOf("/") + 1);

            // Check if media exists
            var mediaOpt = mediaRepository.findById(mediaId);
            if (mediaOpt.isEmpty()) {
                sendError(exchange, 404, "Media not found");
                return;
            }

            MediaEntry targetMedia = mediaOpt.get();

            // Find similar media based on genre, type, and age restriction
            List<MediaEntry> similarMedia = mediaRepository.search(
                    null, // title
                    String.join(",", targetMedia.getGenres()), // genre
                    targetMedia.getMediaType(), // media type
                    targetMedia.getReleaseYear() - 5, // min year
                    targetMedia.getReleaseYear() + 5, // max year
                    targetMedia.getAgeRestriction() + 3, // max age restriction
                    3.0, // min rating
                    null, // sortBy
                    null // sortOrder
            );

            // Remove the target media from results
            similarMedia.removeIf(media -> media.getId().equals(mediaId));

            // Limit results
            similarMedia = similarMedia.stream()
                    .limit(8)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("targetMedia", targetMedia.getTitle());
            response.put("similarMedia", similarMedia);
            response.put("count", similarMedia.size());

            sendSuccess(exchange, 200, response);

        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    // Helper methods
    private Set<String> getPreferredGenres(List<String> mediaIds) throws SQLException {
        Set<String> genres = new HashSet<>();
        for (String mediaId : mediaIds) {
            var mediaOpt = mediaRepository.findById(mediaId);
            if (mediaOpt.isPresent()) {
                genres.addAll(mediaOpt.get().getGenres());
            }
        }
        return genres;
    }

    private List<MediaEntry> getMediaByGenres(Set<String> genres, String excludeUserId) throws SQLException {
        // Simple implementation - get media that matches any of the preferred genres
        List<MediaEntry> allMedia = mediaRepository.findAll();
        List<MediaEntry> filtered = new ArrayList<>();

        for (MediaEntry media : allMedia) {
            for (String genre : media.getGenres()) {
                if (genres.contains(genre)) {
                    filtered.add(media);
                    break;
                }
            }
        }

        return filtered;
    }

    private List<MediaEntry> getPopularMedia() throws SQLException {
        // Get all media and sort by average rating (you might want to cache this)
        List<MediaEntry> allMedia = mediaRepository.findAll();

        // Sort by rating (you need to get ratings for each)
        allMedia.sort((m1, m2) -> {
            try {
                double r1 = ratingRepository.getAverageRatingForMedia(m1.getId());
                double r2 = ratingRepository.getAverageRatingForMedia(m2.getId());
                return Double.compare(r2, r1); // Descending
            } catch (SQLException e) {
                return 0;
            }
        });

        return allMedia.stream().limit(10).collect(Collectors.toList());
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

    private void sendSuccess(HttpExchange exchange, int code, Object data) throws IOException {
        String response = JsonUtil.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    public boolean isAuthenticated(HttpExchange exchange) {
        return getUserIdFromToken(exchange) != null;
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