package mrp.server;

import com.sun.net.httpserver.HttpServer;
import mrp.handler.UserHandler;
import mrp.handler.MediaHandler;
import java.io.IOException;
import java.net.InetSocketAddress;

public class MRPServer {
    private static final int PORT = 8080;

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Create handlers
        UserHandler userHandler = new UserHandler();
        MediaHandler mediaHandler = new MediaHandler();


        // Register endpoints
        server.createContext("/api/users", userHandler::handle);
        server.createContext("/api/media", mediaHandler::handle);

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + PORT);
    }
}