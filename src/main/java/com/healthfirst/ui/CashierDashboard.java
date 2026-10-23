package com.healthfirst.ui;

import com.healthfirst.dao.MedicineDAO;
import com.healthfirst.dao.SaleDAO;
import com.healthfirst.model.CartItem;
import com.healthfirst.model.Medicine;
import com.healthfirst.model.Sale;
import com.healthfirst.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CashierDashboard extends JFrame {
    private User currentUser;
    private MedicineDAO medicineDAO;
    private SaleDAO saleDAO;

    // POS State
    private List<Medicine> availableMedicines = new ArrayList<>();
    private List<CartItem> cartItems = new ArrayList<>();
    private Medicine selectedMedicine = null;

    // POS UI components
    private JComboBox<Medicine> medicineComboBox;
    private JTextField posSearchField;
    private JLabel lblSelectedPrice;
    private JLabel lblSelectedStock;
    private JLabel lblSelectedType;
    private JLabel lblSelectedExpiry;
    private JSpinner quantitySpinner;
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JLabel lblTotalAmount;
    private JLabel lblTotalItems;
    private JTextField tenderField;
    private JLabel lblChangeDue;

    // Stock Check UI components
    private JTextField stockSearchField;
    private JComboBox<String> stockTypeFilter;
    private DefaultTableModel stockTableModel;
    private JTable stockTable;

    private JTabbedPane tabbedPane;

    public CashierDashboard(User user) {
        this.currentUser = user;
        this.medicineDAO = new MedicineDAO();
        this.saleDAO = new SaleDAO();
        initUI();
        loadMedicines();
        loadStockCheckData();
    }

    private void initUI() {
        setTitle("HealthFirst Pharmacy - Cashier Point of Sale");
        setSize(1100, 750);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JPanel header = UITheme.createHeader(
                "Cashier Portal (POS)",
                "Fast Billing & Medicine Stock Verification",
                currentUser.getFullName(),
                currentUser.getRole(),
                this::logout
        );
        add(header, BorderLayout.NORTH);

        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("🛒 Point of Sale (POS)", createPOSTab());
        tabbedPane.addTab("🔍 Quick Stock Check", createStockCheckTab());

        applyTabStyle(UITheme.isDarkMode());

        add(tabbedPane, BorderLayout.CENTER);

        UITheme.addThemeListener(() -> {
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

        if (tabbedPane != null) {
            tabbedPane.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
            tabbedPane.setUI(new CardTabbedPaneUI(strip, unselected, selected, hover,
                    border, textUnselected, textSelected, accent, new Insets(10, 22, 10, 22)));
            tabbedPane.setBackground(strip);
            tabbedPane.setOpaque(true);
            tabbedPane.revalidate();
            tabbedPane.repaint();
        }
    }

    private JPanel createPOSTab() {
        JPanel posPanel = new JPanel(new BorderLayout(12, 12));
        posPanel.setBackground(UITheme.BG_LIGHT);
        posPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // LEFT PANEL: Product Selection & Live Preview
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setPreferredSize(new Dimension(420, 0));
        leftPanel.setOpaque(false);

        // Card 1: Medicine Selector
        JPanel selectorCard = new JPanel(new GridBagLayout());
        selectorCard.setBackground(Color.WHITE);
        selectorCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel selTitle = new JLabel("Select Medicine to Add");
        selTitle.setFont(UITheme.FONT_HEADER);
        selTitle.setForeground(UITheme.PRIMARY);
        selectorCard.add(selTitle, gbc);

        // Search box
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.3;
        JLabel lblSearch = new JLabel("Filter Medicines:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        selectorCard.add(lblSearch, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        posSearchField = new JTextField();
        posSearchField.putClientProperty("JTextField.placeholderText", "Type medicine name...");
        posSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterPosMedicines(posSearchField.getText().trim());
            }
        });
        selectorCard.add(posSearchField, gbc);

        // Medicine Dropdown
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lblMed = new JLabel("Medicine:");
        lblMed.setFont(UITheme.FONT_BOLD);
        selectorCard.add(lblMed, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        medicineComboBox = new JComboBox<>();
        medicineComboBox.setFont(UITheme.FONT_BODY);
        medicineComboBox.addActionListener(e -> onMedicineSelected());
        selectorCard.add(medicineComboBox, gbc);

        // Details Preview Panel
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel detailsBox = new JPanel(new GridLayout(4, 2, 8, 6));
        detailsBox.setBackground(new Color(248, 250, 252));
        detailsBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 235, 240), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        detailsBox.add(new JLabel("Type:"));
        lblSelectedType = new JLabel("-");
        lblSelectedType.setFont(UITheme.FONT_BOLD);
        detailsBox.add(lblSelectedType);

        detailsBox.add(new JLabel("Unit Price:"));
        lblSelectedPrice = new JLabel("R 0.00");
        lblSelectedPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelectedPrice.setForeground(UITheme.PRIMARY);
        detailsBox.add(lblSelectedPrice);

        detailsBox.add(new JLabel("In Stock:"));
        lblSelectedStock = new JLabel("-");
        lblSelectedStock.setFont(UITheme.FONT_BOLD);
        detailsBox.add(lblSelectedStock);

        detailsBox.add(new JLabel("Expiry Date:"));
        lblSelectedExpiry = new JLabel("-");
        lblSelectedExpiry.setFont(UITheme.FONT_BOLD);
        detailsBox.add(lblSelectedExpiry);

        selectorCard.add(detailsBox, gbc);

        // Quantity & Add Button
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel lblQty = new JLabel("Quantity:");
        lblQty.setFont(UITheme.FONT_BOLD);
        selectorCard.add(lblQty, gbc);

        gbc.gridx = 1;
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        quantitySpinner.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectorCard.add(quantitySpinner, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JButton addBtn = UITheme.createStyledButton("➕ Add to Cart", UITheme.PRIMARY_LIGHT, Color.WHITE);
        addBtn.addActionListener(e -> addToCart());
        selectorCard.add(addBtn, gbc);

        leftPanel.add(selectorCard, BorderLayout.NORTH);

        // Simulation Info note
        JPanel notePanel = new JPanel(new BorderLayout());
        notePanel.setBackground(new Color(235, 245, 255));
        notePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 220, 245), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel noteHeader = new JLabel("ℹ POS Simulation Mode");
        noteHeader.setFont(UITheme.FONT_BOLD);
        noteHeader.setForeground(UITheme.PRIMARY);
        notePanel.add(noteHeader, BorderLayout.NORTH);

        JLabel noteBody = new JLabel("<html>This screen simulates the cashier register. Completing checkout atomically updates database stock levels and prints or exports an official customer bill.</html>");
        noteBody.setFont(UITheme.FONT_SMALL);
        notePanel.add(noteBody, BorderLayout.CENTER);

        leftPanel.add(notePanel, BorderLayout.CENTER);
        posPanel.add(leftPanel, BorderLayout.WEST);

        // RIGHT PANEL: Cart Table & Checkout
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setOpaque(false);

        // Cart Table Card
        String[] cols = {"#", "Medicine Name", "Type", "Unit Price", "Qty", "Subtotal", "Action"};
        cartTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        cartTable = new JTable(cartTableModel);
        UITheme.styleTable(cartTable);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(190);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        cartTable.getColumnModel().getColumn(6).setPreferredWidth(70);

        JScrollPane cartScroll = new JScrollPane(cartTable);
        JPanel tableCard = UITheme.createCard("Current Transaction Cart", cartScroll);
        rightPanel.add(tableCard, BorderLayout.CENTER);

        // Checkout / Summary Footer
        JPanel checkoutPanel = new JPanel(new BorderLayout(10, 0));
        checkoutPanel.setBackground(Color.WHITE);
        checkoutPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Left summary
        JPanel summaryLeft = new JPanel(new GridLayout(2, 2, 8, 4));
        summaryLeft.setOpaque(false);
        summaryLeft.add(new JLabel("Items in Cart:"));
        lblTotalItems = new JLabel("0 items");
        lblTotalItems.setFont(UITheme.FONT_BOLD);
        summaryLeft.add(lblTotalItems);

        summaryLeft.add(new JLabel("Grand Total:"));
        lblTotalAmount = new JLabel("R 0.00");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalAmount.setForeground(UITheme.PRIMARY);
        summaryLeft.add(lblTotalAmount);

        checkoutPanel.add(summaryLeft, BorderLayout.WEST);

        // Middle Tender / Change
        JPanel tenderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        tenderPanel.setOpaque(false);
        tenderPanel.add(new JLabel("Amount Tendered (R):"));
        tenderField = new JTextField(7);
        tenderField.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tenderField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateChange();
            }
        });
        tenderPanel.add(tenderField);

        tenderPanel.add(new JLabel("Change:"));
        lblChangeDue = new JLabel("R 0.00");
        lblChangeDue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblChangeDue.setForeground(UITheme.SUCCESS);
        tenderPanel.add(lblChangeDue);

        checkoutPanel.add(tenderPanel, BorderLayout.CENTER);

        // Right Action Buttons
        JPanel actionBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtnPanel.setOpaque(false);

        JButton removeItemBtn = new JButton("Remove Item");
        removeItemBtn.setFont(UITheme.FONT_BOLD);
        removeItemBtn.addActionListener(e -> removeSelectedCartItem());

        JButton clearCartBtn = UITheme.createStyledButton("Clear Cart", new Color(130, 140, 150), Color.WHITE);
        clearCartBtn.addActionListener(e -> clearCart());

        JButton checkoutBtn = UITheme.createStyledButton("💳 Checkout & Bill", UITheme.SUCCESS, Color.WHITE);
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        checkoutBtn.addActionListener(e -> performCheckout());

        actionBtnPanel.add(removeItemBtn);
        actionBtnPanel.add(clearCartBtn);
        actionBtnPanel.add(checkoutBtn);

        checkoutPanel.add(actionBtnPanel, BorderLayout.EAST);
        rightPanel.add(checkoutPanel, BorderLayout.SOUTH);

        posPanel.add(rightPanel, BorderLayout.CENTER);
        return posPanel;
    }

    private JPanel createStockCheckTab() {
        JPanel stockPanel = new JPanel(new BorderLayout(10, 10));
        stockPanel.setBackground(UITheme.BG_LIGHT);
        stockPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Filter / Search Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        toolbar.add(new JLabel("Search Medicine / Brand:"));
        stockSearchField = new JTextField(18);
        stockSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadStockCheckData();
            }
        });
        toolbar.add(stockSearchField);

        toolbar.add(new JLabel("Medicine Type:"));
        stockTypeFilter = new JComboBox<>(new String[]{"All", "Tablet", "Capsule", "Syrup", "Injection", "Cream"});
        stockTypeFilter.addActionListener(e -> loadStockCheckData());
        toolbar.add(stockTypeFilter);

        JButton refreshBtn = UITheme.createStyledButton("🔄 Refresh Stock", UITheme.PRIMARY_LIGHT, Color.WHITE);
        refreshBtn.addActionListener(e -> {
            loadStockCheckData();
            loadMedicines();
        });
        toolbar.add(refreshBtn);

        JLabel infoNote = new JLabel(" (Read-only view: Cashiers cannot modify medicine catalog)");
        infoNote.setFont(UITheme.FONT_SMALL);
        infoNote.setForeground(UITheme.TEXT_MUTED);
        toolbar.add(infoNote);

        stockPanel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Medicine Name", "Manufacturer / Company", "Type", "Price (R)", "Stock Available", "Reorder Level", "Status", "Expiry Date", "Supplier"};
        stockTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        stockTable = new JTable(stockTableModel);
        UITheme.styleTable(stockTable);
        stockTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        stockTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        stockTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        stockTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        stockTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        stockTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        stockTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        stockTable.getColumnModel().getColumn(7).setPreferredWidth(95);
        stockTable.getColumnModel().getColumn(8).setPreferredWidth(90);
        stockTable.getColumnModel().getColumn(9).setPreferredWidth(140);

        stockPanel.add(new JScrollPane(stockTable), BorderLayout.CENTER);
        return stockPanel;
    }

    private void loadMedicines() {
        try {
            availableMedicines = medicineDAO.getAllMedicines();
            filterPosMedicines(posSearchField != null ? posSearchField.getText().trim() : "");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching medicines: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterPosMedicines(String query) {
        if (medicineComboBox == null) return;
        medicineComboBox.removeAllItems();
        for (Medicine m : availableMedicines) {
            if (query.isEmpty() || m.getName().toLowerCase().contains(query.toLowerCase()) ||
                    m.getCompany().toLowerCase().contains(query.toLowerCase())) {
                medicineComboBox.addItem(m);
            }
        }
        if (medicineComboBox.getItemCount() > 0) {
            medicineComboBox.setSelectedIndex(0);
        } else {
            selectedMedicine = null;
            updateMedicinePreview();
        }
    }

    private void onMedicineSelected() {
        selectedMedicine = (Medicine) medicineComboBox.getSelectedItem();
        updateMedicinePreview();
    }

    private void updateMedicinePreview() {
        if (selectedMedicine == null) {
            lblSelectedType.setText("-");
            lblSelectedPrice.setText("R 0.00");
            lblSelectedStock.setText("-");
            lblSelectedExpiry.setText("-");
            return;
        }

        lblSelectedType.setText(selectedMedicine.getMedicineType());
        lblSelectedPrice.setText("R " + String.format("%.2f", selectedMedicine.getPrice()));

        int stock = selectedMedicine.getQuantityInStock();
        if (stock <= 0) {
            lblSelectedStock.setForeground(UITheme.DANGER);
            lblSelectedStock.setText("0 (OUT OF STOCK)");
        } else if (stock <= selectedMedicine.getReorderLevel()) {
            lblSelectedStock.setForeground(UITheme.WARNING);
            lblSelectedStock.setText(stock + " units (LOW STOCK)");
        } else {
            lblSelectedStock.setForeground(UITheme.SUCCESS);
            lblSelectedStock.setText(stock + " units in stock");
        }

        lblSelectedExpiry.setText(selectedMedicine.getExpiryDate() != null ? selectedMedicine.getExpiryDate().toString() : "N/A");
    }

    private void addToCart() {
        if (selectedMedicine == null) {
            JOptionPane.showMessageDialog(this, "Please select a medicine first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty = (int) quantitySpinner.getValue();
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be at least 1.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if already in cart
        int alreadyInCart = 0;
        CartItem existingItem = null;
        for (CartItem item : cartItems) {
            if (item.getMedicine().getMedicineId() == selectedMedicine.getMedicineId()) {
                alreadyInCart = item.getQuantity();
                existingItem = item;
                break;
            }
        }

        int totalRequested = alreadyInCart + qty;
        if (totalRequested > selectedMedicine.getQuantityInStock()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot add " + qty + " item(s).\n" +
                    "Current stock: " + selectedMedicine.getQuantityInStock() + "\n" +
                    "Already in cart: " + alreadyInCart + "\n" +
                    "Available to add: " + (selectedMedicine.getQuantityInStock() - alreadyInCart),
                    "Insufficient Stock",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (existingItem != null) {
            existingItem.setQuantity(totalRequested);
        } else {
            cartItems.add(new CartItem(selectedMedicine, qty));
        }

        refreshCartTable();
        quantitySpinner.setValue(1);
    }

    private void removeSelectedCartItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0 || row >= cartItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item from the cart to remove.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        cartItems.remove(row);
        refreshCartTable();
    }

    private void clearCart() {
        if (cartItems.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all items in the cart?", "Clear Cart", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            cartItems.clear();
            refreshCartTable();
        }
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        BigDecimal grandTotal = BigDecimal.ZERO;
        int totalUnits = 0;

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            BigDecimal subtotal = item.getSubtotal();
            grandTotal = grandTotal.add(subtotal);
            totalUnits += item.getQuantity();

            cartTableModel.addRow(new Object[]{
                    (i + 1),
                    item.getMedicine().getName(),
                    item.getMedicine().getMedicineType(),
                    "R " + String.format("%.2f", item.getUnitPrice()),
                    item.getQuantity(),
                    "R " + String.format("%.2f", subtotal),
                    "Remove"
            });
        }

        lblTotalItems.setText(totalUnits + " units (" + cartItems.size() + " items)");
        lblTotalAmount.setText("R " + String.format("%.2f", grandTotal));
        calculateChange();
    }

    private BigDecimal getGrandTotal() {
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            grandTotal = grandTotal.add(item.getSubtotal());
        }
        return grandTotal;
    }

    private void calculateChange() {
        try {
            String text = tenderField.getText().trim();
            if (text.isEmpty()) {
                lblChangeDue.setText("R 0.00");
                return;
            }
            BigDecimal tender = new BigDecimal(text);
            BigDecimal total = getGrandTotal();
            BigDecimal change = tender.subtract(total);
            if (change.compareTo(BigDecimal.ZERO) >= 0) {
                lblChangeDue.setForeground(UITheme.SUCCESS);
                lblChangeDue.setText("R " + String.format("%.2f", change));
            } else {
                lblChangeDue.setForeground(UITheme.DANGER);
                lblChangeDue.setText("- R " + String.format("%.2f", change.abs()) + " (Short)");
            }
        } catch (NumberFormatException e) {
            lblChangeDue.setText("Invalid amount");
        }
    }

    private void performCheckout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The cart is empty. Please add items before checking out.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal total = getGrandTotal();
        BigDecimal tender = total; // Default tender to exact amount if field empty
        String tenderText = tenderField.getText().trim();
        if (!tenderText.isEmpty()) {
            try {
                tender = new BigDecimal(tenderText);
                if (tender.compareTo(total) < 0) {
                    JOptionPane.showMessageDialog(this,
                            "Tendered amount (R " + tender + ") is less than the Grand Total (R " + total + ").",
                            "Insufficient Payment", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric payment amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        BigDecimal change = tender.subtract(total);

        // Process in database transaction
        Sale sale = new Sale();
        sale.setTotalAmount(total);
        sale.setUserId(currentUser.getUserId());
        sale.setCashierName(currentUser.getFullName());

        try {
            boolean success = saleDAO.processSale(sale, cartItems);
            if (success) {
                // Open Bill / Receipt window
                BillDialog billDialog = new BillDialog(this, sale, new ArrayList<>(cartItems), tender, change);
                billDialog.setVisible(true);

                // Reset Cart & reload stock data
                cartItems.clear();
                tenderField.setText("");
                refreshCartTable();
                loadMedicines();
                loadStockCheckData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to process sale transaction:\n" + ex.getMessage(),
                    "Transaction Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadStockCheckData() {
        if (stockTableModel == null) return;
        String keyword = stockSearchField != null ? stockSearchField.getText().trim() : "";
        String typeFilter = (stockTypeFilter != null && stockTypeFilter.getSelectedItem() != null)
                ? stockTypeFilter.getSelectedItem().toString() : "All";

        try {
            List<Medicine> list = medicineDAO.searchMedicines(keyword, typeFilter);
            stockTableModel.setRowCount(0);

            for (Medicine m : list) {
                String status;
                if (m.getQuantityInStock() <= 0) {
                    status = "Out of Stock";
                } else if (m.isLowStock()) {
                    status = "Low Stock (" + m.getQuantityInStock() + ")";
                } else {
                    status = "Adequate";
                }

                stockTableModel.addRow(new Object[]{
                        m.getMedicineId(),
                        m.getName(),
                        m.getCompany(),
                        m.getMedicineType(),
                        String.format("%.2f", m.getPrice()),
                        m.getQuantityInStock(),
                        m.getReorderLevel(),
                        status,
                        m.getExpiryDate() != null ? m.getExpiryDate().toString() : "-",
                        m.getSupplierName() != null ? m.getSupplierName() : "-"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading stock check: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
