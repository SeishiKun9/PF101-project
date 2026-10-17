import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Lets a new resident create their own account (a convenience on top of the
 * Admin's createUser() capability).
 */
public class RegisterFrame extends JFrame {
    private final FileManager fileManager;
    private final JFrame loginFrame;

    private final JTextField nameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JTextField addressField = new JTextField(16);
    private final JTextField contactField = new JTextField(16);

    public RegisterFrame(FileManager fileManager, JFrame loginFrame) {
        super("Resident Registration");
        this.fileManager = fileManager;
        this.loginFrame = loginFrame;
        buildUI();
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(440, 480);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 12, 28));

        JLabel title = new JLabel("Create Resident Account");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(JButtonStyle.TEXT_DARK);

        JLabel sub = new JLabel("Fill out the details below to register for barangay services.");
        sub.setFont(sub.getFont().deriveFont(Font.PLAIN, 12f));
        sub.setForeground(JButtonStyle.TEXT_MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(8, 28, 16, 28));

        nameField.setPreferredSize(new Dimension(380, 34));
        nameField.setBorder(JButtonStyle.createFieldBorder());
        passwordField.setPreferredSize(new Dimension(380, 34));
        passwordField.setBorder(JButtonStyle.createFieldBorder());
        addressField.setPreferredSize(new Dimension(380, 34));
        addressField.setBorder(JButtonStyle.createFieldBorder());
        contactField.setPreferredSize(new Dimension(380, 34));
        contactField.setBorder(JButtonStyle.createFieldBorder());

        form.add(createFieldLabel("Full Name"));
        form.add(nameField);
        form.add(createFieldLabel("Password"));
        form.add(passwordField);
        form.add(createFieldLabel("Residential Address"));
        form.add(addressField);
        form.add(createFieldLabel("Contact Number"));
        form.add(contactField);
        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 16));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, JButtonStyle.BORDER_LIGHT));

        JButton back = new JButton("Back to Login");
        JButton submit = new JButton("Create Account");
        JButtonStyle.applySecondary(back);
        JButtonStyle.applyAccent(submit);
        bottom.add(back);
        bottom.add(submit);
        add(bottom, BorderLayout.SOUTH);

        submit.addActionListener(e -> register());
        back.addActionListener(e -> {
            dispose();
            loginFrame.setVisible(true);
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                loginFrame.setVisible(true);
            }
        });
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        lbl.setForeground(new Color(51, 65, 85));
        return lbl;
    }

    private void register() {
        String name = nameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String address = addressField.getText().trim();
        String contactText = contactField.getText().trim();

        if (name.isEmpty() || password.isEmpty() || address.isEmpty() || contactText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!contactText.matches("\\+?[0-9]+")) {
            JOptionPane.showMessageDialog(this, "Contact number must be numeric.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<User> existing = fileManager.readUsers();
        for (User u : existing) {
            if (u.getFullName().equalsIgnoreCase(name)) {
                JOptionPane.showMessageDialog(this, "That name is already registered.",
                        "Duplicate Account", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String residentId = fileManager.nextResidentId();
        Resident resident = new Resident(name, password, residentId, address, contactText);
        fileManager.saveUser(resident);

        JOptionPane.showMessageDialog(this, "Account created! You may now log in.",
                "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        loginFrame.setVisible(true);
    }
}
