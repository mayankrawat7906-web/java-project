package services;

import java.sql.Connection;
import java.sql.Statement;

public class DbResetUtility {
    public static void main(String[] args) {
        resetDatabase();
    }

    public static void resetDatabase() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Resetting database...");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("TRUNCATE TABLE reports");
            stmt.execute("TRUNCATE TABLE workers");
            stmt.execute("TRUNCATE TABLE users");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            System.out.println("[OK] All data erased successfully.");
            
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to reset database: " + e.getMessage());
        }
    }
}
