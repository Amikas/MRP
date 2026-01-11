package mrp.service;

import mrp.model.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RatingServiceTest {

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingService = new RatingService();
    }

    @Test
    void testRatingServiceInitialization() {
        // Test that the service can be created without exceptions
        assertNotNull(ratingService);
        assertDoesNotThrow(() -> new RatingService());
    }

    @Test
    void testRatingModel() {
        // Test the Rating model directly
        Rating rating = new Rating();
        
        // Test default values
        assertNull(rating.getId());
        assertNull(rating.getMediaId());
        assertNull(rating.getUserId());
        assertEquals(0, rating.getScore());
        assertNull(rating.getComment());
        assertFalse(rating.isCommentPublic());
        
        // Test setters and getters
        rating.setId("rating123");
        rating.setMediaId("media123");
        rating.setUserId("user123");
        rating.setScore(5);
        rating.setComment("Great movie!");
        rating.setCommentPublic(true);
        
        assertEquals("rating123", rating.getId());
        assertEquals("media123", rating.getMediaId());
        assertEquals("user123", rating.getUserId());
        assertEquals(5, rating.getScore());
        assertEquals("Great movie!", rating.getComment());
        assertTrue(rating.isCommentPublic());
    }
}