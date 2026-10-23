package com.healthfirst.ui;

import com.healthfirst.config.DBConfigDialog;
import com.healthfirst.config.DatabaseConnection;
import com.healthfirst.dao.UserDAO;
import com.healthfirst.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private UserDAO userDAO;

    public LoginFrame() {
        this.userDAO = new UserDAO();
        initUI();
    }

    private void initUI() {
        setTitle("HealthFirst Pharmacy - System Login");
        setSize(460, 560);
        setMinimumSize(new Dimension(420, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(25, 20, 25, 20));

        JLabel titleLabel = new JLabel("HEALTHFIRST PHARMACY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel subLabel = new JLabel("Pharmacy Inventory Management System (PIMS)", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(220, 235, 250));
        headerPanel.add(subLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Center Login Card
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setBackground(UITheme.BG_LIGHT);
        cardWrapper.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel loginCard = new JPanel(new GridBagLayout());
        loginCard.setBackground(Color.WHITE);
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel cardTitle = new JLabel("Account Authentication");
        cardTitle.setFont(UITheme.FONT_HEADER);
        cardTitle.setForeground(UITheme.PRIMARY);
        loginCard.add(cardTitle, gbc);

        row++;
        gbc.gridy = row;
        JLabel cardDesc = new JLabel("Please enter your credentials to access your dashboard");
        cardDesc.setFont(UITheme.FONT_SMALL);
        cardDesc.setForeground(UITheme.TEXT_MUTED);
        loginCard.add(cardDesc, gbc);

        // Username
        row++;
        gbc.gridy = row; gbc.gridwidth = 2;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(UITheme.FONT_BOLD);
        loginCard.add(lblUser, gbc);

        row++;
        gbc.gridy = row;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(0, 32));
        loginCard.add(usernameField, gbc);

        // Password
        row++;
        gbc.gridy = row;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.FONT_BOLD);
        loginCard.add(lblPass, gbc);

        row++;
        gbc.gridy = row;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(0, 32));
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
        loginCard.add(passwordField, gbc);

        // Status Label
        row++;
        gbc.gridy = row;
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.DANGER);
        loginCard.add(statusLabel, gbc);

        // Login Button
        row++;
        gbc.gridy = row;
        loginButton = UITheme.createStyledButton("🔐 Sign In", UITheme.PRIMARY, Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(0, 36));
        loginButton.addActionListener(e -> performLogin());
        loginCard.add(loginButton, gbc);

        // DB Settings button
        row++;
        gbc.gridy = row;
        JPanel loginActionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        loginActionRow.setOpaque(false);
        JButton dbConfigBtn = new JButton("\u2699 Database Settings");
        dbConfigBtn.setFont(UITheme.FONT_SMALL);
        dbConfigBtn.setFocusPainted(false);
        dbConfigBtn.addActionListener(e -> openDbSettings());
        loginActionRow.add(dbConfigBtn);
        JButton themeBtn = UITheme.createThemeToggleButton();
        themeBtn.addActionListener(e -> {
            UITheme.toggleTheme();
        });
        loginActionRow.add(themeBtn);
        loginCard.add(loginActionRow, gbc);

        cardWrapper.add(loginCard);
        add(cardWrapper, BorderLayout.CENTER);

        // Footer note with default logins
        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 2));
        footer.setBackground(UITheme.BG_LIGHT);
        footer.setBorder(new EmptyBorder(0, 15, 12, 15));

        JLabel demoHint = new JLabel("Default logins — Admin: admin / admin123  |  Cashier: cashier / cash123  |  Admin name: Emmanuel Komape", SwingConstants.CENTER);
        demoHint.setFont(UITheme.FONT_SMALL);
        demoHint.setForeground(UITheme.TEXT_MUTED);
        footer.add(demoHint);

        JLabel copyright = new JLabel("© HealthFirst Pharmacy - Programming 632", SwingConstants.CENTER);
        copyright.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        copyright.setForeground(new Color(150, 150, 150));
        footer.add(copyright);

        add(footer, BorderLayout.SOUTH);

        // Register theme listener to repaint login frame
        UITheme.addThemeListener(() -> SwingUtilities.updateComponentTreeUI(LoginFrame.this));
    }

    private void openDbSettings() {
        DBConfigDialog dlg = new DBConfigDialog(this);
        dlg.setVisible(true);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Authenticating...");
        statusLabel.setText(" ");

        SwingUtilities.invokeLater(() -> {
            try {
                User user = userDAO.authenticate(username, password);
                if (user == null) {
                    statusLabel.setText("Invalid username or password.");
                    JOptionPane.showMessageDialog(this,
                            "Login failed: Invalid credentials provided.\n\nPlease check your username and password.",
                            "Authentication Failed",
                            JOptionPane.ERROR_MESSAGE);
                    loginButton.setEnabled(true);
                    loginButton.setText("🔐 Sign In");
                    return;
                }

                // Successful login: Redirect based on role
                dispose(); // Close login frame
                if (user.isAdmin()) {
                    new AdminDashboard(user).setVisible(true);
                } else if (user.isCashier()) {
                    new CashierDashboard(user).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Unrecognized user role: " + user.getRole(), "Access Denied", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                statusLabel.setText("Database connection error.");
                int choice = JOptionPane.showConfirmDialog(this,
                        "Could not connect to MySQL database:\n" + ex.getMessage() +
                        "\n\nWould you like to configure your MySQL connection settings now?",
                        "Database Connection Error",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    openDbSettings();
                }
                loginButton.setEnabled(true);
                loginButton.setText("🔐 Sign In");
            }
        });
    }
}
