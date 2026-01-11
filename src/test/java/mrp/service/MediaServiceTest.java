// File: test/service/MediaServiceTest.java
package mrp.service;

import mrp.model.MediaEntry;
import mrp.repository.MediaRepository;
import mrp.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class MediaServiceTest {
    private MediaService mediaService;

    // Mock the repositories manually (no annotations, no ByteBuddy issues)
    private MediaRepository mediaRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        // Create mocks manually
        mediaRepository = mock(MediaRepository.class);
        userRepository = mock(UserRepository.class);

        // Create service and inject mocks via reflection
        mediaService = new MediaService();
        injectMock(mediaService, "mediaRepository", mediaRepository);
        injectMock(mediaService, "userRepository", userRepository);
    }

    @Test
    void testCreateMediaValid() throws Exception {
        // Mock HttpExchange
        HttpExchange exchange = mock(HttpExchange.class);

        // Setup request headers with token
        com.sun.net.httpserver.Headers headers = new com.sun.net.httpserver.Headers();
        headers.add("Authorization", "Bearer test-token");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        // Setup response headers
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());

        // Setup request body
        String json = """
            {
                "title": "The Matrix",
                "mediaType": "movie",
                "description": "A computer hacker learns about reality",
                "releaseYear": 1999,
                "genres": ["Action", "Sci-Fi"],
                "ageRestriction": 13
            }
            """;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes());
        when(exchange.getRequestBody()).thenReturn(inputStream);

        // Setup response
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(outputStream);

        // Mock userRepository to return a valid user
        mrp.model.User mockUser = new mrp.model.User();
        mockUser.setId("user-123");
        when(userRepository.findByToken("test-token")).thenReturn(Optional.of(mockUser));

        // Mock mediaRepository.save() to do nothing
        doNothing().when(mediaRepository).save(any(MediaEntry.class));

        // Execute
        mediaService.createMedia(exchange);

        // Verify media was saved
        verify(mediaRepository, times(1)).save(any(MediaEntry.class));

        // Check response (simplified)
        String response = outputStream.toString();
        assertNotNull(response);
    }

    @Test
    void testGetMedia() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);

        // Setup path
        when(exchange.getRequestURI()).thenReturn(new java.net.URI("/api/media/media-123"));

        // Setup response headers
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());

        // Setup response
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(outputStream);

        // Mock repository response
        MediaEntry mockMedia = new MediaEntry();
        mockMedia.setId("media-123");
        mockMedia.setTitle("Test Movie");
        when(mediaRepository.findById("media-123")).thenReturn(Optional.of(mockMedia));

        // Execute
        mediaService.getMedia(exchange);

        // Verify
        String response = outputStream.toString();
        assertTrue(response.contains("Test Movie"));
    }

    private void injectMock(Object target, String fieldName, Object mock) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, mock);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock into " + fieldName, e);
        }
    }
}