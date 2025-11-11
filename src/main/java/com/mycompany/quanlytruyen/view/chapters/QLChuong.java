package com.mycompany.quanlytruyen.view.chapters;

import com.mycompany.quanlytruyen.utils.UIUtils;
import com.mycompany.quanlytruyen.config.AppConfig;
import com.mycompany.quanlytruyen.dao.BookDao;
import com.mycompany.quanlytruyen.dao.ChapterDao;
import com.mycompany.quanlytruyen.dao.DataSourceFactory;
import com.mycompany.quanlytruyen.model.BetaStatus;
import com.mycompany.quanlytruyen.model.Book;
import com.mycompany.quanlytruyen.model.Chapter;

import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.AbstractCellEditor;
import java.awt.Component;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.mycompany.quanlytruyen.view.books.SuaSach;


public class QLChuong extends javax.swing.JPanel {

    private ChapterDao chapterDao;
    private BookDao bookDao;
    private DefaultTableModel tableModel;
    private final List<Chapter> allChapters = new ArrayList<>();
    private final Map<Long, String> bookTitleMap = new HashMap<>();

    /**
     * Creates new form QLChuong
     */
    public QLChuong() {
        initComponents();
        UIUtils.enableTextComponentShortcuts(this);
        initialize();
    }

    private void initialize() {
        try {
            AppConfig config = AppConfig.getInstance();
            DataSource ds = DataSourceFactory.create(
                config.getDatabaseUrl(),
                config.getDatabaseUser(),
                config.getDatabasePassword()
            );
            chapterDao = new ChapterDao(ds);
            bookDao = new BookDao(ds);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối cơ sở dữ liệu: " + e.getMessage());
        }

        tableModel = new DefaultTableModel(new Object[]{
            "ID", "ID Truyên", "Tên Truyện", "STT Chương", "Tên Chương", "Nội dung", "Tình trạng Beta", "Copy1", "Copy2", "Chia Chương"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 7;
            }
        };
        Tbooks.setModel(tableModel);
        Tbooks.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer("Copy"));
        Tbooks.getColumnModel().getColumn(7).setCellEditor(new CopyButtonEditor("Copy"));
        Tbooks.getColumnModel().getColumn(8).setCellRenderer(new ButtonRenderer("Copy"));
        Tbooks.getColumnModel().getColumn(8).setCellEditor(new CopyButtonEditor("Copy"));
        Tbooks.getColumnModel().getColumn(9).setCellRenderer(new ButtonRenderer("Chia"));
        Tbooks.getColumnModel().getColumn(9).setCellEditor(new SplitButtonEditor());
        
        tableModel = new ChapterTableModel(new Object[]{
            "ID", "ID Truyên", "Tên Truyện", "STT Chương", "Tên Chương", "Nội dung", "Tình trạng Beta", "Copy1", "Copy2", "Chia Chương"
        }, 0);
        Tbooks.setModel(tableModel);
        Tbooks.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer("Copy"));
        Tbooks.getColumnModel().getColumn(7).setCellEditor(new CopyButtonEditor("Copy"));
        Tbooks.getColumnModel().getColumn(8).setCellRenderer(new ButtonRenderer("Copy"));
        Tbooks.getColumnModel().getColumn(8).setCellEditor(new CopyButtonEditor("Copy"));
        Tbooks.getColumnModel().getColumn(9).setCellRenderer(new ButtonRenderer("Chia"));
        Tbooks.getColumnModel().getColumn(9).setCellEditor(new SplitButtonEditor());
        Tbooks.getColumnModel().getColumn(6).setCellRenderer(new BetaStatusRenderer());
        Tbooks.getColumnModel().getColumn(6).setCellEditor(new BetaStatusEditor());
        Tbooks.setAutoCreateRowSorter(true);

        Tbooks.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int viewRow = Tbooks.rowAtPoint(e.getPoint());
                    if (viewRow < 0) {
                        return;
                    }
                    int modelRow = Tbooks.convertRowIndexToModel(viewRow);
                    Object bookIdValue = tableModel.getValueAt(modelRow, 1);
                    if (bookIdValue instanceof Number number) {
                        openBookEditor(number.longValue());
                    }
                }
            }
        });
        
        initCombos();
        loadAllChapters();

        tableModel.addColumn("Chọn");
        Tbooks.setModel(tableModel);
        Tbooks.getColumnModel().moveColumn(tableModel.getColumnCount() - 1, 0);
        // Re-apply button renderers/editors after resetting the model
        Tbooks.getColumnModel().getColumn(8).setCellRenderer(new ButtonRenderer("Copy"));
        Tbooks.getColumnModel().getColumn(8).setCellEditor(new CopyButtonEditor("Copy"));
        Tbooks.getColumnModel().getColumn(9).setCellRenderer(new ButtonRenderer("Copy"));
        Tbooks.getColumnModel().getColumn(9).setCellEditor(new CopyButtonEditor("Copy"));
        Tbooks.getColumnModel().getColumn(10).setCellRenderer(new ButtonRenderer("Chia"));
        Tbooks.getColumnModel().getColumn(10).setCellEditor(new SplitButtonEditor());
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(Boolean.FALSE, i, tableModel.getColumnCount() - 1);
        }        
    }

    private void initCombos() {
        tenTruyen.removeAllItems();
        tenTruyen.addItem("Tất cả");
        if (bookDao != null) {
            try {
                List<Book> books = bookDao.getAllBooks();
                for (Book b : books) {
                    tenTruyen.addItem(b.getId() + " - " + b.getTitle());
                    bookTitleMap.put(b.getId(), b.getTitle());
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi tải truyện: " + e.getMessage());
            }
        }

        stTranslate.removeAllItems();
        stTranslate.addItem("Tất cả");
        for (BetaStatus bs : BetaStatus.values()) {
            stTranslate.addItem(bs.getDisplayName());
        }
    }

    private void loadAllChapters() {
        if (chapterDao == null || bookDao == null) {
            return;
        }
        try {
            allChapters.clear();
            List<Book> books = bookDao.getAllBooks();
            for (Book b : books) {
                bookTitleMap.put(b.getId(), b.getTitle());
                allChapters.addAll(chapterDao.findByBookId(b.getId()));
            }
            refreshTable(allChapters);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage());
        }
    }

    private void refreshTable(List<Chapter> chapters) {
        tableModel.setRowCount(0);
        if (chapters == null) return;
        for (Chapter c : chapters) {
            String title = bookTitleMap.get(c.getBookId());
            String[] parts = splitContent(c.getContent());
            tableModel.addRow(new Object[]{
                c.getId(),
                c.getBookId(),
                title,
                c.getChapterNumber(),
                c.getChapterTitle(),
                c.getContent(),
                c.getBetaStatus() != null ? c.getBetaStatus().getDisplayName() : "",
                parts[0],
                parts[1],
                "Chia"
            });
            tableModel.setValueAt(Boolean.FALSE, tableModel.getRowCount() - 1, tableModel.getColumnCount() - 1);
        }
    }

    private String[] splitContent(String content) {
        if (content == null) return new String[]{"", ""};
        int mid = content.length() / 2;
        int idx = content.lastIndexOf('\n', mid);
        if (idx == -1) idx = mid;
        String part1 = content.substring(0, idx);
        String part2 = content.substring(idx);
        part2 = part2.replaceFirst("^(\\s*\\r?\\n)+", "");
        return new String[]{part1, part2};
    }
    private void openBookEditor(long bookId) {
        if (bookDao == null) {
            return;
        }
        try {
            Book book = bookDao.getBookById(bookId);
            if (book == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy truyện để sửa");
                return;
            }
            Window window = SwingUtilities.getWindowAncestor(this);
            java.awt.Frame frame = window instanceof java.awt.Frame ? (java.awt.Frame) window : null;
            SuaSach dialog = new SuaSach(frame, true, null, book);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            loadAllChapters();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể mở hộp thoại sửa sách: " + ex.getMessage());
        }
    }
    private Long getSelectedBookId() {
        int idx = tenTruyen.getSelectedIndex();
        if (idx <= 0) return null;
        Object item = tenTruyen.getSelectedItem();
        if (item instanceof String) {
            String str = (String) item;
            int dash = str.indexOf(" - ");
            if (dash > 0) {
                try {
                    return Long.parseLong(str.substring(0, dash));
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
        }
        return null;
    }

    private BetaStatus getSelectedBetaStatus() {
        int idx = stTranslate.getSelectedIndex();
        if (idx <= 0) return null;
        String name = (String) stTranslate.getSelectedItem();
        for (BetaStatus bs : BetaStatus.values()) {
            if (bs.getDisplayName().equals(name)) {
                return bs;
            }
        }
        return null;
    }
    
    private static class ChapterTableModel extends DefaultTableModel {
        public ChapterTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 6 || column >= 7;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == getColumnCount() - 1) {
                return Boolean.class;
            }
            return super.getColumnClass(columnIndex);
        }        
    }

    private static class BetaStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String v = value != null ? value.toString() : "";
            if ("Chưa beta".equals(v)) {
                c.setForeground(Color.RED);
                c.setBackground(new Color(255, 204, 204));
            } else if ("Đã beta".equals(v)) {
                c.setForeground(new Color(0,128,0));
                c.setBackground(new Color(204, 255, 204));
            } else if ("Đã đăng".equals(v)) {
                c.setForeground(Color.ORANGE);
                c.setBackground(new Color(255, 235, 205));
            } else {
                c.setForeground(Color.BLACK);
                c.setBackground(table.getBackground());
            }
            //c.setOpaque(true);
            return c;
        }
    }

    private class BetaStatusEditor extends AbstractCellEditor implements TableCellEditor {
        private final JComboBox<String> comboBox = new JComboBox<>(BetaStatus.getDisplayNames());
        public BetaStatusEditor() {
            comboBox.setRenderer(new BetaStatusComboRenderer());
        }        
        private int row;

        @Override
        public Object getCellEditorValue() {
            return comboBox.getSelectedItem();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            comboBox.setSelectedItem(value);
            return comboBox;
        }

        @Override
        public boolean stopCellEditing() {
            String selected = (String) comboBox.getSelectedItem();
            try {
                Long bookId = Long.parseLong(Tbooks.getValueAt(row, 1).toString());
                int chapterNumber = Integer.parseInt(Tbooks.getValueAt(row, 3).toString());
                BetaStatus status = BetaStatus.fromDisplayName(selected);
                chapterDao.updateBetaStatus(bookId, chapterNumber, status);
                for (Chapter c : allChapters) {
                    if (c.getBookId() != null && c.getBookId().equals(bookId) && c.getChapterNumber() != null && c.getChapterNumber().equals(chapterNumber)) {
                        c.setBetaStatus(status);
                        break;
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(QLChuong.this, "Lỗi cập nhật tình trạng: " + ex.getMessage());
            }
            return super.stopCellEditing();
        }
        private class BetaStatusComboRenderer extends DefaultListCellRenderer {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String v = value != null ? value.toString() : "";
                if ("Chưa beta".equals(v)) {
                    c.setForeground(Color.RED);
                } else if ("Đã beta".equals(v)) {
                    c.setForeground(new Color(0,128,0));
                } else if ("Đã đăng".equals(v)) {
                    c.setForeground(Color.ORANGE);
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
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
        btnDelete = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        FilterPanel1 = new javax.swing.JPanel();
        tenTruyen = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        stTranslate = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        btnFilter2 = new javax.swing.JButton();
        btnFilter3 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Tbooks = new javax.swing.JTable();
        jCheckBox1 = new javax.swing.JCheckBox();
        export = new javax.swing.JButton();
        btnChiaChuong = new javax.swing.JButton();

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
        jLabel1.setText("Quản Lý Từng Chương");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addComponent(jLabel1)
                .addGap(183, 183, 183)
                .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(29, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(17, 17, 17))
        );

        FilterPanel1.setBackground(new java.awt.Color(249, 245, 245));
        FilterPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tenTruyen.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        tenTruyen.setPreferredSize(new java.awt.Dimension(100, 22));
        tenTruyen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tenTruyenActionPerformed(evt);
            }
        });

        jLabel6.setForeground(new java.awt.Color(153, 153, 153));
        jLabel6.setText("Truyện");

        stTranslate.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        stTranslate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stTranslateActionPerformed(evt);
            }
        });

        jLabel7.setForeground(new java.awt.Color(153, 153, 153));
        jLabel7.setText("Tình trạng Dịch");

        btnFilter2.setBackground(new java.awt.Color(255, 130, 0));
        btnFilter2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnFilter2.setForeground(new java.awt.Color(255, 255, 255));
        btnFilter2.setText("Lọc");
        btnFilter2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilter2ActionPerformed(evt);
            }
        });

        btnFilter3.setBackground(new java.awt.Color(153, 153, 153));
        btnFilter3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnFilter3.setForeground(new java.awt.Color(255, 255, 255));
        btnFilter3.setText("Xóa Lọc");
        btnFilter3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilter3ActionPerformed(evt);
            }
        });

        jLabel8.setForeground(new java.awt.Color(153, 153, 153));
        jLabel8.setText("từ chương:");

        jLabel9.setForeground(new java.awt.Color(153, 153, 153));
        jLabel9.setText("đến chương:");

        jTextField1.setText("100");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jTextField2.setText("01");
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout FilterPanel1Layout = new javax.swing.GroupLayout(FilterPanel1);
        FilterPanel1.setLayout(FilterPanel1Layout);
        FilterPanel1Layout.setHorizontalGroup(
            FilterPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FilterPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(FilterPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FilterPanel1Layout.createSequentialGroup()
                        .addGroup(FilterPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(FilterPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel7))
                            .addGroup(FilterPanel1Layout.createSequentialGroup()
                                .addComponent(tenTruyen, 0, 140, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)))
                        .addGap(30, 30, 30))
                    .addGroup(FilterPanel1Layout.createSequentialGroup()
                        .addGroup(FilterPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(stTranslate, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(FilterPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(FilterPanel1Layout.createSequentialGroup()
                                    .addComponent(btnFilter2)
                                    .addGap(18, 18, 18)
                                    .addComponent(btnFilter3))
                                .addGroup(FilterPanel1Layout.createSequentialGroup()
                                    .addGroup(FilterPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel8)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(FilterPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel9)))))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        FilterPanel1Layout.setVerticalGroup(
            FilterPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FilterPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(FilterPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tenTruyen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(27, 27, 27)
                .addGroup(FilterPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stTranslate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(27, 27, 27)
                .addGroup(FilterPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(FilterPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(FilterPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFilter2)
                    .addComponent(btnFilter3))
                .addContainerGap(116, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(FilterPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(FilterPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(263, Short.MAX_VALUE))
        );

        Tbooks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Chọn","ID", "ID Truyên", "Tên Truyện", "STT Chương", "Tên Chương", "Nội dung", "Tình trạng Beta", "Copy1", "Copy2", "Chia Chương"
            }
        ));
        jScrollPane1.setViewportView(Tbooks);

        jCheckBox1.setText("Chọn tất cả");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        export.setBackground(new java.awt.Color(51, 51, 255));
        export.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        export.setForeground(new java.awt.Color(255, 255, 255));
        export.setText("Xuất file .docx");
        export.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportActionPerformed(evt);
            }
        });

        btnChiaChuong.setBackground(new java.awt.Color(0, 204, 204));
        btnChiaChuong.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnChiaChuong.setForeground(new java.awt.Color(255, 255, 255));
        btnChiaChuong.setText("Chia Chương");
        btnChiaChuong.setToolTipText("");
        btnChiaChuong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChiaChuongActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 757, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox1)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(export)
                                .addGap(18, 18, 18)
                                .addComponent(btnChiaChuong)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 557, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(export)
                    .addComponent(btnChiaChuong))
                .addGap(8, 8, 8))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(796, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(117, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int row = Tbooks.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chương cần xóa");
            return;
        }
        long id = ((Number) tableModel.getValueAt(row, 0)).longValue();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa chương này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            chapterDao.deleteChapter(id);
            loadAllChapters();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + e.getMessage());
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        int row = Tbooks.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chương cần sửa");
            return;
        }
        long id = ((Number) tableModel.getValueAt(row, 0)).longValue();
        Chapter selected = null;
        for (Chapter c : allChapters) {
            if (c.getId() != null && c.getId() == id) {
                selected = c;
                break;
            }
        }
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy chương đã chọn");
            return;
        }
        java.awt.Frame frame = (java.awt.Frame) SwingUtilities.getWindowAncestor(this);
        SuaChuong dialog = new SuaChuong(frame, true, this, selected);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        loadAllChapters();        

    }//GEN-LAST:event_btnEditActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
        txtSearch.setText("");
        loadAllChapters();
    }//GEN-LAST:event_btnResetActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            refreshTable(allChapters);
            return;
        }
        List<Chapter> result = new ArrayList<>();
        for (Chapter c : allChapters) {
            String bookTitle = bookTitleMap.get(c.getBookId());
            if ((c.getChapterTitle() != null && c.getChapterTitle().toLowerCase().contains(keyword)) ||
                (bookTitle != null && bookTitle.toLowerCase().contains(keyword)) ||
                (c.getContent() != null && c.getContent().toLowerCase().contains(keyword))) {
                result.add(c);
            }
        }
        refreshTable(result);
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_btnSearchActionPerformed

    private void tenTruyenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tenTruyenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tenTruyenActionPerformed

    private void stTranslateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stTranslateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stTranslateActionPerformed

    private void btnFilter2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter2ActionPerformed
        // TODO add your handling code here:
        Long bookId = getSelectedBookId();
        BetaStatus beta = getSelectedBetaStatus();

        Integer fromChapter = parseChapterNumber(jTextField2.getText(), "từ chương");
        if (fromChapter == null && !jTextField2.getText().trim().isEmpty()) {
            return;
        }
        Integer toChapter = parseChapterNumber(jTextField1.getText(), "đến chương");
        if (toChapter == null && !jTextField1.getText().trim().isEmpty()) {
            return;
        }

        if (fromChapter != null && toChapter != null && fromChapter > toChapter) {
            JOptionPane.showMessageDialog(this, "Giá trị 'từ chương' phải nhỏ hơn hoặc bằng 'đến chương'.");
            return;
        }

        try {
            List<Chapter> chapters = new ArrayList<>();
            if (bookId != null && beta != null) {
                chapters = chapterDao.findByBookIdAndBetaStatus(bookId, beta);
            } else if (bookId != null) {
                chapters = chapterDao.findByBookId(bookId);
            } else if (beta != null) {
                List<Book> books = bookDao.getAllBooks();
                for (Book b : books) {
                    chapters.addAll(chapterDao.findByBookIdAndBetaStatus(b.getId(), beta));
                    bookTitleMap.put(b.getId(), b.getTitle());
                }
            } else {
                chapters.addAll(allChapters);
            }

            List<Chapter> filteredChapters = new ArrayList<>();
            for (Chapter c : chapters) {
                Integer chapterNumber = c.getChapterNumber();
                if (chapterNumber == null) {
                    if (fromChapter == null && toChapter == null) {
                        filteredChapters.add(c);
                    }
                    continue;
                }
                if (fromChapter != null && chapterNumber < fromChapter) {
                    continue;
                }
                if (toChapter != null && chapterNumber > toChapter) {
                    continue;
                }
                filteredChapters.add(c);
            }
            refreshTable(filteredChapters);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lọc: " + e.getMessage());
        }        

    }//GEN-LAST:event_btnFilter2ActionPerformed

    private void btnFilter3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter3ActionPerformed
        // TODO add your handling code here:
        tenTruyen.setSelectedIndex(0);
        stTranslate.setSelectedIndex(0);
        loadAllChapters();
    }//GEN-LAST:event_btnFilter3ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
        boolean select = jCheckBox1.isSelected();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(select, i, tableModel.getColumnCount() - 1);
        }        
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void exportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File dir = chooser.getSelectedFile();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Object val = tableModel.getValueAt(i, tableModel.getColumnCount() - 1);
                if (Boolean.TRUE.equals(val)) {
                    Object numObj = tableModel.getValueAt(i, 3);
                    String number = numObj != null ? numObj.toString() : String.valueOf(i + 1);
                    Object contentObj = tableModel.getValueAt(i, 5);
                    String content = contentObj != null ? contentObj.toString() : "";
                    File f = new File(dir, "C" + number + ".docx");
                    try (FileWriter fw = new FileWriter(f)) {
                        fw.write(content);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage());
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "Xuất file thành công!");
        }        
    }//GEN-LAST:event_exportActionPerformed

    private void btnChiaChuongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChiaChuongActionPerformed
        // TODO add your handling code here:
            java.awt.Frame frame = (java.awt.Frame) SwingUtilities.getWindowAncestor(QLChuong.this);
            ChiaChuong dialog = new ChiaChuong(frame, true, QLChuong.this, "");
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
    }//GEN-LAST:event_btnChiaChuongActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

     private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String label) {
            setText(label);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class CopyButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private final JButton button;
        private String text;

        public CopyButtonEditor(String label) {
            button = new JButton(label);
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            text = value != null ? value.toString() : "";
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return text;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            StringSelection selection = new StringSelection(text);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            fireEditingStopped();
            JOptionPane.showMessageDialog(button, "Đã copy vào clipboard");
        }
    }

    private class SplitButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private final JButton button = new JButton("Chia");
        private int row;

        public SplitButtonEditor() {
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped();
            String content = (String) tableModel.getValueAt(row, 5);
            java.awt.Frame frame = (java.awt.Frame) SwingUtilities.getWindowAncestor(QLChuong.this);
            ChiaChuong dialog = new ChiaChuong(frame, true, QLChuong.this, content);
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
        }
    }

    public ChapterDao getChapterDao() {
        return chapterDao;
    }

    public BookDao getBookDao() {
        return bookDao;
    }

    public void reloadChapters() {
        loadAllChapters();
    }
    private Integer parseChapterNumber(String value, String fieldLabel) {
        String trimmed = value != null ? value.trim() : "";
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá trị " + fieldLabel + " phải là số.");
            return null;
        }
    }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel FilterPanel1;
    private javax.swing.JTable Tbooks;
    private javax.swing.JButton btnChiaChuong;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnFilter2;
    private javax.swing.JButton btnFilter3;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton export;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JComboBox<String> stTranslate;
    private javax.swing.JComboBox<String> tenTruyen;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
