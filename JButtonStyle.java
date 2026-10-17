import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class JButtonStyle {
    public static final Color PRIMARY_BLUE = new Color(37, 99, 235);
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color BG_PAGE = new Color(241, 245, 249);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(15, 23, 42);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color BORDER_LIGHT = new Color(226, 232, 240);
    public static final Color BORDER_FOCUS = new Color(59, 130, 246);

    private JButtonStyle() {
    }

    public static void applyAccent(JButton button) {
        button.setBackground(PRIMARY_BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(PRIMARY_HOVER); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(PRIMARY_BLUE); }
        });
    }

    public static void applySecondary(JButton button) {
        button.setBackground(new Color(248, 250, 252));
        button.setForeground(TEXT_DARK);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 12f));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(new Color(226, 232, 240)); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(new Color(248, 250, 252)); }
        });
    }

    public static void applyDanger(JButton button) {
        button.setBackground(new Color(254, 242, 242));
        button.setForeground(new Color(225, 29, 72));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(254, 205, 211), 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(new Color(254, 226, 226)); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(new Color(254, 242, 242)); }
        });
    }

    public static Border createFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.setForeground(TEXT_DARK);
        table.setFont(table.getFont().deriveFont(12.5f));

        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(new Color(71, 85, 105));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
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
        subLbl.setForeground(new Color(148, 163, 184));

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
