package com.healthfirst.dao;

import com.healthfirst.config.DatabaseConnection;
import com.healthfirst.model.Medicine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    public List<Medicine> getAllMedicines() throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String query = "SELECT m.medicine_id, m.name, m.company, m.medicine_type, m.price, " +
                "m.quantity_in_stock, m.reorder_level, m.expiry_date, m.supplier_id, s.name AS supplier_name " +
                "FROM medicines m " +
                "LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id " +
                "ORDER BY m.name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToMedicine(rs));
            }
        }
        return list;
    }

    public Medicine getMedicineById(int medicineId) throws SQLException {
        String query = "SELECT m.medicine_id, m.name, m.company, m.medicine_type, m.price, " +
                "m.quantity_in_stock, m.reorder_level, m.expiry_date, m.supplier_id, s.name AS supplier_name " +
                "FROM medicines m " +
                "LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id " +
                "WHERE m.medicine_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedicine(rs);
                }
            }
        }
        return null;
    }

    public List<Medicine> searchMedicines(String keyword, String typeFilter) throws SQLException {
        List<Medicine> list = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                "SELECT m.medicine_id, m.name, m.company, m.medicine_type, m.price, " +
                "m.quantity_in_stock, m.reorder_level, m.expiry_date, m.supplier_id, s.name AS supplier_name " +
                "FROM medicines m " +
                "LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id WHERE 1=1 "
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append("AND (m.name LIKE ? OR m.company LIKE ? OR s.name LIKE ?) ");
        }
        if (typeFilter != null && !typeFilter.trim().isEmpty() && !typeFilter.equalsIgnoreCase("All")) {
            query.append("AND m.medicine_type = ? ");
        }
        query.append("ORDER BY m.name ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {
            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim() + "%";
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
            }
            if (typeFilter != null && !typeFilter.trim().isEmpty() && !typeFilter.equalsIgnoreCase("All")) {
                ps.setString(paramIndex++, typeFilter.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMedicine(rs));
                }
            }
        }
        return list;
    }

    public boolean addMedicine(Medicine m) throws SQLException {
        String query = "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getCompany());
            ps.setString(3, m.getMedicineType());
            ps.setBigDecimal(4, m.getPrice());
            ps.setInt(5, m.getQuantityInStock());
            ps.setInt(6, m.getReorderLevel());
            ps.setDate(7, Date.valueOf(m.getExpiryDate()));
            ps.setInt(8, m.getSupplierId());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        m.setMedicineId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateMedicine(Medicine m) throws SQLException {
        String query = "UPDATE medicines SET name = ?, company = ?, medicine_type = ?, price = ?, " +
                "quantity_in_stock = ?, reorder_level = ?, expiry_date = ?, supplier_id = ? " +
                "WHERE medicine_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getCompany());
            ps.setString(3, m.getMedicineType());
            ps.setBigDecimal(4, m.getPrice());
            ps.setInt(5, m.getQuantityInStock());
            ps.setInt(6, m.getReorderLevel());
            ps.setDate(7, Date.valueOf(m.getExpiryDate()));
            ps.setInt(8, m.getSupplierId());
            ps.setInt(9, m.getMedicineId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteMedicine(int medicineId) throws SQLException {
        String query = "DELETE FROM medicines WHERE medicine_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, medicineId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Medicine> getLowStockMedicines() throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String query = "SELECT m.medicine_id, m.name, m.company, m.medicine_type, m.price, " +
                "m.quantity_in_stock, m.reorder_level, m.expiry_date, m.supplier_id, s.name AS supplier_name " +
                "FROM medicines m " +
                "LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id " +
                "WHERE m.quantity_in_stock <= m.reorder_level " +
                "ORDER BY m.quantity_in_stock ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToMedicine(rs));
            }
        }
        return list;
    }

    public List<Medicine> getExpiringMedicines(int withinDays) throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String query = "SELECT m.medicine_id, m.name, m.company, m.medicine_type, m.price, " +
                "m.quantity_in_stock, m.reorder_level, m.expiry_date, m.supplier_id, s.name AS supplier_name " +
                "FROM medicines m " +
                "LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id " +
                "WHERE m.expiry_date <= DATE_ADD(CURRENT_DATE, INTERVAL ? DAY) " +
                "ORDER BY m.expiry_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, withinDays);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMedicine(rs));
                }
            }
        }
        return list;
    }

    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        Medicine m = new Medicine(
                rs.getInt("medicine_id"),
                rs.getString("name"),
                rs.getString("company"),
                rs.getString("medicine_type"),
                rs.getBigDecimal("price"),
                rs.getInt("quantity_in_stock"),
                rs.getInt("reorder_level"),
                rs.getDate("expiry_date").toLocalDate(),
                rs.getInt("supplier_id")
        );
        m.setSupplierName(rs.getString("supplier_name"));
        return m;
    }
}
