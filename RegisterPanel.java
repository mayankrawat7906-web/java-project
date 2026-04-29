package gui;

import javax.swing.*;
import java.awt.*;
import users.User;
import services.Storage;

public class RegisterPanel extends JPanel {

    private final MainFrame mainFrame;
    private final JTextField nameField;
    private final JTextField emailField;
    private final JPasswordField passwordField;

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(UIUtils.BACKGROUND_COLOR);

        JPanel card = UIUtils.createCard(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Header
        JLabel title = UIUtils.createLabel("Create Account", UIUtils.FONT_TITLE, UIUtils.PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(title, gbc);

        JLabel subtitle = UIUtils.createLabel("Join the environmental reporting community", UIUtils.FONT_REGULAR, Color.GRAY);
        gbc.gridy = 1;
        card.add(subtitle, gbc);

        // Fields
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        card.add(UIUtils.createLabel("Full Name:", UIUtils.FONT_BOLD, UIUtils.TEXT_COLOR), gbc);
        nameField = UIUtils.createTextField();
        gbc.gridx = 1;
        card.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        card.add(UIUtils.createLabel("Email:", UIUtils.FONT_BOLD, UIUtils.TEXT_COLOR), gbc);
        emailField = UIUtils.createTextField();
        gbc.gridx = 1;
        card.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        card.add(UIUtils.createLabel("Password:", UIUtils.FONT_BOLD, UIUtils.TEXT_COLOR), gbc);
        passwordField = UIUtils.createPasswordField();
        gbc.gridx = 1;
        card.add(passwordField, gbc);

        // Register Button
        JButton registerBtn = UIUtils.createButton("REGISTER", UIUtils.PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        card.add(registerBtn, gbc);

        // Back to Login
        JButton backBtn = new JButton("Already have an account? Login here.");
        backBtn.setFont(UIUtils.FONT_REGULAR);
        backBtn.setForeground(UIUtils.PRIMARY_COLOR);
        backBtn.setBorder(null);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 10, 10, 10);
        card.add(backBtn, gbc);

        add(card);

        // Actions
        registerBtn.addActionListener(e -> handleRegister());
        backBtn.addActionListener(e -> mainFrame.showView("LOGIN"));
    }

    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String pass = new String(passwordField.getPassword());

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (pass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (mainFrame.getUsers().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email))) {
            JOptionPane.showMessageDialog(this, "Email is already registered.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newId = "U" + (mainFrame.getUsers().size() + 1);
        User newUser = new User(newId, name, email, pass);
        mainFrame.getUsers().add(newUser);
        Storage.saveUsers(mainFrame.getUsers());

        JOptionPane.showMessageDialog(this, "Registration Successful! Your ID: " + newId, "Success", JOptionPane.INFORMATION_MESSAGE);
        mainFrame.showView("LOGIN");
    }
}
