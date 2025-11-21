package com.mycompany.quanlytruyen.view.accounts;

import com.mycompany.quanlytruyen.config.AppConfig;
import com.mycompany.quanlytruyen.dao.AccountDAO;
import com.mycompany.quanlytruyen.dao.DataSourceFactory;
import com.mycompany.quanlytruyen.model.Account;
import com.mycompany.quanlytruyen.view.post.SuaLichPost;

import javax.sql.DataSource;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QLTK extends javax.swing.JPanel {

    private AccountDAO accountDao;
    private DefaultTableModel tableModel;
    private final List<Account> accounts = new ArrayList<>();
    private Account selectedAccount;
    private DataSource dataSource;
    private static final int COLUMN_SCHEDULE = 5;

    /**
     * Creates new form QLSach
     */
    public QLTK() {
        initComponents();
        initialize();
    }

    private void initialize() {
        setupTable();
        initDao();
        loadAccounts();
        attachListeners();
    }

    private void setupTable() {
        tableModel = new DefaultTableModel(new Object[]{
            "ID", "Tên TK", "Email", "Mật khẩu", "Ghi chú", "Lịch Đăng"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == COLUMN_SCHEDULE;
            }
        };
        Tbooks.setModel(tableModel);
        Tbooks.setAutoCreateRowSorter(true);
        Tbooks.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        configureScheduleColumn();
    }

    private void configureScheduleColumn() {
        TableCellRenderer renderer = new ScheduleButtonRenderer();
        TableCellEditor editor = new ScheduleButtonEditor();
        Tbooks.getColumnModel().getColumn(COLUMN_SCHEDULE).setCellRenderer(renderer);
        Tbooks.getColumnModel().getColumn(COLUMN_SCHEDULE).setCellEditor(editor);
        Tbooks.getColumnModel().getColumn(COLUMN_SCHEDULE).setPreferredWidth(110);
    }
    
    private void initDao() {
        try {
            AppConfig config = AppConfig.getInstance();
            dataSource = DataSourceFactory.create(
                config.getDatabaseUrl(),
                config.getDatabaseUser(),
                config.getDatabasePassword()
            );
            accountDao = new AccountDAO(dataSource);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối cơ sở dữ liệu: " + e.getMessage());
        }
    }

    private void attachListeners() {
        Tbooks.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateSelectedAccount();
                }
            }
        });
        Tbooks.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int viewRow = Tbooks.rowAtPoint(e.getPoint());
                    int viewColumn = Tbooks.columnAtPoint(e.getPoint());
                    if (viewRow < 0 || viewColumn < 0) {
                        return;
                    }
                    int modelColumn = Tbooks.convertColumnIndexToModel(viewColumn);
                    if (modelColumn == COLUMN_SCHEDULE) {
                        return;
                    }                    
                    openEditDialog();
                }
            }
        });
    }

    private void loadAccounts() {
        accounts.clear();
        if (accountDao == null) {
            return;
        }
        try {
            accounts.addAll(accountDao.getAllAccounts());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải tài khoản: " + e.getMessage());
        }
        refreshTable(accounts);
        selectedAccount = null;
    }

    private void refreshTable(List<Account> data) {
        tableModel.setRowCount(0);
        if (data == null) {
            return;
        }
        for (Account account : data) {
            tableModel.addRow(new Object[]{
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.getPassword(),
                account.getNote(),
                "Lịch"
            });
        }
    }

    private void updateSelectedAccount() {
        int viewRow = Tbooks.getSelectedRow();
        if (viewRow < 0) {
            selectedAccount = null;
            return;
        }
        int modelRow = Tbooks.convertRowIndexToModel(viewRow);
        if (modelRow >= 0 && modelRow < accounts.size()) {
            selectedAccount = accounts.get(modelRow);
        } else {
            selectedAccount = null;
        }
    }

    private void openAddDialog() {
        if (accountDao == null) {
            JOptionPane.showMessageDialog(this, "Chưa kết nối được tới cơ sở dữ liệu");
            return;
        }
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        ThemTK dialog = new ThemTK(frame, true, this);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void openEditDialog() {
        if (selectedAccount == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần sửa");
            return;
        }
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        SuaTK dialog = new SuaTK(frame, true, this, selectedAccount);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void openScheduleDialogFromRow(int modelRow) {
        if (modelRow < 0) {
            return;
        }
        if (dataSource == null) {
            JOptionPane.showMessageDialog(this, "Chưa kết nối được tới cơ sở dữ liệu");
            return;
        }
        Account account = getAccountFromTable(modelRow);
        if (account == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin tài khoản");
            return;
        }
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        SuaLichPost dialog = new SuaLichPost(frame, true, dataSource, null, account);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private Account getAccountFromTable(int modelRow) {
        if (modelRow < 0 || modelRow >= tableModel.getRowCount()) {
            return null;
        }
        Object value = tableModel.getValueAt(modelRow, 0);
        if (!(value instanceof Number)) {
            return null;
        }
        long accountId = ((Number) value).longValue();
        return findAccountById(accountId);
    }

    private Account findAccountById(long accountId) {
        for (Account account : accounts) {
            if (account.getId() != null && account.getId() == accountId) {
                return account;
            }
        }
        return null;
    }

    private static class ScheduleButtonRenderer extends JButton implements TableCellRenderer {
        private ScheduleButtonRenderer() {
            setOpaque(true);
            setText("Lịch");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "Lịch");
            return this;
        }
    }

    private class ScheduleButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private final JButton button = new JButton();
        private int currentRow = -1;

        private ScheduleButtonEditor() {
            button.addActionListener(this);
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            button.setText(value != null ? value.toString() : "Lịch");
            currentRow = row;
            return button;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped();
            if (currentRow < 0) {
                return;
            }
            int modelRow = Tbooks.convertRowIndexToModel(currentRow);
            openScheduleDialogFromRow(modelRow);
        }
    }
    
    private void deleteSelectedAccount() {
        if (accountDao == null) {
            JOptionPane.showMessageDialog(this, "Chưa kết nối được tới cơ sở dữ liệu");
            return;
        }
        if (selectedAccount == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần xóa");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn xóa tài khoản '" + selectedAccount.getUsername() + "' ?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        accountDao.deleteAccount(selectedAccount.getId());
        JOptionPane.showMessageDialog(this, "Xóa tài khoản thành công");
        loadAccounts();
    }
    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            refreshTable(accounts);
            return;
        }
        List<Account> filtered = new ArrayList<>();
        for (Account account : accounts) {
            if (containsIgnoreCase(account.getUsername(), keyword)
                || containsIgnoreCase(account.getEmail(), keyword)
                || containsIgnoreCase(account.getUuid(), keyword)
                || containsIgnoreCase(account.getStatus(), keyword)) {
                
                filtered.add(account);
            }
        }
        refreshTable(filtered);
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (source == null) {
            return false;
        }
        return source.toLowerCase().contains(keyword.toLowerCase());
    }

    public void refreshAccounts() {
        loadAccounts();
    }

    public AccountDAO getAccountDao() {
        return accountDao;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        btnDelete = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Tbooks = new javax.swing.JTable();

        jPanel2.setBackground(new java.awt.Color(0, 40, 85));
        jPanel2.setForeground(new java.awt.Color(255, 255, 255));

        btnDelete.setBackground(new java.awt.Color(0, 40, 85));
        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/delete.png"))); // NOI18N
        btnDelete.setBorder(null);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnAdd.setBackground(new java.awt.Color(0, 40, 85));
        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/add.png"))); // NOI18N
        btnAdd.setBorder(null);
        btnAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnEdit.setBackground(new java.awt.Color(0, 40, 85));
        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/edit.png"))); // NOI18N
        btnEdit.setBorder(null);
        btnEdit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnReset.setBackground(new java.awt.Color(0, 40, 85));
        btnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/refresh.png"))); // NOI18N
        btnReset.setBorder(null);
        btnReset.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        txtSearch.setForeground(new java.awt.Color(153, 153, 153));
        txtSearch.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        btnSearch.setBackground(new java.awt.Color(0, 40, 85));
        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/search.png"))); // NOI18N
        btnSearch.setBorder(null);
        btnSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("UVN Chim Bien", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Quản Lý Tài Khoản");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jLabel1)
                .addGap(127, 127, 127)
                .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 219, Short.MAX_VALUE)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
        );

        Tbooks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Tên TK", "Email", "Mật khẩu", "Ghi chú", "Lịch Đăng"
            }
        ));
        jScrollPane1.setViewportView(Tbooks);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 663, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        deleteSelectedAccount();
 
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
          openAddDialog();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
            openEditDialog();
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
        txtSearch.setText("");
        loadAccounts();
    }//GEN-LAST:event_btnResetActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
        performSearch();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
        performSearch();
    }//GEN-LAST:event_btnSearchActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Tbooks;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
