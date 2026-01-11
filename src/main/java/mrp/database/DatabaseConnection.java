package mrp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/mrp";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    // Load driver once
    static {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load PostgreSQL JDBC driver", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return createConnection();
    }

    public static Connection createConnection() throws SQLException {
        try {
            // Test connection with a short timeout
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            // Set reasonable timeouts
            conn.setNetworkTimeout(java.util.concurrent.Executors.newSingleThreadExecutor(), 5000);

            // Simple validation
            if (conn.isValid(2)) {
                return conn;
            } else {
                throw new SQLException("Connection is not valid");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            System.err.println("URL: " + URL);
            System.err.println("User: " + USER);

            // Try one more time after a short delay
            try {
                Thread.sleep(100);
                return DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (Exception ex) {
                throw new SQLException("Failed to connect after retry: " + ex.getMessage(), ex);
            }
        }
    }

    // Simple connection test
    public static boolean testConnection() {
        try (Connection conn = createConnection()) {
            System.out.println("Database connection test: SUCCESS");
            return true;
        } catch (SQLException e) {
            System.err.println("Database connection test: FAILED - " + e.getMessage());
            return false;
        }
    }
}