package com.healthfirst.ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class UITheme {

    // ── Theme Toggle ──────────────────────────────────────────────────────────
    private static boolean darkMode = false;
    private static final List<Runnable> themeListeners = new ArrayList<>();

    public static boolean isDarkMode() { return darkMode; }

    public static void toggleTheme() {
        darkMode = !darkMode;
        themeListeners.forEach(Runnable::run);
    }

    public static void addThemeListener(Runnable listener) {
        themeListeners.add(listener);
    }

    public static void removeThemeListener(Runnable listener) {
        themeListeners.remove(listener);
    }

    // ── Semantic Color Getters (always reflect current theme) ─────────────────
    public static Color bg()          { return darkMode ? new Color(18, 22, 30)    : new Color(245, 247, 250); }
    public static Color cardBg()      { return darkMode ? new Color(28, 34, 46)    : Color.WHITE; }
    public static Color surface()     { return darkMode ? new Color(36, 44, 58)    : new Color(248, 250, 252); }
    public static Color textPrimary() { return darkMode ? new Color(230, 235, 245) : new Color(33, 37, 41); }
    public static Color textMuted()   { return darkMode ? new Color(140, 155, 175) : new Color(108, 117, 125); }
    public static Color border()      { return darkMode ? new Color(52, 62, 80)    : new Color(222, 226, 230); }
    public static Color headerBg()    { return darkMode ? new Color(15, 20, 35)    : PRIMARY; }
    public static Color tableHeader() { return darkMode ? new Color(36, 44, 58)    : new Color(238, 242, 246); }
    public static Color tableGrid()   { return darkMode ? new Color(46, 56, 72)    : new Color(235, 238, 242); }
    public static Color selection()   { return darkMode ? new Color(41, 98, 155)   : new Color(218, 234, 250); }
    public static Color inputBg()     { return darkMode ? new Color(40, 50, 66)    : Color.WHITE; }
    public static Color inputFg()     { return darkMode ? new Color(220, 230, 245) : new Color(33, 37, 41); }

    // ── Static Brand / Accent Colors ──────────────────────────────────────────
    public static final Color PRIMARY       = new Color(24, 76, 120);
    public static final Color PRIMARY_LIGHT = new Color(41, 128, 185);
    public static final Color SUCCESS       = new Color(39, 144, 88);
    public static final Color DANGER        = new Color(204, 51, 51);
    public static final Color WARNING       = new Color(230, 126, 34);

    // ── Legacy static aliases kept for backward compatibility ─────────────────
    public static final Color PRIMARY_HOVER  = new Color(18, 58, 92);
    public static final Color SUCCESS_HOVER  = new Color(30, 115, 70);
    public static final Color DANGER_HOVER   = new Color(170, 35, 35);
    public static final Color BG_LIGHT       = new Color(245, 247, 250);
    public static final Color CARD_BG        = Color.WHITE;
    public static final Color TEXT_DARK      = new Color(33, 37, 41);
    public static final Color TEXT_MUTED     = new Color(108, 117, 125);
    public static final Color BORDER_COLOR   = new Color(222, 226, 230);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_HEADER   = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD     = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);

    // ── Rounded Border ────────────────────────────────────────────────────────
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        private final int thickness;

        public RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x + thickness / 2f, y + thickness / 2f,
                    width - thickness, height - thickness, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(thickness + 4, thickness + 4, thickness + 4, thickness + 4); }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(thickness + 4, thickness + 4, thickness + 4, thickness + 4);
            return insets;
        }
    }

    // ── Rounded Panel ─────────────────────────────────────────────────────────
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private Color bg;

        public RoundedPanel(LayoutManager layout, int radius, Color bg) {
            super(layout);
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        public void updateBg(Color bg) { this.bg = bg; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
        }
    }

    // ── Apply Global Look ─────────────────────────────────────────────────────
    public static void applyGlobalTheme() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_BOLD);
    }

    // ── Themed Button ─────────────────────────────────────────────────────────
    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                // Paint text on top
                FontMetrics fm = g.getFontMetrics();
                g.setColor(getForeground());
                g.setFont(getFont());
                String label = getText();
                int tx = (getWidth() - fm.stringWidth(label)) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(label, tx, ty);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Theme Toggle Button ───────────────────────────────────────────────────
    public static JButton createThemeToggleButton() {
        JButton btn = new JButton(darkMode ? "\u2600 Light Mode" : "\uD83C\uDF19 Dark Mode") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                FontMetrics fm = g.getFontMetrics();
                g.setColor(getForeground());
                g.setFont(getFont());
                String label = getText();
                int tx = (getWidth() - fm.stringWidth(label)) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(label, tx, ty);
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateToggleButtonColors(btn);
        return btn;
    }

    public static void updateToggleButtonColors(JButton btn) {
        if (darkMode) {
            btn.setBackground(new Color(255, 213, 79));
            btn.setForeground(new Color(30, 30, 30));
            btn.setText("\u2600 Light Mode");
        } else {
            btn.setBackground(new Color(50, 65, 95));
            btn.setForeground(Color.WHITE);
            btn.setText("\uD83C\uDF19 Dark Mode");
        }
    }

    // ── Header Panel ──────────────────────────────────────────────────────────
    public static JPanel createHeader(String title, String subtitle, String userName, String userRole, Runnable onLogout) {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBackground(headerBg());
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("HealthFirst Pharmacy  |  " + title);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);
        brandPanel.add(lblTitle);

        JLabel lblSub = new JLabel(subtitle != null ? subtitle : "Pharmacy Inventory Management System (PIMS)");
        lblSub.setFont(FONT_SUBTITLE);
        lblSub.setForeground(new Color(200, 220, 245));
        brandPanel.add(lblSub);

        header.add(brandPanel, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        userPanel.setOpaque(false);

        JLabel lblUser = new JLabel("Logged in: " + userName + " (" + userRole + ")");
        lblUser.setFont(FONT_BOLD);
        lblUser.setForeground(Color.WHITE);
        userPanel.add(lblUser);

        // Theme toggle button
        JButton themeBtn = createThemeToggleButton();
        themeBtn.addActionListener(e -> toggleTheme());
        userPanel.add(themeBtn);

        JButton logoutBtn = createStyledButton("Logout", new Color(180, 40, 40), Color.WHITE);
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(header,
                    "Are you sure you want to log out?", "Confirm Logout",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION && onLogout != null) {
                onLogout.run();
            }
        });
        userPanel.add(logoutBtn);
        header.add(userPanel, BorderLayout.EAST);

        // Wire theme toggle to update header background and toggle button label
        addThemeListener(() -> {
            header.setBackground(headerBg());
            updateToggleButtonColors(themeBtn);
            header.repaint();
        });

        return header;
    }

    // ── Table Styling ─────────────────────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(tableGrid());
        table.setBackground(cardBg());
        table.setForeground(textPrimary());
        table.setSelectionBackground(selection());
        table.setSelectionForeground(textPrimary());
        table.setFont(FONT_BODY);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(tableHeader());
        header.setForeground(PRIMARY_LIGHT);
        header.setPreferredSize(new Dimension(0, 36));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }

    // ── Card Panel ────────────────────────────────────────────────────────────
    public static JPanel createCard(String title, JComponent content) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 10), 12, cardBg());
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(border(), 12, 1),
                new EmptyBorder(12, 14, 14, 14)
        ));

        if (title != null && !title.isEmpty()) {
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(FONT_HEADER);
            lblTitle.setForeground(PRIMARY_LIGHT);
            lblTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, border()));
            card.add(lblTitle, BorderLayout.NORTH);
        }
        card.add(content, BorderLayout.CENTER);

        addThemeListener(() -> {
            card.updateBg(cardBg());
            card.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(border(), 12, 1),
                    new EmptyBorder(12, 14, 14, 14)
            ));
            card.repaint();
        });

        return card;
    }

    // ── KPI Card ──────────────────────────────────────────────────────────────
    public static JPanel createKpiCard(String title, JLabel valueLabel, String note, Color accent) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(), 12, cardBg());
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(border(), 12, 1),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lblT = new JLabel(title);
        lblT.setFont(FONT_SMALL);
        lblT.setForeground(textMuted());
        top.add(lblT, BorderLayout.WEST);

        JPanel strip = new JPanel();
        strip.setBackground(accent);
        strip.setPreferredSize(new Dimension(4, 14));
        top.add(strip, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accent);
        valueLabel.setBorder(new EmptyBorder(6, 0, 4, 0));
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel lblN = new JLabel(note);
        lblN.setFont(FONT_SMALL);
        lblN.setForeground(textMuted());
        card.add(lblN, BorderLayout.SOUTH);

        addThemeListener(() -> {
            card.updateBg(cardBg());
            card.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(border(), 12, 1),
                    new EmptyBorder(12, 14, 12, 14)
            ));
            lblT.setForeground(textMuted());
            lblN.setForeground(textMuted());
            card.repaint();
        });

        return card;
    }

    // ── Legacy createStatCard (kept for backward compatibility) ───────────────
    public static JPanel createStatCard(String title, String value, String note, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(FONT_SMALL);
        lblTitle.setForeground(TEXT_MUTED);
        top.add(lblTitle, BorderLayout.WEST);

        JPanel strip = new JPanel();
        strip.setBackground(accentColor);
        strip.setPreferredSize(new Dimension(4, 20));
        top.add(strip, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(accentColor);
        lblVal.setBorder(new EmptyBorder(6, 0, 4, 0));
        card.add(lblVal, BorderLayout.CENTER);

        if (note != null) {
            JLabel lblNote = new JLabel(note);
            lblNote.setFont(FONT_SMALL);
            lblNote.setForeground(TEXT_MUTED);
            card.add(lblNote, BorderLayout.SOUTH);
        }

        return card;
    }
}
