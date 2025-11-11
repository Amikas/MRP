package mrp.service;

import com.sun.net.httpserver.HttpExchange;
import mrp.model.MediaEntry;
import mrp.util.JsonUtil;
import mrp.database.DatabaseConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaService {
    private DatabaseConnection dbConnection = new DatabaseConnection();

    public boolean isAuthenticated(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

    private String getUsernameFromToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        String token = authHeader.substring(7);
        return token.replace("-mrpToken", "");
    }

    public void createMedia(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        try (Connection conn = dbConnection.getConnection()) {
            String body = new String(exchange.getRequestBody().readAllBytes());
            MediaEntry media = JsonUtil.fromJson(body, MediaEntry.class);

            // Get actual user ID
            String username = getUsernameFromToken(exchange);
            String userId = getUserId(conn, username);
            if (userId == null) {
                sendError(exchange, 401, "User not found");
                return;
            }

            String sql = "INSERT INTO media_entries (id, title, description, media_type, release_year, genres, age_restriction, creator_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String mediaId = java.util.UUID.randomUUID().toString();
                stmt.setString(1, mediaId);
                stmt.setString(2, media.getTitle());
                stmt.setString(3, media.getDescription());
                stmt.setString(4, media.getMediaType());
                stmt.setInt(5, media.getReleaseYear());
                stmt.setString(6, media.getGenres());
                stmt.setInt(7, media.getAgeRestriction());
                stmt.setString(8, userId);

                stmt.executeUpdate();

                media.setId(mediaId);
                sendSuccess(exchange, 201, media);
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private String getUserId(Connection conn, String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("id") : null;
        }
    }

    public void updateMedia(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        try (Connection conn = dbConnection.getConnection()) {
            String path = exchange.getRequestURI().getPath();
            String mediaId = path.substring(path.lastIndexOf("/") + 1);

            String body = new String(exchange.getRequestBody().readAllBytes());
            MediaEntry updateData = JsonUtil.fromJson(body, MediaEntry.class);

            StringBuilder sql = new StringBuilder("UPDATE media_entries SET ");
            List<Object> params = new ArrayList<>();

            if (updateData.getTitle() != null) {
                sql.append("title = ?, ");
                params.add(updateData.getTitle());
            }
            if (updateData.getDescription() != null) {
                sql.append("description = ?, ");
                params.add(updateData.getDescription());
            }
            if (updateData.getMediaType() != null) {
                sql.append("media_type = ?, ");
                params.add(updateData.getMediaType());
            }
            if (updateData.getReleaseYear() > 0) {
                sql.append("release_year = ?, ");
                params.add(updateData.getReleaseYear());
            }
            if (updateData.getGenres() != null) {
                sql.append("genres = ?, ");
                params.add(updateData.getGenres());
            }
            if (updateData.getAgeRestriction() > 0) {
                sql.append("age_restriction = ?, ");
                params.add(updateData.getAgeRestriction());
            }

            if (params.isEmpty()) {
                sendError(exchange, 400, "No fields to update");
                return;
            }

            sql.setLength(sql.length() - 2);
            sql.append(" WHERE id = ?");
            params.add(mediaId);

            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }

                int updated = stmt.executeUpdate();
                if (updated > 0) {
                    // Use your existing getMedia method to fetch the complete updated media
                    MediaEntry updatedMedia = getMediaById(conn, mediaId);
                    sendSuccess(exchange, 200, updatedMedia);
                } else {
                    sendError(exchange, 404, "Media not found");
                }
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    // Extract the database logic from getMedia into a reusable method
    private MediaEntry getMediaById(Connection conn, String mediaId) throws SQLException {
        String sql = "SELECT * FROM media_entries WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mediaId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                MediaEntry media = new MediaEntry();
                media.setId(rs.getString("id"));
                media.setTitle(rs.getString("title"));
                media.setDescription(rs.getString("description"));
                media.setMediaType(rs.getString("media_type"));
                media.setReleaseYear(rs.getInt("release_year"));
                media.setGenres(rs.getString("genres"));
                media.setAgeRestriction(rs.getInt("age_restriction"));
                media.setCreatorId(rs.getString("creator_id"));
                return media;
            }
        }
        return null;
    }

    // Update your existing getMedia method to use the helper
    public void getMedia(HttpExchange exchange) throws IOException {
        try (Connection conn = dbConnection.getConnection()) {
            String path = exchange.getRequestURI().getPath();
            String mediaId = path.substring(path.lastIndexOf("/") + 1);

            MediaEntry media = getMediaById(conn, mediaId);
            if (media != null) {
                sendSuccess(exchange, 200, media);
            } else {
                sendError(exchange, 404, "Media not found");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }
    public void getAllMedia(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        try (Connection conn = dbConnection.getConnection()) {
            String sql = "SELECT * FROM media_entries";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();

                List<MediaEntry> mediaList = new ArrayList<>();
                while (rs.next()) {
                    MediaEntry media = new MediaEntry();
                    media.setId(rs.getString("id"));
                    media.setTitle(rs.getString("title"));
                    media.setDescription(rs.getString("description"));
                    media.setMediaType(rs.getString("media_type"));
                    media.setReleaseYear(rs.getInt("release_year"));
                    media.setGenres(rs.getString("genres"));
                    media.setAgeRestriction(rs.getInt("age_restriction"));
                    media.setCreatorId(rs.getString("creator_id"));
                    mediaList.add(media);


                }
                sendSuccess(exchange, 200, mediaList);
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }


    public void deleteMedia(HttpExchange exchange) throws IOException {
        if (!isAuthenticated(exchange)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }

        try (Connection conn = dbConnection.getConnection()) {
            String path = exchange.getRequestURI().getPath();
            String mediaId = path.substring(path.lastIndexOf("/") + 1);

            // First get the media before deleting it
            MediaEntry deletedMedia = getMediaById(conn, mediaId);
            if (deletedMedia == null) {
                sendError(exchange, 404, "Media not found");
                return;
            }

            String sql = "DELETE FROM media_entries WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, mediaId);

                int deleted = stmt.executeUpdate();
                if (deleted > 0) {
                    sendSuccess(exchange, 200, deletedMedia);
                } else {
                    sendError(exchange, 404, "Media not found");
                }
            }
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