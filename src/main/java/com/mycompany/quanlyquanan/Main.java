/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.quanlyquanan;

// Theme tối

import com.formdev.flatlaf.FlatLightLaf; // Theme sáng

import com.mycompany.quanlyquanan.view.LoginFrame;

/**
 * @author Admin
 */
public class Main {

    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        new LoginFrame().setVisible(true);

    }
}
