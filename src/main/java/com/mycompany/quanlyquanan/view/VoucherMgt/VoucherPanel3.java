/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.quanlyquanan.view.VoucherMgt;

import com.mycompany.quanlyquanan.controller.VoucherController;
import com.mycompany.quanlyquanan.controller.VoucherUsageController;
import com.mycompany.quanlyquanan.model.ModelVoucher;
import com.mycompany.quanlyquanan.service.VoucherService;
import com.mycompany.quanlyquanan.service.VoucherUsageService;
import com.mycompany.quanlyquanan.utils.ComponentStyleUtil;
import com.mycompany.quanlyquanan.view.table.CreateTableDialog;
import com.mycompany.quanlyquanan.view.table.EditTableDialog;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.KeyEvent;
import java.util.List;

public class VoucherPanel3 extends javax.swing.JPanel {

    VoucherService vcService;
    DefaultTableModel defaultTableModel;
    private int hoveredRow = -1;
    private int itemsPerPage = 10; //
    private int currentPage = 1;
    private List<ModelVoucher> allVouchers;
    private VoucherUsageService voucherUsageService;
    private VoucherController vcController;
    private VoucherUsageController vcUsageController;


    public VoucherPanel3() {
        initComponents();
        
        ComponentStyleUtil.styleSearchTextField(ipSearch);
//        btnSearch.setBorder(BorderFactory.createEmptyBorder()); // Xóa border của iconSearch
//        btnSearch.setContentAreaFilled(false); // Không vẽ nền button
//        btnSearch.setFocusPainted(false);      // Không vẽ viền khi focus
//        btnSearch.setOpaque(false);            // Không vẽ nền button
        vcController = new VoucherController();
        vcUsageController = new VoucherUsageController();
//        liveSearch();
        initTable();
        initTableData();

    }

    private void initTableData() {
        allVouchers = vcController.getAll();
        setTableData(allVouchers);
    }

//    private void liveSearch() {
//        ipSearch.getDocument().addDocumentListener(new DocumentListener() {
//            public void insertUpdate(DocumentEvent e) {
//                searchNow();
//            }
//            public void removeUpdate(DocumentEvent e) {
//                searchNow();
//            }
//
//            public void changedUpdate(DocumentEvent e) {
//                searchNow();
//            }
//
//            private void searchNow() {
//                String keyword = ipSearch.getText().trim();
//                if (keyword.equals(ipSearch.getText().trim())) {
//                    keyword = "";
//                }
//                allVouchers = vcController.getVoucherByVoucherCode(keyword);
////                int totalItems = allVouchers.size();
////                int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
////                currentPage = 1;
////                pagination1.setPagegination(currentPage, totalPages);
////                updateTableDataForPage();
//                setTableData(allVouchers);
//            }
//        });
//    }

    private void initTable() {
        defaultTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8;
            }
        };

        // Cập nhật các cột của bảng
        String[] columns = {
                "ID", "Voucher Code", "Description", "Percentage", "Quantity", "Start Date", "End Date", "Remaining"
//                , "Action"
        };
        for (String col : columns) {
            defaultTableModel.addColumn(col);
        }
        voucherTable.setModel(defaultTableModel);

        // Chỉnh lề phải cho các cột
        String[] rightColumns = {"Percentage", "Quantity", "Remaining", "Start Date", "End Date"};
        for (String colName : rightColumns) {
            voucherTable.getColumn(colName).setCellRenderer(MyTable.getRightRenderer(6));
        }

        // Chỉnh lề trái cho các cột
        String[] leftColumns = {"ID", "Voucher Code", "Description"};
        for (String colName : leftColumns) {
            voucherTable.getColumn(colName).setCellRenderer(MyTable.getLeftRenderer(6));
        }

        // Thiết lập kích thước cột và chiều cao hàng của table
        voucherTable.setColumnWidths();
//        voucherTable.setRowHeight(50);

        voucherTable.fixTable(jScrollPane1);
        ComponentStyleUtil.styleTable(voucherTable);

    }


    // Hàm dùng để set dữ liệu bảng với danh sách voucher từ cơ sở dữ liệu + remaining voucher (tự tính)
    private void setTableData(List<ModelVoucher> vouchers) {
        defaultTableModel.setRowCount(0);
        for (ModelVoucher vc : vouchers) {
            int used = vcUsageController.countVoucherUsageById(vc.getId());
            int remaining = vc.getQuantity() - used;
            defaultTableModel.addRow(new Object[]{
                    vc.getId(),
                    vc.getVoucherCode(),
                    vc.getDescription(),
                    vc.getPercentage(),
                    vc.getQuantity(),
                    vc.getStartDate(),
                    vc.getEndDate(),
                    remaining // Thêm dữ liệu vào cột Remaining
            });

        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolNav = new javax.swing.JPanel();
        ipSearch = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        deleteVoucher = new javax.swing.JButton();
        addVoucher = new javax.swing.JButton();
        editVoucher = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        voucherTable = new com.mycompany.quanlyquanan.view.VoucherMgt.MyTable();

        setBackground(new java.awt.Color(244, 244, 244));
        setPreferredSize(new java.awt.Dimension(1050, 850));

        toolNav.setBackground(new java.awt.Color(255, 255, 255));
        toolNav.setPreferredSize(new java.awt.Dimension(1300, 100));

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

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("Quản lý Khuyến mãi");
        jLabel2.setPreferredSize(new java.awt.Dimension(200, 22));

        deleteVoucher.setBackground(new java.awt.Color(255, 242, 242));
        deleteVoucher.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        deleteVoucher.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/delete-40.png"))); // NOI18N
        deleteVoucher.setText("Delete");
        deleteVoucher.setBorder(null);
        deleteVoucher.setBorderPainted(false);
        deleteVoucher.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        deleteVoucher.setFocusable(false);
        deleteVoucher.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteVoucher.setOpaque(true);
        deleteVoucher.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteVoucher.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteVoucherMouseClicked(evt);
            }
        });
        deleteVoucher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteVoucherActionPerformed(evt);
            }
        });

        addVoucher.setBackground(new java.awt.Color(255, 242, 242));
        addVoucher.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        addVoucher.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/plus-40.png"))); // NOI18N
        addVoucher.setText("Add");
        addVoucher.setBorder(null);
        addVoucher.setBorderPainted(false);
        addVoucher.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addVoucher.setFocusable(false);
        addVoucher.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addVoucher.setOpaque(true);
        addVoucher.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addVoucher.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addVoucherMouseClicked(evt);
            }
        });
        addVoucher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addVoucherActionPerformed(evt);
            }
        });

        editVoucher.setBackground(new java.awt.Color(255, 242, 242));
        editVoucher.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        editVoucher.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/pencil-40.png"))); // NOI18N
        editVoucher.setText("Edit");
        editVoucher.setBorder(null);
        editVoucher.setBorderPainted(false);
        editVoucher.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        editVoucher.setFocusable(false);
        editVoucher.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editVoucher.setOpaque(true);
        editVoucher.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editVoucher.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editVoucherMouseClicked(evt);
            }
        });
        editVoucher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editVoucherActionPerformed(evt);
            }
        });

        btnSearch.setBackground(new java.awt.Color(255, 242, 242));
        btnSearch.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnSearch.setForeground(new java.awt.Color(255, 255, 255));
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

        javax.swing.GroupLayout toolNavLayout = new javax.swing.GroupLayout(toolNav);
        toolNav.setLayout(toolNavLayout);
        toolNavLayout.setHorizontalGroup(
            toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolNavLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(addVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(editVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(deleteVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                            .addComponent(addVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(editVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deleteVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, toolNavLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ipSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(31, 31, 31))
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(1050, 940));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(1000, 600));

        voucherTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Voucher Code", "Description", "Percentage", "Quantity", "Start Date", "End Date", "Action"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        voucherTable.setPreferredScrollableViewportSize(new java.awt.Dimension(600, 600));
        jScrollPane1.setViewportView(voucherTable);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1044, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(toolNav, javax.swing.GroupLayout.DEFAULT_SIZE, 1050, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(toolNav, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 652, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(254, 254, 254))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ipSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipSearchActionPerformed

    private void ipSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ipSearchKeyPressed

        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
//            liveSearch();
            String keyword = ipSearch.getText().trim();
            if (keyword.equals(ipSearch.getText().trim())) {
                keyword = "";
            }
            allVouchers = vcController.getVoucherByVoucherCode(keyword);
//                int totalItems = allVouchers.size();
//                int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
//                currentPage = 1;
//                pagination1.setPagegination(currentPage, totalPages);
//                updateTableDataForPage();
            setTableData(allVouchers);

        }
    }//GEN-LAST:event_ipSearchKeyPressed

    private void deleteVoucherMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteVoucherMouseClicked

    }//GEN-LAST:event_deleteVoucherMouseClicked

//    private void deleteVoucherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteVoucherActionPerformed
//        // TODO add your handling code here:
//        List<Integer> selectedIds = getSelectedTableIds();
//        if (selectedIds.isEmpty()) {
//            return;
//        }
//
//        int firstId = selectedIds.get(0);
//
//        int confirm = JOptionPane.showConfirmDialog(
//            this,
//            "Bạn có chắc muốn xóa " + selectedIds.size() + " bàn?",
//            "Xác nhận",
//            JOptionPane.YES_NO_OPTION
//        );
//
//        if (confirm == JOptionPane.YES_OPTION) {
//            controller.deleteTable(firstId);
//            loadTableData();
//        }
//    }//GEN-LAST:event_deleteVoucherActionPerformed

    private void addVoucherMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addVoucherMouseClicked
//        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
//        CreateTableDialog createTableDialog = new CreateTableDialog(parentFrame, true, this);
//        createTableDialog.setLocationRelativeTo(parentFrame);
//        createTableDialog.setVisible(true);
    }//GEN-LAST:event_addVoucherMouseClicked

//    private void addVoucherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addVoucherActionPerformed
//        // TODO add your handling code here:
//    }//GEN-LAST:event_addVoucherActionPerformed

    private void editVoucherMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editVoucherMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_editVoucherMouseClicked

//    private void editVoucherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editVoucherActionPerformed
//        // TODO add your handling code here:
//        List<Integer> selectedIds = getSelectedTableIds();
//
//        if (selectedIds.isEmpty()) {
//            return; // không chọn bàn nào
//        }
//
//        // Chỉ lấy bàn đầu tiên để edit
//        int firstId = selectedIds.get(0);
//
//        Table table = controller.getTableById(firstId);
//
//        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
//        EditTableDialog editDialog = new EditTableDialog(parentFrame, true, this, table);
//        editDialog.setLocationRelativeTo(parentFrame);
//        editDialog.setVisible(true);
//    }//GEN-LAST:event_editVoucherActionPerformed

    private void btnSearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSearchMouseClicked

        // TODO add your handling code here:
    }//GEN-LAST:event_btnSearchMouseClicked

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed

            String keyword = ipSearch.getText().trim();
            if (keyword.isEmpty()) {
                setTableData(allVouchers);
            } else{
                List<ModelVoucher> searchResults = vcController.getVoucherByVoucherCode(keyword);

//                int totalItems = allVouchers.size();
//                int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
//                currentPage = 1;
//                pagination1.setPagegination(currentPage, totalPages);
//                updateTableDataForPage();
                setTableData(searchResults);
            }



    }//GEN-LAST:event_btnSearchActionPerformed

    private void deleteVoucherActionPerformed(ActionEvent evt) {
        int row = voucherTable.getSelectedRow();
        if(row == -1)
        {
            JOptionPane.showMessageDialog(VoucherPanel3.this,"Vui lòng chọn voucher muốn xóa", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }else{
            int confirm = JOptionPane.showConfirmDialog(VoucherPanel3.this, "Bạn chắc chắn muốn xóa?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION){
                int voucherId = Integer.valueOf(String.valueOf(voucherTable.getValueAt(row, 0)));
                vcController.deleteVoucher(voucherId);
                defaultTableModel.setRowCount(0);
                setTableData(vcController.getAll());
                defaultTableModel.fireTableDataChanged(); // Đảm bảo UI cập nhật lại
            }
        }
    }

    private void editVoucherActionPerformed(java.awt.event.ActionEvent evt) {                                            
        // TODO add your handling code here:
        int row = voucherTable.getSelectedRow();
        if(row == -1)
        {
            JOptionPane.showMessageDialog(VoucherPanel3.this,"Vui lòng chọn voucher muốn cập nhật!", "Lỗi",JOptionPane.ERROR_MESSAGE);
        }else{
            int voucherId = Integer.valueOf(String.valueOf(voucherTable.getValueAt(row, 0)));
            new EditVoucherFrame(voucherId,()->{
                defaultTableModel.setRowCount(0);
                setTableData(vcController.getAll());
                defaultTableModel.fireTableDataChanged();
            }).setVisible(true);
//            this.dispose();


        }
    }

    // Thêm sự kiện cho nút "Add Voucher"
    private void addVoucherActionPerformed(java.awt.event.ActionEvent evt) {
//        new AddVoucherFrame().setVisible(true);
        // Nếu muốn đóng panel, có thể fire sự kiện hoặc callback tại đây
//
//        setTableData(vcController.getAll());
//        defaultTableModel.fireTableDataChanged();
        new AddVoucherFrame(() -> {
            defaultTableModel.setRowCount(0);
            setTableData(vcController.getAll());
        }).setVisible(true);
            defaultTableModel.fireTableDataChanged(); // Đảm bảo UI cập nhật lại

    }




    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addVoucher;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton deleteVoucher;
    private javax.swing.JButton editVoucher;
    private javax.swing.JTextField ipSearch;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel toolNav;
    private com.mycompany.quanlyquanan.view.VoucherMgt.MyTable voucherTable;
    // End of variables declaration//GEN-END:variables
}
