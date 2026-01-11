package mrp.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import mrp.di.DIContainer;
import mrp.service.interfaces.IRecommendationService;

import java.io.IOException;

public class RecommendationHandler implements HttpHandler {
    private final IRecommendationService recommendationService;

    public RecommendationHandler() {
        this.recommendationService = DIContainer.getRecommendationService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            
            if (method.equals("GET") && path.equals("/api/recommendations")) {
                recommendationService.getRecommendations(exchange);
            }
            
            else if (method.equals("GET") && path.matches("/api/media/[^/]+/similar")) {
                recommendationService.getSimilarMedia(exchange);
            } else {
                sendError(exchange, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        String response = "{\"error\":\"" + message + "\",\"status\":" + code + "}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.length());
        try (var os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}