package mrp.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import mrp.service.UserService;
import mrp.util.JsonUtil;

import java.io.IOException;

public class UserHandler implements HttpHandler {
    private UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("POST".equals(method)) {
                if (path.equals("/api/users/register")) {
                    userService.register(exchange);
                } else if (path.equals("/api/users/login")) {
                    userService.login(exchange);
                } else {
                    sendError(exchange, 404, "Not found");
                }
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error");
        }
    }

    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        exchange.sendResponseHeaders(code, message.length());
        exchange.getResponseBody().write(message.getBytes());
        exchange.getResponseBody().close();
    }
}