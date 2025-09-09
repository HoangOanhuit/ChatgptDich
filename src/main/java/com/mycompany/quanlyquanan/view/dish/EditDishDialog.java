/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.quanlyquanan.view.dish;

import com.mycompany.quanlyquanan.controller.CategoryController;
import com.mycompany.quanlyquanan.controller.DishController;
import com.mycompany.quanlyquanan.model.Category;
import com.mycompany.quanlyquanan.model.Dish;
import com.mycompany.quanlyquanan.utils.ComponentStyleUtil;
import com.mycompany.quanlyquanan.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
/**
 *
 * @author Administrator
 */
public class EditDishDialog extends javax.swing.JDialog {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(EditDishDialog.class.getName());
    
    private DishController dishController;
    private CategoryController categoryController;
    private List<Category> categories;
    private Dish dish;
    private File selectedImageFile;
    private String savedImagePath;
    private javax.swing.ButtonGroup statusGroup;    
    private QuanLyMonAnPanel parentPanel;
    

    public EditDishDialog(Dish selectedDish) {
        
        initComponents();
        dishController = new DishController();
        categoryController = new CategoryController();
        statusGroup = new javax.swing.ButtonGroup();
        statusGroup.add(ipTrueMerge);
        statusGroup.add(ipFalseMerge1);
        enableEditing();
        loadCategories();

    }
    // Constructor mới với parent panel
    public EditDishDialog(java.awt.Frame parent, boolean modal, Dish selectedDish, QuanLyMonAnPanel parentPanel) {
        super(parent, modal);
        initComponents();
        this.parentPanel = parentPanel;
        initializeDialog();
        setDish(selectedDish);
        setLocationRelativeTo(parent);
        ComponentStyleUtil.styleMainButton(bntAdd);
        ComponentStyleUtil.styleSecondButton(btnCancel);
    }
    
    // Constructor với Dialog parent
    public EditDishDialog(Dialog parent, boolean modal, Dish selectedDish, QuanLyMonAnPanel parentPanel) {
        super(parent, modal);
        initComponents();
        this.parentPanel = parentPanel;
        initializeDialog();
        setDish(selectedDish);
        setLocationRelativeTo(parent);
    }
    
    private void initializeDialog() {
        dishController = new DishController();
        categoryController = new CategoryController();
        statusGroup = new javax.swing.ButtonGroup();
        statusGroup.add(ipTrueMerge);
        statusGroup.add(ipFalseMerge1);
        enableEditing();
        loadCategories();
        
        // Set title cho dialog
        setTitle("Chỉnh sửa món ăn");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    private void enableEditing() {
        ipName.setEditable(true);
        ipName.setEnabled(true);
        ipCost.setEditable(true);
        ipCost.setEnabled(true);
        ipPrice.setEditable(true);
        ipPrice.setEnabled(true);
        ipCost1.setEditable(true);
        ipCost1.setEnabled(true);
        ipDescription.setEditable(true);
        ipDescription.setEnabled(true);
        ipCategory.setEnabled(true);
        ipTrueMerge.setSelected(true);
    }

    private void loadCategories() {
        categories = categoryController.getAllActive();
        ipCategory.removeAllItems();
        if (categories != null) {
            for (Category c : categories) {
                ipCategory.addItem(c.getName());
            }
        }
    }

    public void setDish(Dish dish) {
        this.dish = dish;
        populateFields();
    }

    private void populateFields() {
        if (dish == null) {
            return;
        }
        ipName.setText(dish.getName());
        if (dish.getCostPrice() != null) {
            ipCost.setText(dish.getCostPrice().toString());
        }
        if (dish.getPrice() != null) {
            ipPrice.setText(dish.getPrice().toString());
        }
        ipCost1.setText(String.valueOf(dish.getPrepTime()));
        ipDescription.setText(dish.getDescription() != null ? dish.getDescription() : "");
        if (categories != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == dish.getCategoryId()) {
                    ipCategory.setSelectedIndex(i);
                    break;
                }
            }
        }
        if (dish.isAvailable()) {
            ipTrueMerge.setSelected(true);
        } else {
            ipFalseMerge1.setSelected(true);
        }
        if (dish.getImageUrl() != null && !dish.getImageUrl().isEmpty()) {
            savedImagePath = dish.getImageUrl();
            setImage(savedImagePath);
        }
    }

    private void setImage(String path) {
        try {
            ImageIcon icon = new ImageIcon(path);
            Image image = icon.getImage().getScaledInstance(160, 175, Image.SCALE_SMOOTH);
            ipImage.setIcon(new ImageIcon(image));
        } catch (Exception e) {
            logger.warning("Could not load image: " + e.getMessage());
        }
    }

    private void chooseImage() {
        File file = ImageUtils.chooseImageFile(this);
        if (file != null) {
            try {
                String path = ImageUtils.saveImage(file, "assets/images/");
                selectedImageFile = file;
                savedImagePath = path;
                setImage(path);
            } catch (IOException ex) {
                logger.log(java.util.logging.Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu ảnh: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateDish() {
        if (dish == null) {
            JOptionPane.showMessageDialog(this, "Không có món ăn để cập nhật", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            dish.setName(ipName.getText().trim());
            if (categories != null && !categories.isEmpty()) {
                int index = ipCategory.getSelectedIndex();
                if (index >= 0 && index < categories.size()) {
                    dish.setCategoryId(categories.get(index).getId());
                }
            }
            if (!ipCost.getText().trim().isEmpty()) {
                dish.setCostPrice(new BigDecimal(ipCost.getText().trim()));
            }
            if (!ipPrice.getText().trim().isEmpty()) {
                dish.setPrice(new BigDecimal(ipPrice.getText().trim()));
            }
            if (!ipCost1.getText().trim().isEmpty()) {
                dish.setPrepTime(Integer.parseInt(ipCost1.getText().trim()));
            }
            dish.setDescription(ipDescription.getText());
            dish.setAvailable(ipTrueMerge.isSelected());
            if (savedImagePath != null) {
                dish.setImageUrl(savedImagePath);
            }
            boolean success = dishController.update(dish);
            if (success) {
                this.dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật món ăn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnProfile = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        ipImage = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        ipDescription = new javax.swing.JTextField();
        ipCategory = new javax.swing.JComboBox<>();
        btnThemAnh = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        bntAdd = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        ipName = new javax.swing.JTextField();
        ipCost = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        ipPrice = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        ipCost1 = new javax.swing.JTextField();
        ipFalseMerge1 = new javax.swing.JRadioButton();
        ipTrueMerge = new javax.swing.JRadioButton();
        jLabel26 = new javax.swing.JLabel();

        pnProfile.setBackground(new java.awt.Color(255, 255, 255));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setText("Sửa Món Ăn");

        ipImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/employee/coala-160.png"))); // NOI18N
        ipImage.setText("jLabel12");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel17.setText("Tên");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel23.setText("Chi phí:");

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel25.setText("Danh Mục:");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel18.setText("Mô tả:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel18)
                .addGap(18, 18, 18)
                .addComponent(ipDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(ipDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        ipCategory.setEditable(true);
        ipCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Admin", "Nhân Viên", "Bếp", "Thu Ngân" }));
        ipCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipCategoryActionPerformed(evt);
            }
        });

        btnThemAnh.setText("Thêm ảnh");
        btnThemAnh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnThemAnhMouseClicked(evt);
            }
        });
        btnThemAnh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemAnhActionPerformed(evt);
            }
        });

        btnCancel.setBackground(new java.awt.Color(255, 51, 51));
        btnCancel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCancel.setForeground(new java.awt.Color(255, 255, 255));
        btnCancel.setText("Hủy");
        btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCancelMouseClicked(evt);
            }
        });
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        bntAdd.setBackground(new java.awt.Color(0, 153, 0));
        bntAdd.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        bntAdd.setForeground(new java.awt.Color(255, 255, 255));
        bntAdd.setText("Sửa");
        bntAdd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bntAddMouseClicked(evt);
            }
        });
        bntAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bntAddActionPerformed(evt);
            }
        });

        jLabel1.setText("* Kích thước ảnh tối đa 160*160");

        ipName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipNameActionPerformed(evt);
            }
        });

        ipCost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipCostActionPerformed(evt);
            }
        });

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel28.setText("Giá bán:");

        ipPrice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipPriceActionPerformed(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel24.setText("Prep time:");

        ipCost1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipCost1ActionPerformed(evt);
            }
        });

        ipFalseMerge1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        ipFalseMerge1.setForeground(new java.awt.Color(102, 102, 102));
        ipFalseMerge1.setText("False");
        ipFalseMerge1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipFalseMerge1ActionPerformed(evt);
            }
        });

        ipTrueMerge.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        ipTrueMerge.setForeground(new java.awt.Color(0, 153, 51));
        ipTrueMerge.setText("True");
        ipTrueMerge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipTrueMergeActionPerformed(evt);
            }
        });

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel26.setText("Active :");

        javax.swing.GroupLayout pnProfileLayout = new javax.swing.GroupLayout(pnProfile);
        pnProfile.setLayout(pnProfileLayout);
        pnProfileLayout.setHorizontalGroup(
            pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnProfileLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addGap(291, 291, 291))
            .addGroup(pnProfileLayout.createSequentialGroup()
                .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnProfileLayout.createSequentialGroup()
                        .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnProfileLayout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ipImage, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1)))
                            .addGroup(pnProfileLayout.createSequentialGroup()
                                .addGap(80, 80, 80)
                                .addComponent(btnThemAnh)))
                        .addGap(37, 37, 37)
                        .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25)
                            .addGroup(pnProfileLayout.createSequentialGroup()
                                .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel17)
                                    .addComponent(jLabel23))
                                .addGap(34, 34, 34)
                                .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnProfileLayout.createSequentialGroup()
                                        .addComponent(ipCost, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(46, 46, 46)
                                        .addComponent(jLabel28)
                                        .addGap(34, 34, 34)
                                        .addComponent(ipPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(ipCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ipName, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(pnProfileLayout.createSequentialGroup()
                                .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel24)
                                    .addGroup(pnProfileLayout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(jLabel26)))
                                .addGap(17, 17, 17)
                                .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnProfileLayout.createSequentialGroup()
                                        .addComponent(ipTrueMerge)
                                        .addGap(36, 36, 36)
                                        .addComponent(ipFalseMerge1))
                                    .addComponent(ipCost1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(pnProfileLayout.createSequentialGroup()
                        .addGap(171, 171, 171)
                        .addComponent(bntAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(126, 126, 126)
                        .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(73, Short.MAX_VALUE))
        );
        pnProfileLayout.setVerticalGroup(
            pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnProfileLayout.createSequentialGroup()
                .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnProfileLayout.createSequentialGroup()
                        .addGap(81, 81, 81)
                        .addComponent(ipImage, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnThemAnh))
                    .addGroup(pnProfileLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel11)
                        .addGap(33, 33, 33)
                        .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(ipName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel25)
                            .addComponent(ipCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(ipCost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel28)
                            .addComponent(ipPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ipCost1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel24))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                        .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel26)
                            .addComponent(ipTrueMerge)
                            .addComponent(ipFalseMerge1))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bntAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(40, Short.MAX_VALUE)
                .addComponent(pnProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ipCostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipCostActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipCostActionPerformed

    private void ipNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipNameActionPerformed

    private void bntAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bntAddActionPerformed
        // TODO add your handling code here:
        updateDish();
    }//GEN-LAST:event_bntAddActionPerformed

    private void bntAddMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bntAddMouseClicked
        // TODO add your handling code here:
        updateDish();
    }//GEN-LAST:event_bntAddMouseClicked

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnCancelMouseClicked

    private void btnThemAnhActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemAnhActionPerformed
        // TODO add your handling code here:
        chooseImage();
    }//GEN-LAST:event_btnThemAnhActionPerformed

    private void btnThemAnhMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnThemAnhMouseClicked
        
        // TODO add your handling code here:
    }//GEN-LAST:event_btnThemAnhMouseClicked

    private void ipCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipCategoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipCategoryActionPerformed

    private void ipPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipPriceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipPriceActionPerformed

    private void ipCost1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipCost1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipCost1ActionPerformed

    private void ipFalseMerge1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipFalseMerge1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipFalseMerge1ActionPerformed

    private void ipTrueMergeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipTrueMergeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipTrueMergeActionPerformed

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
        //java.awt.EventQueue.invokeLater(() -> new EditDishDialog(selectedDish).setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bntAdd;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnThemAnh;
    private javax.swing.JComboBox<String> ipCategory;
    private javax.swing.JTextField ipCost;
    private javax.swing.JTextField ipCost1;
    private javax.swing.JTextField ipDescription;
    private javax.swing.JRadioButton ipFalseMerge1;
    private javax.swing.JLabel ipImage;
    private javax.swing.JTextField ipName;
    private javax.swing.JTextField ipPrice;
    private javax.swing.JRadioButton ipTrueMerge;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pnProfile;
    // End of variables declaration//GEN-END:variables
}
