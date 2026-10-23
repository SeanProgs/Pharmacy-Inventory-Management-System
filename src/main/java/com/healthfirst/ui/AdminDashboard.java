package com.healthfirst.ui;

import com.healthfirst.dao.MedicineDAO;
import com.healthfirst.dao.SaleDAO;
import com.healthfirst.dao.SupplierDAO;
import com.healthfirst.dao.UserDAO;
import com.healthfirst.model.Medicine;
import com.healthfirst.model.Sale;
import com.healthfirst.model.Supplier;
import com.healthfirst.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboard extends JFrame {
    private User currentUser;
    private MedicineDAO medicineDAO;
    private SupplierDAO supplierDAO;
    private UserDAO userDAO;
    private SaleDAO saleDAO;

    // KPI Cards
    private JLabel kpiTotalMeds;
    private JLabel kpiLowStock;
    private JLabel kpiExpiring;
    private JLabel kpiTotalSales;

    // TAB 1: Medicines UI
    private JTable medTable;
    private DefaultTableModel medTableModel;
    private JTextField medIdField;
    private JTextField medNameField;
    private JTextField medCompanyField;
    private JComboBox<String> medTypeCombo;
    private JTextField medPriceField;
    private JTextField medStockField;
    private JTextField medReorderField;
    private JTextField medExpiryField;
    private JComboBox<Supplier> medSupplierCombo;
    private JTextField medSearchField;
    private JComboBox<String> medFilterTypeCombo;

    // TAB 2: Suppliers UI
    private JTable supTable;
    private DefaultTableModel supTableModel;
    private JTextField supIdField;
    private JTextField supNameField;
    private JTextField supContactField;
    private JTextField supPhoneField;
    private JTextField supEmailField;
    private JTextArea supAddressArea;
    private JTextField supSearchField;

    // TAB 3: Users UI
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JTextField userIdField;
    private JTextField userUsernameField;
    private JPasswordField userPasswordField;
    private JComboBox<String> userRoleCombo;
    private JTextField userFullNameField;

    // TAB 4: Reports UI
    private JTable salesReportTable;
    private DefaultTableModel salesReportModel;
    private JComboBox<String> salesDateFilter;
    private JLabel lblSalesReportTotal;
    private JLabel lblSalesReportCount;

    private JTable itemWiseTable;
    private DefaultTableModel itemWiseModel;
    private JLabel lblItemWiseTotalUnits;
    private JLabel lblItemWiseTotalRevenue;

    private JTable lowStockTable;
    private DefaultTableModel lowStockModel;
    private JLabel lblLowStockCount;

    private JTable expiryTable;
    private DefaultTableModel expiryModel;
    private JLabel lblExpiryCount;

    // Tab Panes
    private JTabbedPane mainTabbedPane;
    private JTabbedPane reportTabbedPane;

    public AdminDashboard(User user) {
        this.currentUser = user;
        this.medicineDAO = new MedicineDAO();
        this.supplierDAO = new SupplierDAO();
        this.userDAO = new UserDAO();
        this.saleDAO = new SaleDAO();

        initUI();
        refreshAllData();
    }

    private void initUI() {
        setTitle("HealthFirst Pharmacy - Administrator Portal");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JPanel header = UITheme.createHeader(
                "Administration & Management",
                "Full Catalog CRUD, User Roles & Business Analytics",
                currentUser.getFullName(),
                currentUser.getRole(),
                this::logout
        );
        add(header, BorderLayout.NORTH);

        // Center: Tabbed Pane + KPI Bar at BOTTOM
        JPanel centerContainer = new JPanel(new BorderLayout(0, 10));
        centerContainer.setBackground(UITheme.bg());
        centerContainer.setBorder(new EmptyBorder(10, 12, 10, 12));

        // KPI Stats Bar moved to SOUTH (bottom)
        JPanel kpiBar = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiBar.setOpaque(false);
        kpiBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        kpiTotalMeds = new JLabel("0");
        kpiLowStock = new JLabel("0");
        kpiExpiring = new JLabel("0");
        kpiTotalSales = new JLabel("R 0.00");

        kpiBar.add(UITheme.createKpiCard("TOTAL MEDICINES", kpiTotalMeds, "In catalog", UITheme.PRIMARY));
        kpiBar.add(UITheme.createKpiCard("LOW STOCK ALERTS", kpiLowStock, "Needs reorder", UITheme.WARNING));
        kpiBar.add(UITheme.createKpiCard("EXPIRING SOON", kpiExpiring, "Within 30 days", UITheme.DANGER));
        kpiBar.add(UITheme.createKpiCard("TOTAL REVENUE", kpiTotalSales, "All-time sales", UITheme.SUCCESS));

        centerContainer.add(kpiBar, BorderLayout.SOUTH);

        // Main Tabs
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab("💊 Manage Medicines", createMedicinesTab());
        mainTabbedPane.addTab("🏢 Manage Suppliers", createSuppliersTab());
        mainTabbedPane.addTab("👥 Manage Users", createUsersTab());
        mainTabbedPane.addTab("📊 Reports & Analytics", createReportsTab());

        applyTabStyle(UITheme.isDarkMode());

        centerContainer.add(mainTabbedPane, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // Theme listener to repaint container background and restyle tabs
        UITheme.addThemeListener(() -> {
            centerContainer.setBackground(UITheme.bg());
            kpiBar.repaint();
            SwingUtilities.updateComponentTreeUI(this);
            applyTabStyle(UITheme.isDarkMode());
        });
    }

    private void applyTabStyle(boolean darkMode) {
        Color strip = darkMode ? new Color(0x0B1220) : new Color(0xE9EEF5);
        Color unselected = darkMode ? new Color(0x111827) : new Color(0xFFFFFF);
        Color selected = darkMode ? new Color(0x1A2438) : new Color(0xFFFFFF);
        Color hover = darkMode ? new Color(0x162033) : new Color(0xF3F6FA);
        Color border = darkMode ? new Color(0x1F2937) : new Color(0xD9E0EA);
        Color textUnselected = darkMode ? new Color(0x94A3B8) : new Color(0x475569);
        Color textSelected = darkMode ? new Color(0xFFFFFF) : new Color(0x0F172A);
        Color accent = darkMode ? new Color(0x3B82F6) : new Color(0x2563EB);

        Font mainFont = new Font("Segoe UI Emoji", Font.BOLD, 14);
        Font subFont = new Font("Segoe UI Emoji", Font.BOLD, 13);

        if (mainTabbedPane != null) {
            mainTabbedPane.setFont(mainFont);
            mainTabbedPane.setUI(new CardTabbedPaneUI(strip, unselected, selected, hover,
                    border, textUnselected, textSelected, accent, new Insets(10, 22, 10, 22)));
            mainTabbedPane.setBackground(strip);
            mainTabbedPane.setOpaque(true);
            mainTabbedPane.revalidate();
            mainTabbedPane.repaint();
        }

        if (reportTabbedPane != null) {
            reportTabbedPane.setFont(subFont);
            reportTabbedPane.setUI(new CardTabbedPaneUI(strip, unselected, selected, hover,
                    border, textUnselected, textSelected, accent, new Insets(8, 18, 8, 18)));
            reportTabbedPane.setBackground(strip);
            reportTabbedPane.setOpaque(true);
            reportTabbedPane.revalidate();
            reportTabbedPane.repaint();
        }
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, String note, Color accent) {
        return UITheme.createKpiCard(title, valueLabel, note, accent);
    }

    // =========================================================================
    // TAB 1: MANAGE MEDICINES
    // =========================================================================
    private JPanel createMedicinesTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Form Panel (Left)
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setPreferredSize(new Dimension(380, 0));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel title = new JLabel("Medicine Details");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.PRIMARY);
        formCard.add(title, gbc);

        medIdField = new JTextField();
        medIdField.setEditable(false);
        medIdField.setBackground(new Color(245, 245, 245));

        medNameField = new JTextField(15);
        medCompanyField = new JTextField(15);
        medTypeCombo = new JComboBox<>(new String[]{"Tablet", "Capsule", "Syrup", "Injection", "Cream", "Ointment", "Drops", "Inhaler"});
        medPriceField = new JTextField(15);
        medStockField = new JTextField(15);
        medReorderField = new JTextField(15);
        medExpiryField = new JTextField(15);
        medExpiryField.setToolTipText("Format: YYYY-MM-DD (e.g. 2026-12-31)");
        medSupplierCombo = new JComboBox<>();

        addFormRow(formCard, gbc, ++row, "Medicine ID:", medIdField);
        addFormRow(formCard, gbc, ++row, "Medicine Name*:", medNameField);
        addFormRow(formCard, gbc, ++row, "Company / Brand*:", medCompanyField);
        addFormRow(formCard, gbc, ++row, "Type*:", medTypeCombo);
        addFormRow(formCard, gbc, ++row, "Price (R)*:", medPriceField);
        addFormRow(formCard, gbc, ++row, "Qty in Stock*:", medStockField);
        addFormRow(formCard, gbc, ++row, "Reorder Level*:", medReorderField);
        addFormRow(formCard, gbc, ++row, "Expiry (YYYY-MM-DD)*:", medExpiryField);
        addFormRow(formCard, gbc, ++row, "Supplier*:", medSupplierCombo);

        // Action Buttons Grid
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);

        JButton addBtn = UITheme.createStyledButton("➕ Add Medicine", UITheme.SUCCESS, Color.WHITE);
        addBtn.addActionListener(e -> addMedicine());

        JButton updateBtn = UITheme.createStyledButton("✏ Update Medicine", UITheme.PRIMARY_LIGHT, Color.WHITE);
        updateBtn.addActionListener(e -> updateMedicine());

        JButton deleteBtn = UITheme.createStyledButton("🗑 Delete Medicine", UITheme.DANGER, Color.WHITE);
        deleteBtn.addActionListener(e -> deleteMedicine());

        JButton clearBtn = UITheme.createStyledButton("🔄 Clear Form", new Color(120, 130, 140), Color.WHITE);
        clearBtn.addActionListener(e -> clearMedicineForm());

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        formCard.add(btnPanel, gbc);
        panel.add(formCard, BorderLayout.WEST);

        // Table Panel (Right)
        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.setOpaque(false);

        // Search toolbar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        searchBar.setBackground(Color.WHITE);
        searchBar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        searchBar.add(new JLabel("Search:"));
        medSearchField = new JTextField(16);
        medSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterMedicinesTable();
            }
        });
        searchBar.add(medSearchField);

        searchBar.add(new JLabel("Type:"));
        medFilterTypeCombo = new JComboBox<>(new String[]{"All", "Tablet", "Capsule", "Syrup", "Injection", "Cream", "Ointment", "Drops", "Inhaler"});
        medFilterTypeCombo.addActionListener(e -> filterMedicinesTable());
        searchBar.add(medFilterTypeCombo);

        JButton refreshMedsBtn = new JButton("Refresh");
        refreshMedsBtn.addActionListener(e -> loadMedicinesTable());
        searchBar.add(refreshMedsBtn);

        tablePanel.add(searchBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Company", "Type", "Price (R)", "Stock", "Reorder", "Expiry Date", "Supplier"};
        medTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        medTable = new JTable(medTableModel);
        UITheme.styleTable(medTable);
        medTable.getSelectionModel().addListSelectionListener(e -> onMedicineTableRowSelected());

        tablePanel.add(new JScrollPane(medTable), BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        comp.setFont(UITheme.FONT_BODY);
        panel.add(comp, gbc);
    }

    // =========================================================================
    // TAB 2: MANAGE SUPPLIERS
    // =========================================================================
    private JPanel createSuppliersTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Form Card (Left)
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setPreferredSize(new Dimension(380, 0));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel title = new JLabel("Supplier Details");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.PRIMARY);
        formCard.add(title, gbc);

        supIdField = new JTextField();
        supIdField.setEditable(false);
        supIdField.setBackground(new Color(245, 245, 245));

        supNameField = new JTextField(15);
        supContactField = new JTextField(15);
        supPhoneField = new JTextField(15);
        supEmailField = new JTextField(15);
        supAddressArea = new JTextArea(3, 15);
        supAddressArea.setFont(UITheme.FONT_BODY);
        supAddressArea.setLineWrap(true);
        supAddressArea.setWrapStyleWord(true);
        JScrollPane addrScroll = new JScrollPane(supAddressArea);

        addFormRow(formCard, gbc, ++row, "Supplier ID:", supIdField);
        addFormRow(formCard, gbc, ++row, "Company Name*:", supNameField);
        addFormRow(formCard, gbc, ++row, "Contact Person:", supContactField);
        addFormRow(formCard, gbc, ++row, "Phone Number:", supPhoneField);
        addFormRow(formCard, gbc, ++row, "Email Address:", supEmailField);
        addFormRow(formCard, gbc, ++row, "Physical Address:", addrScroll);

        // Buttons
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);

        JButton addBtn = UITheme.createStyledButton("➕ Add Supplier", UITheme.SUCCESS, Color.WHITE);
        addBtn.addActionListener(e -> addSupplier());

        JButton updateBtn = UITheme.createStyledButton("✏ Update Supplier", UITheme.PRIMARY_LIGHT, Color.WHITE);
        updateBtn.addActionListener(e -> updateSupplier());

        JButton deleteBtn = UITheme.createStyledButton("🗑 Delete Supplier", UITheme.DANGER, Color.WHITE);
        deleteBtn.addActionListener(e -> deleteSupplier());

        JButton clearBtn = UITheme.createStyledButton("🔄 Clear Form", new Color(120, 130, 140), Color.WHITE);
        clearBtn.addActionListener(e -> clearSupplierForm());

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        formCard.add(btnPanel, gbc);
        panel.add(formCard, BorderLayout.WEST);

        // Table (Right)
        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.setOpaque(false);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        searchBar.setBackground(Color.WHITE);
        searchBar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        searchBar.add(new JLabel("Search Supplier:"));
        supSearchField = new JTextField(18);
        supSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterSuppliersTable();
            }
        });
        searchBar.add(supSearchField);

        JButton refreshSupBtn = new JButton("Refresh");
        refreshSupBtn.addActionListener(e -> loadSuppliersTable());
        searchBar.add(refreshSupBtn);

        tablePanel.add(searchBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Company Name", "Contact Person", "Phone", "Email", "Address"};
        supTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        supTable = new JTable(supTableModel);
        UITheme.styleTable(supTable);
        supTable.getSelectionModel().addListSelectionListener(e -> onSupplierTableRowSelected());

        tablePanel.add(new JScrollPane(supTable), BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // TAB 3: MANAGE USERS
    // =========================================================================
    private JPanel createUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Form Card (Left)
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setPreferredSize(new Dimension(380, 0));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel title = new JLabel("User Account Details");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.PRIMARY);
        formCard.add(title, gbc);

        userIdField = new JTextField();
        userIdField.setEditable(false);
        userIdField.setBackground(new Color(245, 245, 245));

        userUsernameField = new JTextField(15);
        userPasswordField = new JPasswordField(15);
        userRoleCombo = new JComboBox<>(new String[]{"Cashier", "Admin"});
        userFullNameField = new JTextField(15);

        addFormRow(formCard, gbc, ++row, "User ID:", userIdField);
        addFormRow(formCard, gbc, ++row, "Username*:", userUsernameField);
        addFormRow(formCard, gbc, ++row, "Password*:", userPasswordField);
        addFormRow(formCard, gbc, ++row, "Assigned Role*:", userRoleCombo);
        addFormRow(formCard, gbc, ++row, "Full Name*:", userFullNameField);

        // Buttons
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);

        JButton addBtn = UITheme.createStyledButton("➕ Add User", UITheme.SUCCESS, Color.WHITE);
        addBtn.addActionListener(e -> addUser());

        JButton updateBtn = UITheme.createStyledButton("✏ Update User", UITheme.PRIMARY_LIGHT, Color.WHITE);
        updateBtn.addActionListener(e -> updateUser());

        JButton deleteBtn = UITheme.createStyledButton("🗑 Delete User", UITheme.DANGER, Color.WHITE);
        deleteBtn.addActionListener(e -> deleteUser());

        JButton clearBtn = UITheme.createStyledButton("🔄 Clear Form", new Color(120, 130, 140), Color.WHITE);
        clearBtn.addActionListener(e -> clearUserForm());

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        formCard.add(btnPanel, gbc);
        panel.add(formCard, BorderLayout.WEST);

        // Table (Right)
        String[] cols = {"User ID", "Username", "Role", "Full Name"};
        userTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        userTable = new JTable(userTableModel);
        UITheme.styleTable(userTable);
        userTable.getSelectionModel().addListSelectionListener(e -> onUserTableRowSelected());

        JPanel tableCard = UITheme.createCard("Registered System Users", new JScrollPane(userTable));
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // TAB 4: REPORTS & ANALYTICS
    // =========================================================================
    private JPanel createReportsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_LIGHT);

        reportTabbedPane = new JTabbedPane();

        reportTabbedPane.addTab("📈 Sales Report", createSalesReportSubTab());
        reportTabbedPane.addTab("📦 Item-Wise Sales Report", createItemWiseReportSubTab());
        reportTabbedPane.addTab("⚠ Low Stock Report", createLowStockReportSubTab());
        reportTabbedPane.addTab("⏳ Expiry Report (Next 1 Month)", createExpiryReportSubTab());

        panel.add(reportTabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSalesReportSubTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        toolbar.add(new JLabel("Date Filter:"));
        salesDateFilter = new JComboBox<>(new String[]{"All Time", "Today", "Last 7 Days", "This Month"});
        salesDateFilter.addActionListener(e -> loadSalesReport());
        toolbar.add(salesDateFilter);

        JButton refreshBtn = new JButton("Refresh Report");
        refreshBtn.addActionListener(e -> loadSalesReport());
        toolbar.add(refreshBtn);

        JButton exportBtn = UITheme.createStyledButton("Export to CSV", UITheme.SUCCESS, Color.WHITE);
        exportBtn.addActionListener(e -> exportTableToCSV(salesReportTable, "SalesReport.csv"));
        toolbar.add(exportBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Sale ID", "Date & Time", "Cashier Name", "Total Amount (R)"};
        salesReportModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        salesReportTable = new JTable(salesReportModel);
        UITheme.styleTable(salesReportTable);
        panel.add(new JScrollPane(salesReportTable), BorderLayout.CENTER);

        // Summary Bar
        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 8));
        summaryBar.setBackground(Color.WHITE);
        summaryBar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        lblSalesReportCount = new JLabel("Transactions: 0");
        lblSalesReportCount.setFont(UITheme.FONT_BOLD);
        summaryBar.add(lblSalesReportCount);

        lblSalesReportTotal = new JLabel("Total Revenue: R 0.00");
        lblSalesReportTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSalesReportTotal.setForeground(UITheme.PRIMARY);
        summaryBar.add(lblSalesReportTotal);

        panel.add(summaryBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createItemWiseReportSubTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        JButton refreshBtn = new JButton("Refresh Item Report");
        refreshBtn.addActionListener(e -> loadItemWiseReport());
        toolbar.add(refreshBtn);

        JButton exportBtn = UITheme.createStyledButton("Export to CSV", UITheme.SUCCESS, Color.WHITE);
        exportBtn.addActionListener(e -> exportTableToCSV(itemWiseTable, "ItemWiseReport.csv"));
        toolbar.add(exportBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Medicine ID", "Medicine Name", "Type", "Units Sold", "Total Revenue (R)", "Current Stock Remaining"};
        itemWiseModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        itemWiseTable = new JTable(itemWiseModel);
        UITheme.styleTable(itemWiseTable);
        panel.add(new JScrollPane(itemWiseTable), BorderLayout.CENTER);

        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 8));
        summaryBar.setBackground(Color.WHITE);
        summaryBar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        lblItemWiseTotalUnits = new JLabel("Total Units Sold: 0");
        lblItemWiseTotalUnits.setFont(UITheme.FONT_BOLD);
        summaryBar.add(lblItemWiseTotalUnits);

        lblItemWiseTotalRevenue = new JLabel("Catalog Revenue: R 0.00");
        lblItemWiseTotalRevenue.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblItemWiseTotalRevenue.setForeground(UITheme.PRIMARY);
        summaryBar.add(lblItemWiseTotalRevenue);

        panel.add(summaryBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createLowStockReportSubTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        JButton refreshBtn = new JButton("Refresh Low Stock");
        refreshBtn.addActionListener(e -> loadLowStockReport());
        toolbar.add(refreshBtn);

        JButton exportBtn = UITheme.createStyledButton("Export to CSV", UITheme.SUCCESS, Color.WHITE);
        exportBtn.addActionListener(e -> exportTableToCSV(lowStockTable, "LowStockReport.csv"));
        toolbar.add(exportBtn);

        lblLowStockCount = new JLabel("Alert items count: 0");
        lblLowStockCount.setFont(UITheme.FONT_BOLD);
        lblLowStockCount.setForeground(UITheme.DANGER);
        toolbar.add(lblLowStockCount);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Medicine Name", "Company", "Type", "Price (R)", "Current Stock", "Reorder Level", "Deficit", "Supplier Name"};
        lowStockModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        lowStockTable = new JTable(lowStockModel);
        UITheme.styleTable(lowStockTable);
        panel.add(new JScrollPane(lowStockTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createExpiryReportSubTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        JButton refreshBtn = new JButton("Refresh Expiry Report");
        refreshBtn.addActionListener(e -> loadExpiryReport());
        toolbar.add(refreshBtn);

        JButton exportBtn = UITheme.createStyledButton("Export to CSV", UITheme.SUCCESS, Color.WHITE);
        exportBtn.addActionListener(e -> exportTableToCSV(expiryTable, "ExpiryReport.csv"));
        toolbar.add(exportBtn);

        lblExpiryCount = new JLabel("Medicines expiring in next 30 days: 0");
        lblExpiryCount.setFont(UITheme.FONT_BOLD);
        lblExpiryCount.setForeground(UITheme.DANGER);
        toolbar.add(lblExpiryCount);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Medicine Name", "Company", "Type", "Stock on Hand", "Expiry Date", "Days Remaining", "Status", "Supplier Name"};
        expiryModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        expiryTable = new JTable(expiryModel);
        UITheme.styleTable(expiryTable);
        panel.add(new JScrollPane(expiryTable), BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // DATA LOADERS & CRUD OPERATIONS
    // =========================================================================
    public void refreshAllData() {
        loadSuppliersCombo();
        loadMedicinesTable();
        loadSuppliersTable();
        loadUsersTable();
        loadSalesReport();
        loadItemWiseReport();
        loadLowStockReport();
        loadExpiryReport();
        updateKpiCounters();
    }

    private void updateKpiCounters() {
        try {
            List<Medicine> meds = medicineDAO.getAllMedicines();
            kpiTotalMeds.setText(String.valueOf(meds.size()));

            List<Medicine> low = medicineDAO.getLowStockMedicines();
            kpiLowStock.setText(String.valueOf(low.size()));

            List<Medicine> exp = medicineDAO.getExpiringMedicines(30);
            kpiExpiring.setText(String.valueOf(exp.size()));

            List<Sale> sales = saleDAO.getSalesReport(null, null);
            BigDecimal totalRev = BigDecimal.ZERO;
            for (Sale s : sales) {
                totalRev = totalRev.add(s.getTotalAmount());
            }
            kpiTotalSales.setText("R " + String.format("%.2f", totalRev));
        } catch (SQLException ignored) {}
    }

    // --- MEDICINES LOGIC ---
    private void loadSuppliersCombo() {
        try {
            List<Supplier> list = supplierDAO.getAllSuppliers();
            medSupplierCombo.removeAllItems();
            for (Supplier s : list) {
                medSupplierCombo.addItem(s);
            }
        } catch (SQLException e) {
            System.err.println("Error loading suppliers dropdown: " + e.getMessage());
        }
    }

    private void loadMedicinesTable() {
        filterMedicinesTable();
    }

    private void filterMedicinesTable() {
        String keyword = medSearchField != null ? medSearchField.getText().trim() : "";
        String typeFilter = (medFilterTypeCombo != null && medFilterTypeCombo.getSelectedItem() != null)
                ? medFilterTypeCombo.getSelectedItem().toString() : "All";
        try {
            List<Medicine> list = medicineDAO.searchMedicines(keyword, typeFilter);
            medTableModel.setRowCount(0);
            for (Medicine m : list) {
                medTableModel.addRow(new Object[]{
                        m.getMedicineId(),
                        m.getName(),
                        m.getCompany(),
                        m.getMedicineType(),
                        String.format("%.2f", m.getPrice()),
                        m.getQuantityInStock(),
                        m.getReorderLevel(),
                        m.getExpiryDate() != null ? m.getExpiryDate().toString() : "-",
                        m.getSupplierName() != null ? m.getSupplierName() : "-"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching medicines: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onMedicineTableRowSelected() {
        int row = medTable.getSelectedRow();
        if (row < 0) return;

        int id = (int) medTableModel.getValueAt(row, 0);
        try {
            Medicine m = medicineDAO.getMedicineById(id);
            if (m != null) {
                medIdField.setText(String.valueOf(m.getMedicineId()));
                medNameField.setText(m.getName());
                medCompanyField.setText(m.getCompany());
                medTypeCombo.setSelectedItem(m.getMedicineType());
                medPriceField.setText(m.getPrice().toString());
                medStockField.setText(String.valueOf(m.getQuantityInStock()));
                medReorderField.setText(String.valueOf(m.getReorderLevel()));
                medExpiryField.setText(m.getExpiryDate() != null ? m.getExpiryDate().toString() : "");

                for (int i = 0; i < medSupplierCombo.getItemCount(); i++) {
                    Supplier s = medSupplierCombo.getItemAt(i);
                    if (s.getSupplierId() == m.getSupplierId()) {
                        medSupplierCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving medicine: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addMedicine() {
        Medicine m = validateAndBuildMedicine(false);
        if (m == null) return;
        try {
            if (medicineDAO.addMedicine(m)) {
                JOptionPane.showMessageDialog(this, "Medicine added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearMedicineForm();
                refreshAllData();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding medicine: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMedicine() {
        if (medIdField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a medicine from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Medicine m = validateAndBuildMedicine(true);
        if (m == null) return;
        try {
            if (medicineDAO.updateMedicine(m)) {
                JOptionPane.showMessageDialog(this, "Medicine updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearMedicineForm();
                refreshAllData();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating medicine: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMedicine() {
        String idStr = medIdField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a medicine from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(idStr);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete medicine: " + medNameField.getText() + "?\n" +
                "Note: Cannot delete if sales records reference this item.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (medicineDAO.deleteMedicine(id)) {
                    JOptionPane.showMessageDialog(this, "Medicine deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearMedicineForm();
                    refreshAllData();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete medicine: " + e.getMessage() + "\nIt may be linked to previous sales records.",
                        "Foreign Key Constraint", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Medicine validateAndBuildMedicine(boolean isUpdate) {
        String name = medNameField.getText().trim();
        String company = medCompanyField.getText().trim();
        String type = (String) medTypeCombo.getSelectedItem();
        String priceStr = medPriceField.getText().trim();
        String stockStr = medStockField.getText().trim();
        String reorderStr = medReorderField.getText().trim();
        String expiryStr = medExpiryField.getText().trim();
        Supplier sup = (Supplier) medSupplierCombo.getSelectedItem();

        if (name.isEmpty() || company.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || reorderStr.isEmpty() || expiryStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all mandatory fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        if (sup == null) {
            JOptionPane.showMessageDialog(this, "Please select or create a supplier first.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        BigDecimal price;
        int stock;
        int reorder;
        LocalDate expiryDate;

        try {
            price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive price.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Stock quantity must be a non-negative integer.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            reorder = Integer.parseInt(reorderStr);
            if (reorder < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Reorder level must be a non-negative integer.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            expiryDate = LocalDate.parse(expiryStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid expiry date format. Please use YYYY-MM-DD (e.g. 2026-12-31).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        Medicine m = new Medicine();
        if (isUpdate) {
            m.setMedicineId(Integer.parseInt(medIdField.getText().trim()));
        }
        m.setName(name);
        m.setCompany(company);
        m.setMedicineType(type);
        m.setPrice(price);
        m.setQuantityInStock(stock);
        m.setReorderLevel(reorder);
        m.setExpiryDate(expiryDate);
        m.setSupplierId(sup.getSupplierId());
        return m;
    }

    private void clearMedicineForm() {
        medIdField.setText("");
        medNameField.setText("");
        medCompanyField.setText("");
        if (medTypeCombo.getItemCount() > 0) medTypeCombo.setSelectedIndex(0);
        medPriceField.setText("");
        medStockField.setText("");
        medReorderField.setText("");
        medExpiryField.setText("");
        if (medSupplierCombo.getItemCount() > 0) medSupplierCombo.setSelectedIndex(0);
        medTable.clearSelection();
    }

    // --- SUPPLIERS LOGIC ---
    private void loadSuppliersTable() {
        filterSuppliersTable();
    }

    private void filterSuppliersTable() {
        String keyword = supSearchField != null ? supSearchField.getText().trim() : "";
        try {
            List<Supplier> list = keyword.isEmpty() ? supplierDAO.getAllSuppliers() : supplierDAO.searchSuppliers(keyword);
            supTableModel.setRowCount(0);
            for (Supplier s : list) {
                supTableModel.addRow(new Object[]{
                        s.getSupplierId(),
                        s.getName(),
                        s.getContactPerson(),
                        s.getPhone(),
                        s.getEmail(),
                        s.getAddress()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching suppliers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSupplierTableRowSelected() {
        int row = supTable.getSelectedRow();
        if (row < 0) return;

        int id = (int) supTableModel.getValueAt(row, 0);
        try {
            Supplier s = supplierDAO.getSupplierById(id);
            if (s != null) {
                supIdField.setText(String.valueOf(s.getSupplierId()));
                supNameField.setText(s.getName());
                supContactField.setText(s.getContactPerson());
                supPhoneField.setText(s.getPhone());
                supEmailField.setText(s.getEmail());
                supAddressArea.setText(s.getAddress());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving supplier: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSupplier() {
        String name = supNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Supplier company name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Supplier s = new Supplier(name, supContactField.getText().trim(), supPhoneField.getText().trim(),
                supEmailField.getText().trim(), supAddressArea.getText().trim());

        try {
            if (supplierDAO.addSupplier(s)) {
                JOptionPane.showMessageDialog(this, "Supplier added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearSupplierForm();
                refreshAllData();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding supplier: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSupplier() {
        String idStr = supIdField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = supNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Supplier company name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Supplier s = new Supplier(Integer.parseInt(idStr), name, supContactField.getText().trim(),
                supPhoneField.getText().trim(), supEmailField.getText().trim(), supAddressArea.getText().trim());

        try {
            if (supplierDAO.updateSupplier(s)) {
                JOptionPane.showMessageDialog(this, "Supplier updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearSupplierForm();
                refreshAllData();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating supplier: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSupplier() {
        String idStr = supIdField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(idStr);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete supplier: " + supNameField.getText() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (supplierDAO.deleteSupplier(id)) {
                    JOptionPane.showMessageDialog(this, "Supplier deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearSupplierForm();
                    refreshAllData();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete supplier: " + e.getMessage() + "\nExisting medicines depend on this supplier.",
                        "Constraint Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearSupplierForm() {
        supIdField.setText("");
        supNameField.setText("");
        supContactField.setText("");
        supPhoneField.setText("");
        supEmailField.setText("");
        supAddressArea.setText("");
        supTable.clearSelection();
    }

    // --- USERS LOGIC ---
    private void loadUsersTable() {
        try {
            List<User> list = userDAO.getAllUsers();
            userTableModel.setRowCount(0);
            for (User u : list) {
                userTableModel.addRow(new Object[]{
                        u.getUserId(),
                        u.getUsername(),
                        u.getRole(),
                        u.getFullName()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUserTableRowSelected() {
        int row = userTable.getSelectedRow();
        if (row < 0) return;

        userIdField.setText(userTableModel.getValueAt(row, 0).toString());
        userUsernameField.setText(userTableModel.getValueAt(row, 1).toString());
        userRoleCombo.setSelectedItem(userTableModel.getValueAt(row, 2).toString());
        userFullNameField.setText(userTableModel.getValueAt(row, 3).toString());
        userPasswordField.setText(""); // Keep password blank unless changing
    }

    private void addUser() {
        String uname = userUsernameField.getText().trim();
        String pass = new String(userPasswordField.getPassword());
        String role = (String) userRoleCombo.getSelectedItem();
        String fname = userFullNameField.getText().trim();

        if (uname.isEmpty() || pass.isEmpty() || fname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, password, and full name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (userDAO.isUsernameTaken(uname, 0)) {
                JOptionPane.showMessageDialog(this, "Username '" + uname + "' is already taken. Please pick another.", "Duplicate Username", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User u = new User(uname, pass, role, fname);
            if (userDAO.addUser(u)) {
                JOptionPane.showMessageDialog(this, "User created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearUserForm();
                loadUsersTable();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding user: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUser() {
        String idStr = userIdField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a user to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(idStr);
        String uname = userUsernameField.getText().trim();
        String pass = new String(userPasswordField.getPassword());
        String role = (String) userRoleCombo.getSelectedItem();
        String fname = userFullNameField.getText().trim();

        if (uname.isEmpty() || fname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and full name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (userDAO.isUsernameTaken(uname, id)) {
                JOptionPane.showMessageDialog(this, "Username '" + uname + "' is already taken.", "Duplicate Username", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // If password left blank, retrieve existing
            if (pass.isEmpty()) {
                List<User> all = userDAO.getAllUsers();
                for (User u : all) {
                    if (u.getUserId() == id) {
                        pass = u.getPassword();
                        break;
                    }
                }
            }

            User u = new User(id, uname, pass, role, fname);
            if (userDAO.updateUser(u)) {
                JOptionPane.showMessageDialog(this, "User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearUserForm();
                loadUsersTable();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating user: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUser() {
        String idStr = userIdField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(idStr);
        if (id == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own active administrator account.", "Action Prohibited", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user: " + userUsernameField.getText() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (userDAO.deleteUser(id)) {
                    JOptionPane.showMessageDialog(this, "User account deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearUserForm();
                    loadUsersTable();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete user: " + e.getMessage() + "\nUser is associated with existing transactions.",
                        "Constraint Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearUserForm() {
        userIdField.setText("");
        userUsernameField.setText("");
        userPasswordField.setText("");
        if (userRoleCombo.getItemCount() > 0) userRoleCombo.setSelectedIndex(0);
        userFullNameField.setText("");
        userTable.clearSelection();
    }

    // --- REPORTS LOGIC ---
    private void loadSalesReport() {
        LocalDate from = null;
        LocalDate to = LocalDate.now();
        String filter = salesDateFilter != null ? (String) salesDateFilter.getSelectedItem() : "All Time";

        if ("Today".equalsIgnoreCase(filter)) {
            from = LocalDate.now();
        } else if ("Last 7 Days".equalsIgnoreCase(filter)) {
            from = LocalDate.now().minusDays(7);
        } else if ("This Month".equalsIgnoreCase(filter)) {
            from = LocalDate.now().withDayOfMonth(1);
        }

        try {
            List<Sale> sales = saleDAO.getSalesReport(from, to);
            salesReportModel.setRowCount(0);
            BigDecimal totalRev = BigDecimal.ZERO;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Sale s : sales) {
                totalRev = totalRev.add(s.getTotalAmount());
                salesReportModel.addRow(new Object[]{
                        s.getSaleId(),
                        s.getSaleDate() != null ? s.getSaleDate().format(dtf) : "-",
                        s.getCashierName() != null ? s.getCashierName() : "-",
                        String.format("%.2f", s.getTotalAmount())
                });
            }

            lblSalesReportCount.setText("Transactions: " + sales.size());
            lblSalesReportTotal.setText("Total Revenue: R " + String.format("%.2f", totalRev));
        } catch (SQLException e) {
            System.err.println("Error loading sales report: " + e.getMessage());
        }
    }

    private void loadItemWiseReport() {
        try {
            List<SaleDAO.ItemWiseReportRow> rows = saleDAO.getItemWiseSalesReport();
            itemWiseModel.setRowCount(0);
            int totalUnits = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;

            for (SaleDAO.ItemWiseReportRow r : rows) {
                totalUnits += r.totalSold;
                totalRevenue = totalRevenue.add(r.totalRevenue);

                itemWiseModel.addRow(new Object[]{
                        r.medicineId,
                        r.medicineName,
                        r.medicineType,
                        r.totalSold,
                        String.format("%.2f", r.totalRevenue),
                        r.currentStock
                });
            }

            lblItemWiseTotalUnits.setText("Total Units Sold: " + totalUnits);
            lblItemWiseTotalRevenue.setText("Catalog Revenue: R " + String.format("%.2f", totalRevenue));
        } catch (SQLException e) {
            System.err.println("Error loading item-wise report: " + e.getMessage());
        }
    }

    private void loadLowStockReport() {
        try {
            List<Medicine> list = medicineDAO.getLowStockMedicines();
            lowStockModel.setRowCount(0);

            for (Medicine m : list) {
                int deficit = m.getReorderLevel() - m.getQuantityInStock();
                lowStockModel.addRow(new Object[]{
                        m.getMedicineId(),
                        m.getName(),
                        m.getCompany(),
                        m.getMedicineType(),
                        String.format("%.2f", m.getPrice()),
                        m.getQuantityInStock(),
                        m.getReorderLevel(),
                        deficit > 0 ? ("Deficit of " + deficit) : "At Reorder Level",
                        m.getSupplierName() != null ? m.getSupplierName() : "-"
                });
            }

            lblLowStockCount.setText("Alert items count: " + list.size());
        } catch (SQLException e) {
            System.err.println("Error loading low stock report: " + e.getMessage());
        }
    }

    private void loadExpiryReport() {
        try {
            List<Medicine> list = medicineDAO.getExpiringMedicines(30);
            expiryModel.setRowCount(0);
            LocalDate today = LocalDate.now();

            for (Medicine m : list) {
                long days = ChronoUnit.DAYS.between(today, m.getExpiryDate());
                String status;
                if (days < 0) {
                    status = "EXPIRED (" + Math.abs(days) + " days ago)";
                } else if (days == 0) {
                    status = "EXPIRES TODAY";
                } else {
                    status = "Expiring in " + days + " day(s)";
                }

                expiryModel.addRow(new Object[]{
                        m.getMedicineId(),
                        m.getName(),
                        m.getCompany(),
                        m.getMedicineType(),
                        m.getQuantityInStock(),
                        m.getExpiryDate().toString(),
                        days,
                        status,
                        m.getSupplierName() != null ? m.getSupplierName() : "-"
                });
            }

            lblExpiryCount.setText("Medicines expiring within next 30 days: " + list.size());
        } catch (SQLException e) {
            System.err.println("Error loading expiry report: " + e.getMessage());
        }
    }

    private void exportTableToCSV(JTable table, String defaultFileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultFileName));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(file)) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                // Write Header
                for (int c = 0; c < model.getColumnCount(); c++) {
                    fw.write("\"" + model.getColumnName(c) + "\"" + (c == model.getColumnCount() - 1 ? "" : ","));
                }
                fw.write("\n");
                // Write Rows
                for (int r = 0; r < model.getRowCount(); r++) {
                    for (int c = 0; c < model.getColumnCount(); c++) {
                        Object val = model.getValueAt(r, c);
                        fw.write("\"" + (val != null ? val.toString().replace("\"", "\"\"") : "") + "\"" +
                                (c == model.getColumnCount() - 1 ? "" : ","));
                    }
                    fw.write("\n");
                }
                JOptionPane.showMessageDialog(this, "Report successfully exported to:\n" + file.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to write CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
