package com.healthfirst.dao;

import com.healthfirst.config.DatabaseConnection;
import com.healthfirst.model.CartItem;
import com.healthfirst.model.Sale;
import com.healthfirst.model.SaleItem;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleDAO {

    /**
     * Executes the sale atomically in a transaction:
     * 1. Validates available stock for each medicine
     * 2. Inserts sales record
     * 3. Inserts sale_items records
     * 4. Deducts stock from medicines table
     */
    public boolean processSale(Sale sale, List<CartItem> cartItems) throws SQLException {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Step 1: Verify and lock current stock
            String stockCheckSql = "SELECT name, quantity_in_stock FROM medicines WHERE medicine_id = ? FOR UPDATE";
            try (PreparedStatement checkPs = conn.prepareStatement(stockCheckSql)) {
                for (CartItem item : cartItems) {
                    checkPs.setInt(1, item.getMedicine().getMedicineId());
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            throw new SQLException("Medicine not found in inventory: ID " + item.getMedicine().getMedicineId());
                        }
                        int currentStock = rs.getInt("quantity_in_stock");
                        String medName = rs.getString("name");
                        if (currentStock < item.getQuantity()) {
                            conn.rollback();
                            throw new SQLException("Insufficient stock for '" + medName +
                                    "'. In stock: " + currentStock + ", Requested: " + item.getQuantity());
                        }
                    }
                }
            }

            // Step 2: Insert into sales header table
            String insertSaleSql = "INSERT INTO sales (sale_date, total_amount, user_id) VALUES (CURRENT_TIMESTAMP, ?, ?)";
            int saleId;
            try (PreparedStatement salePs = conn.prepareStatement(insertSaleSql, Statement.RETURN_GENERATED_KEYS)) {
                salePs.setBigDecimal(1, sale.getTotalAmount());
                salePs.setInt(2, sale.getUserId());
                salePs.executeUpdate();

                try (ResultSet keys = salePs.getGeneratedKeys()) {
                    if (keys.next()) {
                        saleId = keys.getInt(1);
                        sale.setSaleId(saleId);
                    } else {
                        conn.rollback();
                        throw new SQLException("Failed to retrieve generated sale_id.");
                    }
                }
            }

            // Step 3: Insert line items and update medicine stock
            String insertItemSql = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";
            String deductStockSql = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id = ?";

            try (PreparedStatement itemPs = conn.prepareStatement(insertItemSql);
                 PreparedStatement stockPs = conn.prepareStatement(deductStockSql)) {

                for (CartItem item : cartItems) {
                    // Line item
                    itemPs.setInt(1, saleId);
                    itemPs.setInt(2, item.getMedicine().getMedicineId());
                    itemPs.setInt(3, item.getQuantity());
                    itemPs.setBigDecimal(4, item.getUnitPrice());
                    itemPs.addBatch();

                    // Deduct stock
                    stockPs.setInt(1, item.getQuantity());
                    stockPs.setInt(2, item.getMedicine().getMedicineId());
                    stockPs.addBatch();
                }

                itemPs.executeBatch();
                stockPs.executeBatch();
            }

            conn.commit(); // Transaction successful!
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Failed to close connection: " + closeEx.getMessage());
                }
            }
        }
    }

    public List<Sale> getSalesReport(LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Sale> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT s.sale_id, s.sale_date, s.total_amount, s.user_id, u.full_name AS cashier_name " +
                "FROM sales s " +
                "LEFT JOIN users u ON s.user_id = u.user_id WHERE 1=1 "
        );

        if (fromDate != null) {
            sql.append("AND DATE(s.sale_date) >= ? ");
        }
        if (toDate != null) {
            sql.append("AND DATE(s.sale_date) <= ? ");
        }
        sql.append("ORDER BY s.sale_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (fromDate != null) {
                ps.setDate(idx++, Date.valueOf(fromDate));
            }
            if (toDate != null) {
                ps.setDate(idx++, Date.valueOf(toDate));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale(
                            rs.getInt("sale_id"),
                            rs.getTimestamp("sale_date").toLocalDateTime(),
                            rs.getBigDecimal("total_amount"),
                            rs.getInt("user_id")
                    );
                    sale.setCashierName(rs.getString("cashier_name"));
                    list.add(sale);
                }
            }
        }
        return list;
    }

    public List<SaleItem> getSaleItemsBySaleId(int saleId) throws SQLException {
        List<SaleItem> list = new ArrayList<>();
        String sql = "SELECT si.sale_item_id, si.sale_id, si.medicine_id, si.quantity_sold, si.price_at_sale, " +
                "m.name AS medicine_name " +
                "FROM sale_items si " +
                "LEFT JOIN medicines m ON si.medicine_id = m.medicine_id " +
                "WHERE si.sale_id = ? ORDER BY si.sale_item_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem(
                            rs.getInt("sale_item_id"),
                            rs.getInt("sale_id"),
                            rs.getInt("medicine_id"),
                            rs.getInt("quantity_sold"),
                            rs.getBigDecimal("price_at_sale")
                    );
                    item.setMedicineName(rs.getString("medicine_name"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    public static class ItemWiseReportRow {
        public int medicineId;
        public String medicineName;
        public String medicineType;
        public int totalSold;
        public BigDecimal totalRevenue;
        public int currentStock;
    }

    public List<ItemWiseReportRow> getItemWiseSalesReport() throws SQLException {
        List<ItemWiseReportRow> list = new ArrayList<>();
        String sql = "SELECT m.medicine_id, m.name, m.medicine_type, m.quantity_in_stock, " +
                "COALESCE(SUM(si.quantity_sold), 0) AS total_sold, " +
                "COALESCE(SUM(si.quantity_sold * si.price_at_sale), 0.00) AS total_revenue " +
                "FROM medicines m " +
                "LEFT JOIN sale_items si ON m.medicine_id = si.medicine_id " +
                "GROUP BY m.medicine_id, m.name, m.medicine_type, m.quantity_in_stock " +
                "ORDER BY total_sold DESC, total_revenue DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ItemWiseReportRow row = new ItemWiseReportRow();
                row.medicineId = rs.getInt("medicine_id");
                row.medicineName = rs.getString("name");
                row.medicineType = rs.getString("medicine_type");
                row.currentStock = rs.getInt("quantity_in_stock");
                row.totalSold = rs.getInt("total_sold");
                row.totalRevenue = rs.getBigDecimal("total_revenue");
                list.add(row);
            }
        }
        return list;
    }
}
