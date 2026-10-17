package ui;

import data.FileManager;
import data.RequestManager;
import model.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ResidentMenuFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final Resident resident;
    private final FileManager fileManager = new FileManager();
    private final RequestManager requestManager = new RequestManager(fileManager);
    private static final DateTimeFormatter WHEN_NEEDED_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Color DARK_BLUE = new Color(15, 40, 80);
    private static final Color DARK_BLUE_BTN = new Color(25, 70, 140);
    private static final Color GRAY_DISABLED = new Color(148, 163, 184);

    private final DefaultListModel<String> requestListModel = new DefaultListModel<>();
    private final JList<String> requestList = new JList<>(requestListModel);
    private final JPanel detailsPanel = new JPanel();
    private final JLabel detailsPlaceholder = new JLabel("Select a request to view details", SwingConstants.CENTER);
    private final JTextField requestSearchField = new JTextField(14);
    private List<AssistanceRequest> currentRequests = new ArrayList<>();

    private final JButton updateBtn = new JButton("Update Request");

    public ResidentMenuFrame(Resident resident) {
        super("Resident Menu - " + resident.getFullName());
        this.resident = resident;
        buildUI();
        refreshList();
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 660);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 230)));

        JMenu residentMenu = new JMenu("Resident Menu");
        residentMenu.setFont(residentMenu.getFont().deriveFont(Font.BOLD, 12f));

        JMenuItem submitMenuItem = new JMenuItem("+ Submit Assistance Request");
        JMenuItem updateMenuItem = new JMenuItem("Update Selected Request");
        JMenuItem cancelMenuItem = new JMenuItem("Cancel Selected Request");
        JMenuItem logoutMenuItem = new JMenuItem("Logout");

        residentMenu.add(submitMenuItem);
        residentMenu.add(updateMenuItem);
        residentMenu.add(cancelMenuItem);
        residentMenu.addSeparator();
        residentMenu.add(logoutMenuItem);
        menuBar.add(residentMenu);
        setJMenuBar(menuBar);

        // Header - dark blue like login
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DARK_BLUE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 28, 16, 28));

        JPanel nameAndMeta = new JPanel();
        nameAndMeta.setLayout(new BoxLayout(nameAndMeta, BoxLayout.Y_AXIS));
        nameAndMeta.setOpaque(false);

        JLabel nameLbl = new JLabel(resident.getFullName());
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 18f));
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel metaLbl = new JLabel("ID: " + resident.getResidentId()
                + "  \u2022  " + resident.getAddress()
                + "  \u2022  " + resident.getContactNo());
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

        // Toolbar: only Submit Request + Search
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(new Color(245, 247, 250));
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 20, 8, 20));

        JPanel actionsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionsLeft.setOpaque(false);
        JButton submitBtn = createDarkBlueButton("+ Submit Request");
        actionsLeft.add(submitBtn);

        JPanel searchRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchRight.setOpaque(false);
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(searchLbl.getFont().deriveFont(Font.BOLD, 12f));
        searchLbl.setForeground(DARK_BLUE);
        requestSearchField.setPreferredSize(new Dimension(150, 30));
        requestSearchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        JButton searchBtn = createDarkBlueButton("Search");
        JButton clearSearchBtn = createDarkBlueButton("Clear");

        searchRight.add(searchLbl);
        searchRight.add(requestSearchField);
        searchRight.add(searchBtn);
        searchRight.add(clearSearchBtn);

        toolbar.add(actionsLeft, BorderLayout.WEST);
        toolbar.add(searchRight, BorderLayout.EAST);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(280);
        splitPane.setDividerSize(4);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);

        // LEFT: request list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(203, 213, 225)));

        JLabel listTitle = new JLabel("  My Requests");
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
        listScroll.getViewport().setBackground(Color.WHITE);

        leftPanel.add(listTitle, BorderLayout.NORTH);
        leftPanel.add(listScroll, BorderLayout.CENTER);

        // RIGHT: details + bottom action buttons
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
        detailsScroll.getViewport().setBackground(Color.WHITE);

        // Bottom right: Update only (Cancel is inside Update dialog)
        styleActionButton(updateBtn, false);

        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomActions.setBackground(Color.WHITE);
        bottomActions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)));
        bottomActions.add(updateBtn);

        rightPanel.add(detailsTitle, BorderLayout.NORTH);
        rightPanel.add(detailsScroll, BorderLayout.CENTER);
        rightPanel.add(bottomActions, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        JLabel footerNote = new JLabel("  Note: Requests can be modified or cancelled only while their status is 'Pending'.");
        footerNote.setFont(footerNote.getFont().deriveFont(Font.PLAIN, 11f));
        footerNote.setForeground(new Color(100, 116, 139));
        footerNote.setBorder(BorderFactory.createEmptyBorder(8, 16, 10, 16));

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(245, 247, 250));
        mainContent.add(toolbar, BorderLayout.NORTH);
        mainContent.add(splitPane, BorderLayout.CENTER);
        mainContent.add(footerNote, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);

        requestList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedDetails();
                updateActionButtonsState();
            }
        });

        submitBtn.addActionListener(e -> submitRequestDialog());
        submitMenuItem.addActionListener(e -> submitRequestDialog());

        updateBtn.addActionListener(e -> updateSelectedDialog());
        updateMenuItem.addActionListener(e -> updateSelectedDialog());

        cancelMenuItem.addActionListener(e -> cancelSelectedRequest());

        searchBtn.addActionListener(e -> refreshList(requestSearchField.getText().trim()));
        requestSearchField.addActionListener(e -> refreshList(requestSearchField.getText().trim()));
        clearSearchBtn.addActionListener(e -> {
            requestSearchField.setText("");
            refreshList("");
        });

        logoutBtn.addActionListener(e -> {
            resident.logout();
            dispose();
            new LoginFrame(fileManager).setVisible(true);
        });
        logoutMenuItem.addActionListener(e -> {
            resident.logout();
            dispose();
            new LoginFrame(fileManager).setVisible(true);
        });
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
        AssistanceRequest req = null;
        int idx = requestList.getSelectedIndex();
        if (idx >= 0 && idx < currentRequests.size()) {
            req = currentRequests.get(idx);
        }
        boolean pending = req != null && "Pending".equalsIgnoreCase(req.getStatus());
        styleActionButton(updateBtn, pending);
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

        // Header: Request ID + status badge
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

        // Two-column grid: Type | Date Needed, Location | Date Submitted
        JPanel grid = new JPanel(new GridLayout(2, 2, 32, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        grid.add(makeFieldBlock("Type", req.getType()));
        grid.add(makeFieldBlock("Date Needed", formatDisplayDate(req.getTimeNeeded())));
        String locationVal = req.getLocation();
        if (req instanceof TransportationRequest) {
            TransportationRequest tr = (TransportationRequest) req;
            locationVal = tr.getPickupLocation() + " \u2192 " + tr.getDestination();
        }
        grid.add(makeFieldBlock(
                req instanceof TransportationRequest ? "Route" : "Location",
                locationVal == null || locationVal.isEmpty() ? "-" : locationVal));
        grid.add(makeFieldBlock("Date Submitted", formatDisplayDate(req.getDateRequested())));
        card.add(grid);
        card.add(Box.createVerticalStrut(16));

        // Divider
        card.add(makeDivider());
        card.add(Box.createVerticalStrut(12));

        // Requested items / transport details
        if (req instanceof MedicalRequest) {
            MedicalRequest medical = (MedicalRequest) req;
            card.add(makeSectionTitle("Requested Items"));
            card.add(Box.createVerticalStrut(8));
            for (MedicalItem item : medical.getMedicalItems()) {
                card.add(makeItemRow(item.getName(), item.getQuantity() + " " + item.getUnit()));
                card.add(Box.createVerticalStrut(6));
            }
        } else if (req instanceof FoodRequest) {
            FoodRequest food = (FoodRequest) req;
            card.add(makeSectionTitle("Requested Items"));
            card.add(Box.createVerticalStrut(8));
            for (FoodItem item : food.getFoodItems()) {
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

        // Reason / additional details
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

    private void refreshList() {
        refreshList(requestSearchField.getText().trim());
    }

    private void refreshList(String searchTerm) {
        requestListModel.clear();
        currentRequests.clear();
        String query = searchTerm == null ? "" : searchTerm.toLowerCase();
        List<AssistanceRequest> all = requestManager.getAllRequests();

        for (AssistanceRequest r : all) {
            if (!r.getResidentId().equals(resident.getResidentId())) continue;
            if (!query.isEmpty()
                    && !r.getRequestId().toLowerCase().contains(query)
                    && !r.getStatus().toLowerCase().contains(query)
                    && !r.getType().toLowerCase().contains(query)) continue;
            currentRequests.add(r);
            requestListModel.addElement(r.getRequestId() + "  (" + r.getStatus() + ")");
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

    private void cancelSelectedRequest() {
        AssistanceRequest req = getSelectedRequest();
        if (req == null) return;
        if (!"Pending".equalsIgnoreCase(req.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only pending requests can be cancelled.",
                    "Cancel Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cancelRequest(req);
    }

    private void updateSelectedDialog() {
        AssistanceRequest req = getSelectedRequest();
        if (req == null) return;
        resident.updateRequest(req.getRequestId());
        if (!"Pending".equalsIgnoreCase(req.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only pending requests can be updated.",
                    "Update Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        RequestFormFields fields = new RequestFormFields();
        fields.setFromRequest(req);
        if (req instanceof MedicalRequest) {
            fields.setMedicalItems(((MedicalRequest) req).getMedicalItems());
        } else if (req instanceof FoodRequest) {
            fields.setFoodItems(((FoodRequest) req).getFoodItems());
        } else if (req instanceof TransportationRequest) {
            TransportationRequest transportation = (TransportationRequest) req;
            fields.pickupField.setText(transportation.getPickupLocation());
            fields.destinationField.setText(transportation.getDestination());
            fields.passengerCountField.setText(String.valueOf(transportation.getPassengerCount()));
        }

        JPanel panel = new JPanel();
        rebuildRequestForm(panel, req.getType(), fields, new JLabel(toAssistanceLabel(req.getType())));
        JScrollPane formScroll = new JScrollPane(panel);
        formScroll.setPreferredSize(new Dimension(560, 420));
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        JDialog dialog = new JDialog(this, "Update Request - " + req.getRequestId(), true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.add(formScroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)),
                BorderFactory.createEmptyBorder(4, 16, 4, 8)));
        JButton saveBtn = createDarkBlueButton("Save Changes");
        JButton cancelReqBtn = createDarkBlueButton("Cancel Request");
        JButton closeBtn = createDarkBlueButton("Close");
        buttons.add(saveBtn);
        buttons.add(cancelReqBtn);
        buttons.add(closeBtn);
        dialog.add(buttons, BorderLayout.SOUTH);

        final boolean[] saved = {false};
        saveBtn.addActionListener(e -> {
            if (!hasRequiredFields(req.getType(), fields)) return;
            req.setLocation(fields.locationField.getText().trim());
            req.setTimeNeeded(parseWhenNeeded(fields.whenNeededField.getText().trim()));
            req.setAdditionalDetails(fields.additionalDetailsArea.getText().trim());
            if (req instanceof MedicalRequest) {
                ((MedicalRequest) req).setMedicalItems(fields.readMedicalItems());
            } else if (req instanceof FoodRequest) {
                ((FoodRequest) req).setFoodItems(fields.readFoodItems());
            } else if (req instanceof TransportationRequest) {
                TransportationRequest transportation = (TransportationRequest) req;
                transportation.setPickupLocation(fields.pickupField.getText().trim());
                transportation.setDestination(fields.destinationField.getText().trim());
                transportation.setPassengerCount(Integer.parseInt(fields.passengerCountField.getText().trim()));
            }
            requestManager.updateRequest(req);
            fileManager.recordResidentRequestAction(resident, req, "Updated");
            JOptionPane.showMessageDialog(dialog, "Request updated successfully.");
            saved[0] = true;
            dialog.dispose();
            refreshList();
        });
        cancelReqBtn.addActionListener(e -> {
            resident.cancelRequest();
            cancelRequest(req);
            dialog.dispose();
        });
        closeBtn.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setMinimumSize(new Dimension(600, 520));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void submitRequestDialog() {
        String[] types = {"Food", "Medical", "Transportation"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        RequestFormFields fields = new RequestFormFields();
        JPanel panel = new JPanel();
        Runnable rebuildForm = () -> rebuildRequestForm(panel,
                (String) typeBox.getSelectedItem(), fields, typeBox);
        typeBox.addActionListener(e -> rebuildForm.run());
        rebuildForm.run();

        JScrollPane formScroll = new JScrollPane(panel);
        formScroll.setPreferredSize(new Dimension(580, 460));
        formScroll.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

        JDialog dialog = new JDialog(this, "Submit Assistance Request", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.add(formScroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(203, 213, 225)));
        JButton submitButton = createDarkBlueButton("Submit Request");
        JButton cancelButton = createDarkBlueButton("Cancel");
        buttons.add(cancelButton);
        buttons.add(submitButton);
        dialog.add(buttons, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> {
            String type = toRequestType((String) typeBox.getSelectedItem());
            if (!hasRequiredFields(type, fields)) return;
            AssistanceRequest newRequest = createRequest(type, requestManager.nextRequestId(), fields,
                    "Pending", LocalDateTime.now());
            try {
                requestManager.addRequest(newRequest);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog, "Could not save the request: " + ex.getMessage(),
                        "Save Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(dialog, "Request submitted successfully!\n"
                    + "Your Reference No. is: " + newRequest.getRequestId() + "\n"
                    + "Please keep this number for tracking your request.",
                    "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
            fields.reset();
            typeBox.setSelectedIndex(0);
            rebuildForm.run();
            refreshList();
            dialog.dispose();
        });
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.pack();
        dialog.setMinimumSize(new Dimension(610, 540));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void rebuildRequestForm(JPanel panel, String selectedType, RequestFormFields fields,
                                    Component typeInput) {
        String type = toRequestType(selectedType);
        panel.removeAll();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        // Show type picker only when submitting (combo box). On update, only the heading.
        if (!(typeInput instanceof JLabel)) {
            addFormRow(panel, "Assistance Type", typeInput);
            panel.add(Box.createVerticalStrut(16));
        }

        // Section heading
        JLabel heading = new JLabel(toAssistanceLabel(type).toUpperCase());
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

        if ("Food".equals(type) || "Medical".equals(type)) {
            String itemLabel = "Food".equals(type) ? "Food Item" : "Medical Item";
            String[] units = "Food".equals(type)
                    ? new String[]{"kg", "pcs", "pack", "g", "liter", "ml"}
                    : new String[]{"pcs", "box", "bottle", "pack", "tablet", "ml"};
            if (fields.itemRows.isEmpty()) {
                fields.itemRows.add(new ItemFormRow(units));
            }
            for (ItemFormRow row : fields.itemRows) row.setUnits(units);

            // Item header + add/remove
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
            JButton addItemButton = createDarkBlueButton("+");
            JButton removeItemButton = createDarkBlueButton("-");
            removeItemButton.setEnabled(fields.itemRows.size() > 1);
            itemBtns.add(addItemButton);
            itemBtns.add(removeItemButton);
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
            for (ItemFormRow row : fields.itemRows) {
                JPanel itemRow = new JPanel(new GridLayout(1, 3, 12, 0));
                itemRow.setOpaque(false);
                itemRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                styleField(row.nameField);
                styleField(row.quantityField);
                itemRow.add(row.nameField);
                itemRow.add(row.quantityField);
                itemRow.add(row.unitBox);
                panel.add(itemRow);
                panel.add(Box.createVerticalStrut(6));
            }

            addItemButton.addActionListener(e -> {
                fields.itemRows.add(new ItemFormRow(units));
                rebuildRequestForm(panel, selectedType, fields, typeInput);
            });
            removeItemButton.addActionListener(e -> {
                if (fields.itemRows.size() > 1) {
                    fields.itemRows.remove(fields.itemRows.size() - 1);
                    rebuildRequestForm(panel, selectedType, fields, typeInput);
                }
            });

            panel.add(Box.createVerticalStrut(10));
            styleField(fields.locationField);
            addFormRow(panel, "Location / Address", fields.locationField);
        } else {
            styleField(fields.pickupField);
            styleField(fields.destinationField);
            styleField(fields.passengerCountField);
            addFormRow(panel, "Pickup Location", fields.pickupField);
            addFormRow(panel, "Destination", fields.destinationField);
            addFormRow(panel, "No. of Passengers", fields.passengerCountField);
        }

        panel.add(Box.createVerticalStrut(6));
        styleField(fields.whenNeededField);
        addFormRow(panel, "Time Needed", fields.whenNeededField);
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
        addFormRow(panel, "Additional Details", detailsScroll);

        panel.revalidate();
        panel.repaint();
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    private void addFormRow(JPanel panel, String label, Component input) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        int h = Math.max(36, input.getPreferredSize().height + 4);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, h + 8));
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("SansSerif", Font.BOLD, 12));
        labelComponent.setForeground(DARK_BLUE);
        labelComponent.setPreferredSize(new Dimension(150, 28));
        labelComponent.setVerticalAlignment(SwingConstants.TOP);

        row.add(labelComponent, BorderLayout.WEST);
        row.add(input, BorderLayout.CENTER);
        panel.add(row);
    }

    private boolean hasRequiredFields(String type, RequestFormFields fields) {
        if (!"Transportation".equals(type) && fields.locationField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the assistance location.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (fields.whenNeededField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter when the assistance is needed.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            parseWhenNeeded(fields.whenNeededField.getText().trim());
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Enter needed date/time as yyyy-MM-dd HH:mm.",
                    "Invalid Date/Time", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if ("Transportation".equals(type)) {
            if (fields.pickupField.getText().trim().isEmpty()
                    || fields.destinationField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both pickup location and destination.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return validatePositiveInteger(fields.passengerCountField, "number of passengers");
        }
        return validateItemRows(fields);
    }

    private boolean validateItemRows(RequestFormFields fields) {
        boolean hasItem = false;
        for (ItemFormRow row : fields.itemRows) {
            String name = row.nameField.getText().trim();
            String quantity = row.quantityField.getText().trim();
            if (name.isEmpty() && quantity.isEmpty()) continue;
            if (name.isEmpty() || quantity.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete each item row or leave it entirely blank.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (!isPositiveInteger(quantity)) {
                JOptionPane.showMessageDialog(this, "Item quantities must be whole numbers greater than zero.",
                        "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            hasItem = true;
        }
        if (!hasItem) {
            JOptionPane.showMessageDialog(this, "Please add at least one requested item.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
        }
        return hasItem;
    }

    private boolean validatePositiveInteger(JTextField field, String label) {
        if (!isPositiveInteger(field.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Enter a whole number greater than zero for " + label + ".",
                    "Invalid Number", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean isPositiveInteger(String value) {
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private AssistanceRequest createRequest(String type, String requestId, RequestFormFields fields,
                                             String status, LocalDateTime dateRequested) {
        LocalDateTime timeNeeded = parseWhenNeeded(fields.whenNeededField.getText().trim());
        String additionalDetails = fields.additionalDetailsArea.getText().trim();
        if ("Food".equals(type)) {
            return new FoodRequest(requestId, resident.getResidentId(), fields.locationField.getText().trim(),
                    timeNeeded, additionalDetails, status, dateRequested, fields.readFoodItems());
        }
        if ("Medical".equals(type)) {
            return new MedicalRequest(requestId, resident.getResidentId(), fields.locationField.getText().trim(),
                    timeNeeded, additionalDetails, status, dateRequested, fields.readMedicalItems());
        }
        return new TransportationRequest(requestId, resident.getResidentId(), fields.pickupField.getText().trim(),
                fields.destinationField.getText().trim(),
                Integer.parseInt(fields.passengerCountField.getText().trim()), timeNeeded,
                additionalDetails, status, dateRequested);
    }

    private String toRequestType(String label) {
        if (label == null) return "Food";
        if (label.startsWith("Food")) return "Food";
        if (label.startsWith("Medical")) return "Medical";
        return "Transportation";
    }

    private String toAssistanceLabel(String type) {
        return type + " Assistance";
    }

    private LocalDateTime parseWhenNeeded(String value) {
        return LocalDateTime.parse(value, WHEN_NEEDED_FORMAT);
    }

    private static class RequestFormFields {
        private final List<ItemFormRow> itemRows = new ArrayList<>();
        private final JTextField locationField = new JTextField();
        private final JTextField pickupField = new JTextField();
        private final JTextField destinationField = new JTextField();
        private final JTextField passengerCountField = new JTextField("1");
        private final JTextField whenNeededField = new JTextField(
                LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).format(WHEN_NEEDED_FORMAT));
        private final JTextArea additionalDetailsArea = new JTextArea(3, 24);

        private RequestFormFields() {
            reset();
        }

        private void reset() {
            locationField.setText("");
            pickupField.setText("");
            destinationField.setText("");
            passengerCountField.setText("1");
            whenNeededField.setText(LocalDateTime.now().plusDays(1)
                    .withHour(9).withMinute(0).format(WHEN_NEEDED_FORMAT));
            additionalDetailsArea.setText("");
            itemRows.clear();
            itemRows.add(new ItemFormRow(new String[]{"kg", "pcs", "pack", "g", "liter", "ml"}));
        }

        private void setFromRequest(AssistanceRequest request) {
            locationField.setText(request.getLocation());
            whenNeededField.setText(request.getTimeNeeded().format(WHEN_NEEDED_FORMAT));
            additionalDetailsArea.setText(request.getAdditionalDetails());
        }

        private void setFoodItems(List<FoodItem> items) {
            itemRows.clear();
            for (FoodItem item : items) {
                ItemFormRow row = new ItemFormRow(new String[]{"kg", "pcs", "pack", "g", "liter", "ml"});
                row.nameField.setText(item.getName());
                row.quantityField.setText(String.valueOf(item.getQuantity()));
                row.setSelectedUnit(item.getUnit());
                itemRows.add(row);
            }
            if (itemRows.isEmpty()) itemRows.add(new ItemFormRow(new String[]{"kg", "pcs", "pack"}));
        }

        private void setMedicalItems(List<MedicalItem> items) {
            itemRows.clear();
            for (MedicalItem item : items) {
                ItemFormRow row = new ItemFormRow(new String[]{"pcs", "box", "bottle", "pack", "tablet", "ml"});
                row.nameField.setText(item.getName());
                row.quantityField.setText(String.valueOf(item.getQuantity()));
                row.setSelectedUnit(item.getUnit());
                itemRows.add(row);
            }
            if (itemRows.isEmpty()) itemRows.add(new ItemFormRow(new String[]{"pcs", "box", "bottle"}));
        }

        private List<FoodItem> readFoodItems() {
            List<FoodItem> items = new ArrayList<>();
            for (ItemFormRow row : itemRows) {
                String name = row.nameField.getText().trim();
                if (!name.isEmpty()) {
                    items.add(new FoodItem(name, Integer.parseInt(row.quantityField.getText().trim()),
                            (String) row.unitBox.getSelectedItem()));
                }
            }
            return items;
        }

        private List<MedicalItem> readMedicalItems() {
            List<MedicalItem> items = new ArrayList<>();
            for (ItemFormRow row : itemRows) {
                String name = row.nameField.getText().trim();
                if (!name.isEmpty()) {
                    items.add(new MedicalItem(name, Integer.parseInt(row.quantityField.getText().trim()),
                            (String) row.unitBox.getSelectedItem()));
                }
            }
            return items;
        }
    }

    private static class ItemFormRow {
        private final JTextField nameField = new JTextField();
        private final JTextField quantityField = new JTextField();
        private final JComboBox<String> unitBox;

        private ItemFormRow(String[] units) {
            unitBox = new JComboBox<>(units);
            nameField.setPreferredSize(new Dimension(140, 32));
            quantityField.setPreferredSize(new Dimension(60, 32));
            unitBox.setPreferredSize(new Dimension(90, 32));
        }

        private void setUnits(String[] units) {
            String selected = (String) unitBox.getSelectedItem();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(units);
            boolean containsSelected = false;
            for (String unit : units) {
                if (unit.equals(selected)) {
                    containsSelected = true;
                    break;
                }
            }
            if (selected != null && !containsSelected) model.addElement(selected);
            unitBox.setModel(model);
            if (selected != null) unitBox.setSelectedItem(selected);
        }

        private void setSelectedUnit(String unit) {
            unitBox.setSelectedItem(unit);
            if (!unit.equals(unitBox.getSelectedItem())) {
                unitBox.addItem(unit);
                unitBox.setSelectedItem(unit);
            }
        }
    }

    private void cancelRequest(AssistanceRequest req) {
        int confirm = JOptionPane.showConfirmDialog(this, "Cancel request " + req.getRequestId() + "?",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            req.updateStatus("Cancelled");
            requestManager.updateRequest(req);
            fileManager.recordResidentRequestAction(resident, req, "Cancelled");
            JOptionPane.showMessageDialog(this, "Request marked as cancelled.");
            refreshList();
        }
    }
}
