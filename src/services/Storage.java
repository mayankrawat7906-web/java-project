package services;

import java.sql.*;
import java.util.*;
import users.User;
import users.Worker;
import reports.Report;

/**
 * JDBC-based persistence layer for MySQL.
 */
public class Storage {

    // ---------------------------------------------------------------- USERS
    /**
     * Saves a list of users to the database.
     */
    public static void saveUsers(List<User> users) {
        String sql = "INSERT INTO users (id, name, email, password, points, is_blocked, penalties) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), " +
                     "password=VALUES(password), points=VALUES(points), is_blocked=VALUES(is_blocked), penalties=VALUES(penalties)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (User u : users) {
                pstmt.setString(1, u.getId());
                pstmt.setString(2, u.getName());
                pstmt.setString(3, u.getEmail());
                pstmt.setString(4, u.getPassword());
                pstmt.setInt(5, u.getPoints());
                pstmt.setBoolean(6, u.isBlocked());
                pstmt.setInt(7, u.getPenalties());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Error saving users: " + e.getMessage());
        }
    }

    public static List<User> loadUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User u = new User(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password")
                );
                u.setPoints(rs.getInt("points"));
                u.setBlocked(rs.getBoolean("is_blocked"));
                u.setPenalties(rs.getInt("penalties"));
                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Error loading users: " + e.getMessage());
        }
        return list;
    }

    // --------------------------------------------------------------- WORKERS
    public static void saveWorkers(List<Worker> workers) {
        String sql = "INSERT INTO workers (id, name, password, assigned_count, resolved_count, complaint_count) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name=VALUES(name), password=VALUES(password), " +
                     "assigned_count=VALUES(assigned_count), resolved_count=VALUES(resolved_count), " +
                     "complaint_count=VALUES(complaint_count)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (Worker w : workers) {
                pstmt.setString(1, w.getId());
                pstmt.setString(2, w.getName());
                pstmt.setString(3, w.getPassword());
                pstmt.setInt(4, w.getAssignedCount());
                pstmt.setInt(5, w.getResolvedCount());
                pstmt.setInt(6, w.getComplaintCount());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Error saving workers: " + e.getMessage());
        }
    }

    public static List<Worker> loadWorkers() {
        List<Worker> list = new ArrayList<>();
        String sql = "SELECT * FROM workers";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Worker w = new Worker(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("password")
                );
                w.setAssignedCount(rs.getInt("assigned_count"));
                w.setResolvedCount(rs.getInt("resolved_count"));
                w.setComplaintCount(rs.getInt("complaint_count"));
                list.add(w);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Error loading workers: " + e.getMessage());
        }
        return list;
    }

    public static void deleteWorker(String id) {
        String sql = "DELETE FROM workers WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERROR] Error deleting worker: " + e.getMessage());
        }
    }
    
    public static void unassignReports(String workerId) {
        String sql = "UPDATE reports SET status = 'PENDING', assigned_worker_id = NULL WHERE assigned_worker_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, workerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERROR] Error unassigning reports: " + e.getMessage());
        }
    }

    // --------------------------------------------------------------- REPORTS
    public static void saveReports(List<Report> reports) {
        String sql = "INSERT INTO reports (id, user_id, latitude, longitude, description, reported_date, status, assigned_worker_id, category, upvotes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE user_id=VALUES(user_id), latitude=VALUES(latitude), longitude=VALUES(longitude), " +
                     "description=VALUES(description), reported_date=VALUES(reported_date), status=VALUES(status), " +
                     "assigned_worker_id=VALUES(assigned_worker_id), category=VALUES(category), upvotes=VALUES(upvotes)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (Report r : reports) {
                pstmt.setString(1, r.getId());
                pstmt.setString(2, r.getUserId());
                pstmt.setDouble(3, r.getLat());
                pstmt.setDouble(4, r.getLon());
                pstmt.setString(5, r.getDescription());
                pstmt.setString(6, r.getDate());
                pstmt.setString(7, r.getStatus());
                pstmt.setString(8, r.getAssignedWorkerId());
                pstmt.setString(9, r.getCategory());
                pstmt.setInt(10, r.getUpvotes());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Error saving reports: " + e.getMessage());
        }
    }

    public static List<Report> loadReports() {
        List<Report> list = new ArrayList<>();
        String sql = "SELECT * FROM reports";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Report r = new Report(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    rs.getDouble("latitude"),
                    rs.getDouble("longitude"),
                    rs.getString("description"),
                    rs.getString("reported_date")
                );
                r.setStatus(rs.getString("status"));
                r.setAssignedWorkerId(rs.getString("assigned_worker_id"));
                r.setCategory(rs.getString("category"));
                r.setUpvotes(rs.getInt("upvotes"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Error loading reports: " + e.getMessage());
        }
        return list;
    }
}
