package mrp.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import mrp.di.DIContainer;
import mrp.service.interfaces.IUserService;
import mrp.util.JsonUtil;

import java.io.IOException;

public class UserHandler implements HttpHandler {
    private IUserService userService = DIContainer.getUserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            // Route requests based on HTTP method and path
            if ("POST".equals(method)) {
                if (path.equals("/api/users/register")) {
                    // Handle user registration
                    userService.register(exchange);
                } else if (path.equals("/api/users/login")) {
                    // Handle user authentication
                    userService.login(exchange);
                } else {
                    sendError(exchange, 404, "Not found");
                }
            }

            else if ("GET".equals(method)) {
                if (path.matches("/api/users/[^/]+/profile")) {
                    // Return user profile information
                    userService.getUserProfile(exchange);
                }
                else if (path.equals("/api/users/leaderboard")) {
                    // Return top users based on activity
                    userService.getLeaderboard(exchange);
                } else {
                    sendError(exchange, 404, "Not found");
                }
            }

            else if ("PATCH".equals(method)) {
                if (path.matches("/api/users/[^/]+/profile")) {
                    // Update user profile information
                    userService.updateUserProfile(exchange);
                } else {
                    sendError(exchange, 404, "Not found");
                }
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        String jsonResponse = "{\"error\":\"" + message + "\",\"status\":" + code + "}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, jsonResponse.length());
        exchange.getResponseBody().write(jsonResponse.getBytes());
        exchange.getResponseBody().close();
    }
}