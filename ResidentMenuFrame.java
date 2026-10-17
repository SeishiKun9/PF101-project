import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ResidentMenuFrame extends JFrame {
    private final Resident resident;
    private final FileManager fileManager = new FileManager();
    private final RequestManager requestManager = new RequestManager(fileManager);
    private static final DateTimeFormatter WHEN_NEEDED_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Request ID", "Type", "Details & Summary", "Status"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable table = new JTable(tableModel);
    private final JTextField requestSearchField = new JTextField(16);

    // KPI Card Labels
    private final JLabel totalCountLabel = new JLabel("0");
    private final JLabel pendingCountLabel = new JLabel("0");
    private final JLabel inProgressCountLabel = new JLabel("0");
    private final JLabel completedCountLabel = new JLabel("0");

    public ResidentMenuFrame(Resident resident) {
        super("Resident Menu - " + resident.getFullName());
        this.resident = resident;
        buildUI();
        refreshTable();
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 660);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(JButtonStyle.BG_PAGE);

        // NATIVE MENU BAR (Resident Menu)
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, JButtonStyle.BORDER_LIGHT));

        JMenu residentMenu = new JMenu("Resident Menu");
        residentMenu.setFont(residentMenu.getFont().deriveFont(Font.BOLD, 12f));

        JMenuItem submitMenuItem = new JMenuItem("+ Submit Assistance Request");
        JMenuItem viewMenuItem = new JMenuItem("View Request Details");
        JMenuItem updateMenuItem = new JMenuItem("Update Selected Request");
        JMenuItem cancelMenuItem = new JMenuItem("Cancel Selected Request");
        JMenuItem refreshMenuItem = new JMenuItem("Refresh My Requests");
        JMenuItem logoutMenuItem = new JMenuItem("Logout");

        residentMenu.add(submitMenuItem);
        residentMenu.add(viewMenuItem);
        residentMenu.add(updateMenuItem);
        residentMenu.add(cancelMenuItem);
        residentMenu.addSeparator();
        residentMenu.add(refreshMenuItem);
        residentMenu.add(logoutMenuItem);
        menuBar.add(residentMenu);
        setJMenuBar(menuBar);

        // TOP NAVIGATION / HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, JButtonStyle.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        userInfoPanel.setOpaque(false);

        // Avatar Initial Circle
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(new Color(191, 219, 254));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(JButtonStyle.PRIMARY_BLUE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 15));
                String initials = getInitials(resident.getFullName());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, (getWidth() - fm.stringWidth(initials)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
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
        JLabel nameLbl = new JLabel(resident.getFullName());
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 17f));
        nameLbl.setForeground(JButtonStyle.TEXT_DARK);

        JLabel rolePill = new JLabel(" RESIDENT MENU ");
        rolePill.setFont(rolePill.getFont().deriveFont(Font.BOLD, 10.5f));
        rolePill.setOpaque(true);
        rolePill.setBackground(new Color(219, 234, 254));
        rolePill.setForeground(new Color(29, 78, 216));
        rolePill.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        titleRoleRow.add(nameLbl);
        titleRoleRow.add(rolePill);

        JLabel metaLbl = new JLabel("Resident ID: " + resident.getResidentId()
                + "  \u2022  Address: " + resident.getAddress()
                + "  \u2022  Contact: " + resident.getContactNo());
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

        // STATS METRIC ROW
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(statsRow.getPreferredSize().width, 82));
        statsRow.add(JButtonStyle.createStatCard("Total Requests", totalCountLabel, new Color(59, 130, 246), "All submissions"));
        statsRow.add(JButtonStyle.createStatCard("Pending", pendingCountLabel, new Color(245, 158, 11), "Awaiting review"));
        statsRow.add(JButtonStyle.createStatCard("In Progress", inProgressCountLabel, new Color(99, 102, 241), "Being processed"));
        statsRow.add(JButtonStyle.createStatCard("Completed", completedCountLabel, new Color(16, 185, 129), "Resolved / Delivered"));

        // TOOLBAR & TABLE PANEL
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        // Resident Menu Action Buttons on Left
        JPanel actionsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionsLeft.setOpaque(false);
        JButton submitBtn = new JButton("+ Submit Request");
        JButton viewBtn = new JButton("View Details");
        JButton updateBtn = new JButton("Update Request");
        JButton cancelBtn = new JButton("Cancel Request");
        JButton refreshBtn = new JButton("Refresh");

        JButtonStyle.applyAccent(submitBtn);
        JButtonStyle.applySecondary(viewBtn);
        JButtonStyle.applySecondary(updateBtn);
        JButtonStyle.applyDanger(cancelBtn);
        JButtonStyle.applySecondary(refreshBtn);

        actionsLeft.add(submitBtn);
        actionsLeft.add(viewBtn);
        actionsLeft.add(updateBtn);
        actionsLeft.add(cancelBtn);
        actionsLeft.add(refreshBtn);

        // Search Bar on Right
        JPanel searchRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchRight.setOpaque(false);
        JLabel searchIcon = new JLabel("Search:");
        searchIcon.setFont(searchIcon.getFont().deriveFont(Font.BOLD, 12f));
        searchIcon.setForeground(JButtonStyle.TEXT_MUTED);
        requestSearchField.setPreferredSize(new Dimension(170, 32));
        requestSearchField.setBorder(JButtonStyle.createFieldBorder());
        JButton searchBtn = new JButton("Search");
        JButton clearSearchBtn = new JButton("Clear");
        JButtonStyle.applySecondary(searchBtn);
        JButtonStyle.applySecondary(clearSearchBtn);
        searchRight.add(searchIcon);
        searchRight.add(requestSearchField);
        searchRight.add(searchBtn);
        searchRight.add(clearSearchBtn);

        toolbar.add(actionsLeft, BorderLayout.WEST);
        toolbar.add(searchRight, BorderLayout.EAST);

        // Styled Table
        JButtonStyle.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(450);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setCellRenderer(new JButtonStyle.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(JButtonStyle.BORDER_LIGHT, 1, true));

        // Footer note
        JLabel footerNote = new JLabel("Note: Requests can be modified or cancelled only while their status is 'Pending'.");
        footerNote.setFont(footerNote.getFont().deriveFont(Font.PLAIN, 11.5f));
        footerNote.setForeground(JButtonStyle.TEXT_MUTED);
        footerNote.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 0));

        centerPanel.add(toolbar, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(footerNote, BorderLayout.SOUTH);

        mainContent.add(statsRow, BorderLayout.NORTH);
        mainContent.add(centerPanel, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        // EVENT LISTENERS (Both Buttons and Menu Items)
        submitBtn.addActionListener(e -> submitRequestDialog());
        submitMenuItem.addActionListener(e -> submitRequestDialog());

        viewBtn.addActionListener(e -> viewSelectedRequestDetails());
        viewMenuItem.addActionListener(e -> viewSelectedRequestDetails());

        updateBtn.addActionListener(e -> updateSelectedDialog());
        updateMenuItem.addActionListener(e -> updateSelectedDialog());

        cancelBtn.addActionListener(e -> cancelSelectedRequest());
        cancelMenuItem.addActionListener(e -> cancelSelectedRequest());

        refreshBtn.addActionListener(e -> { resident.viewMyRequest(); refreshTable(); });
        refreshMenuItem.addActionListener(e -> { resident.viewMyRequest(); refreshTable(); });

        searchBtn.addActionListener(e -> refreshTable(requestSearchField.getText().trim()));
        requestSearchField.addActionListener(e -> refreshTable(requestSearchField.getText().trim()));
        clearSearchBtn.addActionListener(e -> {
            requestSearchField.setText("");
            refreshTable("");
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

    private void viewSelectedRequestDetails() {
        AssistanceRequest req = getSelectedRequest();
        if (req == null) return;
        resident.viewMyRequest();

        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 8, 6));
        addDetailRow(detailsPanel, "Request ID", req.getRequestId());
        addDetailRow(detailsPanel, "Type", req.getType());
        addDetailRow(detailsPanel, "Status", req.getStatus());
        addDetailRow(detailsPanel, "Date Needed", req.getTimeNeeded().format(WHEN_NEEDED_FORMAT));
        addDetailRow(detailsPanel, "Date Submitted", req.getDateRequested().format(WHEN_NEEDED_FORMAT));
        addDetailRow(detailsPanel, "Additional Details", req.getAdditionalDetails().isEmpty() ? "(None)" : req.getAdditionalDetails());

        if (req instanceof MedicalRequest) {
            MedicalRequest medical = (MedicalRequest) req;
            addDetailRow(detailsPanel, "Location", req.getLocation());
            int idx = 1;
            for (MedicalItem item : medical.getMedicalItems()) {
                addDetailRow(detailsPanel, "Medical Item " + idx++, item.getName() + " (" + item.getQuantity() + " " + item.getUnit() + ")");
            }
        } else if (req instanceof FoodRequest) {
            FoodRequest food = (FoodRequest) req;
            addDetailRow(detailsPanel, "Location", req.getLocation());
            int idx = 1;
            for (FoodItem item : food.getFoodItems()) {
                addDetailRow(detailsPanel, "Food Item " + idx++, item.getName() + " (" + item.getQuantity() + " " + item.getUnit() + ")");
            }
        } else if (req instanceof TransportationRequest) {
            TransportationRequest tr = (TransportationRequest) req;
            addDetailRow(detailsPanel, "Pickup", tr.getPickupLocation());
            addDetailRow(detailsPanel, "Destination", tr.getDestination());
            addDetailRow(detailsPanel, "Passengers", String.valueOf(tr.getPassengerCount()));
        }

        JOptionPane.showMessageDialog(this, detailsPanel,
                "Request Details - " + req.getRequestId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label + ":");
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setForeground(new Color(51, 65, 85));
        JLabel v = new JLabel(value == null || value.isEmpty() ? "-" : value);
        v.setFont(v.getFont().deriveFont(Font.PLAIN, 12f));
        panel.add(l);
        panel.add(v);
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private void refreshTable() {
        refreshTable(requestSearchField.getText().trim());
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
            if (!r.getResidentId().equals(resident.getResidentId())) continue;
            total++;
            String status = r.getStatus();
            if ("Pending".equalsIgnoreCase(status)) pending++;
            else if ("Completed".equalsIgnoreCase(status) || "Delivered".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status)) completed++;
            else inProgress++;

            if (!query.isEmpty()
                    && !r.getRequestId().toLowerCase().contains(query)
                    && !r.getStatus().toLowerCase().contains(query)) continue;
            tableModel.addRow(new Object[]{r.getRequestId(), r.getType(), r.toDisplaySummary(), r.getStatus()});
        }

        totalCountLabel.setText(String.valueOf(total));
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
        formScroll.setBorder(BorderFactory.createLineBorder(JButtonStyle.BORDER_LIGHT));

        JDialog dialog = new JDialog(this, "Submit Assistance Request", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.add(formScroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, JButtonStyle.BORDER_LIGHT));
        JButton submitButton = new JButton("Submit Request");
        JButton cancelButton = new JButton("Cancel");
        JButtonStyle.applyAccent(submitButton);
        JButtonStyle.applySecondary(cancelButton);
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
            refreshTable();
            dialog.dispose();
        });
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.pack();
        dialog.setMinimumSize(new Dimension(610, 540));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
            MedicalRequest medical = (MedicalRequest) req;
            fields.setMedicalItems(medical.getMedicalItems());
        } else if (req instanceof FoodRequest) {
            FoodRequest food = (FoodRequest) req;
            fields.setFoodItems(food.getFoodItems());
        } else if (req instanceof TransportationRequest) {
            TransportationRequest transportation = (TransportationRequest) req;
            fields.pickupField.setText(transportation.getPickupLocation());
            fields.destinationField.setText(transportation.getDestination());
            fields.passengerCountField.setText(String.valueOf(transportation.getPassengerCount()));
        }

        JPanel panel = new JPanel();
        rebuildRequestForm(panel, req.getType(), fields, new JLabel(toAssistanceLabel(req.getType())));
        JScrollPane formScroll = new JScrollPane(panel);
        formScroll.setPreferredSize(new Dimension(580, 440));
        formScroll.setBorder(BorderFactory.createLineBorder(JButtonStyle.BORDER_LIGHT));

        Object[] options = {"Save Changes", "Cancel Request", "Close"};
        JOptionPane optionPane = new JOptionPane(formScroll, JOptionPane.PLAIN_MESSAGE,
            JOptionPane.DEFAULT_OPTION, null, options, options[0]);
        JDialog dialog = optionPane.createDialog(this, "Update Request - " + req.getRequestId());
        dialog.setResizable(true);
        dialog.setVisible(true);
        Object choice = optionPane.getValue();
        dialog.dispose();

        if (options[1].equals(choice)) {
            resident.cancelRequest();
            cancelRequest(req);
            return;
        }
        if (!options[0].equals(choice) || !hasRequiredFields(req.getType(), fields)) return;

        req.setLocation(fields.locationField.getText().trim());
        req.setTimeNeeded(parseWhenNeeded(fields.whenNeededField.getText().trim()));
        req.setAdditionalDetails(fields.additionalDetailsArea.getText().trim());
        if (req instanceof MedicalRequest) {
            MedicalRequest medical = (MedicalRequest) req;
            medical.setMedicalItems(fields.readMedicalItems());
        } else if (req instanceof FoodRequest) {
            FoodRequest food = (FoodRequest) req;
            food.setFoodItems(fields.readFoodItems());
        } else if (req instanceof TransportationRequest) {
            TransportationRequest transportation = (TransportationRequest) req;
            transportation.setPickupLocation(fields.pickupField.getText().trim());
            transportation.setDestination(fields.destinationField.getText().trim());
            transportation.setPassengerCount(Integer.parseInt(fields.passengerCountField.getText().trim()));
        }
        requestManager.updateRequest(req);
        fileManager.recordResidentRequestAction(resident, req, "Updated");
        JOptionPane.showMessageDialog(this, "Request updated successfully.");
        refreshTable();
    }

    private void rebuildRequestForm(JPanel panel, String selectedType, RequestFormFields fields,
                                    Component typeInput) {
        String type = toRequestType(selectedType);
        panel.removeAll();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        addFormRow(panel, "Assistance Type:", typeInput);
        JLabel heading = new JLabel(toAssistanceLabel(type).toUpperCase());
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 15f));
        heading.setForeground(JButtonStyle.PRIMARY_BLUE);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.setMaximumSize(new Dimension(Integer.MAX_VALUE, heading.getPreferredSize().height));
        panel.add(Box.createVerticalStrut(10));
        panel.add(heading);
        panel.add(Box.createVerticalStrut(10));

        if ("Food".equals(type) || "Medical".equals(type)) {
            String itemLabel = "Food".equals(type) ? "Food Item" : "Medical Item";
            String[] units = "Food".equals(type)
                    ? new String[]{"kg", "pcs", "pack", "g", "liter", "ml"}
                    : new String[]{"pcs", "box", "bottle", "pack", "tablet", "ml"};
            if (fields.itemRows.isEmpty()) {
                fields.itemRows.add(new ItemFormRow(units));
            }
            for (ItemFormRow row : fields.itemRows) row.setUnits(units);

            JButton addItemButton = new JButton("+");
            addItemButton.setToolTipText("Add another " + itemLabel.toLowerCase());
            JButtonStyle.applySecondary(addItemButton);

            JButton removeItemButton = new JButton("-");
            removeItemButton.setToolTipText("Remove last row");
            JButtonStyle.applySecondary(removeItemButton);
            removeItemButton.setEnabled(fields.itemRows.size() > 1);

            JPanel addItemPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
            addItemPanel.setOpaque(false);
            addItemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            addItemPanel.add(removeItemButton);
            addItemPanel.add(addItemButton);
            addItemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, addItemPanel.getPreferredSize().height));
            panel.add(addItemPanel);

            JPanel itemTable = new JPanel(new GridLayout(0, 3, 10, 8));
            itemTable.setOpaque(false);
            itemTable.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel h1 = new JLabel(itemLabel); h1.setFont(h1.getFont().deriveFont(Font.BOLD, 12f));
            JLabel h2 = new JLabel("Quantity"); h2.setFont(h2.getFont().deriveFont(Font.BOLD, 12f));
            JLabel h3 = new JLabel("Unit"); h3.setFont(h3.getFont().deriveFont(Font.BOLD, 12f));
            itemTable.add(h1); itemTable.add(h2); itemTable.add(h3);

            for (ItemFormRow row : fields.itemRows) {
                row.nameField.setBorder(JButtonStyle.createFieldBorder());
                row.quantityField.setBorder(JButtonStyle.createFieldBorder());
                itemTable.add(row.nameField);
                itemTable.add(row.quantityField);
                itemTable.add(row.unitBox);
            }
            itemTable.setMaximumSize(new Dimension(Integer.MAX_VALUE, itemTable.getPreferredSize().height));
            panel.add(itemTable);

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
        } else {
            fields.pickupField.setBorder(JButtonStyle.createFieldBorder());
            fields.destinationField.setBorder(JButtonStyle.createFieldBorder());
            fields.passengerCountField.setBorder(JButtonStyle.createFieldBorder());
            addFormRow(panel, "Pickup Location:", fields.pickupField);
            addFormRow(panel, "Destination:", fields.destinationField);
            addFormRow(panel, "No. of Passengers:", fields.passengerCountField);
        }
        if (!"Transportation".equals(type)) {
            fields.locationField.setBorder(JButtonStyle.createFieldBorder());
            addFormRow(panel, "Location / Address:", fields.locationField);
        }
        fields.whenNeededField.setBorder(JButtonStyle.createFieldBorder());
        addFormRow(panel, "Time Needed (yyyy-MM-dd HH:mm):", fields.whenNeededField);

        fields.additionalDetailsArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        fields.additionalDetailsArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane detailsScroll = new JScrollPane(fields.additionalDetailsArea);
        detailsScroll.setBorder(JButtonStyle.createFieldBorder());
        addFormRow(panel, "Additional Details:", detailsScroll);

        panel.revalidate();
        panel.repaint();
    }

    private void addFormRow(JPanel panel, String label, Component input) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(40, input.getPreferredSize().height + 6)));
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 12f));
        labelComponent.setForeground(new Color(51, 65, 85));
        labelComponent.setPreferredSize(new Dimension(195, 26));
        row.add(labelComponent, BorderLayout.WEST);
        row.add(input, BorderLayout.CENTER);
        panel.add(Box.createVerticalStrut(6));
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
            boolean empty = name.isEmpty() && quantity.isEmpty();
            if (empty) continue;
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
        String pickup = fields.pickupField.getText().trim();
        return new TransportationRequest(requestId, resident.getResidentId(), pickup,
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
            // Start with only ONE row by default
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
            refreshTable();
        }
    }
}
