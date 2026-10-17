package ui;

import storage.FileManager;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Formal login window for Barangay Assistance System.
 */
public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final FileManager fileManager;
    private final JTextField userIdField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginFrame(FileManager fileManager) {
        super("Barangay Assistance System - Portal Login");
        this.fileManager = fileManager;
        buildUI();
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 530);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Top header bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 40, 80));
        headerPanel.setPreferredSize(new Dimension(860, 90));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 40, 18, 40));

        JLabel titleLabel = new JLabel("Barangay Assistance System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Official Portal for Community Public Assistance");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(180, 200, 230));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(titleLabel);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitleLabel);

        headerPanel.add(titleBox, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Center form area
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(245, 247, 250));

        // Form card
        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225), 1),
                BorderFactory.createEmptyBorder(36, 48, 36, 48)
        ));
        formCard.setPreferredSize(new Dimension(420, 320));

        JLabel formTitle = new JLabel("User Login");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        formTitle.setForeground(new Color(30, 50, 90));
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel formSub = new JLabel("Enter your credentials to access the system");
        formSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        formSub.setForeground(new Color(100, 110, 130));
        formSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(formTitle);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(formSub);
        formCard.add(Box.createVerticalStrut(28));

        // User ID
        JLabel idLabel = new JLabel("User ID");
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        idLabel.setForeground(new Color(50, 60, 80));
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userIdField.setPreferredSize(new Dimension(320, 38));
        userIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        userIdField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        userIdField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        userIdField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(idLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(userIdField);
        formCard.add(Box.createVerticalStrut(18));

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        passLabel.setForeground(new Color(50, 60, 80));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField.setPreferredSize(new Dimension(320, 38));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(passLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(passwordField);
        formCard.add(Box.createVerticalStrut(28));

        // Sign In button
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginBtn.setBackground(new Color(25, 70, 140));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(320, 42));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(loginBtn);

        centerPanel.add(formCard);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(230, 235, 242));
        footerPanel.setPreferredSize(new Dimension(860, 36));
        JLabel footerLabel = new JLabel("Authorized personnel only  •  Barangay Local Government Unit");
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(90, 100, 120));
        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);

        // Actions
        loginBtn.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
        userIdField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String userId = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both User ID and password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<User> users = fileManager.readUsers();
        for (User u : users) {
            String storedUserId = getUserId(u);
            if (storedUserId != null && storedUserId.equalsIgnoreCase(userId)
                    && hasValidPrefix(storedUserId) && u.authenticate(password) && u.login()) {
                dispose();
                u.showMenu();
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Invalid User ID or password.",
                "Login Failed", JOptionPane.ERROR_MESSAGE);
    }

    private String getUserId(User user) {
        return user.getUserId();
    }

    private boolean hasValidPrefix(String userId) {
        String normalizedId = userId.toUpperCase();
        return normalizedId.startsWith("ADM")
            || normalizedId.startsWith("STF")
            || normalizedId.startsWith("RSD");
    }
}