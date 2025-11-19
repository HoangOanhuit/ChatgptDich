package com.mycompany.quanlytruyen.view.post;

import com.mycompany.quanlytruyen.dao.AccountDAO;
import com.mycompany.quanlytruyen.dao.BookDao;
import com.mycompany.quanlytruyen.dao.PostDao;
import com.mycompany.quanlytruyen.model.Account;
import com.mycompany.quanlytruyen.model.Book;
import com.mycompany.quanlytruyen.model.Post;

import javax.sql.DataSource;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Frame;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SuaLichPost extends javax.swing.JDialog {

    private static final Logger LOGGER = Logger.getLogger(SuaLichPost.class.getName());
    private static final int[][] DEFAULT_SCHEDULE = {
        {10, 3},
        {11, 3},
        {14, 3},
        {16, 3},
        {19, 3},
        {20, 3},
        {20, 51},
        {21, 33},
        {22, 33},
        {23, 33}
    };

    private final DataSource dataSource;
    private final BookDao bookDao;
    private final PostDao postDao;
    private final AccountDAO accountDao;
    private final Book book;
    private Account account;
    private final ScheduleTableModel scheduleTableModel = new ScheduleTableModel();
    private boolean updatingChapterField;

    public SuaLichPost(Frame parent, boolean modal, DataSource dataSource, Book book, Account account) {
        super(parent, modal);
        this.dataSource = dataSource;
        this.book = book;
        this.account = account;
        this.postDao = dataSource != null ? new PostDao(dataSource) : null;
        this.bookDao = dataSource != null ? new BookDao(dataSource) : null;
        this.accountDao = dataSource != null ? new AccountDAO(dataSource) : null;
        initComponents();
      
        initializeForm();
    }

   private void initializeForm() {
        setTitle(buildDialogTitle());
        Tbooks.setModel(scheduleTableModel);
        Tbooks.setRowHeight(28);
        Tbooks.setFillsViewportHeight(true);
        Tbooks.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        loadAccountInfo();

        chapterPerDay.getDocument().addDocumentListener(new SimpleDocumentListener(this::handleChapterPerDayChanged));

        loadExistingSchedule();
    }

    private void loadAccountInfo() {
        if (account != null && account.getUsername() != null) {
            txtYeuCau3.setText(account.getUsername());
            setTitle(buildDialogTitle());
            return;
        }
        if (book == null || book.getAccountId() == null || accountDao == null) {
            txtYeuCau3.setText("(Chưa gán)");
            setTitle(buildDialogTitle());
            return;
        }
        try {
            account = accountDao.getAccountById(book.getAccountId());
            txtYeuCau3.setText(account != null && account.getUsername() != null
                ? account.getUsername()
                : "(Chưa gán)");
            setTitle(buildDialogTitle());
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Không thể tải thông tin tài khoản", ex);
            txtYeuCau3.setText("(Không tải được)");
            JOptionPane.showMessageDialog(this, "Không thể tải thông tin tài khoản: " + ex.getMessage());
        }
    }

    private String buildDialogTitle() {
        if (account != null && account.getUsername() != null && !account.getUsername().isBlank()) {
            return "Lịch Post của Tài Khoản - " + account.getUsername();
        }
        return "Lịch Post của Tài Khoản";
    }


    private String resolveBookTitle() {
        if (book == null) {
            return "";
        }
        if (book.getShortTitle() != null && !book.getShortTitle().isBlank()) {
            return book.getShortTitle();
        }
        return book.getTitle() != null ? book.getTitle() : "";
    }

    private void loadExistingSchedule() {
        if (account == null || account.getId() == null || postDao == null) {
            scheduleTableModel.applyDefaultSchedule();
            setChapterPerDayField(scheduleTableModel.getRowCount());
            if (account == null || account.getId() == null) {
                JOptionPane.showMessageDialog(this, "Chưa có tài khoản để tải lịch post.");
            }            
            return;
        }
        try {
            List<Post> posts = postDao.findByAccountId(account.getId());
            if (!posts.isEmpty()) {
                scheduleTableModel.setPosts(posts);
                setChapterPerDayField(posts.size());
            } else {
                scheduleTableModel.applyDefaultSchedule();
                setChapterPerDayField(scheduleTableModel.getRowCount());
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Không thể tải lịch post", ex);
            JOptionPane.showMessageDialog(this, "Không thể tải lịch post: " + ex.getMessage());
            scheduleTableModel.applyDefaultSchedule();
            setChapterPerDayField(scheduleTableModel.getRowCount());
        }
    }

    private int parseChapterPerDay() {
        String text = chapterPerDay.getText();
        if (text == null || text.isBlank()) {
            return 1;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return value > 0 ? value : 1;
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private void setChapterPerDayField(int count) {
        updatingChapterField = true;
        chapterPerDay.setText(String.valueOf(Math.max(count, 1)));
        updatingChapterField = false;
    }

    private void handleChapterPerDayChanged() {
        if (updatingChapterField) {
            return;
        }
        int count = parseChapterPerDay();
        scheduleTableModel.setRowCount(count);
    }

    private void onSave() {
        if (account == null || account.getId() == null) {
            JOptionPane.showMessageDialog(this, "Thiếu thông tin tài khoản để lưu lịch post.");
            return;
        }
        if (Tbooks.isEditing()) {
            Tbooks.getCellEditor().stopCellEditing();
        }
        List<Post> posts = null;

        try {
            if (postDao != null) {
                postDao.replaceAccountPosts(account.getId(), posts);
            }
            JOptionPane.showMessageDialog(this, "Đã lưu lịch post thành công.");
            dispose();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể lưu lịch post", ex);
            JOptionPane.showMessageDialog(this, "Không thể lưu lịch post: " + ex.getMessage());
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
        pnProfile1 = new javax.swing.JPanel();
        btnCancel = new javax.swing.JButton();
        btnCreate = new javax.swing.JButton();
        chapterPerDay = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        txtYeuCau3 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        Tbooks = new javax.swing.JTable();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        pnProfile.setBackground(new java.awt.Color(255, 255, 255));

        jLabel11.setFont(new java.awt.Font("UTM Americana EB", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 40, 85));
        jLabel11.setText("Lịch Đăng TRUYỆN của Tài Khoản");

        pnProfile1.setBackground(new java.awt.Color(255, 255, 255));

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

        btnCreate.setBackground(new java.awt.Color(0, 153, 0));
        btnCreate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCreate.setForeground(new java.awt.Color(255, 255, 255));
        btnCreate.setText("Lưu");
        btnCreate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCreateMouseClicked(evt);
            }
        });
        btnCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateActionPerformed(evt);
            }
        });

        chapterPerDay.setText("10");
        chapterPerDay.setToolTipText("");
        chapterPerDay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chapterPerDayActionPerformed(evt);
            }
        });

        jLabel7.setForeground(new java.awt.Color(51, 51, 51));
        jLabel7.setText("Số chương postmỗi ngày:");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(51, 51, 51));
        jLabel19.setText("Tài khoản:");

        txtYeuCau3.setEditable(false);
        txtYeuCau3.setForeground(new java.awt.Color(153, 153, 153));
        txtYeuCau3.setEnabled(false);
        txtYeuCau3.setFocusable(false);
        txtYeuCau3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtYeuCau3ActionPerformed(evt);
            }
        });

        Tbooks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "STT","Giờ", "Phút"
            }
        ));
        jScrollPane1.setViewportView(Tbooks);

        javax.swing.GroupLayout pnProfile1Layout = new javax.swing.GroupLayout(pnProfile1);
        pnProfile1.setLayout(pnProfile1Layout);
        pnProfile1Layout.setHorizontalGroup(
            pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnProfile1Layout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel19)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(pnProfile1Layout.createSequentialGroup()
                            .addComponent(btnCreate, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(144, 144, 144)
                            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtYeuCau3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(chapterPerDay, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 65, Short.MAX_VALUE))
        );
        pnProfile1Layout.setVerticalGroup(
            pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnProfile1Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(txtYeuCau3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(chapterPerDay, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreate, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(95, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnProfileLayout = new javax.swing.GroupLayout(pnProfile);
        pnProfile.setLayout(pnProfileLayout);
        pnProfileLayout.setHorizontalGroup(
            pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnProfileLayout.createSequentialGroup()
                .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnProfileLayout.createSequentialGroup()
                        .addGap(209, 209, 209)
                        .addComponent(jLabel11))
                    .addGroup(pnProfileLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnProfile1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnProfileLayout.setVerticalGroup(
            pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnProfileLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnProfile1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        getContentPane().add(pnProfile, new java.awt.GridBagConstraints());

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
         dispose();
    }//GEN-LAST:event_closeDialog

    private void btnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
        onSave();
    }//GEN-LAST:event_btnCreateActionPerformed

    private void btnCreateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCreateMouseClicked
        // TODO add your handling code here:
        onSave();
    }//GEN-LAST:event_btnCreateMouseClicked

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_btnCancelMouseClicked

    private void btnRefresh1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefresh1ActionPerformed

    }//GEN-LAST:event_btnRefresh1ActionPerformed

    private void chapterPerDayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chapterPerDayActionPerformed
        // TODO add your handling code here:
         handleChapterPerDayChanged();
    }//GEN-LAST:event_chapterPerDayActionPerformed

    private void txtYeuCau3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtYeuCau3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtYeuCau3ActionPerformed
 private static class ScheduleRow {
        private int order;
        private Integer hour;
        private Integer minute;

        private ScheduleRow(int order) {
            this.order = order;
        }

        private int getOrder() {
            return order;
        }

        private void setOrder(int order) {
            this.order = order;
        }

        private Integer getHour() {
            return hour;
        }

        private void setHour(Integer hour) {
            this.hour = hour;
        }

        private Integer getMinute() {
            return minute;
        }

        private void setMinute(Integer minute) {
            this.minute = minute;
        }
    }

    private class ScheduleTableModel extends AbstractTableModel {
        private final String[] columns = {"STT", "Giờ", "Phút"};
        private final List<ScheduleRow> rows = new ArrayList<>();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Integer.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ScheduleRow row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.getOrder();
                case 1 -> row.getHour();
                case 2 -> row.getMinute();
                default -> null;
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0 || rowIndex < 0 || rowIndex >= rows.size()) {
                return;
            }
            ScheduleRow row = rows.get(rowIndex);
            Integer parsed = parseInteger(aValue);
            if (columnIndex == 1) {
                row.setHour(parsed);
            } else if (columnIndex == 2) {
                row.setMinute(parsed);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        private Integer parseInteger(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            String text = value.toString().trim();
            if (text.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        void setRowCount(int count) {
            int sanitized = Math.max(1, count);
            if (rows.size() > sanitized) {
                rows.subList(sanitized, rows.size()).clear();
            } else {
                for (int i = rows.size(); i < sanitized; i++) {
                    rows.add(new ScheduleRow(i + 1));
                }
            }
            for (int i = 0; i < rows.size(); i++) {
                rows.get(i).setOrder(i + 1);
            }
            fireTableDataChanged();
        }

        void setPosts(List<Post> posts) {
            rows.clear();
            if (posts != null) {
                int index = 1;
                for (Post post : posts) {
                    ScheduleRow row = new ScheduleRow(index++);
                    row.setHour(post.getHour());
                    row.setMinute(post.getMinute());
                    rows.add(row);
                }
            }
            fireTableDataChanged();
        }

        void applyDefaultSchedule() {
            rows.clear();
            int order = 1;
            for (int[] defaultTime : DEFAULT_SCHEDULE) {
                ScheduleRow row = new ScheduleRow(order++);
                row.setHour(defaultTime[0]);
                row.setMinute(defaultTime[1]);
                rows.add(row);
            }
            fireTableDataChanged();
        }        

        List<Post> buildPosts(long accountId) {
            List<Post> posts = new ArrayList<>();
            int previousHour = -1;
            int previousMinute = -1;
            for (ScheduleRow row : rows) {
                Integer hour = row.getHour();
                Integer minute = row.getMinute();
                if (hour == null || minute == null) {
                    throw new IllegalArgumentException("Vui lòng nhập đủ giờ/phút cho chương thứ " + row.getOrder() + ".");
                }
                if (hour < 0 || hour > 24) {
                    throw new IllegalArgumentException("Giờ của chương thứ " + row.getOrder() + " phải nằm trong khoảng 0-24.");
                }
                if (minute < 0 || minute > 59) {
                    throw new IllegalArgumentException("Phút của chương thứ " + row.getOrder() + " phải nằm trong khoảng 0-59.");
                }
                if (hour == 24 && minute != 0) {
                    throw new IllegalArgumentException("Nếu giờ = 24 thì phút phải bằng 0 (chương thứ " + row.getOrder() + ").");
                }
                if (previousHour > hour || (previousHour == hour && previousMinute >= minute)) {
                    throw new IllegalArgumentException("Giờ/phút của chương thứ " + row.getOrder() + " phải lớn hơn chương trước đó.");
                }
                Post post = new Post();
                post.setAccountId(accountId);
                post.setChapterOrder(row.getOrder());
                post.setHour(hour);
                post.setMinute(minute);
                posts.add(post);
                previousHour = hour;
                previousMinute = minute;
            }
            return posts;
        }
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable callback;

        private SimpleDocumentListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            callback.run();
        }
    }

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
            LOGGER.log(Level.SEVERE, null, ex);
        }
        //</editor-fold>


    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Tbooks;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCreate;
    private javax.swing.JTextField chapterPerDay;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pnProfile;
    private javax.swing.JPanel pnProfile1;
    private javax.swing.JTextField txtYeuCau3;
    // End of variables declaration//GEN-END:variables

}
