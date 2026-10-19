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
        JMenuItem fileMenuItem = new JMenuItem("+ File Request for Resident");
        JMenuItem updateMenuItem = new JMenuItem("Update Request Status");
        JMenuItem refreshMenuItem = new JMenuItem("Refresh Requests");
        JMenuItem logoutMenuItem = new JMenuItem("Logout");
        staffMenu.add(fileMenuItem);
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
        JButton fileBtn = createDarkBlueButton("+ File Request");
        searchRight.add(sLbl);
        searchRight.add(searchField);
        searchRight.add(viewLbl);
        searchRight.add(assignmentFilterBox);
        searchRight.add(searchBtn);
        searchRight.add(clearBtn);
        searchRight.add(fileBtn);
        toolbar.add(deptLbl, BorderLayout.WEST);
        toolbar.add(searchRight, BorderLayout.EAST);

        // Split
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

        fileBtn.addActionListener(e -> fileRequestDialog());
        fileMenuItem.addActionListener(e -> fileRequestDialog());
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
            // List is already restricted to own assigned type; double-check anyway.
            hasSel = staff.canHandle(sel);
            String st = sel.getStatus();
            boolean cancelled = st != null && "Cancelled".equalsIgnoreCase(st);
            boolean pending = st != null && "Pending".equalsIgnoreCase(st);
            boolean mine = hasSel && staff.getStaffId().equals(sel.getAssignedStaffId());
            String assignee = sel.getAssignedStaffId();
            boolean unassigned = assignee == null || assignee.trim().isEmpty();
            boolean takenByOther = hasSel && !unassigned && !mine;
            // Rule: must claim first before updating. Only the claimer can update.
            // Completed and Cancelled are terminal - never updatable.
            boolean terminal = cancelled
                    || (st != null && "Completed".equalsIgnoreCase(st));
            canUpdate = mine && !terminal;
            if (hasSel) {
                if (mine) {
                    canClaim = true;
                    claimLabel = "Release";
                } else if (takenByOther) {
                    canClaim = false;
                    claimLabel = "Taken by " + assignee.trim();
                } else if (unassigned && pending) {
                    // Strict: only Pending requests can be claimed.
                    canClaim = true;
                    claimLabel = "Claim";
                } else {
                    // Unassigned but not Pending (Completed/Cancelled/In Progress) -> locked.
                    canClaim = false;
                    claimLabel = "Claim";
                }
            }
        }
        claimBtn.setText(claimLabel);
        styleActionButton(claimBtn, canClaim);
        styleActionButton(updateStatusBtn, canUpdate);
        // Delete is admin-only; staff UI has no delete button.
    }

    /** Shows the history timeline for the selected (own-type) request, oldest first. */
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
            historyPanel.add(buildStaffRequestTimeline(req.getRequestId()), BorderLayout.CENTER);
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

        JPanel grid = new JPanel(new GridLayout(0, 2, 32, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.add(makeFieldBlock("Resident ID", req.getResidentId()));
        grid.add(makeFieldBlock("Date Needed", formatDisplayDate(req.getTimeNeeded())));
        String locationVal = req.getLocation();
        if (req instanceof TransportationRequest) {
            TransportationRequest tr = (TransportationRequest) req;
            locationVal = tr.getPickupLocation() + " \u2192 " + tr.getDestination();
        }
        grid.add(makeFieldBlock(req instanceof TransportationRequest ? "Route" : "Location",
                locationVal == null || locationVal.isEmpty() ? "-" : locationVal));
        grid.add(makeFieldBlock("Date Submitted", formatDisplayDate(req.getDateRequested())));
        String handler = req.getAssignedStaffId();
        if (handler == null || handler.trim().isEmpty()) handler = "Unassigned";
        else if (handler.equals(staff.getStaffId())) handler = handler + " (You)";
        grid.add(makeFieldBlock("Handled By", handler));
        // Submitter info looked up from users file (name + contact) so staff
        // can see who filed the request, not just the resident ID.
        String submitterName = null;
        String submitterContact = null;
        for (User u : fileManager.readUsers()) {
            if (u instanceof Resident && req.getResidentId() != null
                    && req.getResidentId().equals(((Resident) u).getResidentId())) {
                submitterName = u.getFullName();
                submitterContact = ((Resident) u).getContactNo();
                break;
            }
        }
        grid.add(makeFieldBlock("Submitted By", submitterName == null ? req.getResidentId() : submitterName));
        grid.add(makeFieldBlock("Contact No", submitterContact == null ? "-" : submitterContact));
        card.add(grid);
        card.add(Box.createVerticalStrut(16));
        card.add(makeDivider());
        card.add(Box.createVerticalStrut(12));

        if (req instanceof MedicalRequest) {
            card.add(makeSectionTitle("Requested Items"));
            card.add(Box.createVerticalStrut(8));
            for (MedicalItem item : ((MedicalRequest) req).getMedicalItems()) {
                card.add(makeItemRow(item.getName(), item.getQuantity() + " " + item.getUnit()));
                card.add(Box.createVerticalStrut(6));
            }
        } else if (req instanceof FoodRequest) {
            card.add(makeSectionTitle("Requested Items"));
            card.add(Box.createVerticalStrut(8));
            for (FoodItem item : ((FoodRequest) req).getFoodItems()) {
                card.add(makeItemRow(item.getName(), item.getQuantity() + " " + item.getUnit()));
                card.add(Box.createVerticalStrut(6));
            }
        } else if (req instanceof TransportationRequest) {
            TransportationRequest tr = (TransportationRequest) req;
            card.add(makeSectionTitle("Transport Details"));
            card.add(Box.createVerticalStrut(8));
            card.add(makeItemRow("Passengers", String.valueOf(tr.getPassengerCount())));
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
                // Type guard + ownership + history inside Staff.updateRequestStatus.
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

    /** Timeline of stored history for one own-type request, oldest first. Never fabricated. */
    private JComponent buildStaffRequestTimeline(String requestId) {
        List<TimelineEvent> events = new ArrayList<>();
        // Only own-type requests reach this method, so the request ID alone selects the entries.
        for (ResidentRequestHistory entry : fileManager.readResidentRequestHistory()) {
            if (requestId.equals(entry.getRequestId())) {
                events.add(new TimelineEvent(entry.getUpdatedAt(), entry.getAction(), null));
            }
        }
        for (StaffRequestHistory entry : fileManager.readStaffRequestHistory()) {
            if (requestId.equals(entry.getRequestId())
                    && staff.getAssignedType().equalsIgnoreCase(entry.getRequestType())) {
                events.add(new TimelineEvent(entry.getProcessedAt(),
                        "Status: " + entry.getStatus(), "Staff ID: " + entry.getStaffId()));
            }
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
                timeline.add(buildTimelineRow(ev, i == events.size() - 1));
            }
        }
        timeline.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(timeline);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JComponent buildTimelineRow(TimelineEvent ev, boolean last) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        TimelineMarker marker = new TimelineMarker(!last);
        marker.setPreferredSize(new Dimension(20, 48));
        row.add(marker, BorderLayout.WEST);
        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        JLabel aLbl = new JLabel("<html><body style='width:190px'>" + escapeHtml(ev.action == null ? "" : ev.action) + "</body></html>");
        aLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        aLbl.setForeground(DARK_BLUE);
        aLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(aLbl);
        JLabel tLbl = new JLabel(ev.at == null ? "" : ev.at.format(
                DateTimeFormatter.ofPattern("MMM d, h:mm a")));
        tLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        tLbl.setForeground(new Color(100, 116, 139));
        tLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(tLbl);
        if (ev.detail != null && !ev.detail.isEmpty()) {
            JLabel dLbl = new JLabel("<html><body style='width:190px'>" + escapeHtml(ev.detail) + "</body></html>");
            dLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
            dLbl.setForeground(new Color(100, 116, 139));
            dLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            text.add(dLbl);
        }
        row.add(text, BorderLayout.CENTER);
        return row;
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

    private static class TimelineMarker extends JComponent {
        private final boolean drawLine;

        TimelineMarker(boolean drawLine) {
            this.drawLine = drawLine;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                if (drawLine) {
                    g2.setColor(new Color(170, 186, 210));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(cx, 14, cx, getHeight());
                }
                g2.setColor(DARK_BLUE);
                g2.fillOval(cx - 5, 4, 10, 10);
            } finally {
                g2.dispose();
            }
        }
    }

    /** Claims the selected request, or releases it when already claimed by self. */
    private void claimOrReleaseSelected() {
        AssistanceRequest req = getSelectedRequest();
        if (req == null) return;
        if (staff.getStaffId().equals(req.getAssignedStaffId())) {
            try {
                staff.releaseRequest(req);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Cannot Release", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Released " + req.getRequestId() + " back to the pool.");
            refreshList(searchField.getText().trim());
            showSelectedHistory();
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


    /** Files a request on behalf of a resident, locked to this staff type. */
    private void fileRequestDialog() {
        String onlyType = staff.getAssignedType();
        List<Resident> residents = new ArrayList<>();
        for (User u : fileManager.readUsers()) {
            if (u instanceof Resident) residents.add((Resident) u);
        }
        if (residents.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No residents found.",
                    "No Residents", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JComboBox<String> residentBox = new JComboBox<>();
        for (Resident r : residents) {
            residentBox.addItem(r.getFullName() + "  (" + r.getResidentId() + ")");
        }
        JLabel typeLbl = new JLabel(onlyType + " Assistance");
        typeLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        typeLbl.setForeground(DARK_BLUE);
        JPanel typeHolder = new JPanel(new BorderLayout());
        typeHolder.setBackground(new Color(248, 250, 252));
        typeHolder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        typeHolder.add(typeLbl, BorderLayout.CENTER);
        typeHolder.setPreferredSize(new Dimension(260, 36));
        RequestFormHelper.Fields fields = new RequestFormHelper.Fields();
        // Fields() starts with food units; replace the starting row with this type's units.
        fields.itemRows.clear();
        fields.itemRows.add(newItemRow(RequestFormHelper.toRequestType(onlyType)));
        JPanel formPanel = new JPanel();
        rebuildStaffRequestForm(formPanel, onlyType, fields);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(4, 4, 12, 4));
        top.add(formRow("Resident", residentBox));
        top.add(formRow("Assistance Type", typeHolder));
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        body.add(top);
        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        formScroll.setPreferredSize(new Dimension(560, 380));
        formScroll.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        body.add(formScroll);
        JDialog dialog = new JDialog(this, "File " + onlyType + " Request", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.add(body, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)));
        JButton submitBtn = createDarkBlueButton("Submit Request");
        JButton cancelBtn = createDarkBlueButton("Cancel");
        buttons.add(cancelBtn);
        buttons.add(submitBtn);
        dialog.add(buttons, BorderLayout.SOUTH);

        submitBtn.addActionListener(e -> {
            int rIdx = residentBox.getSelectedIndex();
            if (rIdx < 0 || rIdx >= residents.size()) return;
            Resident target = residents.get(rIdx);
            String type = RequestFormHelper.toRequestType(onlyType);
            if (!RequestFormHelper.hasRequiredFields(dialog, type, fields)) return;
            try {
                AssistanceRequest req = RequestFormHelper.createRequest(type,
                        requestManager.nextRequestId(), target.getResidentId(),
                        fields, "Pending", java.time.LocalDateTime.now());
                // Type guard inside Staff.fileRequestFor; "Filed" recorded there.
                staff.fileRequestFor(target.getResidentId(), req);
                fileManager.recordResidentRequestAction(target, req, "Filed by staff");
                refreshList(searchField.getText().trim());
                showSelectedHistory();
                JOptionPane.showMessageDialog(dialog,
                        "Request filed for " + target.getFullName()
                                + ".\nReference No.: " + req.getRequestId(),
                        "Request Filed", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(),
                        "Invalid Request", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Could not save the request: " + ex.getMessage(),
                        "Save Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.pack();
        dialog.setMinimumSize(new Dimension(620, 600));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static final String[] FOOD_UNITS = {"kg", "pcs", "pack", "g", "liter", "ml"};
    private static final String[] MEDICAL_UNITS = {"pcs", "box", "bottle", "pack", "tablet", "ml"};

    /** New item row using the unit list for the given request type. */
    private RequestFormHelper.ItemRow newItemRow(String type) {
        return new RequestFormHelper.ItemRow("Food".equals(type) ? FOOD_UNITS : MEDICAL_UNITS);
    }

    private JPanel formRow(String label, JComponent input) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        int h = Math.max(36, input.getPreferredSize().height + 4);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, h + 8));
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(DARK_BLUE);
        lbl.setPreferredSize(new Dimension(150, 28));
        lbl.setVerticalAlignment(SwingConstants.TOP);
        row.add(lbl, BorderLayout.WEST);
        row.add(input, BorderLayout.CENTER);
        return row;
    }

    private void styleFormField(JComponent field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        if (field instanceof JTextField) {
            ((JTextField) field).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        }
    }

    /** Form body for the staff member's own assigned type only (no type picker). */
    private void rebuildStaffRequestForm(JPanel panel, String onlyType,
                                         RequestFormHelper.Fields fields) {
        String type = RequestFormHelper.toRequestType(onlyType);
        panel.removeAll();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        // Section heading, left aligned (same as the resident form).
        JLabel heading = new JLabel((type + " Assistance").toUpperCase());
        heading.setFont(new Font("SansSerif", Font.BOLD, 14));
        heading.setForeground(DARK_BLUE);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        panel.add(heading);
        panel.add(Box.createVerticalStrut(4));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setForeground(new Color(220, 225, 230));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep);
        panel.add(Box.createVerticalStrut(14));

        if (!"Transportation".equals(type)) {
            String itemLabel = "Food".equals(type) ? "Food Item" : "Medical Item";

            // "Requested Items" header with + and - buttons (same as the resident form).
            JPanel itemHeader = new JPanel(new BorderLayout());
            itemHeader.setOpaque(false);
            itemHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            JLabel itemsLbl = new JLabel("Requested Items");
            itemsLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            itemsLbl.setForeground(DARK_BLUE);
            itemHeader.add(itemsLbl, BorderLayout.WEST);
            JPanel itemBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            itemBtns.setOpaque(false);
            JButton addItemBtn = createDarkBlueButton("+");
            JButton removeItemBtn = createDarkBlueButton("-");
            removeItemBtn.setEnabled(fields.itemRows.size() > 1);
            itemBtns.add(addItemBtn);
            itemBtns.add(removeItemBtn);
            itemHeader.add(itemBtns, BorderLayout.EAST);
            panel.add(itemHeader);
            panel.add(Box.createVerticalStrut(8));

            // Column headers
            JPanel colHeader = new JPanel(new GridLayout(1, 3, 12, 0));
            colHeader.setOpaque(false);
            colHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            colHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
            for (String h : new String[]{itemLabel, "Quantity", "Unit"}) {
                JLabel hl = new JLabel(h);
                hl.setFont(new Font("SansSerif", Font.BOLD, 11));
                hl.setForeground(new Color(100, 116, 139));
                colHeader.add(hl);
            }
            panel.add(colHeader);
            panel.add(Box.createVerticalStrut(6));

            // Item rows
            for (RequestFormHelper.ItemRow r : fields.itemRows) {
                JPanel itemRow = new JPanel(new GridLayout(1, 3, 12, 0));
                itemRow.setOpaque(false);
                itemRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                styleFormField(r.nameField);
                styleFormField(r.quantityField);
                itemRow.add(r.nameField);
                itemRow.add(r.quantityField);
                itemRow.add(r.unitBox);
                panel.add(itemRow);
                panel.add(Box.createVerticalStrut(6));
            }

            addItemBtn.addActionListener(e -> {
                fields.itemRows.add(newItemRow(type));
                rebuildStaffRequestForm(panel, onlyType, fields);
            });
            removeItemBtn.addActionListener(e -> {
                if (fields.itemRows.size() > 1) {
                    fields.itemRows.remove(fields.itemRows.size() - 1);
                    rebuildStaffRequestForm(panel, onlyType, fields);
                }
            });

            panel.add(Box.createVerticalStrut(10));
            styleFormField(fields.locationField);
            panel.add(formRow("Location / Address", fields.locationField));
        } else {
            styleFormField(fields.pickupField);
            styleFormField(fields.destinationField);
            styleFormField(fields.passengerCountField);
            panel.add(formRow("Pickup Location", fields.pickupField));
            panel.add(formRow("Destination", fields.destinationField));
            panel.add(formRow("No. of Passengers", fields.passengerCountField));
        }

        panel.add(Box.createVerticalStrut(6));
        styleFormField(fields.whenNeededField);
        panel.add(formRow("Time Needed", fields.whenNeededField));
        JLabel hint = new JLabel("Format: yyyy-MM-dd HH:mm");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 10));
        hint.setForeground(new Color(148, 163, 184));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setBorder(BorderFactory.createEmptyBorder(0, 160, 8, 0));
        panel.add(hint);

        fields.additionalDetailsArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        fields.additionalDetailsArea.setLineWrap(true);
        fields.additionalDetailsArea.setWrapStyleWord(true);
        fields.additionalDetailsArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane detailsScroll = new JScrollPane(fields.additionalDetailsArea);
        detailsScroll.setBorder(BorderFactory.createLineBorder(new Color(180, 190, 210), 1));
        detailsScroll.setPreferredSize(new Dimension(320, 72));
        detailsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.add(formRow("Additional Details", detailsScroll));
        panel.revalidate();
        panel.repaint();
    }
}