package mrp.di;

import mrp.repository.interfaces.*;
import mrp.service.interfaces.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class DIContainerTest {

    @Test
    void testDIContainerInitialization() {
        // Test that the DI container initializes without throwing exceptions
        assertDoesNotThrow(() -> {
            // Accessing any getter will trigger static initialization
            IUserRepository userRepository = DIContainer.getUserRepository();
            assertNotNull(userRepository);
        });
    }

    @Test
    void testGetUserService() {
        IUserService userService = DIContainer.getUserService();
        assertNotNull(userService);
        assertTrue(userService instanceof mrp.service.UserService);
    }

    @Test
    void testGetMediaService() {
        IMediaService mediaService = DIContainer.getMediaService();
        assertNotNull(mediaService);
        assertTrue(mediaService instanceof mrp.service.MediaService);
    }

    @Test
    void testGetRatingService() {
        IRatingService ratingService = DIContainer.getRatingService();
        assertNotNull(ratingService);
        assertTrue(ratingService instanceof mrp.service.RatingService);
    }

    @Test
    void testGetAllServicesAreNotNull() {
        // Test that all services can be retrieved without null values
        assertNotNull(DIContainer.getUserService());
        assertNotNull(DIContainer.getMediaService());
        assertNotNull(DIContainer.getRatingService());
        assertNotNull(DIContainer.getFavoriteService());
        assertNotNull(DIContainer.getRecommendationService());
    }
}