package ui;

import storage.FileManager;
import storage.RequestManager;
import model.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StaffMenuFrame extends JFrame {
    private final Staff staff;
    private final FileManager fileManager = new FileManager();
    private final RequestManager requestManager = new RequestManager(fileManager);

    private static final Color DARK_BLUE = new Color(15, 40, 80);
    private static final Color DARK_BLUE_BTN = new Color(25, 70, 140);
    private static final Color GRAY_DISABLED = new Color(148, 163, 184);

    private final DefaultListModel<String> requestListModel = new DefaultListModel<>();
    private final JList<String> requestList = new JList<>(requestListModel);
    private final JPanel detailsPanel = new JPanel();
    private final JLabel detailsPlaceholder = new JLabel("Select a request to view details", SwingConstants.CENTER);
    private final JPanel historyPanel = new JPanel(new BorderLayout());
    private final JTextField searchField = new JTextField(14);
    private List<AssistanceRequest> currentRequests = new ArrayList<>();
    private final JButton updateStatusBtn = new JButton("Update Status");
    private final JButton claimBtn = new JButton("Claim");
    private final JComboBox<String> assignmentFilterBox = new JComboBox<>(new String[]{"All", "Unassigned", "Mine"});

    public StaffMenuFrame(Staff staff) {
        super("Staff Menu - " + staff.getFullName() + " (" + staff.getAssignedType() + ")");
        this.staff = staff;
        buildUI();
        refreshList(null);
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 660);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 230)));
        JMenu staffMenu = new JMenu("Staff Menu");
        staffMenu.setFont(staffMenu.getFont().deriveFont(Font.BOLD, 12f));
        JMenuItem updateMenuItem = new JMenuItem("Update Request Status");
        JMenuItem refreshMenuItem = new JMenuItem("Refresh Requests");
        JMenuItem logoutMenuItem = new JMenuItem("Logout");
        staffMenu.add(updateMenuItem);
        staffMenu.addSeparator();
        staffMenu.add(refreshMenuItem);
        staffMenu.add(logoutMenuItem);
        menuBar.add(staffMenu);
        setJMenuBar(menuBar);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DARK_BLUE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 28, 16, 28));

        JPanel nameAndMeta = new JPanel();
        nameAndMeta.setLayout(new BoxLayout(nameAndMeta, BoxLayout.Y_AXIS));
        nameAndMeta.setOpaque(false);
        JLabel nameLbl = new JLabel(staff.getFullName());
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 18f));
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel metaLbl = new JLabel("Staff ID: " + staff.getStaffId()
                + "  \u2022  Department: " + staff.getAssignedType() + " Assistance");
        metaLbl.setFont(metaLbl.getFont().deriveFont(Font.PLAIN, 12f));
        metaLbl.setForeground(new Color(180, 200, 230));
        metaLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameAndMeta.add(nameLbl);
        nameAndMeta.add(Box.createVerticalStrut(4));
        nameAndMeta.add(metaLbl);
        headerPanel.add(nameAndMeta, BorderLayout.WEST);

        JButton logoutBtn = createDarkBlueButton("Logout");
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        rightHeader.setOpaque(false);
        rightHeader.add(logoutBtn);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(new Color(245, 247, 250));
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 20, 8, 20));
        JLabel deptLbl = new JLabel(staff.getAssignedType() + " Requests");
        deptLbl.setFont(deptLbl.getFont().deriveFont(Font.BOLD, 13f));
        deptLbl.setForeground(DARK_BLUE);
        JPanel searchRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchRight.setOpaque(false);
        JLabel sLbl = new JLabel("Search:");
        sLbl.setFont(sLbl.getFont().deriveFont(Font.BOLD, 12f));
        sLbl.setForeground(DARK_BLUE);
        searchField.setPreferredSize(new Dimension(150, 30));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        JLabel viewLbl = new JLabel("Show:");
        viewLbl.setFont(viewLbl.getFont().deriveFont(Font.BOLD, 12f));
        viewLbl.setForeground(DARK_BLUE);
        assignmentFilterBox.setPreferredSize(new Dimension(110, 30));
        JButton searchBtn = createDarkBlueButton("Search");
        JButton clearBtn = createDarkBlueButton("Clear");
        searchRight.add(sLbl);
        searchRight.add(searchField);
        searchRight.add(viewLbl);
        searchRight.add(assignmentFilterBox);
        searchRight.add(searchBtn);
        searchRight.add(clearBtn);
        toolbar.add(deptLbl, BorderLayout.WEST);
        toolbar.add(searchRight, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(280);
        splitPane.setDividerSize(4);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(203, 213, 225)));
        JLabel listTitle = new JLabel("  Assigned Requests");
        listTitle.setFont(listTitle.getFont().deriveFont(Font.BOLD, 13f));
        listTitle.setForeground(DARK_BLUE);
        listTitle.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        listTitle.setBackground(new Color(248, 250, 252));
        listTitle.setOpaque(true);
        requestList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        requestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestList.setFixedCellHeight(36);
        requestList.setBorder(new EmptyBorder(4, 8, 4, 8));
        requestList.setBackground(Color.WHITE);
        requestList.setSelectionBackground(new Color(219, 234, 254));
        requestList.setSelectionForeground(DARK_BLUE);
        JScrollPane listScroll = new JScrollPane(requestList);
        listScroll.setBorder(null);
        leftPanel.add(listTitle, BorderLayout.NORTH);
        leftPanel.add(listScroll, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        JLabel detailsTitle = new JLabel("  Request Details");
        detailsTitle.setFont(detailsTitle.getFont().deriveFont(Font.BOLD, 13f));
        detailsTitle.setForeground(DARK_BLUE);
        detailsTitle.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        detailsTitle.setBackground(new Color(248, 250, 252));
        detailsTitle.setOpaque(true);
        detailsPanel.setLayout(new BorderLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPlaceholder.setFont(detailsPlaceholder.getFont().deriveFont(Font.PLAIN, 14f));
        detailsPlaceholder.setForeground(new Color(148, 163, 184));
        detailsPanel.add(detailsPlaceholder, BorderLayout.CENTER);
        JScrollPane detailsScroll = new JScrollPane(detailsPanel);
        detailsScroll.setBorder(null);

        styleActionButton(updateStatusBtn, false);
        styleActionButton(claimBtn, false);
        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomActions.setBackground(Color.WHITE);
        bottomActions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)));
        bottomActions.add(claimBtn);
        bottomActions.add(updateStatusBtn);

        JPanel detailsWrap = new JPanel(new BorderLayout());
        detailsWrap.setBackground(Color.WHITE);
        detailsWrap.add(detailsTitle, BorderLayout.NORTH);
        detailsWrap.add(detailsScroll, BorderLayout.CENTER);
        detailsWrap.add(bottomActions, BorderLayout.SOUTH);

        historyPanel.setBackground(Color.WHITE);
        historyPanel.add(makeEmptyState("Select a request to view its history."), BorderLayout.CENTER);
        JPanel historyWrap = new JPanel(new BorderLayout());
        historyWrap.setBackground(Color.WHITE);
        JLabel historyTitle = new JLabel("  Request History");
        historyTitle.setFont(historyTitle.getFont().deriveFont(Font.BOLD, 13f));
        historyTitle.setForeground(DARK_BLUE);
        historyTitle.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        historyTitle.setBackground(new Color(248, 250, 252));
        historyTitle.setOpaque(true);
        historyWrap.add(historyTitle, BorderLayout.NORTH);
        historyWrap.add(historyPanel, BorderLayout.CENTER);

        JSplitPane detailsHistorySplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        detailsHistorySplit.setDividerLocation(430);
        detailsHistorySplit.setDividerSize(4);
        detailsHistorySplit.setBorder(null);
        detailsHistorySplit.setContinuousLayout(true);
        detailsHistorySplit.setLeftComponent(detailsWrap);
        detailsHistorySplit.setRightComponent(historyWrap);
        detailsHistorySplit.setResizeWeight(0.55);

        rightPanel.add(detailsHistorySplit, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(245, 247, 250));
        mainContent.add(toolbar, BorderLayout.NORTH);
        mainContent.add(splitPane, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        requestList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedDetails();
                showSelectedHistory();
                updateActionButtonsState();
            }
        });

        updateStatusBtn.addActionListener(e -> updateStatusDialog());
        updateMenuItem.addActionListener(e -> updateStatusDialog());
        claimBtn.addActionListener(e -> claimOrReleaseSelected());
        assignmentFilterBox.addActionListener(e -> refreshList(searchField.getText().trim()));
        refreshMenuItem.addActionListener(e -> { staff.viewRequests(); refreshList(searchField.getText().trim()); });
        searchBtn.addActionListener(e -> { staff.searchRequest(); refreshList(searchField.getText().trim()); });
        searchField.addActionListener(e -> { staff.searchRequest(); refreshList(searchField.getText().trim()); });
        clearBtn.addActionListener(e -> { searchField.setText(""); refreshList(null); });
        logoutBtn.addActionListener(e -> { staff.logout(); dispose(); new LoginFrame(fileManager).setVisible(true); });
        logoutMenuItem.addActionListener(e -> { staff.logout(); dispose(); new LoginFrame(fileManager).setVisible(true); });
    }

    private JButton createDarkBlueButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 12f));
        btn.setBackground(DARK_BLUE_BTN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return btn;
    }

    private void styleActionButton(JButton btn, boolean enabled) {
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 12f));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        btn.setEnabled(enabled);
        if (enabled) {
            btn.setBackground(DARK_BLUE_BTN);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(GRAY_DISABLED);
            btn.setForeground(Color.WHITE);
        }
    }

    private void updateActionButtonsState() {
        int idx = requestList.getSelectedIndex();
        boolean canUpdate = false;
        boolean hasSel = false;
        boolean canClaim = false;
        String claimLabel = "Claim";
        if (idx >= 0 && idx < currentRequests.size()) {
            AssistanceRequest sel = currentRequests.get(idx);
            hasSel = staff.canHandle(sel);
            String st = sel.getStatus();
            boolean cancelled = st != null && "Cancelled".equalsIgnoreCase(st);
            boolean pending = st != null && "Pending".equalsIgnoreCase(st);
            boolean mine = hasSel && staff.getStaffId().equals(sel.getAssignedStaffId());
            String assignee = sel.getAssignedStaffId();
            boolean unassigned = assignee == null || assignee.trim().isEmpty();
            boolean takenByOther = hasSel && !unassigned && !mine;
            boolean terminal = cancelled || (st != null && "Completed".equalsIgnoreCase(st));
            canUpdate = mine && !terminal;
            if (hasSel) {
                if (mine) {
                    canClaim = false;
                    claimLabel = "Claimed";
                } else if (takenByOther) {
                    canClaim = false;
                    claimLabel = "Taken by " + assignee.trim();
                } else if (unassigned && pending) {
                    canClaim = true;
                    claimLabel = "Claim";
                } else {
                    canClaim = false;
                    claimLabel = "Claim";
                }
            }
        }
        claimBtn.setText(claimLabel);
        styleActionButton(claimBtn, canClaim);
        styleActionButton(updateStatusBtn, canUpdate);
    }

    private void showSelectedHistory() {
        historyPanel.removeAll();
        AssistanceRequest req = null;
        int idx = requestList.getSelectedIndex();
        if (idx >= 0 && idx < currentRequests.size()) {
            req = currentRequests.get(idx);
        }
        if (req == null || !staff.canHandle(req)) {
            historyPanel.add(makeEmptyState("Select a request to view its history."),
                    BorderLayout.CENTER);
        } else {
            historyPanel.add(buildStaffRequestTimeline(req), BorderLayout.CENTER);
        }
        historyPanel.revalidate();
        historyPanel.repaint();
    }

    private void showSelectedDetails() {
        int idx = requestList.getSelectedIndex();
        detailsPanel.removeAll();
        if (idx < 0 || idx >= currentRequests.size()) {
            detailsPanel.setLayout(new BorderLayout());
            detailsPanel.add(detailsPlaceholder, BorderLayout.CENTER);
            detailsPanel.revalidate();
            detailsPanel.repaint();
            return;
        }
        AssistanceRequest req = currentRequests.get(idx);
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel idLbl = new JLabel(req.getRequestId());
        idLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        idLbl.setForeground(DARK_BLUE);
        JLabel statusBadge = new JLabel("  " + req.getStatus() + "  ");
        statusBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusBadge.setOpaque(true);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusBadge.setBackground(new Color(219, 234, 254));
        statusBadge.setForeground(DARK_BLUE);
        headerRow.add(idLbl, BorderLayout.WEST);
        headerRow.add(statusBadge, BorderLayout.EAST);
        card.add(headerRow);
        card.add(Box.createVerticalStrut(18));

        String submitterContact = null;
        for (User u : fileManager.readUsers()) {
            if (u instanceof Resident && req.getResidentId() != null
                    && req.getResidentId().equals(((Resident) u).getResidentId())) {
                submitterContact = ((Resident) u).getContactNo();
                break;
            }
        }

        JPanel grid = new JPanel(new GridLayout(0, 2, 32, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Row 1: Resident ID | Contact No
        grid.add(makeFieldBlock("Resident ID", req.getResidentId()));
        grid.add(makeFieldBlock("Contact No", submitterContact == null ? "-" : submitterContact));

        // Row 2: Date Needed | Date Submitted
        grid.add(makeFieldBlock("Date Needed", formatDisplayDate(req.getTimeNeeded())));
        grid.add(makeFieldBlock("Date Submitted", formatDisplayDate(req.getDateRequested())));

        // Row 3: Route & No. of Passengers (for Transpo) OR Location (for Food & Medical)
        if (req instanceof TransportationRequest) {
            TransportationRequest tr = (TransportationRequest) req;
            String routeVal = tr.getPickupLocation() + " \u2192 " + tr.getDestination();
            grid.add(makeFieldBlock("Route", routeVal.isEmpty() ? "-" : routeVal));
            grid.add(makeFieldBlock("No. of Passengers", String.valueOf(tr.getPassengerCount())));
        } else {
            String locationVal = req.getLocation();
            grid.add(makeFieldBlock("Location", locationVal == null || locationVal.isEmpty() ? "-" : locationVal));
        }

        card.add(grid);
        card.add(Box.createVerticalStrut(16));

        if (req instanceof MedicalRequest) {
            card.add(makeDivider());
            card.add(Box.createVerticalStrut(12));
            card.add(makeSectionTitle("Requested Items"));
            card.add(Box.createVerticalStrut(8));
            for (MedicalItem item : ((MedicalRequest) req).getMedicalItems()) {
                card.add(makeItemRow(item.getName(), item.getQuantity() + " " + item.getUnit()));
                card.add(Box.createVerticalStrut(6));
            }
        } else if (req instanceof FoodRequest) {
            card.add(makeDivider());
            card.add(Box.createVerticalStrut(12));
            card.add(makeSectionTitle("Requested Items"));
            card.add(Box.createVerticalStrut(8));
            for (FoodItem item : ((FoodRequest) req).getFoodItems()) {
                card.add(makeItemRow(item.getName(), item.getQuantity() + " " + item.getUnit()));
                card.add(Box.createVerticalStrut(6));
            }
        }

        card.add(Box.createVerticalStrut(10));
        card.add(makeDivider());
        card.add(Box.createVerticalStrut(12));
        card.add(makeSectionTitle("Reason for Request"));
        card.add(Box.createVerticalStrut(6));
        String reason = req.getAdditionalDetails();
        JLabel reasonLbl = new JLabel(reason == null || reason.isEmpty() ? "(None)" : reason);
        reasonLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        reasonLbl.setForeground(new Color(30, 41, 59));
        reasonLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(reasonLbl);

        detailsPanel.setLayout(new BorderLayout());
        detailsPanel.add(card, BorderLayout.NORTH);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private JPanel makeFieldBlock(String label, String value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(new Color(100, 116, 139));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        String shown = (value == null || value.trim().isEmpty()) ? "-" : value;
        JLabel v = new JLabel("<html><body style='width:180px'>" + escapeHtml(shown) + "</body></html>");
        v.setFont(new Font("SansSerif", Font.BOLD, 13));
        v.setForeground(new Color(15, 23, 42));
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(v);
        return p;
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private JLabel makeSectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(new Color(100, 116, 139));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel makeItemRow(String name, String qty) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel n = new JLabel(name);
        n.setFont(new Font("SansSerif", Font.BOLD, 13));
        n.setForeground(new Color(15, 23, 42));
        JLabel q = new JLabel(qty);
        q.setFont(new Font("SansSerif", Font.PLAIN, 13));
        q.setForeground(new Color(71, 85, 105));
        row.add(n, BorderLayout.WEST);
        row.add(q, BorderLayout.EAST);
        return row;
    }

    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setForeground(new Color(226, 232, 240));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private String formatDisplayDate(java.time.LocalDateTime dt) {
        if (dt == null) return "-";
        return dt.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"));
    }

    private void refreshList(String searchTerm) {
        String selectedRequestId = null;
        int selectedIndex = requestList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < currentRequests.size()) {
            selectedRequestId = currentRequests.get(selectedIndex).getRequestId();
        }
        requestListModel.clear();
        currentRequests.clear();
        String query = searchTerm == null ? "" : searchTerm.toLowerCase();
        String view = (String) assignmentFilterBox.getSelectedItem();
        if (view == null) view = "All";
        for (AssistanceRequest r : requestManager.getAllRequests()) {
            if (!r.getType().equalsIgnoreCase(staff.getAssignedType())) continue;
            if ("Unassigned".equals(view) && r.isAssigned()) continue;
            if ("Mine".equals(view) && !staff.getStaffId().equals(r.getAssignedStaffId())) continue;
            if (!query.isEmpty()
                    && !r.getRequestId().toLowerCase().contains(query)
                    && !r.getResidentId().toLowerCase().contains(query)
                    && !r.getStatus().toLowerCase().contains(query)) continue;
            currentRequests.add(r);
            String tag = "";
            if (r.isAssigned()) {
                tag = staff.getStaffId().equals(r.getAssignedStaffId())
                        ? "  [Mine]" : "  [" + r.getAssignedStaffId() + " handling]";
            }
            requestListModel.addElement(r.getRequestId() + "  (" + r.getStatus() + ")" + tag);
        }
        detailsPanel.removeAll();
        detailsPanel.setLayout(new BorderLayout());
        detailsPanel.add(detailsPlaceholder, BorderLayout.CENTER);
        detailsPanel.revalidate();
        detailsPanel.repaint();
        if (selectedRequestId != null) {
            for (int i = 0; i < currentRequests.size(); i++) {
                if (selectedRequestId.equals(currentRequests.get(i).getRequestId())) {
                    requestList.setSelectedIndex(i);
                    break;
                }
            }
        }
        updateActionButtonsState();
    }

    private AssistanceRequest getSelectedRequest() {
        int idx = requestList.getSelectedIndex();
        if (idx < 0 || idx >= currentRequests.size()) {
            JOptionPane.showMessageDialog(this, "Please select a request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentRequests.get(idx);
    }

    private void updateStatusDialog() {
        AssistanceRequest req = getSelectedRequest();
        if (req == null) return;
        if (!staff.getStaffId().equals(req.getAssignedStaffId())) {
            String owner = req.getAssignedStaffId();
            JOptionPane.showMessageDialog(this,
                    "Claim this request first before updating its status.\nCurrently handled by: "
                            + (owner == null || owner.trim().isEmpty() ? "no one" : owner) + ".",
                    "Claim Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("Cancelled".equalsIgnoreCase(req.getStatus())
                || "Completed".equalsIgnoreCase(req.getStatus())) {
            JOptionPane.showMessageDialog(this, req.getStatus() + " requests cannot be updated.",
                    "Update Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] statuses = {"Pending", "In Progress", "Help is OTW", "Completed", "Cancelled"};
        JComboBox<String> statusBox = new JComboBox<>(statuses);
        String currentStatus = req.getStatus() == null ? "Pending" : req.getStatus().trim();
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(currentStatus)) {
                statusBox.setSelectedIndex(i);
                break;
            }
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 12, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        panel.setBackground(Color.WHITE);
        JLabel idL = new JLabel("Request ID");
        idL.setForeground(DARK_BLUE);
        idL.setFont(idL.getFont().deriveFont(Font.BOLD, 12f));
        panel.add(idL);
        panel.add(new JLabel(req.getRequestId()));
        JLabel resL = new JLabel("Resident ID");
        resL.setForeground(DARK_BLUE);
        resL.setFont(resL.getFont().deriveFont(Font.BOLD, 12f));
        panel.add(resL);
        panel.add(new JLabel(req.getResidentId()));
        JLabel curL = new JLabel("Current Status");
        curL.setForeground(DARK_BLUE);
        curL.setFont(curL.getFont().deriveFont(Font.BOLD, 12f));
        panel.add(curL);
        panel.add(new JLabel(currentStatus));
        JLabel newL = new JLabel("New Status");
        newL.setForeground(DARK_BLUE);
        newL.setFont(newL.getFont().deriveFont(Font.BOLD, 12f));
        panel.add(newL);
        panel.add(statusBox);

        JDialog dialog = new JDialog(this, "Update Request Status", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)));
        JButton saveBtn = createDarkBlueButton("Save Status");
        JButton closeBtn = createDarkBlueButton("Close");
        buttons.add(saveBtn);
        buttons.add(closeBtn);
        dialog.add(buttons, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            String newStatus = (String) statusBox.getSelectedItem();
            try {
                staff.updateRequestStatus(req, newStatus);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(),
                        "Update Not Allowed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            refreshList(searchField.getText().trim());
            showSelectedHistory();
            JOptionPane.showMessageDialog(dialog, "Status updated to " + newStatus + ".");
            dialog.dispose();
        });
        closeBtn.addActionListener(e -> dialog.dispose());
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, 280));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JLabel makeEmptyState(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(new Color(148, 163, 184));
        return lbl;
    }

    private JComponent buildStaffRequestTimeline(AssistanceRequest req) {
        String requestId = req.getRequestId();
        List<TimelineEvent> events = new ArrayList<>();

        for (ResidentRequestHistory entry : fileManager.readResidentRequestHistory()) {
            if (requestId.equals(entry.getRequestId())) {
                String[] shown = splitAdminAction(entry.getAction());
                events.add(new TimelineEvent(entry.getUpdatedAt(), shown[0], shown[1]));
            }
        }
        for (StaffRequestHistory entry : fileManager.readStaffRequestHistory()) {
            if (requestId.equals(entry.getRequestId())
                    && staff.getAssignedType().equalsIgnoreCase(entry.getRequestType())) {
                events.add(new TimelineEvent(entry.getProcessedAt(),
                        "Status changed to " + entry.getStatus(), "Staff ID: " + entry.getStaffId()));
            }
        }

        java.time.LocalDateTime submitted = req.getDateRequested();
        if (submitted != null) {
            events.removeIf(ev -> ev.at != null && ev.at.isBefore(submitted));
        }
        events.sort((a, b) -> a.at.compareTo(b.at));

        JPanel timeline = new JPanel();
        timeline.setLayout(new BoxLayout(timeline, BoxLayout.Y_AXIS));
        timeline.setBackground(Color.WHITE);
        timeline.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        if (events.isEmpty()) {
            JLabel none = new JLabel("No updates recorded for this request.");
            none.setFont(new Font("SansSerif", Font.PLAIN, 13));
            none.setForeground(new Color(148, 163, 184));
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            timeline.add(none);
        } else {
            for (int i = 0; i < events.size(); i++) {
                TimelineEvent ev = events.get(i);
                timeline.add(buildTimelineRow(ev.action, ev.at, ev.detail));
                if (i < events.size() - 1) {
                    timeline.add(Box.createVerticalStrut(10));
                }
            }
        }
        timeline.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(timeline);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private String[] splitAdminAction(String action) {
        if (action == null || !action.startsWith("Status changed to ") || !action.contains(" by ")) {
            return new String[]{action, null};
        }
        int at = action.lastIndexOf(" by ");
        String title = action.substring(0, at);
        String who = action.substring(at + 4).trim();
        if (who.regionMatches(true, 0, "admin ", 0, 6)) {
            return new String[]{title, "Admin: " + who.substring(6).trim()};
        }
        return new String[]{title, "Admin ID: " + who};
    }

    private JPanel buildTimelineRow(String action, java.time.LocalDateTime at, String detail) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel actionLbl = new JLabel(action == null ? "" : action);
        actionLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        actionLbl.setForeground(new Color(15, 23, 42));
        actionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(actionLbl);

        String formattedTime = (at != null) ? at.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) : "";
        JLabel whenLbl = new JLabel(formattedTime);
        whenLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        whenLbl.setForeground(new Color(100, 116, 139));
        whenLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(2));
        card.add(whenLbl);

        if (detail != null && !detail.isEmpty()) {
            JLabel detailLbl = new JLabel(detail);
            detailLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            detailLbl.setForeground(new Color(100, 116, 139));
            detailLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(Box.createVerticalStrut(2));
            card.add(detailLbl);
        }

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private static class TimelineEvent {
        final java.time.LocalDateTime at;
        final String action;
        final String detail;

        TimelineEvent(java.time.LocalDateTime at, String action, String detail) {
            this.at = at;
            this.action = action;
            this.detail = detail;
        }
    }

    private void claimOrReleaseSelected() {
        AssistanceRequest req = getSelectedRequest();
        if (req == null) return;
        if (staff.getStaffId().equals(req.getAssignedStaffId())) {
            return;
        }
        if (!"Pending".equalsIgnoreCase(req.getStatus())) {
            JOptionPane.showMessageDialog(this,
                    "Only Pending requests can be claimed (current: " + req.getStatus() + ").",
                    "Cannot Claim", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            staff.claimRequest(req);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Cannot Claim", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "You are now handling " + req.getRequestId() + ".");
        refreshList(searchField.getText().trim());
        showSelectedHistory();
    }
}