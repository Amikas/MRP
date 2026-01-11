package mrp.repository.interfaces;

import mrp.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUserRepository {
    Optional<User> findByUsername(String username) throws SQLException;
    Optional<User> findById(String id) throws SQLException;
    Optional<User> findByToken(String token) throws SQLException;
    boolean usernameExists(String username) throws SQLException;
    void save(User user) throws SQLException;
    void update(User user) throws SQLException;
    void updateToken(String userId, String token) throws SQLException;
    void close() throws SQLException;
    List<Map<String, Object>> getLeaderboard() throws SQLException;
    Map<String, Object> getUserStats(String userId) throws SQLException;
}