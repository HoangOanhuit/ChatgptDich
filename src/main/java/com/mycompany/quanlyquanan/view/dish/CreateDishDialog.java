/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/OkCancelDialog.java to edit this template
 */
package com.mycompany.quanlyquanan.view.dish;


import com.mycompany.quanlyquanan.controller.CategoryController;
import com.mycompany.quanlyquanan.controller.DishController;
import com.mycompany.quanlyquanan.model.Category;
import com.mycompany.quanlyquanan.model.Dish;
import com.mycompany.quanlyquanan.utils.ComponentStyleUtil;
import com.mycompany.quanlyquanan.utils.DishValidationUtils;
import com.mycompany.quanlyquanan.utils.ImageUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author Admin
 */
public class CreateDishDialog extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CreateDishDialog.class.getName());

    public static final int RET_CANCEL = 0;
    public static final int RET_OK = 1;

    private QuanLyMonAnPanel parentPanel;
    // Controllers
    private DishController dishController;
    private CategoryController categoryController;
    // Data
    private List<Category> categories;
    private File selectedImageFile;
    private String savedImagePath;
    
    // Return status
    private int returnStatus = RET_CANCEL;
    
    private boolean dataSaved = false;
    
    

    // UI Components
    private ButtonGroup statusGroup;

   //contructor for panel

    public CreateDishDialog(java.awt.Frame parent, boolean modal, QuanLyMonAnPanel parentPanel) {
        super(parent, modal);
        initComponents();
        initControllers();
        this.parentPanel = parentPanel;
        loadCategories();
        setupEventHandlers();
        setupEscapeKey();
        setLocationRelativeTo(parent);

        // Close the dialog when Esc is pressed
        String cancelName = "cancel";
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put(cancelName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doClose(RET_CANCEL);
            }
        });

        ComponentStyleUtil.styleSecondButton(btnCancel);
        ComponentStyleUtil.styleMainButton(btnCreate);
    }
    
    public CreateDishDialog(Dialog parent, boolean modal) {
        super(parent, modal);
        initControllers();
        initComponents();
        loadCategories();
        setupEventHandlers();
        setupEscapeKey();
        setLocationRelativeTo(parent);
    }
    
    public CreateDishDialog() {
        initControllers();
        initComponents();
        loadCategories();
        setupEventHandlers();
        setupEscapeKey();
    }
    
    private void initControllers() {
        dishController = new DishController();
        categoryController = new CategoryController();
    }
    
        /**
     * Setup các component để có thể nhập liệu
     */
    private void setupComponents() {
        // Đảm bảo tất cả các trường đều có thể nhập
        txtName.setEditable(true);
        txtName.setEnabled(true);
        
        cboCategory.setEnabled(true);
        
        txtCostPrice.setEditable(true);
        txtCostPrice.setEnabled(true);
        
        txtPrice.setEditable(true);
        txtPrice.setEnabled(true);
        
        txtPrepTime.setEditable(true);
        txtPrepTime.setEnabled(true);
        
        txtDescription.setEditable(true);
        txtDescription.setEnabled(true);
        
        // Setup RadioButton group cho trạng thái Available/Unavailable
        statusGroup = new ButtonGroup();
        statusGroup.add(rbAvailable);
        statusGroup.add(rbUnavailable);
        
        // Mặc định chọn Available
        rbAvailable.setSelected(true);
        
        // Enable các radio button
        rbAvailable.setEnabled(true);
        rbUnavailable.setEnabled(true);
        
        // Setup button chọn ảnh
        btnSelectImage.setEnabled(true);
        
        // Setup placeholder text
        setupPlaceholderText();
        
        // Setup input validation
        setupInputValidation();
    }

    /**
     * Thiết lập placeholder text cho các trường
     */
    private void setupPlaceholderText() {
        txtName.setToolTipText("Nhập tên món ăn (ví dụ: Phở Bò Tái)");
        txtCostPrice.setToolTipText("Nhập giá vốn (ví dụ: 25000)");
        txtPrice.setToolTipText("Nhập giá bán (ví dụ: 45000)");
        txtPrepTime.setToolTipText("Nhập thời gian chuẩn bị (phút)");
        txtDescription.setToolTipText("Nhập mô tả món ăn");
        cboCategory.setToolTipText("Chọn danh mục món ăn");
    }

    /**
     * Thiết lập validation đầu vào
     */
    private void setupInputValidation() {
        // Chỉ cho phép nhập số cho giá và thời gian
        txtCostPrice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE && c != '.') {
                    evt.consume();
                }
            }
        });
        
        txtPrice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE && c != '.') {
                    evt.consume();
                }
            }
        });
        
        txtPrepTime.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    evt.consume();
                }
            }
        });
    }

    /**
     * Load danh sách categories vào combobox
     */
    private void loadCategories() {
        try {
            categories = categoryController.getAllCategories();
            cboCategory.removeAllItems();
            
            if (categories != null && !categories.isEmpty()) {
                for (Category category : categories) {
                    cboCategory.addItem(category.getName());
                }
                cboCategory.setSelectedIndex(0);
            } else {
                cboCategory.addItem("Không có danh mục");
                cboCategory.setEnabled(false);
            }
        } catch (Exception e) {
            logger.severe("Error loading categories: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Không thể tải danh sách danh mục: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        /*
        // Select image button
        btnSelectImage.addActionListener(e -> btnSelectImageActionPerformed(e));
        
        // Create button
        //btnCreate.addActionListener(e -> createDish());
        
        // Cancel button
        btnCancel.addActionListener(e -> {
            returnStatus = RET_CANCEL;
            dispose();
        });
        */
        
        // Set default button
        getRootPane().setDefaultButton(btnCreate);
    }


    /**
     * Hiển thị preview ảnh đã chọn
     */
    private void displayImagePreview() {
        if (selectedImageFile != null) {
            try {
                ImageIcon imageIcon = new ImageIcon(selectedImageFile.getAbsolutePath());
                
                // Scale ảnh để fit với label
                Image image = imageIcon.getImage();
                Image scaledImage = image.getScaledInstance(
                    lblImagePreview.getWidth(), 
                    lblImagePreview.getHeight(), 
                    Image.SCALE_SMOOTH);
                
                lblImagePreview.setIcon(new ImageIcon(scaledImage));
                lblImagePreview.setText("");
                
                btnSelectImage.setText("Đổi ảnh");
            } catch (Exception e) {
                logger.warning("Error displaying image preview: " + e.getMessage());
                JOptionPane.showMessageDialog(this,
                    "Không thể hiển thị ảnh: " + e.getMessage(),
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Tạo món ăn mới
     */
/**
 * Tạo món ăn mới - Method cho CreateDishDialog
 */
private void createDish() {
    try {
        // Validate input trước khi tạo
        if (!validateInput()) {
            return;
        }
        
        // Save image trước (nếu có)
        if (selectedImageFile != null) {
            savedImagePath = saveImage();
            if (savedImagePath == null) {
                JOptionPane.showMessageDialog(this,
                    "Không thể lưu ảnh. Vui lòng thử lại.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Tạo đối tượng Dish từ form
        Dish dish = createDishFromForm();
        
        // Thêm món ăn vào database
        boolean success = dishController.create(dish);
        
        if (success) {
            // Hiển thị thông báo thành công
            JOptionPane.showMessageDialog(this,
                "Thêm món ăn thành công!",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            // Đặt cờ dữ liệu đã được lưu
            dataSaved = true;
            returnStatus = RET_OK;
            
            // Refresh parent panel trước khi đóng
            if (parentPanel != null) {
                try {
                    parentPanel.refreshData();
                } catch (Exception e) {
                    logger.warning("Error refreshing parent panel: " + e.getMessage());
                }
            }
            
            // Đóng dialog
            dispose();
            
        } else {
            // Xóa file ảnh đã lưu nếu tạo món ăn thất bại
            if (savedImagePath != null) {
                deleteImageFile(savedImagePath);
                savedImagePath = null;
            }
            
            // Thông báo lỗi đã được hiển thị trong dishController.create()
            // Không cần hiển thị thêm popup lỗi ở đây
        }
        
    } catch (Exception e) {
        logger.severe("Unexpected error creating dish: " + e.getMessage());
        
        // Xóa file ảnh đã lưu nếu có lỗi
        if (savedImagePath != null) {
            deleteImageFile(savedImagePath);
            savedImagePath = null;
        }
        
        JOptionPane.showMessageDialog(this,
            "Có lỗi bất ngờ xảy ra: " + e.getMessage(),
            "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
    /**
     * Validate input fields
     */
    private boolean validateInput() {
        // Kiểm tra tên món ăn
        if (txtName.getText().trim().isEmpty()) {
            showValidationError("Vui lòng nhập tên món ăn!");
            txtName.requestFocus();
            return false;
        }
        
        // Kiểm tra danh mục
        if (cboCategory.getSelectedIndex() == -1 || categories == null || categories.isEmpty()) {
            showValidationError("Vui lòng chọn danh mục!");
            cboCategory.requestFocus();
            return false;
        }
        
        // Kiểm tra giá vốn
        if (txtCostPrice.getText().trim().isEmpty()) {
            showValidationError("Vui lòng nhập giá vốn!");
            txtCostPrice.requestFocus();
            return false;
        }
        
        try {
            double costPrice = Double.parseDouble(txtCostPrice.getText().trim());
            if (costPrice <= 0) {
                showValidationError("Giá vốn phải lớn hơn 0!");
                txtCostPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showValidationError("Giá vốn phải là số!");
            txtCostPrice.requestFocus();
            return false;
        }
        
        // Kiểm tra giá bán
        if (txtPrice.getText().trim().isEmpty()) {
            showValidationError("Vui lòng nhập giá bán!");
            txtPrice.requestFocus();
            return false;
        }
        
        try {
            double price = Double.parseDouble(txtPrice.getText().trim());
            if (price <= 0) {
                showValidationError("Giá bán phải lớn hơn 0!");
                txtPrice.requestFocus();
                return false;
            }
            
            // Kiểm tra logic giá bán > giá vốn
            double costPrice = Double.parseDouble(txtCostPrice.getText().trim());
            if (price <= costPrice) {
                int result = JOptionPane.showConfirmDialog(this,
                    "Cảnh báo: Giá bán không nên thấp hơn hoặc bằng giá vốn!\n" +
                    "Bạn có chắc chắn muốn tiếp tục?",
                    "Cảnh báo", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
                return result == JOptionPane.YES_OPTION;
            }
        } catch (NumberFormatException e) {
            showValidationError("Giá bán phải là số!");
            txtPrice.requestFocus();
            return false;
        }
        
        // Kiểm tra thời gian chuẩn bị
        if (txtPrepTime.getText().trim().isEmpty()) {
            showValidationError("Vui lòng nhập thời gian chuẩn bị!");
            txtPrepTime.requestFocus();
            return false;
        }
        
        try {
            int prepTime = Integer.parseInt(txtPrepTime.getText().trim());
            if (prepTime <= 0) {
                showValidationError("Thời gian chuẩn bị phải lớn hơn 0!");
                txtPrepTime.requestFocus();
                return false;
            }
            if (prepTime > 300) { // 5 giờ
                showValidationError("Thời gian chuẩn bị không nên quá 300 phút!");
                txtPrepTime.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showValidationError("Thời gian chuẩn bị phải là số nguyên!");
            txtPrepTime.requestFocus();
            return false;
        }
        
        // Kiểm tra trạng thái (Available/Unavailable)
        if (!rbAvailable.isSelected() && !rbUnavailable.isSelected()) {
            showValidationError("Vui lòng chọn trạng thái món ăn!");
            rbAvailable.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * Hiển thị thông báo lỗi validation
     */
    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
    }

   
    /**
     * Tạo đối tượng Dish từ dữ liệu form
     */
    
    private Dish createDishFromForm() {
        String name = txtName.getText().trim();
        int categoryId = categories.get(cboCategory.getSelectedIndex()).getId();
        BigDecimal price = new BigDecimal(txtPrice.getText().trim());
        BigDecimal costPrice = new BigDecimal(txtCostPrice.getText().trim());
        int prepTime = Integer.parseInt(txtPrepTime.getText().trim());
        String description = txtDescription.getText().trim();
        boolean isAvailable = rbAvailable.isSelected();
        
        Dish dish = new Dish();
        dish.setName(name);
        dish.setCategoryId(categoryId);
        dish.setPrice(price);
        dish.setCostPrice(costPrice);
        dish.setPrepTime(prepTime);
        dish.setDescription(description.isEmpty() ? null : description);
        dish.setAvailable(isAvailable);
        
        if (savedImagePath != null) {
            dish.setImageUrl(savedImagePath);
        }
        
        return dish;
    }

    /**
     * Lưu ảnh đã chọn vào thư mục dishes
     */
    private String saveImage() {
        try {
            /*
            // Tạo thư mục dishes images nếu chưa tồn tại
            //Path dishesDir = Paths.get("src/main/resources/assets/images/dishes");
            // Tạo thư mục dish images nếu chưa tồn tại
            Path dishesDir = Paths.get("src/main/resources/assets/images/dish");           
            if (!Files.exists(dishesDir)) {
                Files.createDirectories(dishesDir);
            }
            
            // Tạo tên file unique
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String originalName = selectedImageFile.getName();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String newFileName = "dish_" + timestamp + extension;
            
            // Copy file vào thư mục dishes
            Path targetPath = dishesDir.resolve(newFileName);
            Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Trả về đường dẫn tương đối để lưu database
            return "/assets/images/dishes/" + newFileName;
            */
            return ImageUtils.saveImage(selectedImageFile, "assets/images/dishes/");
        } catch (IOException e) {
            logger.severe("Error saving image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Xóa file ảnh đã lưu
     */
    private void deleteImageFile(String imagePath) {
        try {
            /*
            
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                Path imageFile = Paths.get(imagePath);
                if (Files.exists(imageFile)) {
                    Files.delete(imageFile);
                    logger.info("Deleted image file: " + imagePath);
                }
            }
            */
            if (imagePath != null) {
                Files.deleteIfExists(Paths.get(imagePath));   
            }    
        } catch (Exception e) {
            logger.warning("Error deleting image file: " + e.getMessage());
        }
    }
    /**
     * Setup ESC key để đóng dialog
     */
    private void setupEscapeKey() {
        String cancelName = "cancel";
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put(cancelName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnStatus = RET_CANCEL;
                dispose();
            }
        });
    }

    /**
     * Get return status
     */
    public int getReturnStatus() {
        return returnStatus;
    }

    /**
     * Reset form về trạng thái ban đầu
     */
    public void resetForm() {
        txtName.setText("");
        txtCostPrice.setText("");
        txtPrice.setText("");
        txtPrepTime.setText("");
        txtDescription.setText("");
        
        if (cboCategory.getItemCount() > 0) {
            cboCategory.setSelectedIndex(0);
        }
        
        rbAvailable.setSelected(true);
        rbUnavailable.setSelected(false);
        
        lblImagePreview.setIcon(null);
        lblImagePreview.setText("Không có ảnh");
        btnSelectImage.setText("Thêm ảnh");
        
        selectedImageFile = null;
        savedImagePath = null;
        
        txtName.requestFocus();
    }

    /**
     * Cập nhật danh sách categories
     */
    public void refreshCategories() {
        loadCategories();
    }
    
    /*
     * @return the return status of this dialog - one of RET_OK or RET_CANCEL
     
    public int getReturnStatus() {
        return returnStatus;
    }*/

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
        lblImagePreview = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        txtDescription = new javax.swing.JTextField();
        cboCategory = new javax.swing.JComboBox<>();
        btnSelectImage = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnCreate = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        txtCostPrice = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        txtPrepTime = new javax.swing.JTextField();
        rbUnavailable = new javax.swing.JRadioButton();
        rbAvailable = new javax.swing.JRadioButton();
        jLabel26 = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        pnProfile.setBackground(new java.awt.Color(255, 255, 255));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setText("Tạo Món Ăn Mới");

        pnProfile1.setBackground(new java.awt.Color(255, 255, 255));

        lblImagePreview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/employee/coala-160.png"))); // NOI18N
        lblImagePreview.setText("jLabel12");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel17.setText("Tên");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel23.setText("Chi phí:");

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel25.setText("Danh Mục:");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel18.setText("Mô tả:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel18)
                .addGap(18, 18, 18)
                .addComponent(txtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(txtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        cboCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Admin", "Nhân Viên", "Bếp", "Thu Ngân" }));
        cboCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboCategoryActionPerformed(evt);
            }
        });

        btnSelectImage.setText("Thêm ảnh");
        btnSelectImage.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnSelectImageMouseClicked(evt);
            }
        });
        btnSelectImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectImageActionPerformed(evt);
            }
        });

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
        btnCreate.setText("Tạo Mới");
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

        jLabel2.setText("* Kích thước ảnh tối đa 160*160");

        txtName.setToolTipText("");
        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });

        txtCostPrice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCostPriceActionPerformed(evt);
            }
        });

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel28.setText("Giá bán:");

        txtPrice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPriceActionPerformed(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel24.setText("Prep time:");

        txtPrepTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPrepTimeActionPerformed(evt);
            }
        });

        rbUnavailable.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        rbUnavailable.setForeground(new java.awt.Color(102, 102, 102));
        rbUnavailable.setText("Ngừng bán");
        rbUnavailable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbUnavailableActionPerformed(evt);
            }
        });

        rbAvailable.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        rbAvailable.setForeground(new java.awt.Color(0, 153, 51));
        rbAvailable.setText("Đang bán");
        rbAvailable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbAvailableActionPerformed(evt);
            }
        });

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel26.setText("Trạng Thái:");

        javax.swing.GroupLayout pnProfile1Layout = new javax.swing.GroupLayout(pnProfile1);
        pnProfile1.setLayout(pnProfile1Layout);
        pnProfile1Layout.setHorizontalGroup(
            pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnProfile1Layout.createSequentialGroup()
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnProfile1Layout.createSequentialGroup()
                        .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnProfile1Layout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2)))
                            .addGroup(pnProfile1Layout.createSequentialGroup()
                                .addGap(80, 80, 80)
                                .addComponent(btnSelectImage)))
                        .addGap(37, 37, 37)
                        .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25)
                            .addGroup(pnProfile1Layout.createSequentialGroup()
                                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel17)
                                    .addComponent(jLabel23))
                                .addGap(34, 34, 34)
                                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnProfile1Layout.createSequentialGroup()
                                        .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(46, 46, 46)
                                        .addComponent(jLabel28)
                                        .addGap(34, 34, 34)
                                        .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(cboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(pnProfile1Layout.createSequentialGroup()
                                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel24)
                                    .addComponent(jLabel26))
                                .addGap(23, 23, 23)
                                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnProfile1Layout.createSequentialGroup()
                                        .addComponent(rbAvailable)
                                        .addGap(36, 36, 36)
                                        .addComponent(rbUnavailable))
                                    .addComponent(txtPrepTime, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(pnProfile1Layout.createSequentialGroup()
                        .addGap(171, 171, 171)
                        .addComponent(btnCreate, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(126, 126, 126)
                        .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(73, Short.MAX_VALUE))
        );
        pnProfile1Layout.setVerticalGroup(
            pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnProfile1Layout.createSequentialGroup()
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnProfile1Layout.createSequentialGroup()
                        .addGap(81, 81, 81)
                        .addComponent(lblImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSelectImage))
                    .addGroup(pnProfile1Layout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel25)
                            .addComponent(cboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(txtCostPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel28)
                            .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtPrepTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel24))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                        .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel26)
                            .addComponent(rbAvailable)
                            .addComponent(rbUnavailable))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(pnProfile1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreate, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout pnProfileLayout = new javax.swing.GroupLayout(pnProfile);
        pnProfile.setLayout(pnProfileLayout);
        pnProfileLayout.setHorizontalGroup(
            pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnProfileLayout.createSequentialGroup()
                .addContainerGap(307, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addGap(291, 291, 291))
            .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnProfileLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(pnProfile1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        pnProfileLayout.setVerticalGroup(
            pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnProfileLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel11)
                .addContainerGap(433, Short.MAX_VALUE))
            .addGroup(pnProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnProfileLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(pnProfile1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        getContentPane().add(pnProfile, new java.awt.GridBagConstraints());

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void cboCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboCategoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboCategoryActionPerformed

    private void btnSelectImageMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSelectImageMouseClicked

        // TODO add your handling code here:
    }//GEN-LAST:event_btnSelectImageMouseClicked

    private void btnSelectImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectImageActionPerformed
        // TODO add your handling code here:
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh món ăn");
        
        // Filter chỉ cho phép chọn file ảnh
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "gif", "bmp");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            displayImagePreview();
        }
    }//GEN-LAST:event_btnSelectImageActionPerformed

    private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnCancelMouseClicked

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        // TODO add your handling code here:
        returnStatus = RET_CANCEL;
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnCreateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCreateMouseClicked
        // TODO add your handling code here:

        createDish();
        
    }//GEN-LAST:event_btnCreateMouseClicked

    private void btnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
    
        createDish();
    }//GEN-LAST:event_btnCreateActionPerformed

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNameActionPerformed

    private void txtCostPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCostPriceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCostPriceActionPerformed

    private void txtPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPriceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPriceActionPerformed

    private void txtPrepTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPrepTimeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPrepTimeActionPerformed

    private void rbUnavailableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbUnavailableActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rbUnavailableActionPerformed

    private void rbAvailableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbAvailableActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rbAvailableActionPerformed

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
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
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>


    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCreate;
    private javax.swing.JButton btnSelectImage;
    private javax.swing.JComboBox<String> cboCategory;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblImagePreview;
    private javax.swing.JPanel pnProfile;
    private javax.swing.JPanel pnProfile1;
    private javax.swing.JRadioButton rbAvailable;
    private javax.swing.JRadioButton rbUnavailable;
    private javax.swing.JTextField txtCostPrice;
    private javax.swing.JTextField txtDescription;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPrepTime;
    private javax.swing.JTextField txtPrice;
    // End of variables declaration//GEN-END:variables

    public boolean isDataSaved() {
        return dataSaved;
    }
}
