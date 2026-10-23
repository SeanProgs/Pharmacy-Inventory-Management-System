package com.healthfirst.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

/**
 * Custom TabbedPaneUI that styles tabs to look like stat cards:
 * rounded corners, subtle borders, accent indicator bar under selected tab,
 * smooth hover effects, and clean flat content.
 */
public class CardTabbedPaneUI extends BasicTabbedPaneUI {
    private final Color stripColor;
    private final Color tabUnselected;
    private final Color tabSelected;
    private final Color tabHover;
    private final Color borderColor;
    private final Color textUnselected;
    private final Color textSelected;
    private final Color accentColor;
    private final Insets padding;

    public CardTabbedPaneUI(Color stripColor, Color tabUnselected, Color tabSelected, Color tabHover,
                            Color borderColor, Color textUnselected, Color textSelected,
                            Color accentColor, Insets padding) {
        this.stripColor = stripColor;
        this.tabUnselected = tabUnselected;
        this.tabSelected = tabSelected;
        this.tabHover = tabHover;
        this.borderColor = borderColor;
        this.textUnselected = textUnselected;
        this.textSelected = textSelected;
        this.accentColor = accentColor;
        this.padding = padding;
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        this.tabInsets = this.padding;
        this.selectedTabPadInsets = new Insets(0, 0, 0, 0);
        this.tabAreaInsets = new Insets(8, 12, 0, 12);
        this.contentBorderInsets = new Insets(0, 0, 0, 0);
    }

    @Override
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    @Override
    public void setRolloverTab(int index) {
        super.setRolloverTab(index);
        if (tabPane != null) {
            tabPane.repaint();
        }
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                      int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int rx = x + 3;
        int ry = y + 2;
        int rw = w - 6;
        int rh = h - 4;

        Color fillColor;
        if (isSelected) {
            fillColor = tabSelected;
        } else if (tabIndex == getRolloverTab()) {
            fillColor = tabHover;
        } else {
            fillColor = tabUnselected;
        }

        // Fill tab shape
        g2.setColor(fillColor);
        g2.fillRoundRect(rx, ry, rw, rh, 10, 10);

        // 1px border
        g2.setColor(borderColor);
        g2.drawRoundRect(rx, ry, rw, rh, 10, 10);

        // Accent indicator bar under selected tab
        if (isSelected) {
            g2.setColor(accentColor);
            int barH = 3;
            int barInset = 14;
            int barX = rx + barInset;
            int barY = ry + rh - barH - 2;
            int barW = rw - (barInset * 2);
            if (barW > 0) {
                g2.fillRoundRect(barX, barY, barW, barH, 3, 3);
            }
        }

        g2.dispose();
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                  int x, int y, int w, int h, boolean isSelected) {
        // Leave empty: border is drawn in paintTabBackground
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                       int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        // Leave empty: removes dotted focus rectangle
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        // Leave empty: removes lines around the content area
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                             int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(font);
        g2.setColor(isSelected ? textSelected : textUnselected);
        g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
        g2.dispose();
    }
}
