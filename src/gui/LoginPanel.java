package gui;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import users.*;

public class LoginPanel extends JPanel {

    private enum Role { USER, WORKER, ADMIN }
    private Role selectedRole = Role.USER;

    private final MainFrame mainFrame;
    private final JTextField idField;
    private final JPasswordField passwordField;
    private final JLabel idLabel;
    
    private final JButton btnUser;
    private final JButton btnWorker;
    private final JButton btnAdmin;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(UIUtils.BACKGROUND_COLOR);

        JPanel card = UIUtils.createCard(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Header ---
        JLabel title = UIUtils.createLabel("Environmental Reporting", UIUtils.FONT_TITLE, UIUtils.PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 15, 5, 15);
        card.add(title, gbc);

        JLabel subtitle = UIUtils.createLabel("Select your role and sign in", UIUtils.FONT_REGULAR, Color.GRAY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 15, 20, 15);
        card.add(subtitle, gbc);

        // --- Role Selector ---
        JPanel rolePanel = new JPanel(new GridLayout(1, 3, 0, 0));
        rolePanel.setBackground(Color.WHITE);
        
        btnUser   = UIUtils.createRoleButton("USER");
        btnWorker = UIUtils.createRoleButton("WORKER");
        btnAdmin  = UIUtils.createRoleButton("ADMIN");

        rolePanel.add(btnUser);
        rolePanel.add(btnWorker);
        rolePanel.add(btnAdmin);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 15, 25, 15);
        card.add(rolePanel, gbc);

        // --- Input Fields ---
        gbc.gridwidth = 3;
        gbc.gridy = 3;
        idLabel = UIUtils.createLabel("User Email / ID:", UIUtils.FONT_BOLD, UIUtils.TEXT_COLOR);
        gbc.insets = new Insets(0, 15, 5, 15);
        card.add(idLabel, gbc);

        idField = UIUtils.createTextField();
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 15, 15, 15);
        card.add(idField, gbc);

        gbc.gridy = 5;
        card.add(UIUtils.createLabel("Password:", UIUtils.FONT_BOLD, UIUtils.TEXT_COLOR), gbc);

        passwordField = UIUtils.createPasswordField();
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 15, 25, 15);
        card.add(passwordField, gbc);

        // --- Login Button ---
        JButton loginBtn = UIUtils.createButton("LOGIN", UIUtils.PRIMARY_COLOR);
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 15, 10, 15);
        card.add(loginBtn, gbc);

        // --- Register Link ---
        JButton registerLink = new JButton("New user? Create an account.");
        registerLink.setFont(UIUtils.FONT_REGULAR);
        registerLink.setForeground(UIUtils.PRIMARY_COLOR);
        registerLink.setBorder(null);
        registerLink.setContentAreaFilled(false);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 8;
        card.add(registerLink, gbc);

        add(card);

        // --- Logic & Listeners ---
        updateRoleSelection(Role.USER);

        btnUser.addActionListener(e -> updateRoleSelection(Role.USER));
        btnWorker.addActionListener(e -> updateRoleSelection(Role.WORKER));
        btnAdmin.addActionListener(e -> updateRoleSelection(Role.ADMIN));

        loginBtn.addActionListener(e -> handleLogin());
        registerLink.addActionListener(e -> mainFrame.showView("REGISTER"));
    }

    private void updateRoleSelection(Role role) {
        this.selectedRole = role;
        
        // Update selection UI
        btnUser.setBackground(role == Role.USER ? UIUtils.SECONDARY_COLOR : Color.WHITE);
        btnUser.setForeground(role == Role.USER ? Color.DARK_GRAY : UIUtils.TEXT_COLOR);
        
        btnWorker.setBackground(role == Role.WORKER ? UIUtils.SECONDARY_COLOR : Color.WHITE);
        btnWorker.setForeground(role == Role.WORKER ? Color.DARK_GRAY : UIUtils.TEXT_COLOR);
        
        btnAdmin.setBackground(role == Role.ADMIN ? UIUtils.SECONDARY_COLOR : Color.WHITE);
        btnAdmin.setForeground(role == Role.ADMIN ? Color.DARK_GRAY : UIUtils.TEXT_COLOR);

        // Update Labels
        switch (role) {
            case USER:   idLabel.setText("User Email / ID:"); break;
            case WORKER: idLabel.setText("Worker ID:");       break;
            case ADMIN:  idLabel.setText("Admin ID:");        break;
        }
        idField.setText("");
        passwordField.setText("");
    }

    private void handleLogin() {
        String input = idField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (input.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all details.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        switch (selectedRole) {
            case ADMIN:
                Optional<Admin> admin = mainFrame.getAdmins().stream()
                        .filter(a -> a.getId().equals(input) && a.getPassword().equals(pass))
                        .findFirst();
                if (admin.isPresent()) {
                    mainFrame.onLoginSuccess(admin.get());
                } else {
                    showError();
                }
                break;

            case WORKER:
                Optional<Worker> worker = mainFrame.getWorkers().stream()
                        .filter(w -> w.getId().equals(input) && w.getPassword().equals(pass))
                        .findFirst();
                if (worker.isPresent()) {
                    mainFrame.onLoginSuccess(worker.get());
                } else {
                    showError();
                }
                break;

            case USER:
                Optional<User> user = mainFrame.getUsers().stream()
                        .filter(u -> (u.getEmail().equalsIgnoreCase(input) || u.getId().equalsIgnoreCase(input)) 
                                     && u.getPassword().equals(pass))
                        .findFirst();
                if (user.isPresent()) {
                    mainFrame.onLoginSuccess(user.get());
                } else {
                    showError();
                }
                break;
        }
    }

    private void showError() {
        JOptionPane.showMessageDialog(this, "Invalid ID/Email or Password for " + selectedRole, "Login Error", JOptionPane.ERROR_MESSAGE);
    }
}
