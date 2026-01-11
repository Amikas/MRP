package mrp.service.interfaces;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public interface IMediaService {
    void createMedia(HttpExchange exchange) throws IOException;
    void updateMedia(HttpExchange exchange) throws IOException;
    void getMedia(HttpExchange exchange) throws IOException;
    void getAllMedia(HttpExchange exchange) throws IOException;
    void deleteMedia(HttpExchange exchange) throws IOException;
    void searchMedia(HttpExchange exchange) throws IOException;
    boolean isAuthenticated(HttpExchange exchange);
}