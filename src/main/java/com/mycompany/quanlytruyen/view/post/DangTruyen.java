package com.mycompany.quanlytruyen.view.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.quanlytruyen.config.AppConfig;
import com.mycompany.quanlytruyen.dao.AccountDAO;
import com.mycompany.quanlytruyen.dao.BookDao;
import com.mycompany.quanlytruyen.dao.ChapterDao;
import com.mycompany.quanlytruyen.dao.DataSourceFactory;
import com.mycompany.quanlytruyen.dao.PostDao;
import com.mycompany.quanlytruyen.model.Account;
import com.mycompany.quanlytruyen.model.BetaStatus;
import com.mycompany.quanlytruyen.model.Book;
import com.mycompany.quanlytruyen.model.Book.PostStatus;
import com.mycompany.quanlytruyen.model.Chapter;
import com.mycompany.quanlytruyen.model.Post;
import com.mycompany.quanlytruyen.database.DatabaseManager;

import javax.swing.*;
import javax.swing.LayoutStyle;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.sql.DataSource;

public class DangTruyen extends javax.swing.JPanel {

    private static final int DEFAULT_NUM_CHAPTER = 5;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String API_ENDPOINT = "https://s1apihd.com/wp-json/v1/app/user/themchuong";
    
    // DAOs
    private BookDao bookDao;
    private AccountDAO accountDao;
    private ChapterDao chapterDao;
    private PostDao postDao;
    private DataSource dataSource;
    
    // UI Components
    private JTable tBooks;
    private JTextField txtNumChapter; // Số chương mỗi lần đăng
    private JTextField txtDay; // Ngày đăng
    private JTextField txtMonth; // Tháng đăng
    private JTextField txtYear; // Năm đăng
    private JButton btnRefresh; // Nút "Làm mới"
    
    // Data
    private BookTableModel tableModel;
    private List<BookEntry> bookEntries;
    private List<Account> allAccounts;


    public DangTruyen() {
        this.bookEntries = new ArrayList<>();
        this.allAccounts = new ArrayList<>();
        
        // Khởi tạo DataSource và DAOs
        try {
            AppConfig config = AppConfig.getInstance();
            this.dataSource = DataSourceFactory.create(
                config.getDatabaseUrl(),
                config.getDatabaseUser(),
                config.getDatabasePassword()
            );
            this.bookDao = new BookDao(dataSource);
            this.accountDao = new AccountDAO(dataSource);
            this.chapterDao = new ChapterDao(dataSource);
            this.postDao = new PostDao(dataSource);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể khởi tạo kết nối CSDL: " + ex.getMessage());
        }
        
        initComponents();
        configureCustomComponents();
        initData();

    }

 /**
     * Thiết lập các thành phần UI tùy chỉnh sau khi form được tạo
     */
    private void configureCustomComponents() {
        this.txtDay = this.day;
        this.txtMonth = this.month;
        this.txtYear = this.year;

        if (this.tableModel == null) {
            this.tableModel = new BookTableModel();
        }
        Tbooks.setModel(tableModel);
        Tbooks.setFillsViewportHeight(true);
        Tbooks.setRowHeight(28);
        Tbooks.setAutoCreateRowSorter(true);

        TableColumnModel columnModel = Tbooks.getColumnModel();
        if (columnModel.getColumnCount() > 0) {
            TableColumn selectColumn = columnModel.getColumn(0);
            selectColumn.setMaxWidth(70);
            selectColumn.setCellRenderer(new CheckBoxRenderer());
            selectColumn.setCellEditor(new CheckBoxEditor());
        }
        if (columnModel.getColumnCount() > 3) {
            columnModel.getColumn(1).setPreferredWidth(150);
            columnModel.getColumn(2).setPreferredWidth(240);
            columnModel.getColumn(3).setPreferredWidth(110);
        }

        initializeNumChapterControl();
        setDefaultScheduleDate();
    }

    private void initializeNumChapterControl() {
        txtNumChapter = new JTextField(String.valueOf(DEFAULT_NUM_CHAPTER));
        txtNumChapter.setColumns(5);
        txtNumChapter.setHorizontalAlignment(JTextField.CENTER);
        txtNumChapter.setToolTipText("Số chương sẽ đăng cho mỗi truyện");
        txtNumChapter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateToChapters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateToChapters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateToChapters();
            }
        });

        JPanel numChapterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        numChapterPanel.setOpaque(false);
        JLabel lblNumChapter = new JLabel("Số chương/lần đăng:");
        numChapterPanel.add(lblNumChapter);
        numChapterPanel.add(txtNumChapter);

        rebuildSidePanelLayout(numChapterPanel);
    }

    private void rebuildSidePanelLayout(JPanel numChapterPanel) {
        GroupLayout layout = new GroupLayout(sideBox);
        sideBox.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(FilterPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(numChapterPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel11)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(day, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel12)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(month, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel13)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(year, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)))
                            .addGap(0, 20, Short.MAX_VALUE)))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(25, 25, 25)
                    .addComponent(FilterPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(numChapterPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGap(24, 24, 24)
                    .addComponent(jLabel11)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(day, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(month, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(year, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel12)
                        .addComponent(jLabel13))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sideBox.revalidate();
        sideBox.repaint();
    }

    private void setDefaultScheduleDate() {
        if (txtDay == null || txtMonth == null || txtYear == null) {
            return;
        }
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        txtDay.setText(String.format("%02d", tomorrow.getDayOfMonth()));
        txtMonth.setText(String.format("%02d", tomorrow.getMonthValue()));
        txtYear.setText(String.valueOf(tomorrow.getYear()));
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
        btnEdit = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Tbooks = new javax.swing.JTable();
        jCheckBox1 = new javax.swing.JCheckBox();
        sideBox = new javax.swing.JPanel();
        FilterPanel2 = new javax.swing.JPanel();
        tenTruyen1 = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        btnFilter4 = new javax.swing.JButton();
        btnFilter5 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        day = new javax.swing.JTextField();
        month = new javax.swing.JTextField();
        year = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        btnFilter2 = new javax.swing.JButton();

        jPanel2.setBackground(new java.awt.Color(0, 40, 85));
        jPanel2.setForeground(new java.awt.Color(255, 255, 255));

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
        jLabel1.setText("Đăng Truyện Hằng Ngày");

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 226, Short.MAX_VALUE)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
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
                            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(17, 17, 17))
        );

        Tbooks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Chọn","Tài khoản", "Tên Truyện", "Giá/chương", "Từ Chương", "Đến Chương"
            }
        ));
        jScrollPane1.setViewportView(Tbooks);

        jCheckBox1.setText("Chọn tất cả");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        FilterPanel2.setBackground(new java.awt.Color(249, 245, 245));
        FilterPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tenTruyen1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        tenTruyen1.setPreferredSize(new java.awt.Dimension(100, 22));
        tenTruyen1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tenTruyen1ActionPerformed(evt);
            }
        });

        jLabel10.setForeground(new java.awt.Color(153, 153, 153));
        jLabel10.setText("Tài khoản");

        btnFilter4.setBackground(new java.awt.Color(255, 130, 0));
        btnFilter4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnFilter4.setForeground(new java.awt.Color(255, 255, 255));
        btnFilter4.setText("Lọc");
        btnFilter4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilter4ActionPerformed(evt);
            }
        });

        btnFilter5.setBackground(new java.awt.Color(153, 153, 153));
        btnFilter5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnFilter5.setForeground(new java.awt.Color(255, 255, 255));
        btnFilter5.setText("Xóa Lọc");
        btnFilter5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilter5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout FilterPanel2Layout = new javax.swing.GroupLayout(FilterPanel2);
        FilterPanel2.setLayout(FilterPanel2Layout);
        FilterPanel2Layout.setHorizontalGroup(
            FilterPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FilterPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(FilterPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FilterPanel2Layout.createSequentialGroup()
                        .addComponent(tenTruyen1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel10))
                    .addGroup(FilterPanel2Layout.createSequentialGroup()
                        .addComponent(btnFilter4)
                        .addGap(18, 18, 18)
                        .addComponent(btnFilter5)))
                .addGap(0, 85, Short.MAX_VALUE))
        );
        FilterPanel2Layout.setVerticalGroup(
            FilterPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FilterPanel2Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(FilterPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tenTruyen1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addGap(18, 18, 18)
                .addGroup(FilterPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFilter4)
                    .addComponent(btnFilter5))
                .addContainerGap(43, Short.MAX_VALUE))
        );

        jLabel11.setForeground(new java.awt.Color(51, 51, 51));
        jLabel11.setText("Ngày đăng:");

        day.setText("13");
        day.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dayActionPerformed(evt);
            }
        });

        month.setText("11");
        month.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monthActionPerformed(evt);
            }
        });

        year.setText("2025");
        year.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yearActionPerformed(evt);
            }
        });

        jLabel12.setForeground(new java.awt.Color(51, 51, 51));
        jLabel12.setText("/");

        jLabel13.setForeground(new java.awt.Color(51, 51, 51));
        jLabel13.setText("/");

        javax.swing.GroupLayout sideBoxLayout = new javax.swing.GroupLayout(sideBox);
        sideBox.setLayout(sideBoxLayout);
        sideBoxLayout.setHorizontalGroup(
            sideBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sideBoxLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sideBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sideBoxLayout.createSequentialGroup()
                        .addComponent(FilterPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(30, 30, 30))
                    .addGroup(sideBoxLayout.createSequentialGroup()
                        .addGroup(sideBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addGroup(sideBoxLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(day, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(month, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(year, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(sideBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(sideBoxLayout.createSequentialGroup()
                    .addGap(111, 111, 111)
                    .addComponent(jLabel13)
                    .addContainerGap(230, Short.MAX_VALUE)))
        );
        sideBoxLayout.setVerticalGroup(
            sideBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sideBoxLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(FilterPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sideBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(day, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(month, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(year, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addContainerGap(314, Short.MAX_VALUE))
            .addGroup(sideBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(sideBoxLayout.createSequentialGroup()
                    .addGap(237, 237, 237)
                    .addComponent(jLabel13)
                    .addContainerGap(315, Short.MAX_VALUE)))
        );

        btnFilter2.setBackground(new java.awt.Color(255, 130, 0));
        btnFilter2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnFilter2.setForeground(new java.awt.Color(255, 255, 255));
        btnFilter2.setText("ĐĂNG");
        btnFilter2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilter2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sideBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 757, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox1)
                            .addComponent(btnFilter2, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 557, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sideBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnFilter2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 19, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
 
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
            // TODO add your handling code here:
            refreshData();
    }//GEN-LAST:event_btnResetActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnFilter2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter2ActionPerformed
        postSelectedBooks();
    }//GEN-LAST:event_btnFilter2ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
        toggleSelectAll();
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void tenTruyen1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tenTruyen1ActionPerformed
        filterBooksByAccount();
  
    }//GEN-LAST:event_tenTruyen1ActionPerformed

    private void btnFilter4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter4ActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_btnFilter4ActionPerformed

    private void btnFilter5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter5ActionPerformed
        // TODO add your handling code here:
        tenTruyen1.setSelectedIndex(0);
        loadBooks(null);
    }//GEN-LAST:event_btnFilter5ActionPerformed

    private void dayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dayActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dayActionPerformed

    private void monthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monthActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_monthActionPerformed

    private void yearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yearActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_yearActionPerformed

    /**
     * Khởi tạo dữ liệu ban đầu
     */
    private void initData() {
        try {
            // Load tất cả accounts
            allAccounts = accountDao.getAllAccounts();
            tenTruyen1.removeAllItems();
            tenTruyen1.addItem("-- Tất cả --");
            for (Account account : allAccounts) {
                tenTruyen1.addItem(account.getUsername());
            }
            
            // Load books
            loadBooks(null);
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi tải dữ liệu: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /**
     * Làm mới dữ liệu
     */
    private void refreshData() {
        initData();
    }
    
    /**
     * Load books theo account được chọn
     */
    private void filterBooksByAccount() {
        String selectedAccount = (String) tenTruyen1.getSelectedItem();
        Long accountId = null;
        
        if (selectedAccount != null && !selectedAccount.equals("-- Tất cả --")) {
            for (Account account : allAccounts) {
                if (account.getUsername().equals(selectedAccount)) {
                    accountId = account.getId();
                    break;
                }
            }
        }
        
        loadBooks(accountId);
    }
    
    /**
     * Load danh sách books có post_status = not_hoan
     */
    private void loadBooks(Long accountId) {
        try {
            bookEntries.clear();
            
            List<Book> books;
            if (accountId == null) {
                // Lấy tất cả books có post_status = not_hoan
                books = bookDao.getBooksByStatus(null, null, PostStatus.NOT_HOAN);
            } else {
                // Lấy books theo account và post_status
                books = filterBooksByAccountId(accountId, PostStatus.NOT_HOAN);
            }
            
            for (Book book : books) {
                Account account = null;
                if (book.getAccountId() != null) {
                    account = accountDao.getAccountById(book.getAccountId());
                }
                bookEntries.add(new BookEntry(book, account));
            }
            
            tableModel.fireTableDataChanged();
            updateToChapters();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi tải danh sách truyện: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        
    }
    
    
    /**
     * Lọc books theo accountId và postStatus
     */
    private List<Book> filterBooksByAccountId(Long accountId, PostStatus postStatus) throws SQLException {
        List<Book> allBooks = bookDao.getBooksByStatus(null, null, PostStatus.NOT_HOAN);
        List<Book> filtered = new ArrayList<>();
        
        for (Book book : allBooks) {
            if (book.getAccountId() != null && book.getAccountId().equals(accountId)) {
                filtered.add(book);
            }
        }
        
        return filtered;
    }
    
    
    /**
     * Cập nhật "Đến chương" khi thay đổi số chương
     */
    private void updateToChapters() {
        if (txtNumChapter == null || bookEntries == null) {
            return;
        }

        String rawValue = txtNumChapter.getText();
        if (rawValue == null) {
            return;
        }

        rawValue = rawValue.trim();
        if (rawValue.isEmpty()) {
            return;
        }        
        try {
            int numChapter = Integer.parseInt(rawValue);
            if (numChapter <= 0) {
                JOptionPane.showMessageDialog(this, "Số chương phải lớn hơn 0");
                txtNumChapter.setText(String.valueOf(DEFAULT_NUM_CHAPTER));
                return;
            }
            
            for (BookEntry entry : bookEntries) {
                entry.setToChapter(entry.getFromChapter() + numChapter);
            }
            
            if (tableModel != null) {
                tableModel.fireTableDataChanged();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số chương không hợp lệ");
            txtNumChapter.setText(String.valueOf(DEFAULT_NUM_CHAPTER));
        }
    }
    
    /**
     * Toggle chọn tất cả / bỏ chọn tất cả
     */
    private void toggleSelectAll() {
        boolean selected = jCheckBox1.isSelected();
        for (BookEntry entry : bookEntries) {
            entry.setSelected(selected);
        }
        tableModel.fireTableDataChanged();
    }
    
    /**
     * Đăng các truyện đã chọn
     */
    private void postSelectedBooks() {
        List<BookEntry> selectedBooks = new ArrayList<>();
        for (BookEntry entry : bookEntries) {
            if (entry.isSelected()) {
                selectedBooks.add(entry);
            }
        }
        
        if (selectedBooks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một truyện để đăng");
            return;
        }
        
        // Validate ngày đăng
        LocalDate scheduleDate;
        try {
            int day = Integer.parseInt(txtDay.getText().trim());
            int month = Integer.parseInt(txtMonth.getText().trim());
            int year = Integer.parseInt(txtYear.getText().trim());
            scheduleDate = LocalDate.of(year, month, day);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ngày đăng không hợp lệ: " + ex.getMessage());
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Bạn có chắc chắn muốn đăng %d truyện?\nNgày đăng: %s",
                selectedBooks.size(), scheduleDate),
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Hiển thị progress dialog
        JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Đang đăng...", true);
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressDialog.add(progressBar);
        progressDialog.setSize(400, 100);
        progressDialog.setLocationRelativeTo(this);
        
        // Thực hiện đăng trong background thread
        SwingWorker<PostingSummary, Integer> worker = new SwingWorker<PostingSummary, Integer>() {
            @Override
            protected PostingSummary doInBackground() throws Exception {
                PostingSummary summary = new PostingSummary();
                int totalBooks = selectedBooks.size();
                
                for (int i = 0; i < totalBooks; i++) {
                    BookEntry entry = selectedBooks.get(i);
                    try {
                        postBook(entry, scheduleDate, summary);
                        publish((i + 1) * 100 / totalBooks);
                    } catch (Exception ex) {
                        summary.addError(String.format("Truyện '%s': %s",
                            entry.getBookTitle(), ex.getMessage()));
                        ex.printStackTrace();
                    }
                }
                
                return summary;
            }
            
            @Override
            protected void process(List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    progressBar.setValue(chunks.get(chunks.size() - 1));
                }
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    PostingSummary summary = get();
                    showPostingSummary(summary);
                    refreshData(); // Làm mới danh sách
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DangTruyen.this,
                        "Lỗi không mong muốn: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    /**
     * Đăng một truyện
     */
    private void postBook(BookEntry entry, LocalDate scheduleDate, PostingSummary summary) throws Exception {
        Book book = entry.getBook();
        Account account = entry.getAccount();
        
        if (account == null) {
            throw new IllegalStateException("Truyện chưa được gán tài khoản");
        }
        
        // Load chapters của book
        Map<Integer, Chapter> chaptersMap = loadChaptersForBook(book.getId());
        
        // Load lịch đăng
        List<Post> schedule = postDao.findByBookId(book.getId());
        if (schedule.isEmpty()) {
            throw new IllegalStateException("Chưa có lịch đăng cho truyện này");
        }
        
        int fromChapter = entry.getFromChapter();
        int numberOfChapters = entry.getChapterCount();
        if (numberOfChapters <= 0) {
            throw new IllegalStateException("Số chương cần đăng phải lớn hơn 0");
        }
        
        int chaptersPosted = 0;
        
        for (int offset = 0; offset < numberOfChapters; offset++) {
            int chapterNumber = fromChapter + offset;
            Chapter chapter = chaptersMap.get(chapterNumber);
            
            if (chapter == null) {
                throw new IllegalStateException("Không tìm thấy chương " + chapterNumber);
            }
            
            if (chapter.getChapterNumber() == null) {
                throw new IllegalStateException("Chương " + chapterNumber + " chưa có số thứ tự hợp lệ");
            }
            
            if (chapter.getBetaStatus() != BetaStatus.DONE_BETA && 
                chapter.getBetaStatus() != BetaStatus.DONE_POST) {
                throw new IllegalStateException("Chương " + chapterNumber + " chưa beta xong");
            }
            
            if (chapter.getContent() == null || chapter.getContent().isBlank()) {
                throw new IllegalStateException("Chương " + chapterNumber + " chưa có nội dung");
            }
            
            // Lấy lịch đăng tương ứng
            int scheduleIndex = offset;
            if (scheduleIndex >= schedule.size()) {
                throw new IllegalStateException("Không đủ lịch đăng cho chương " + chapterNumber);
            }
            
            Post slot = schedule.get(scheduleIndex);
            LocalDateTime scheduleDateTime = buildScheduleDateTime(scheduleDate, slot);
            
            // Chia chapter thành 2 phần
            List<String> parts = splitChapterContent(chapter.getContent());
            
            boolean chapterSuccess = true;
            for (int partIndex = 0; partIndex < parts.size(); partIndex++) {
                String content = parts.get(partIndex);
                if (content == null) {
                    content = "";
                }
                
                String title = buildChapterTitle(chapter, partIndex);
                String auth2 = buildAuth2(book.getId(), chapter.getChapterNumber(), partIndex);
                String requestBody = buildRequestBody(entry, account, title, auth2, scheduleDateTime, content);
                
                //executePostRequest(book, chapter.getChapterNumber(), partIndex, requestBody, summary);
            }
            
            //chaptersPosted++;
        }
        
        // Cập nhật posted trong database
        if (chaptersPosted > 0) {
            int newPosted = (book.getPosted() != null ? book.getPosted() : 0) + chaptersPosted;
            book.setPosted(newPosted);
            bookDao.updateBook(book);
            
            summary.addSuccess(String.format("Truyện '%s': Đăng thành công %d chương (posted: %d)",
                book.getTitle(), chaptersPosted, newPosted));
        }
    }
    
    /**
     * Load chapters của một book
     */
    private Map<Integer, Chapter> loadChaptersForBook(long bookId) throws SQLException {
        Map<Integer, Chapter> result = new HashMap<>();
        List<Chapter> chapters = chapterDao.findByBookId(bookId);
        for (Chapter chapter : chapters) {
            if (chapter.getChapterNumber() != null) {
                result.put(chapter.getChapterNumber(), chapter);
            }
        }
        return result;
    }
    
    /**
     * Tạo LocalDateTime từ ngày và lịch đăng
     */
    private LocalDateTime buildScheduleDateTime(LocalDate baseDate, Post slot) {
        if (baseDate == null) {
            baseDate = LocalDate.now().plusDays(1);
        }
        if (slot == null) {
            return LocalDateTime.of(baseDate, LocalTime.of(0, 0));
        }
        
        int hour = slot.getHour();
        int minute = slot.getMinute();
        
        try {
            if (hour == 24 && minute == 0) {
                return LocalDateTime.of(baseDate.plusDays(1), LocalTime.MIDNIGHT);
            }
            return LocalDateTime.of(baseDate, LocalTime.of(hour, minute));
        } catch (Exception e) {
            return LocalDateTime.of(baseDate, LocalTime.of(0, 0));
        }
    }
    
    /**
     * Chia content của chapter thành 2 phần
     */
    private List<String> splitChapterContent(String content) {
        if (content == null || content.isEmpty()) {
            return Arrays.asList("", "");
        }
        
        String normalizedContent = normalizeChapterContent(content);
        String[] parts = splitContentIntoParts(normalizedContent, 2);

        if (parts.length == 1) {
            return Arrays.asList(parts[0], "");
        }
        return Arrays.asList(parts[0], parts[1]);
    }

    private String normalizeChapterContent(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        String normalized = content.replace("\r\n", "\n").replace("\r", "\n");
        normalized = normalized.replaceAll("\\n\\s+\\n", "\n\n");
        normalized = normalized.replaceAll("(?<=\\S)\\n(?=\\s*\\S)", "\n\n");
        normalized = normalized.replaceAll("\\n{3,}", "\n\n");

        String[] paragraphs = normalized.split("\\n{2}");
        StringBuilder processed = new StringBuilder();
        for (String paragraph : paragraphs) {
            String cleanedParagraph = removeLeadingSpaces(paragraph);
            if (cleanedParagraph.isEmpty()) {
                continue;
            }
            String capitalizedParagraph = capitalizeFirstLetter(cleanedParagraph);
            if (processed.length() > 0) {
                processed.append("\n\n");
            }
            processed.append(capitalizedParagraph);
        }
        return processed.toString();
    }

    private String removeLeadingSpaces(String paragraph) {
        int index = 0;
        while (index < paragraph.length() && paragraph.charAt(index) == ' ') {
            index++;
        }
        return paragraph.substring(index);
    }

    private String capitalizeFirstLetter(String paragraph) {
        for (int i = 0; i < paragraph.length(); i++) {
            char current = paragraph.charAt(i);
            if (Character.isLetter(current)) {
                if (!Character.isUpperCase(current)) {
                    return paragraph.substring(0, i) + Character.toUpperCase(current) + paragraph.substring(i + 1);
                }
                break;
            }
        }
        return paragraph;
    }

    private String[] splitContentIntoParts(String content, int numberOfParts) {
        String[] result = new String[numberOfParts];
        if (content == null) {
            Arrays.fill(result, "");
            return result;
        }

        int len = content.length();
        int start = 0;
        for (int i = 0; i < numberOfParts; i++) {
            start = Math.min(start, len);
            if (i == numberOfParts - 1) {
                result[i] = content.substring(start);
            } else {
                int approxEnd = start + (len - start) / (numberOfParts - i);
                int idx = content.lastIndexOf('\n', approxEnd);
                if (idx < start) {
                    idx = approxEnd;
                }
                int end = Math.max(start, Math.min(idx, len));
                if (idx < len && idx >= start && content.charAt(idx) == '\n') {
                    end = idx + 1;
                }
                result[i] = content.substring(start, Math.min(end, len));
                start = Math.min(len, Math.max(end, start));
                while (start < len && (content.charAt(start) == '\n' || content.charAt(start) == '\r')) {
                    start++;
                }

            }
        }
        
        for (int i = 0; i < result.length; i++) {
            if (result[i] == null) {
                result[i] = "";
            }
        }
        return result;
    }
    
    /**
     * Tạo title cho chapter
     */
    private String buildChapterTitle(Chapter chapter, int partIndex) {
        int chapterNumber = chapter.getChapterNumber() != null ? chapter.getChapterNumber() : 0;
        String suffix = partIndex == 0 ? ".1" : ".2";
        String baseTitle = chapter.getChapterTitle() != null ? chapter.getChapterTitle() : "";
        return "Chương " + chapterNumber + suffix + ":" + baseTitle;
    }
    
    /**
     * Tạo auth2 cho request
     */
    private String buildAuth2(long bookId, int chapterNumber, int partIndex) {
        int order = partIndex == 0 ? chapterNumber * 2 - 1 : chapterNumber * 2;
        return "TruyenHD" + bookId + order + "themchuong";
    }
    
    /**
     * Tạo request body cho cURL
     */
    private String buildRequestBody(BookEntry entry, Account account, String title, String auth2,
                                    LocalDateTime scheduleDateTime, String content) throws IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        
        payload.put("price", entry.getPriceString());
        payload.put("registered", defaultString(account.getRegistered(), 
            DATE_TIME_FORMATTER.format(LocalDateTime.now())));
        payload.put("title", title);
        payload.put("auth2", auth2);
        payload.put("user_id", account.getId() != null ? String.valueOf(account.getId()) : "");
        payload.put("activation_key", defaultString(account.getActivationKey(), ""));
        payload.put("versionIOS", "1");
        payload.put("auth", defaultString(account.getAuth(), ""));
        payload.put("date_schedule", DATE_TIME_FORMATTER.format(scheduleDateTime));
        payload.put("suggest_password", "");
        payload.put("id", String.valueOf(entry.getBook().getId()));
        payload.put("content", content);
        payload.put("email", defaultString(account.getEmail(), ""));
        payload.put("value_password", "");
        payload.put("uuid", defaultString(account.getUuid(), ""));
        payload.put("status", "future");
        
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(payload);
    }
    
    /**
     * Thực thi POST request
     */
    private boolean executePostRequest(Book book, int chapterNumber, int partIndex,
                                   String requestBody, PostingSummary summary) {
        boolean success = false;
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "curl",
                "-H", "Host: s1apihd.com",
                "-H", "accept: */*",
                "-H", "content-type: application/json",
                "-H", "user-agent: TruyenHD/2.3 (com.vnvnads.TruyenHD; build:32; iOS 18.3.1) Alamofire/5.9.0",
                "-H", "accept-language: vi-VN;q=1.0, en-VN;q=0.9",
                "--data-binary", requestBody,
                "--compressed",
                API_ENDPOINT
            );
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                String partName = partIndex == 0 ? "phần 1" : "phần 2";
                summary.addSuccess(String.format("  - Chương %d %s: OK", chapterNumber, partName));
                success = true;
            } else {
                throw new RuntimeException("cURL exit code: " + exitCode);
            }
            
        } catch (Exception ex) {
            String partName = partIndex == 0 ? "phần 1" : "phần 2";
            summary.addError(String.format("Truyện '%s' chương %d %s: %s",
                book.getTitle(), chapterNumber, partName, ex.getMessage()));
        }
        return success;
    }
    
    /**
     * Hiển thị kết quả đăng
     */
    private void showPostingSummary(PostingSummary summary) {
        StringBuilder message = new StringBuilder();
        message.append("Hoàn thành đăng truyện!\n\n");
        message.append(String.format("Thành công: %d yêu cầu\n", summary.getSuccessCount()));
        
        if (summary.hasErrors()) {
            message.append(String.format("\nLỗi: %d yêu cầu\n", summary.getErrorMessages().size()));
            message.append("\nChi tiết lỗi:\n");
            for (String error : summary.getErrorMessages()) {
                message.append("- ").append(error).append("\n");
            }
        }
        
        int messageType = summary.hasErrors() ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
        String content = message.length() > 0 ? message.toString() : "Không có yêu cầu nào được gửi.";
        
        JOptionPane.showMessageDialog(this, content, "Kết quả đăng", messageType);
    }
    
    /**
     * Utility method: trả về giá trị mặc định nếu chuỗi rỗng
     */
    private String defaultString(String value, String defaultValue) {
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
    
    // ========== Inner Classes ==========
    
    /**
     * Table model cho bảng books
     */
    private class BookTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "Chọn", "Tài khoản", "Tên truyện", "Giá/chương",
            "Từ chương", "Đến chương"
        };
        
        @Override
        public int getRowCount() {
            return bookEntries.size();
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            }
            return String.class;
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
            // Cột "Chọn" và "Đến chương" có thể edit
            return column == 0 || column == 5;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            BookEntry entry = bookEntries.get(rowIndex);
            
            switch (columnIndex) {
                case 0: return entry.isSelected();
                case 1: return entry.getAccountName();
                case 2: return entry.getBookTitle();
                case 3: return entry.getPriceDisplay();
                case 4: return entry.getFromChapter();
                case 5: return entry.getToChapter();
                default: return "";
            }
        }
        
        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            BookEntry entry = bookEntries.get(rowIndex);
            
            if (columnIndex == 0) {
                entry.setSelected((Boolean) value);
            } else if (columnIndex == 5) {
                try {
                    int toChapter = Integer.parseInt(value.toString());
                    entry.setToChapter(toChapter);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(DangTruyen.this, ex.getMessage());
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(DangTruyen.this, ex.getMessage());
                }
            }
            
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
    
    /**
     * Renderer cho checkbox
     */
    private class CheckBoxRenderer extends JCheckBox implements javax.swing.table.TableCellRenderer {
        public CheckBoxRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setSelected(value != null && (Boolean) value);
            return this;
        }
    }
    
    /**
     * Editor cho checkbox
     */
    private class CheckBoxEditor extends DefaultCellEditor {
        public CheckBoxEditor() {
            super(new JCheckBox());
            JCheckBox checkBox = (JCheckBox) getComponent();
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }
    
    /**
     * Class lưu thông tin một book entry
     */
    private class BookEntry {
        private final Book book;
        private Account account;
        private final BigDecimal price;
        private boolean selected;
        private final int fromChapter;
        private int chapterOffset;
        
        BookEntry(Book book, Account account) {
            this.book = book;
            this.account = account;
            this.price = book.getPrice() != null ? book.getPrice() : BigDecimal.ZERO;
            
            int posted = book.getPosted() != null ? book.getPosted() : 0;
            this.fromChapter = posted + 1;
            
            try {
                int numChapter = Integer.parseInt(txtNumChapter.getText().trim());
                this.chapterOffset = numChapter;
            } catch (NumberFormatException ex) {
                this.chapterOffset = DEFAULT_NUM_CHAPTER;
            }
        }
        
        Book getBook() {
            return book;
        }
        
        Account getAccount() {
            return account;
        }
        
        void setAccount(Account account) {
            this.account = account;
        }
        
        boolean isSelected() {
            return selected;
        }
        
        void setSelected(boolean selected) {
            this.selected = selected;
        }
        
        int getFromChapter() {
            return fromChapter;
        }
        
        int getToChapter() {
            return fromChapter + chapterOffset;
        }
        
        int getChapterCount() {
            return chapterOffset;
        }        
        
        void setToChapter(int toChapter) {
            if (toChapter < fromChapter) {
                throw new IllegalArgumentException("'Đến chương' phải lớn hơn hoặc bằng 'Từ chương'.");
            }
            this.chapterOffset = toChapter - fromChapter;
        }
        
        String getAccountName() {
            return account != null && account.getUsername() != null ? 
                account.getUsername() : "(Chưa gán)";
        }
        
        String getBookTitle() {
            return book.getTitle() != null ? book.getTitle() : "";
        }
        
        String getPriceDisplay() {
            return price != null ? price.toString() : "0";
        }
        
        String getPriceString() {
            return price != null ? price.toString() : "0";
        }
    }
    
    /**
     * Class tổng hợp kết quả đăng
     */
    private static class PostingSummary {
        private final List<String> successMessages = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();
        
        void addSuccess(String message) {
            successMessages.add(message);
        }
        
        void addError(String message) {
            errorMessages.add(message);
        }
        
        int getSuccessCount() {
            return successMessages.size();
        }
        
        List<String> getErrorMessages() {
            return errorMessages;
        }
        
        boolean hasErrors() {
            return !errorMessages.isEmpty();
        }
    }
  
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel FilterPanel2;
    private javax.swing.JTable Tbooks;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnFilter2;
    private javax.swing.JButton btnFilter4;
    private javax.swing.JButton btnFilter5;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JTextField day;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField month;
    private javax.swing.JPanel sideBox;
    private javax.swing.JComboBox<String> tenTruyen1;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField year;
    // End of variables declaration//GEN-END:variables
}
