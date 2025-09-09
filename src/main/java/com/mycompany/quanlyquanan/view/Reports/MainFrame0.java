/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.quanlyquanan.view.Reports;

import com.mycompany.quanlyquanan.model.ModelCard;
import java.awt.BorderLayout;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import java.awt.Color;
import javax.swing.Icon;

/**
 *
 * @author macos
 */
public class MainFrame0 extends javax.swing.JFrame {

    
    public MainFrame0() {
        initComponents();

        // Register the icon font - Có thể code trong class Main nhưng phải chạy trước khi khởi tạo các icon
        IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
        initData();
    }
    
    public void initData(){
        initCardData();
    }

    //[TODO] - cần lấy dữ liệu từ database
    // Hiện tại chỉ là dữ liệu mẫu
    public void initCardData(){
        Icon icon1 = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCOUNT_BALANCE_WALLET, 60, new Color(255,255,255,100));
        card1.setData(new ModelCard("Today's Income", 5100, icon1));
        Icon icon2 = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.MONETIZATION_ON, 60, new Color(255,255,255,100));
        card2.setData(new ModelCard("Today's Expense", 4000, icon2));
        Icon icon3 = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SHOW_CHART, 60, new Color(255,255,255,100));
        card3.setData(new ModelCard("Today's Revenue", 1100, icon3));
        
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        card2 = new com.mycompany.quanlyquanan.view.Reports.CardGradient();
        card3 = new com.mycompany.quanlyquanan.view.Reports.CardGradient();
        card1 = new com.mycompany.quanlyquanan.view.Reports.CardGradient();
        tabPane = new com.mycompany.quanlyquanan.view.Reports.TabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(51, 51, 255));
        jLabel1.setText("Dashboard");

        card2.setBackground(new java.awt.Color(102, 204, 255));
        card2.setColorGradient(new java.awt.Color(255, 204, 255));

        card3.setBackground(new java.awt.Color(255, 204, 102));
        card3.setForeground(new java.awt.Color(255, 204, 153));
        card3.setColorGradient(new java.awt.Color(255, 204, 204));

        card1.setBackground(new java.awt.Color(205, 139, 237));
        card1.setColorGradient(new java.awt.Color(255, 153, 153));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(card1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(card2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(card3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(tabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 813, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(card1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(card3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(card2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tabPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

   
    public static void main(String args[]) {
    
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame0().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mycompany.quanlyquanan.view.Reports.CardGradient card1;
    private com.mycompany.quanlyquanan.view.Reports.CardGradient card2;
    private com.mycompany.quanlyquanan.view.Reports.CardGradient card3;
    private javax.swing.JLabel jLabel1;
    private com.mycompany.quanlyquanan.view.Reports.TabbedPane tabPane;
    // End of variables declaration//GEN-END:variables
    
}
