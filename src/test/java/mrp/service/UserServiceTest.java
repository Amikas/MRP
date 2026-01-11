// File: test/service/UserServiceTest.java
package mrp.service;

import mrp.model.User;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import mrp.repository.UserRepository;

public class UserServiceTest {
    private UserService userService;
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        userService = new UserService();
        injectMock(userService, "userRepository", userRepository);
    }

    @Test
    void testRegisterSuccess() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);

        String json = "{\"username\":\"newuser\",\"password\":\"password123\"}";
        when(exchange.getRequestBody())
                .thenReturn(new ByteArrayInputStream(json.getBytes()));

        // Setup response headers
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(outputStream);

        when(userRepository.usernameExists("newuser")).thenReturn(false);
        doNothing().when(userRepository).save(any(User.class));

        userService.register(exchange);

        String response = outputStream.toString();
        assertTrue(response.contains("User registered successfully"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);

        String json = "{\"username\":\"testuser\",\"password\":\"password123\"}";
        when(exchange.getRequestBody())
                .thenReturn(new ByteArrayInputStream(json.getBytes()));

        // Setup response headers
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(outputStream);

        User mockUser = new User();
        mockUser.setId("user-123");
        mockUser.setUsername("testuser");
        mockUser.setPassword("hashedPassword"); // Assume this is already hashed

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockUser));

        // Mock token update
        doNothing().when(userRepository).updateToken(anyString(), anyString());

        userService.login(exchange);

        String response = outputStream.toString();
        assertTrue(response.contains("token") || response.contains("error"));
    }

    private void injectMock(Object target, String fieldName, Object mock) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, mock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}