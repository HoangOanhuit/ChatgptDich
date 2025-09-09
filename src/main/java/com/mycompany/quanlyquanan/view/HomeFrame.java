/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.quanlyquanan.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.quanlyquanan.utils.Session;

import com.mycompany.quanlyquanan.view.category.QuanLyDanhMucPanel;
import com.mycompany.quanlyquanan.view.dish.QuanLyMonAnPanel;
import com.mycompany.quanlyquanan.view.recipe.QuanLyCongThucPanel;
import com.mycompany.quanlyquanan.view.inventory.QuanLyKhoPanel;
import com.mycompany.quanlyquanan.view.material.QuanLyNguyenLieuPanel;

import com.mycompany.quanlyquanan.view.Employee.QuanLyNhanVienPanel;
import com.mycompany.quanlyquanan.view.table.DieuPhoiBanPanel;
import com.mycompany.quanlyquanan.view.table.QuanLyBanPanel;
import com.mycompany.quanlyquanan.view.userProfile.UserProfilePanel;
import com.mycompany.quanlyquanan.view.waitingQueue.WaitingQueuePanel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Admin
 */
public class HomeFrame extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(HomeFrame.class.getName());
    private JPanel contentPanel;
    private List<JButton> navButtons = new ArrayList<>();
    private Map<JButton, String> navMap = new LinkedHashMap<>();
    private Map<String, JPanel> panelMap = new HashMap<>();
    private Color hoverColor = new Color(220, 220, 220);
    private Color activeColor = new Color(200, 200, 255);
    private Font defaultFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font boldFont = new Font("Segoe UI", Font.BOLD, 14);
    int width = 250;
    int height = 700;

    /**
     * Creates new form HomeFrame
     */
    public HomeFrame() {
        initComponents();
        lbCloseMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menuLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        
        navButtons.add(jBDanhMuc);
        navButtons.add(jBMonAn);
        navButtons.add(jBCongThuc);
        navButtons.add(jBKho);
        navButtons.add(jBNguyenLieu);
        
        navButtons.add(jBBan);
        navButtons.add(jBNv);
        

        setTitle("Java Hotpot");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ===== Content Panel (Card Layout) =====
        contentPanel = pnContainer;

        // ===== Các panel đã có sẵn =====
        
        JPanel panelBan = new QuanLyBanPanel();
//        JPanel panelDieuPhoiBan = new DieuPhoiBanPanel();
        JPanel panelNhanVien = new QuanLyNhanVienPanel();
        JPanel panelWaitingQueue = new WaitingQueuePanel();
        JPanel panelUserProfile = new UserProfilePanel();
        JPanel panelDanhMuc = new QuanLyDanhMucPanel();
        JPanel panelMonAn = new QuanLyMonAnPanel();
        JPanel panelCongThuc = new QuanLyCongThucPanel();
        JPanel panelKho = new QuanLyKhoPanel();
        JPanel panelNguyenLieu = new QuanLyNguyenLieuPanel();
        

        // ===== Đăng ký panel vào card layout =====
        
        panelMap.put("Bàn", panelBan);
//        panelMap.put("Điều phối bàn", panelDieuPhoiBan);
        panelMap.put("Nhân viên", panelNhanVien);
        panelMap.put("Waiting queue", panelWaitingQueue);
        panelMap.put("Profile", panelUserProfile);
        panelMap.put("Danh mục", panelDanhMuc);
        panelMap.put("Món Ăn", panelMonAn);
        panelMap.put("Công thức", panelCongThuc);
        panelMap.put("Kho", panelKho);
        panelMap.put("Nguyên Liệu", panelNguyenLieu);

        contentPanel.add(panelDanhMuc, "Danh mục");
        contentPanel.add(panelMonAn, "Món Ăn");
        contentPanel.add(panelCongThuc, "Công thức");
        contentPanel.add(panelKho, "Kho");
        contentPanel.add(panelNguyenLieu, "Nguyên Liệu");
        
        contentPanel.add(panelBan, "Bàn");
//        contentPanel.add(panelDieuPhoiBan, "Điều phối bàn");
        contentPanel.add(panelNhanVien, "Nhân viên");
         contentPanel.add(panelWaitingQueue, "Waiting queue");
        contentPanel.add(panelUserProfile, "Profile");

        // ===== Nút
        navMap.put(jBDanhMuc, "Danh mục");
        navMap.put(jBMonAn, "Món Ăn");
        navMap.put(jBCongThuc, "Công thức");
        navMap.put(jBKho, "Kho");
        navMap.put(jBNguyenLieu, "Nguyên Liệu");
        
        navMap.put(jBBan, "Bàn");
//        navMap.put(jBDieuPhoiBan,"Điều phối bàn");
        navMap.put(jBNv, "Nhân viên");
        navMap.put(jBWaitingQueue, "Waiting queue");
        navMap.put(jBProfile, "Profile");

        // ===== Gắn listener & style =====
        for (Map.Entry<JButton, String> entry : navMap.entrySet()) {
            JButton btn = entry.getKey();
            String panelName = entry.getValue();

//            addHoverEffect(btn);
            navButtons.add(btn);

            btn.addActionListener(e -> switchPanel(panelName, btn));
        }

        // ===== Mặc định mở panel Bàn =====
        switchPanel("Bàn", jBBan);

    }

    private void addHoverEffect(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (button.getFont().isPlain()) {
                    button.setBackground(hoverColor);
                }
            }

            public void mouseExited(MouseEvent e) {
                if (button.getFont().isPlain()) {
                    button.setBackground(Color.WHITE);
                }
            }
        });
    }

    private void switchPanel(String name, JButton activeButton) {
        // Nếu là panel Nhân viên thì refresh trước khi show
        if ("Nhân viên".equals(name)) {
            JPanel panel = panelMap.get(name);
            if (panel instanceof QuanLyNhanVienPanel) {
                ((QuanLyNhanVienPanel) panel).refresh();
            }
        }

        if ("Profile".equals(name)) {
            JPanel panel = panelMap.get(name);
            if (panel instanceof UserProfilePanel) {
                ((UserProfilePanel) panel).setSelectedEmp(Session.getInstance().getCurrentUser());
            }
        }

        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, name);
        contentPanel.revalidate();
        contentPanel.repaint();

        for (JButton btn : navButtons) {
            btn.setFont(defaultFont);
//            btn.setBackground(Color.WHITE);
        }

        activeButton.setFont(boldFont);
//        activeButton.setBackground(activeColor);
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {

                pnMenu.setSize(width, height);

            }

        }).start();
        */
        SwingUtilities.invokeLater(() -> pnMenu.setSize(width, height));        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnMenu = new javax.swing.JPanel();
        logo = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lbCloseMenu = new javax.swing.JLabel();
        jBDanhMuc = new javax.swing.JButton();
        jBNv = new javax.swing.JButton();
        jBBan = new javax.swing.JButton();
        jBProfile = new javax.swing.JButton();
        jBWaitingQueue = new javax.swing.JButton();
        jBMonAn = new javax.swing.JButton();
        jBCongThuc = new javax.swing.JButton();
        jBKho = new javax.swing.JButton();
        jBNguyenLieu = new javax.swing.JButton();
        bnNav = new javax.swing.JPanel();
        menuLogo = new javax.swing.JLabel();
        pnContainer = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnMenu.setBackground(new java.awt.Color(247, 255, 247));
        pnMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnMenuMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnMenuMouseExited(evt);
            }
        });

        logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/hotpot-40.png"))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(112, 0, 0));
        jLabel1.setText("Java Hotpot");

        lbCloseMenu.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lbCloseMenu.setText("  x");
        lbCloseMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbCloseMenuMouseClicked(evt);
            }
        });

        jBDanhMuc.setBackground(new java.awt.Color(255, 242, 242));
        jBDanhMuc.setText("Quản Lý Danh Mục");
        jBDanhMuc.setBorder(null);
        jBDanhMuc.setBorderPainted(false);
        jBDanhMuc.setFocusable(false);
        jBDanhMuc.setOpaque(true);
        jBDanhMuc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBDanhMucActionPerformed(evt);
            }
        });

        jBNv.setBackground(new java.awt.Color(255, 242, 242));
        jBNv.setText("Quản Lý Nhân Viên");
        jBNv.setBorder(null);
        jBNv.setBorderPainted(false);
        jBNv.setFocusable(false);
        jBNv.setOpaque(true);
        jBNv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBNvActionPerformed(evt);
            }
        });

        jBBan.setBackground(new java.awt.Color(255, 242, 242));
        jBBan.setText("Quản Lý Bàn");
        jBBan.setBorder(null);
        jBBan.setBorderPainted(false);
        jBBan.setFocusable(false);
        jBBan.setOpaque(true);
        jBBan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBBanActionPerformed(evt);
            }
        });

        jBProfile.setBackground(new java.awt.Color(255, 242, 242));
        jBProfile.setText("User Profile");
        jBProfile.setBorder(null);
        jBProfile.setBorderPainted(false);
        jBProfile.setFocusable(false);
        jBProfile.setOpaque(true);
        jBProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBProfileActionPerformed(evt);
            }
        });

        jBWaitingQueue.setBackground(new java.awt.Color(255, 242, 242));
        jBWaitingQueue.setText("Quản Lý Hàng Đợi");
        jBWaitingQueue.setBorder(null);
        jBWaitingQueue.setBorderPainted(false);
        jBWaitingQueue.setFocusable(false);
        jBWaitingQueue.setOpaque(true);
        jBWaitingQueue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBWaitingQueueActionPerformed(evt);
            }
        });

        jBMonAn.setBackground(new java.awt.Color(255, 242, 242));
        jBMonAn.setText("Quản Lý Món Ăn");
        jBMonAn.setBorder(null);
        jBMonAn.setBorderPainted(false);
        jBMonAn.setFocusable(false);
        jBMonAn.setOpaque(true);
        jBMonAn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBMonAnActionPerformed(evt);
            }
        });

        jBCongThuc.setBackground(new java.awt.Color(255, 242, 242));
        jBCongThuc.setText("Quản Lý Công Thức");
        jBCongThuc.setBorder(null);
        jBCongThuc.setBorderPainted(false);
        jBCongThuc.setFocusable(false);
        jBCongThuc.setMaximumSize(new java.awt.Dimension(90, 16));
        jBCongThuc.setMinimumSize(new java.awt.Dimension(90, 16));
        jBCongThuc.setOpaque(true);
        jBCongThuc.setPreferredSize(new java.awt.Dimension(90, 16));
        jBCongThuc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBCongThucActionPerformed(evt);
            }
        });

        jBKho.setBackground(new java.awt.Color(255, 242, 242));
        jBKho.setText("Quản Lý Kho");
        jBKho.setBorder(null);
        jBKho.setBorderPainted(false);
        jBKho.setFocusable(false);
        jBKho.setOpaque(true);
        jBKho.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBKhoActionPerformed(evt);
            }
        });

        jBNguyenLieu.setBackground(new java.awt.Color(255, 242, 242));
        jBNguyenLieu.setText("Quản Lý Nguyên Liệu");
        jBNguyenLieu.setBorder(null);
        jBNguyenLieu.setBorderPainted(false);
        jBNguyenLieu.setFocusable(false);
        jBNguyenLieu.setOpaque(true);
        jBNguyenLieu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBNguyenLieuActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnMenuLayout = new javax.swing.GroupLayout(pnMenu);
        pnMenu.setLayout(pnMenuLayout);
        pnMenuLayout.setHorizontalGroup(
            pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnMenuLayout.createSequentialGroup()
                        .addComponent(logo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lbCloseMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnMenuLayout.createSequentialGroup()
                        .addGroup(pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jBWaitingQueue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(pnMenuLayout.createSequentialGroup()
                                .addGroup(pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jBDanhMuc, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jBNv, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jBKho, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnMenuLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jBBan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jBProfile, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jBMonAn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jBCongThuc, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap())
                    .addGroup(pnMenuLayout.createSequentialGroup()
                        .addComponent(jBNguyenLieu, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        pnMenuLayout.setVerticalGroup(
            pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnMenuLayout.createSequentialGroup()
                .addGroup(pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnMenuLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(logo))
                    .addGroup(pnMenuLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(lbCloseMenu))))
                .addGap(18, 18, 18)
                .addComponent(jBDanhMuc, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBMonAn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBCongThuc, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBKho, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBNguyenLieu, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jBBan, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBWaitingQueue, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBNv, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bnNav.setBackground(new java.awt.Color(247, 255, 247));

        menuLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/list-20.png"))); // NOI18N
        menuLogo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuLogoMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout bnNavLayout = new javax.swing.GroupLayout(bnNav);
        bnNav.setLayout(bnNavLayout);
        bnNavLayout.setHorizontalGroup(
            bnNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bnNavLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(menuLogo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        bnNavLayout.setVerticalGroup(
            bnNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bnNavLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(menuLogo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnContainer.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                    .addComponent(bnNav, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(bnNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pnContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void menuLogoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuLogoMouseClicked
        // TODO add your handling code here:
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < width; i++) {
                    pnMenu.setSize(width, height);

                }
            }

        }).start();
    }//GEN-LAST:event_menuLogoMouseClicked

    private void pnMenuMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnMenuMouseExited
        // TODO add your handling code here:

    }//GEN-LAST:event_pnMenuMouseExited

    private void pnMenuMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnMenuMouseEntered

    }//GEN-LAST:event_pnMenuMouseEntered

    private void lbCloseMenuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbCloseMenuMouseClicked
        // TODO add your handling code here:
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = width; i >= 0; i--) {
                    pnMenu.setSize(0, height);

                }
            }

        }).start();
    }//GEN-LAST:event_lbCloseMenuMouseClicked

    private void jBNvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBNvActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBNvActionPerformed

    private void jBBanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBBanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBBanActionPerformed

    private void jBProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBProfileActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBProfileActionPerformed

    private void jBWaitingQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBWaitingQueueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBWaitingQueueActionPerformed

    private void jBDanhMucActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBDanhMucActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBDanhMucActionPerformed

    private void jBMonAnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBMonAnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBMonAnActionPerformed

    private void jBCongThucActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBCongThucActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBCongThucActionPerformed

    private void jBKhoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBKhoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBKhoActionPerformed

    private void jBNguyenLieuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBNguyenLieuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBNguyenLieuActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new HomeFrame().setVisible(true));

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bnNav;
    private javax.swing.JButton jBBan;
    private javax.swing.JButton jBCongThuc;
    private javax.swing.JButton jBDanhMuc;
    private javax.swing.JButton jBKho;
    private javax.swing.JButton jBMonAn;
    private javax.swing.JButton jBNguyenLieu;
    private javax.swing.JButton jBNv;
    private javax.swing.JButton jBProfile;
    private javax.swing.JButton jBWaitingQueue;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lbCloseMenu;
    private javax.swing.JLabel logo;
    private javax.swing.JLabel menuLogo;
    private javax.swing.JPanel pnContainer;
    private javax.swing.JPanel pnMenu;
    // End of variables declaration//GEN-END:variables
}
