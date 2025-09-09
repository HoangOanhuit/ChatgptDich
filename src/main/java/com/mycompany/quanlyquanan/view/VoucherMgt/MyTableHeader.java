/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.view.VoucherMgt;

import java.awt.*;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author macos
 */
public class MyTableHeader extends JLabel{
    public MyTableHeader(String text) {
        super(text);
        setOpaque(true);
        setBackground(new Color(209, 204, 204));
        setFont(new Font("Helvetica Neue", Font.BOLD, 14 ));
        setHorizontalAlignment(JLabel.CENTER);
        setForeground(new Color(4, 4, 4));
        setBorder(new EmptyBorder(10, 5, 10, 5));
        setPreferredSize(new Dimension( 100,45)); // Chiều cao của header
    }
    
    @Override
    protected void paintComponent(Graphics grphcs){
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);// Chống răng cưa
        g2.setColor(new Color(255, 255, 255)); // Màu nền là màu xám nhạt
        g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1); // Vẽ đường viền dưới
        super.paintComponent(grphcs);
    }
            
    
}
