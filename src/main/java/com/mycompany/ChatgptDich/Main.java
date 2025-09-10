/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.ChatgptDich;
import javax.swing.SwingUtilities;

// Theme tối

//import com.formdev.flatlaf.FlatLightLaf; // Theme sáng

import com.mycompany.ChatgptDich.view.HomeFrame;

/**
 * @author Admin
 */
public class Main {

    public static void main(String[] args) {
        try { javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName()); } 
        catch (Exception ignore) {}
             SwingUtilities.invokeLater(() -> new HomeFrame().setVisible(true));
    }

}
