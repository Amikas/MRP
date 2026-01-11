package mrp.server;

import com.sun.net.httpserver.HttpServer;
import mrp.di.DIContainer;
import mrp.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MRPServer {
    private static final int PORT = 8080;

    /**
     * Starts the MRP server on the configured port and sets up all API endpoints
     */
    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Initialize all the API handlers
        UserHandler userHandler = new UserHandler();
        MediaHandler mediaHandler = new MediaHandler();
        RatingHandler ratingHandler = new RatingHandler();
        FavoriteHandler favoriteHandler = new FavoriteHandler();
        RecommendationHandler recommendationHandler = new RecommendationHandler();

        // Map each endpoint to its corresponding handler
        server.createContext("/api/users", userHandler::handle);
        server.createContext("/api/media", mediaHandler::handle);
        server.createContext("/api/ratings", ratingHandler::handle);
        server.createContext("/api/favorites", favoriteHandler::handle);
        server.createContext("/api/recommendations", recommendationHandler::handle);

        // Start the server with default executor
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + PORT);
    }
}