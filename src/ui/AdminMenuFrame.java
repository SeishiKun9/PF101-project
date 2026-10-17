package ui;

import data.FileManager;
import data.RequestManager;
import model.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Admin menu: manage users and view all requests.
 * Styled like ResidentMenuFrame (dark blue, split panels).
 */
public class AdminMenuFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final Admin admin;
    private final FileManager fileManager = new FileManager();
    private final RequestManager requestManager = new RequestManager(fileManager);

    private static final Color DARK_BLUE = new Color(15, 40, 80);
    private static final Color DARK_BLUE_BTN = new Color(25, 70, 140);
    private static final Color GRAY_DISABLED = new Color(148, 163, 184);

    private final DefaultListModel<String> userListModel = new DefaultListModel<>();
    private final JList<String> userList = new JList<>(userListModel);
    private final JPanel userDetailsPanel = new JPanel();
    private final JLabel userDetailsPlaceholder =
            new JLabel("Select a user to view details", SwingConstants.CENTER);
    private final JTextField userSearchField = new JTextField(14);
    private List<User> currentUsers = new ArrayList<>();
    private final JButton manageUserBtn = new JButton("Manage User");

    private final DefaultListModel<String> requestListModel = new DefaultListModel<>();
    private final JList<String> requestList = new JList<>(requestListModel);
    private final JPanel requestDetailsPanel = new JPanel();
    private final JLabel requestDetailsPlaceholder =
            new JLabel("Select a request to view details", SwingConstants.CENTER);
    private final JTextField requestSearchField = new JTextField(14);
    private List<AssistanceRequest> currentRequests = new ArrayList<>();
    private final JButton deleteRequestBtn = new JButton("Delete Request");
    private final JPanel requestListCards = new JPanel(new CardLayout());
    private final JPanel requestHistoryPanel = new JPanel(new BorderLayout());

    public AdminMenuFrame(Admin admin) {
        super("Admin Menu - " + admin.getFullName());
        this.admin = admin;
        buildUI();
        refreshUserList();
        refreshRequestList(null);
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

    private void styleList(JList<String> list) {
        list.setFont(new Font("SansSerif", Font.PLAIN, 13));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(36);
        list.setBorder(new EmptyBorder(4, 8, 4, 8));
        list.setBackground(Color.WHITE);
        list.setSelectionBackground(new Color(219, 234, 254));
        list.setSelectionForeground(DARK_BLUE);
    }

    private JSplitPane makeSplit(JComponent left, JComponent right, int div) {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(div);
        split.setDividerSize(2);
        split.setBorder(null);
        split.setContinuousLayout(true);
        split.setLeftComponent(left);
        split.setRightComponent(right);
        split.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(new Color(180, 190, 210));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
            }
        });
        return split;
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1040, 680);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 230)));
        JMenu adminMenu = new JMenu("Admin Menu");
        adminMenu.setFont(adminMenu.getFont().deriveFont(Font.BOLD, 12f));
        JMenuItem createMenuItem = new JMenuItem("+ Create User");
        JMenuItem editMenuItem = new JMenuItem("Manage Selected User");
        JMenuItem historyMenuItem = new JMenuItem("View User History");
        JMenuItem myHistoryMenuItem = new JMenuItem("View My Admin History");
        JMenuItem refreshReqMenuItem = new JMenuItem("Refresh All Requests");
        JMenuItem deleteReqMenuItem = new JMenuItem("Delete Selected Request");
        JMenuItem logoutMenuItem = new JMenuItem("Logout");
        adminMenu.add(createMenuItem);
        adminMenu.add(editMenuItem);
        adminMenu.add(historyMenuItem);
        adminMenu.add(myHistoryMenuItem);
        adminMenu.addSeparator();
        adminMenu.add(refreshReqMenuItem);
        adminMenu.add(deleteReqMenuItem);
        adminMenu.addSeparator();
        adminMenu.add(logoutMenuItem);
        menuBar.add(adminMenu);
        setJMenuBar(menuBar);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DARK_BLUE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 28, 16, 28));
        JPanel nameAndMeta = new JPanel();
        nameAndMeta.setLayout(new BoxLayout(nameAndMeta, BoxLayout.Y_AXIS));
        nameAndMeta.setOpaque(false);
        JLabel nameLbl = new JLabel(admin.getFullName());
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 18f));
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel metaLbl = new JLabel("Admin ID: " + admin.getAdminId() + "  \u2022  Full System Access");
        metaLbl.setFont(metaLbl.getFont().deriveFont(Font.PLAIN, 12f));
        metaLbl.setForeground(new Color(180, 200, 230));
        metaLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameAndMeta.add(nameLbl);
        nameAndMeta.add(Box.createVerticalStrut(4));
        nameAndMeta.add(metaLbl);
        headerPanel.add(nameAndMeta, BorderLayout.WEST);
        JButton logoutBtn = createDarkBlueButton("Logout");
        JButton myHistoryBtn = createDarkBlueButton("My History");
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        rightHeader.setOpaque(false);
        rightHeader.add(myHistoryBtn);
        rightHeader.add(logoutBtn);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(0, 12));
        mainContent.setBackground(new Color(245, 247, 250));
        mainContent.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(tabs.getFont().deriveFont(Font.BOLD, 12.5f));
        tabs.setBackground(Color.WHITE);

        // ========== USERS TAB (split) ==========
        JPanel userOuter = new JPanel(new BorderLayout());
        userOuter.setBackground(Color.WHITE);

        JPanel userTop = new JPanel(new BorderLayout());
        userTop.setBackground(new Color(245, 247, 250));
        userTop.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JButton createBtn = createDarkBlueButton("+ Create User");
        JPanel userLeftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userLeftActions.setOpaque(false);
        userLeftActions.add(createBtn);
        JPanel userSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        userSearch.setOpaque(false);
        JLabel uLbl = new JLabel("Search:");
        uLbl.setFont(uLbl.getFont().deriveFont(Font.BOLD, 12f));
        uLbl.setForeground(DARK_BLUE);
        userSearchField.setPreferredSize(new Dimension(160, 30));
        userSearchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        JButton searchUsersBtn = createDarkBlueButton("Search");
        JButton clearUserSearchBtn = createDarkBlueButton("Clear");
        userSearch.add(uLbl);
        userSearch.add(userSearchField);
        userSearch.add(searchUsersBtn);
        userSearch.add(clearUserSearchBtn);
        userTop.add(userLeftActions, BorderLayout.WEST);
        userTop.add(userSearch, BorderLayout.EAST);

        JPanel userLeft = new JPanel(new BorderLayout());
        userLeft.setBackground(Color.WHITE);
        userLeft.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, new Color(180, 190, 210)));
        JLabel userListTitle = new JLabel("  Users");
        userListTitle.setFont(userListTitle.getFont().deriveFont(Font.BOLD, 13f));
        userListTitle.setForeground(DARK_BLUE);
        userListTitle.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        userListTitle.setBackground(new Color(248, 250, 252));
        userListTitle.setOpaque(true);
        styleList(userList);
        JScrollPane userListScroll = new JScrollPane(userList);
        userListScroll.setBorder(null);
        userLeft.add(userListTitle, BorderLayout.NORTH);
        userLeft.add(userListScroll, BorderLayout.CENTER);

        JPanel userRight = new JPanel(new BorderLayout());
        userRight.setBackground(Color.WHITE);
        JLabel userDetailsTitle = new JLabel("  User Details");
        userDetailsTitle.setFont(userDetailsTitle.getFont().deriveFont(Font.BOLD, 13f));
        userDetailsTitle.setForeground(DARK_BLUE);
        userDetailsTitle.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        userDetailsTitle.setBackground(new Color(248, 250, 252));
        userDetailsTitle.setOpaque(true);
        userDetailsPanel.setLayout(new BorderLayout());
        userDetailsPanel.setBackground(Color.WHITE);
        userDetailsPlaceholder.setFont(userDetailsPlaceholder.getFont().deriveFont(Font.PLAIN, 14f));
        userDetailsPlaceholder.setForeground(new Color(148, 163, 184));
        userDetailsPanel.add(userDetailsPlaceholder, BorderLayout.CENTER);
        JScrollPane userDetailsScroll = new JScrollPane(userDetailsPanel);
        userDetailsScroll.setBorder(null);

        styleActionButton(manageUserBtn, false);
        JPanel userBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        userBottom.setBackground(Color.WHITE);
        userBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)));
        userBottom.add(manageUserBtn);

        userRight.add(userDetailsTitle, BorderLayout.NORTH);
        userRight.add(userDetailsScroll, BorderLayout.CENTER);
        userRight.add(userBottom, BorderLayout.SOUTH);

        JSplitPane userSplit = makeSplit(userLeft, userRight, 280);
        userOuter.add(userTop, BorderLayout.NORTH);
        userOuter.add(userSplit, BorderLayout.CENTER);
        tabs.addTab("  User Management  ", userOuter);

        // ========== REQUESTS TAB (split) ==========
        JPanel reqOuter = new JPanel(new BorderLayout());
        reqOuter.setBackground(Color.WHITE);
        JPanel reqTop = new JPanel(new BorderLayout());
        reqTop.setBackground(new Color(245, 247, 250));
        reqTop.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JPanel reqSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        reqSearch.setOpaque(false);
        JLabel rLbl = new JLabel("Search:");
        rLbl.setFont(rLbl.getFont().deriveFont(Font.BOLD, 12f));
        rLbl.setForeground(DARK_BLUE);
        requestSearchField.setPreferredSize(new Dimension(160, 30));
        requestSearchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        JButton searchReqBtn = createDarkBlueButton("Search");
        JButton clearReqBtn = createDarkBlueButton("Clear");
        reqSearch.add(rLbl);
        reqSearch.add(requestSearchField);
        reqSearch.add(searchReqBtn);
        reqSearch.add(clearReqBtn);
        reqTop.add(reqSearch, BorderLayout.EAST);

        // Left panel (about 22%): request IDs and statuses
        styleList(requestList);
        JScrollPane reqListScroll = new JScrollPane(requestList);
        reqListScroll.setBorder(null);
        requestListCards.removeAll();
        requestListCards.add(reqListScroll, "list");
        requestListCards.add(makeEmptyState("No assistance requests found."), "empty");
        JPanel reqLeft = createTitledPanel("  All Requests", requestListCards);

        // Middle panel (about 39%): details of the selected request
        requestDetailsPanel.setLayout(new BorderLayout());
        requestDetailsPanel.setBackground(Color.WHITE);
        requestDetailsPlaceholder.setFont(requestDetailsPlaceholder.getFont().deriveFont(Font.PLAIN, 14f));
        requestDetailsPlaceholder.setForeground(new Color(148, 163, 184));
        requestDetailsPanel.add(requestDetailsPlaceholder, BorderLayout.CENTER);
        JScrollPane reqDetailsScroll = new JScrollPane(requestDetailsPanel);
        reqDetailsScroll.setBorder(null);
        styleActionButton(deleteRequestBtn, false);
        JPanel reqBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        reqBottom.setBackground(Color.WHITE);
        reqBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)));
        reqBottom.add(deleteRequestBtn);
        JPanel reqMiddle = createTitledPanel("  Request Details", reqDetailsScroll);
        reqMiddle.add(reqBottom, BorderLayout.SOUTH);

        // Right panel (about 39%): activity timeline for the selected request
        requestHistoryPanel.setBackground(Color.WHITE);
        requestHistoryPanel.add(makeEmptyState("Select a request to view its history."), BorderLayout.CENTER);
        JPanel reqRight = createTitledPanel("  Request History", requestHistoryPanel);

        // Outer split: 22% | rest. Inner split: middle and right share the rest equally (39% each).
        JSplitPane middleRightSplit = makeSplit(reqMiddle, reqRight, 390);
        middleRightSplit.setResizeWeight(0.5);
        JSplitPane reqSplit = makeSplit(reqLeft, middleRightSplit, 220);
        reqSplit.setResizeWeight(0.22);

        reqOuter.add(reqTop, BorderLayout.NORTH);
        reqOuter.add(reqSplit, BorderLayout.CENTER);
        tabs.addTab("  All Assistance Requests  ", reqOuter);

        mainContent.add(tabs, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        // Listeners
        createBtn.addActionListener(e -> createUserDialog());
        createMenuItem.addActionListener(e -> createUserDialog());
        manageUserBtn.addActionListener(e -> editSelectedUser());
        editMenuItem.addActionListener(e -> editSelectedUser());
        historyMenuItem.addActionListener(e -> { admin.viewUserHistory(); viewSelectedUserHistory(); });
        myHistoryBtn.addActionListener(e -> viewOwnAdminHistory());
        myHistoryMenuItem.addActionListener(e -> viewOwnAdminHistory());
        searchUsersBtn.addActionListener(e -> refreshUserList(userSearchField.getText().trim()));
        userSearchField.addActionListener(e -> refreshUserList(userSearchField.getText().trim()));
        clearUserSearchBtn.addActionListener(e -> { userSearchField.setText(""); refreshUserList(""); });

        searchReqBtn.addActionListener(e -> refreshRequestList(requestSearchField.getText().trim()));
        requestSearchField.addActionListener(e -> refreshRequestList(requestSearchField.getText().trim()));
        clearReqBtn.addActionListener(e -> { requestSearchField.setText(""); refreshRequestList(null); });
        refreshReqMenuItem.addActionListener(e -> { admin.viewAllRequest(); refreshRequestList(requestSearchField.getText().trim()); });
        deleteRequestBtn.addActionListener(e -> deleteSelectedRequest());
        deleteReqMenuItem.addActionListener(e -> deleteSelectedRequest());

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedUserDetails();
                int idx = userList.getSelectedIndex();
                boolean ok = idx >= 0 && idx < currentUsers.size();
                styleActionButton(manageUserBtn, ok);
            }
        });
        requestList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedRequestDetails();
                showSelectedRequestHistory();
                int idx = requestList.getSelectedIndex();
                styleActionButton(deleteRequestBtn, idx >= 0 && idx < currentRequests.size());
            }
        });

        logoutBtn.addActionListener(e -> { admin.logout(); dispose(); new LoginFrame(fileManager).setVisible(true); });
        logoutMenuItem.addActionListener(e -> { admin.logout(); dispose(); new LoginFrame(fileManager).setVisible(true); });
    }

    private void refreshUserList() {
        refreshUserList(userSearchField.getText().trim());
    }

    private void refreshUserList(String searchTerm) {
        userListModel.clear();
        currentUsers.clear();
        String query = searchTerm == null ? "" : searchTerm.toLowerCase();
        for (User u : fileManager.readUsers()) {
            String id = getUserId(u);
            String searchable = (u.getRole() + " " + u.getFullName() + " " + id).toLowerCase();
            if (!query.isEmpty() && !searchable.contains(query)) continue;
            currentUsers.add(u);
            userListModel.addElement(u.getFullName() + "  (" + u.getRole() + ")");
        }
        userDetailsPanel.removeAll();
        userDetailsPanel.setLayout(new BorderLayout());
        userDetailsPanel.add(userDetailsPlaceholder, BorderLayout.CENTER);
        userDetailsPanel.revalidate();
        userDetailsPanel.repaint();
        styleActionButton(manageUserBtn, false);
    }

    private void showSelectedUserDetails() {
        int idx = userList.getSelectedIndex();
        userDetailsPanel.removeAll();
        if (idx < 0 || idx >= currentUsers.size()) {
            userDetailsPanel.setLayout(new BorderLayout());
            userDetailsPanel.add(userDetailsPlaceholder, BorderLayout.CENTER);
            userDetailsPanel.revalidate();
            userDetailsPanel.repaint();
            return;
        }
        User u = currentUsers.get(idx);
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JLabel nameLbl = new JLabel(u.getFullName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLbl.setForeground(DARK_BLUE);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(nameLbl);
        card.add(Box.createVerticalStrut(4));
        JLabel roleLbl = new JLabel(u.getRole());
        roleLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        roleLbl.setForeground(new Color(100, 116, 139));
        roleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(roleLbl);
        card.add(Box.createVerticalStrut(16));
        card.add(makeDivider());
        card.add(Box.createVerticalStrut(12));

        JPanel grid = new JPanel(new GridLayout(0, 2, 32, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.add(makeFieldBlock("User ID", getUserId(u)));
        grid.add(makeFieldBlock("Role", u.getRole()));
        if (u instanceof Resident) {
            Resident r = (Resident) u;
            grid.add(makeFieldBlock("Address", r.getAddress()));
            grid.add(makeFieldBlock("Contact", r.getContactNo()));
        } else if (u instanceof Staff) {
            Staff s = (Staff) u;
            grid.add(makeFieldBlock("Assigned Type", s.getAssignedType()));
            grid.add(makeFieldBlock("Staff ID", s.getStaffId()));
        } else if (u instanceof Admin) {
            grid.add(makeFieldBlock("Admin ID", ((Admin) u).getAdminId()));
            grid.add(makeFieldBlock("Access", "Full System"));
        }
        card.add(grid);

        userDetailsPanel.setLayout(new BorderLayout());
        userDetailsPanel.add(card, BorderLayout.NORTH);
        userDetailsPanel.revalidate();
        userDetailsPanel.repaint();
    }

    private void refreshRequestList(String searchTerm) {
        // Remember the selection so it can be restored if it is still in the results.
        String previousId = getSelectedRequestId();
        requestListModel.clear();
        currentRequests.clear();
        String query = searchTerm == null ? "" : searchTerm.toLowerCase();
        for (AssistanceRequest r : requestManager.getAllRequests()) {
            if (!query.isEmpty()
                    && !r.getRequestId().toLowerCase().contains(query)
                    && !r.getResidentId().toLowerCase().contains(query)
                    && !r.getStatus().toLowerCase().contains(query)
                    && !r.getType().toLowerCase().contains(query)) continue;
            currentRequests.add(r);
            requestListModel.addElement(r.getRequestId() + "  (" + r.getStatus() + ")");
        }
        ((CardLayout) requestListCards.getLayout())
                .show(requestListCards, currentRequests.isEmpty() ? "empty" : "list");

        int restoreIdx = -1;
        for (int i = 0; previousId != null && i < currentRequests.size(); i++) {
            if (currentRequests.get(i).getRequestId().equals(previousId)) {
                restoreIdx = i;
                break;
            }
        }
        if (restoreIdx >= 0) {
            // The list listener refreshes the details and history panels.
            requestList.setSelectedIndex(restoreIdx);
        } else {
            requestList.clearSelection();
            showSelectedRequestDetails();
            showSelectedRequestHistory();
            styleActionButton(deleteRequestBtn, false);
        }
    }

    private String getSelectedRequestId() {
        int idx = requestList.getSelectedIndex();
        if (idx < 0 || idx >= currentRequests.size()) return null;
        return currentRequests.get(idx).getRequestId();
    }

    /** Shows the timeline for the selected request, or a placeholder when nothing is selected. */
    private void showSelectedRequestHistory() {
        requestHistoryPanel.removeAll();
        String requestId = getSelectedRequestId();
        if (requestId == null) {
            requestHistoryPanel.add(makeEmptyState("Select a request to view its history."), BorderLayout.CENTER);
        } else {
            requestHistoryPanel.add(buildAdminRequestTimeline(requestId), BorderLayout.CENTER);
        }
        requestHistoryPanel.revalidate();
        requestHistoryPanel.repaint();
    }

    /** Timeline of one request's stored history, oldest first, in its own scroll pane. */
    private JComponent buildAdminRequestTimeline(String requestId) {
        List<TimelineEvent> events = loadRequestTimeline(requestId);
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
                timeline.add(buildTimelineRow(ev.action, ev.at, ev.detail, i == events.size() - 1));
            }
        }
        timeline.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(timeline);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** Stored resident updates and staff processing records for one request, oldest first. */
    private List<TimelineEvent> loadRequestTimeline(String requestId) {
        List<TimelineEvent> events = new ArrayList<>();
        for (ResidentRequestHistory entry : fileManager.readResidentRequestHistory()) {
            if (requestId.equals(entry.getRequestId())) {
                events.add(new TimelineEvent(entry.getUpdatedAt(), entry.getAction(), null));
            }
        }
        for (StaffRequestHistory entry : fileManager.readStaffRequestHistory()) {
            if (requestId.equals(entry.getRequestId())) {
                events.add(new TimelineEvent(entry.getProcessedAt(),
                        "Status changed to " + entry.getStatus(), "Staff ID: " + entry.getStaffId()));
            }
        }
        events.sort((a, b) -> a.at.compareTo(b.at));
        return events;
    }

    /** A single timeline entry; detail is null when the stored record has no extra information. */
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

    /** A panel with a title bar above its content, styled like the other admin panel titles. */
    private JPanel createTitledPanel(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(titleLbl.getFont().deriveFont(Font.BOLD, 13f));
        titleLbl.setForeground(DARK_BLUE);
        titleLbl.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        titleLbl.setBackground(new Color(248, 250, 252));
        titleLbl.setOpaque(true);
        panel.add(titleLbl, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void showSelectedRequestDetails() {
        int idx = requestList.getSelectedIndex();
        requestDetailsPanel.removeAll();
        if (idx < 0 || idx >= currentRequests.size()) {
            requestDetailsPanel.setLayout(new BorderLayout());
            requestDetailsPanel.add(requestDetailsPlaceholder, BorderLayout.CENTER);
            requestDetailsPanel.revalidate();
            requestDetailsPanel.repaint();
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

        JPanel grid = new JPanel(new GridLayout(2, 2, 32, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        grid.add(makeFieldBlock("Resident ID", req.getResidentId()));
        grid.add(makeFieldBlock("Type", req.getType()));
        String locationVal = req.getLocation();
        if (req instanceof TransportationRequest) {
            TransportationRequest tr = (TransportationRequest) req;
            locationVal = tr.getPickupLocation() + " \u2192 " + tr.getDestination();
        }
        grid.add(makeFieldBlock(req instanceof TransportationRequest ? "Route" : "Location",
                locationVal == null || locationVal.isEmpty() ? "-" : locationVal));
        grid.add(makeFieldBlock("Date Submitted", formatDisplayDate(req.getDateRequested())));
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

        requestDetailsPanel.setLayout(new BorderLayout());
        requestDetailsPanel.add(card, BorderLayout.NORTH);
        requestDetailsPanel.revalidate();
        requestDetailsPanel.repaint();
    }

    private JPanel makeFieldBlock(String label, String value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(new Color(100, 116, 139));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel v = new JLabel(value == null || value.isEmpty() ? "-" : value);
        v.setFont(new Font("SansSerif", Font.BOLD, 14));
        v.setForeground(new Color(15, 23, 42));
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(v);
        return p;
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

    private void deleteSelectedRequest() {
        int idx = requestList.getSelectedIndex();
        if (idx < 0 || idx >= currentRequests.size()) {
            JOptionPane.showMessageDialog(this, "Please select a request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AssistanceRequest req = currentRequests.get(idx);
        int confirm = JOptionPane.showConfirmDialog(this, "Permanently delete request " + req.getRequestId() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            requestManager.deleteRequest(req.getRequestId());
            logAdminAction("Deleted request " + req.getRequestId());
            refreshRequestList(requestSearchField.getText().trim());
        }
    }

    private User getSelectedUser() {
        int idx = userList.getSelectedIndex();
        if (idx < 0 || idx >= currentUsers.size()) {
            JOptionPane.showMessageDialog(this, "Please select a user first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentUsers.get(idx);
    }

    private void styleFormField(JComponent field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        if (field instanceof JTextField) {
            ((JTextField) field).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        }
    }

    private JPanel formRow(String label, JComponent input) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(DARK_BLUE);
        lbl.setPreferredSize(new Dimension(160, 28));
        row.add(lbl, BorderLayout.WEST);
        row.add(input, BorderLayout.CENTER);
        return row;
    }

    private void createUserDialog() {
        String[] roles = {"Resident", "Admin", "MedicalStaff", "FoodStaff", "TransportationStaff"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        JTextField nameField = new JTextField();
        JTextField passField = new JTextField();
        JTextField extra1 = new JTextField();
        JTextField extra2 = new JTextField();
        styleFormField(nameField);
        styleFormField(passField);
        styleFormField(extra1);
        styleFormField(extra2);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));
        panel.add(formRow("Role", roleBox));
        panel.add(formRow("Name", nameField));
        panel.add(formRow("Password", passField));
        panel.add(formRow("Address (Resident)", extra1));
        panel.add(formRow("Contact No (Resident)", extra2));

        JDialog dialog = new JDialog(this, "Create New User", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)));
        JButton okBtn = createDarkBlueButton("Create User");
        JButton cancelBtn = createDarkBlueButton("Cancel");
        buttons.add(cancelBtn);
        buttons.add(okBtn);
        dialog.add(buttons, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            String role = (String) roleBox.getSelectedItem();
            String name = nameField.getText().trim();
            String pass = passField.getText().trim();
            if (name.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name and password are required.");
                return;
            }
            User newUser;
            switch (role) {
                case "Resident":
                    String contact = extra2.getText().trim();
                    if (!contact.matches("\\+?[0-9]+")) {
                        JOptionPane.showMessageDialog(dialog, "Contact number must be numeric.",
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
            if (newUser instanceof Staff) {
                ((Staff) newUser).setAssignedType(getAssignedTypeForRole(role));
            }
            admin.createUser(newUser);
            logAdminAction("Created user " + getUserId(newUser) + " (" + role + ")");
            refreshUserList();
            JOptionPane.showMessageDialog(dialog, "User created.");
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setSize(520, 420);
        dialog.setMinimumSize(new Dimension(480, 380));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
        userIdField.setEditable(false);
        userIdField.setBackground(new Color(241, 245, 249));
        String address = "";
        String contact = "";
        if (u instanceof Resident) {
            Resident resident = (Resident) u;
            address = resident.getAddress();
            contact = resident.getContactNo();
        }
        JTextField addressField = new JTextField(address);
        JTextField contactField = new JTextField(contact);
        styleFormField(nameField);
        styleFormField(passwordField);
        styleFormField(userIdField);
        styleFormField(addressField);
        styleFormField(contactField);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));

        Runnable rebuildFields = () -> {
            String selectedRole = (String) roleBox.getSelectedItem();
            panel.removeAll();
            panel.add(formRow("Role", roleBox));
            panel.add(formRow("Name", nameField));
            panel.add(formRow("Password", passwordField));
            panel.add(formRow("User ID", userIdField));
            if ("Resident".equals(selectedRole)) {
                panel.add(formRow("Address", addressField));
                panel.add(formRow("Contact No", contactField));
            }
            panel.revalidate();
            panel.repaint();
            dialogPackSafe();
        };

        final JDialog[] dialogRef = new JDialog[1];
        // Residents may not change role, so the selector is locked for them.
        roleBox.setEnabled(!(u instanceof Resident));
        roleBox.addActionListener(e -> {
            String selectedRole = (String) roleBox.getSelectedItem();
            // Only regenerate the ID when the new role needs a different prefix,
            // so a staff-to-staff change keeps the existing ID.
            String currentId = userIdField.getText().trim();
            if (!currentId.toUpperCase().startsWith(getUserIdPrefix(selectedRole))) {
                userIdField.setText(createUserIdForRole(selectedRole));
            }
            rebuildFields.run();
        });
        rebuildFields.run();

        JDialog dialog = new JDialog(this, "Manage User", true);
        dialogRef[0] = dialog;
        dialog.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        // Delete uses the same dark-blue theme as the rest of the admin interface.
        JButton deleteBtn = createDarkBlueButton("Delete User");

        // Cancel + Save on the right
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(cancelBtn.getFont().deriveFont(Font.BOLD, 12f));
        cancelBtn.setBackground(new Color(235, 242, 252));
        cancelBtn.setForeground(DARK_BLUE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)));
        JButton saveBtn = createDarkBlueButton("Save Changes");
        rightBtns.add(cancelBtn);
        rightBtns.add(saveBtn);

        buttons.add(deleteBtn, BorderLayout.WEST);
        buttons.add(rightBtns, BorderLayout.EAST);
        dialog.add(buttons, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String newPassword = passwordField.getText().trim();
            String newUserId = userIdField.getText().trim();
            // Enforced here as well as in the UI: a Resident can never change role.
            String newRole = (u instanceof Resident) ? "Resident" : (String) roleBox.getSelectedItem();
            if (newName.isEmpty() || newPassword.isEmpty() || newUserId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name, password, and UserID are required.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String requiredPrefix = getUserIdPrefix(newRole);
            if (!newUserId.toUpperCase().startsWith(requiredPrefix)) {
                JOptionPane.showMessageDialog(dialog, "The UserID for " + newRole + " must start with "
                        + requiredPrefix + ".", "Invalid UserID", JOptionPane.WARNING_MESSAGE);
                return;
            }
            for (User existingUser : fileManager.readUsers()) {
                if (!getUserId(existingUser).equalsIgnoreCase(oldUserId)
                        && getUserId(existingUser).equalsIgnoreCase(newUserId)) {
                    JOptionPane.showMessageDialog(dialog, "That UserID is already in use.",
                            "Duplicate UserID", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            User updatedUser;
            if ("Resident".equals(newRole)) {
                String contactNo = contactField.getText().trim();
                if (!contactNo.matches("\\+?[0-9]+")) {
                    JOptionPane.showMessageDialog(dialog, "Contact number must be numeric.",
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
                ((Staff) updatedUser).setAssignedType(getAssignedTypeForRole(newRole));
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
            logAdminAction("Updated user " + oldUserId + " -> " + newUserId + " (" + newRole + ")");
            refreshUserList();
            JOptionPane.showMessageDialog(dialog, "User updated as " + newRole + ".");
            dialog.dispose();
        });
        deleteBtn.addActionListener(e -> {
            deleteUser(u);
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setSize(560, 480);
        dialog.setMinimumSize(new Dimension(520, 420));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void dialogPackSafe() {
        // no-op helper for rebuild; dialog size is fixed
    }

    private String getAssignedTypeForRole(String role) {
        if ("MedicalStaff".equals(role)) return "Medical";
        if ("FoodStaff".equals(role)) return "Food";
        if ("TransportationStaff".equals(role)) return "Transportation";
        return "";
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
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete user " + u.getFullName() + "? This action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        String userId = getUserId(u);
        admin.deleteUser(userId);
        logAdminAction("Deleted user " + userId + " (" + u.getFullName() + ")");
        refreshUserList();
    }

    private void viewSelectedUserHistory() {
        User u = getSelectedUser();
        if (u == null) return;
        showHistoryDialog(u);
    }

    /** Shows the logged-in admin's own recorded actions. */
    private void viewOwnAdminHistory() {
        showHistoryDialog(admin);
    }

    private void showHistoryDialog(User u) {
        JDialog dialog = new JDialog(this, "History - " + u.getFullName(), true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK_BLUE);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        boolean resident = u instanceof Resident;
        JLabel title = new JLabel(resident ? "Request History" : u.getFullName());
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        JLabel nameLine = new JLabel(u.getFullName());
        nameLine.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLine.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel(u.getRole() + "  \u2022  ID: " + getUserId(u));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(180, 200, 230));
        JPanel titleCol = new JPanel();
        titleCol.setLayout(new BoxLayout(titleCol, BoxLayout.Y_AXIS));
        titleCol.setOpaque(false);
        titleCol.add(title);
        titleCol.add(Box.createVerticalStrut(2));
        if (resident) {
            titleCol.add(nameLine);
            titleCol.add(Box.createVerticalStrut(2));
        }
        titleCol.add(subtitle);
        header.add(titleCol, BorderLayout.WEST);
        dialog.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));

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
            styleHistoryTable(historyTable);
            JScrollPane scroll = new JScrollPane(historyTable);
            scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230), 1));
            body.add(scroll, BorderLayout.CENTER);
            if (model.getRowCount() == 0) {
                JLabel empty = new JLabel("No request history found for this staff member.", SwingConstants.CENTER);
                empty.setFont(new Font("SansSerif", Font.PLAIN, 13));
                empty.setForeground(new Color(148, 163, 184));
                body.removeAll();
                body.add(empty, BorderLayout.CENTER);
            }
        } else if (u instanceof Resident) {
            body.add(buildResidentRequestHistory((Resident) u), BorderLayout.CENTER);
        } else if (u instanceof Admin) {
            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Date and Time", "Admin Action"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (String[] entry : readAdminHistory()) {
                if (entry.length >= 3 && entry[2].equals(getUserId(u))) {
                    model.addRow(new Object[]{entry[0], entry[1]});
                }
            }
            JTable historyTable = new JTable(model);
            styleHistoryTable(historyTable);
            JScrollPane scroll = new JScrollPane(historyTable);
            scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230), 1));
            body.add(scroll, BorderLayout.CENTER);
            if (model.getRowCount() == 0) {
                JLabel empty = new JLabel("No admin activity history found.", SwingConstants.CENTER);
                empty.setFont(new Font("SansSerif", Font.PLAIN, 13));
                empty.setForeground(DARK_BLUE);
                body.removeAll();
                body.add(empty, BorderLayout.CENTER);
            }
        }

        dialog.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)));
        JButton closeBtn = createDarkBlueButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        footer.add(closeBtn);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setSize(720, 440);
        dialog.setMinimumSize(new Dimension(600, 360));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** Resident request history: all requests on the left, selected request details and timeline on the right. */
    private JComponent buildResidentRequestHistory(Resident resident) {
        String residentId = resident.getResidentId();
        List<AssistanceRequest> myRequests = new ArrayList<>();
        for (AssistanceRequest req : requestManager.getAllRequests()) {
            if (req.getResidentId().equals(residentId)) myRequests.add(req);
        }
        List<ResidentRequestHistory> allUpdates = fileManager.readResidentRequestHistory();

        // Left sidebar
        DefaultListModel<String> reqModel = new DefaultListModel<>();
        for (AssistanceRequest req : myRequests) {
            reqModel.addElement(req.getRequestId() + " \u2014 " + req.getType() + " \u00B7 " + req.getStatus());
        }
        JList<String> reqList = new JList<>(reqModel);
        styleList(reqList);

        JLabel sidebarHeading = new JLabel("ALL REQUESTS (" + myRequests.size() + ")");
        sidebarHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        sidebarHeading.setForeground(new Color(100, 116, 139));
        sidebarHeading.setBorder(BorderFactory.createEmptyBorder(12, 12, 10, 12));
        sidebarHeading.setOpaque(true);
        sidebarHeading.setBackground(new Color(248, 250, 252));

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.add(sidebarHeading, BorderLayout.NORTH);
        if (myRequests.isEmpty()) {
            sidebar.add(makeEmptyState("You have no requests yet."), BorderLayout.CENTER);
        } else {
            JScrollPane listScroll = new JScrollPane(reqList);
            listScroll.setBorder(null);
            sidebar.add(listScroll, BorderLayout.CENTER);
        }

        // Right panel
        JPanel details = new JPanel(new BorderLayout());
        details.setBackground(Color.WHITE);
        if (myRequests.isEmpty()) {
            details.add(makeEmptyState("Select a request to view its details."), BorderLayout.CENTER);
        } else {
            reqList.addListSelectionListener(e -> {
                if (e.getValueIsAdjusting()) return;
                int idx = reqList.getSelectedIndex();
                if (idx >= 0 && idx < myRequests.size()) {
                    showResidentRequestDetail(details, myRequests.get(idx), allUpdates, residentId);
                }
            });
            reqList.setSelectedIndex(0);
        }

        return makeSplit(sidebar, details, 280);
    }

    /** Renders the selected request's heading, details and its own activity timeline. */
    private void showResidentRequestDetail(JPanel container, AssistanceRequest req,
                                           List<ResidentRequestHistory> allUpdates, String residentId) {
        container.removeAll();

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        JLabel idLbl = new JLabel(req.getRequestId());
        idLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        idLbl.setForeground(DARK_BLUE);
        idLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel typeLbl = new JLabel("Type: " + req.getType());
        typeLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        typeLbl.setForeground(new Color(71, 85, 105));
        typeLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel statusLbl = new JLabel("Status: " + req.getStatus());
        statusLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusLbl.setForeground(new Color(71, 85, 105));
        statusLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(idLbl);
        top.add(Box.createVerticalStrut(6));
        top.add(typeLbl);
        top.add(Box.createVerticalStrut(2));
        top.add(statusLbl);
        top.add(Box.createVerticalStrut(16));
        top.add(makeDivider());
        top.add(Box.createVerticalStrut(12));

        JLabel timelineTitle = new JLabel("Activity Timeline");
        timelineTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        timelineTitle.setForeground(DARK_BLUE);
        timelineTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(timelineTitle);

        // Only this resident's events for this request, oldest first.
        List<ResidentRequestHistory> events = new ArrayList<>();
        for (ResidentRequestHistory entry : allUpdates) {
            if (entry.getResidentId().equals(residentId) && entry.getRequestId().equals(req.getRequestId())) {
                events.add(entry);
            }
        }
        events.sort(java.util.Comparator.comparing(ResidentRequestHistory::getUpdatedAt));

        JPanel timeline = new JPanel();
        timeline.setLayout(new BoxLayout(timeline, BoxLayout.Y_AXIS));
        timeline.setBackground(Color.WHITE);
        timeline.setBorder(BorderFactory.createEmptyBorder(12, 24, 16, 24));
        if (events.isEmpty()) {
            JLabel none = new JLabel("No updates recorded for this request.");
            none.setFont(new Font("SansSerif", Font.PLAIN, 13));
            none.setForeground(new Color(148, 163, 184));
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            timeline.add(none);
        } else {
            for (int i = 0; i < events.size(); i++) {
                timeline.add(buildTimelineRow(events.get(i), i == events.size() - 1));
            }
        }
        timeline.add(Box.createVerticalGlue());

        JScrollPane timelineScroll = new JScrollPane(timeline);
        timelineScroll.setBorder(null);
        timelineScroll.getVerticalScrollBar().setUnitIncrement(16);

        container.setLayout(new BorderLayout());
        container.add(top, BorderLayout.NORTH);
        container.add(timelineScroll, BorderLayout.CENTER);
        container.revalidate();
        container.repaint();
    }

    private JPanel buildTimelineRow(ResidentRequestHistory entry, boolean last) {
        return buildTimelineRow(entry.getAction(), entry.getUpdatedAt(), null, last);
    }

    /** One timeline entry: a dark blue marker, the action, the recorded time, and optional detail text. */
    private JPanel buildTimelineRow(String action, java.time.LocalDateTime at, String detail, boolean last) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(new TimelineMarker(!last), BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.setBorder(BorderFactory.createEmptyBorder(0, 0, last ? 0 : 18, 0));
        JLabel actionLbl = new JLabel(action);
        actionLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        actionLbl.setForeground(new Color(15, 23, 42));
        actionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel whenLbl = new JLabel(at.format(AssistanceRequest.DT_FMT));
        whenLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        whenLbl.setForeground(new Color(100, 116, 139));
        whenLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(actionLbl);
        text.add(Box.createVerticalStrut(3));
        text.add(whenLbl);
        if (detail != null && !detail.isEmpty()) {
            JLabel detailLbl = new JLabel(detail);
            detailLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            detailLbl.setForeground(new Color(100, 116, 139));
            detailLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            text.add(Box.createVerticalStrut(2));
            text.add(detailLbl);
        }
        row.add(text, BorderLayout.CENTER);

        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private JLabel makeEmptyState(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(new Color(148, 163, 184));
        return l;
    }

    /** Dot marker with a connecting line down to the next timeline entry. */
    private static class TimelineMarker extends JComponent {
        private static final long serialVersionUID = 1L;
        private final boolean drawLine;

        TimelineMarker(boolean drawLine) {
            this.drawLine = drawLine;
            setOpaque(false);
            setPreferredSize(new Dimension(16, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

    private Path adminHistoryPath() {
        // Single persistent data folder at project root (see FileManager.resolveDataDir()).
        return Paths.get(data.FileManager.resolveDataDir(), "admin_history.txt");
    }

    private void logAdminAction(String action) {
        try {
            Path path = adminHistoryPath();
            Files.createDirectories(path.getParent());
            String safeAction = action.replace("|", "/").replace("\r", " ").replace("\n", " ");
            String line = java.time.LocalDateTime.now().format(AssistanceRequest.DT_FMT)
                    + "|" + safeAction + "|" + getUserId(admin) + System.lineSeparator();
            Files.write(path, line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save admin history: " + ex.getMessage(),
                    "History Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private List<String[]> readAdminHistory() {
        List<String[]> entries = new ArrayList<>();
        Path path = adminHistoryPath();
        if (!Files.exists(path)) return entries;
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String[] parts = line.split("\\|", 3);
                if (parts.length == 3) entries.add(parts);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not read admin history: " + ex.getMessage(),
                    "History Error", JOptionPane.WARNING_MESSAGE);
        }
        return entries;
    }

    private void styleHistoryTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(DARK_BLUE);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(DARK_BLUE);
        table.setGridColor(new Color(226, 232, 240));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
    }
}
