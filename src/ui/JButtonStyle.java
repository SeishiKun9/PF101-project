package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class JButtonStyle {
    public static final Color PRIMARY_BLUE = new Color(29, 78, 216);      // Vivid Royal Blue (blue-700)
    public static final Color PRIMARY_HOVER = new Color(30, 64, 175);     // Darker Blue (blue-800)
    public static final Color SUCCESS_GREEN = new Color(22, 163, 74);     // Green-600
    public static final Color SUCCESS_HOVER = new Color(21, 128, 61);     // Green-700
    public static final Color DANGER_RED = new Color(220, 38, 38);        // Red-600
    public static final Color DANGER_HOVER = new Color(185, 28, 28);      // Red-700
    public static final Color SECONDARY_BG = new Color(241, 245, 249);    // Slate-100
    public static final Color SECONDARY_HOVER = new Color(226, 232, 240); // Slate-200
    public static final Color SECONDARY_BORDER = new Color(148, 163, 184);// Slate-400

    public static final Color BG_PAGE = new Color(241, 245, 249);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(15, 23, 42);
    public static final Color TEXT_MUTED = new Color(71, 85, 105);
    public static final Color BORDER_LIGHT = new Color(203, 213, 225);
    public static final Color BORDER_FOCUS = new Color(37, 99, 235);

    private JButtonStyle() {
    }

    /** Primary prominent button with strong royal blue background and crisp white text. */
    public static void applyAccent(JButton button) {
        button.setOpaque(true);
        button.setBackground(PRIMARY_BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_HOVER, 1, true),
                BorderFactory.createEmptyBorder(9, 18, 9, 18)));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (button.isEnabled()) button.setBackground(PRIMARY_HOVER); }
            @Override public void mouseExited(MouseEvent e) { if (button.isEnabled()) button.setBackground(PRIMARY_BLUE); }
        });
    }

    /** High-contrast secondary button with visible slate border and dark bold text. */
    public static void applySecondary(JButton button) {
        button.setOpaque(true);
        button.setBackground(SECONDARY_BG);
        button.setForeground(TEXT_DARK);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12.5f));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (button.isEnabled()) button.setBackground(SECONDARY_HOVER); }
            @Override public void mouseExited(MouseEvent e) { if (button.isEnabled()) button.setBackground(SECONDARY_BG); }
        });
    }

    /** Prominent green action button for creation / approval actions. */
    public static void applySuccess(JButton button) {
        button.setOpaque(true);
        button.setBackground(SUCCESS_GREEN);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12.5f));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SUCCESS_HOVER, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (button.isEnabled()) button.setBackground(SUCCESS_HOVER); }
            @Override public void mouseExited(MouseEvent e) { if (button.isEnabled()) button.setBackground(SUCCESS_GREEN); }
        });
    }

    /** High-visibility danger button with bold white text on vivid red. */
    public static void applyDanger(JButton button) {
        button.setOpaque(true);
        button.setBackground(DANGER_RED);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12.5f));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DANGER_HOVER, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (button.isEnabled()) button.setBackground(DANGER_HOVER); }
            @Override public void mouseExited(MouseEvent e) { if (button.isEnabled()) button.setBackground(DANGER_RED); }
        });
    }

    public static Border createFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(148, 163, 184), 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10));
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(226, 232, 240));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_DARK);
        table.setForeground(TEXT_DARK);
        table.setFont(table.getFont().deriveFont(12.5f));

        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(51, 65, 85));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));
        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getPreferredSize().width, 36));
        table.getTableHeader().setReorderingAllowed(false);
    }

    public static JPanel createStatCard(String title, JLabel valueLabel, Color accentColor, String subtitle) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                // accent color indicator bar on the left
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 5, getHeight() - 1, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(4, 4));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(titleLbl.getFont().deriveFont(Font.BOLD, 11f));
        titleLbl.setForeground(TEXT_MUTED);

        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 22f));
        valueLabel.setForeground(TEXT_DARK);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(subLbl.getFont().deriveFont(Font.PLAIN, 11f));
        subLbl.setForeground(new Color(100, 116, 139));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);
        return card;
    }

    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            String status = value == null ? "" : value.toString();

            label.setOpaque(true);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 11.5f));

            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
                return label;
            }

            switch (status.toLowerCase()) {
                case "pending":
                    label.setBackground(new Color(254, 243, 199)); // amber 100
                    label.setForeground(new Color(180, 83, 9));    // amber 700
                    break;
                case "approved":
                case "in progress":
                case "dispatched":
                    label.setBackground(new Color(219, 234, 254)); // blue 100
                    label.setForeground(new Color(29, 78, 216));   // blue 700
                    break;
                case "completed":
                case "delivered":
                case "resolved":
                    label.setBackground(new Color(220, 252, 231)); // emerald 100
                    label.setForeground(new Color(21, 128, 61));   // emerald 700
                    break;
                case "cancelled":
                case "rejected":
                    label.setBackground(new Color(254, 226, 226)); // rose 100
                    label.setForeground(new Color(190, 18, 60));   // rose 700
                    break;
                default:
                    label.setBackground(new Color(241, 245, 249));
                    label.setForeground(TEXT_DARK);
                    break;
            }
            label.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            return label;
        }
    }
}
