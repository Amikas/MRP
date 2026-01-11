package mrp.repository;

import mrp.model.Rating;
import mrp.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RatingRepository {
    private Connection connection;
    private boolean externalConnection = false;

    public RatingRepository() throws SQLException {
        this.connection = DatabaseConnection.createConnection();
        this.externalConnection = false;
    }

    public RatingRepository(Connection connection) {
        this.connection = connection;
        this.externalConnection = true;
    }

    public Optional<Rating> findById(String id) throws SQLException {
        String sql = """
            SELECT r.*, u.username, m.title as media_title,
                   (SELECT COUNT(*) FROM rating_likes rl WHERE rl.rating_id = r.id) as like_count
            FROM ratings r
            LEFT JOIN users u ON r.user_id = u.id
            LEFT JOIN media_entries m ON r.media_id = m.id
            WHERE r.id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRating(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Rating> findByUserAndMedia(String userId, String mediaId) throws SQLException {
        String sql = """
            SELECT r.*, u.username, m.title as media_title,
                   (SELECT COUNT(*) FROM rating_likes rl WHERE rl.rating_id = r.id) as like_count
            FROM ratings r
            LEFT JOIN users u ON r.user_id = u.id
            LEFT JOIN media_entries m ON r.media_id = m.id
            WHERE r.user_id = ? AND r.media_id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, mediaId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRating(rs));
            }
        }
        return Optional.empty();
    }

    public List<Rating> findByUserId(String userId) throws SQLException {
        String sql = """
            SELECT r.*, u.username, m.title as media_title,
                   (SELECT COUNT(*) FROM rating_likes rl WHERE rl.rating_id = r.id) as like_count
            FROM ratings r
            LEFT JOIN users u ON r.user_id = u.id
            LEFT JOIN media_entries m ON r.media_id = m.id
            WHERE r.user_id = ?
            ORDER BY r.created_at DESC
            """;

        List<Rating> ratings = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ratings.add(mapResultSetToRating(rs));
            }
        }
        return ratings;
    }

    public List<Rating> findByMediaId(String mediaId) throws SQLException {
        String sql = """
            SELECT r.*, u.username, m.title as media_title,
                   (SELECT COUNT(*) FROM rating_likes rl WHERE rl.rating_id = r.id) as like_count
            FROM ratings r
            LEFT JOIN users u ON r.user_id = u.id
            LEFT JOIN media_entries m ON r.media_id = m.id
            WHERE r.media_id = ?
            ORDER BY r.created_at DESC
            """;

        List<Rating> ratings = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mediaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ratings.add(mapResultSetToRating(rs));
            }
        }
        return ratings;
    }

    public void save(Rating rating) throws SQLException {
        String sql = """
            INSERT INTO ratings (id, media_id, user_id, score, comment, is_comment_public, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            rating.setId(UUID.randomUUID().toString());
            LocalDateTime now = LocalDateTime.now();
            rating.setCreatedAt(now);
            rating.setUpdatedAt(now);

            stmt.setString(1, rating.getId());
            stmt.setString(2, rating.getMediaId());
            stmt.setString(3, rating.getUserId());
            stmt.setInt(4, rating.getScore());
            stmt.setString(5, rating.getComment());
            stmt.setBoolean(6, rating.isCommentPublic());
            stmt.setTimestamp(7, Timestamp.valueOf(rating.getCreatedAt()));
            stmt.setTimestamp(8, Timestamp.valueOf(rating.getUpdatedAt()));

            stmt.executeUpdate();
        }
    }

    public void update(Rating rating) throws SQLException {
        String sql = """
            UPDATE ratings 
            SET score = ?, comment = ?, is_comment_public = ?, updated_at = ?
            WHERE id = ? AND user_id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            rating.setUpdatedAt(LocalDateTime.now());

            stmt.setInt(1, rating.getScore());
            stmt.setString(2, rating.getComment());
            stmt.setBoolean(3, rating.isCommentPublic());
            stmt.setTimestamp(4, Timestamp.valueOf(rating.getUpdatedAt()));
            stmt.setString(5, rating.getId());
            stmt.setString(6, rating.getUserId());

            stmt.executeUpdate();
        }
    }

    public void delete(String id, String userId) throws SQLException {
        String sql = "DELETE FROM ratings WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        }
    }

    public boolean hasUserLikedRating(String userId, String ratingId) throws SQLException {
        String sql = "SELECT 1 FROM rating_likes WHERE user_id = ? AND rating_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, ratingId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public void addLike(String userId, String ratingId) throws SQLException {
        String sql = "INSERT INTO rating_likes (id, user_id, rating_id, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, userId);
            stmt.setString(3, ratingId);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }
    }

    public void removeLike(String userId, String ratingId) throws SQLException {
        String sql = "DELETE FROM rating_likes WHERE user_id = ? AND rating_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, ratingId);
            stmt.executeUpdate();
        }
    }

    public double getAverageRatingForMedia(String mediaId) throws SQLException {
        String sql = "SELECT COALESCE(AVG(score), 0) as avg_rating FROM ratings WHERE media_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mediaId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        }
        return 0.0;
    }

    public int getRatingCountForMedia(String mediaId) throws SQLException {
        String sql = "SELECT COUNT(*) as rating_count FROM ratings WHERE media_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mediaId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("rating_count");
            }
        }
        return 0;
    }

    public void close() throws SQLException {
        if (!externalConnection && connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private Rating mapResultSetToRating(ResultSet rs) throws SQLException {
        Rating rating = new Rating();
        rating.setId(rs.getString("id"));
        rating.setMediaId(rs.getString("media_id"));
        rating.setUserId(rs.getString("user_id"));
        rating.setScore(rs.getInt("score"));
        rating.setComment(rs.getString("comment"));
        rating.setCommentPublic(rs.getBoolean("is_comment_public"));

        // Handle nullable timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null && !rs.wasNull()) {
            rating.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null && !rs.wasNull()) {
            rating.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        // Additional fields (handle nulls)
        String username = rs.getString("username");
        if (username != null && !rs.wasNull()) {
            rating.setUsername(username);
        }

        String mediaTitle = rs.getString("media_title");
        if (mediaTitle != null && !rs.wasNull()) {
            rating.setMediaTitle(mediaTitle);
        }

        int likeCount = rs.getInt("like_count");
        if (!rs.wasNull()) {
            rating.setLikeCount(likeCount);
        }

        return rating;
    }
}