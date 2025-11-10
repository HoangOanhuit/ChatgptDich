package com.mycompany.quanlytruyen.view.books;

import com.mycompany.quanlytruyen.config.AppConfig;
import com.mycompany.quanlytruyen.dao.BookDao;
import com.mycompany.quanlytruyen.dao.DataSourceFactory;
import com.mycompany.quanlytruyen.model.Book;
import com.mycompany.quanlytruyen.view.books.SuaSach;
import com.mycompany.quanlytruyen.view.books.ThemSach;
import com.mycompany.quanlytruyen.utils.UIUtils;

import javax.sql.DataSource;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.Frame;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


public class QLSach extends javax.swing.JPanel {

    private BookDao bookDao;
    private DefaultTableModel tableModel;
    private Book selectedBook;

    /**
     * Creates new form QLSach
     */
    public QLSach() {
        initComponents();
        initialize();
        UIUtils.enableTextComponentShortcuts(this);
        setupResponsiveLayout();
    }

    private void initialize() {
        try {
            AppConfig config = AppConfig.getInstance();
            DataSource ds = DataSourceFactory.create(
                config.getDatabaseUrl(),
                config.getDatabaseUser(),
                config.getDatabasePassword()
            );
            bookDao = new BookDao(ds);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối cơ sở dữ liệu: " + e.getMessage());
        }

        tableModel = new DefaultTableModel(new Object[]{
            "ID", "Tên Truyện", "Tác giả", "Yêu Cầu", "Bảng tên",
            "Raw", "Tình trạng Dịch", "Tình trạng Đăng"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        Tbooks.setModel(tableModel);
        Tbooks.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
        Tbooks.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        Tbooks.getColumnModel().getColumn(7).setCellRenderer(new StatusCellRenderer());
        Tbooks.setAutoCreateRowSorter(true);      

        initStatusCombos();
        loadAllBooks();
    }

    private void initStatusCombos() {
        stRaw.removeAllItems();
        stRaw.addItem("Tất cả");
        for (Book.RawStatus rs : Book.RawStatus.values()) {
            stRaw.addItem(rs.getDisplayName());
        }

        stRaw1.removeAllItems();
        stRaw1.addItem("Tất cả");
        for (Book.TranslateStatus ts : Book.TranslateStatus.values()) {
            stRaw1.addItem(ts.getDisplayName());
        }

        stRaw2.removeAllItems();
        stRaw2.addItem("Tất cả");
        for (Book.PostStatus ps : Book.PostStatus.values()) {
            stRaw2.addItem(ps.getDisplayName());
        }
    }

    private void loadAllBooks() {
        if (bookDao == null) {
            return;
        }
        try {
            List<Book> books = bookDao.getAllBooks();
            refreshTable(books);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage());
        }
    }

    private void refreshTable(List<Book> books) {
        tableModel.setRowCount(0);
        if (books == null) return;
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                b.getId(),
                b.getTitle(),
                b.getAuthor(),
                b.getGuidelines(),
                b.getNameTable(),
                b.getRawStatus().getDisplayName(),
                b.getTranslateStatus().getDisplayName(),
                b.getPostStatus().getDisplayName()
            });
        }
    }

    private Book.RawStatus getSelectedRawStatus() {
        Object value = stRaw.getSelectedItem();
        if (value instanceof String) {
            String str = (String) value;
            if ("Tất cả".equals(str)) return null;
            for (Book.RawStatus rs : Book.RawStatus.values()) {
                if (rs.getDisplayName().equals(str)) {
                    return rs;
                }
            }
        }
        return null;
    }

    private Book.TranslateStatus getSelectedTranslateStatus() {
        Object value = stRaw1.getSelectedItem();
        if (value instanceof String) {
            String str = (String) value;
            if ("Tất cả".equals(str)) return null;
            for (Book.TranslateStatus ts : Book.TranslateStatus.values()) {
                if (ts.getDisplayName().equals(str)) {
                    return ts;
                }
            }
        }
        return null;
    }

    private Book.PostStatus getSelectedPostStatus() {
        Object value = stRaw2.getSelectedItem();
        if (value instanceof String) {
            String str = (String) value;
            if ("Tất cả".equals(str)) return null;
            for (Book.PostStatus ps : Book.PostStatus.values()) {
                if (ps.getDisplayName().equals(str)) {
                    return ps;
                }
            }
        }
        return null;
    }

    public Book getSelectedBook() {
        return selectedBook;
    }

        private void setupResponsiveLayout() {
        removeAll();
        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, FilterPanel, jScrollPane1);
        splitPane.setDividerLocation(FilterPanel.getPreferredSize().width);
        splitPane.setResizeWeight(0.0);
        splitPane.setOneTouchExpandable(true);
        add(jPanel2, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                comp.setForeground(Color.BLACK);
                if (column == 5) {
                    if (Book.RawStatus.FULL.getDisplayName().equals(value)) {
                        comp.setForeground(new Color(0, 128, 0));
                    } else if (Book.RawStatus.NOT_FULL.getDisplayName().equals(value)) {
                        comp.setForeground(Color.RED);
                    }
                } else if (column == 6) {
                    if (Book.TranslateStatus.HOAN.getDisplayName().equals(value)) {
                        comp.setForeground(new Color(0, 128, 0));
                    } else if (Book.TranslateStatus.NOT_HOAN.getDisplayName().equals(value)) {
                        comp.setForeground(Color.RED);
                    }
                } else if (column == 7) {
                    if (Book.PostStatus.HOAN.getDisplayName().equals(value)) {
                        comp.setForeground(new Color(0, 128, 0));
                    } else if (Book.PostStatus.NOT_HOAN.getDisplayName().equals(value)) {
                        comp.setForeground(Color.RED);
                    }
                }
            }
            return comp;
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

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnDelete = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Tbooks = new javax.swing.JTable();
        FilterPanel = new javax.swing.JPanel();
        stRaw = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        stRaw1 = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        stRaw2 = new javax.swing.JComboBox<>();
        btnFilter = new javax.swing.JButton();
        btnFilter1 = new javax.swing.JButton();

        jPanel2.setBackground(new java.awt.Color(232, 223, 202));
        jPanel2.setForeground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Lava Devanagari", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 255));
        jLabel1.setText("Quản Lý Truyện");

        btnDelete.setBackground(new java.awt.Color(232, 223, 202));
        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/delete.png"))); // NOI18N
        btnDelete.setBorder(null);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnAdd.setBackground(new java.awt.Color(232, 223, 202));
        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/add.png"))); // NOI18N
        btnAdd.setBorder(null);
        btnAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnEdit.setBackground(new java.awt.Color(232, 223, 202));
        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/edit.png"))); // NOI18N
        btnEdit.setBorder(null);
        btnEdit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnReset.setBackground(new java.awt.Color(232, 223, 202));
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

        btnSearch.setBackground(new java.awt.Color(232, 223, 202));
        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/search.png"))); // NOI18N
        btnSearch.setBorder(null);
        btnSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addComponent(jLabel1)
                .addGap(137, 137, 137)
                .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        Tbooks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Tên Truyện", "Tác giả", "Yêu Cầu", "Bảng tên", "Raw", "Tình trạng Dịch", "Tình trạng Đăng"
            }
        ));
        jScrollPane1.setViewportView(Tbooks);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 745, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE)
                .addContainerGap())
        );

        FilterPanel.setBackground(new java.awt.Color(249, 245, 245));
        FilterPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        stRaw.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        stRaw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stRawActionPerformed(evt);
            }
        });

        jLabel3.setForeground(new java.awt.Color(153, 153, 153));
        jLabel3.setText("Tình trạng Raw");

        stRaw1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        stRaw1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stRaw1ActionPerformed(evt);
            }
        });

        jLabel4.setForeground(new java.awt.Color(153, 153, 153));
        jLabel4.setText("Tình trạng Dịch");

        jLabel5.setForeground(new java.awt.Color(153, 153, 153));
        jLabel5.setText("Tình trạng Đăng");

        stRaw2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        stRaw2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stRaw2ActionPerformed(evt);
            }
        });

        btnFilter.setBackground(new java.awt.Color(0, 102, 102));
        btnFilter.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnFilter.setForeground(new java.awt.Color(255, 255, 255));
        btnFilter.setText("Lọc");
        btnFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterActionPerformed(evt);
            }
        });

        btnFilter1.setBackground(new java.awt.Color(153, 153, 153));
        btnFilter1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnFilter1.setForeground(new java.awt.Color(255, 255, 255));
        btnFilter1.setText("Xóa Lọc");
        btnFilter1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilter1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout FilterPanelLayout = new javax.swing.GroupLayout(FilterPanel);
        FilterPanel.setLayout(FilterPanelLayout);
        FilterPanelLayout.setHorizontalGroup(
            FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FilterPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FilterPanelLayout.createSequentialGroup()
                        .addComponent(btnFilter)
                        .addGap(18, 18, 18)
                        .addComponent(btnFilter1)
                        .addGap(0, 69, Short.MAX_VALUE))
                    .addGroup(FilterPanelLayout.createSequentialGroup()
                        .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(stRaw, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(stRaw1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(stRaw2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(FilterPanelLayout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 5, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FilterPanelLayout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel4))))
                        .addGap(23, 23, 23))))
        );
        FilterPanelLayout.setVerticalGroup(
            FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FilterPanelLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stRaw, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(27, 27, 27)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stRaw1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(27, 27, 27)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stRaw2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(55, 55, 55)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFilter)
                    .addComponent(btnFilter1))
                .addContainerGap(116, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(FilterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(762, 762, 762))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGap(0, 269, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(FilterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 340, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(95, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void stRawActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stRawActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stRawActionPerformed

    private void stRaw1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stRaw1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stRaw1ActionPerformed

    private void stRaw2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stRaw2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stRaw2ActionPerformed

    private void btnFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterActionPerformed
        // TODO add your handling code here:
        Book.RawStatus raw = getSelectedRawStatus();
        Book.TranslateStatus translate = getSelectedTranslateStatus();
        Book.PostStatus post = getSelectedPostStatus();

        try {
            List<Book> books = bookDao.getBooksByStatus(raw, translate, post);
            refreshTable(books);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lọc: " + e.getMessage());
        }        
    }//GEN-LAST:event_btnFilterActionPerformed

    private void btnFilter1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter1ActionPerformed
        // TODO add your handling code here:
        stRaw.setSelectedIndex(0);
        stRaw1.setSelectedIndex(0);
        stRaw2.setSelectedIndex(0);
        loadAllBooks();        
    }//GEN-LAST:event_btnFilter1ActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        int row = Tbooks.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn truyện cần xóa");
            return;
        }
        long id = ((Number) tableModel.getValueAt(row, 0)).longValue();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa truyện này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            bookDao.deleteBook(id);
            loadAllBooks();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + e.getMessage());
        }        
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        ThemSach dialog = new ThemSach(frame, true, this);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        loadAllBooks();        
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        int row = Tbooks.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn truyện cần sửa");
            return;
        }
        selectedBook = new Book();
        selectedBook.setId(((Number) tableModel.getValueAt(row, 0)).longValue());
        selectedBook.setTitle((String) tableModel.getValueAt(row, 1));
        selectedBook.setAuthor((String) tableModel.getValueAt(row, 2));
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        SuaSach dialog = new SuaSach(frame, true, this);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        loadAllBooks();        
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
        txtSearch.setText("");
        loadAllBooks();        
    }//GEN-LAST:event_btnResetActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
        btnSearchActionPerformed(evt);
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadAllBooks();
            return;
        }
        try {
            List<Book> all = bookDao.getAllBooks();
            List<Book> result = new ArrayList<>();
            for (Book b : all) {
                if ((b.getTitle() != null && b.getTitle().toLowerCase().contains(keyword)) ||
                    (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(keyword))) {
                    result.add(b);
                }
            }
            refreshTable(result);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm: " + e.getMessage());
        }        
    }//GEN-LAST:event_btnSearchActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel FilterPanel;
    private javax.swing.JTable Tbooks;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnFilter;
    private javax.swing.JButton btnFilter1;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<String> stRaw;
    private javax.swing.JComboBox<String> stRaw1;
    private javax.swing.JComboBox<String> stRaw2;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
