package main;

import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import users.*;
import reports.*;
import services.*;
import gui.MainFrame;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // ---- Connectivity Test ----
        DatabaseManager.testConnection();

        // ---- Load data from DB ----
        List<User>   users   = Storage.loadUsers();
        List<Worker> workers = Storage.loadWorkers();

        // Removed automatic worker initialization.

        List<Admin> admins = new ArrayList<>();
        admins.add(new Admin("admin", "Administrator", "adminpass"));

        AssignmentService assignmentService = new AssignmentService(workers);
        ReportService     reportService     = new ReportService(assignmentService);
        reportService.setReports(Storage.loadReports());

        printBanner();

        // ---- Startup mode selection loop ----
        while (true) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("  1. Console Mode  (Terminal UI)");
            System.out.println("  2. GUI Mode      (Java Swing Window)");
            System.out.println("  3. Exit Application");
            System.out.print("Choice: ");
            int mode = safeNextInt(sc);

            if (mode == 3) {
                saveAll(users, workers, reportService);
                System.out.println("\n  Goodbye! Data saved to Database.");
                break;
            }

            if (mode == 2) {
                try {
                    // Force Swing to use System Look and Feel
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}

                final Object lock = new Object();
                SwingUtilities.invokeLater(() -> {
                    MainFrame mainFrame = new MainFrame(users, workers, admins, reportService, assignmentService);
                    mainFrame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            synchronized (lock) {
                                lock.notify();
                            }
                        }
                    });
                    mainFrame.setVisible(true);
                });
                
                System.out.println("\n  [OK] GUI launched in a new window.");
                System.out.println("  [INFO] Terminal is paused. Close the GUI window to return to this menu.");
                
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                saveAll(users, workers, reportService);
                System.out.println("  [OK] GUI closed. Data saved safely to database.");
                continue;
            }

            if (mode == 1) {
                // ---- Console main loop ----
                boolean backToMain = false;
                while (!backToMain) {
                    System.out.println("\n");
                    System.out.println("INCEPTIVE SMART REPORTING (CONSOLE)");
                    System.out.println("1. Login as Admin");
                    System.out.println("2. Login as Worker");
                    System.out.println("3. Login as User");
                    System.out.println("4. Back to Main Menu");
                    System.out.print("Choice: ");
                    int choice = safeNextInt(sc);

                    switch (choice) {
                        case 1: loginAdmin(sc, admins, reportService, workers, assignmentService, users); break;
                        case 2: loginWorker(sc, workers, reportService, users);                          break;
                        case 3: userFlow(sc, users, reportService, workers);                             break;
                        case 4:
                            saveAll(users, workers, reportService);
                            backToMain = true;
                            break;
                        default:
                            System.out.println("  [ERROR] Invalid option.");
                    }
                }
            } else {
                System.out.println("  [ERROR] Invalid option.");
            }
        }
        sc.close();
    }

    // ================================================================
    // ADMIN LOGIN + MENU
    // ================================================================
    private static void loginAdmin(Scanner sc, List<Admin> admins,
                                   ReportService reportService, List<Worker> workers,
                                   AssignmentService assignmentService, List<User> users) {
        System.out.print("Admin ID: ");
        String id = sc.next();
        System.out.print("Password: ");
        String pass = sc.next();
        Optional<Admin> opt = admins.stream()
                .filter(a -> a.getId().equals(id) && a.getPassword().equals(pass))
                .findFirst();
        if (!opt.isPresent()) { System.out.println("  [!] Invalid credentials."); return; }
        System.out.println("  Welcome, " + opt.get().getName() + "!");
        adminMenu(sc, reportService, workers, assignmentService, users);
        saveAll(users, workers, reportService);
    }

    private static void adminMenu(Scanner sc, ReportService reportService,
                                  List<Worker> workers, AssignmentService assignmentService,
                                  List<User> users) {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("  1.  All reports (latest first)");
            System.out.println("  2.  Pending reports");
            System.out.println("  3.  Assigned reports");
            System.out.println("  4.  In-progress reports");
            System.out.println("  5.  Resolved reports");
            System.out.println("  6.  Suspicious reports");
            System.out.println("  7.  Worker complaints");
            System.out.println("  8.  Filter by category");
            System.out.println("  9.  Worker statistics");
            System.out.println("  10. Assignment history");
            System.out.println("  11. System summary");
            System.out.println("  12. Close a report");
            System.out.println("  13. Reassign a report");
            System.out.println("  14. Add worker");
            System.out.println("  15. User ranking");
            System.out.println("  0.  Back");
            System.out.print("Choice: ");
            int c = safeNextInt(sc);
            switch (c) {
                case 1:  reportService.showAllReportsLatestFirst();                   break;
                case 2:  reportService.showReportsByStatus(Report.PENDING);           break;
                case 3:  reportService.showReportsByStatus(Report.ASSIGNED);          break;
                case 4:  reportService.showReportsByStatus(Report.IN_PROGRESS);       break;
                case 5:  reportService.showReportsByStatus(Report.RESOLVED);          break;
                case 6:  reportService.showReportsByStatus(Report.SUSPICIOUS);        break;
                case 7:  reportService.showReportsByStatus(Report.WORKER_COMPLAINT);  break;
                case 8:
                    System.out.println("  Categories: " + String.join(", ", Report.CATEGORIES));
                    System.out.print("  Enter category: ");
                    sc.nextLine(); String cat = sc.nextLine().trim();
                    reportService.showReportsByCategory(cat);
                    break;
                case 9:  reportService.showWorkerStats(workers);                      break;
                case 10: assignmentService.showAssignmentReports();                   break;
                case 11: reportService.showSummaryStats();                            break;
                case 12:
                    System.out.print("  Report ID to close: ");
                    reportService.adminCloseReport(sc.next());
                    break;
                case 13:
                    System.out.print("  Report ID: "); String rid = sc.next();
                    System.out.print("  Worker ID: "); String wid = sc.next();
                    reportService.adminReassign(rid, wid, workers);
                    break;
                case 14:
                    System.out.print("  Worker ID: ");   String nid  = sc.next();
                    System.out.print("  Worker Name: "); String nname = sc.next();
                    System.out.print("  Password: ");    String npass = sc.next();
                    workers.add(new Worker(nid, nname, npass));
                    System.out.println("  [✓] Worker " + nname + " added.");
                    reportService.assignPendingReports();
                    break;
                case 15: reportService.showUserRanking(users);                        break;
                case 0:  return;
                default: System.out.println("  [ERROR] Invalid option.");
            }
        }
    }

    // ================================================================
    // WORKER LOGIN + MENU
    // ================================================================
    private static void loginWorker(Scanner sc, List<Worker> workers,
                                    ReportService reportService, List<User> users) {
        System.out.print("Worker ID: ");
        String id = sc.next();
        System.out.print("Password: ");
        String pass = sc.next();
        Optional<Worker> opt = workers.stream()
                .filter(w -> w.getId().equals(id) && w.getPassword().equals(pass))
                .findFirst();
        if (!opt.isPresent()) { System.out.println("  [!] Invalid credentials."); return; }
        Worker worker = opt.get();
        System.out.println("  Welcome, " + worker.getName() + "!");
        workerMenu(sc, reportService, worker.getId(), users, workers);
    }

    private static void workerMenu(Scanner sc, ReportService reportService,
                                   String wid, List<User> users, List<Worker> workers) {
        while (true) {
            System.out.println("\n--- Worker Menu ---");
            System.out.println("  1. View my assigned reports");
            System.out.println("  2. Start working on a report (→ IN_PROGRESS)");
            System.out.println("  3. Mark report resolved / suspicious");
            System.out.println("  0. Logout");
            System.out.print("Choice: ");
            int c = safeNextInt(sc);
            switch (c) {
                case 1:
                    reportService.showReportsForWorker(wid);
                    break;
                case 2:
                    System.out.print("  Report ID: ");
                    reportService.workerStartWork(wid, sc.next());
                    break;
                case 3:
                    System.out.print("  Report ID: ");   String rid  = sc.next();
                    System.out.print("  Mark suspicious? (true/false): ");
                    boolean susp = safeNextBoolean(sc);
                    reportService.workerMarkReport(wid, rid, susp, users, workers);
                    break;
                case 0: return;
                default: System.out.println("  [!] Invalid option.");
            }
        }
    }

    // ================================================================
    // USER FLOW
    // ================================================================
    private static void userFlow(Scanner sc, List<User> users,
                                 ReportService reportService, List<Worker> workers) {
        while (true) {
            System.out.println("\n--- User Portal ---");
            System.out.println("  1. Login");
            System.out.println("  2. Register");
            System.out.println("  0. Back");
            System.out.print("Choice: ");
            int c = safeNextInt(sc);
            if (c == 0) return;

            if (c == 2) {
                registerUser(sc, users);
            } else if (c == 1) {
                User u = loginUser(sc, users);
                if (u != null) {
                    userMenu(sc, u, users, reportService, workers);
                    Storage.saveUsers(users);
                }
            }
        }
    }

    private static void registerUser(Scanner sc, List<User> users) {
        sc.nextLine();
        System.out.print("  Full name: ");
        String name = sc.nextLine().trim();
        System.out.print("  Email: ");
        String email = sc.next().trim().toLowerCase();
        if (users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email))) {
            System.out.println("  [ERROR] Email already registered."); return;
        }
        System.out.print("  Password (min 4 chars): ");
        String pass = sc.next();
        if (pass.length() < 4) { System.out.println("  [ERROR] Password too short."); return; }

        String newId = "U" + (users.size() + 1);
        users.add(new User(newId, name, email, pass));
        Storage.saveUsers(users);
        System.out.println("  [OK] Registration successful! Your ID: " + newId);
    }

    private static User loginUser(Scanner sc, List<User> users) {
        System.out.print("  Email: ");
        String email = sc.next().trim().toLowerCase();
        System.out.print("  Password: ");
        String pass = sc.next();
        Optional<User> opt = users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(pass))
                .findFirst();
        if (!opt.isPresent()) { System.out.println("  [ERROR] Invalid credentials."); return null; }
        User u = opt.get();
        System.out.println("  Welcome back, " + u.getName() + "! Points: " + u.getPoints());
        return u;
    }

    private static void userMenu(Scanner sc, User user, List<User> users,
                                 ReportService reportService, List<Worker> workers) {
        while (true) {
            System.out.println("\n--- User Menu [" + user.getName() + " | Points: " + user.getPoints() + "] ---");
            System.out.println("  1. Submit a report");
            System.out.println("  2. View my reports");
            System.out.println("  3. Upvote a report");
            System.out.println("  4. File worker complaint");
            System.out.println("  5. View user ranking");
            System.out.println("  6. View community reports");
            System.out.println("  0. Logout");
            System.out.print("Choice: ");
            int c = safeNextInt(sc);
            switch (c) {
                case 1: submitReportFlow(sc, user, reportService); break;
                case 2: reportService.showReportsByUser(user.getId()); break;
                case 3:
                    System.out.print("  Report ID to upvote: ");
                    reportService.upvoteReport(sc.next(), user.getId());
                    break;
                case 4:
                    System.out.print("  Report ID to complain about: ");
                    reportService.userReportWorker(sc.next(), user.getId(), workers);
                    break;
                case 5: reportService.showUserRanking(users); break;
                case 6: reportService.showAllReportsLatestFirst(); break;
                case 0: return;
                default: System.out.println("  [!] Invalid option.");
            }
        }
    }

    private static void submitReportFlow(Scanner sc, User user, ReportService reportService) {
        System.out.println("\n  --- Submit Report ---");
        System.out.println("\nEnter the location details:");
        System.out.print("  Latitude:  ");  double lat = safeNextDouble(sc);
        System.out.print("  Longitude: "); double lon = safeNextDouble(sc);
        sc.nextLine();
        System.out.print("  Description: "); String desc = sc.nextLine();
        System.out.print("  Date (YYYY-MM-DD): "); String date = sc.next();

        System.out.println("  Categories:");
        for (int i = 0; i < Report.CATEGORIES.length; i++)
            System.out.println("    " + (i + 1) + ". " + Report.CATEGORIES[i]);
        System.out.print("  Choose category (1-" + Report.CATEGORIES.length + "): ");
        int catIdx = safeNextInt(sc);
        String category = (catIdx >= 1 && catIdx <= Report.CATEGORIES.length)
                ? Report.CATEGORIES[catIdx - 1] : "Other";

        reportService.submitReport(user, lat, lon, desc, date, category);
    }

    // ================================================================
    // HELPERS
    // ================================================================
    private static void saveAll(List<User> users, List<Worker> workers, ReportService rs) {
        Storage.saveUsers(users);
        Storage.saveWorkers(workers);
        Storage.saveReports(rs.getAllReports());
    }

    private static void printBanner() {
    	System.out.println("\n");
    	System.out.println("======================================");
        System.out.println("   INCENTIVE SMART REPORTING SYSTEM   ");
        System.out.println("======================================");
    }


    private static int safeNextInt(Scanner sc) {
        while (true) {
            try { return sc.nextInt(); }
            catch (InputMismatchException e) {
                System.out.print("  [!] Enter a valid number: "); sc.nextLine();
            }
        }
    }

    private static double safeNextDouble(Scanner sc) {
        while (true) {
            try { return sc.nextDouble(); }
            catch (InputMismatchException e) {
                System.out.print("  [!] Enter a valid number: "); sc.nextLine();
            }
        }
    }

    private static boolean safeNextBoolean(Scanner sc) {
        while (true) {
            try { return sc.nextBoolean(); }
            catch (InputMismatchException e) {
                System.out.print("  [ERROR] Enter true or false: "); sc.nextLine();
            }
        }
    }
}
