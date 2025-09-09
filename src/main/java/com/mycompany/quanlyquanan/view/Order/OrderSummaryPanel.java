/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.view.Order;

import java.awt.*;
import javax.swing.*;

/**
 *
 * @author Tyler
 */
public class OrderSummaryPanel extends JPanel {
    public OrderSummaryPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Order Summary"));

        JLabel label = new JLabel("Order #123");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
    }

}
