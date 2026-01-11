package mrp.service.interfaces;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Optional;

public interface IUserService {
    void register(HttpExchange exchange) throws IOException;
    void login(HttpExchange exchange) throws IOException;
    void getUserProfile(HttpExchange exchange) throws IOException;
    void updateUserProfile(HttpExchange exchange) throws IOException;
    void getLeaderboard(HttpExchange exchange) throws IOException;
    boolean validateToken(String token);
    String getUserIdFromToken(String token);
    Optional<mrp.model.User> getUserFromToken(String token);
    String getUserIdFromExchange(HttpExchange exchange);
    boolean isAuthenticated(HttpExchange exchange);
}