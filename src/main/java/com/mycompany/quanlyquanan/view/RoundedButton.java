/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.view;

import java.awt.Button;
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Admin
 */
public class RoundedButton extends JButton {
    private int radius;

    public RoundedButton(String text, int radius) {
        super(text);
        this.radius = radius;
        setContentAreaFilled(false);  // Không vẽ nền mặc định
        setFocusPainted(false);       // Bỏ viền focus
        setBorderPainted(false);      // Bỏ viền mặc định
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Nền nút
        if (getModel().isArmed()) {
            g2.setColor(getBackground().darker());
        } else {
            g2.setColor(getBackground());
        }
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        // Chữ
        FontMetrics fm = g2.getFontMetrics();
        Rectangle r = new Rectangle(0, 0, getWidth(), getHeight());
        int x = (r.width - fm.stringWidth(getText())) / 2;
        int y = (r.height - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(getForeground());
        g2.drawString(getText(), x, y);

        g2.dispose();
        super.paintComponent(g); // Nếu bạn không muốn hiệu ứng JButton mặc định, có thể bỏ dòng này
    }

    @Override
    protected void paintBorder(Graphics g) {
        // Không vẽ border, hoặc bạn có thể vẽ border bo tròn tại đây nếu muốn
    }
}
