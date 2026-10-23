import com.healthfirst.config.DatabaseConnection;
import com.healthfirst.ui.LoginFrame;
import com.healthfirst.ui.UITheme;

import javax.swing.*;

/**
 * Main Entry Point for HealthFirst Pharmacy Inventory Management System (PIMS)
 * Richfield Graduate Institute of Technology - Programming 632
 */
public class Pharmacy {
    public static void main(String[] args) {
        // Set Look & Feel
        UITheme.applyGlobalTheme();

        // Attempt automatic database schema initialization in the background
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseConnection.initializeDatabase();
            } catch (Exception e) {
                System.err.println("Note: Database auto-initialization skipped or deferred: " + e.getMessage());
            }

            // Launch the secure Login window
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
