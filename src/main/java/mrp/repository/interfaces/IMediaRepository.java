package mrp.repository.interfaces;

import mrp.model.MediaEntry;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IMediaRepository {
    Optional<MediaEntry> findById(String id) throws SQLException;
    List<MediaEntry> findAll() throws SQLException;
    void save(MediaEntry media) throws SQLException;
    void update(MediaEntry media) throws SQLException;
    void delete(String id) throws SQLException;
    List<MediaEntry> search(String title, String genre, String mediaType, Integer minYear, Integer maxYear,
                           Integer maxAgeRestriction, Double minRating, String sortBy, String sortOrder) throws SQLException;
}