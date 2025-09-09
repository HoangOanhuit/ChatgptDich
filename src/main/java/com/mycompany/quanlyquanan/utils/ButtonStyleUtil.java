package com.mycompany.quanlyquanan.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.Timer;

/**
 * Utility class for applying consistent styling and effects to buttons
 * @author macos
 */
public class ButtonStyleUtil {

    // Button styling constants
    public static final Font BUTTON_FONT = new Font("Helvetica Neue", Font.BOLD, 13);
    public static final int BUTTON_BORDER_RADIUS = 10;// Radius for rounded corners
    public static final int BUTTON_BORDER_THICKNESS = 1; // Thickness of the button
    public static final int BUTTON_HEIGHT = 40; // Height of the button
    public static final int BUTTON_PADDING = 8;

    // Primary button colors
    public static final Color PRIMARY_BG = new Color(255, 153, 153);
    public static final Color PRIMARY_BG_HOVER = new Color(255, 120, 120);
    public static final Color PRIMARY_BG_PRESSED = new Color(230, 100, 100);
    public static final Color PRIMARY_TEXT = Color.WHITE;

    // Secondary button colors
    public static final Color SECONDARY_BG = new Color(204, 204, 204);
    public static final Color SECONDARY_BG_HOVER = new Color(180, 180, 180);
    public static final Color SECONDARY_BG_PRESSED = new Color(160, 160, 160);
    public static final Color SECONDARY_TEXT = Color.WHITE;

    /**
     * Style primary button (Submit, Save, etc.)
     */
    public static void stylePrimaryButton(JButton button) {
        styleButton(button, PRIMARY_BG, PRIMARY_BG_HOVER, PRIMARY_BG_PRESSED, PRIMARY_TEXT);
    }

    /**
     * Style secondary button (Close, Cancel, etc.)
     */
    public static void styleSecondaryButton(JButton button) {
        styleButton(button, SECONDARY_BG, SECONDARY_BG_HOVER, SECONDARY_BG_PRESSED, SECONDARY_TEXT);
    }

    /**
     * Apply styling and effects to a button
     */
    private static void styleButton(JButton button, Color bgColor, Color hoverColor, Color pressedColor, Color textColor) {
        // Basic styling
        button.setFont(BUTTON_FONT);

        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(BUTTON_PADDING, BUTTON_PADDING * 2, BUTTON_PADDING, BUTTON_PADDING * 2));



        // Add mouse effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverColor);
                    button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor);
                    button.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(pressedColor);
                    // Scale effect
                    button.setSize((int)(button.getWidth() * 0.98), (int)(button.getHeight() * 0.98));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.isEnabled()) {
                    // Reset scale with animation
                    Timer timer = new Timer(100, event -> {
                        button.setSize(button.getPreferredSize());
                        button.setBackground(button.getBounds().contains(e.getPoint()) ? hoverColor : bgColor);
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        });
    }

    /**
     * Apply click animation effect to button
     */
    public static void addClickAnimation(JButton button) {
        button.addActionListener(e -> {
            // Ripple effect simulation
            Color originalBg = button.getBackground();

            // Flash effect
            Timer flashTimer = new Timer(50, null);
            flashTimer.addActionListener(event -> {
                if (flashTimer.isRunning()) {
                    button.setBackground(button.getBackground().brighter());
                    Timer resetTimer = new Timer(50, resetEvent -> {
                        button.setBackground(originalBg);
                    });
                    resetTimer.setRepeats(false);
                    resetTimer.start();
                    flashTimer.stop();
                }
            });
            flashTimer.setRepeats(false);
            flashTimer.start();
        });
    }

    /**
     * Style multiple primary buttons
     */
    public static void stylePrimaryButtons(JButton... buttons) {
        for (JButton button : buttons) {
            stylePrimaryButton(button);
            addClickAnimation(button);
        }
    }

    /**
     * Style multiple secondary buttons
     */
    public static void styleSecondaryButtons(JButton... buttons) {
        for (JButton button : buttons) {
            styleSecondaryButton(button);
            addClickAnimation(button);
        }
    }
}
