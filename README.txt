================================================================================
HEALTHFIRST PHARMACY INVENTORY MANAGEMENT SYSTEM (PIMS)
Richfield Graduate Institute of Technology
Programming 632 Assignment
================================================================================

1. SYSTEM OVERVIEW
------------------
HealthFirst Pharmacy Inventory Management System (PIMS) is a desktop 
application built with Java Swing/AWT and JDBC for managing medicines, 
tracking supplier relations, processing retail point-of-sale (POS) transactions, 
generating itemized tax invoices, and delivering actionable analytics.

The system features strict Role-Based Access Control (RBAC):
- Administrators: Full CRUD control over medicines, suppliers, user accounts,
  and real-time business reports (Sales, Item-Wise, Low Stock, Expiry).
- Cashiers: Streamlined POS checkout counter, live stock availability verification, 
  automated change calculation, receipt rendering, and text/print billing.


2. DEFAULT LOGIN CREDENTIALS
----------------------------
Role: Administrator
  Username: admin
  Password: admin123
  Name:     Emmanuel Komape (Admin)

Role: Cashier
  Username: cashier
  Password: cash123
  Name:     Jane Doe (Senior Cashier)

Role: Cashier (Alternate)
  Username: cashier2
  Password: cash123
  Name:     John Smith (Cashier)


3. DATABASE SETUP (USING MYSQL WORKBENCH)
-----------------------------------------
Step 1: Open MySQL Workbench and connect to your local MySQL Server instance.
Step 2: In MySQL Workbench, click "File" -> "Open SQL Script..." (or press Ctrl+Shift+O).
Step 3: Select the file "database.sql" located in the root of this project folder:
        C:\Personal stuff\402410914PRO632\database.sql
Step 4: Click the Execute (lightning bolt ⚡) button to run the script.
        This script will:
        - Create the database `pims_db`
        - Create all 5 required tables (`users`, `suppliers`, `medicines`, `sales`, `sale_items`)
        - Populate initial suppliers, sample medicines, near-expiry alerts, low-stock items,
          and sample sales records for instant reporting.
Step 5: Verify the tables under the `pims_db` schema in MySQL Workbench schemas navigator.

Database Connection Properties:
- The system connects by default to:
  Host:     localhost
  Port:     3306
  Database: pims_db
  User:     root
  Password: [Empty by default]

- If your local MySQL root user has a password (e.g. 'root' or 'admin'), 
  you can either:
  a) Edit the "db.properties" file in the project root:
     db.password=your_password
  b) OR simply click the "⚙ MySQL Database Settings" button directly on the 
     Login screen in the application to update and test credentials live.


4. HOW TO RUN IN INTELLIJ IDEA
------------------------------
Step 1: Open IntelliJ IDEA.
Step 2: Click "Open" and select the project directory:
        C:\Personal stuff\402410914PRO632
Step 3: IntelliJ will automatically detect the Maven `pom.xml` configuration.
Step 4: Ensure Project SDK is set to Java 17 or higher:
        File -> Project Structure -> Project -> SDK (select JDK 17, 21, or 25).
Step 5: In the Project Explorer, navigate to:
        src/main/java/Pharmacy.java
Step 6: Right-click `Pharmacy.java` and select "Run 'Pharmacy.main()'".


5. RUNNING VIA SCRIPT / COMMAND LINE
------------------------------------
You can also launch the application immediately using the included Windows batch script:
- Double-click `run.bat` in the project root folder.
OR in PowerShell:
  .\run.bat


6. SYSTEM MODULES & KEY FEATURES
--------------------------------
1. Authentication Module:
   - Clean, professional card UI with HealthFirst Pharmacy branding.
   - Live credential validation against MySQL `users` table.
   - Dynamic redirection: Admins to AdminDashboard, Cashiers to CashierDashboard.
   - Database Configuration Dialog: Easily update host/port/credentials with 
     instant connection testing.

2. Cashier Module (Point of Sale):
   - Interactive POS register with live medicine dropdown and keyword filtering.
   - Real-time stock verification prevents selling more units than available in inventory.
   - Cart table displaying Item, Type, Unit Price, Quantity, Subtotal, and Remove action.
   - Dynamic checkout panel calculates Grand Total, Tendered Amount, and Change Due.
   - Atomic database transactions ensure sales records and inventory deductions remain synchronized.
   - Bill / Receipt Window: Displays official HealthFirst Tax Invoice with date, cashier, itemized 
     table, total, tendered amount, and change. Includes "Print Bill" and "Save as Text" features.
   - Stock Check Tab: Dedicated read-only catalog for cashiers to verify pricing and stock availability 
     without edit rights.

3. Administrator Module:
   - Manage Medicines: Full CRUD operations (Add, Update, Delete, View) with company, type, price, 
     stock count, reorder thresholds, expiry dates, and supplier assignment.
   - Manage Suppliers: Full CRUD operations for pharmaceutical distributors and contacts.
   - Manage Users: Account administration for cashiers and administrators.
   - Business Analytics & Reports:
     a) Sales Report: Filter by date range (All Time, Today, Last 7 Days, This Month) with total revenue KPI.
     b) Item-Wise Sales Report: Breakdown of units sold, revenue generated, and remaining stock per item.
     c) Low Stock Report: Highlights all medicines where `quantity_in_stock <= reorder_level` with deficit count.
     d) Expiry Report: Identifies all stock expiring in the next 30 days (1 month) or expired items.
     - All reports feature one-click "Export to CSV" for spreadsheet compatibility.

================================================================================
Developed for HealthFirst Pharmacy | By sean Komape
================================================================================
