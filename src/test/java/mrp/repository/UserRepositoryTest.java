package mrp.repository;

import mrp.model.User;
import mrp.repository.interfaces.IUserRepository;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.util.Optional;

public class UserRepositoryTest {


    @Test
    void testFindByUsernameReturnsEmptyWhenUserNotFound() throws SQLException {
        IUserRepository userRepository = new UserRepository();
        
        // This test assumes the database is empty or doesn't have this user
        Optional<User> user = userRepository.findByUsername("nonexistent_user");
        
        assertNotNull(user);
        assertFalse(user.isPresent());
    }

    @Test
    void testFindByIdReturnsEmptyWhenUserNotFound() throws SQLException {
        IUserRepository userRepository = new UserRepository();
        
        // This test assumes the database is empty or doesn't have this user
        Optional<User> user = userRepository.findById("nonexistent_id");
        
        assertNotNull(user);
        assertFalse(user.isPresent());
    }


    @Test
    void testUserRepositoryMethodsExist() throws SQLException {
        IUserRepository userRepository = new UserRepository();
        
        // Test that all required methods exist and can be called (without necessarily succeeding due to DB setup)
        assertAll("Repository methods exist",
            () -> assertDoesNotThrow(() -> userRepository.findByUsername("test")),
            () -> assertDoesNotThrow(() -> userRepository.findById("test")),
            () -> assertDoesNotThrow(() -> userRepository.findByToken("test")),
            () -> assertDoesNotThrow(() -> userRepository.usernameExists("test")),
            () -> assertDoesNotThrow(() -> userRepository.getLeaderboard()),
            () -> assertDoesNotThrow(() -> userRepository.getUserStats("test"))
        );
    }
}