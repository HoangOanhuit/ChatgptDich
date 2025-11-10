package com.mycompany.quanlytruyen.view.books;

import com.mycompany.quanlytruyen.model.Book;
import com.mycompany.quanlytruyen.dao.BookDao;
import com.mycompany.quanlytruyen.utils.UIUtils;
import com.mycompany.quanlytruyen.utils.TextFieldKeyboardUtils;
import com.mycompany.quanlytruyen.dao.DataSourceFactory;
import com.mycompany.quanlytruyen.config.AppConfig;
import com.mycompany.quanlytruyen.service.FileService;
import javax.sql.DataSource;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SuaSach extends javax.swing.JDialog {


    private QLSach parentPanel;
    private Book editingBook;
    private FileService fileService;
    private String guidelinesContent;
    private String nameTableContent;
    private static final Logger logger = Logger.getLogger(SuaSach.class.getName());
    
    public SuaSach(java.awt.Frame parent, boolean modal, QLSach parentPanel) {
        initComponents();
        this.parentPanel = parentPanel;
        btnCreate.setText("Sửa");
        btnRefresh1.addActionListener(evt -> btnRefresh1ActionPerformed(evt));
        this.fileService = new FileService();
        setupComboBoxes();
        setupDragAndDrop();
        setupKeyboardShortcuts();
        UIUtils.enableTextComponentShortcuts(this);
        // Populate form with the selected book from parent panel
        if (parentPanel != null && parentPanel.getSelectedBook() != null) {
            try {
                BookDao dao = new BookDao(createDataSource());
                Book book = dao.getBookById(parentPanel.getSelectedBook().getId());
                setBook(book != null ? book : parentPanel.getSelectedBook());
            } catch (Exception ex) {
                setBook(parentPanel.getSelectedBook());
            }
        }        
    }


    public SuaSach(java.awt.Frame parent, boolean modal, QLSach parentPanel, Book book) {
        this(parent, modal, parentPanel);
        setBook(book);
    }
    
    private void setupKeyboardShortcuts() {
        // Setup keyboard shortcuts cho tất cả text fields trong form
        TextFieldKeyboardUtils.setupEnhancedTextComponent(txtName);
        TextFieldKeyboardUtils.setupEnhancedTextComponent(txtAuthor);
        TextFieldKeyboardUtils.setupEnhancedTextComponent(txtYeuCau1);
        TextFieldKeyboardUtils.setupEnhancedTextComponent(txtBangTen);
    }
    private void setBook(Book book) {
        this.editingBook = book;
        if (book != null) {
            txtName.setText(book.getTitle());
            txtAuthor.setText(book.getAuthor());
            if (book.getGuidelines() != null && !book.getGuidelines().isEmpty()) {
                guidelinesContent = book.getGuidelines();
                txtYeuCau1.setText("✓");
            }
            if (book.getNameTable() != null && !book.getNameTable().isEmpty()) {
                nameTableContent = book.getNameTable();
                txtBangTen.setText("✓");
            }
            stRaw.setSelectedItem(book.getRawStatus());
            stTranslate.setSelectedItem(book.getTranslateStatus());
            stPost.setSelectedItem(book.getPostStatus());
        }
    }

    private void setupComboBoxes() {
        stRaw.setModel(new DefaultComboBoxModel<String>(new String[]{
            Book.RawStatus.NOT_FULL.getDisplayName(),
            Book.RawStatus.FULL.getDisplayName()
        }));
        stRaw.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                // Value đã là String nên không cần cast
                return this;
            }
        });

        stTranslate.setModel(new DefaultComboBoxModel<String>(new String[]{
            Book.TranslateStatus.NOT_HOAN.getDisplayName(),
            Book.TranslateStatus.HOAN.getDisplayName()
        }));
        stTranslate.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                // Value đã là String nên không cần cast
                return this;
            }
        });

        stPost.setModel(new DefaultComboBoxModel<String>(new String[]{
            Book.PostStatus.NOT_HOAN.getDisplayName(), 
            Book.PostStatus.HOAN.getDisplayName()
        }));
        stPost.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                // Value đã là String nên không cần cast
                return this;
            }
        });
    }

    // Thêm các method helper để convert từ String sang Enum khi cần:
    private Book.RawStatus getSelectedRawStatus() {
        String selected = (String) stRaw.getSelectedItem();
        for (Book.RawStatus status : Book.RawStatus.values()) {
            if (status.getDisplayName().equals(selected)) {
                return status;
            }
        }
        return Book.RawStatus.NOT_FULL; // default
    }

    private Book.TranslateStatus getSelectedTranslateStatus() {
        String selected = (String) stTranslate.getSelectedItem();
        for (Book.TranslateStatus status : Book.TranslateStatus.values()) {
            if (status.getDisplayName().equals(selected)) {
                return status;
            }
        }
        return Book.TranslateStatus.NOT_HOAN; // default
    }

    private Book.PostStatus getSelectedPostStatus() {
        String selected = (String) stPost.getSelectedItem();
        for (Book.PostStatus status : Book.PostStatus.values()) {
            if (status.getDisplayName().equals(selected)) {
                return status;
            }
        }
        return Book.PostStatus.NOT_HOAN; // default
    }    
    private void setupDragAndDrop() {
        enableFileDrop(txtYeuCau1, this::handleGuidelinesFile);
        enableFileDrop(txtBangTen, this::handleNameTableFile);
    }

    private void enableFileDrop(JTextField field, Consumer<File> handler) {
        field.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        handler.accept(files.get(0));
                        return true;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SuaSach.this, ex.getMessage());
                }
                return false;
            }
        });
    }

    private void handleGuidelinesFile(File file) {
        try {
            guidelinesContent = fileService.readTextFile(file);
            txtYeuCau1.setText(file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void handleNameTableFile(File file) {
        try {
            nameTableContent = fileService.readTextFile(file);
            txtBangTen.setText(file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private DataSource createDataSource() {
        AppConfig config = AppConfig.getInstance();
        return DataSourceFactory.create(config.getDatabaseUrl(), config.getDatabaseUser(), config.getDatabasePassword());
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
        jLabel17 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnCreate = new javax.swing.JButton();
        txtName = new javax.swing.JTextField();
        txtAuthor = new javax.swing.JTextField();
        yeuCauPanel1 = new javax.swing.JPanel();
        btnYeuCau1 = new javax.swing.JButton();
        txtYeuCau1 = new javax.swing.JTextField();
        btnRefresh1 = new javax.swing.JButton();
        bangTenPanel = new javax.swing.JPanel();
        btnBangTen = new javax.swing.JButton();
        txtBangTen = new javax.swing.JTextField();
        btnRefresh2 = new javax.swing.JButton();
        stRaw = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        stTranslate = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        stPost = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtName1 = new javax.swing.JTextField();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        pnProfile.setBackground(new java.awt.Color(255, 255, 255));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setText("Sửa Truyện");

        pnProfile1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel17.setText("Tên Truyện:");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel23.setText("Tác giả:");

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
        btnCreate.setText("Sửa");
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

        txtName.setToolTipText("");
        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });

        txtAuthor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAuthorActionPerformed(evt);
            }
        });

        yeuCauPanel1.setBackground(new java.awt.Color(255, 255, 255));

        btnYeuCau1.setBackground(new java.awt.Color(0, 153, 153));
        btnYeuCau1.setForeground(new java.awt.Color(255, 255, 255));
        btnYeuCau1.setText("Yêu Cầu");
        btnYeuCau1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnYeuCau1ActionPerformed(evt);
            }
        });

        txtYeuCau1.setEditable(false);
        txtYeuCau1.setForeground(new java.awt.Color(153, 153, 153));
        txtYeuCau1.setText("(File Yêu Cầu)");
        txtYeuCau1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtYeuCau1ActionPerformed(evt);
            }
        });

        btnRefresh1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/refresh.png"))); // NOI18N
        btnRefresh1.setText("jButton1");

        javax.swing.GroupLayout yeuCauPanel1Layout = new javax.swing.GroupLayout(yeuCauPanel1);
        yeuCauPanel1.setLayout(yeuCauPanel1Layout);
        yeuCauPanel1Layout.setHorizontalGroup(
            yeuCauPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(yeuCauPanel1Layout.createSequentialGroup()
                .addComponent(txtYeuCau1, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addGap(24, 24, 24)
                .addComponent(btnYeuCau1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefresh1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        yeuCauPanel1Layout.setVerticalGroup(
            yeuCauPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(yeuCauPanel1Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(yeuCauPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnYeuCau1)
                    .addComponent(txtYeuCau1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefresh1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bangTenPanel.setBackground(new java.awt.Color(255, 255, 255));

        btnBangTen.setBackground(new java.awt.Color(0, 102, 102));
        btnBangTen.setForeground(new java.awt.Color(255, 255, 255));
        btnBangTen.setText("Bảng Tên");
        btnBangTen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBangTenActionPerformed(evt);
            }
        });

        txtBangTen.setEditable(false);
        txtBangTen.setForeground(new java.awt.Color(153, 153, 153));
        txtBangTen.setText("(File Bảng Tên)");
        txtBangTen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBangTenActionPerformed(evt);
            }
        });

        btnRefresh2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/refresh.png"))); // NOI18N
        btnRefresh2.setText("jButton1");
        btnRefresh2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefresh2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bangTenPanelLayout = new javax.swing.GroupLayout(bangTenPanel);
        bangTenPanel.setLayout(bangTenPanelLayout);
        bangTenPanelLayout.setHorizontalGroup(
            bangTenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bangTenPanelLayout.createSequentialGroup()
                .addComponent(txtBangTen)
                .addGap(24, 24, 24)
                .addComponent(btnBangTen, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefresh2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        bangTenPanelLayout.setVerticalGroup(
            bangTenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bangTenPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bangTenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtBangTen, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(bangTenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnBangTen)
                        .addComponent(btnRefresh2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        stRaw.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        stRaw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stRawActionPerformed(evt);
            }
        });

        jLabel3.setForeground(new java.awt.Color(51, 51, 51));
        jLabel3.setText("Tình trạng Raw");

        stTranslate.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        stTranslate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stTranslateActionPerformed(evt);
            }
        });

        jLabel4.setForeground(new java.awt.Color(51, 51, 51));
        jLabel4.setText("Tình trạng Dịch");

        stPost.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        stPost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stPostActionPerformed(evt);
            }
        });

        jLabel5.setForeground(new java.awt.Color(51, 51, 51));
        jLabel5.setText("Tình trạng Đăng");

        jLabel6.setForeground(new java.awt.Color(51, 51, 51));
        jLabel6.setText("Giá mỗi chương");

        txtName1.setToolTipText("");
        txtName1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtName1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnProfile1Layout = new javax.swing.GroupLayout(pnProfile1);
        pnProfile1.setLayout(pnProfile1Layout);
        pnProfile1Layout.setHorizontalGroup(
            pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnProfile1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(pnProfile1Layout.createSequentialGroup()
                            .addComponent(btnCreate, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(126, 126, 126)
                            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnProfile1Layout.createSequentialGroup()
                            .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel17)
                                .addComponent(jLabel23))
                            .addGap(18, 18, 18)
                            .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtAuthor, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                                .addComponent(txtName))))
                    .addGroup(pnProfile1Layout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bangTenPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(yeuCauPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnProfile1Layout.createSequentialGroup()
                .addGap(263, 263, 263)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(stPost, 0, 173, Short.MAX_VALUE)
                        .addComponent(stTranslate, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(stRaw, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(txtName1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnProfile1Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel5)
                        .addGap(81, 81, 81))
                    .addGroup(pnProfile1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel6))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        pnProfile1Layout.setVerticalGroup(
            pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnProfile1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(txtAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(yeuCauPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bangTenPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6)
                    .addComponent(txtName1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stRaw, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(12, 12, 12)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stTranslate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(12, 12, 12)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stPost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreate, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26))
        );

        javax.swing.GroupLayout pnProfileLayout = new javax.swing.GroupLayout(pnProfile);
        pnProfile.setLayout(pnProfileLayout);
        pnProfileLayout.setHorizontalGroup(
            pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnProfileLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addGap(291, 291, 291))
            .addGroup(pnProfileLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnProfile1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnProfileLayout.setVerticalGroup(
            pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnProfileLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void txtAuthorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAuthorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAuthorActionPerformed

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNameActionPerformed

    private void btnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
        if (editingBook == null) {
            dispose();
            return;
        }
        try {
            editingBook.setTitle(txtName.getText().trim());
            editingBook.setAuthor(txtAuthor.getText().trim());
            editingBook.setGuidelines(guidelinesContent);
            editingBook.setNameTable(nameTableContent);
            editingBook.setRawStatus((Book.RawStatus) stRaw.getSelectedItem());
            editingBook.setTranslateStatus((Book.TranslateStatus) stTranslate.getSelectedItem());
            editingBook.setPostStatus((Book.PostStatus) stPost.getSelectedItem());

            BookDao dao = new BookDao(createDataSource());
            dao.updateBook(editingBook);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật sách: " + e.getMessage());
        }      
    }//GEN-LAST:event_btnCreateActionPerformed

    private void btnCreateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCreateMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCreateMouseClicked

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed

    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_btnCancelMouseClicked

    private void btnYeuCau1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnYeuCau1ActionPerformed
        File file = fileService.selectTextFile(this, "Chọn file yêu cầu");
        if (file != null) {
            handleGuidelinesFile(file);
        }       
    }//GEN-LAST:event_btnYeuCau1ActionPerformed

    private void txtYeuCau1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtYeuCau1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtYeuCau1ActionPerformed

    private void btnBangTenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBangTenActionPerformed
        File file = fileService.selectTextFile(this, "Chọn file bảng tên");
        if (file != null) {
            handleNameTableFile(file);
        }       
    }//GEN-LAST:event_btnBangTenActionPerformed

    private void txtBangTenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBangTenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBangTenActionPerformed

    private void btnRefresh1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefresh1ActionPerformed
        guidelinesContent = null;
        txtYeuCau1.setText("(File Yêu Cầu)");
    }//GEN-LAST:event_btnRefresh1ActionPerformed

    private void btnRefresh2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefresh2ActionPerformed
        nameTableContent = null;
        txtBangTen.setText("(File Bảng Tên)");
    }//GEN-LAST:event_btnRefresh2ActionPerformed

    private void stRawActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stRawActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stRawActionPerformed

    private void stTranslateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stTranslateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stTranslateActionPerformed

    private void stPostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stPostActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stPostActionPerformed

    private void txtName1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtName1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtName1ActionPerformed

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


    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bangTenPanel;
    private javax.swing.JButton btnBangTen;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCreate;
    private javax.swing.JButton btnRefresh1;
    private javax.swing.JButton btnRefresh2;
    private javax.swing.JButton btnYeuCau1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel pnProfile;
    private javax.swing.JPanel pnProfile1;
    private javax.swing.JComboBox<String> stPost;
    private javax.swing.JComboBox<String> stRaw;
    private javax.swing.JComboBox<String> stTranslate;
    private javax.swing.JTextField txtAuthor;
    private javax.swing.JTextField txtBangTen;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtName1;
    private javax.swing.JTextField txtYeuCau1;
    private javax.swing.JPanel yeuCauPanel1;
    // End of variables declaration//GEN-END:variables

}
