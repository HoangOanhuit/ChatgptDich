package com.mycompany.quanlyquanan.view.Pagination;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class ButtonUI extends BasicButtonUI {
    private boolean hover;
    private JButton button;

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        button = (JButton) c;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
            }
        });
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setForeground(Color.BLACK);
        button.setBackground(new Color(248, 196, 196));
        button.setBorder(new EmptyBorder(5, 10, 5, 10));
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();

        if (button.isSelected() || hover) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (hover) {
                g2.setColor(new Color(234, 127, 146));
            } else {
                g2.setColor(c.getBackground());
            }
            int width = c.getWidth();
            int height = c.getHeight();
            if (button.isSelected()) {
                Shape shape = new RoundRectangle2D.Double(0, 0, width, height, 5, 5);
                g2.fill(shape);
            }
            g2.dispose();
        }
        super.paint(g, c);
    }
}
