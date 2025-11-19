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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.sql.DataSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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
    private JTextField txtDays; 
    
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
        this.txtDays = this.day1; 

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
        initializeDaysControl(); 
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
    }

    private void initializeDaysControl() {
        if (txtDays == null) {
            return;
        }

        txtDays.setText("1"); // Mặc định 1 ngày
        txtDays.setHorizontalAlignment(JTextField.CENTER);
        txtDays.setToolTipText("Số ngày liên tục đăng (mỗi ngày 5 chương)");

        // Thêm listener để tự động cập nhật "Đến chương"
        txtDays.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateToChaptersWithDays();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateToChaptersWithDays();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateToChaptersWithDays();
            }
        });
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
        jLabel14 = new javax.swing.JLabel();
        day1 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addGap(0, 31, Short.MAX_VALUE))
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
        jLabel11.setText("Bắt đầu tư  ngày:");

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

        jLabel14.setForeground(new java.awt.Color(51, 51, 51));
        jLabel14.setText("Đăng liên tục");

        day1.setText("01");
        day1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                day1ActionPerformed(evt);
            }
        });

        jLabel15.setForeground(new java.awt.Color(51, 51, 51));
        jLabel15.setText("ngày");

        javax.swing.GroupLayout sideBoxLayout = new javax.swing.GroupLayout(sideBox);
        sideBox.setLayout(sideBoxLayout);
        sideBoxLayout.setHorizontalGroup(
            sideBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sideBoxLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sideBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sideBoxLayout.createSequentialGroup()
                        .addComponent(FilterPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(12, 12, 12))
                    .addGroup(sideBoxLayout.createSequentialGroup()
                        .addGroup(sideBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addGroup(sideBoxLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(day, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(jLabel12)
                                .addGap(5, 5, 5)
                                .addComponent(month, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(jLabel13)
                                .addGap(5, 5, 5)
                                .addComponent(year, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(113, Short.MAX_VALUE))
                    .addGroup(sideBoxLayout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(day1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel15)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        sideBoxLayout.setVerticalGroup(
            sideBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sideBoxLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(FilterPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addGroup(sideBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(day1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(20, 20, 20)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sideBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(day, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(month, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(year, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(jLabel13))
                .addContainerGap(314, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 757, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnFilter2, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox1))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(sideBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnFilter2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(87, Short.MAX_VALUE))
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void day1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_day1ActionPerformed
        // TODO add your handling code here:
        updateToChaptersWithDays();
    }//GEN-LAST:event_day1ActionPerformed

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
    private void updateToChaptersWithDays() {
        if (txtDays == null || bookEntries == null) {
            return;
        }

        String rawValue = txtDays.getText();
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return;
        }

        try {
            int days = Integer.parseInt(rawValue.trim());
            if (days <= 0) {
                JOptionPane.showMessageDialog(this, "Số ngày phải lớn hơn 0");
                txtDays.setText("1");
                return;
            }

            // Công thức: "Đến chương" = "Từ chương" + days * 5 - 1
            // Số chương = days * 5
            int numChapters = days * 5;

            for (BookEntry entry : bookEntries) {
                entry.setChapterOffset(numChapters);
            }

            if (tableModel != null) {
                tableModel.fireTableDataChanged();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số ngày không hợp lệ");
            txtDays.setText("1");
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
                    refreshData();
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
    private void postBook(BookEntry entry, LocalDate scheduleDate, PostingSummary summary) 
        throws Exception {
        Book book = entry.getBook();
        Account account = entry.getAccount();

        if (account == null) {
            throw new IllegalStateException("Truyện chưa được gán tài khoản");
        }

        // Load chapters từ database
        Map<Integer, Chapter> chaptersMap = loadChaptersForBook(book.getId());

        if (account.getId() == null) {
            throw new IllegalStateException("Tài khoản chưa có ID hợp lệ");
        }

        // Load lịch đăng theo tài khoản
        List<Post> schedule = postDao.findByAccountId(account.getId());
        if (schedule.isEmpty()) {
            throw new IllegalStateException(String.format(
                "Chưa có lịch đăng cho tài khoản %s",
                account.getUsername() != null ? account.getUsername() : account.getEmail())
            );
        }

        // ✅ TÍNH SỐ NGÀY CẦN ĐĂNG
        int fromChapter = entry.getFromChapter(); 
        int toChapter = entry.getToChapter(); 
        int totalChapters = toChapter - fromChapter + 1;

        // Mỗi chapter = 2 parts, nên cần totalChapters * 2 slots
        int totalSlotsNeeded = totalChapters * 2;

        // Số slots mỗi ngày
        int slotsPerDay = schedule.size();

        // Tính số ngày cần
        int daysNeeded = (int) Math.ceil((double) totalSlotsNeeded / slotsPerDay);

        System.out.println("═══════════════════════════════════");
        System.out.println("📚 ĐĂNG TRUYỆN: " + book.getTitle());
        System.out.println("📖 Từ chương: " + fromChapter + " → Đến chương: " + toChapter);
        System.out.println("📊 Tổng số chương: " + totalChapters);
        System.out.println("📊 Tổng số parts: " + totalSlotsNeeded);
        System.out.println("📅 Slots mỗi ngày: " + slotsPerDay);
        System.out.println("📅 Số ngày cần đăng: " + daysNeeded);
        System.out.println("═══════════════════════════════════");

        int slotIndex = 0;
        int chaptersPosted = 0;
        LocalDate currentDate = scheduleDate;

        for (int chapterNum = fromChapter; chapterNum <= toChapter; chapterNum++) {
            Chapter chapter = chaptersMap.get(chapterNum);

            if (chapter == null) {
                summary.addError(String.format("Không tìm thấy chương %d của truyện '%s'", 
                    chapterNum, book.getTitle()));
                continue;
            }

            String content = chapter.getContent();
            if (content == null || content.trim().isEmpty()) {
                summary.addError(String.format("Chương %d của truyện '%s' chưa có nội dung", 
                    chapterNum, book.getTitle()));
                continue;
            }

            // Chia content thành 2 phần
            List<String> parts = splitChapterContent(content);

            boolean chapterSuccess = true;
            for (int partIndex = 0; partIndex < parts.size(); partIndex++) {
                String partContent = parts.get(partIndex);

                // ✅ KIỂM TRA NẾU HẾT SLOTS TRONG NGÀY → CHUYỂN SANG NGÀY TIẾP THEO
                if (slotIndex >= schedule.size()) {
                    slotIndex = 0; // Reset về slot đầu tiên
                    currentDate = currentDate.plusDays(1); // Sang ngày tiếp theo

                    System.out.println("🔄 Chuyển sang ngày mới: " + currentDate);
                }

                Post slot = schedule.get(slotIndex);
                LocalDateTime scheduleDateTime = buildScheduleDateTime(currentDate, slot);

                // Tạo title: "Chương 101.1:..." hoặc "Chương 101.2:..."
                String title = buildChapterTitle(chapter, partIndex);

                // Tạo auth2 token
                String auth2 = buildAuth2(book.getId(), chapter.getChapterNumber(), partIndex);

                // Tạo request body JSON
                String requestBody = buildRequestBody(entry, account, title, auth2, 
                                                     scheduleDateTime, partContent);

                // 🚀 Gửi request qua cURL
                boolean success = executePostRequest(book, chapter.getChapterNumber(), 
                                                   partIndex, requestBody, summary);

                if (!success) {
                    chapterSuccess = false;
                }

                slotIndex++;
            }

            if (chapterSuccess) {
                chaptersPosted++;
            }
        }

        // Cập nhật số chương đã đăng vào database
        if (chaptersPosted > 0) {
            int newPosted = (book.getPosted() != null ? book.getPosted() : 0) + chaptersPosted;
            book.setPosted(newPosted);
            bookDao.updateBook(book);

            summary.addSuccess(String.format("Truyện '%s': Đăng thành công %d chương (posted: %d → %d)",
                book.getTitle(), chaptersPosted, book.getPosted() - chaptersPosted, newPosted));
        }
    }
    /**
     * ✅ NEW: Lấy chapter_per_day từ Book
     */
    private int getChapterPerDay(Book book) {
        // Nếu Book model có field chapterPerDay
        if (book.getChapterPerDay() != null && book.getChapterPerDay() > 0) {
            return book.getChapterPerDay();
        }
        
        // Mặc định: 5 chương/ngày
        return DEFAULT_NUM_CHAPTER;
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
     * ✅ FIXED: Tạo LocalDateTime từ ngày và slot
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
            // Trường hợp đặc biệt: 24:00 → 00:00 ngày hôm sau
            if (hour == 24 && minute == 0) {
                return LocalDateTime.of(baseDate.plusDays(1), LocalTime.MIDNIGHT);
            }
            return LocalDateTime.of(baseDate, LocalTime.of(hour, minute));
        } catch (Exception e) {
            return LocalDateTime.of(baseDate, LocalTime.of(0, 0));
        }
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
    private List<String> splitChapterContent(String content) {
        if (content == null || content.isEmpty()) {
            return Arrays.asList("", "");
        }

        // Chia content thành 2 phần bằng nhau
        String[] parts = splitContentIntoParts(content, 2);
        return Arrays.asList(parts[0], parts[1]);
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
        return "Chương " + chapterNumber + suffix + ": " + baseTitle;
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
      private String buildRequestBody(BookEntry entry, Account account, String title, String auth2, LocalDateTime scheduleDateTime, String content) throws IOException {          
        if (account == null) {
            throw new IllegalArgumentException("Thiếu thông tin tài khoản khi tạo request đăng truyện");
        }          

        // ✅ DEBUG: Kiểm tra content trước khi đưa vào JSON
        System.out.println("📝 Content preview (first 100 chars):");
        String preview = content.length() > 100 ? content.substring(0, 100) : content;
        System.out.println(preview);
        System.out.println("📊 Content length: " + content.length() + " chars");

        Map<String, Object> payload = new LinkedHashMap<>();

        Book book = entry != null ? entry.getBook() : null;
        String scheduleString = formatScheduleDate(scheduleDateTime);

        payload.put("email", safeString(account.getEmail()));
        payload.put("content", content != null ? content : "");
        payload.put("auth", safeString(book != null ? book.getAuth() : null));
        payload.put("price", entry != null ? entry.getPriceString() : "0");
        payload.put("auth2", auth2);
        payload.put("versionIOS", resolveVersionIos(account));
        payload.put("uuid", safeString(account.getUuid()));
        payload.put("suggest_password", "");
        payload.put("title", title != null ? title : "");
        payload.put("registered", resolveRegisteredAt(account));
        payload.put("id", book != null && book.getId() != null ? String.valueOf(book.getId()) : "");
        payload.put("status", resolveStatus(scheduleDateTime));
        payload.put("date_schedule", scheduleString);
        payload.put("user_id", account.getId() != null ? String.valueOf(account.getId()) : "");
        payload.put("activation_key", safeString(account.getActivationKey()));
        payload.put("value_password", safeString(account.getPassword()));

        // ✅ Tạo JSON với ObjectMapper (tự động handle UTF-8)
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(payload);

        // ✅ DEBUG: Kiểm tra JSON output
        System.out.println("📋 JSON preview (first 200 chars):");
        String jsonPreview = json.length() > 200 ? json.substring(0, 200) : json;
        System.out.println(jsonPreview);

        return json;
    }

    private String resolveVersionIos(Account account) {
        if (account == null || account.getVersionIos() == null || account.getVersionIos().isBlank()) {
            return "1";
        }
        return account.getVersionIos();
    }

    private String resolveRegisteredAt(Account account) {
        if (account == null || account.getRegistered() == null || account.getRegistered().isBlank()) {
            return DATE_TIME_FORMATTER.format(LocalDateTime.now());
        }
        String registered = account.getRegistered().trim();
        String normalized = registered.replace('T', ' ');
        try {
            LocalDateTime parsed = LocalDateTime.parse(normalized, DATE_TIME_FORMATTER);
            return parsed.format(DATE_TIME_FORMATTER);
        } catch (Exception ex) {
            try {
                LocalDateTime parsed = LocalDateTime.parse(registered);
                return parsed.format(DATE_TIME_FORMATTER);
            } catch (Exception ignored) {
                return normalized;
            }
        }
    }

    private String resolveStatus(LocalDateTime scheduleDateTime) {
        if (scheduleDateTime == null) {
            return "future";
        }
        return scheduleDateTime.isAfter(LocalDateTime.now()) ? "future" : "publish";
    }

    private String formatScheduleDate(LocalDateTime scheduleDateTime) {
        if (scheduleDateTime == null) {
            return DATE_TIME_FORMATTER.format(LocalDateTime.now());
        }
        return scheduleDateTime.format(DATE_TIME_FORMATTER);
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }      
    /**
     * Thực thi POST request
     */
    private boolean executePostRequest(Book book, int chapterNumber, int partIndex,
                                   String requestBody, PostingSummary summary) {
        File tempFile = null;
        boolean success = false;

        try {
            // Tạo file tạm
            tempFile = File.createTempFile("curl_request_", ".json");

            // Ghi file với UTF-8 encoding
            java.nio.file.Files.write(
                tempFile.toPath(), 
                requestBody.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            System.out.println("═══════════════════════════════════");
            System.out.println("📝 ĐANG ĐĂNG CHƯƠNG");
            System.out.println("═══════════════════════════════════");
            System.out.println("📚 Truyện: " + book.getTitle());
            System.out.println("📖 Chương: " + chapterNumber + " - Phần: " + (partIndex + 1));
            System.out.println("🔗 API Endpoint: " + API_ENDPOINT);
            System.out.println("📄 Temp file: " + tempFile.getAbsolutePath());
            System.out.println("📏 Request body length: " + requestBody.length() + " chars");

            // ✅ IN RA CHI TIẾT CÁC TRƯỜNG QUAN TRỌNG
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> jsonMap = mapper.readValue(requestBody, Map.class);

                System.out.println("\n📋 REQUEST FIELDS:");
                System.out.println("  - email: " + jsonMap.get("email"));
                System.out.println("  - title: " + jsonMap.get("title"));
                System.out.println("  - auth: " + jsonMap.get("auth"));
                System.out.println("  - auth2: " + jsonMap.get("auth2"));
                System.out.println("  - user_id: " + jsonMap.get("user_id"));
                System.out.println("  - id (book_id): " + jsonMap.get("id"));
                System.out.println("  - uuid: " + jsonMap.get("uuid"));
                System.out.println("  - versionIOS: " + jsonMap.get("versionIOS"));
                System.out.println("  - registered: " + jsonMap.get("registered"));
                System.out.println("  - activation_key: " + jsonMap.get("activation_key"));
                System.out.println("  - status: " + jsonMap.get("status"));
                System.out.println("  - date_schedule: " + jsonMap.get("date_schedule"));
                System.out.println("  - price: " + jsonMap.get("price"));

                String content = (String) jsonMap.get("content");
                if (content != null) {
                    String preview = content.length() > 100 ? content.substring(0, 100) + "..." : content;
                    System.out.println("  - content (preview): " + preview);
                    System.out.println("  - content length: " + content.length() + " chars");
                }

            } catch (Exception e) {
                System.out.println("⚠️ Không thể parse JSON để hiển thị: " + e.getMessage());
            }

            System.out.println("\n🔧 CURL COMMAND:");
            ProcessBuilder pb = new ProcessBuilder(
                "curl",
                "-X", "POST",
                "-H", "Host: s1apihd.com",
                "-H", "accept: */*",
                "-H", "content-type: application/json; charset=utf-8",
                "-H", "user-agent: TruyenHD/2.3 (com.vnvnads.TruyenHD; build:32; iOS 18.3.1) Alamofire/5.9.0",
                "-H", "accept-language: vi-VN;q=1.0, en-VN;q=0.9",
                "--data-binary", "@" + tempFile.getAbsolutePath(),
                API_ENDPOINT
            );

            // In ra command để có thể test thủ công
            System.out.println("curl -X POST \\");
            System.out.println("  -H 'Host: s1apihd.com' \\");
            System.out.println("  -H 'accept: */*' \\");
            System.out.println("  -H 'content-type: application/json; charset=utf-8' \\");
            System.out.println("  -H 'user-agent: TruyenHD/2.3 (com.vnvnads.TruyenHD; build:32; iOS 18.3.1) Alamofire/5.9.0' \\");
            System.out.println("  -H 'accept-language: vi-VN;q=1.0, en-VN;q=0.9' \\");
            System.out.println("  --data-binary '@" + tempFile.getAbsolutePath() + "' \\");
            System.out.println("  " + API_ENDPOINT);

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Đọc output với UTF-8
            StringBuilder output = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream(), 
                        java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            String curlOutput = output.toString();

            System.out.println("\n📥 RESPONSE:");
            System.out.println("  - Exit code: " + exitCode);
            System.out.println("  - Output: " + curlOutput);

            // Parse response
            if (exitCode == 0) {
                if (curlOutput.contains("\"error\":0")) {
                    // Thành công
                    String postId = extractPostId(curlOutput);
                    String partName = partIndex == 0 ? ".1" : ".2";

                    summary.addSuccess(String.format(
                        "✅ %s - Chương %d%s (Post ID: %s)",
                        book.getTitle(), chapterNumber, partName, postId
                    ));

                    System.out.println("✅ SUCCESS - Post ID: " + postId);
                    success = true;

                } else {
                    // API trả về lỗi
                    summary.addError(String.format(
                        "❌ %s - Chương %d.%d: API error - %s",
                        book.getTitle(), chapterNumber, partIndex + 1, curlOutput
                    ));

                    System.out.println("❌ API ERROR: " + curlOutput);
                    success = false;
                }
            } else {
                // cURL thất bại
                summary.addError(String.format(
                    "❌ %s - Chương %d.%d: cURL failed (exit %d) - %s",
                    book.getTitle(), chapterNumber, partIndex + 1, exitCode, curlOutput
                ));

                System.out.println("❌ CURL FAILED: " + curlOutput);
                success = false;
            }

            System.out.println("═══════════════════════════════════\n");

        } catch (IOException ex) {
            summary.addError(String.format(
                "❌ %s - Chương %d.%d: IO error - %s",
                book.getTitle(), chapterNumber, partIndex + 1, ex.getMessage()
            ));
            ex.printStackTrace();

        } catch (InterruptedException ex) {
            summary.addError(String.format(
                "❌ %s - Chương %d.%d: Interrupted - %s",
                book.getTitle(), chapterNumber, partIndex + 1, ex.getMessage()
            ));
            Thread.currentThread().interrupt();
            ex.printStackTrace();

        } finally {
            // Dọn dẹp file tạm
            if (tempFile != null && tempFile.exists()) {
                try {
                    tempFile.delete();
                } catch (Exception e) {
                    System.err.println("Không thể xóa file tạm: " + e.getMessage());
                }
            }
        }

        return success;
    }

// ✅ Helper method để extract Post ID từ response
private String extractPostId(String jsonResponse) {
    try {
        int statusIndex = jsonResponse.indexOf("\"status\":\"");
        if (statusIndex > 0) {
            int start = statusIndex + 10;  // Length of "status":"
            int end = jsonResponse.indexOf("\"", start);
            if (end > start) {
                return jsonResponse.substring(start, end);
            }
        }
        return "unknown";
    } catch (Exception e) {
        return "unknown";
    }
}
    // ⭐ Thêm helper method để extract error message
    private String extractErrorMessage(String jsonResponse) {
        try {
            // Extract status field from JSON
            int statusIndex = jsonResponse.indexOf("\"status\":");
            if (statusIndex > 0) {
                int start = jsonResponse.indexOf("\"", statusIndex + 9) + 1;
                int end = jsonResponse.indexOf("\"", start);
                if (start > 0 && end > start) {
                    String msg = jsonResponse.substring(start, end);
                    // Decode unicode
                    return decodeUnicode(msg);
                }
            }
            return jsonResponse;
        } catch (Exception e) {
            return jsonResponse;
        }
    }

    private String decodeUnicode(String str) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            if (str.charAt(i) == '\\' && i + 1 < str.length() && str.charAt(i + 1) == 'u') {
                String unicode = str.substring(i + 2, Math.min(i + 6, str.length()));
                try {
                    sb.append((char) Integer.parseInt(unicode, 16));
                    i += 6;
                } catch (Exception e) {
                    sb.append(str.charAt(i));
                    i++;
                }
            } else {
                sb.append(str.charAt(i));
                i++;
            }
        }
        return sb.toString();
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
            return price != null ? price.stripTrailingZeros().toPlainString() : "0";
        }
        
        String getPriceString() {
            return price != null ? price.stripTrailingZeros().toPlainString() : "0";
        }

    void setChapterOffset(int offset) {
        if (offset <= 0) {
            throw new IllegalArgumentException("Chapter offset phải lớn hơn 0");
        }
        this.chapterOffset = offset;
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
    private javax.swing.JTextField day1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
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
