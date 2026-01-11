package mrp.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import mrp.di.DIContainer;
import mrp.service.interfaces.IMediaService;
import java.io.IOException;

public class MediaHandler implements HttpHandler {
    private IMediaService mediaService = DIContainer.getMediaService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {

            if (!mediaService.isAuthenticated(exchange)) {
                sendError(exchange, 401, "Unauthorized");
                return;
            }

            switch (method) {
                case "POST":
                    mediaService.createMedia(exchange);
                    break;
                case "GET":
                    if (path.matches("/api/media/[^/]+")) {
                        mediaService.getMedia(exchange);
                    } else if (path.equals("/api/media/search")) {
                        
                        mediaService.searchMedia(exchange);
                    } else {
                        mediaService.getAllMedia(exchange);
                    }
                    break;
                case "PATCH":
                    mediaService.updateMedia(exchange);
                    break;
                case "DELETE":
                    mediaService.deleteMedia(exchange);
                    break;
                default:
                    sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
            e.printStackTrace();
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