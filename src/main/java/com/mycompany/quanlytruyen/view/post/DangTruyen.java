package com.mycompany.quanlytruyen.view.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.quanlytruyen.config.AppConfig;
import com.mycompany.quanlytruyen.dao.AccountDAO;
import com.mycompany.quanlytruyen.dao.BookDao;
import com.mycompany.quanlytruyen.dao.ChapterDao;
import com.mycompany.quanlytruyen.dao.DataSourceFactory;
import com.mycompany.quanlytruyen.model.Account;
import com.mycompany.quanlytruyen.model.Book;
import com.mycompany.quanlytruyen.model.Chapter;

import javax.sql.DataSource;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DangTruyen extends javax.swing.JPanel {

    

    private static final String ALL_ACCOUNTS_OPTION = "Tất cả tài khoản";
    private static final String API_URL = "https://s1apihd.com/wp-json/v1/app/user/themchuong";
    private static final String USER_AGENT = "TruyenHD/2.3 (com.vnvnads.TruyenHD; build:32; iOS 18.3.1) Alamofire/5.9.0";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final String[] SMART_TIMES_WEEKDAY = {
        "06:30", "07:15", "11:45", "12:30", "13:00",
        "17:30", "19:00", "20:30", "22:00", "22:45"
    };
    private static final String[] SMART_TIMES_WEEKEND = {
        "09:00", "10:30", "12:00", "13:30", "15:00",
        "16:30", "18:00", "19:30", "21:00", "22:30"
    };
    private static final int DEFAULT_CHAPTER_COUNT = 5;

    private DataSource dataSource;
    private AccountDAO accountDao;
    private BookDao bookDao;
    private ChapterDao chapterDao;
    private final Map<Long, Account> accountsById = new HashMap<>();
    private final Map<String, Long> accountFilterMap = new HashMap<>();
    private final List<BookRow> allRows = new ArrayList<>();
    private final BookTableModel tableModel = new BookTableModel();
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private PostingWorker currentWorker;
    private String cachedManualHour;
    private String cachedManualMinute;

    public DangTruyen() {
        initComponents();

        initializePanel();

    }

    private void initializePanel() {
        configureTable();
        applyDefaultSchedule();
        setupDocumentListeners();
        configureDataAccess();
        loadAccounts();
        loadBooks();
        updateSmartHourState();
    }

    private void configureDataAccess() {
        try {
            AppConfig config = AppConfig.getInstance();
            dataSource = DataSourceFactory.create(
                config.getDatabaseUrl(),
                config.getDatabaseUser(),
                config.getDatabasePassword()
            );
            accountDao = new AccountDAO(dataSource);
            bookDao = new BookDao(dataSource);
            chapterDao = new ChapterDao(dataSource);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối cơ sở dữ liệu: " + ex.getMessage());
        }
    }

    private void configureTable() {
        Tbooks.setModel(tableModel);
        Tbooks.setAutoCreateRowSorter(true);
        Tbooks.setRowHeight(24);
    }

    private void setupDocumentListeners() {
        addDocumentListener(num_chapter, this::updateChapterRangeFromInput);
        addDocumentListener(num_chapter4, () -> {
            if (!smartHour.isSelected()) {
                cachedManualHour = padTwoDigits(num_chapter4.getText());
            }
        });
        addDocumentListener(num_chapter5, () -> {
            if (!smartHour.isSelected()) {
                cachedManualMinute = padTwoDigits(num_chapter5.getText());
            }
        });
    }

    private void addDocumentListener(JTextField field, Runnable callback) {
        if (field == null || callback == null) {
            return;
        }
        field.getDocument().addDocumentListener(new SimpleDocumentListener(callback));
    }

    private void applyDefaultSchedule() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        num_chapter1.setText(String.format("%02d", tomorrow.getDayOfMonth()));
        num_chapter2.setText(String.format("%02d", tomorrow.getMonthValue()));
        num_chapter3.setText(String.valueOf(tomorrow.getYear()));

        LocalTime plusTen = LocalTime.now().plusMinutes(10);
        cachedManualHour = String.format("%02d", plusTen.getHour());
        cachedManualMinute = String.format("%02d", plusTen.getMinute());
        num_chapter4.setText(cachedManualHour);
        num_chapter5.setText(cachedManualMinute);

        num_chapter6.setText("02");
        num_chapter7.setText("00");
    }

    private void loadAccounts() {
        if (accountDao == null) {
            return;
        }
        accountFilterMap.clear();
        accountsById.clear();
        tenTruyen1.removeAllItems();
        tenTruyen1.addItem(ALL_ACCOUNTS_OPTION);
        try {
            for (Account account : accountDao.getAllAccounts()) {
                if (account.getId() == null) {
                    continue;
                }
                accountsById.put(account.getId(), account);
                String display = formatAccountDisplay(account);
                accountFilterMap.put(display, account.getId());
                tenTruyen1.addItem(display);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải tài khoản: " + ex.getMessage());
        }
    }

    private void loadBooks() {
        allRows.clear();
        if (bookDao == null) {
            return;
        }
        try {
            List<Book> books = bookDao.getAllBooks();
            int chapterCount = getSafeChapterCount();
            for (Book book : books) {
                if (book.getPostStatus() != null && book.getPostStatus() != Book.PostStatus.NOT_HOAN) {
                    continue;
                }
                Account account = null;
                if (book.getAccountId() != null) {
                    account = accountsById.get(book.getAccountId());
                }
                BookRow row = new BookRow(book, account);
                row.setChapterCount(chapterCount);
                allRows.add(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải truyện: " + ex.getMessage());
        }
        applyAccountFilter();
    }

    private void applyAccountFilter() {
        String selected = (String) tenTruyen1.getSelectedItem();
        Long accountId = accountFilterMap.get(selected);
        List<BookRow> filtered = allRows.stream()
            .filter(row -> accountId == null || Objects.equals(row.getAccountId(), accountId))
            .collect(Collectors.toList());
        tableModel.setRows(filtered);
        tableModel.updateChapterCount(getSafeChapterCount());
        jCheckBox1.setSelected(false);
        Tbooks.revalidate();
        Tbooks.repaint();
    }

    private void updateChapterRangeFromInput() {
        tableModel.updateChapterCount(getSafeChapterCount());
        Tbooks.repaint();
    }

    private int getSafeChapterCount() {
        String text = num_chapter.getText();
        if (text == null || text.isBlank()) {
            return DEFAULT_CHAPTER_COUNT;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return value > 0 ? value : DEFAULT_CHAPTER_COUNT;
        } catch (NumberFormatException ex) {
            return DEFAULT_CHAPTER_COUNT;
        }
    }

    private void resetFilters() {
        tenTruyen1.setSelectedItem(ALL_ACCOUNTS_OPTION);
        jCheckBox1.setSelected(false);
        applyDefaultSchedule();
        updateSmartHourState();
        loadBooks();
    }

    private void clearAccountFilter() {
        tenTruyen1.setSelectedItem(ALL_ACCOUNTS_OPTION);
        applyAccountFilter();
    }

    private void updateSmartHourState() {
        boolean smart = smartHour.isSelected();
        if (smart) {
            cachedManualHour = padTwoDigits(num_chapter4.getText());
            cachedManualMinute = padTwoDigits(num_chapter5.getText());
            num_chapter4.setText("00");
            num_chapter5.setText("00");
        } else {
            if (cachedManualHour == null || cachedManualHour.isBlank()) {
                cachedManualHour = padTwoDigits(num_chapter4.getText());
            }
            if (cachedManualMinute == null || cachedManualMinute.isBlank()) {
                cachedManualMinute = padTwoDigits(num_chapter5.getText());
            }
            num_chapter4.setText(padTwoDigits(cachedManualHour));
            num_chapter5.setText(padTwoDigits(cachedManualMinute));
        }
        num_chapter4.setEnabled(!smart);
        num_chapter5.setEnabled(!smart);
    }

    private String padTwoDigits(String value) {
        if (value == null || value.isBlank()) {
            return "00";
        }
        String trimmed = value.trim();
        if (trimmed.length() == 1) {
            return "0" + trimmed;
        }
        if (trimmed.length() >= 2) {
            return trimmed.substring(trimmed.length() - 2);
        }
        return "00";
    }

    private String formatAccountDisplay(Account account) {
        return account.getUsername() + " (#" + account.getId() + ")";
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
        jPanel1 = new javax.swing.JPanel();
        FilterPanel2 = new javax.swing.JPanel();
        tenTruyen1 = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        btnFilter4 = new javax.swing.JButton();
        btnFilter5 = new javax.swing.JButton();
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(FilterPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(30, 30, 30))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(FilterPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(454, Short.MAX_VALUE))
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 557, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnFilter2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
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
    resetFilters();
    }//GEN-LAST:event_btnResetActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnFilter2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter2ActionPerformed
        // TODO add your handling code here:
        startPostingWorkflow();
    }//GEN-LAST:event_btnFilter2ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
        tableModel.setAllSelected(jCheckBox1.isSelected());
        Tbooks.repaint();
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void tenTruyen1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tenTruyen1ActionPerformed
        // TODO add your handling code here:
        applyAccountFilter();
    }//GEN-LAST:event_tenTruyen1ActionPerformed

    private void btnFilter4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter4ActionPerformed
        // TODO add your handling code here:
        applyAccountFilter();
    }//GEN-LAST:event_btnFilter4ActionPerformed

    private void btnFilter5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilter5ActionPerformed
        // TODO add your handling code here:
        clearAccountFilter();
    }//GEN-LAST:event_btnFilter5ActionPerformed

    private void startPostingWorkflow() {
        if (currentWorker != null && !currentWorker.isDone()) {
            JOptionPane.showMessageDialog(this, "Đang có tiến trình đăng truyện, vui lòng đợi hoàn tất trước khi thực hiện thao tác mới.");
            return;
        }

        List<BookRow> selectedRows = tableModel.getSelectedRows();
        if (selectedRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một truyện trong danh sách để đăng.");
            return;
        }

        Integer chapterCount = parseIntegerField(num_chapter, "Chương/truyện", 1, null);
        if (chapterCount == null) {
            return;
        }

        ScheduleParameters scheduleParameters = readScheduleParameters(smartHour.isSelected());
        if (scheduleParameters == null) {
            return;
        }

        if (chapterDao == null || dataSource == null) {
            JOptionPane.showMessageDialog(this, "Không thể đăng truyện vì kết nối cơ sở dữ liệu chưa sẵn sàng.");
            return;
        }

        btnFilter2.setEnabled(false);
        currentWorker = new PostingWorker(new ArrayList<>(selectedRows), chapterCount, scheduleParameters);
        currentWorker.execute();
    }

    private Integer parseIntegerField(JTextField field, String fieldName, int minValue, Integer maxValue) {
        if (field == null) {
            return null;
        }
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, fieldName + " không được để trống.");
            return null;
        }
        try {
            int value = Integer.parseInt(text.trim());
            if (value < minValue) {
                JOptionPane.showMessageDialog(this, fieldName + " phải lớn hơn hoặc bằng " + minValue + ".");
                return null;
            }
            if (maxValue != null && value > maxValue) {
                JOptionPane.showMessageDialog(this, fieldName + " phải nhỏ hơn hoặc bằng " + maxValue + ".");
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, fieldName + " phải là số hợp lệ.");
            return null;
        }
    }

    private ScheduleParameters readScheduleParameters(boolean smart) {
        Integer day = parseIntegerField(num_chapter1, "Ngày bắt đầu", 1, 31);
        Integer month = parseIntegerField(num_chapter2, "Tháng bắt đầu", 1, 12);
        Integer year = parseIntegerField(num_chapter3, "Năm bắt đầu", 1900, 3000);
        if (day == null || month == null || year == null) {
            return null;
        }

        LocalDate date;
        try {
            date = LocalDate.of(year, month, day);
        } catch (DateTimeException ex) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu không hợp lệ: " + ex.getMessage());
            return null;
        }

        Duration gap = parseGapDuration();
        if (gap == null) {
            return null;
        }

        LocalTime startTime = null;
        if (!smart) {
            Integer hour = parseIntegerField(num_chapter4, "Giờ bắt đầu", 0, 23);
            Integer minute = parseIntegerField(num_chapter5, "Phút bắt đầu", 0, 59);
            if (hour == null || minute == null) {
                return null;
            }
            startTime = LocalTime.of(hour, minute);
        }

        return new ScheduleParameters(date, startTime, gap, smart);
    }

    private Duration parseGapDuration() {
        Integer hours = parseIntegerField(num_chapter6, "Giờ cách nhau", 0, 72);
        if (hours == null) {
            return null;
        }
        Integer minutes = parseIntegerField(num_chapter7, "Phút cách nhau", 0, 59);
        if (minutes == null) {
            return null;
        }
        return Duration.ofHours(hours).plusMinutes(minutes);
    }

    private List<LocalTime> getSmartSlots(DayOfWeek dayOfWeek) {
        String[] raw = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)
            ? SMART_TIMES_WEEKEND : SMART_TIMES_WEEKDAY;
        List<LocalTime> slots = new ArrayList<>(raw.length);
        for (String value : raw) {
            slots.add(LocalTime.parse(value));
        }
        return slots;
    }

    private BookPostingResult postBook(BookRow row, int chaptersToPost, ScheduleParameters scheduleParameters) {
        BookPostingResult result = new BookPostingResult(row);
        Book book = row.getBook();
        Account account = row.getAccount();

        if (account == null) {
            result.addMessage("Truyện chưa được gán tài khoản.");
            return result;
        }
        if (book.getId() == null) {
            result.addMessage("Thiếu ID truyện.");
            return result;
        }
        if (account.getId() == null) {
            result.addMessage("Tài khoản " + account.getUsername() + " chưa có ID.");
            return result;
        }
        if (isBlank(account.getActivationKey()) || isBlank(account.getAuth())
            || isBlank(account.getEmail()) || isBlank(account.getUuid())) {
            result.addMessage("Tài khoản thiếu thông tin đăng truyện (activation_key/auth/email/uuid).");
            return result;
        }
        if (chaptersToPost <= 0) {
            result.addMessage("Số chương cần đăng phải lớn hơn 0.");
            return result;
        }

        int totalPosts = chaptersToPost * 2;
        List<LocalDateTime> schedule;
        try {
            schedule = scheduleParameters.buildSchedule(totalPosts);
        } catch (IllegalArgumentException ex) {
            result.addMessage(ex.getMessage());
            return result;
        }
        if (schedule.size() < totalPosts) {
            result.addMessage("Không thể tạo đủ lịch đăng cho " + totalPosts + " phần.");
            return result;
        }

        List<Chapter> readyChapters;
        try {
            readyChapters = chapterDao.findChaptersNeedPost(book.getId());
        } catch (SQLException ex) {
            result.addMessage("Không thể lấy danh sách chương cần đăng: " + ex.getMessage());
            return result;
        }

        int fromChapter = row.getFromChapter();
        List<Chapter> usableChapters = readyChapters.stream()
            .filter(ch -> ch.getChapterNumber() != null && ch.getChapterNumber() >= fromChapter)
            .sorted((c1, c2) -> Integer.compare(c1.getChapterNumber(), c2.getChapterNumber()))
            .collect(Collectors.toList());

        if (usableChapters.size() < chaptersToPost) {
            result.addMessage("Không đủ chương đã beta để đăng (cần " + chaptersToPost + ", có " + usableChapters.size() + ").");
            return result;
        }

        int scheduleIndex = 0;
        for (int i = 0; i < chaptersToPost; i++) {
            Chapter chapter = usableChapters.get(i);
            String normalizedContent = normalizeParagraphs(chapter.getContent());
            String[] parts = splitContent(normalizedContent, 2);

            for (int partIndex = 0; partIndex < 2; partIndex++) {
                String partContent = parts.length > partIndex ? parts[partIndex] : "";
                if (partContent == null) {
                    partContent = "";
                }
                partContent = partContent.trim();
                if (partContent.isEmpty()) {
                    result.addMessage("Chương " + chapter.getChapterNumber() + " phần " + (partIndex + 1) + " không có nội dung.");
                    result.incrementFailure();
                    continue;
                }

                LocalDateTime scheduleTime = schedule.get(scheduleIndex++);
                String title = buildChapterTitle(chapter, partIndex == 0);
                String auth2 = buildAuth2(book, chapter, partIndex == 0);
                Map<String, Object> payload = buildPayload(account, book, chapter, partContent, title, auth2, scheduleTime);
                SendResult sendResult = sendPostRequest(payload);
                if (sendResult.success()) {
                    result.incrementSuccess();
                    result.addMessage("Đăng " + title + " thành công.");
                } else {
                    result.incrementFailure();
                    result.addMessage("Đăng " + title + " thất bại: " + sendResult.message());
                }
            }
        }

        if (result.getFailureCount() == 0 && result.getSuccessCount() == totalPosts) {
            int newPosted = usableChapters.get(chaptersToPost - 1).getChapterNumber();
            try {
                chapterDao.markChaptersAsPostedUpTo(book.getId(), newPosted);
                updateBookPostedValue(book.getId(), newPosted);
                row.updatePosted(newPosted);
                result.setNewPosted(newPosted);
                result.setSuccess(true);
                result.addMessage("Đã cập nhật số chương đã đăng tới " + newPosted + ".");
            } catch (SQLException ex) {
                result.addMessage("Đăng thành công nhưng không thể cập nhật trạng thái trong cơ sở dữ liệu: " + ex.getMessage());
            }
        } else {
            result.addMessage("Có " + result.getFailureCount() + " phần đăng thất bại, không cập nhật tiến độ.");
        }

        return result;
    }

    private Map<String, Object> buildPayload(Account account, Book book, Chapter chapter, String content,
                                             String title, String auth2, LocalDateTime scheduleTime) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("price", formatPriceValue(book.getPrice()));
        payload.put("registered", normalizeTimestamp(account.getRegistered()));
        payload.put("title", title);
        payload.put("auth2", auth2);
        payload.put("user_id", String.valueOf(account.getId()));
        payload.put("activation_key", account.getActivationKey());
        payload.put("versionIOS", isBlank(account.getVersionIos()) ? "1" : account.getVersionIos());
        payload.put("auth", account.getAuth());
        payload.put("date_schedule", scheduleTime.format(DATE_TIME_FORMATTER));
        payload.put("suggest_password", "");
        payload.put("id", String.valueOf(book.getId()));
        payload.put("content", content);
        payload.put("email", account.getEmail());
        payload.put("value_password", "");
        payload.put("uuid", account.getUuid());
        payload.put("status", "future");
        return payload;
    }

    private String buildChapterTitle(Chapter chapter, boolean firstPart) {
        int chapterNumber = chapter.getChapterNumber() != null ? chapter.getChapterNumber() : 0;
        String chapterTitle = chapter.getChapterTitle() != null ? chapter.getChapterTitle() : "";
        return "Chương " + chapterNumber + (firstPart ? ".1:" : ".2:") + chapterTitle;
    }

    private String buildAuth2(Book book, Chapter chapter, boolean firstPart) {
        long bookId = book.getId() != null ? book.getId() : 0L;
        int chapterNumber = chapter.getChapterNumber() != null ? chapter.getChapterNumber() : 0;
        int order = firstPart ? (chapterNumber * 2 - 1) : (chapterNumber * 2);
        return "TruyenHD" + bookId + order + "themchuong";
    }

    private String normalizeTimestamp(String value) {
        if (isBlank(value)) {
            return LocalDateTime.now().format(DATE_TIME_FORMATTER);
        }
        String normalized = value.trim().replace('T', ' ');
        try {
            LocalDateTime dt = LocalDateTime.parse(normalized.replace(' ', 'T'));
            return dt.format(DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            return normalized;
        }
    }

    private String formatPriceValue(BigDecimal price) {
        if (price == null) {
            return "0";
        }
        return price.stripTrailingZeros().toPlainString();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private SendResult sendPostRequest(Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .header("Host", "s1apihd.com")
                .header("accept", "*/*")
                .header("content-type", "application/json")
                .header("user-agent", USER_AGENT)
                .header("accept-language", "vi-VN;q=1.0, en-VN;q=0.9")
                .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    return new SendResult(false, "HTTP " + response.code() + ": " + responseBody);
                }
                return new SendResult(true, responseBody);
            }
        } catch (IOException ex) {
            return new SendResult(false, ex.getMessage());
        }
    }

    private void updateBookPostedValue(long bookId, int posted) throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Datasource chưa sẵn sàng");
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE books SET posted = ? WHERE id = ?")) {
            ps.setInt(1, posted);
            ps.setLong(2, bookId);
            ps.executeUpdate();
        }
    }

    private void applyPostingResults(List<BookPostingResult> results) {
        if (results == null || results.isEmpty()) {
            btnFilter2.setEnabled(true);
            return;
        }

        StringBuilder message = new StringBuilder();
        for (BookPostingResult result : results) {
            if (message.length() > 0) {
                message.append("\n\n");
            }
            message.append(result.getBookTitle()).append(": ")
                .append(result.isSuccess() ? "Thành công" : "Thất bại");
            for (String detail : result.getMessages()) {
                message.append("\n - ").append(detail);
            }
            if (result.isSuccess()) {
                result.getRow().setSelected(false);
            }
        }

        tableModel.fireTableDataChanged();
        jCheckBox1.setSelected(false);
        updateChapterRangeFromInput();
        JOptionPane.showMessageDialog(this, message.toString());
    }

    private String normalizeParagraphs(String content) {
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

    private String[] splitContent(String content, int n) {
        if (n <= 0) {
            return new String[]{""};
        }
        if (content == null) {
            String[] empty = new String[n];
            for (int i = 0; i < n; i++) {
                empty[i] = "";
            }
            return empty;
        }
        String[] result = new String[n];
        int len = content.length();
        int start = 0;
        for (int i = 0; i < n; i++) {
            if (i == n - 1) {
                result[i] = content.substring(start);
            } else {
                int approxEnd = start + (len - start) / (n - i);
                int idx = content.lastIndexOf('\n', approxEnd);
                if (idx < start) {
                    idx = approxEnd;
                }
                int end = idx;
                if (idx < len && content.charAt(idx) == '\n') {
                    end = idx + 1;
                }
                result[i] = content.substring(start, Math.min(end, len));
                start = Math.min(end, len);
                while (start < len && (content.charAt(start) == '\n' || content.charAt(start) == '\r')) {
                    start++;
                }
            }
        }
        return result;
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

    private static class BookRow {
        private final Book book;
        private final Account account;
        private boolean selected;
        private int chapterCount;

        private BookRow(Book book, Account account) {
            this.book = book;
            this.account = account;
        }

        private Book getBook() {
            return book;
        }

        private Account getAccount() {
            return account;
        }

        private Long getAccountId() {
            return book.getAccountId();
        }

        private boolean isSelected() {
            return selected;
        }

        private void setSelected(boolean selected) {
            this.selected = selected;
        }

        private int getFromChapter() {
            Integer posted = book.getPosted();
            return (posted != null ? posted : 0) + 1;
        }

        private int getToChapter() {
            int from = getFromChapter();
            return chapterCount > 0 ? from + chapterCount - 1 : from;
        }

        private void setChapterCount(int chapterCount) {
            this.chapterCount = Math.max(0, chapterCount);
        }

        private String getAccountDisplay() {
            if (account == null || account.getUsername() == null) {
                return "(Chưa gán)";
            }
            return account.getUsername();
        }

        private String getBookTitle() {
            return book.getShortTitle() != null ? book.getShortTitle() : "";
        }

        private String getPriceDisplay() {
            BigDecimal price = book.getPrice();
            return price != null ? price.stripTrailingZeros().toPlainString() : "0";
        }

        private void updatePosted(int newPosted) {
            book.setPosted(newPosted);
        }
    }

    private class BookTableModel extends AbstractTableModel {
        private final String[] columns = {"Chọn", "Tài khoản", "Tên Truyện", "Giá/chương", "Từ Chương", "Đến Chương"};
        private final List<BookRow> rows = new ArrayList<>();

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
            return columnIndex == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            BookRow row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.isSelected();
                case 1 -> row.getAccountDisplay();
                case 2 -> row.getBookTitle();
                case 3 -> row.getPriceDisplay();
                case 4 -> String.valueOf(row.getFromChapter());
                case 5 -> String.valueOf(row.getToChapter());
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0 && rowIndex >= 0 && rowIndex < rows.size()) {
                rows.get(rowIndex).setSelected(Boolean.TRUE.equals(aValue));
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        private void setRows(List<BookRow> newRows) {
            rows.clear();
            if (newRows != null) {
                rows.addAll(newRows);
            }
            fireTableDataChanged();
        }

        private void updateChapterCount(int chapterCount) {
            for (BookRow row : rows) {
                row.setChapterCount(chapterCount);
            }
            if (!rows.isEmpty()) {
                fireTableRowsUpdated(0, rows.size() - 1);
            }
        }

        private void setAllSelected(boolean selected) {
            for (BookRow row : rows) {
                row.setSelected(selected);
            }
            if (!rows.isEmpty()) {
                fireTableRowsUpdated(0, rows.size() - 1);
            }
        }

        private List<BookRow> getSelectedRows() {
            return rows.stream()
                .filter(BookRow::isSelected)
                .collect(Collectors.toList());
        }
    }

    private class PostingWorker extends SwingWorker<List<BookPostingResult>, Void> {
        private final List<BookRow> rows;
        private final int chaptersToPost;
        private final ScheduleParameters scheduleParameters;

        private PostingWorker(List<BookRow> rows, int chaptersToPost, ScheduleParameters scheduleParameters) {
            this.rows = rows;
            this.chaptersToPost = chaptersToPost;
            this.scheduleParameters = scheduleParameters;
        }

        @Override
        protected List<BookPostingResult> doInBackground() {
            List<BookPostingResult> results = new ArrayList<>();
            for (BookRow row : rows) {
                results.add(postBook(row, chaptersToPost, scheduleParameters));
            }
            return results;
        }

        @Override
        protected void done() {
            btnFilter2.setEnabled(true);
            currentWorker = null;
            try {
                applyPostingResults(get());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(DangTruyen.this, "Đăng truyện thất bại: " + ex.getMessage());
            }
        }
    }

    private class ScheduleParameters {
        private final LocalDate startDate;
        private final LocalTime startTime;
        private final Duration gap;
        private final boolean smart;

        private ScheduleParameters(LocalDate startDate, LocalTime startTime, Duration gap, boolean smart) {
            this.startDate = startDate;
            this.startTime = startTime;
            this.gap = gap;
            this.smart = smart;
        }

        private List<LocalDateTime> buildSchedule(int totalPosts) {
            if (totalPosts <= 0) {
                return Collections.emptyList();
            }
            if (smart) {
                List<LocalTime> slots = getSmartSlots(startDate.getDayOfWeek());
                if (totalPosts > slots.size()) {
                    throw new IllegalArgumentException("Không đủ khung giờ thông minh cho " + totalPosts + " lần đăng.");
                }
                List<LocalDateTime> times = new ArrayList<>(totalPosts);
                for (int i = 0; i < totalPosts; i++) {
                    times.add(LocalDateTime.of(startDate, slots.get(i)));
                }
                return times;
            }

            LocalDateTime current = LocalDateTime.of(startDate, startTime != null ? startTime : LocalTime.MIDNIGHT);
            Duration step = gap != null ? gap : Duration.ZERO;
            List<LocalDateTime> times = new ArrayList<>(totalPosts);
            for (int i = 0; i < totalPosts; i++) {
                times.add(current);
                current = current.plus(step);
            }
            return times;
        }
    }

    private static class BookPostingResult {
        private final BookRow row;
        private final List<String> messages = new ArrayList<>();
        private boolean success;
        private Integer newPosted;
        private int successCount;
        private int failureCount;

        private BookPostingResult(BookRow row) {
            this.row = row;
        }

        private void addMessage(String message) {
            messages.add(message);
        }

        private void incrementSuccess() {
            successCount++;
        }

        private void incrementFailure() {
            failureCount++;
        }

        private List<String> getMessages() {
            return messages;
        }

        private boolean isSuccess() {
            return success;
        }

        private void setSuccess(boolean success) {
            this.success = success;
        }

        private String getBookTitle() {
            return row.getBookTitle();
        }

        private BookRow getRow() {
            return row;
        }

        private void setNewPosted(Integer newPosted) {
            this.newPosted = newPosted;
        }

        private Integer getNewPosted() {
            return newPosted;
        }

        private int getSuccessCount() {
            return successCount;
        }

        private int getFailureCount() {
            return failureCount;
        }
    }

    private record SendResult(boolean success, String message) { }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel FilterPanel2;
    private javax.swing.JTable Tbooks;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnFilter2;
    private javax.swing.JButton btnFilter4;
    private javax.swing.JButton btnFilter5;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<String> tenTruyen1;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
