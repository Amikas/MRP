package mrp.service.interfaces;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public interface IFavoriteService {
    void addFavorite(HttpExchange exchange) throws IOException;
    void removeFavorite(HttpExchange exchange) throws IOException;
    void getUserFavorites(HttpExchange exchange) throws IOException;
    void getMediaFavorites(HttpExchange exchange) throws IOException;
    void checkIsFavorite(HttpExchange exchange) throws IOException;
    void getMediaFavoriteCount(HttpExchange exchange) throws IOException;
    boolean isAuthenticated(HttpExchange exchange);
}