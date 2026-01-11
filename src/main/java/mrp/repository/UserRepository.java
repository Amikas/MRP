package mrp.repository;

import mrp.model.User;
import mrp.database.DatabaseConnection;

import java.sql.*;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class UserRepository {
    private final Connection connection;

    public UserRepository() throws SQLException {
        this.connection = DatabaseConnection.createConnection();
    }

    public UserRepository(Connection connection) {
        this.connection = connection;
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, token FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setToken(rs.getString("token"));
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(String id) throws SQLException {
        String sql = "SELECT id, username, password, token FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setToken(rs.getString("token"));
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByToken(String token) throws SQLException {
        String sql = "SELECT id, username, password, token FROM users WHERE token = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setToken(rs.getString("token"));
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (id, username, password, token) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getToken());
            stmt.executeUpdate();
        }
    }

    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ?, token = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getToken());
            stmt.setString(4, user.getId());
            stmt.executeUpdate();
        }
    }

    public void updateToken(String userId, String token) throws SQLException {
        String sql = "UPDATE users SET token = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        }
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Other methods remain the same...
    public List<Map<String, Object>> getLeaderboard() throws SQLException {
        String sql = """
            SELECT 
                u.username,
                COUNT(r.id) as rating_count,
                COALESCE(AVG(r.score), 0) as avg_score,
                COUNT(DISTINCT f.id) as favorite_count,
                COUNT(DISTINCT rl.id) as likes_given
            FROM users u
            LEFT JOIN ratings r ON u.id = r.user_id
            LEFT JOIN favorites f ON u.id = f.user_id
            LEFT JOIN rating_likes rl ON u.id = rl.user_id
            GROUP BY u.id, u.username
            ORDER BY rating_count DESC, avg_score DESC
            LIMIT 20
            """;

        List<Map<String, Object>> leaderboard = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> entry = new java.util.HashMap<>();
                entry.put("username", rs.getString("username"));
                entry.put("ratingCount", rs.getInt("rating_count"));
                entry.put("averageScore", rs.getDouble("avg_score"));
                entry.put("favoriteCount", rs.getInt("favorite_count"));
                entry.put("likesGiven", rs.getInt("likes_given"));
                leaderboard.add(entry);
            }
        }
        return leaderboard;
    }

    public Map<String, Object> getUserStats(String userId) throws SQLException {
        String sql = """
            SELECT 
                u.username,
                COUNT(r.id) as total_ratings,
                COALESCE(AVG(r.score), 0) as average_score,
                COUNT(DISTINCT f.id) as total_favorites,
                COUNT(DISTINCT rl.id) as total_likes_given,
                (
                    SELECT STRING_AGG(DISTINCT genres, ',')
                    FROM media_entries m
                    JOIN ratings r2 ON m.id = r2.media_id
                    WHERE r2.user_id = u.id AND r2.score >= 4
                ) as favorite_genres,
                (
                    SELECT COUNT(*)
                    FROM rating_likes rl2
                    JOIN ratings r3 ON rl2.rating_id = r3.id
                    WHERE r3.user_id = u.id
                ) as likes_received
            FROM users u
            LEFT JOIN ratings r ON u.id = r.user_id
            LEFT JOIN favorites f ON u.id = f.user_id
            LEFT JOIN rating_likes rl ON u.id = rl.user_id
            WHERE u.id = ?
            GROUP BY u.id, u.username
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> stats = new java.util.HashMap<>();
                stats.put("username", rs.getString("username"));
                stats.put("totalRatings", rs.getInt("total_ratings"));
                stats.put("averageScore", rs.getDouble("average_score"));
                stats.put("totalFavorites", rs.getInt("total_favorites"));
                stats.put("totalLikesGiven", rs.getInt("total_likes_given"));
                stats.put("likesReceived", rs.getInt("likes_received"));
                stats.put("favoriteGenres", rs.getString("favorite_genres"));
                return stats;
            }
        }
        return new java.util.HashMap<>();
    }
}
