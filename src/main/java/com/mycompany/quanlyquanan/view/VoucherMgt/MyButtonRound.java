/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.view.VoucherMgt;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 *
 * @author macos
 */
public class MyButtonRound extends JButton {

    public static final Font BUTTON_FONT = new Font("Helvetica Neue", Font.BOLD, 14);
    public static final int BUTTON_BORDER_RADIUS = 40;
    public static final int BUTTON_BORDER_THICKNESS = 1;
    public static final int BUTTON_HEIGHT = 40;
    public static final int BUTTON_PADDING = 8;

    public static final Color PRIMARY_BG_PRESSED = new Color(255, 153, 153);
    public static final Color PRIMARY_BG_HOVER = new Color(225, 193, 193);
    public static final Color PRIMARY_BG = new Color(245, 245, 245);
    public static final Color PRIMARY_TEXT = Color.WHITE;

    private Color bgColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;
    private Color currentBgColor;

    public MyButtonRound() {
        setFont(BUTTON_FONT);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setContentAreaFilled(false);
        setPreferredSize(new Dimension(120, BUTTON_HEIGHT));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(BUTTON_PADDING, BUTTON_PADDING * 2, BUTTON_PADDING, BUTTON_PADDING * 2));

        bgColor = PRIMARY_BG;
        hoverColor = PRIMARY_BG_HOVER;
        pressedColor = PRIMARY_BG_PRESSED;
        textColor = PRIMARY_TEXT;
        currentBgColor = bgColor;

        setForeground(textColor);

        // Mouse effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    currentBgColor = hoverColor;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) {
                    currentBgColor = bgColor;
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    currentBgColor = pressedColor;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    currentBgColor = getBounds().contains(e.getPoint()) ? hoverColor : bgColor;
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw shadow (optional)
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, BUTTON_BORDER_RADIUS, BUTTON_BORDER_RADIUS);

        // Draw button background
        g2.setColor(currentBgColor); // Sử dụng màu nền thực tế
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_BORDER_RADIUS, BUTTON_BORDER_RADIUS);

        // Draw border
        g2.setColor(new Color(234, 127, 146));
        g2.setStroke(new BasicStroke(BUTTON_BORDER_THICKNESS));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, BUTTON_BORDER_RADIUS, BUTTON_BORDER_RADIUS);

        g2.dispose();
        super.paintComponent(g);
    }
}
