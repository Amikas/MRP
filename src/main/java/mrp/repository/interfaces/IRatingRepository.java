package mrp.repository.interfaces;

import mrp.model.Rating;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IRatingRepository {
    Optional<Rating> findById(String id) throws SQLException;
    Optional<Rating> findByUserAndMedia(String userId, String mediaId) throws SQLException;
    List<Rating> findByMediaId(String mediaId) throws SQLException;
    List<Rating> findByUserId(String userId) throws SQLException;
    void save(Rating rating) throws SQLException;
    void update(Rating rating) throws SQLException;
    void delete(String ratingId, String userId) throws SQLException;
    double getAverageRatingForMedia(String mediaId) throws SQLException;
    int getRatingCountForMedia(String mediaId) throws SQLException;
    boolean hasUserLikedRating(String userId, String ratingId) throws SQLException;
    void addLike(String userId, String ratingId) throws SQLException;
    void removeLike(String userId, String ratingId) throws SQLException;
}