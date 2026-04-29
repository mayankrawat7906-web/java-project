package services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC Connection Manager for MySQL.
 */
public class DatabaseManager {
    
    private static final String URL = "jdbc:mysql://localhost:3306/environmental_system?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "16june2005";
    
    static {
        try {
            // Register JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found. Please add mysql-connector-j jar to the classpath.");
            e.printStackTrace();
        }
    }

    /**
     * @return a new connection to the MySQL database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    /**
     * Utility method to test connectivity.
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("[OK] Connected to MySQL successfully.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Connection failed: " + e.getMessage());
        }
    }
}
