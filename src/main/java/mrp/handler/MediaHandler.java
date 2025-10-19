package mrp.handler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import mrp.service.MediaService;
import java.io.IOException;

public class MediaHandler implements HttpHandler {
    private MediaService mediaService = new MediaService();
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
                    } else {
                        mediaService.getAllMedia(exchange);
                    }
                    break;
                case "PUT":
                    mediaService.updateMedia(exchange);
                    break;
                case "DELETE":
                    mediaService.deleteMedia(exchange);
                    break;
                default:
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
