-- ========================================================
-- Pharmacy Inventory Management System (PIMS) - "HealthFirst"
-- Database Schema and Initial Seed Data
-- Compatible with MySQL 8.0+ and MySQL Workbench
-- ========================================================

DROP DATABASE IF EXISTS pims_db;
CREATE DATABASE pims_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pims_db;

-- --------------------------------------------------------
-- Table 1: users
-- Stores login credentials and roles
-- --------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Cashier') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- --------------------------------------------------------
-- Table 2: suppliers
-- Stores medicine supplier information
-- --------------------------------------------------------
CREATE TABLE suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- --------------------------------------------------------
-- Table 3: medicines
-- The core inventory table
-- --------------------------------------------------------
CREATE TABLE medicines (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    company VARCHAR(100) NOT NULL,
    medicine_type VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10,
    expiry_date DATE NOT NULL,
    supplier_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicines_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------
-- Table 4: sales
-- Stores header information for each transaction
-- --------------------------------------------------------
CREATE TABLE sales (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    user_id INT NOT NULL,
    CONSTRAINT fk_sales_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------
-- Table 5: sale_items
-- Stores line items for each sale (normalized form)
-- --------------------------------------------------------
CREATE TABLE sale_items (
    sale_item_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity_sold INT NOT NULL,
    price_at_sale DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_sale_items_sale
        FOREIGN KEY (sale_id) REFERENCES sales(sale_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_sale_items_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ========================================================
-- Seed Initial Data
-- ========================================================

-- Users (Default passwords as per specification)
INSERT INTO users (username, password, role, full_name) VALUES
('admin', 'admin123', 'Admin', 'Emmanuel Komape (Admin)'),
('cashier', 'cash123', 'Cashier', 'Jane Doe (Senior Cashier)'),
('cashier2', 'cash123', 'Cashier', 'John Smith (Cashier)');

-- Suppliers
INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
('Aspen Pharmacare', 'Mark Harrison', '+27 11 555 0101', 'orders@aspenpharma.com', 'Building 8, Healthcare Park, Woodlands Drive, Woodmead'),
('Adcock Ingram', 'Priya Naidoo', '+27 11 555 0202', 'supplies@adcock.com', '1 New Road, Midrand, Johannesburg'),
('Cipla Medpro', 'David Van Der Merwe', '+27 21 555 0303', 'sales@cipla.co.za', 'Rosen Heights, Pasita Street, Bellville, Cape Town'),
('Pfizer South Africa', 'Nokuthula Khumalo', '+27 11 555 0404', 'contact@pfizer.co.za', '85 Bute Lane, Sandton, Johannesburg'),
('Dis-Chem Distribution', 'Tebogo Molefe', '+27 11 555 0505', 'wholesale@dischem.co.za', '23 Stag Road, Glen Austin, Midrand');

-- Medicines
-- Note: Expiry dates include some near expiry (within 30 days) and some with low stock to showcase reporting
INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES
('Panado 500mg Tablets (24s)', 'Adcock Ingram', 'Tablet', 45.50, 150, 30, DATE_ADD(CURRENT_DATE, INTERVAL 365 DAY), 2),
('Amoxicillin 500mg Capsules', 'Cipla Medpro', 'Capsule', 115.00, 8, 20, DATE_ADD(CURRENT_DATE, INTERVAL 180 DAY), 3),
('Benylin 4 Flu Syrup 100ml', 'Johnson & Johnson', 'Syrup', 78.90, 45, 15, DATE_ADD(CURRENT_DATE, INTERVAL 20 DAY), 1),
('Voltaren Emulgel 50g', 'GSK Consumer', 'Cream', 125.00, 35, 10, DATE_ADD(CURRENT_DATE, INTERVAL 450 DAY), 4),
('Vitamin C 1000mg Effervescent', 'Aspen Pharmacare', 'Tablet', 65.00, 5, 25, DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 1),
('Allergex 4mg Tablets (30s)', 'Adcock Ingram', 'Tablet', 38.50, 85, 20, DATE_ADD(CURRENT_DATE, INTERVAL 500 DAY), 2),
('Brufen 400mg Tablets', 'Abbott', 'Tablet', 92.00, 60, 20, DATE_ADD(CURRENT_DATE, INTERVAL 240 DAY), 3),
('Corenza C Effervescent (10s)', 'Cipla Medpro', 'Tablet', 85.00, 12, 30, DATE_ADD(CURRENT_DATE, INTERVAL 25 DAY), 3),
('Bactroban Ointment 15g', 'GSK Consumer', 'Cream', 145.00, 18, 10, DATE_ADD(CURRENT_DATE, INTERVAL 400 DAY), 4),
('Cataflam D 50mg (20s)', 'Novartis', 'Tablet', 110.00, 4, 15, DATE_ADD(CURRENT_DATE, INTERVAL 12 DAY), 5),
('Gaviscon Peppermint Liquid 300ml', 'Reckitt', 'Syrup', 119.50, 40, 15, DATE_ADD(CURRENT_DATE, INTERVAL 300 DAY), 5),
('Prospan Cough Syrup 100ml', 'Engystol', 'Syrup', 99.00, 55, 15, DATE_ADD(CURRENT_DATE, INTERVAL 600 DAY), 1),
('Hydrocortisone 1% Cream 15g', 'Aspen Pharmacare', 'Cream', 55.00, 22, 10, DATE_ADD(CURRENT_DATE, INTERVAL 90 DAY), 1),
('Ceutrex 1g Injection Vial', 'Pfizer', 'Injection', 250.00, 15, 10, DATE_ADD(CURRENT_DATE, INTERVAL 180 DAY), 4);

-- Initial Sample Sales & Sale Items for Instant Reporting Demonstration
INSERT INTO sales (sale_date, total_amount, user_id) VALUES
(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 209.00, 2),
(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 160.50, 2),
(CURRENT_TIMESTAMP, 243.90, 3);

INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES
(1, 1, 2, 45.50),  -- 2 x Panado = 91.00
(1, 11, 1, 118.00), -- 1 x Gaviscon = 118.00 (Total 209.00)
(2, 6, 2, 38.50),  -- 2 x Allergex = 77.00
(2, 8, 1, 83.50),  -- 1 x Corenza C = 83.50 (Total 160.50)
(3, 3, 1, 78.90),  -- 1 x Benylin = 78.90
(3, 4, 1, 125.00), -- 1 x Voltaren = 125.00
(3, 1, 1, 40.00);  -- 1 x Panado = 40.00 (Total 243.90)

-- ========================================================
-- End of database.sql
-- ========================================================
