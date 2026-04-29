package services;
import java.sql.*;

public class DbUpdateUtility {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/environmental_system?useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "16june2005";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Connecting to database...");
            stmt.execute("ALTER TABLE users ADD COLUMN is_blocked BOOLEAN DEFAULT FALSE");
            System.out.println("[SUCCESS] Column 'is_blocked' added to users table.");
            
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate column name")) {
                System.out.println("[INFO] Column 'is_blocked' already exists.");
            } else {
                System.err.println("[ERROR] Failed to update database: " + e.getMessage());
            }
        }
    }
}
