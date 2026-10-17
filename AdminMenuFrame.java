import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * GUI for a logged-in Admin: create/manage/delete users, view all requests,
 * and view a specific resident's request history.
 */
public class AdminMenuFrame extends JFrame {
    private final Admin admin;
    private final FileManager fileManager = new FileManager();
    private final RequestManager requestManager = new RequestManager(fileManager);

    private final DefaultTableModel userTableModel =
            new DefaultTableModel(new Object[]{"Role", "Name", "ID", "Extra Info"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable userTable = new JTable(userTableModel);
    private final JTextField userSearchField = new JTextField(16);

    private final DefaultTableModel requestTableModel =
            new DefaultTableModel(new Object[]{"Request ID", "Resident ID", "Type", "Status"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable requestTable = new JTable(requestTableModel);
    private final JTextField requestSearchField = new JTextField(16);

    // KPI Stat Labels
    private final JLabel userCountLabel = new JLabel("0");
    private final JLabel residentCountLabel = new JLabel("0");
    private final JLabel staffCountLabel = new JLabel("0");
    private final JLabel requestCountLabel = new JLabel("0");

    public AdminMenuFrame(Admin admin) {
        super("Admin Menu - " + admin.getFullName());
        this.admin = admin;
        buildUI();
        refreshUserTable();
        refreshRequestTable(null);
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1040, 680);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(JButtonStyle.BG_PAGE);

        // NATIVE MENU BAR (Admin Menu)
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, JButtonStyle.BORDER_LIGHT));

        JMenu adminMenu = new JMenu("Admin Menu");
        adminMenu.setFont(adminMenu.getFont().deriveFont(Font.BOLD, 12f));

        JMenuItem createMenuItem = new JMenuItem("+ Create User");
        JMenuItem editMenuItem = new JMenuItem("Manage Selected User");
        JMenuItem historyMenuItem = new JMenuItem("View User History");
        JMenuItem viewReqMenuItem = new JMenuItem("View Request Details");
        JMenuItem refreshReqMenuItem = new JMenuItem("Refresh All Requests");
        JMenuItem deleteReqMenuItem = new JMenuItem("Delete Selected Request");
        JMenuItem logoutMenuItem = new JMenuItem("Logout");

        adminMenu.add(createMenuItem);
        adminMenu.add(editMenuItem);
        adminMenu.add(historyMenuItem);
        adminMenu.addSeparator();
        adminMenu.add(viewReqMenuItem);
        adminMenu.add(refreshReqMenuItem);
        adminMenu.add(deleteReqMenuItem);
        adminMenu.addSeparator();
        adminMenu.add(logoutMenuItem);
        menuBar.add(adminMenu);
        setJMenuBar(menuBar);

        // TOP HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, JButtonStyle.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        userInfoPanel.setOpaque(false);

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(243, 232, 255));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(new Color(216, 180, 254));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(new Color(126, 34, 206));
                g2.setFont(new Font("SansSerif", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                String in = "AD";
                g2.drawString(in, (getWidth() - fm.stringWidth(in)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(44, 44));
        avatar.setOpaque(false);
        userInfoPanel.add(avatar);

        JPanel nameAndMeta = new JPanel();
        nameAndMeta.setLayout(new BoxLayout(nameAndMeta, BoxLayout.Y_AXIS));
        nameAndMeta.setOpaque(false);

        JPanel titleRoleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRoleRow.setOpaque(false);
        JLabel nameLbl = new JLabel(admin.getFullName());
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 17f));
        nameLbl.setForeground(JButtonStyle.TEXT_DARK);

        JLabel rolePill = new JLabel(" SYSTEM ADMIN ");
        rolePill.setFont(rolePill.getFont().deriveFont(Font.BOLD, 10.5f));
        rolePill.setOpaque(true);
        rolePill.setBackground(new Color(243, 232, 255));
        rolePill.setForeground(new Color(126, 34, 206));
        rolePill.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        titleRoleRow.add(nameLbl);
        titleRoleRow.add(rolePill);

        JLabel metaLbl = new JLabel("Admin ID: " + admin.getAdminId() + "  \u2022  Full System Access");
        metaLbl.setFont(metaLbl.getFont().deriveFont(Font.PLAIN, 12f));
        metaLbl.setForeground(JButtonStyle.TEXT_MUTED);

        nameAndMeta.add(titleRoleRow);
        nameAndMeta.add(Box.createVerticalStrut(3));
        nameAndMeta.add(metaLbl);
        userInfoPanel.add(nameAndMeta);
        headerPanel.add(userInfoPanel, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        JButtonStyle.applyDanger(logoutBtn);
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        rightHeader.setOpaque(false);
        rightHeader.add(logoutBtn);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // MAIN CONTENT CONTAINER
        JPanel mainContent = new JPanel(new BorderLayout(0, 16));
        mainContent.setBackground(JButtonStyle.BG_PAGE);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // STATS ROW
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(statsRow.getPreferredSize().width, 82));
        statsRow.add(JButtonStyle.createStatCard("Total Users", userCountLabel, new Color(59, 130, 246), "Registered accounts"));
        statsRow.add(JButtonStyle.createStatCard("Residents", residentCountLabel, new Color(16, 185, 129), "Community members"));
        statsRow.add(JButtonStyle.createStatCard("Staff Members", staffCountLabel, new Color(99, 102, 241), "Assigned personnel"));
        statsRow.add(JButtonStyle.createStatCard("Total Requests", requestCountLabel, new Color(245, 158, 11), "All system requests"));
        mainContent.add(statsRow, BorderLayout.NORTH);

        // TABBED PANE
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(tabs.getFont().deriveFont(Font.BOLD, 12.5f));
        tabs.setBackground(Color.WHITE);

        // Style tables
        JButtonStyle.styleTable(userTable);
        JButtonStyle.styleTable(requestTable);
        requestTable.getColumnModel().getColumn(3).setCellRenderer(new JButtonStyle.StatusBadgeRenderer());

        // USERS TAB
        JPanel userPanel = new JPanel(new BorderLayout(0, 12));
        userPanel.setBackground(JButtonStyle.BG_PAGE);
        userPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel userToolbar = new JPanel(new BorderLayout());
        userToolbar.setOpaque(false);

        JPanel userButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userButtons.setOpaque(false);
        JButton createBtn = new JButton("+ Create User");
        JButton editBtn = new JButton("Manage Selected");
        JButton historyBtn = new JButton("View History");
        JButtonStyle.applyAccent(createBtn);
        JButtonStyle.applySecondary(editBtn);
        JButtonStyle.applySecondary(historyBtn);
        userButtons.add(createBtn);
        userButtons.add(editBtn);
        userButtons.add(historyBtn);

        JPanel userSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        userSearchPanel.setOpaque(false);
        JLabel uSearchLbl = new JLabel("Search:");
        uSearchLbl.setFont(uSearchLbl.getFont().deriveFont(Font.BOLD, 12f));
        uSearchLbl.setForeground(JButtonStyle.TEXT_MUTED);
        userSearchField.setPreferredSize(new Dimension(190, 32));
        userSearchField.setBorder(JButtonStyle.createFieldBorder());
        JButton searchUsersBtn = new JButton("Search");
        JButton clearUserSearchBtn = new JButton("Clear");
        JButtonStyle.applySecondary(searchUsersBtn);
        JButtonStyle.applySecondary(clearUserSearchBtn);
        userSearchPanel.add(uSearchLbl);
        userSearchPanel.add(userSearchField);
        userSearchPanel.add(searchUsersBtn);
        userSearchPanel.add(clearUserSearchBtn);

        userToolbar.add(userButtons, BorderLayout.WEST);
        userToolbar.add(userSearchPanel, BorderLayout.EAST);

        JScrollPane userScrollPane = new JScrollPane(userTable);
        userScrollPane.getViewport().setBackground(Color.WHITE);
        userScrollPane.setBorder(BorderFactory.createLineBorder(JButtonStyle.BORDER_LIGHT, 1, true));

        userPanel.add(userToolbar, BorderLayout.NORTH);
        userPanel.add(userScrollPane, BorderLayout.CENTER);
        tabs.addTab("  User Management  ", userPanel);

        // REQUESTS TAB
        JPanel reqPanel = new JPanel(new BorderLayout(0, 12));
        reqPanel.setBackground(JButtonStyle.BG_PAGE);
        reqPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel reqToolbar = new JPanel(new BorderLayout());
        reqToolbar.setOpaque(false);

        JPanel reqButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        reqButtons.setOpaque(false);
        JButton refreshReqBtn = new JButton("Refresh All");
        JButton viewRequestDetailsBtn = new JButton("View Details");
        JButton deleteRequestBtn = new JButton("Delete Request");
        JButtonStyle.applySecondary(refreshReqBtn);
        JButtonStyle.applyAccent(viewRequestDetailsBtn);
        JButtonStyle.applyDanger(deleteRequestBtn);
        reqButtons.add(viewRequestDetailsBtn);
        reqButtons.add(refreshReqBtn);
        reqButtons.add(deleteRequestBtn);

        JPanel requestSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        requestSearchPanel.setOpaque(false);
        JLabel rSearchLbl = new JLabel("Search:");
        rSearchLbl.setFont(rSearchLbl.getFont().deriveFont(Font.BOLD, 12f));
        rSearchLbl.setForeground(JButtonStyle.TEXT_MUTED);
        requestSearchField.setPreferredSize(new Dimension(190, 32));
        requestSearchField.setBorder(JButtonStyle.createFieldBorder());
        JButton searchRequestsBtn = new JButton("Search");
        JButton clearRequestSearchBtn = new JButton("Clear");
        JButtonStyle.applySecondary(searchRequestsBtn);
        JButtonStyle.applySecondary(clearRequestSearchBtn);
        requestSearchPanel.add(rSearchLbl);
        requestSearchPanel.add(requestSearchField);
        requestSearchPanel.add(searchRequestsBtn);
        requestSearchPanel.add(clearRequestSearchBtn);

        reqToolbar.add(reqButtons, BorderLayout.WEST);
        reqToolbar.add(requestSearchPanel, BorderLayout.EAST);

        JScrollPane reqScrollPane = new JScrollPane(requestTable);
        reqScrollPane.getViewport().setBackground(Color.WHITE);
        reqScrollPane.setBorder(BorderFactory.createLineBorder(JButtonStyle.BORDER_LIGHT, 1, true));

        reqPanel.add(reqToolbar, BorderLayout.NORTH);
        reqPanel.add(reqScrollPane, BorderLayout.CENTER);
        tabs.addTab("  All Assistance Requests  ", reqPanel);

        mainContent.add(tabs, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        // EVENT LISTENERS
        createBtn.addActionListener(e -> createUserDialog());
        createMenuItem.addActionListener(e -> createUserDialog());

        editBtn.addActionListener(e -> editSelectedUser());
        editMenuItem.addActionListener(e -> editSelectedUser());

        historyBtn.addActionListener(e -> { admin.viewUserHistory(); viewSelectedUserHistory(); });
        historyMenuItem.addActionListener(e -> { admin.viewUserHistory(); viewSelectedUserHistory(); });

        searchUsersBtn.addActionListener(e -> refreshUserTable(userSearchField.getText().trim()));
        userSearchField.addActionListener(e -> refreshUserTable(userSearchField.getText().trim()));
        clearUserSearchBtn.addActionListener(e -> {
            userSearchField.setText("");
            refreshUserTable("");
        });

        refreshReqBtn.addActionListener(e -> {
            admin.viewAllRequest();
            refreshRequestTable(requestSearchField.getText().trim());
        });
        refreshReqMenuItem.addActionListener(e -> {
            admin.viewAllRequest();
            refreshRequestTable(requestSearchField.getText().trim());
        });

        searchRequestsBtn.addActionListener(e -> refreshRequestTable(requestSearchField.getText().trim()));
        requestSearchField.addActionListener(e -> refreshRequestTable(requestSearchField.getText().trim()));
        clearRequestSearchBtn.addActionListener(e -> {
            requestSearchField.setText("");
            refreshRequestTable("");
        });

        viewRequestDetailsBtn.addActionListener(e -> viewSelectedRequestDetails());
        viewReqMenuItem.addActionListener(e -> viewSelectedRequestDetails());

        deleteRequestBtn.addActionListener(e -> deleteSelectedRequest());
        deleteReqMenuItem.addActionListener(e -> deleteSelectedRequest());

        logoutBtn.addActionListener(e -> {
            admin.logout();
            dispose();
            new LoginFrame(fileManager).setVisible(true);
        });
        logoutMenuItem.addActionListener(e -> {
            admin.logout();
            dispose();
            new LoginFrame(fileManager).setVisible(true);
        });
    }

    private void refreshUserTable() {
        refreshUserTable(userSearchField.getText().trim());
    }

    private void refreshUserTable(String searchTerm) {
        userTableModel.setRowCount(0);
        String query = searchTerm == null ? "" : searchTerm.toLowerCase();
        List<User> users = fileManager.readUsers();

        int totalUsers = users.size();
        int residentCount = 0;
        int staffCount = 0;

        for (User u : users) {
            String id = "-";
            String extra = "-";
            if (u instanceof Resident) {
                residentCount++;
                Resident resident = (Resident) u;
                id = resident.getResidentId();
                extra = resident.getAddress() + " / " + resident.getContactNo();
            } else if (u instanceof Admin) {
                id = ((Admin) u).getAdminId();
            } else if (u instanceof Staff) {
                staffCount++;
                Staff staff = (Staff) u;
                id = staff.getStaffId();
                extra = "Handles: " + staff.getAssignedType();
            }
            String searchable = (u.getRole() + " " + u.getFullName() + " " + id + " " + extra).toLowerCase();
            if (!query.isEmpty() && !searchable.contains(query)) continue;
            userTableModel.addRow(new Object[]{u.getRole(), u.getFullName(), id, extra});
        }

        userCountLabel.setText(String.valueOf(totalUsers));
        residentCountLabel.setText(String.valueOf(residentCount));
        staffCountLabel.setText(String.valueOf(staffCount));
    }

    private void refreshRequestTable(String searchTerm) {
        requestTableModel.setRowCount(0);
        String query = searchTerm == null ? "" : searchTerm.toLowerCase();
        List<AssistanceRequest> all = requestManager.getAllRequests();
        requestCountLabel.setText(String.valueOf(all.size()));

        for (AssistanceRequest r : all) {
            if (!query.isEmpty()
                    && !r.getRequestId().toLowerCase().contains(query)
                    && !r.getResidentId().toLowerCase().contains(query)
                    && !r.getStatus().toLowerCase().contains(query)) continue;
            requestTableModel.addRow(new Object[]{r.getRequestId(), r.getResidentId(), r.getType(),
                    r.getStatus()});
        }
    }

    private void viewSelectedRequestDetails() {
        int row = requestTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String requestId = (String) requestTableModel.getValueAt(row, 0);
        AssistanceRequest request = requestManager.findRequest(requestId);
        if (request == null) return;

        JPanel detailFields = new JPanel(new GridLayout(0, 2, 8, 6));
        addRequestDetailRow(detailFields, "Request ID", request.getRequestId());
        addRequestDetailRow(detailFields, "Resident ID", request.getResidentId());
        addRequestDetailRow(detailFields, "Type", request.getType());
        addRequestDetailRow(detailFields, "Time needed", request.getTimeNeeded().format(AssistanceRequest.DT_FMT));
        addRequestDetailRow(detailFields, "Additional details", request.getAdditionalDetails());
        addRequestDetailRow(detailFields, "Current status", request.getStatus());
        addRequestDetailRow(detailFields, "Submitted", request.getDateRequested().format(AssistanceRequest.DT_FMT));

        if (request instanceof MedicalRequest) {
            MedicalRequest medical = (MedicalRequest) request;
            addRequestDetailRow(detailFields, "Location", request.getLocation());
            int itemNumber = 1;
            for (MedicalItem item : medical.getMedicalItems()) {
                addRequestDetailRow(detailFields, "Medical item " + itemNumber++, item.toString());
            }
        } else if (request instanceof FoodRequest) {
            FoodRequest food = (FoodRequest) request;
            addRequestDetailRow(detailFields, "Location", request.getLocation());
            int itemNumber = 1;
            for (FoodItem item : food.getFoodItems()) {
                addRequestDetailRow(detailFields, "Food item " + itemNumber++, item.toString());
            }
        } else if (request instanceof TransportationRequest) {
            TransportationRequest transportation = (TransportationRequest) request;
            addRequestDetailRow(detailFields, "Pickup location", transportation.getPickupLocation());
            addRequestDetailRow(detailFields, "Destination", transportation.getDestination());
            addRequestDetailRow(detailFields, "Passengers", String.valueOf(transportation.getPassengerCount()));
        }

        DefaultTableModel historyModel = new DefaultTableModel(
                new Object[]{"Staff ID", "Status", "Updated At"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (StaffRequestHistory entry : fileManager.readStaffRequestHistory()) {
            if (entry.getRequestId().equals(requestId)) {
                historyModel.addRow(new Object[]{entry.getStaffId(), entry.getStatus(),
                        entry.getProcessedAt().format(AssistanceRequest.DT_FMT)});
            }
        }

        DefaultTableModel residentHistoryModel = new DefaultTableModel(
                new Object[]{"Resident ID", "Action", "Request Snapshot", "Updated At"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (ResidentRequestHistory entry : fileManager.readResidentRequestHistory()) {
            if (entry.getRequestId().equals(requestId)) {
                residentHistoryModel.addRow(new Object[]{entry.getResidentId(), entry.getAction(),
                        entry.getRequestSummary(), entry.getUpdatedAt().format(AssistanceRequest.DT_FMT)});
            }
        }

        JPanel content = new JPanel(new BorderLayout(8, 10));
        content.add(detailFields, BorderLayout.NORTH);
        JTabbedPane historyTabs = new JTabbedPane();
        historyTabs.addTab("Staff Status Changes", new JScrollPane(new JTable(historyModel)));
        historyTabs.addTab("Resident Updates", new JScrollPane(new JTable(residentHistoryModel)));
        historyTabs.setPreferredSize(new Dimension(700, 180));
        content.add(historyTabs, BorderLayout.CENTER);
        content.setPreferredSize(new Dimension(700, 440));
        JOptionPane.showMessageDialog(this, new JScrollPane(content),
                "Request Details - " + requestId, JOptionPane.PLAIN_MESSAGE);
    }

    private void addRequestDetailRow(JPanel panel, String label, String value) {
        panel.add(new JLabel(label + ":"));
        JTextArea valueArea = new JTextArea(value == null ? "" : value);
        valueArea.setEditable(false);
        valueArea.setLineWrap(true);
        valueArea.setWrapStyleWord(true);
        valueArea.setRows(Math.max(1, Math.min(4, (valueArea.getText().length() / 45) + 1)));
        valueArea.setBackground(panel.getBackground());
        valueArea.setBorder(BorderFactory.createEmptyBorder());
        panel.add(valueArea);
    }

    private void deleteSelectedRequest() {
        int row = requestTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String requestId = (String) requestTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Permanently delete request " + requestId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            requestManager.deleteRequest(requestId);
            refreshRequestTable(requestSearchField.getText().trim());
        }
    }

    private User getSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String name = (String) userTableModel.getValueAt(row, 1);
        for (User u : fileManager.readUsers()) {
            if (u.getFullName().equals(name)) return u;
        }
        return null;
    }

    private void createUserDialog() {
        String[] roles = {"Resident", "Admin", "MedicalStaff", "FoodStaff", "TransportationStaff"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        JTextField nameField = new JTextField();
        JTextField passField = new JTextField();
        JTextField extra1 = new JTextField(); // address (Resident)
        JTextField extra2 = new JTextField(); // contact (Resident)

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Role:")); panel.add(roleBox);
        panel.add(new JLabel("Name:")); panel.add(nameField);
        panel.add(new JLabel("Password:")); panel.add(passField);
        panel.add(new JLabel("Address (Resident only):")); panel.add(extra1);
        panel.add(new JLabel("Contact No (Resident only):")); panel.add(extra2);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create New User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String role = (String) roleBox.getSelectedItem();
        String name = nameField.getText().trim();
        String pass = passField.getText().trim();
        if (name.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and password are required.");
            return;
        }

        User newUser;
        switch (role) {
            case "Resident":
                String contact = extra2.getText().trim();
                if (!contact.matches("\\+?[0-9]+")) {
                    JOptionPane.showMessageDialog(this, "Contact number must be numeric.",
                            "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                newUser = new Resident(name, pass, fileManager.nextResidentId(), extra1.getText().trim(), contact);
                break;
            case "Admin":
                newUser = new Admin(name, pass, "ADM" + System.currentTimeMillis());
                break;
            case "MedicalStaff":
                newUser = new MedicalStaff(name, pass, "STF" + System.currentTimeMillis());
                break;
            case "FoodStaff":
                newUser = new FoodStaff(name, pass, "STF" + System.currentTimeMillis());
                break;
            default:
                newUser = new TransportationStaff(name, pass, "STF" + System.currentTimeMillis());
                break;
        }

        admin.createUser(newUser);
        refreshUserTable();
        JOptionPane.showMessageDialog(this, "User created.");
    }

    private void editSelectedUser() {
        User u = getSelectedUser();
        if (u == null) return;

        String oldUserId = getUserId(u);
        admin.manageUser(oldUserId);
        String[] roles = {"Resident", "Admin", "MedicalStaff", "FoodStaff", "TransportationStaff"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setSelectedItem(u.getRole());
        JTextField nameField = new JTextField(u.getFullName());
        JTextField passwordField = new JTextField(u.getPassword());
        JTextField userIdField = new JTextField(oldUserId);
        String address = "";
        String contact = "";
        if (u instanceof Resident) {
            Resident resident = (Resident) u;
            address = resident.getAddress();
            contact = resident.getContactNo();
        }
        JTextField addressField = new JTextField(address);
        JTextField contactField = new JTextField(contact);
        JComboBox<String> assignedTypeBox = new JComboBox<>(new String[]{"Medical", "Food", "Transportation"});
        if (u instanceof Staff) {
            assignedTypeBox.setSelectedItem(((Staff) u).getAssignedType());
        }
        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        Runnable rebuildFields = () -> {
            String selectedRole = (String) roleBox.getSelectedItem();
            panel.removeAll();
            panel.add(new JLabel("Role:"));
            panel.add(roleBox);
            panel.add(new JLabel("Name:"));
            panel.add(nameField);
            panel.add(new JLabel("Password:"));
            panel.add(passwordField);
            panel.add(new JLabel("UserID:"));
            panel.add(userIdField);

            if ("Resident".equals(selectedRole)) {
            panel.add(new JLabel("Address:"));
            panel.add(addressField);
            panel.add(new JLabel("Contact No:"));
            panel.add(contactField);
            } else if (selectedRole != null && selectedRole.endsWith("Staff")) {
            panel.add(new JLabel("Assigned Type:"));
            panel.add(assignedTypeBox);
            }
            panel.revalidate();
            panel.repaint();
        };
        roleBox.addActionListener(e -> {
            String selectedRole = (String) roleBox.getSelectedItem();
            userIdField.setText(createUserIdForRole(selectedRole));
            if ("MedicalStaff".equals(selectedRole)) assignedTypeBox.setSelectedItem("Medical");
            else if ("FoodStaff".equals(selectedRole)) assignedTypeBox.setSelectedItem("Food");
            else if ("TransportationStaff".equals(selectedRole)) assignedTypeBox.setSelectedItem("Transportation");
            rebuildFields.run();
        });
        rebuildFields.run();

        JScrollPane manageScroll = new JScrollPane(panel);
        manageScroll.setPreferredSize(new Dimension(700, 280));
        Object[] options = {"Save Changes", "Delete User", "Cancel"};
        int result = JOptionPane.showOptionDialog(this, manageScroll, "Manage User",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (result == 1) {
            deleteUser(u);
            return;
        }
        if (result != 0) return;

        String newName = nameField.getText().trim();
        String newPassword = passwordField.getText().trim();
        String newUserId = userIdField.getText().trim();
        String newRole = (String) roleBox.getSelectedItem();
        if (newName.isEmpty() || newPassword.isEmpty() || newUserId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, password, and UserID are required.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String requiredPrefix = getUserIdPrefix(newRole);
        if (!newUserId.toUpperCase().startsWith(requiredPrefix)) {
            JOptionPane.showMessageDialog(this, "The UserID for " + newRole + " must start with "
                    + requiredPrefix + ".", "Invalid UserID", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (User existingUser : fileManager.readUsers()) {
            if (!getUserId(existingUser).equalsIgnoreCase(oldUserId)
                    && getUserId(existingUser).equalsIgnoreCase(newUserId)) {
                JOptionPane.showMessageDialog(this, "That UserID is already in use.",
                        "Duplicate UserID", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        User updatedUser;
        if ("Resident".equals(newRole)) {
            String contactNo = contactField.getText().trim();
            if (!contactNo.matches("\\+?[0-9]+")) {
                JOptionPane.showMessageDialog(this, "Contact number must be numeric.",
                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            updatedUser = new Resident(newName, newPassword, newUserId, addressField.getText().trim(), contactNo);
        } else if ("Admin".equals(newRole)) {
            updatedUser = new Admin(newName, newPassword, newUserId);
        } else if ("MedicalStaff".equals(newRole)) {
            updatedUser = new MedicalStaff(newName, newPassword, newUserId);
        } else if ("FoodStaff".equals(newRole)) {
            updatedUser = new FoodStaff(newName, newPassword, newUserId);
        } else {
            updatedUser = new TransportationStaff(newName, newPassword, newUserId);
        }
        if (updatedUser instanceof Staff) {
            ((Staff) updatedUser).setAssignedType((String) assignedTypeBox.getSelectedItem());
        }

        if (u instanceof Resident && !oldUserId.equals(newUserId)) {
            fileManager.updateResidentIdInRequests(oldUserId, newUserId);
        }
        if (u instanceof Staff && updatedUser instanceof Staff && !oldUserId.equals(newUserId)) {
            fileManager.updateStaffIdInHistory(oldUserId, newUserId);
        }

        List<User> all = fileManager.readUsers();
        for (int i = 0; i < all.size(); i++) {
            if (getUserId(all.get(i)).equals(oldUserId)) {
                all.set(i, updatedUser);
                break;
            }
        }
        fileManager.saveAllUsers(all);
        refreshUserTable();
        JOptionPane.showMessageDialog(this, "User updated as " + newRole + ".");
    }

    private String getUserIdPrefix(String role) {
        if ("Resident".equals(role)) return "RSD";
        if ("Admin".equals(role)) return "ADM";
        return "STF";
    }

    private String createUserIdForRole(String role) {
        if ("Resident".equals(role)) return fileManager.nextResidentId();
        return getUserIdPrefix(role) + System.currentTimeMillis();
    }

    private String getUserId(User user) {
        return user.getUserId();
    }

    private void deleteUser(User u) {
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user " + u.getFullName() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String userId = getUserId(u);
        admin.deleteUser(userId);
        refreshUserTable();
    }

    private void viewSelectedUserHistory() {
        User u = getSelectedUser();
        if (u == null) return;
        if (u instanceof Staff) {
            Staff staff = (Staff) u;
            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Request ID", "Resident ID", "Type", "Status", "Processed At"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (StaffRequestHistory entry : fileManager.readStaffRequestHistory()) {
                if (entry.getStaffId().equals(staff.getStaffId())) {
                    model.addRow(new Object[]{entry.getRequestId(), entry.getResidentId(), entry.getRequestType(),
                            entry.getStatus(), entry.getProcessedAt().format(AssistanceRequest.DT_FMT)});
                }
            }
            JTable historyTable = new JTable(model);
            JScrollPane scroll = new JScrollPane(historyTable);
            scroll.setPreferredSize(new Dimension(700, 280));
            JOptionPane.showMessageDialog(this, scroll, "Requests handled by " + staff.getFullName(),
                    JOptionPane.PLAIN_MESSAGE);
            return;
        }
        if (!(u instanceof Resident)) {
            JOptionPane.showMessageDialog(this, "History is available for Resident and Staff accounts.",
                    "Not Applicable", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Resident resident = (Resident) u;

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Request ID", "Type", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (AssistanceRequest req : requestManager.getAllRequests()) {
            if (req.getResidentId().equals(resident.getResidentId())) {
                model.addRow(new Object[]{req.getRequestId(), req.getType(), req.getStatus()});
            }
        }
        JTable historyTable = new JTable(model);
        DefaultTableModel updatesModel = new DefaultTableModel(
                new Object[]{"Request ID", "Action", "Request Snapshot", "Updated At"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (ResidentRequestHistory entry : fileManager.readResidentRequestHistory()) {
            if (entry.getResidentId().equals(resident.getResidentId())) {
                updatesModel.addRow(new Object[]{entry.getRequestId(), entry.getAction(),
                        entry.getRequestSummary(), entry.getUpdatedAt().format(AssistanceRequest.DT_FMT)});
            }
        }

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Requests", new JScrollPane(historyTable));
        tabs.addTab("Request Updates", new JScrollPane(new JTable(updatesModel)));
        tabs.setPreferredSize(new Dimension(700, 320));
        JOptionPane.showMessageDialog(this, tabs, "History for " + resident.getFullName(),
                JOptionPane.PLAIN_MESSAGE);
    }
}
