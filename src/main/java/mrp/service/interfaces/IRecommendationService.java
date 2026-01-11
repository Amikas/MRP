package mrp.service.interfaces;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public interface IRecommendationService {
    void getRecommendations(HttpExchange exchange) throws IOException;
    void getSimilarMedia(HttpExchange exchange) throws IOException;
    boolean isAuthenticated(HttpExchange exchange);
}