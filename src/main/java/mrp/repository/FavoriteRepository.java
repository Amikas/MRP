package mrp.repository;

import mrp.database.DatabaseConnection;
import mrp.model.MediaEntry;
import mrp.repository.interfaces.IFavoriteRepository;

import java.sql.*;
import java.util.*;

public class FavoriteRepository implements IFavoriteRepository {

    public FavoriteRepository() throws SQLException {
        
    }

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

    public void removeFavorite(String userId, String mediaId) throws SQLException {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND media_id = ?";

        try (Connection conn = DatabaseConnection.createConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, mediaId);
            stmt.executeUpdate();
        }
    }

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