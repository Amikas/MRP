package mrp.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import mrp.di.DIContainer;
import mrp.service.interfaces.IFavoriteService;

import java.io.IOException;

public class FavoriteHandler implements HttpHandler {
    private IFavoriteService favoriteService;

    public FavoriteHandler() {
        this.favoriteService = DIContainer.getFavoriteService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            // POST /api/favorites/{mediaId} - Add to favorites
            if (method.equals("POST") && path.matches(".*/api/favorites/[^/]+$")) {
                favoriteService.addFavorite(exchange);
            }
            // DELETE /api/favorites/{mediaId} - Remove from favorites
            else if (method.equals("DELETE") && path.matches(".*/api/favorites/[^/]+$")) {
                favoriteService.removeFavorite(exchange);
            }
            // GET /api/favorites - Get all user's favorites
            else if (method.equals("GET") && path.endsWith("/api/favorites")) {
                favoriteService.getUserFavorites(exchange);
            }
            // GET /api/favorites/{mediaId}/check - Check if favorite
            else if (method.equals("GET") && path.matches(".*/api/favorites/[^/]+/check$")) {
                favoriteService.checkIsFavorite(exchange);
            }
            // GET /api/media/{mediaId}/favorite-count - Get favorite count
            else if (method.equals("GET") && path.matches(".*/api/media/[^/]+/favorite-count$")) {
                favoriteService.getMediaFavoriteCount(exchange);
            }
            else {
                sendError(exchange, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        String response = "{\"error\":\"" + message + "\",\"status\":\"" + code + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.length());
        try (var os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}