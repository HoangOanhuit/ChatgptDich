/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.quanlyquanan.view.category;

import com.mycompany.quanlyquanan.controller.CategoryController;
import com.mycompany.quanlyquanan.model.Category;
import com.mycompany.quanlyquanan.utils.ComponentStyleUtil;
import com.mycompany.quanlyquanan.utils.Session;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import com.mycompany.quanlyquanan.utils.RedisSubscriber;
import com.mycompany.quanlyquanan.view.*;
import com.mycompany.quanlyquanan.view.Employee.CreateEmployeeDialog;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Admin
 */
public class QuanLyDanhMucPanel extends javax.swing.JPanel {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(QuanLyDanhMucPanel.class.getName());
    
    // Controllers & Models
    private CategoryController controller;
    private Category selectedCategory;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Creates new form QuanLyBanPanel
     */
    public QuanLyDanhMucPanel() {
        
       this.controller = new CategoryController();
        initComponents();
        customizeComponents();
        loadTableData();
        setupEventHandlers();
   
    }

   
    private void customizeComponents() {
        // Thiết lập table
        setupTable();
        
        // Thiết lập permissions
        setupPermissions();
        
        // Style cho buttons
//        styleButtons();

        ComponentStyleUtil.styleTableScrollPane(btnDeactivate);
        ComponentStyleUtil.styleTable(jTable1);
        ComponentStyleUtil.enableTableSorting(jTable1);
        ComponentStyleUtil.styleSearchTextField(ipSearch);
    }
    private void setupTable() {
        // Tạo model mới cho table
        String[] columns = {"ID", "Tên danh mục", "Mô tả", "Trạng thái", "Ngày tạo"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho edit trực tiếp trên table
            }
        };
        jTable1.setModel(model);
        
        // Styling table
//        jTable1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        jTable1.setRowHeight(35);
//        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        jTable1.setSelectionBackground(new Color(184, 207, 229));
//        jTable1.setGridColor(new Color(240, 240, 240));
//        jTable1.setShowHorizontalLines(true);
//        jTable1.setShowVerticalLines(false);
//
//        // Header styling
//        jTable1.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
//        jTable1.getTableHeader().setBackground(new Color(247, 255, 247));
//        jTable1.getTableHeader().setForeground(new Color(70, 0, 0));
//        jTable1.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(70, 0, 0)));
        
        // Column widths
        jTable1.getColumnModel().getColumn(0).setMaxWidth(60);
        jTable1.getColumnModel().getColumn(0).setMinWidth(60);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(200);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(300);
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(120);
        jTable1.getColumnModel().getColumn(4).setPreferredWidth(120);
        
        // Custom renderer cho cột trạng thái
        jTable1.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof Boolean) {
                    boolean active = (Boolean) value;
                    setText(active ? "Hoạt động" : "Tạm ngưng");
                    setForeground(active ? new Color(46, 125, 50) : new Color(244, 67, 54));
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    super.setValue(value);
                }
            }
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }
                return c;
            }
        });
        
        // Alternate row colors for other columns
        DefaultTableCellRenderer alternateRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }
                return c;
            }
        };
        
        for (int i = 0; i < jTable1.getColumnCount(); i++) {
            if (i != 3) { // Skip status column
                jTable1.getColumnModel().getColumn(i).setCellRenderer(alternateRenderer);
            }
        }
    }
    
    private void setupPermissions() {
        Session session = Session.getInstance();
        boolean canManage = session.canManageMenu();
        
        // Ẩn/hiện buttons dựa trên quyền
        btnAdd.setVisible(canManage);
        btnEdit.setVisible(canManage);  
        btnDelete.setVisible(canManage);
        
        // Chỉ admin mới thấy nút reset
        btnReset.setVisible(session.isAdmin());
        
        if (!canManage) {
            // Nếu không có quyền, chỉ cho phép xem và tìm kiếm
            jLabel2.setText("Xem Danh Mục Món Ăn");
        }
    }
    
    private void styleButtons() {
        // Style cho button Add
        btnAdd.setBackground(new Color(46, 125, 50));
        btnAdd.setForeground(Color.WHITE);
        
        // Style cho button Edit  
        btnEdit.setBackground(new Color(255, 193, 7));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setEnabled(false);
        
        // Style cho button Delete
        btnDelete.setBackground(new Color(244, 67, 54));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setEnabled(false);
        
        // Style cho button Search
        btnSearch.setBackground(new Color(23, 162, 184));
        btnSearch.setForeground(Color.WHITE);
        
        // Style cho button Reset
        btnReset.setBackground(new Color(108, 117, 125));
        btnReset.setForeground(Color.WHITE);
    }
    
    private void setupEventHandlers() {
        // Table selection handler
        jTable1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = jTable1.getSelectedRow();
                if (row != -1) {
                    int id = (int) jTable1.getValueAt(row, 0);
                    selectedCategory = controller.getById(id);
                    updateButtonStates();
                }
            }
        });
    }
    
    public void loadTableData() {
        List<Category> categories = controller.getAllCategories();
        loadTableData(categories);
    }
    
    public void loadTableData(List<Category> categories) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        
        for (Category category : categories) {
            model.addRow(new Object[]{
                category.getId(),
                category.getName(),
                category.getDescription() != null ? category.getDescription() : "Chưa có mô tả",
                category.isActive(),
                category.getCreatedAt() != null ? category.getCreatedAt().format(dateFormatter) : "N/A"
            });
        }
        
        // Select first row if available
        if (jTable1.getRowCount() > 0) {
            jTable1.setRowSelectionInterval(0, 0);
            int id = (int) jTable1.getValueAt(0, 0);
            selectedCategory = controller.getById(id);
            updateButtonStates();
        }
    }
    
    private void updateButtonStates() {
        boolean hasSelection = selectedCategory != null;
        btnEdit.setEnabled(hasSelection);
        btnDelete.setEnabled(hasSelection);
        
        // Cập nhật tooltip
        if (hasSelection) {
            btnEdit.setToolTipText("Chỉnh sửa danh mục: " + selectedCategory.getName());
            btnDelete.setToolTipText("Xóa danh mục: " + selectedCategory.getName());
        }
    }
    
    private List<Integer> getSelectedCategoryIds() {
        List<Integer> selectedIds = new ArrayList<>();
        int row = jTable1.getSelectedRow();
        if (row != -1) {
            selectedIds.add((Integer) jTable1.getValueAt(row, 0));
        }
        return selectedIds;
    }
    private void activateCategory() {
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục");
            return;
        }
        if (!selectedCategory.isActive() && controller.toggleStatus(selectedCategory.getId())) {
            loadTableData();
        }
    }

    private void deactivateCategory() {
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục");
            return;
        }
        if (selectedCategory.isActive() && controller.toggleStatus(selectedCategory.getId())) {
            loadTableData();
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

        toolNav = new javax.swing.JPanel();
        ipSearch = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btnDelete = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        btnActivate = new javax.swing.JButton();
        btnDeactivateDish = new javax.swing.JButton();
        btnDeactivate = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        toolNav.setBackground(new java.awt.Color(255, 255, 255));
        toolNav.setPreferredSize(new java.awt.Dimension(1099, 100));

        ipSearch.setPreferredSize(new java.awt.Dimension(200, 40));
        ipSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipSearchActionPerformed(evt);
            }
        });
        ipSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ipSearchKeyPressed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        jLabel2.setText("Quản lý Danh Mục");
        jLabel2.setPreferredSize(new java.awt.Dimension(200, 21));

        btnDelete.setBackground(new java.awt.Color(255, 242, 242));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/delete-40.png"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.setBorder(null);
        btnDelete.setBorderPainted(false);
        btnDelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDelete.setFocusable(false);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.setOpaque(true);
        btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDelete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnDeleteMouseClicked(evt);
            }
        });
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnAdd.setBackground(new java.awt.Color(255, 242, 242));
        btnAdd.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/plus-40.png"))); // NOI18N
        btnAdd.setText("Add");
        btnAdd.setBorder(null);
        btnAdd.setBorderPainted(false);
        btnAdd.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAdd.setFocusable(false);
        btnAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAdd.setOpaque(true);
        btnAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAdd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAddMouseClicked(evt);
            }
        });
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnEdit.setBackground(new java.awt.Color(255, 242, 242));
        btnEdit.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/pencil-40.png"))); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.setBorder(null);
        btnEdit.setBorderPainted(false);
        btnEdit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEdit.setFocusable(false);
        btnEdit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEdit.setOpaque(true);
        btnEdit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnEdit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnEditMouseClicked(evt);
            }
        });
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnSearch.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/search-40.png"))); // NOI18N
        btnSearch.setBorder(null);
        btnSearch.setBorderPainted(false);
        btnSearch.setContentAreaFilled(false);
        btnSearch.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSearch.setFocusPainted(false);
        btnSearch.setFocusable(false);
        btnSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnSearchMouseClicked(evt);
            }
        });
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        btnReset.setBackground(new java.awt.Color(244, 244, 244));
        btnReset.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/reset-password-40.png"))); // NOI18N
        btnReset.setText("Reset");
        btnReset.setBorder(null);
        btnReset.setBorderPainted(false);
        btnReset.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnReset.setFocusable(false);
        btnReset.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReset.setOpaque(true);
        btnReset.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnReset.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnResetMouseClicked(evt);
            }
        });
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        btnActivate.setBackground(new java.awt.Color(244, 244, 244));
        btnActivate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnActivate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/verified-account-40.png"))); // NOI18N
        btnActivate.setText("Activate");
        btnActivate.setBorder(null);
        btnActivate.setBorderPainted(false);
        btnActivate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnActivate.setFocusable(false);
        btnActivate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnActivate.setOpaque(true);
        btnActivate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnActivate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnActivateMouseClicked(evt);
            }
        });
        btnActivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActivateActionPerformed(evt);
            }
        });

        btnDeactivateDish.setBackground(new java.awt.Color(244, 244, 244));
        btnDeactivateDish.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeactivateDish.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/delete-user-40.png"))); // NOI18N
        btnDeactivateDish.setText("Deactivate");
        btnDeactivateDish.setBorder(null);
        btnDeactivateDish.setBorderPainted(false);
        btnDeactivateDish.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDeactivateDish.setFocusable(false);
        btnDeactivateDish.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDeactivateDish.setOpaque(true);
        btnDeactivateDish.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDeactivateDish.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnDeactivateDishMouseClicked(evt);
            }
        });
        btnDeactivateDish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeactivateDishActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout toolNavLayout = new javax.swing.GroupLayout(toolNav);
        toolNav.setLayout(toolNavLayout);
        toolNavLayout.setHorizontalGroup(
            toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolNavLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnActivate, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDeactivateDish, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ipSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch)
                .addGap(16, 16, 16))
        );
        toolNavLayout.setVerticalGroup(
            toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolNavLayout.createSequentialGroup()
                .addGroup(toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(toolNavLayout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(toolNavLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDeactivateDish, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnActivate, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(toolNavLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnSearch)
                                    .addComponent(ipSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap())
        );

        btnDeactivate.setBackground(new java.awt.Color(255, 255, 255));
        btnDeactivate.setBorder(null);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Tên danh mục", "Mô tả", "Trạng thái", "Ngày tạo"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setColumnSelectionAllowed(true);
        btnDeactivate.setViewportView(jTable1);
        jTable1.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolNav, javax.swing.GroupLayout.PREFERRED_SIZE, 1050, Short.MAX_VALUE)
            .addComponent(btnDeactivate, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeactivate, javax.swing.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ipSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipSearchActionPerformed
        // TODO add your handling code here:
        performSearch();
    }//GEN-LAST:event_ipSearchActionPerformed

    private void ipSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ipSearchKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            performSearch();
        }
    }//GEN-LAST:event_ipSearchKeyPressed

    private void btnDeleteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDeleteMouseClicked

    }//GEN-LAST:event_btnDeleteMouseClicked

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn danh mục cần xóa!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn xóa danh mục '" + selectedCategory.getName() + "'?\n" +
            "Thao tác này không thể hoàn tác!",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = controller.permanentDelete(selectedCategory.getId());
            if (success) {
                loadTableData();
                selectedCategory = null;
                updateButtonStates();
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnAddMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddMouseClicked
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        CreateCategoryDialog createDialog = new CreateCategoryDialog(parentFrame, true, this);
        createDialog.setLocationRelativeTo(parentFrame);
        createDialog.setVisible(true);

    }//GEN-LAST:event_btnAddMouseClicked

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnEditMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditMouseClicked
        // TODO add your handling code here:
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn danh mục cần sửa!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        EditCategoryDialog editDialog = new EditCategoryDialog(parentFrame, true, this, selectedCategory);
        editDialog.setLocationRelativeTo(parentFrame);
        editDialog.setVisible(true);
        

    }//GEN-LAST:event_btnEditMouseClicked

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        

    }//GEN-LAST:event_btnEditActionPerformed

    private void btnSearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSearchMouseClicked

        // TODO add your handling code here:
        performSearch();
    }//GEN-LAST:event_btnSearchMouseClicked

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        performSearch();
        // TODO add your handling code here:
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnResetMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnResetMouseClicked
        
        ipSearch.setText("");
        loadTableData();
        
        JOptionPane.showMessageDialog(this, 
            "Đã reset danh sách!", 
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        // TODO add your handling code here:
    }//GEN-LAST:event_btnResetMouseClicked

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnActivateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnActivateMouseClicked
        activateCategory();
        // TODO add your handling code here:
    }//GEN-LAST:event_btnActivateMouseClicked

    private void btnActivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActivateActionPerformed
        // TODO add your handling code here:
        activateCategory();
    }//GEN-LAST:event_btnActivateActionPerformed

    private void btnDeactivateDishMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDeactivateDishMouseClicked
        // TODO add your handling code here:
        deactivateCategory();
    }//GEN-LAST:event_btnDeactivateDishMouseClicked

    private void btnDeactivateDishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeactivateDishActionPerformed
        // TODO add your handling code here:
        deactivateCategory();
    }//GEN-LAST:event_btnDeactivateDishActionPerformed
    
    private void performSearch() {
        String keyword = ipSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadTableData(); // Load all if search is empty
        } else {
            List<Category> searchResults = controller.search(keyword);
            loadTableData(searchResults);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActivate;
    private javax.swing.JButton btnAdd;
    private javax.swing.JScrollPane btnDeactivate;
    private javax.swing.JButton btnDeactivateDish;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JTextField ipSearch;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel toolNav;
    // End of variables declaration//GEN-END:variables
}
