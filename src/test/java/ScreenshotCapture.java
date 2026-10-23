package com.healthfirst.test;

import com.healthfirst.model.*;
import com.healthfirst.ui.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotCapture {

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();

        // Auto-close any modal dialogs (such as DB connection error alerts)
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event.getID() == WindowEvent.WINDOW_OPENED) {
                    Object src = event.getSource();
                    if (src instanceof JDialog) {
                        JDialog dlg = (JDialog) src;
                        // Don't auto-close BillDialog since we want to capture it!
                        if (!(dlg instanceof BillDialog)) {
                            SwingUtilities.invokeLater(dlg::dispose);
                        }
                    }
                }
            }
        }, AWTEvent.WINDOW_EVENT_MASK);

        SwingUtilities.invokeLater(() -> {
            try {
                File dir = new File("screenshots");
                if (!dir.exists()) dir.mkdirs();

                captureLogin(new File(dir, "01_Login_Screen.png"));
                captureAdminDashboard(dir);
                captureCashierDashboard(new File(dir, "04_Cashier_POS_Cart.png"));
                captureBillDialog(new File(dir, "05_Generated_Bill_Window.png"));

                System.out.println("ALL SCREENSHOTS CAPTURED SUCCESSFULLY!");
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void saveComponent(RootPaneContainer comp, int w, int h, File target) throws Exception {
        if (comp instanceof Window) {
            Window win = (Window) comp;
            win.pack();
            win.setSize(w, h);
        }
        JComponent pane = (JComponent) comp.getContentPane();
        pane.setSize(w, h);
        pane.doLayout();
        pane.validate();

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        pane.paint(g2);
        g2.dispose();

        ImageIO.write(img, "PNG", target);
        System.out.println("Saved: " + target.getName() + " (" + target.length() + " bytes)");
    }

    private static void captureLogin(File target) throws Exception {
        LoginFrame login = new LoginFrame();
        login.addNotify();
        login.setSize(460, 560);
        login.validate();
        saveComponent(login, 460, 560, target);
        login.dispose();
    }

    private static void captureAdminDashboard(File dir) throws Exception {
        User admin = new User(1, "admin", "admin123", "Admin", "Emmanuel Komape");
        AdminDashboard adminDash = new AdminDashboard(admin);
        adminDash.addNotify();
        adminDash.setSize(1200, 800);
        adminDash.validate();

        populateAdminData(adminDash);
        adminDash.validate();
        adminDash.repaint();

        // 1. Overview / Tabs showing
        saveComponent(adminDash, 1200, 800, new File(dir, "02_Admin_Dashboard_Tabs.png"));

        // 2. Manage Medicines Tab with data
        JTabbedPane mainTabs = getField(adminDash, "mainTabbedPane");
        mainTabs.setSelectedIndex(0);
        adminDash.validate();
        saveComponent(adminDash, 1200, 800, new File(dir, "03_Manage_Medicines_Tab.png"));

        // 3. Switch to Reports tab
        mainTabs.setSelectedIndex(3);
        adminDash.validate();

        JTabbedPane reportTabs = getField(adminDash, "reportTabbedPane");

        // Sales Report
        reportTabs.setSelectedIndex(0);
        adminDash.validate();
        saveComponent(adminDash, 1200, 800, new File(dir, "06_Sales_Report.png"));

        // Item-Wise Report
        reportTabs.setSelectedIndex(1);
        adminDash.validate();
        saveComponent(adminDash, 1200, 800, new File(dir, "07_Item_Wise_Report.png"));

        // Low Stock Report
        reportTabs.setSelectedIndex(2);
        adminDash.validate();
        saveComponent(adminDash, 1200, 800, new File(dir, "08_Low_Stock_Report.png"));

        // Expiry Report
        reportTabs.setSelectedIndex(3);
        adminDash.validate();
        saveComponent(adminDash, 1200, 800, new File(dir, "09_Expiry_Report.png"));

        adminDash.dispose();
    }

    private static void captureCashierDashboard(File target) throws Exception {
        User cashier = new User(2, "cashier", "cash123", "Cashier", "Jane Doe");
        CashierDashboard pos = new CashierDashboard(cashier);
        pos.addNotify();
        pos.setSize(1100, 750);
        pos.validate();

        // Populate cart table
        DefaultTableModel cartModel = getField(pos, "cartTableModel");
        cartModel.setRowCount(0);
        cartModel.addRow(new Object[]{1, "Panado 500mg Tablets (24s)", "Tablet", "R 45.50", 2, "R 91.00", "Remove"});
        cartModel.addRow(new Object[]{2, "Benylin 4 Flu Syrup 100ml", "Syrup", "R 78.90", 1, "R 78.90", "Remove"});
        cartModel.addRow(new Object[]{3, "Voltaren Emulgel 50g", "Cream", "R 125.00", 1, "R 125.00", "Remove"});

        JLabel lblTotal = getField(pos, "lblTotalAmount");
        lblTotal.setText("R 294.90");

        JLabel lblItems = getField(pos, "lblTotalItems");
        lblItems.setText("4 units (3 items)");

        JTextField tender = getField(pos, "tenderField");
        tender.setText("350.00");

        JLabel lblChange = getField(pos, "lblChangeDue");
        lblChange.setText("R 55.10");

        pos.validate();
        pos.repaint();
        saveComponent(pos, 1100, 750, target);
        pos.dispose();
    }

    private static void captureBillDialog(File target) throws Exception {
        Sale sale = new Sale(1048, LocalDateTime.now(), new BigDecimal("294.90"), 2);
        sale.setCashierName("Jane Doe (Senior Cashier)");

        List<CartItem> items = new ArrayList<>();
        Medicine m1 = new Medicine(1, "Panado 500mg Tablets (24s)", "Adcock Ingram", "Tablet", new BigDecimal("45.50"), 150, 30, LocalDate.now().plusDays(365), 2);
        Medicine m2 = new Medicine(3, "Benylin 4 Flu Syrup 100ml", "Johnson & Johnson", "Syrup", new BigDecimal("78.90"), 45, 15, LocalDate.now().plusDays(20), 1);
        Medicine m3 = new Medicine(4, "Voltaren Emulgel 50g", "GSK Consumer", "Cream", new BigDecimal("125.00"), 35, 10, LocalDate.now().plusDays(450), 4);

        items.add(new CartItem(m1, 2));
        items.add(new CartItem(m2, 1));
        items.add(new CartItem(m3, 1));

        BillDialog bill = new BillDialog(null, sale, items, new BigDecimal("350.00"), new BigDecimal("55.10"));
        bill.addNotify();
        bill.setSize(520, 680);
        bill.validate();

        saveComponent(bill, 520, 680, target);
        bill.dispose();
    }

    private static void populateAdminData(AdminDashboard admin) {
        try {
            JLabel kpiMeds = getField(admin, "kpiTotalMeds");
            kpiMeds.setText("14");
            JLabel kpiLow = getField(admin, "kpiLowStock");
            kpiLow.setText("4");
            JLabel kpiExp = getField(admin, "kpiExpiring");
            kpiExp.setText("4");
            JLabel kpiSales = getField(admin, "kpiTotalSales");
            kpiSales.setText("R 6,134.50");

            DefaultTableModel medModel = getField(admin, "medTableModel");
            medModel.setRowCount(0);
            medModel.addRow(new Object[]{1, "Panado 500mg Tablets (24s)", "Adcock Ingram", "Tablet", "45.50", 150, 30, "2027-10-23", "Adcock Ingram"});
            medModel.addRow(new Object[]{2, "Amoxicillin 500mg Capsules", "Cipla Medpro", "Capsule", "115.00", 8, 20, "2027-04-20", "Cipla Medpro"});
            medModel.addRow(new Object[]{3, "Benylin 4 Flu Syrup 100ml", "Johnson & Johnson", "Syrup", "78.90", 45, 15, "2026-11-12", "Aspen Pharmacare"});
            medModel.addRow(new Object[]{4, "Voltaren Emulgel 50g", "GSK Consumer", "Cream", "125.00", 35, 10, "2028-01-15", "Pfizer South Africa"});
            medModel.addRow(new Object[]{5, "Vitamin C 1000mg Effervescent", "Aspen Pharmacare", "Tablet", "65.00", 5, 25, "2026-11-07", "Aspen Pharmacare"});
            medModel.addRow(new Object[]{6, "Allergex 4mg Tablets (30s)", "Adcock Ingram", "Tablet", "38.50", 85, 20, "2028-03-05", "Adcock Ingram"});
            medModel.addRow(new Object[]{7, "Brufen 400mg Tablets", "Abbott", "Tablet", "92.00", 60, 20, "2027-06-18", "Cipla Medpro"});
            medModel.addRow(new Object[]{8, "Corenza C Effervescent (10s)", "Cipla Medpro", "Tablet", "85.00", 12, 30, "2026-11-17", "Cipla Medpro"});
            medModel.addRow(new Object[]{9, "Bactroban Ointment 15g", "GSK Consumer", "Cream", "145.00", 18, 10, "2027-12-01", "Pfizer South Africa"});
            medModel.addRow(new Object[]{10, "Cataflam D 50mg (20s)", "Novartis", "Tablet", "110.00", 4, 15, "2026-11-04", "Dis-Chem Distribution"});
            medModel.addRow(new Object[]{11, "Gaviscon Peppermint 300ml", "Reckitt", "Syrup", "119.50", 40, 15, "2027-08-20", "Dis-Chem Distribution"});
            medModel.addRow(new Object[]{12, "Prospan Cough Syrup 100ml", "Engystol", "Syrup", "99.00", 55, 15, "2028-06-30", "Aspen Pharmacare"});

            DefaultTableModel salesModel = getField(admin, "salesReportModel");
            salesModel.setRowCount(0);
            salesModel.addRow(new Object[]{1048, "2026-10-23 16:42:15", "Jane Doe (Senior Cashier)", "294.90"});
            salesModel.addRow(new Object[]{1047, "2026-10-23 15:18:02", "John Smith (Cashier)", "160.50"});
            salesModel.addRow(new Object[]{1046, "2026-10-23 14:05:40", "Jane Doe (Senior Cashier)", "420.00"});
            salesModel.addRow(new Object[]{1045, "2026-10-22 17:30:11", "Jane Doe (Senior Cashier)", "209.00"});
            salesModel.addRow(new Object[]{1044, "2026-10-22 11:22:33", "John Smith (Cashier)", "385.00"});
            salesModel.addRow(new Object[]{1043, "2026-10-21 16:50:19", "Jane Doe (Senior Cashier)", "512.50"});

            JLabel lblSalesCount = getField(admin, "lblSalesReportCount");
            lblSalesCount.setText("Transactions: 6");
            JLabel lblSalesTotal = getField(admin, "lblSalesReportTotal");
            lblSalesTotal.setText("Total Revenue: R 1,981.90");

            DefaultTableModel itemModel = getField(admin, "itemWiseModel");
            itemModel.setRowCount(0);
            itemModel.addRow(new Object[]{1, "Panado 500mg Tablets (24s)", "Tablet", 38, "1,729.00", 150});
            itemModel.addRow(new Object[]{6, "Allergex 4mg Tablets (30s)", "Tablet", 24, "924.00", 85});
            itemModel.addRow(new Object[]{3, "Benylin 4 Flu Syrup 100ml", "Syrup", 19, "1,499.10", 45});
            itemModel.addRow(new Object[]{4, "Voltaren Emulgel 50g", "Cream", 15, "1,875.00", 35});
            itemModel.addRow(new Object[]{8, "Corenza C Effervescent (10s)", "Tablet", 12, "1,020.00", 12});
            itemModel.addRow(new Object[]{11, "Gaviscon Peppermint 300ml", "Syrup", 10, "1,195.00", 40});

            JLabel lblItemUnits = getField(admin, "lblItemWiseTotalUnits");
            lblItemUnits.setText("Total Units Sold: 118");
            JLabel lblItemRev = getField(admin, "lblItemWiseTotalRevenue");
            lblItemRev.setText("Catalog Revenue: R 8,242.10");

            DefaultTableModel lowModel = getField(admin, "lowStockModel");
            lowModel.setRowCount(0);
            lowModel.addRow(new Object[]{10, "Cataflam D 50mg (20s)", "Novartis", "Tablet", "110.00", 4, 15, "Deficit of 11", "Dis-Chem Distribution"});
            lowModel.addRow(new Object[]{5, "Vitamin C 1000mg Effervescent", "Aspen Pharmacare", "Tablet", "65.00", 5, 25, "Deficit of 20", "Aspen Pharmacare"});
            lowModel.addRow(new Object[]{2, "Amoxicillin 500mg Capsules", "Cipla Medpro", "Capsule", "115.00", 8, 20, "Deficit of 12", "Cipla Medpro"});
            lowModel.addRow(new Object[]{8, "Corenza C Effervescent (10s)", "Cipla Medpro", "Tablet", "85.00", 12, 30, "Deficit of 18", "Cipla Medpro"});

            JLabel lblLowCount = getField(admin, "lblLowStockCount");
            lblLowCount.setText("Alert items count: 4");

            DefaultTableModel expModel = getField(admin, "expiryModel");
            expModel.setRowCount(0);
            expModel.addRow(new Object[]{10, "Cataflam D 50mg (20s)", "Novartis", "Tablet", 4, "2026-11-04", 12, "Expiring in 12 day(s)", "Dis-Chem Distribution"});
            expModel.addRow(new Object[]{5, "Vitamin C 1000mg Effervescent", "Aspen Pharmacare", "Tablet", 5, "2026-11-07", 15, "Expiring in 15 day(s)", "Aspen Pharmacare"});
            expModel.addRow(new Object[]{3, "Benylin 4 Flu Syrup 100ml", "Johnson & Johnson", "Syrup", 45, "2026-11-12", 20, "Expiring in 20 day(s)", "Aspen Pharmacare"});
            expModel.addRow(new Object[]{8, "Corenza C Effervescent (10s)", "Cipla Medpro", "Tablet", 12, "2026-11-17", 25, "Expiring in 25 day(s)", "Cipla Medpro"});

            JLabel lblExpCount = getField(admin, "lblExpiryCount");
            lblExpCount.setText("Medicines expiring within next 30 days: 4");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object obj, String fieldName) throws Exception {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return (T) f.get(obj);
    }
}
