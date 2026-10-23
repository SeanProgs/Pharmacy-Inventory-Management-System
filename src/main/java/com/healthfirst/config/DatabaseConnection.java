package com.healthfirst.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {
    private static final String CONFIG_FILE = "db.properties";
    private static Properties properties = new Properties();

    private static String host = "localhost";
    private static String port = "3306";
    private static String database = "pims_db";
    private static String user = "root";
    private static String password = ""; // Default root password commonly empty or 'root'

    static {
        loadConfig();
    }

    public static synchronized void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                properties.load(in);
                host = properties.getProperty("db.host", "localhost");
                port = properties.getProperty("db.port", "3306");
                database = properties.getProperty("db.name", "pims_db");
                user = properties.getProperty("db.user", "root");
                password = properties.getProperty("db.password", "");
            } catch (IOException e) {
                System.err.println("Could not load db.properties, using defaults: " + e.getMessage());
            }
        } else {
            saveConfig(host, port, database, user, password);
        }
    }

    public static synchronized void saveConfig(String newHost, String newPort, String newDb, String newUser, String newPass) {
        host = newHost;
        port = newPort;
        database = newDb;
        user = newUser;
        password = newPass;

        properties.setProperty("db.host", host);
        properties.setProperty("db.port", port);
        properties.setProperty("db.name", database);
        properties.setProperty("db.user", user);
        properties.setProperty("db.password", password);

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            properties.store(out, "HealthFirst PIMS Database Configuration");
        } catch (IOException e) {
            System.err.println("Could not save db.properties: " + e.getMessage());
        }
    }

    public static String getJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true";
    }

    public static String getServerJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port +
                "/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found on classpath: " + e.getMessage(), e);
        }
        return DriverManager.getConnection(getJdbcUrl(), user, password);
    }

    public static boolean testConnection(String tHost, String tPort, String tDb, String tUser, String tPass) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found: " + e.getMessage());
        }
        String url = "jdbc:mysql://" + tHost + ":" + tPort + "/" + tDb +
                "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
        try (Connection conn = DriverManager.getConnection(url, tUser, tPass)) {
            return conn != null && !conn.isClosed();
        }
    }

    /**
     * Initializes database tables and default sample data if not already present.
     */
    public static void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // First connect to the server and ensure the database exists
            try (Connection conn = DriverManager.getConnection(getServerJdbcUrl(), user, password);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + database + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            } catch (SQLException ex) {
                // Ignore if database creation from server fails (e.g. user lacks global CREATE privilege)
            }

            // Now connect to the database and create tables
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                // 1. users table
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "  user_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  username VARCHAR(50) NOT NULL UNIQUE," +
                    "  password VARCHAR(255) NOT NULL," +
                    "  role ENUM('Admin', 'Cashier') NOT NULL," +
                    "  full_name VARCHAR(100) NOT NULL," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB"
                );

                // 2. suppliers table
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS suppliers (" +
                    "  supplier_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  name VARCHAR(100) NOT NULL," +
                    "  contact_person VARCHAR(100)," +
                    "  phone VARCHAR(20)," +
                    "  email VARCHAR(100)," +
                    "  address TEXT," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB"
                );

                // 3. medicines table
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS medicines (" +
                    "  medicine_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  name VARCHAR(150) NOT NULL," +
                    "  company VARCHAR(100) NOT NULL," +
                    "  medicine_type VARCHAR(50) NOT NULL," +
                    "  price DECIMAL(10, 2) NOT NULL," +
                    "  quantity_in_stock INT NOT NULL DEFAULT 0," +
                    "  reorder_level INT NOT NULL DEFAULT 10," +
                    "  expiry_date DATE NOT NULL," +
                    "  supplier_id INT NOT NULL," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  CONSTRAINT fk_medicines_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) " +
                    "    ON DELETE RESTRICT ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB"
                );

                // 4. sales table
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS sales (" +
                    "  sale_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                    "  total_amount DECIMAL(10, 2) NOT NULL," +
                    "  user_id INT NOT NULL," +
                    "  CONSTRAINT fk_sales_user FOREIGN KEY (user_id) REFERENCES users(user_id) " +
                    "    ON DELETE RESTRICT ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB"
                );

                // 5. sale_items table
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS sale_items (" +
                    "  sale_item_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  sale_id INT NOT NULL," +
                    "  medicine_id INT NOT NULL," +
                    "  quantity_sold INT NOT NULL," +
                    "  price_at_sale DECIMAL(10, 2) NOT NULL," +
                    "  CONSTRAINT fk_sale_items_sale FOREIGN KEY (sale_id) REFERENCES sales(sale_id) " +
                    "    ON DELETE CASCADE ON UPDATE CASCADE," +
                    "  CONSTRAINT fk_sale_items_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) " +
                    "    ON DELETE RESTRICT ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB"
                );

                // Seed admin if users table is empty
                var rsUser = stmt.executeQuery("SELECT COUNT(*) FROM users");
                if (rsUser.next() && rsUser.getInt(1) == 0) {
                    stmt.executeUpdate(
                        "INSERT INTO users (username, password, role, full_name) VALUES " +
                        "('admin', 'admin123', 'Admin', 'Emmanuel Komape (Admin)')," +
                        "('cashier', 'cash123', 'Cashier', 'Jane Doe (Senior Cashier)')," +
                        "('cashier2', 'cash123', 'Cashier', 'John Smith (Cashier)')"
                    );
                }

                // Seed suppliers if empty
                var rsSup = stmt.executeQuery("SELECT COUNT(*) FROM suppliers");
                if (rsSup.next() && rsSup.getInt(1) == 0) {
                    stmt.executeUpdate(
                        "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES " +
                        "('Aspen Pharmacare', 'Mark Harrison', '+27 11 555 0101', 'orders@aspenpharma.com', 'Woodmead, Sandton')," +
                        "('Adcock Ingram', 'Priya Naidoo', '+27 11 555 0202', 'supplies@adcock.com', '1 New Road, Midrand')," +
                        "('Cipla Medpro', 'David Van Der Merwe', '+27 21 555 0303', 'sales@cipla.co.za', 'Rosen Heights, Bellville')," +
                        "('Pfizer South Africa', 'Nokuthula Khumalo', '+27 11 555 0404', 'contact@pfizer.co.za', '85 Bute Lane, Sandton')," +
                        "('Dis-Chem Distribution', 'Tebogo Molefe', '+27 11 555 0505', 'wholesale@dischem.co.za', '23 Stag Road, Midrand')"
                    );
                }

                // Seed medicines if empty
                var rsMed = stmt.executeQuery("SELECT COUNT(*) FROM medicines");
                if (rsMed.next() && rsMed.getInt(1) == 0) {
                    stmt.executeUpdate(
                        "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES " +
                        "('Panado 500mg Tablets (24s)', 'Adcock Ingram', 'Tablet', 45.50, 150, 30, DATE_ADD(CURRENT_DATE, INTERVAL 365 DAY), 2)," +
                        "('Amoxicillin 500mg Capsules', 'Cipla Medpro', 'Capsule', 115.00, 8, 20, DATE_ADD(CURRENT_DATE, INTERVAL 180 DAY), 3)," +
                        "('Benylin 4 Flu Syrup 100ml', 'Johnson & Johnson', 'Syrup', 78.90, 45, 15, DATE_ADD(CURRENT_DATE, INTERVAL 20 DAY), 1)," +
                        "('Voltaren Emulgel 50g', 'GSK Consumer', 'Cream', 125.00, 35, 10, DATE_ADD(CURRENT_DATE, INTERVAL 450 DAY), 4)," +
                        "('Vitamin C 1000mg Effervescent', 'Aspen Pharmacare', 'Tablet', 65.00, 5, 25, DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 1)," +
                        "('Allergex 4mg Tablets (30s)', 'Adcock Ingram', 'Tablet', 38.50, 85, 20, DATE_ADD(CURRENT_DATE, INTERVAL 500 DAY), 2)," +
                        "('Brufen 400mg Tablets', 'Abbott', 'Tablet', 92.00, 60, 20, DATE_ADD(CURRENT_DATE, INTERVAL 240 DAY), 3)," +
                        "('Corenza C Effervescent (10s)', 'Cipla Medpro', 'Tablet', 85.00, 12, 30, DATE_ADD(CURRENT_DATE, INTERVAL 25 DAY), 3)," +
                        "('Bactroban Ointment 15g', 'GSK Consumer', 'Cream', 145.00, 18, 10, DATE_ADD(CURRENT_DATE, INTERVAL 400 DAY), 4)," +
                        "('Cataflam D 50mg (20s)', 'Novartis', 'Tablet', 110.00, 4, 15, DATE_ADD(CURRENT_DATE, INTERVAL 12 DAY), 5)," +
                        "('Gaviscon Peppermint Liquid 300ml', 'Reckitt', 'Syrup', 119.50, 40, 15, DATE_ADD(CURRENT_DATE, INTERVAL 300 DAY), 5)," +
                        "('Ceutrex 1g Injection Vial', 'Pfizer', 'Injection', 250.00, 15, 10, DATE_ADD(CURRENT_DATE, INTERVAL 180 DAY), 4)"
                    );
                }

                // Seed initial sales if empty
                var rsSales = stmt.executeQuery("SELECT COUNT(*) FROM sales");
                if (rsSales.next() && rsSales.getInt(1) == 0) {
                    stmt.executeUpdate(
                        "INSERT INTO sales (sale_date, total_amount, user_id) VALUES " +
                        "(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 209.00, 2)," +
                        "(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 160.50, 2)," +
                        "(CURRENT_TIMESTAMP, 243.90, 3)"
                    );
                    stmt.executeUpdate(
                        "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES " +
                        "(1, 1, 2, 45.50)," +
                        "(1, 11, 1, 118.00)," +
                        "(2, 6, 2, 38.50)," +
                        "(2, 8, 1, 83.50)," +
                        "(3, 3, 1, 78.90)," +
                        "(3, 4, 1, 125.00)," +
                        "(3, 1, 1, 40.00)"
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Database auto-initialization notice: " + e.getMessage());
        }
    }

    public static String getHost() { return host; }
    public static String getPort() { return port; }
    public static String getDatabase() { return database; }
    public static String getUser() { return user; }
    public static String getPassword() { return password; }
}
