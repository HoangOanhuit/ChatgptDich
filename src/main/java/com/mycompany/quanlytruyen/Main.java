
package com.mycompany.quanlytruyen;

import com.mycompany.quanlytruyen.config.AppConfig;
import com.mycompany.quanlytruyen.view.HomeFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JOptionPane;

/**
 * Main application entry point
 * @author Admin
 */
public class Main {

    public static void main(String[] args) {
        // Set system look and feel
        setLookAndFeel();
        
        // Initialize configuration
        AppConfig config = AppConfig.getInstance();
        
        // Validate configuration
        AppConfig.ConfigValidationResult validation = config.validate();
        if (!validation.isValid()) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                    "Configuration errors found:\n" + validation.getErrorsAsString() +
                    "\n\nPlease update your app.properties file.",
                    "Configuration Error",
                    JOptionPane.ERROR_MESSAGE);
            });
        }
        
        if (validation.hasWarnings()) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                    "Configuration warnings:\n" + validation.getWarningsAsString(),
                    "Configuration Warning",
                    JOptionPane.WARNING_MESSAGE);
            });
        }
        
        // Start the application
        SwingUtilities.invokeLater(() -> {
            try {
                HomeFrame homeFrame = new HomeFrame();
                homeFrame.setDefaultCloseOperation(HomeFrame.EXIT_ON_CLOSE);
                homeFrame.setLocationRelativeTo(null); // Center on screen
                homeFrame.setVisible(true);
                
                /*
                // Show welcome message if API key is not configured
                if (config.getOpenAIApiKey().isEmpty()) {
                    JOptionPane.showMessageDialog(homeFrame,
                        "Welcome to Translation App!\n\n" +
                        "To get started:\n" +
                        "1. Update your API key in app.properties\n" +
                        "2. Restart the application\n" +
                        "3. Select your files and start translating!",
                        "Welcome",
                        JOptionPane.INFORMATION_MESSAGE);
                }
                */
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Failed to start application:\n" + e.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
    
    private static void setLookAndFeel() {
        try {
            // Use Nimbus look and feel
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            // Use default look and feel if Nimbus fails
            System.err.println("Could not set look and feel: " + ex.getMessage());
        }
    }
}