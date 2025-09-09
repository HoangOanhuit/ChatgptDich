/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.view.Menu;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.*;

/**
 *
 * @author Tyler
 */
public class MenuLayout1 extends JPanel {
    private final Menu menu;

    public MenuLayout1() {
        menu = new Menu();
        setOpaque(false);
        setLayout(new BorderLayout());
        add(menu, BorderLayout.CENTER);
        JPanel p = new JPanel();
    }
    
    @Override
    public Dimension getPreferredSize() {
        if (menu != null) {
            Dimension menuSize = menu.getPreferredSize();
            Container parent = getParent();
            int height = parent != null ? parent.getHeight() : menuSize.height;
            return new Dimension(menuSize.width, height);
        }
        return super.getPreferredSize();
    }

    public Menu getMenu() {
        return menu;
    }
}

