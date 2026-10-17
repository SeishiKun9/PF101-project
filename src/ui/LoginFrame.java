package ui;

import data.FileManager;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Modern login window shared by every role, with resident self-registration support.
 */
public class LoginFrame extends JFrame {
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

        // Left Branding / Hero Panel
        JPanel leftHero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient from Deep Navy to Indigo Blue
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42),
                        getWidth(), getHeight(), new Color(30, 58, 138));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative subtle circle pattern in corner
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fill(new Ellipse2D.Double(getWidth() - 120, -50, 240, 240));
                g2.fill(new Ellipse2D.Double(-80, getHeight() - 140, 260, 260));

                // Draw emblem crest
                drawEmblem(g2, 50, 48);
                g2.dispose();
            }

            private void drawEmblem(Graphics2D g2, int x, int y) {
                // Outer circle
                g2.setColor(new Color(59, 130, 246, 200));
                g2.fillOval(x, y, 54, 54);
                g2.setColor(new Color(147, 197, 253));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(x, y, 54, 54);

                // Inner shield/icon representation
                g2.setColor(Color.WHITE);
                int[] px = {x + 27, x + 43, x + 38, x + 27, x + 16, x + 11};
                int[] py = {y + 11, y + 17, y + 36, y + 44, y + 36, y + 17};
                g2.fillPolygon(px, py, 6);
                g2.setColor(new Color(30, 58, 138));
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String star = "\u2605"; // star symbol
                g2.drawString(star, x + 27 - fm.stringWidth(star) / 2, y + 33);
            }
        };
        leftHero.setPreferredSize(new Dimension(360, 530));
        leftHero.setLayout(new BoxLayout(leftHero, BoxLayout.Y_AXIS));
        leftHero.setBorder(BorderFactory.createEmptyBorder(115, 42, 36, 42));

        JLabel portalTag = new JLabel("PUBLIC ASSISTANCE SYSTEM");
        portalTag.setFont(portalTag.getFont().deriveFont(Font.BOLD, 11f));
        portalTag.setForeground(new Color(147, 197, 253));

        JLabel portalTitle = new JLabel("Barangay Portal");
        portalTitle.setFont(portalTitle.getFont().deriveFont(Font.BOLD, 26f));
        portalTitle.setForeground(Color.WHITE);

        JLabel portalDesc = new JLabel("<html>Prompt, reliable, and transparent public assistance for all community residents.</html>");
        portalDesc.setFont(portalDesc.getFont().deriveFont(Font.PLAIN, 12.5f));
        portalDesc.setForeground(new Color(203, 213, 225));
        portalDesc.setPreferredSize(new Dimension(270, 44));

        leftHero.add(portalTag);
        leftHero.add(Box.createVerticalStrut(6));
        leftHero.add(portalTitle);
        leftHero.add(Box.createVerticalStrut(10));
        leftHero.add(portalDesc);
        leftHero.add(Box.createVerticalStrut(24));

        // Service badges
        leftHero.add(createHeroPill("Medical Supplies & Consultation"));
        leftHero.add(Box.createVerticalStrut(8));
        leftHero.add(createHeroPill("Food Subsidy & Relief Assistance"));
        leftHero.add(Box.createVerticalStrut(8));
        leftHero.add(createHeroPill("Community & Emergency Transport"));

        leftHero.add(Box.createVerticalGlue());

        // Quick login hint panel
        JPanel hintCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        hintCard.setOpaque(false);
        hintCard.setLayout(new BoxLayout(hintCard, BoxLayout.Y_AXIS));
        hintCard.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel hintTitle = new JLabel("Sample Test Accounts:");
        hintTitle.setFont(hintTitle.getFont().deriveFont(Font.BOLD, 10.5f));
        hintTitle.setForeground(new Color(226, 232, 240));
        JLabel hint1 = new JLabel("Resident: RSD3000 (res123) \u2022 Admin: ADM1000");
        hint1.setFont(hint1.getFont().deriveFont(Font.PLAIN, 10f));
        hint1.setForeground(new Color(186, 204, 234));
        JLabel hint2 = new JLabel("Staff: STF2000 (Medical) \u2022 STF2001 (Food)");
        hint2.setFont(hint2.getFont().deriveFont(Font.PLAIN, 10f));
        hint2.setForeground(new Color(186, 204, 234));
        hintCard.add(hintTitle);
        hintCard.add(Box.createVerticalStrut(2));
        hintCard.add(hint1);
        hintCard.add(hint2);
        leftHero.add(hintCard);

        add(leftHero, BorderLayout.WEST);

        // Right Login Form Panel
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(Color.WHITE);
        formCard.setPreferredSize(new Dimension(360, 420));

        JLabel welcomeTitle = new JLabel("Welcome Back");
        welcomeTitle.setFont(welcomeTitle.getFont().deriveFont(Font.BOLD, 24f));
        welcomeTitle.setForeground(JButtonStyle.TEXT_DARK);

        JLabel welcomeSub = new JLabel("Please enter your User ID and password.");
        welcomeSub.setFont(welcomeSub.getFont().deriveFont(Font.PLAIN, 13f));
        welcomeSub.setForeground(JButtonStyle.TEXT_MUTED);

        formCard.add(welcomeTitle);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(welcomeSub);
        formCard.add(Box.createVerticalStrut(26));

        // User ID Field
        JLabel idLabel = new JLabel("User ID");
        idLabel.setFont(idLabel.getFont().deriveFont(Font.BOLD, 12f));
        idLabel.setForeground(new Color(51, 65, 85));
        userIdField.setPreferredSize(new Dimension(360, 38));
        userIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        userIdField.setFont(userIdField.getFont().deriveFont(13.5f));
        userIdField.setBorder(JButtonStyle.createFieldBorder());

        formCard.add(idLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(userIdField);
        formCard.add(Box.createVerticalStrut(16));

        // Password Field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(passLabel.getFont().deriveFont(Font.BOLD, 12f));
        passLabel.setForeground(new Color(51, 65, 85));
        passwordField.setPreferredSize(new Dimension(360, 38));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setFont(passwordField.getFont().deriveFont(13.5f));
        passwordField.setBorder(JButtonStyle.createFieldBorder());

        formCard.add(passLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(passwordField);
        formCard.add(Box.createVerticalStrut(24));

        // Login Button
        JButton loginBtn = new JButton("Sign In");
        JButtonStyle.applyAccent(loginBtn);
        loginBtn.setPreferredSize(new Dimension(360, 40));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        formCard.add(loginBtn);
        formCard.add(Box.createVerticalStrut(14));

        // Divider
        JPanel divPanel = new JPanel(new GridBagLayout());
        divPanel.setBackground(Color.WHITE);
        divPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JSeparator s1 = new JSeparator(); s1.setForeground(JButtonStyle.BORDER_LIGHT);
        JSeparator s2 = new JSeparator(); s2.setForeground(JButtonStyle.BORDER_LIGHT);
        JLabel orLabel = new JLabel(" OR ");
        orLabel.setFont(orLabel.getFont().deriveFont(Font.BOLD, 10.5f));
        orLabel.setForeground(new Color(148, 163, 184));
        GridBagConstraints dc = new GridBagConstraints();
        dc.fill = GridBagConstraints.HORIZONTAL;
        dc.weightx = 1.0;
        divPanel.add(s1, dc);
        dc.fill = GridBagConstraints.NONE;
        dc.weightx = 0;
        divPanel.add(orLabel, dc);
        dc.fill = GridBagConstraints.HORIZONTAL;
        dc.weightx = 1.0;
        divPanel.add(s2, dc);
        formCard.add(divPanel);
        formCard.add(Box.createVerticalStrut(14));

        // Register Button for new Residents
        JButton registerBtn = new JButton("Register as New Resident");
        JButtonStyle.applySecondary(registerBtn);
        registerBtn.setPreferredSize(new Dimension(360, 36));
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        formCard.add(registerBtn);

        rightPanel.add(formCard);
        add(rightPanel, BorderLayout.CENTER);

        // Actions
        loginBtn.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
        userIdField.addActionListener(e -> attemptLogin());

        registerBtn.addActionListener(e -> {
            setVisible(false);
            new RegisterFrame(fileManager, this).setVisible(true);
        });
    }

    private JPanel createHeroPill(String text) {
        JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        pill.setOpaque(false);
        pill.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel dot = new JLabel("\u2022");
        dot.setFont(dot.getFont().deriveFont(Font.BOLD, 14f));
        dot.setForeground(new Color(147, 197, 253));
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 12f));
        lbl.setForeground(new Color(241, 245, 249));
        pill.add(dot);
        pill.add(lbl);
        return pill;
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
            || normalizedId.startsWith("RSD")
            || normalizedId.startsWith("RES");
    }
}
