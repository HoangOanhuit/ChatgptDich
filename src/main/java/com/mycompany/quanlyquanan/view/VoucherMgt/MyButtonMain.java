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
public class MyButtonMain extends JButton {

    public static final Font BUTTON_FONT = new Font("Helvetica Neue", Font.BOLD, 14);
    public static final int BUTTON_BORDER_RADIUS = 20;
    public static final int BUTTON_BORDER_THICKNESS = 1;
    public static final int BUTTON_HEIGHT = 40;
    public static final int BUTTON_PADDING = 8;

    public static final Color PRIMARY_BG_PRESSED = new Color(255, 153, 153);
    public static final Color PRIMARY_BG_HOVER = new Color(255, 120, 120);
    public static final Color PRIMARY_BG = new Color(230, 100, 100);
    public static final Color PRIMARY_TEXT = Color.WHITE;

    private Color bgColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;

    public MyButtonMain() {
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

        setForeground(textColor);

        // Mouse effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(hoverColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(bgColor);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(pressedColor);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(getBounds().contains(e.getPoint()) ? hoverColor : bgColor);
                }
            }
        });
        setBackground(bgColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw shadow (optional)
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, BUTTON_BORDER_RADIUS, BUTTON_BORDER_RADIUS);

        // Draw button background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_BORDER_RADIUS, BUTTON_BORDER_RADIUS);

        // Draw border
        g2.setColor(new Color(230, 100, 100));
        g2.setStroke(new BasicStroke(BUTTON_BORDER_THICKNESS));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, BUTTON_BORDER_RADIUS, BUTTON_BORDER_RADIUS);

        g2.dispose();
        super.paintComponent(g);
    }
}
