package mrp.repository;

import mrp.model.MediaEntry;
import mrp.database.DatabaseConnection;

import java.sql.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class MediaRepository {
    private final Connection connection;

    public MediaRepository() throws SQLException {
        this.connection = DatabaseConnection.createConnection();
    }

    public MediaRepository(Connection connection) {
        this.connection = connection;
    }

    public Optional<MediaEntry> findById(String id) throws SQLException {
        String sql = "SELECT * FROM media_entries WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToMedia(rs));
            }
        }
        return Optional.empty();
    }

    public List<MediaEntry> findAll() throws SQLException {
        String sql = "SELECT * FROM media_entries";
        List<MediaEntry> mediaList = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                mediaList.add(mapResultSetToMedia(rs));
            }
        }
        return mediaList;
    }

    public List<MediaEntry> findByCreator(String creatorId) throws SQLException {
        String sql = "SELECT * FROM media_entries WHERE creator_id = ?";
        List<MediaEntry> mediaList = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, creatorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                mediaList.add(mapResultSetToMedia(rs));
            }
        }
        return mediaList;
    }

    public List<MediaEntry> search(String title, String genre, String mediaType,
                                   Integer minYear, Integer maxYear,
                                   Integer maxAgeRestriction, Double minRating,
                                   String sortBy, String sortOrder) throws SQLException {
        StringBuilder sql = new StringBuilder("""
        SELECT m.*, COALESCE(AVG(r.score), 0) as average_rating
        FROM media_entries m
        LEFT JOIN ratings r ON m.id = r.media_id
        WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (title != null && !title.isEmpty()) {
            sql.append(" AND LOWER(m.title) LIKE LOWER(?)");
            params.add("%" + title + "%");
        }

        if (genre != null && !genre.isEmpty()) {
            // Handle multiple genres (comma-separated)
            if (genre.contains(",")) {
                String[] genreArray = genre.split(",");
                sql.append(" AND (");
                for (int i = 0; i < genreArray.length; i++) {
                    if (i > 0) sql.append(" OR ");
                    sql.append("LOWER(m.genres) LIKE LOWER(?)");
                    params.add("%" + genreArray[i].trim() + "%");
                }
                sql.append(")");
            } else {
                sql.append(" AND LOWER(m.genres) LIKE LOWER(?)");
                params.add("%" + genre + "%");
            }
        }

        if (mediaType != null && !mediaType.isEmpty()) {
            sql.append(" AND m.media_type = ?");
            params.add(mediaType);
        }

        if (minYear != null) {
            sql.append(" AND m.release_year >= ?");
            params.add(minYear);
        }

        if (maxYear != null) {
            sql.append(" AND m.release_year <= ?");
            params.add(maxYear);
        }

        if (maxAgeRestriction != null) {
            sql.append(" AND m.age_restriction <= ?");
            params.add(maxAgeRestriction);
        }

        sql.append(" GROUP BY m.id");

        if (minRating != null && minRating > 0) {
            sql.append(" HAVING COALESCE(AVG(r.score), 0) >= ?");
            params.add(minRating);
        }

        // Add sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            String order = "ASC";
            if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
                order = "DESC";
            }

            switch (sortBy.toLowerCase()) {
                case "title":
                    sql.append(" ORDER BY m.title ").append(order);
                    break;
                case "year":
                    sql.append(" ORDER BY m.release_year ").append(order);
                    break;
                case "score":
                    sql.append(" ORDER BY average_rating ").append(order);
                    break;
                case "rating":
                    sql.append(" ORDER BY average_rating ").append(order);
                    break;
                default:
                    sql.append(" ORDER BY m.title ASC");
            }
        } else {
            // Default sorting by title
            sql.append(" ORDER BY m.title ASC");
        }

        // Optional: Add LIMIT for pagination
        // sql.append(" LIMIT ? OFFSET ?");

        List<MediaEntry> mediaList = new ArrayList<>();

        // Debug: Print SQL for testing
        System.out.println("Search SQL: " + sql.toString());
        System.out.println("Parameters: " + params);

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MediaEntry media = mapResultSetToMedia(rs);
                mediaList.add(media);
            }
        }
        return mediaList;
    }

    // Keep the existing search method for backward compatibility
    public List<MediaEntry> search(String title, String genre, String mediaType,
                                   Integer minYear, Integer maxYear,
                                   Integer maxAgeRestriction, Double minRating) throws SQLException {
        return search(title, genre, mediaType, minYear, maxYear, maxAgeRestriction, minRating, null, null);
    }

    public void save(MediaEntry media) throws SQLException {
        String sql = "INSERT INTO media_entries (id, title, description, media_type, release_year, genres, age_restriction, creator_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, media.getId());
            stmt.setString(2, media.getTitle());
            stmt.setString(3, media.getDescription());
            stmt.setString(4, media.getMediaType());
            stmt.setInt(5, media.getReleaseYear());
            stmt.setString(6, String.join(",", media.getGenres()));            stmt.setInt(7, media.getAgeRestriction());
            stmt.setString(8, media.getCreatorId());
            stmt.executeUpdate();
        }
    }

    public void update(MediaEntry media) throws SQLException {
        String sql = "UPDATE media_entries SET title = ?, description = ?, media_type = ?, release_year = ?, genres = ?, age_restriction = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, media.getTitle());
            stmt.setString(2, media.getDescription());
            stmt.setString(3, media.getMediaType());
            stmt.setInt(4, media.getReleaseYear());
            stmt.setString(5, String.join(",", media.getGenres()));
            stmt.setInt(6, media.getAgeRestriction());
            stmt.setString(7, media.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM media_entries WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private MediaEntry mapResultSetToMedia(ResultSet rs) throws SQLException {
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

        // Add average rating if it exists in the result set
        try {
            double avgRating = rs.getDouble("average_rating");
            if (!rs.wasNull()) {
                media.setAverageRating(avgRating);
            }
        } catch (SQLException e) {
            // Column might not exist in all queries, ignore
        }

        return media;
    }
}
