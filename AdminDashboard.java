package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import users.Admin;
import users.Worker;
import reports.Report;
import services.Storage;
import services.AssignmentReport;

public class AdminDashboard extends JPanel {

    private final MainFrame mainFrame;
    private final Admin admin;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;
    
    private String currentStatusFilter = null;
    private String currentCategoryFilter = null;

    public AdminDashboard(MainFrame mainFrame, Admin admin) {
        this.mainFrame = mainFrame;
        this.admin = admin;
        setLayout(new BorderLayout());
        setBackground(UIUtils.BACKGROUND_COLOR);

        // --- NEW SIDEBAR LAYOUT (Fixed Logout) ---
        JPanel sidebarWrapper = new JPanel(new BorderLayout());
        sidebarWrapper.setPreferredSize(new Dimension(250, 0));
        sidebarWrapper.setBackground(UIUtils.TEXT_COLOR);

        // Scrollable Options
        JScrollPane sidebarScroll = new JScrollPane(createSidebarOptions());
        sidebarScroll.setBorder(null);
        sidebarScroll.setOpaque(false);
        sidebarScroll.getViewport().setOpaque(false);
        sidebarScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarWrapper.add(sidebarScroll, BorderLayout.CENTER);

        // Fixed Logout at Bottom
        JPanel logoutArea = new JPanel(new BorderLayout());
        logoutArea.setBackground(UIUtils.TEXT_COLOR);
        logoutArea.setBorder(new EmptyBorder(10, 20, 20, 20));
        JButton logoutBtn = UIUtils.createButton("Logout", UIUtils.DANGER_COLOR);
        logoutBtn.addActionListener(e -> mainFrame.onLogout());
        logoutArea.add(logoutBtn, BorderLayout.CENTER);
        sidebarWrapper.add(logoutArea, BorderLayout.SOUTH);

        add(sidebarWrapper, BorderLayout.WEST);

        // Content
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIUtils.BACKGROUND_COLOR);

        refreshContent("REPORTS");
        add(contentPanel, BorderLayout.CENTER);
    }

    private void refreshContent(String name) {
        if ("REPORTS".equals(name)) {
            contentPanel.add(createReportsPanel(), "REPORTS");
        } else if ("WORKER_STATS".equals(name)) {
            contentPanel.add(createWorkerStatsPanel(), "WORKER_STATS");
        } else if ("ADD_WORKER".equals(name)) {
            contentPanel.add(createAddWorkerPanel(), "ADD_WORKER");
        } else if ("SYSTEM_STATS".equals(name)) {
            contentPanel.add(createSystemStatsPanel(), "SYSTEM_STATS");
        } else if ("HISTORY".equals(name)) {
            contentPanel.add(createHistoryPanel(), "HISTORY");
        } else if ("LEADERBOARD".equals(name)) {
            contentPanel.add(createLeaderboardPanel(), "LEADERBOARD");
        }
        cardLayout.show(contentPanel, name);
    }

    private JPanel createSidebarOptions() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIUtils.TEXT_COLOR);
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel title = UIUtils.createLabel("ISR Admin", UIUtils.FONT_TITLE, Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(20));

        addSidebarSection(sidebar, "MANAGEMENT");
        sidebar.add(createNavBtn("Reports Overview", e -> { 
            currentStatusFilter = null; 
            currentCategoryFilter = null;
            refreshContent("REPORTS"); 
        }));
        sidebar.add(createNavBtn("Worker Stats", e -> refreshContent("WORKER_STATS")));
        sidebar.add(createNavBtn("Add Worker", e -> refreshContent("ADD_WORKER")));
        sidebar.add(createNavBtn("System Stats", e -> refreshContent("SYSTEM_STATS")));
        sidebar.add(createNavBtn("Assign History", e -> refreshContent("HISTORY")));
        sidebar.add(createNavBtn("Community Leaderboard", e -> refreshContent("LEADERBOARD")));

        sidebar.add(Box.createVerticalStrut(20));
        
        addSidebarSection(sidebar, "FILTER BY STATUS");
        sidebar.add(createNavBtn("Pending", e -> { currentStatusFilter = Report.PENDING; refreshContent("REPORTS"); }));
        sidebar.add(createNavBtn("Assigned", e -> { currentStatusFilter = Report.ASSIGNED; refreshContent("REPORTS"); }));
        sidebar.add(createNavBtn("In Progress", e -> { currentStatusFilter = Report.IN_PROGRESS; refreshContent("REPORTS"); }));
        sidebar.add(createNavBtn("Resolved", e -> { currentStatusFilter = Report.RESOLVED; refreshContent("REPORTS"); }));
        sidebar.add(createNavBtn("Suspicious", e -> { currentStatusFilter = Report.SUSPICIOUS; refreshContent("REPORTS"); }));
        sidebar.add(createNavBtn("Complaints", e -> { currentStatusFilter = Report.WORKER_COMPLAINT; refreshContent("REPORTS"); }));
        sidebar.add(createNavBtn("Closed", e -> { currentStatusFilter = Report.CLOSED; refreshContent("REPORTS"); }));

        return sidebar;
    }

    private void addSidebarSection(JPanel sidebar, String label) {
        JLabel lbl = UIUtils.createLabel(label, new Font("Segoe UI", Font.BOLD, 11), Color.LIGHT_GRAY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lbl);
        sidebar.add(Box.createVerticalStrut(5));
    }

    private JButton createNavBtn(String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtils.FONT_REGULAR);
        btn.setForeground(Color.BLACK); // Requested black text
        btn.setBackground(Color.WHITE); // High contrast background
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(8, 10, 8, 10));
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    // --- SECTION: ALL REPORTS ---
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BACKGROUND_COLOR);
        panel.setBorder(UIUtils.createPadding(20, 20, 20, 20));

        String title = currentStatusFilter == null ? "All Reports" : currentStatusFilter + " Reports";

        JPanel headerOuter = new JPanel(new BorderLayout());
        headerOuter.setBackground(UIUtils.BACKGROUND_COLOR);
        headerOuter.add(UIUtils.createLabel(title, UIUtils.FONT_SUBTITLE, UIUtils.PRIMARY_COLOR), BorderLayout.NORTH);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.setBackground(UIUtils.BACKGROUND_COLOR);
        filters.add(new JLabel("Category: "));
        JComboBox<String> catBox = new JComboBox<>(new String[]{"All categories"});
        for(String c : Report.CATEGORIES) catBox.addItem(c);
        if (currentCategoryFilter != null) catBox.setSelectedItem(currentCategoryFilter);
        filters.add(catBox);
        headerOuter.add(filters, BorderLayout.CENTER);
        
        panel.add(headerOuter, BorderLayout.NORTH);

        String[] cols = {"ID", "User", "Worker", "Status", "Category", "Upvotes"};
        DefaultTableModel model = UIUtils.createNonEditableModel(cols);
        
        List<Report> filtered = mainFrame.getReportService().getAllReports();
        if (currentStatusFilter != null) {
            filtered = filtered.stream().filter(r -> r.getStatus().equals(currentStatusFilter)).collect(Collectors.toList());
        }
        if (currentCategoryFilter != null && !"All categories".equals(currentCategoryFilter)) {
            filtered = filtered.stream().filter(r -> r.getCategory().equalsIgnoreCase(currentCategoryFilter)).collect(Collectors.toList());
        }

        for (Report r : filtered) {
            model.addRow(new Object[]{r.getId(), r.getUserId(), r.getAssignedWorkerId(), r.getStatus(), r.getCategory(), r.getUpvotes()});
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(UIUtils.BACKGROUND_COLOR);
        JButton closeBtn = UIUtils.createButton("Close", UIUtils.DANGER_COLOR);
        JButton reassignBtn = UIUtils.createButton("Reassign", UIUtils.PRIMARY_COLOR);
        footer.add(reassignBtn);
        footer.add(closeBtn);
        panel.add(footer, BorderLayout.SOUTH);

        catBox.addActionListener(e -> {
            currentCategoryFilter = (String) catBox.getSelectedItem();
            refreshContent("REPORTS");
        });

        closeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            String rid = (String) model.getValueAt(row, 0);
            mainFrame.getReportService().adminCloseReport(rid);
            Storage.saveReports(mainFrame.getReportService().getAllReports());
            refreshContent("REPORTS");
        });

        reassignBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            String rid = (String) model.getValueAt(row, 0);
            String wid = JOptionPane.showInputDialog(this, "Enter Worker ID to reassign to:");
            if (wid != null && !wid.isEmpty()) {
                mainFrame.getReportService().adminReassign(rid, wid, mainFrame.getWorkers());
                Storage.saveReports(mainFrame.getReportService().getAllReports());
                Storage.saveWorkers(mainFrame.getWorkers());
                refreshContent("REPORTS");
            }
        });

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BACKGROUND_COLOR);
        panel.setBorder(UIUtils.createPadding(20, 20, 20, 20));
        panel.add(UIUtils.createLabel("Assignment History Log", UIUtils.FONT_SUBTITLE, UIUtils.PRIMARY_COLOR), BorderLayout.NORTH);
        String[] cols = {"Report ID", "Category", "Assigned To", "Current Status"};
        DefaultTableModel model = UIUtils.createNonEditableModel(cols);
        List<AssignmentReport> list = mainFrame.getAssignmentService().getAssignments();
        for (AssignmentReport ar : list) {
            model.addRow(new Object[]{ar.getReport().getId(), ar.getReport().getCategory(), ar.getWorkerId(), ar.getReport().getStatus()});
        }
        JTable table = new JTable(model);
        table.setRowHeight(30);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createWorkerStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BACKGROUND_COLOR);
        panel.setBorder(UIUtils.createPadding(20, 20, 20, 20));
        panel.add(UIUtils.createLabel("Worker Performance", UIUtils.FONT_SUBTITLE, UIUtils.PRIMARY_COLOR), BorderLayout.NORTH);
        String[] cols = {"ID", "Name", "Assigned", "Resolved", "Complaints"};
        DefaultTableModel model = UIUtils.createNonEditableModel(cols);
        for (Worker w : mainFrame.getWorkers()) {
            model.addRow(new Object[]{w.getId(), w.getName(), w.getAssignedCount(), w.getResolvedCount(), w.getComplaintCount()});
        }
        JTable table = new JTable(model);
        table.setRowHeight(30);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(UIUtils.BACKGROUND_COLOR);
        JButton deleteBtn = UIUtils.createButton("Delete Worker", UIUtils.DANGER_COLOR);
        footer.add(deleteBtn);
        panel.add(footer, BorderLayout.SOUTH);

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a worker from the table.");
                return;
            }
            String wid = (String) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete worker " + wid + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.getReportService().deleteWorker(wid, mainFrame.getWorkers());
                Storage.saveReports(mainFrame.getReportService().getAllReports());
                Storage.saveWorkers(mainFrame.getWorkers());
                refreshContent("WORKER_STATS");
            }
        });

        return panel;
    }

    private JPanel createAddWorkerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIUtils.BACKGROUND_COLOR);
        JPanel card = UIUtils.createCard(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(UIUtils.createLabel("Register New Worker", UIUtils.FONT_SUBTITLE, UIUtils.ACCENT_COLOR), gbc);
        gbc.gridwidth = 1; gbc.gridy = 1;
        card.add(UIUtils.createLabel("Worker ID:", UIUtils.FONT_BOLD, UIUtils.TEXT_COLOR), gbc);
        JTextField idField = UIUtils.createTextField();
        gbc.gridx = 1;
        card.add(idField, gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        card.add(UIUtils.createLabel("Full Name:", UIUtils.FONT_BOLD, UIUtils.TEXT_COLOR), gbc);
        JTextField nameField = UIUtils.createTextField();
        gbc.gridx = 1;
        card.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 3;
        card.add(UIUtils.createLabel("Password:", UIUtils.FONT_BOLD, UIUtils.TEXT_COLOR), gbc);
        JTextField passField = UIUtils.createTextField();
        gbc.gridx = 1;
        card.add(passField, gbc);
        JButton addBtn = UIUtils.createButton("ADD WORKER", UIUtils.PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        card.add(addBtn, gbc);
        panel.add(card);
        addBtn.addActionListener(e -> {
            String id = idField.getText().trim(); String name = nameField.getText().trim(); String pass = passField.getText().trim();
            if (id.isEmpty() || name.isEmpty() || pass.isEmpty()) return;
            if (mainFrame.getWorkers().stream().anyMatch(w -> w.getId().equals(id))) {
                JOptionPane.showMessageDialog(this, "Worker ID already exists."); return;
            }
            mainFrame.getWorkers().add(new Worker(id, name, pass));
            Storage.saveWorkers(mainFrame.getWorkers());
            mainFrame.getReportService().assignPendingReports();
            Storage.saveReports(mainFrame.getReportService().getAllReports());
            JOptionPane.showMessageDialog(this, "Worker added successfully! Pending reports assigned.");
            idField.setText(""); nameField.setText(""); passField.setText("");
        });
        return panel;
    }

    private JPanel createSystemStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BACKGROUND_COLOR);
        panel.setBorder(UIUtils.createPadding(20, 20, 20, 20));
        panel.add(UIUtils.createLabel("System Overview", UIUtils.FONT_SUBTITLE, UIUtils.PRIMARY_COLOR), BorderLayout.NORTH);
        List<Report> all = mainFrame.getReportService().getAllReports();
        long pending = all.stream().filter(r -> r.getStatus().equals(Report.PENDING)).count();
        long assigned = all.stream().filter(r -> r.getStatus().equals(Report.ASSIGNED)).count();
        long resolved = all.stream().filter(r -> r.getStatus().equals(Report.RESOLVED)).count();
        long complaints = all.stream().filter(r -> r.getStatus().equals(Report.WORKER_COMPLAINT)).count();
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBackground(UIUtils.BACKGROUND_COLOR);
        statsPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        statsPanel.add(createStatCard("Total Reports", "" + all.size(), UIUtils.PRIMARY_COLOR));
        statsPanel.add(createStatCard("Pending", "" + pending, UIUtils.ACCENT_COLOR));
        statsPanel.add(createStatCard("Resolved", "" + resolved, UIUtils.SECONDARY_COLOR));
        statsPanel.add(createStatCard("Complaints", "" + complaints, UIUtils.DANGER_COLOR));
        panel.add(statsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatCard(String label, String value, Color color) {
        JPanel card = UIUtils.createCard(new BorderLayout());
        JLabel valLabel = UIUtils.createLabel(value, UIUtils.FONT_TITLE, color);
        valLabel.setHorizontalAlignment(JLabel.CENTER);
        JLabel lblLabel = UIUtils.createLabel(label, UIUtils.FONT_BOLD, UIUtils.TEXT_COLOR);
        lblLabel.setHorizontalAlignment(JLabel.CENTER);
        card.add(valLabel, BorderLayout.CENTER); card.add(lblLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BACKGROUND_COLOR);
        panel.setBorder(UIUtils.createPadding(20, 20, 20, 20));
        
        JPanel leaderboardHeader = new JPanel(new GridLayout(2, 1));
        leaderboardHeader.setBackground(UIUtils.BACKGROUND_COLOR);
        leaderboardHeader.add(UIUtils.createLabel("Community Leaderboard", UIUtils.FONT_SUBTITLE, UIUtils.PRIMARY_COLOR));
        
        JLabel giftLabel = UIUtils.createLabel("Top 3 users will get a surprise gift!", UIUtils.FONT_BOLD, new Color(0, 150, 0)); // Dark Green
        giftLabel.setHorizontalAlignment(SwingConstants.CENTER);
        leaderboardHeader.add(giftLabel);
        
        panel.add(leaderboardHeader, BorderLayout.NORTH);
        String[] cols = {"Rank", "Name", "Points", "Reports"};
        List<users.User> sortedUsers = mainFrame.getUsers().stream().sorted((a, b) -> b.getPoints() - a.getPoints()).collect(Collectors.toList());
        DefaultTableModel model = UIUtils.createNonEditableModel(cols);
        for (int i = 0; i < sortedUsers.size(); i++) {
            users.User u = sortedUsers.get(i);
            long count = mainFrame.getReportService().getAllReports().stream().filter(r -> r.getUserId().equals(u.getId())).count();
            String displayName = u.getName();
            if (i < 3) displayName += " [Green Warrior]";
            model.addRow(new Object[]{i + 1, displayName, u.getPoints(), count});
        }
        JTable table = new JTable(model);
        table.setRowHeight(30);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
}
