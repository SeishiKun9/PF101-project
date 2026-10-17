package ui;

import model.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Shared request-filing form used by the Admin (file on behalf of resident).
 * Mirrors the Resident submit form: item rows and the same validation,
 * but the caller supplies the resident ID.
 */
public final class RequestFormHelper {

    public static final DateTimeFormatter WHEN_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private RequestFormHelper() {
    }

    /** One item row: name + quantity + unit. */
    public static final class ItemRow {
        public final JTextField nameField = new JTextField();
        public final JTextField quantityField = new JTextField();
        public final JComboBox<String> unitBox;

        public ItemRow(String[] units) {
            unitBox = new JComboBox<>(units);
        }
    }

    /** All editable fields of the form. */
    public static final class Fields {
        public final List<ItemRow> itemRows = new ArrayList<>();
        public final JTextField locationField = new JTextField();
        public final JTextField pickupField = new JTextField();
        public final JTextField destinationField = new JTextField();
        public final JTextField passengerCountField = new JTextField("1");
        public final JTextField whenNeededField = new JTextField(
                LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).format(WHEN_FMT));
        public final JTextArea additionalDetailsArea = new JTextArea(3, 24);

        public Fields() {
            reset();
        }

        public void reset() {
            locationField.setText("");
            pickupField.setText("");
            destinationField.setText("");
            passengerCountField.setText("1");
            whenNeededField.setText(LocalDateTime.now().plusDays(1)
                    .withHour(9).withMinute(0).format(WHEN_FMT));
            additionalDetailsArea.setText("");
            itemRows.clear();
            itemRows.add(new ItemRow(new String[]{"kg", "pcs", "pack", "g", "liter", "ml"}));
        }

        public List<FoodItem> readFoodItems() {
            List<FoodItem> items = new ArrayList<>();
            for (ItemRow row : itemRows) {
                String name = row.nameField.getText().trim();
                if (!name.isEmpty()) {
                    items.add(new FoodItem(name,
                            Integer.parseInt(row.quantityField.getText().trim()),
                            (String) row.unitBox.getSelectedItem()));
                }
            }
            return items;
        }

        public List<MedicalItem> readMedicalItems() {
            List<MedicalItem> items = new ArrayList<>();
            for (ItemRow row : itemRows) {
                String name = row.nameField.getText().trim();
                if (!name.isEmpty()) {
                    items.add(new MedicalItem(name,
                            Integer.parseInt(row.quantityField.getText().trim()),
                            (String) row.unitBox.getSelectedItem()));
                }
            }
            return items;
        }
    }

    public static String toRequestType(String label) {
        if (label == null) return "Food";
        if (label.startsWith("Food")) return "Food";
        if (label.startsWith("Medical")) return "Medical";
        return "Transportation";
    }

    public static LocalDateTime parseWhenNeeded(String value) {
        return LocalDateTime.parse(value, WHEN_FMT);
    }

    /** Builds the request object for the given resident. */
    public static AssistanceRequest createRequest(String type, String requestId, String residentId,
                                                  Fields fields, String status,
                                                  LocalDateTime dateRequested) {
        LocalDateTime timeNeeded = parseWhenNeeded(fields.whenNeededField.getText().trim());
        String details = fields.additionalDetailsArea.getText().trim();
        if ("Food".equals(type)) {
            return new FoodRequest(requestId, residentId, fields.locationField.getText().trim(),
                    timeNeeded, details, status, dateRequested, fields.readFoodItems());
        }
        if ("Medical".equals(type)) {
            return new MedicalRequest(requestId, residentId, fields.locationField.getText().trim(),
                    timeNeeded, details, status, dateRequested, fields.readMedicalItems());
        }
        return new TransportationRequest(requestId, residentId, fields.pickupField.getText().trim(),
                fields.destinationField.getText().trim(),
                Integer.parseInt(fields.passengerCountField.getText().trim()), timeNeeded,
                details, status, dateRequested);
    }

    /** Same required-field validation as the resident form. False + popup on failure. */
    public static boolean hasRequiredFields(Component parent, String type, Fields fields) {
        if (!"Transportation".equals(type) && fields.locationField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Please enter the assistance location.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (fields.whenNeededField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Please enter when the assistance is needed.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            parseWhenNeeded(fields.whenNeededField.getText().trim());
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(parent, "Enter needed date/time as yyyy-MM-dd HH:mm.",
                    "Invalid Date/Time", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if ("Transportation".equals(type)) {
            if (fields.pickupField.getText().trim().isEmpty()
                    || fields.destinationField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "Please enter both pickup location and destination.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            try {
                if (Integer.parseInt(fields.passengerCountField.getText().trim()) <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parent,
                        "Passengers must be a whole number greater than zero.",
                        "Invalid Number", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true;
        }
        boolean hasItem = false;
        for (ItemRow row : fields.itemRows) {
            String name = row.nameField.getText().trim();
            String qty = row.quantityField.getText().trim();
            if (name.isEmpty() && qty.isEmpty()) continue;
            if (name.isEmpty() || qty.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "Complete each item row or leave it entirely blank.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            try {
                if (Integer.parseInt(qty) <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parent,
                        "Item quantities must be whole numbers greater than zero.",
                        "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            hasItem = true;
        }
        if (!hasItem) {
            JOptionPane.showMessageDialog(parent, "Please add at least one requested item.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
}
