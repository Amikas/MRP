package mrp;

import mrp.di.DIContainer;
import mrp.server.MRPServer;

public class Main {
    public static void main(String[] args) {
        try {

            MRPServer server = new MRPServer();
            server.start();
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            
        }
    }
}