package mrp.repository.interfaces;

import mrp.model.MediaEntry;

import java.sql.SQLException;
import java.util.List;

public interface IFavoriteRepository {
    void addFavorite(String userId, String mediaId) throws SQLException;
    void removeFavorite(String userId, String mediaId) throws SQLException;
    boolean isFavorite(String userId, String mediaId) throws SQLException;
    int getFavoriteCount(String mediaId) throws SQLException;
    List<MediaEntry> getUserFavorites(String userId) throws SQLException;
}