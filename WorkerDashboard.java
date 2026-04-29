package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import users.Worker;
import reports.Report;
import services.Storage;

public class WorkerDashboard extends JPanel {

    private final MainFrame mainFrame;
    private final Worker worker;
    private final JTable table;
    private final DefaultTableModel model;

    public WorkerDashboard(MainFrame mainFrame, Worker worker) {
        this.mainFrame = mainFrame;
        this.worker = worker;
        setLayout(new BorderLayout());
        setBackground(UIUtils.BACKGROUND_COLOR);

        // Sidebar with Fixed Logout
        JPanel sidebarWrapper = new JPanel(new BorderLayout());
        sidebarWrapper.setPreferredSize(new Dimension(250, 0));
        sidebarWrapper.setBackground(UIUtils.TEXT_COLOR);

        sidebarWrapper.add(createSidebarOptions(), BorderLayout.CENTER);

        // Fixed Logout at Bottom
        JPanel logoutArea = new JPanel(new BorderLayout());
        logoutArea.setBackground(UIUtils.TEXT_COLOR);
        logoutArea.setBorder(new EmptyBorder(10, 20, 20, 20));
        JButton logoutBtn = UIUtils.createButton("Logout", UIUtils.DANGER_COLOR);
        logoutBtn.addActionListener(e -> mainFrame.onLogout());
        logoutArea.add(logoutBtn, BorderLayout.CENTER);
        sidebarWrapper.add(logoutArea, BorderLayout.SOUTH);

        add(sidebarWrapper, BorderLayout.WEST);

        // Main Content
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(UIUtils.BACKGROUND_COLOR);
        mainContent.setBorder(UIUtils.createPadding(20, 20, 20, 20));

        mainContent.add(UIUtils.createLabel("My Assigned Reports", UIUtils.FONT_SUBTITLE, UIUtils.PRIMARY_COLOR), BorderLayout.NORTH);

        String[] cols = {"ID", "Category", "Status", "Date", "Coordinates", "Description"};
        model = UIUtils.createNonEditableModel(cols);
        table = new JTable(model);
        table.setRowHeight(30);
        mainContent.add(new JScrollPane(table), BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(UIUtils.BACKGROUND_COLOR);
        
        JButton startBtn = UIUtils.createButton("START WORK", UIUtils.PRIMARY_COLOR);
        JButton resolveBtn = UIUtils.createButton("RESOLVE", UIUtils.SECONDARY_COLOR);
        JButton suspiciousBtn = UIUtils.createButton("SUSPICIOUS", UIUtils.DANGER_COLOR);

        actionPanel.add(startBtn);
        actionPanel.add(resolveBtn);
        actionPanel.add(suspiciousBtn);
        mainContent.add(actionPanel, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);

        startBtn.addActionListener(e -> handleAction("start"));
        resolveBtn.addActionListener(e -> handleAction("resolve"));
        suspiciousBtn.addActionListener(e -> handleAction("suspicious"));

        refreshTable();
    }

    private void handleAction(String action) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a report first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reportId = (String) model.getValueAt(row, 0);
        if ("start".equals(action)) {
            mainFrame.getReportService().workerStartWork(worker.getId(), reportId);
        } else {
            boolean suspicious = "suspicious".equals(action);
            mainFrame.getReportService().workerMarkReport(worker.getId(), reportId, suspicious, 
                    mainFrame.getUsers(), mainFrame.getWorkers());
        }

        Storage.saveReports(mainFrame.getReportService().getAllReports());
        Storage.saveUsers(mainFrame.getUsers());
        Storage.saveWorkers(mainFrame.getWorkers());
        refreshTable();
    }

    private void refreshTable() {
        model.setRowCount(0);
        List<Report> assigned = mainFrame.getReportService().getAllReports().stream()
                .filter(r -> worker.getId().equals(r.getAssignedWorkerId()))
                .collect(Collectors.toList());
        for (Report r : assigned) {
            String coords = "(" + r.getLat() + ", " + r.getLon() + ")";
            model.addRow(new Object[]{r.getId(), r.getCategory(), r.getStatus(), r.getDate(), coords, r.getDescription()});
        }
    }

    private JPanel createSidebarOptions() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIUtils.TEXT_COLOR);
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel title = UIUtils.createLabel("ISR System", UIUtils.FONT_TITLE, Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(10));
        
        JLabel role = UIUtils.createLabel("WORKER PORTAL", UIUtils.FONT_BOLD, UIUtils.ACCENT_COLOR);
        role.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(role);
        sidebar.add(Box.createVerticalStrut(40));

        sidebar.add(Box.createVerticalGlue());

        JLabel nameLabel = UIUtils.createLabel(worker.getName(), UIUtils.FONT_BOLD, Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(nameLabel);
        
        JLabel idLabel = UIUtils.createLabel("ID: " + worker.getId(), UIUtils.FONT_REGULAR, Color.LIGHT_GRAY);
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(idLabel);
        sidebar.add(Box.createVerticalStrut(10));

        return sidebar;
    }
}
