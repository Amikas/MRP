package mrp.service;

import com.sun.net.httpserver.HttpExchange;
import mrp.model.MediaEntry;
import mrp.util.JsonUtil;
import mrp.database.DatabaseConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                stmt.setString(1, java.util.UUID.randomUUID().toString());
                stmt.setString(2, media.getTitle());
                stmt.setString(3, media.getDescription());
                stmt.setString(4, media.getMediaType());
                stmt.setInt(5, media.getReleaseYear());
                stmt.setString(6, media.getGenres());
                stmt.setInt(7, media.getAgeRestriction());
                stmt.setString(8, userId);

                stmt.executeUpdate();
                sendSuccess(exchange, 201, "Media created");
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

    public void getMedia(HttpExchange exchange) throws IOException {
        try (Connection conn = dbConnection.getConnection()) {
            String path = exchange.getRequestURI().getPath();
            String mediaId = path.substring(path.lastIndexOf("/") + 1);

            String sql = "SELECT * FROM media_entries WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, mediaId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String response = "Media: " + rs.getString("title");
                    sendSuccess(exchange, 200, response);
                } else {
                    sendError(exchange, 404, "Media not found");
                }
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

                StringBuilder response = new StringBuilder("Media list: ");
                while (rs.next()) {
                    response.append(rs.getString("title")).append(", ");
                }
                sendSuccess(exchange, 200, response.toString());
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
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

            // Get the update data from request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            MediaEntry updateData = JsonUtil.fromJson(body, MediaEntry.class);

            // Build dynamic SQL based on provided fields
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

            // Remove last comma and space, add WHERE clause
            if (params.isEmpty()) {
                sendError(exchange, 400, "No fields to update");
                return;
            }

            sql.setLength(sql.length() - 2); // Remove last ", "
            sql.append(" WHERE id = ?");
            params.add(mediaId);

            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }

                int updated = stmt.executeUpdate();
                if (updated > 0) {
                    sendSuccess(exchange, 200, "Media updated successfully");
                } else {
                    sendError(exchange, 404, "Media not found");
                }
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

            String sql = "DELETE FROM media_entries WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, mediaId);

                int deleted = stmt.executeUpdate();
                if (deleted > 0) {
                    sendSuccess(exchange, 200, "Media deleted");
                } else {
                    sendError(exchange, 404, "Media not found");
                }
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
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