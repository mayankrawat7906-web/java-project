package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import users.User;
import reports.Report;
import services.Storage;

public class UserDashboard extends JPanel {

    private final MainFrame mainFrame;
    private final User user;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;
    private JLabel pointsLabel;

    public UserDashboard(MainFrame mainFrame, User user) {
        this.mainFrame = mainFrame;
        this.user = user;
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
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIUtils.BACKGROUND_COLOR);

        refreshContent("SUBMIT");
        add(contentPanel, BorderLayout.CENTER);
    }

    private void refreshContent(String name) {
        if ("SUBMIT".equals(name)) {
            contentPanel.add(createSubmitReportPanel(), "SUBMIT");
        } else if ("MY_REPORTS".equals(name)) {
            contentPanel.add(createMyReportsPanel(), "MY_REPORTS");
        } else if ("ALL_REPORTS".equals(name)) {
            contentPanel.add(createAllReportsPanel(), "ALL_REPORTS");
        } else if ("LEADERBOARD".equals(name)) {
            contentPanel.add(createLeaderboardPanel(), "LEADERBOARD");
        }
        cardLayout.show(contentPanel, name);
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
        
        JLabel role = UIUtils.createLabel("USER PORTAL", UIUtils.FONT_BOLD, UIUtils.PRIMARY_COLOR);
        role.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(role);
        sidebar.add(Box.createVerticalStrut(40));

        sidebar.add(createNavBtn("Submit Report", "SUBMIT"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createNavBtn("My Reports", "MY_REPORTS"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createNavBtn("Community Reports", "ALL_REPORTS"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createNavBtn("Leaderboard", "LEADERBOARD"));

        sidebar.add(Box.createVerticalGlue());

        JLabel nameLabel = UIUtils.createLabel(user.getName(), UIUtils.FONT_BOLD, Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(nameLabel);
        
        pointsLabel = UIUtils.createLabel("Points: " + user.getPoints(), UIUtils.FONT_REGULAR, Color.LIGHT_GRAY);
        pointsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(pointsLabel);
        sidebar.add(Box.createVerticalStrut(10));

        return sidebar;
    }

    private JButton createNavBtn(String text, String section) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtils.FONT_BOLD);
        btn.setForeground(Color.BLACK); // Requested black text
        btn.setBackground(Color.WHITE); // Lighter background for black text
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(12, 15, 12, 15));
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(210, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> refreshContent(section));
        return btn;
    }

    // --- SECTION: SUBMIT ---
    private JPanel createSubmitReportPanel() {
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(UIUtils.BACKGROUND_COLOR);

        // Center: The Report Card
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setBackground(UIUtils.BACKGROUND_COLOR);
        
        JPanel card = UIUtils.createCard(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(UIUtils.createLabel("Report Environmental Issue", UIUtils.FONT_TITLE, UIUtils.PRIMARY_COLOR), gbc);

        gbc.gridwidth = 1; gbc.gridy = 1;
        card.add(UIUtils.createLabel("Latitude:", UIUtils.FONT_BOLD, UIUtils.TEXT_COLOR), gbc);
        JTextField latField = UIUtils.createTextField();
        gbc.gridx = 1; card.add(latField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        card.add(UIUtils.createLabel("Longitude:", UIUtils.FONT_BOLD, UIUtils.TEXT_COLOR), gbc);
        JTextField lonField = UIUtils.createTextField();
        gbc.gridx = 1; card.add(lonField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        card.add(UIUtils.createLabel("Category:", UIUtils.FONT_BOLD, UIUtils.TEXT_COLOR), gbc);
        JComboBox<String> catBox = new JComboBox<>(Report.CATEGORIES);
        gbc.gridx = 1; card.add(catBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        card.add(UIUtils.createLabel("Description:", UIUtils.FONT_BOLD, UIUtils.TEXT_COLOR), gbc);
        JTextArea descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        gbc.gridx = 1; card.add(new JScrollPane(descArea), gbc);

        JButton submitBtn = UIUtils.createButton("SUBMIT REPORT", UIUtils.PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        card.add(submitBtn, gbc);
        
        cardWrapper.add(card);
        mainContainer.add(cardWrapper, BorderLayout.CENTER);

        // South: Penalty Box at the very bottom right
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setBackground(UIUtils.BACKGROUND_COLOR);
        bottomBar.setBorder(new javax.swing.border.EmptyBorder(0, 0, 10, 10));

        JPanel penaltyBox = new JPanel(new GridLayout(2, 1));
        penaltyBox.setBackground(Color.WHITE);
        penaltyBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel penaltyLbl = new JLabel("Penalty = " + user.getPenalties());
        penaltyLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        penaltyLbl.setForeground(user.getPenalties() > 0 ? UIUtils.DANGER_COLOR : new Color(0, 120, 0));
        
        JLabel warningLbl = new JLabel("Blocked after 3 fake reports");
        warningLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        warningLbl.setForeground(Color.GRAY);

        penaltyBox.add(penaltyLbl);
        penaltyBox.add(warningLbl);
        bottomBar.add(penaltyBox);

        mainContainer.add(bottomBar, BorderLayout.SOUTH);

        submitBtn.addActionListener(e -> {
            if (user.isBlocked()) {
                JOptionPane.showMessageDialog(this, "Your account has been BLOCKED due to multiple suspicious reports. You cannot submit new reports.", "Blocked", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                double lat = Double.parseDouble(latField.getText());
                double lon = Double.parseDouble(lonField.getText());
                String desc = descArea.getText();
                String category = (String) catBox.getSelectedItem();
                String date = java.time.LocalDate.now().toString();
                boolean ok = mainFrame.getReportService().submitReport(user, lat, lon, desc, date, category);
                if (ok) {
                    Storage.saveReports(mainFrame.getReportService().getAllReports());
                    Storage.saveUsers(mainFrame.getUsers());
                    pointsLabel.setText("Points: " + user.getPoints());
                    
                    // Check if assigned (status would be ASSIGNED)
                    Report lastReport = mainFrame.getReportService().getAllReports().get(mainFrame.getReportService().getAllReports().size() - 1);
                    if (Report.ASSIGNED.equals(lastReport.getStatus())) {
                        JOptionPane.showMessageDialog(this, "Report Submitted and Assigned! You earned 10 points.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No worker is available and your report is submitted and assigned to a worker when available.\nReport ID: " + lastReport.getId() + " | You earned 10 points.", "Report Submitted", JOptionPane.INFORMATION_MESSAGE);
                    }
                    latField.setText(""); lonField.setText(""); descArea.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Submission failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid coordinates.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return mainContainer;
    }

    // --- SECTION: MY REPORTS ---
    private JPanel createMyReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BACKGROUND_COLOR);
        panel.setBorder(UIUtils.createPadding(20, 20, 20, 20));

        // Header with "File Complaint" at TOP
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.BACKGROUND_COLOR);
        header.add(UIUtils.createLabel("My Submitted Reports", UIUtils.FONT_SUBTITLE, UIUtils.PRIMARY_COLOR), BorderLayout.WEST);
        
        JButton complainBtn = UIUtils.createButton("File Worker Complaint", UIUtils.DANGER_COLOR);
        header.add(complainBtn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Category", "Status", "Date", "Upvotes"};
        List<Report> myReports = mainFrame.getReportService().getAllReports().stream()
                .filter(r -> r.getUserId().equals(user.getId()))
                .collect(Collectors.toList());

        DefaultTableModel model = UIUtils.createNonEditableModel(cols);
        for (Report r : myReports) {
            model.addRow(new Object[]{r.getId(), r.getCategory(), r.getStatus(), r.getDate(), r.getUpvotes()});
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        complainBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select one of YOUR reports first.", "Guide", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String rid = (String) model.getValueAt(row, 0);
            mainFrame.getReportService().userReportWorker(rid, user.getId(), mainFrame.getWorkers());
            Storage.saveReports(mainFrame.getReportService().getAllReports());
            Storage.saveWorkers(mainFrame.getWorkers());
            refreshContent("MY_REPORTS");
            JOptionPane.showMessageDialog(this, "Complaint filed. Admin will review it.", "Done", JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    // --- SECTION: ALL REPORTS ---
    private JPanel createAllReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BACKGROUND_COLOR);
        panel.setBorder(UIUtils.createPadding(20, 20, 20, 20));
        panel.add(UIUtils.createLabel("Community Reports", UIUtils.FONT_SUBTITLE, UIUtils.PRIMARY_COLOR), BorderLayout.NORTH);
        String[] cols = {"ID", "Category", "Status", "Upvotes", "User"};
        List<Report> all = mainFrame.getReportService().getAllReports();
        DefaultTableModel model = UIUtils.createNonEditableModel(cols);
        for (Report r : all) {
            model.addRow(new Object[]{r.getId(), r.getCategory(), r.getStatus(), r.getUpvotes(), r.getUserId()});
        }
        JTable table = new JTable(model);
        table.setRowHeight(30);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(UIUtils.BACKGROUND_COLOR);
        JButton upvoteBtn = UIUtils.createButton("Upvote Report", UIUtils.SECONDARY_COLOR);
        footer.add(upvoteBtn);
        panel.add(footer, BorderLayout.SOUTH);

        upvoteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            String rid = (String) model.getValueAt(row, 0);
            boolean success = mainFrame.getReportService().upvoteReport(rid, user.getId());
            if (success) {
                Storage.saveReports(mainFrame.getReportService().getAllReports());
                refreshContent("ALL_REPORTS");
            } else {
                JOptionPane.showMessageDialog(this, "You cannot upvote your own report!", "Action Blocked", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }

    // --- SECTION: LEADERBOARD ---
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
        List<User> sortedUsers = mainFrame.getUsers().stream().sorted((a, b) -> b.getPoints() - a.getPoints()).collect(Collectors.toList());
        DefaultTableModel model = UIUtils.createNonEditableModel(cols);
        for (int i = 0; i < sortedUsers.size(); i++) {
            User u = sortedUsers.get(i);
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
