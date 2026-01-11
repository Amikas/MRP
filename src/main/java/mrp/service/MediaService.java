package mrp.service;

import com.sun.net.httpserver.HttpExchange;
import mrp.model.MediaEntry;
import mrp.repository.interfaces.IMediaRepository;
import mrp.repository.interfaces.IUserRepository;
import mrp.service.interfaces.IMediaService;
import mrp.util.JsonUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.*;

public class MediaService implements IMediaService {
    private IMediaRepository mediaRepository;
    private IUserRepository userRepository;

    public MediaService(IMediaRepository mediaRepository, IUserRepository userRepository) {
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
    }

    public MediaService() {
        try {
            this.mediaRepository = (IMediaRepository) new mrp.repository.MediaRepository();
            this.userRepository = (IUserRepository) new mrp.repository.UserRepository();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize repositories", e);
        }
    }

    public boolean isAuthenticated(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);
        return validateToken(token);
    }

    private boolean validateToken(String token) {
        try {
            return userRepository.findByToken(token).isPresent();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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

    public void createMedia(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        try {
            String body = new String(exchange.getRequestBody().readAllBytes());
            MediaEntry media = JsonUtil.fromJson(body, MediaEntry.class);

            String userId = getUserIdFromToken(exchange);
            if (userId == null) {
                sendError(exchange, 401, "User not found");
                return;
            }

            if (media.getTitle() == null || media.getTitle().trim().isEmpty()) {
                sendError(exchange, 400, "Title is required");
                return;
            }
            if (media.getMediaType() == null || media.getMediaType().trim().isEmpty()) {
                sendError(exchange, 400, "Media type is required");
                return;
            }
            if (!isValidMediaType(media.getMediaType())) {
                sendError(exchange, 400, "Invalid media type. Must be 'movie', 'series', or 'game'");
                return;
            }

            media.setId(UUID.randomUUID().toString());
            media.setCreatorId(userId);

            mediaRepository.save(media);

            sendSuccess(exchange, 201, media);
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    public void updateMedia(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        try {
            String path = exchange.getRequestURI().getPath();
            String mediaId = path.substring(path.lastIndexOf("/") + 1);

            var mediaOpt = mediaRepository.findById(mediaId);
            if (mediaOpt.isEmpty()) {
                sendError(exchange, 404, "Media not found");
                return;
            }

            MediaEntry existingMedia = mediaOpt.get();

            String userId = getUserIdFromToken(exchange);
            if (!existingMedia.getCreatorId().equals(userId)) {
                sendError(exchange, 403, "You can only update your own media");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());
            MediaEntry updateData = JsonUtil.fromJson(body, MediaEntry.class);

            if (updateData.getTitle() != null && !updateData.getTitle().trim().isEmpty()) {
                existingMedia.setTitle(updateData.getTitle());
            }
            if (updateData.getDescription() != null) {
                existingMedia.setDescription(updateData.getDescription());
            }
            if (updateData.getMediaType() != null && !updateData.getMediaType().trim().isEmpty()) {
                if (!isValidMediaType(updateData.getMediaType())) {
                    sendError(exchange, 400, "Invalid media type. Must be 'movie', 'series', or 'game'");
                    return;
                }
                existingMedia.setMediaType(updateData.getMediaType());
            }
            if (updateData.getReleaseYear() > 0) {
                existingMedia.setReleaseYear(updateData.getReleaseYear());
            }
            if (updateData.getGenres() != null) {
                existingMedia.setGenres(updateData.getGenres());
            }
            if (updateData.getAgeRestriction() > 0) {
                existingMedia.setAgeRestriction(updateData.getAgeRestriction());
            }

            mediaRepository.update(existingMedia);

            sendSuccess(exchange, 200, existingMedia);
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    public void getMedia(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String mediaId = path.substring(path.lastIndexOf("/") + 1);

            var mediaOpt = mediaRepository.findById(mediaId);
            if (mediaOpt.isPresent()) {
                sendSuccess(exchange, 200, mediaOpt.get());
            } else {
                sendError(exchange, 404, "Media not found");
            }
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    public void getAllMedia(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        try {
            List<MediaEntry> mediaList = mediaRepository.findAll();
            sendSuccess(exchange, 200, mediaList);
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    public void deleteMedia(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        try {
            String path = exchange.getRequestURI().getPath();
            String mediaId = path.substring(path.lastIndexOf("/") + 1);

            var mediaOpt = mediaRepository.findById(mediaId);
            if (mediaOpt.isEmpty()) {
                sendError(exchange, 404, "Media not found");
                return;
            }

            MediaEntry media = mediaOpt.get();

            String userId = getUserIdFromToken(exchange);
            if (!media.getCreatorId().equals(userId)) {
                sendError(exchange, 403, "You can only delete your own media");
                return;
            }

            mediaRepository.delete(mediaId);

            sendSuccess(exchange, 200, media);
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private boolean isValidMediaType(String mediaType) {
        return mediaType.equals("movie") || mediaType.equals("series") || mediaType.equals("game");
    }

    private Map<String, String> parseQueryParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();

        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    params.put(pair[0], pair[1]);
                } else {
                    params.put(pair[0], "");
                }
            }
        }
        return params;
    }
    public void searchMedia(HttpExchange exchange) throws IOException {
        
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        try {
            
            Map<String, String> queryParams = parseQueryParams(exchange);

            String title = queryParams.get("title");
            String genre = queryParams.get("genre");
            String mediaType = queryParams.get("mediaType");
            Integer minYear = queryParams.containsKey("minYear") ?
                    Integer.parseInt(queryParams.get("minYear")) : null;
            Integer maxYear = queryParams.containsKey("maxYear") ?
                    Integer.parseInt(queryParams.get("maxYear")) : null;
            Integer maxAgeRestriction = queryParams.containsKey("maxAge") ?
                    Integer.parseInt(queryParams.get("maxAge")) : null;
            Double minRating = queryParams.containsKey("minRating") ?
                    Double.parseDouble(queryParams.get("minRating")) : null;
            String sortBy = queryParams.get("sortBy"); 
            String sortOrder = queryParams.get("sortOrder"); 

            List<MediaEntry> results = mediaRepository.search(
                    title, genre, mediaType, minYear, maxYear,
                    maxAgeRestriction, minRating, sortBy, sortOrder);

            sendSuccess(exchange, 200, results);
        } catch (NumberFormatException e) {
            sendError(exchange, 400, "Invalid number format in query parameters");
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