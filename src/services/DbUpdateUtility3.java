package services;
import java.sql.*;

public class DbUpdateUtility3 {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/environmental_system?useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "16june2005";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Adding penalties column to users table...");
            stmt.execute("ALTER TABLE users ADD COLUMN penalties INT DEFAULT 0");
            System.out.println("[SUCCESS] Penalties column added.");
            
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate column name")) {
                System.out.println("[INFO] Column already exists.");
            } else {
                System.err.println("[ERROR] Failed to update database: " + e.getMessage());
            }
        }
    }
}
