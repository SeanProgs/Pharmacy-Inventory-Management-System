package com.healthfirst.config;

import javax.swing.*;
import java.awt.*;

public class DBConfigDialog extends JDialog {
    private JTextField hostField;
    private JTextField portField;
    private JTextField dbField;
    private JTextField userField;
    private JPasswordField passField;
    private JLabel statusLabel;
    private boolean saved = false;

    public DBConfigDialog(Frame owner) {
        super(owner, "Database Connection Settings", true);
        initUI();
    }

    private void initUI() {
        setSize(450, 380);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 76, 120));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("MySQL Database Configuration");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel subLabel = new JLabel("Configure MySQL host, credentials and test connectivity");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(220, 235, 252));
        headerPanel.add(subLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        hostField = new JTextField(DatabaseConnection.getHost(), 18);
        portField = new JTextField(DatabaseConnection.getPort(), 18);
        dbField = new JTextField(DatabaseConnection.getDatabase(), 18);
        userField = new JTextField(DatabaseConnection.getUser(), 18);
        passField = new JPasswordField(DatabaseConnection.getPassword(), 18);

        int row = 0;
        addFormField(formPanel, gbc, row++, "Host Server:", hostField);
        addFormField(formPanel, gbc, row++, "Port:", portField);
        addFormField(formPanel, gbc, row++, "Database Name:", dbField);
        addFormField(formPanel, gbc, row++, "Username:", userField);
        addFormField(formPanel, gbc, row++, "Password:", passField);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        formPanel.add(statusLabel, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton testBtn = new JButton("Test Connection");
        testBtn.addActionListener(e -> performTestConnection());

        JButton saveBtn = new JButton("Save & Apply");
        saveBtn.setBackground(new Color(24, 76, 120));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> performSave());

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(testBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(comp, gbc);
    }

    private void performTestConnection() {
        String host = hostField.getText().trim();
        String port = portField.getText().trim();
        String db = dbField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        try {
            boolean success = DatabaseConnection.testConnection(host, port, db, user, pass);
            if (success) {
                statusLabel.setForeground(new Color(30, 130, 60));
                statusLabel.setText("✓ Connection successful! MySQL database is reachable.");
                JOptionPane.showMessageDialog(this,
                        "Successfully connected to MySQL database: " + db + "@" + host + ":" + port,
                        "Connection Succeeded",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            statusLabel.setForeground(new Color(180, 40, 40));
            statusLabel.setText("✗ Connection failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Connection failed:\n" + ex.getMessage() +
                    "\n\nPlease verify MySQL is running and your password is correct.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSave() {
        String host = hostField.getText().trim();
        String port = portField.getText().trim();
        String db = dbField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        if (host.isEmpty() || port.isEmpty() || db.isEmpty() || user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Host, port, database, and username cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DatabaseConnection.saveConfig(host, port, db, user, pass);
        DatabaseConnection.initializeDatabase();
        saved = true;
        JOptionPane.showMessageDialog(this, "Database settings saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }
}
