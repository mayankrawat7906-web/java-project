package gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UIUtils {

    // --- Basic Color Palette ---
    public static final Color PRIMARY_COLOR    = Color.BLUE;
    public static final Color SECONDARY_COLOR  = new Color(100, 149, 237); // Cornflower Blue
    public static final Color ACCENT_COLOR     = Color.DARK_GRAY;
    public static final Color BACKGROUND_COLOR = Color.WHITE;
    public static final Color TEXT_COLOR       = Color.BLACK;
    public static final Color DANGER_COLOR     = Color.RED;
    public static final Color BORDER_COLOR     = Color.LIGHT_GRAY;
    public static final Color HOVER_COLOR      = Color.WHITE; // No hover effect
    public static final Color SELECTED_COLOR   = Color.LIGHT_GRAY;

    // --- Basic Typography ---
    public static final Font FONT_TITLE    = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_BOLD     = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_REGULAR  = new Font("SansSerif", Font.PLAIN, 13);

    public static JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.BLACK); // Standard black text
        btn.setBackground(Color.LIGHT_GRAY); // Standard button color
        btn.setFocusPainted(true);
        btn.setMargin(new Insets(5, 10, 5, 10));
        return btn;
    }

    public static JButton createRoleButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.BLACK);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        return btn;
    }

    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(FONT_REGULAR);
        field.setPreferredSize(new Dimension(200, 30));
        field.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        return field;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(200, 30));
        field.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        return field;
    }

    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    public static JPanel createCard(LayoutManager layout) {
        JPanel card = new JPanel(layout);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createTitledBorder("")); // Simple titled border variant or line border
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        return card;
    }

    public static Border createPadding(int t, int l, int b, int r) {
        return new EmptyBorder(t, l, b, r);
    }

    public static javax.swing.table.DefaultTableModel createNonEditableModel(Object[] columns) {
        return new javax.swing.table.DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
