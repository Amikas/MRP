package mrp;

import mrp.di.DIContainer;
import mrp.server.MRPServer;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize the DI container
            // The DI container is initialized statically, so no explicit call needed

            MRPServer server = new MRPServer();
            server.start();
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            //e.printStackTrace();
        }
    }
}