package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import users.*;
import reports.*;
import services.*;

public class MainFrame extends JFrame {

    private final List<User>   users;
    private final List<Worker> workers;
    private final List<Admin>  admins;
    private final ReportService reportService;
    private final AssignmentService assignmentService;

    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    public MainFrame(List<User> users, List<Worker> workers, List<Admin> admins,
                     ReportService reportService, AssignmentService assignmentService) {
        this.users = users;
        this.workers = workers;
        this.admins = admins;
        this.reportService = reportService;
        this.assignmentService = assignmentService;

        setTitle("Environmental Reporting System");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        showView("LOGIN");
        add(mainPanel);
    }

    public void showView(String name) {
        if ("LOGIN".equals(name)) {
            mainPanel.add(new LoginPanel(this), "LOGIN");
        } else if ("REGISTER".equals(name)) {
            mainPanel.add(new RegisterPanel(this), "REGISTER");
        }
        cardLayout.show(mainPanel, name);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void onLoginSuccess(Object user) {
        if (user instanceof Admin) {
            mainPanel.add(new AdminDashboard(this, (Admin) user), "ADMIN");
            cardLayout.show(mainPanel, "ADMIN");
        } else if (user instanceof Worker) {
            mainPanel.add(new WorkerDashboard(this, (Worker) user), "WORKER");
            cardLayout.show(mainPanel, "WORKER");
        } else if (user instanceof User) {
            mainPanel.add(new UserDashboard(this, (User) user), "USER");
            cardLayout.show(mainPanel, "USER");
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void onLogout() {
        showView("LOGIN");
    }

    // Getters for services
    public List<User>   getUsers()             { return users; }
    public List<Worker> getWorkers()           { return workers; }
    public List<Admin>  getAdmins()            { return admins; }
    public ReportService getReportService()    { return reportService; }
    public AssignmentService getAssignmentService() { return assignmentService; }
}
