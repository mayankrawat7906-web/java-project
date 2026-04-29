package reports;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import users.User;
import users.Worker;
import services.AssignmentService;

public class ReportService {
    private List<Report> reports = new ArrayList<>();
    private AssignmentService assignmentService;
    private static final int POINTS_PER_REPORT   = 10;
    private static final int POINTS_RESOLVED      = 20;
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReportService(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    public void setReports(List<Report> reports) { this.reports = reports; }
    public List<Report> getAllReports()           { return reports; }

    // ----------------------------------------------------------------
    // SUBMIT REPORT
    // ----------------------------------------------------------------
    public boolean submitReport(User user, double lat, double lon,
                                String desc, String date, String category) {
        if (!isValidDate(date)) {
            System.out.println("  [!] Invalid date format. Use YYYY-MM-DD.");
            return false;
        }
        if (desc == null || desc.trim().isEmpty()) {
            System.out.println("  [!] Description cannot be empty.");
            return false;
        }

        // Check for near-duplicate (same user, same category, within 0.1 km, same date)
        for (Report r : reports) {
            if (r.getUserId().equals(user.getId())
                    && r.getCategory().equalsIgnoreCase(category)
                    && r.getDate().equals(date)
                    && GeoUtils.haversineKm(lat, lon, r.getLat(), r.getLon()) < 0.1) {
                System.out.println("  [!] A very similar report already exists (ID: " + r.getId() + "). Skipping.");
                return false;
            }
        }

        String id = generateId();
        Report r = new Report(id, user.getId(), lat, lon, desc.trim(), date, category);
        reports.add(r);
        
        boolean assigned = assignmentService.assignReport(r);
        user.addPoints(POINTS_PER_REPORT);

        if (assigned) {
            System.out.println("  [✓] Report submitted and assigned! ID: " + id 
                    + " | You earned " + POINTS_PER_REPORT + " points.");
        } else {
            System.out.println("  [!] No worker is available and your report is submited and assign to a worker when avialable.");
            System.out.println("  [✓] Report ID: " + id + " | You earned " + POINTS_PER_REPORT + " points.");
        }
        return true;
    }

    // Backward-compatible (no category)
    public void submitReport(User user, double lat, double lon, String desc, String date) {
        submitReport(user, lat, lon, desc, date, "Other");
    }

    // ----------------------------------------------------------------
    // UPVOTE REPORT
    // ----------------------------------------------------------------
    public boolean upvoteReport(String reportId, String votingUserId) {
        for (Report r : reports) {
            if (r.getId().equals(reportId)) {
                if (r.getUserId().equals(votingUserId)) {
                    System.out.println("  [!] You cannot upvote your own report.");
                    return false;
                }
                r.addUpvote();
                System.out.println("  [✓] Upvoted report " + reportId
                        + ". Total upvotes: " + r.getUpvotes());
                return true;
            }
        }
        System.out.println("  [!] Report not found.");
        return false;
    }

    // ----------------------------------------------------------------
    // SHOW REPORTS
    // ----------------------------------------------------------------
    public void showReportsByUser(String userId) {
        List<Report> mine = new ArrayList<>();
        for (Report r : reports)
            if (r.getUserId().equals(userId)) mine.add(r);

        if (mine.isEmpty()) { System.out.println("  No reports found."); return; }
        System.out.println("\n--- Your Reports (" + mine.size() + ") ---");
        for (Report r : mine) System.out.println("  " + r);
    }

    public void showReportsByStatus(String status) {
        List<Report> filtered = new ArrayList<>();
        for (Report r : reports)
            if (r.getStatus().equals(status)) filtered.add(r);

        if (filtered.isEmpty()) {
            System.out.println("  No reports with status: " + status); return;
        }
        System.out.println("\n--- Reports [" + status + "] (" + filtered.size() + ") ---");
        for (Report r : filtered) System.out.println("  " + r);
    }

    public void showReportsByCategory(String category) {
        List<Report> filtered = new ArrayList<>();
        for (Report r : reports)
            if (r.getCategory().equalsIgnoreCase(category)) filtered.add(r);

        if (filtered.isEmpty()) {
            System.out.println("  No reports in category: " + category); return;
        }
        System.out.println("\n--- Reports [" + category + "] (" + filtered.size() + ") ---");
        for (Report r : filtered) System.out.println("  " + r);
    }

    public void showAllReportsLatestFirst() {
        if (reports.isEmpty()) { System.out.println("  No reports available."); return; }
        List<Report> copy = new ArrayList<>(reports);
        Collections.reverse(copy);
        System.out.println("\n--- All Reports (" + copy.size() + ") ---");
        for (Report r : copy) System.out.println("  " + r);
    }

    public void showReportsForWorker(String workerId) {
        List<Report> mine = new ArrayList<>();
        for (Report r : reports)
            if (workerId.equals(r.getAssignedWorkerId())) mine.add(r);

        if (mine.isEmpty()) { System.out.println("  No reports assigned to you."); return; }
        System.out.println("\n--- Your Assigned Reports (" + mine.size() + ") ---");
        for (Report r : mine) System.out.println("  " + r);
    }

    // ----------------------------------------------------------------
    // WORKER ACTIONS
    // ----------------------------------------------------------------
    public void workerMarkReport(String wid, String reportId,
                                  boolean suspicious, List<User> users, List<Worker> workers) {
        for (Report r : reports) {
            if (r.getId().equals(reportId)) {
                if (!wid.equals(r.getAssignedWorkerId())) {
                    System.out.println("  [!] This report is not assigned to you.");
                    return;
                }
                if (r.getStatus().equals(Report.RESOLVED)
                        || r.getStatus().equals(Report.CLOSED)) {
                    System.out.println("  [!] Report is already " + r.getStatus() + ".");
                    return;
                }
                String newStatus = suspicious ? Report.SUSPICIOUS : Report.RESOLVED;
                r.setStatus(newStatus);
                
                // --- BLOCK LOGIC: Strike-3 consecutive suspicious ---
                for (User u : users) {
                    if (u.getId().equals(r.getUserId())) {
                        if (suspicious) {
                            u.incrementPenalties();
                            if (u.getPenalties() >= 3) {
                                u.setBlocked(true);
                                System.out.println("  [!] User " + u.getName() + " blocked (3 penalties).");
                            }
                        } else {
                            // Reset consecutive penalties on successful report resolution
                            u.setPenalties(0);
                        }
                        services.Storage.saveUsers(users); // Persist immediately
                        break;
                    }
                }

                // Award bonus points to user if resolved
                if (!suspicious) {
                    for (User u : users) {
                        if (u.getId().equals(r.getUserId())) {
                            u.addPoints(POINTS_RESOLVED);
                            System.out.println("  [✓] User " + u.getName()
                                    + " earned " + POINTS_RESOLVED + " bonus points!");
                        }
                    }
                    // Track worker resolved count
                    for (Worker w : workers) {
                        if (w.getId().equals(wid)) w.incrementResolvedCount();
                    }
                }
                System.out.println("  [✓] Report " + reportId + " marked as " + newStatus);
                return;
            }
        }
        System.out.println("  [!] Report not found.");
    }

    // Worker updates status to IN_PROGRESS
    public void workerStartWork(String wid, String reportId) {
        for (Report r : reports) {
            if (r.getId().equals(reportId) && wid.equals(r.getAssignedWorkerId())) {
                if (!r.getStatus().equals(Report.ASSIGNED)) {
                    System.out.println("  [!] Report is not in ASSIGNED state.");
                    return;
                }
                r.setStatus(Report.IN_PROGRESS);
                System.out.println("  [✓] Report " + reportId + " is now IN_PROGRESS.");
                return;
            }
        }
        System.out.println("  [!] Report not found or not assigned to you.");
    }

    // ----------------------------------------------------------------
    // USER COMPLAINT
    // ----------------------------------------------------------------
    public void userReportWorker(String reportId, String userId, List<Worker> workers) {
        for (Report r : reports) {
            if (r.getId().equals(reportId)) {
                if (!r.getUserId().equals(userId)) {
                    System.out.println("  [!] You can only complain about your own reports.");
                    return;
                }
                if (r.getStatus().equals(Report.WORKER_COMPLAINT)) {
                    System.out.println("  [!] Complaint already filed for this report.");
                    return;
                }
                r.setStatus(Report.WORKER_COMPLAINT);
                // Increment complaint count on worker
                if (r.getAssignedWorkerId() != null) {
                    for (Worker w : workers) {
                        if (w.getId().equals(r.getAssignedWorkerId()))
                            w.incrementComplaintCount();
                    }
                }
                System.out.println("  [✓] Complaint registered for report " + reportId);
                return;
            }
        }
        System.out.println("  [!] Report not found.");
    }

    // ----------------------------------------------------------------
    // ADMIN: CLOSE / REASSIGN
    // ----------------------------------------------------------------
    public void adminCloseReport(String reportId) {
        for (Report r : reports) {
            if (r.getId().equals(reportId)) {
                r.setStatus(Report.CLOSED);
                System.out.println("  [✓] Report " + reportId + " closed.");
                return;
            }
        }
        System.out.println("  [!] Report not found.");
    }

    public void adminReassign(String reportId, String workerId, List<Worker> workers) {
        for (Report r : reports) {
            if (r.getId().equals(reportId)) {
                // Decrement old worker's count
                String oldWid = r.getAssignedWorkerId();
                if (oldWid != null) {
                    for (Worker w : workers) {
                        if (w.getId().equals(oldWid)) {
                            w.decrementAssignedCount();
                            break;
                        }
                    }
                }
                
                boolean ok = assignmentService.reassignReport(r, workerId, workers);
                if (ok) System.out.println("  [✓] Report " + reportId + " reassigned to " + workerId);
                else {
                    // Revert decrement if assignment failed (worker not found)
                    if (oldWid != null) {
                        for (Worker w : workers) {
                            if (w.getId().equals(oldWid)) {
                                w.incrementAssignedCount();
                                break;
                            }
                        }
                    }
                    System.out.println("  [!] Worker not found.");
                }
                return;
            }
        }
        System.out.println("  [!] Report not found.");
    }

    public void deleteWorker(String workerId, List<Worker> workers) {
        unassignReportsForWorker(workerId);
        services.Storage.unassignReports(workerId);
        workers.removeIf(w -> w.getId().equals(workerId));
        services.Storage.deleteWorker(workerId);
    }

    private void unassignReportsForWorker(String wid) {
        for (Report r : reports) {
            if (wid.equals(r.getAssignedWorkerId())) {
                r.setAssignedWorkerId(null);
                r.setStatus(Report.PENDING);
            }
        }
    }

    public void assignPendingReports() {
        assignmentService.assignPendingReports(reports);
    }

    // ----------------------------------------------------------------
    // STATS & RANKING
    // ----------------------------------------------------------------
    public void showUserRanking(List<User> users) {
        System.out.println("\n--- User Ranking Leaderboard ---");
        List<User> sorted = new ArrayList<>(users);
        sorted.sort((a, b) -> b.getPoints() - a.getPoints());

        for (int i = 0; i < sorted.size(); i++) {
            User u = sorted.get(i);
            long count = reports.stream().filter(r -> r.getUserId().equals(u.getId())).count();
            String badge = (i < 3) ? " | GREEN WARRIOR |" : "";
            System.out.printf("  %2d. %-20s | Points: %4d | Reports: %2d%s%n",
                    i + 1, u.getName(), u.getPoints(), count, badge);
        }
    }

    public void showWorkerStats(List<Worker> workers) {
        System.out.println("\n--- Worker Statistics ---");
        System.out.printf("  %-5s %-15s %8s %8s %8s%n",
                "ID", "Name", "Assigned", "Resolved", "Complaints");
        for (Worker w : workers) {
            System.out.printf("  %-5s %-15s %8d %8d %8d%n",
                    w.getId(), w.getName(),
                    w.getAssignedCount(), w.getResolvedCount(), w.getComplaintCount());
        }
    }

    public void showSummaryStats() {
        long pending    = reports.stream().filter(r -> r.getStatus().equals(Report.PENDING)).count();
        long assigned   = reports.stream().filter(r -> r.getStatus().equals(Report.ASSIGNED)).count();
        long inProgress = reports.stream().filter(r -> r.getStatus().equals(Report.IN_PROGRESS)).count();
        long resolved   = reports.stream().filter(r -> r.getStatus().equals(Report.RESOLVED)).count();
        long suspicious = reports.stream().filter(r -> r.getStatus().equals(Report.SUSPICIOUS)).count();
        long complaints = reports.stream().filter(r -> r.getStatus().equals(Report.WORKER_COMPLAINT)).count();
        long closed     = reports.stream().filter(r -> r.getStatus().equals(Report.CLOSED)).count();

        System.out.println("\n--- System Summary ---");
        System.out.println("  Total reports   : " + reports.size());
        System.out.println("  Pending         : " + pending);
        System.out.println("  Assigned        : " + assigned);
        System.out.println("  In Progress     : " + inProgress);
        System.out.println("  Resolved        : " + resolved);
        System.out.println("  Suspicious      : " + suspicious);
        System.out.println("  Complaints      : " + complaints);
        System.out.println("  Closed          : " + closed);
    }

    // ----------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------
    private String generateId() {
        int maxNum = 0;
        for (Report r : reports) {
            try {
                int num = Integer.parseInt(r.getId().substring(1));
                if (num > maxNum) maxNum = num;
            } catch (Exception ignored) {}
        }
        return "R" + (maxNum + 1);
    }

    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, DATE_FMT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
