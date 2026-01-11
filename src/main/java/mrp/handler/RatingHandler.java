package mrp.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import mrp.service.RatingService;

import java.io.IOException;

public class RatingHandler implements HttpHandler {
    private final RatingService ratingService = new RatingService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        try {
            // Debug: Print what path we're receiving
            System.out.println("RatingHandler received: " + method + " " + path);

            // Since context is "/api/ratings", the path will be like "/my", "/stats", etc.
            // Remove the base "/api/ratings" part if it exists
            if (path.startsWith("/api/ratings")) {
                path = path.substring("/api/ratings".length());
                if (path.isEmpty()) {
                    path = "/";
                }
            }

            System.out.println("Processed path: " + path);

            // IMPORTANT: Check specific endpoints FIRST before general patterns

            // Handle user's own ratings - CHECK THIS FIRST
            if (path.equals("/my") || path.equals("/my/")) {
                // Handle user's own ratings
                System.out.println("Getting user's own ratings");
                if ("GET".equals(method)) {
                    ratingService.getUserRatings(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
                return; // Add return to exit after handling
            }

            // Handle rating statistics - CHECK THIS SECOND
            if (path.equals("/stats") || path.equals("/stats/")) {
                // Handle rating statistics
                System.out.println("Getting rating statistics");
                if ("GET".equals(method)) {
                    ratingService.getMediaRatingStats(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
                return; // Add return to exit after handling
            }

            // Handle like/unlike - path like "/{ratingId}/like"
            if (path.matches("/[^/]+/like")) {
                String ratingId = path.substring(1, path.lastIndexOf("/like"));
                System.out.println("Like/unlike for ratingId: " + ratingId);

                if ("POST".equals(method)) {
                    ratingService.likeRating(exchange);
                } else if ("DELETE".equals(method)) {
                    ratingService.unlikeRating(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
                return; // Add return to exit after handling
            }

            // Handle comment confirmation - path like "/{ratingId}/confirm-comment"
            if (path.matches("/[^/]+/confirm-comment")) {
                String ratingId = path.substring(1, path.lastIndexOf("/confirm-comment"));
                System.out.println("Confirm comment for ratingId: " + ratingId);

                if ("PATCH".equals(method)) {
                    ratingService.confirmComment(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
                return; // Add return to exit after handling
            }

            // Handle individual rating operations - path like "/{ratingId}"
            if (path.matches("/[^/]+")) {
                String ratingId = path.substring(1);
                System.out.println("Individual rating operation for ratingId: " + ratingId);

                switch (method) {
                    case "GET":
                        ratingService.getRating(exchange);
                        break;
                    case "PATCH":
                        ratingService.updateRating(exchange);
                        break;
                    case "DELETE":
                        ratingService.deleteRating(exchange);
                        break;
                    default:
                        sendError(exchange, 405, "Method not allowed");
                }
                return; // Add return to exit after handling
            }

            // Handle ratings collection (root path)
            if (path.equals("/") || path.isEmpty()) {
                System.out.println("Ratings collection endpoint");
                switch (method) {
                    case "GET":
                        // If query has mediaId, get ratings for that media
                        if (query != null && query.contains("mediaId=")) {
                            ratingService.getMediaRatings(exchange);
                        } else {
                            sendError(exchange, 400, "Provide mediaId query parameter");
                        }
                        break;
                    case "POST":
                        ratingService.createRating(exchange);
                        break;
                    default:
                        sendError(exchange, 405, "Method not allowed");
                }
                return; // Add return to exit after handling
            }

            // If we get here, no route matched
            sendError(exchange, 404, "Endpoint not found: " + path);

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        String response = "{\"error\":\"" + message + "\",\"status\":" + code + "}";
        exchange.sendResponseHeaders(code, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }
}