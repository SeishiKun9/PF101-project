package ui;

import data.FileManager;
import data.RequestManager;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * GUI shared by MedicalStaff, FoodStaff, and TransportationStaff: view, search,
 * and update the status of requests matching their assigned type.
 */
public class StaffMenuFrame extends JFrame {
    private static final Dimension REQUEST_DIALOG_SIZE = new Dimension(560, 390);
    private final Staff staff;
    private final FileManager fileManager = new FileManager();
    private final RequestManager requestManager = new RequestManager(fileManager);

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Request ID", "Resident ID", "Details", "Status"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(16);

    // KPI Stat Labels
    private final JLabel totalAssignedLabel = new JLabel("0");
    private final JLabel pendingCountLabel = new JLabel("0");
    private final JLabel inProgressCountLabel = new JLabel("0");
    private final JLabel completedCountLabel = new JLabel("0");

    public StaffMenuFrame(Staff staff) {
        super("Staff Menu - " + staff.getFullName() + " (" + staff.getAssignedType() + ")");
        this.staff = staff;
        buildUI();
        refreshTable(null);
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 660);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(JButtonStyle.BG_PAGE);

        // NATIVE MENU BAR (Staff Menu)
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, JButtonStyle.BORDER_LIGHT));

        JMenu staffMenu = new JMenu("Staff Menu");
        staffMenu.setFont(staffMenu.getFont().deriveFont(Font.BOLD, 12f));

        JMenuItem updateMenuItem = new JMenuItem("Update Request Status");
        JMenuItem viewMenuItem = new JMenuItem("View Request Details");
        JMenuItem refreshMenuItem = new JMenuItem("Refresh Requests");
        JMenuItem logoutMenuItem = new JMenuItem("Logout");

        staffMenu.add(updateMenuItem);
        staffMenu.add(viewMenuItem);
        staffMenu.addSeparator();
        staffMenu.add(refreshMenuItem);
        staffMenu.add(logoutMenuItem);
        menuBar.add(staffMenu);
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
                g2.setColor(new Color(236, 253, 245));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(new Color(167, 243, 208));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(new Color(5, 150, 105));
                g2.setFont(new Font("SansSerif", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                String in = getInitials(staff.getFullName());
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
        JLabel nameLbl = new JLabel(staff.getFullName());
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 17f));
        nameLbl.setForeground(JButtonStyle.TEXT_DARK);

        JLabel rolePill = new JLabel(" " + staff.getAssignedType().toUpperCase() + " STAFF ");
        rolePill.setFont(rolePill.getFont().deriveFont(Font.BOLD, 10.5f));
        rolePill.setOpaque(true);
        rolePill.setBackground(new Color(220, 252, 231));
        rolePill.setForeground(new Color(21, 128, 61));
        rolePill.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        titleRoleRow.add(nameLbl);
        titleRoleRow.add(rolePill);

        JLabel metaLbl = new JLabel("Staff ID: " + staff.getStaffId() + "  \u2022  Department: " + staff.getAssignedType() + " Assistance");
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
        statsRow.add(JButtonStyle.createStatCard("Assigned Requests", totalAssignedLabel, new Color(59, 130, 246), staff.getAssignedType() + " Department"));
        statsRow.add(JButtonStyle.createStatCard("Pending Review", pendingCountLabel, new Color(245, 158, 11), "Needs verification"));
        statsRow.add(JButtonStyle.createStatCard("In Progress", inProgressCountLabel, new Color(99, 102, 241), "Active assistance"));
        statsRow.add(JButtonStyle.createStatCard("Completed", completedCountLabel, new Color(16, 185, 129), "Fulfilled requests"));
        mainContent.add(statsRow, BorderLayout.NORTH);

        // TABLE AND TOOLBAR
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JPanel actionsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionsLeft.setOpaque(false);
        JButton updateStatusBtn = new JButton("Update Status");
        JButton refreshBtn = new JButton("View Details");
        JButton reloadBtn = new JButton("Refresh");
        JButtonStyle.applySuccess(updateStatusBtn);
        JButtonStyle.applyAccent(refreshBtn);
        JButtonStyle.applySecondary(reloadBtn);
        actionsLeft.add(updateStatusBtn);
        actionsLeft.add(refreshBtn);
        actionsLeft.add(reloadBtn);

        JPanel searchRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchRight.setOpaque(false);
        JLabel sLbl = new JLabel("Search:");
        sLbl.setFont(sLbl.getFont().deriveFont(Font.BOLD, 12f));
        sLbl.setForeground(JButtonStyle.TEXT_MUTED);
        searchField.setPreferredSize(new Dimension(200, 32));
        searchField.setBorder(JButtonStyle.createFieldBorder());
        JButton searchBtn = new JButton("Search");
        JButton clearBtn = new JButton("Clear");
        JButtonStyle.applyAccent(searchBtn);
        JButtonStyle.applySecondary(clearBtn);
        searchRight.add(sLbl);
        searchRight.add(searchField);
        searchRight.add(searchBtn);
        searchRight.add(clearBtn);

        toolbar.add(actionsLeft, BorderLayout.WEST);
        toolbar.add(searchRight, BorderLayout.EAST);

        JButtonStyle.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(480);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setCellRenderer(new JButtonStyle.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(JButtonStyle.BORDER_LIGHT, 1, true));

        centerPanel.add(toolbar, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        mainContent.add(centerPanel, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        // EVENT LISTENERS
        refreshBtn.addActionListener(e -> { staff.viewRequests(); showSelectedRequestDetails(); });
        viewMenuItem.addActionListener(e -> { staff.viewRequests(); showSelectedRequestDetails(); });

        reloadBtn.addActionListener(e -> { staff.viewRequests(); refreshTable(null); });
        refreshMenuItem.addActionListener(e -> { staff.viewRequests(); refreshTable(null); });

        searchBtn.addActionListener(e -> { staff.searchRequest(); refreshTable(searchField.getText().trim()); });
        searchField.addActionListener(e -> { staff.searchRequest(); refreshTable(searchField.getText().trim()); });
        clearBtn.addActionListener(e -> { searchField.setText(""); refreshTable(null); });

        updateStatusBtn.addActionListener(e -> { staff.updateRequestStatus(); updateStatusDialog(); });
        updateMenuItem.addActionListener(e -> { staff.updateRequestStatus(); updateStatusDialog(); });

        logoutBtn.addActionListener(e -> {
            staff.logout();
            dispose();
            new LoginFrame(fileManager).setVisible(true);
        });
        logoutMenuItem.addActionListener(e -> {
            staff.logout();
            dispose();
            new LoginFrame(fileManager).setVisible(true);
        });
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "S";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private void refreshTable(String searchTerm) {
        tableModel.setRowCount(0);
        String query = searchTerm == null ? "" : searchTerm.toLowerCase();
        List<AssistanceRequest> all = requestManager.getAllRequests();

        int total = 0;
        int pending = 0;
        int inProgress = 0;
        int completed = 0;

        for (AssistanceRequest r : all) {
            if (!r.getType().equalsIgnoreCase(staff.getAssignedType())) continue;
            total++;
            String status = r.getStatus();
            if ("Pending".equalsIgnoreCase(status)) pending++;
            else if ("Completed".equalsIgnoreCase(status) || "Delivered".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status)) completed++;
            else inProgress++;

            if (!query.isEmpty()
                    && !r.getRequestId().toLowerCase().contains(query)
                    && !r.getResidentId().toLowerCase().contains(query)
                    && !r.getStatus().toLowerCase().contains(query)) {
                continue;
            }
            tableModel.addRow(new Object[]{r.getRequestId(), r.getResidentId(),
                    r.toDisplaySummary(), r.getStatus()});
        }

        totalAssignedLabel.setText(String.valueOf(total));
        pendingCountLabel.setText(String.valueOf(pending));
        inProgressCountLabel.setText(String.valueOf(inProgress));
        completedCountLabel.setText(String.valueOf(completed));
    }

    private AssistanceRequest getSelectedRequest() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        return requestManager.findRequest(id);
    }

    private void showSelectedRequestDetails() {
        AssistanceRequest req = getSelectedRequest();
        if (req == null) return;

        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 8, 6));
        addDetailRow(detailsPanel, "Request ID", req.getRequestId());
        addDetailRow(detailsPanel, "Resident ID", req.getResidentId());
        addDetailRow(detailsPanel, "Type", req.getType());
        addDetailRow(detailsPanel, "Time needed", req.getTimeNeeded().format(AssistanceRequest.DT_FMT));
        addDetailRow(detailsPanel, "Additional details", req.getAdditionalDetails());
        addDetailRow(detailsPanel, "Status", req.getStatus());
        addDetailRow(detailsPanel, "Submitted", req.getDateRequested().format(AssistanceRequest.DT_FMT));

        if (req instanceof MedicalRequest) {
            MedicalRequest medical = (MedicalRequest) req;
            addDetailRow(detailsPanel, "Location", req.getLocation());
            int itemNumber = 1;
            for (MedicalItem item : medical.getMedicalItems()) {
                addDetailRow(detailsPanel, "Medical item " + itemNumber++, item.toString());
            }
        } else if (req instanceof FoodRequest) {
            FoodRequest food = (FoodRequest) req;
            addDetailRow(detailsPanel, "Location", req.getLocation());
            int itemNumber = 1;
            for (FoodItem item : food.getFoodItems()) {
                addDetailRow(detailsPanel, "Food item " + itemNumber++, item.toString());
            }
        } else if (req instanceof TransportationRequest) {
            TransportationRequest transportation = (TransportationRequest) req;
            addDetailRow(detailsPanel, "Pickup location", transportation.getPickupLocation());
            addDetailRow(detailsPanel, "Destination", transportation.getDestination());
            addDetailRow(detailsPanel, "Passengers", String.valueOf(transportation.getPassengerCount()));
        }

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setPreferredSize(new Dimension(520, 320));
        Object[] options = {"Change Status", "Close"};
        int choice = showRequestDialog(scrollPane, "Request Details", options);
        if (choice == 0) updateStatusDialog(req);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
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

    private void updateStatusDialog() {
        AssistanceRequest req = getSelectedRequest();
        if (req == null) return;
        updateStatusDialog(req);
    }

    private void updateStatusDialog(AssistanceRequest req) {
        String[] statuses = {"Pending", "In Progress", "Help is OTW", "Completed", "Cancelled"};
        JComboBox<String> statusBox = new JComboBox<>(statuses);

        String currentStatus = req.getStatus() == null ? "Pending" : req.getStatus().trim();
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(currentStatus)) {
                statusBox.setSelectedIndex(i);
                break;
            }
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Request ID:"));
        panel.add(new JLabel(req.getRequestId()));
        panel.add(new JLabel("Resident ID:"));
        panel.add(new JLabel(req.getResidentId()));
        panel.add(new JLabel("Current status:"));
        panel.add(new JLabel(currentStatus));
        panel.add(new JLabel("New status:"));
        panel.add(statusBox);

        Object[] options = {"Save Status", "Close"};
        int choice = showRequestDialog(panel, "Update Request", options);
        if (choice != 0) return;

        String newStatus = (String) statusBox.getSelectedItem();
        req.updateStatus(newStatus);
        fileManager.recordStaffRequestAction(staff, req, newStatus);
        requestManager.updateRequest(req);
        refreshTable(searchField.getText().trim());
        JOptionPane.showMessageDialog(this, "Status updated to " + newStatus + ".");
    }

    private int showRequestDialog(Component content, String title, Object[] options) {
        JOptionPane optionPane = new JOptionPane(content, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, options, options[0]);
        JDialog dialog = optionPane.createDialog(this, title);
        dialog.setResizable(true);
        dialog.setSize(REQUEST_DIALOG_SIZE);
        dialog.setMinimumSize(REQUEST_DIALOG_SIZE);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        Object selectedOption = optionPane.getValue();
        dialog.dispose();
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(selectedOption)) return i;
        }
        return -1;
    }

}
