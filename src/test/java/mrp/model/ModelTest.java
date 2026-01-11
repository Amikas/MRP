// File: test/model/ModelTest.java
package mrp.model;

import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {

    @Test
    void testMediaEntryCreation() {
        MediaEntry media = new MediaEntry();
        media.setId("123");
        media.setTitle("Test Movie");
        media.setMediaType("movie");
        media.setReleaseYear(2023);
        media.setGenres(Arrays.asList("Action", "Adventure"));

        assertEquals("123", media.getId());
        assertEquals("Test Movie", media.getTitle());
        assertEquals("movie", media.getMediaType());
        assertEquals(2, media.getGenres().size());
    }

    @Test
    void testRatingCreation() {
        Rating rating = new Rating();
        rating.setId("rating-1");
        rating.setScore(4);
        rating.setComment("Great movie!");
        rating.setCommentPublic(true);

        assertEquals("rating-1", rating.getId());
        assertEquals(4, rating.getScore());
        assertTrue(rating.isCommentPublic());
        assertEquals("Great movie!", rating.getComment());
    }

    @Test
    void testUserCreation() {
        User user = new User();
        user.setId("user-123");
        user.setUsername("john_doe");
        user.setPassword("hashedpassword");
        user.setToken("token-abc");

        assertEquals("user-123", user.getId());
        assertEquals("john_doe", user.getUsername());
        assertEquals("hashedpassword", user.getPassword());
        assertEquals("token-abc", user.getToken());
    }
}