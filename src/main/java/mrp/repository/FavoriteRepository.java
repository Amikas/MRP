package mrp.repository;

import mrp.database.DatabaseConnection;
import mrp.model.MediaEntry;
import mrp.repository.interfaces.IFavoriteRepository;

import java.sql.*;
import java.util.*;

public class FavoriteRepository implements IFavoriteRepository {

    public FavoriteRepository() throws SQLException {
        // No initialization needed, we'll use DatabaseConnection.createConnection()
    }

    /**
     * Add a media entry to user's favorites
     */
    public void addFavorite(String userId, String mediaId) throws SQLException {
        String sql = "INSERT INTO favorites (id, user_id, media_id) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.createConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, userId);
            stmt.setString(3, mediaId);
            stmt.executeUpdate();
        }
    }

    /**
     * Remove a media entry from user's favorites
     */
    public void removeFavorite(String userId, String mediaId) throws SQLException {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND media_id = ?";

        try (Connection conn = DatabaseConnection.createConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, mediaId);
            stmt.executeUpdate();
        }
    }

    /**
     * Check if a media entry is in user's favorites
     */
    public boolean isFavorite(String userId, String mediaId) throws SQLException {
        String sql = "SELECT id FROM favorites WHERE user_id = ? AND media_id = ?";

        try (Connection conn = DatabaseConnection.createConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, mediaId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Get count of users who favorited a media entry
     */
    public int getFavoriteCount(String mediaId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM favorites WHERE media_id = ?";

        try (Connection conn = DatabaseConnection.createConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mediaId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
                return 0;
            }
        }
    }

    /**
     * Get all favorite media entries for a user
     */
    public List<MediaEntry> getUserFavorites(String userId) throws SQLException {
        String sql = "SELECT me.* FROM media_entries me " +
                "JOIN favorites f ON me.id = f.media_id " +
                "WHERE f.user_id = ? " +
                "ORDER BY f.created_at DESC";

        List<MediaEntry> favorites = new ArrayList<>();

        try (Connection conn = DatabaseConnection.createConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    favorites.add(mapRowToMediaEntry(rs));
                }
            }
        }

        return favorites;
    }

    /**
     * Helper method to map ResultSet row to MediaEntry
     */
    private MediaEntry mapRowToMediaEntry(ResultSet rs) throws SQLException {
        MediaEntry media = new MediaEntry();
        media.setId(rs.getString("id"));
        media.setTitle(rs.getString("title"));
        media.setDescription(rs.getString("description"));
        media.setMediaType(rs.getString("media_type"));
        media.setReleaseYear(rs.getInt("release_year"));

        String genresStr = rs.getString("genres");
        if (genresStr != null && !genresStr.isEmpty()) {
            media.setGenres(Arrays.asList(genresStr.split(",")));
        } else {
            media.setGenres(new ArrayList<>());
        }

        media.setAgeRestriction(rs.getInt("age_restriction"));
        media.setCreatorId(rs.getString("creator_id"));

        return media;
    }
}