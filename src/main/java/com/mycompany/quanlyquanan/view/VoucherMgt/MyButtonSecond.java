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
public class MyButtonSecond extends JButton {

    public static final Font BUTTON_FONT = new Font("Helvetica Neue", Font.BOLD, 14);
    public static final int BUTTON_BORDER_RADIUS = 20;
    public static final int BUTTON_BORDER_THICKNESS = 1;
    public static final int BUTTON_HEIGHT = 40;
    public static final int BUTTON_PADDING = 8;

//    public static final Color SECONDARY_BG = new Color(204, 204, 204);
    public static final Color SECONDARY_BG = new Color(255,255,255);
//    public static final Color SECONDARY_BG_HOVER = new Color(180, 180, 180);
    public static final Color SECONDARY_BG_HOVER = new Color(239, 220, 220);
    public static final Color SECONDARY_BG_PRESSED = new Color(160, 160, 160);

    public static final Color SECONDARY_TEXT = Color.BLACK;


    private Color bgColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;

    public MyButtonSecond() {
        setFont(BUTTON_FONT);
        setFocusPainted(false);
        setBorderPainted(true);

        setOpaque(false);
        setContentAreaFilled(false);
        setPreferredSize(new Dimension(120, BUTTON_HEIGHT));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(BUTTON_PADDING, BUTTON_PADDING * 2, BUTTON_PADDING, BUTTON_PADDING * 2));

        bgColor = SECONDARY_BG;
        hoverColor = SECONDARY_BG_HOVER;
        pressedColor = SECONDARY_BG_PRESSED;
        textColor = SECONDARY_TEXT;

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
        g2.setColor(new Color(0, 0, 0, 20)); // chỉ định màu cho lần vẽ tiếp theo
        g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, BUTTON_BORDER_RADIUS, BUTTON_BORDER_RADIUS);

        // Draw button background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_BORDER_RADIUS, BUTTON_BORDER_RADIUS); // vẽ hình chữ nhật bo góc

        // Vẽ và tô màu viền Button
        g2.setColor(new Color(230, 100, 100)); // Màu viền border
        g2.setStroke(new BasicStroke(BUTTON_BORDER_THICKNESS)); // Độ dày viền
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, BUTTON_BORDER_RADIUS, BUTTON_BORDER_RADIUS); // vẽ viền bo góc

        g2.dispose();
        super.paintComponent(g);
    }
}
