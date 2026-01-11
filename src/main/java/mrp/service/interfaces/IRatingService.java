package mrp.service.interfaces;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public interface IRatingService {
    void createRating(HttpExchange exchange) throws IOException;
    void updateRating(HttpExchange exchange) throws IOException;
    void deleteRating(HttpExchange exchange) throws IOException;
    void getMediaRatings(HttpExchange exchange) throws IOException;
    void getUserRatings(HttpExchange exchange) throws IOException;
    void getMediaRatingStats(HttpExchange exchange) throws IOException;
    void confirmComment(HttpExchange exchange) throws IOException;
    void likeRating(HttpExchange exchange) throws IOException;
    void unlikeRating(HttpExchange exchange) throws IOException;
    void getRating(HttpExchange exchange) throws IOException;
}