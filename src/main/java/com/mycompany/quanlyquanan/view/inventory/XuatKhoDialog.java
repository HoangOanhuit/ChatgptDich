/*
 * Dialog for exporting materials from inventory or adjusting stock
 * Supports various types of stock movements: export, adjust, waste, return
 * Automatically creates stock logs and updates material quantities
 */
package com.mycompany.quanlyquanan.view.inventory;

import com.mycompany.quanlyquanan.controller.InventoryController;
import com.mycompany.quanlyquanan.model.Employee;
import com.mycompany.quanlyquanan.model.Material;
import com.mycompany.quanlyquanan.model.StockLog;
import com.mycompany.quanlyquanan.service.InventoryService;
import com.mycompany.quanlyquanan.utils.ComponentStyleUtil;
import com.mycompany.quanlyquanan.utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dialog for exporting materials or adjusting stock
 * @author Admin
 */
    public class XuatKhoDialog extends JDialog {
    
    // Services
    private final InventoryService inventoryService;
    private final InventoryController inventoryController;
    
    // UI Components - Material Info
    private JLabel lblMaterialName;
    private JLabel lblMaterialUnit;
    private JLabel lblCurrentQuantity;
    private JLabel lblMaterialImage;
    
    // UI Components - Transaction Form
    private JComboBox<StockLog.ChangeType> cmbTransactionType;
    private JTextField txtQuantity;
    private JTextArea txtReason;
    private JLabel lblNewQuantity;
    private JLabel lblQuantityChange;
    
    // UI Components - Buttons
    private JButton btnSave;
    private JButton btnCancel;
    
    // Data
    private Material material;
    private boolean dataChanged = false;
    
    // Constants
    private static final Color PRIMARY_COLOR = new Color(52, 168, 183);
    private static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    
    /**
     * Constructor
     */
    public XuatKhoDialog(JFrame parent, Material material) {
        super(parent, "Xuất Kho / Điều Chỉnh", true);
        
        this.material = material;
        this.inventoryService = new InventoryService();
        this.inventoryController = new InventoryController();
        
        initComponents();
        setupEventHandlers();
        displayMaterialInfo();
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 550);
        setLocationRelativeTo(parent);

        ComponentStyleUtil.styleMainButton(btnSave);
        btnSave.setPreferredSize(new Dimension(150, 40));
        ComponentStyleUtil.styleSecondButton(btnCancel);
    }
    
    /**
     * Initialize all UI components
     */
    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Material info panel
        add(createMaterialInfoPanel(), BorderLayout.NORTH);
        
        // Transaction form panel
        add(createTransactionPanel(), BorderLayout.CENTER);
        
        // Button panel
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * Create material info panel
     */
    private JPanel createMaterialInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            "Thông Tin Nguyên Liệu",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12),
            PRIMARY_COLOR
        ));
        
        // Image panel
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imagePanel.setBackground(Color.WHITE);
        
        lblMaterialImage = new JLabel();
        lblMaterialImage.setPreferredSize(new Dimension(80, 80));
        lblMaterialImage.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
        lblMaterialImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblMaterialImage.setVerticalAlignment(SwingConstants.CENTER);
        setDefaultMaterialImage();
        
        imagePanel.add(lblMaterialImage);
        panel.add(imagePanel, BorderLayout.WEST);
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        lblMaterialName = createInfoLabel("", new Font("SansSerif", Font.BOLD, 16), Color.BLACK);
        lblMaterialUnit = createInfoLabel("", new Font("SansSerif", Font.PLAIN, 12), SECONDARY_COLOR);
        lblCurrentQuantity = createInfoLabel("", new Font("SansSerif", Font.PLAIN, 12), SECONDARY_COLOR);
        
        infoPanel.add(lblMaterialName);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblMaterialUnit);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblCurrentQuantity);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create transaction panel
     */
    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            "Thông Tin Giao Dịch",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12),
            PRIMARY_COLOR
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Transaction type
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Loại giao dịch:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        cmbTransactionType = new JComboBox<>();
        cmbTransactionType.addItem(StockLog.ChangeType.EXPORT);
        cmbTransactionType.addItem(StockLog.ChangeType.ADJUST);
        cmbTransactionType.addItem(StockLog.ChangeType.WASTE);
        cmbTransactionType.addItem(StockLog.ChangeType.RETURN);
        cmbTransactionType.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cmbTransactionType.setRenderer(new TransactionTypeRenderer());
        panel.add(cmbTransactionType, gbc);
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Số lượng:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtQuantity = new JTextField(15);
        txtQuantity.setFont(new Font("SansSerif", Font.PLAIN, 12));
        txtQuantity.setToolTipText("Nhập số lượng (số dương để xuất/giảm, số âm để tăng)");
        panel.add(txtQuantity, gbc);
        
        // Quantity change preview
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Thay đổi:"), gbc);
        
        gbc.gridx = 1;
        lblQuantityChange = createInfoLabel("0", new Font("SansSerif", Font.BOLD, 12), SECONDARY_COLOR);
        panel.add(lblQuantityChange, gbc);
        
        // New quantity preview
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Số lượng mới:"), gbc);
        
        gbc.gridx = 1;
        lblNewQuantity = createInfoLabel("0", new Font("SansSerif", Font.BOLD, 12), SECONDARY_COLOR);
        panel.add(lblNewQuantity, gbc);
        
        // Reason
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Lý do:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        txtReason = new JTextArea(4, 20);
        txtReason.setFont(new Font("SansSerif", Font.PLAIN, 12));
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR),
            new EmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane reasonScroll = new JScrollPane(txtReason);
        reasonScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(reasonScroll, gbc);
        
        return panel;
    }
    
    /**
     * Create button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SECONDARY_COLOR));
        
        btnSave = new JButton("Lưu Giao Dịch");
        btnSave.setBackground(SUCCESS_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSave.setPreferredSize(new Dimension(150, 40));
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnCancel = new JButton("Hủy");
        btnCancel.setBackground(SECONDARY_COLOR);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panel.add(btnSave);
        panel.add(btnCancel);
        
        return panel;
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Transaction type change listener
        cmbTransactionType.addActionListener(e -> updateTransactionTypeHelp());
        
        // Quantity change listener
        txtQuantity.addCaretListener(e -> updateQuantityPreview());
        
        // Button listeners
        btnSave.addActionListener(e -> saveTransaction());
        btnCancel.addActionListener(e -> dispose());
    }
    
    /**
     * Display material information
     */
    private void displayMaterialInfo() {
        if (material == null) return;
        
        lblMaterialName.setText(material.getName());
        lblMaterialUnit.setText("Đơn vị: " + material.getUnit());
        lblCurrentQuantity.setText("Số lượng hiện tại: " + material.getQuantity());
        
        // Set quantity color based on stock status
        if (material.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            lblCurrentQuantity.setForeground(DANGER_COLOR);
        } else if (material.getQuantity().compareTo(material.getThreshold()) <= 0) {
            lblCurrentQuantity.setForeground(WARNING_COLOR);
        } else {
            lblCurrentQuantity.setForeground(SUCCESS_COLOR);
        }
        
        // Load material image
        //updateMaterialImage();
        
        // Update quantity preview
        updateQuantityPreview();
    }
    
    /**
     * Update material image
     */
    /*
    private void updateMaterialImage() {
        if (material == null) {
            setDefaultMaterialImage();
            return;
        }
        
        try {
            String imagePath = material.getImageUrl();
            if (imagePath != null && !imagePath.isEmpty()) {
                ImageIcon materialIcon = new ImageIcon(getClass().getClassLoader().getResource(imagePath));
                if (materialIcon.getIconWidth() > 0) {
                    Image scaledImage = materialIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                    lblMaterialImage.setIcon(new ImageIcon(scaledImage));
                    lblMaterialImage.setText("");
                    lblMaterialImage.setOpaque(false);
                    return;
                }
            }
        } catch (Exception e) {
            // Image loading failed, use default
        }
        
        setDefaultMaterialImage();
    }
    */
    
    /**
     * Set default material image
     */
    private void setDefaultMaterialImage() {
        lblMaterialImage.setBackground(PRIMARY_COLOR);
        lblMaterialImage.setText("📦");
        lblMaterialImage.setFont(new Font("SansSerif", Font.PLAIN, 30));
        lblMaterialImage.setForeground(Color.WHITE);
        lblMaterialImage.setOpaque(true);
        lblMaterialImage.setIcon(null);
    }
    
    /**
     * Create info label
     */
    private JLabel createInfoLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    /**
     * Update transaction type help text
     */
    private void updateTransactionTypeHelp() {
        StockLog.ChangeType selectedType = (StockLog.ChangeType) cmbTransactionType.getSelectedItem();
        
        switch (selectedType) {
            case EXPORT:
                txtReason.setToolTipText("Lý do xuất kho (VD: Sử dụng sản xuất, bán lẻ, ...)");
                break;
            case ADJUST:
                txtReason.setToolTipText("Lý do điều chỉnh (VD: Kiểm kê, sai sót dữ liệu, ...)");
                break;
            case WASTE:
                txtReason.setToolTipText("Lý do hao hụt (VD: Hết hạn, hỏng, mất mát, ...)");
                break;
            case RETURN:
                txtReason.setToolTipText("Lý do hoàn trả (VD: Trả lại nhà cung cấp, hoàn từ sản xuất, ...)");
                break;
        }
    }
    
    /**
     * Update quantity preview
     */
    private void updateQuantityPreview() {
        if (material == null) return;
        
        try {
            String quantityText = txtQuantity.getText().trim();
            if (quantityText.isEmpty()) {
                lblQuantityChange.setText("0");
                lblNewQuantity.setText(material.getQuantity().toString());
                lblQuantityChange.setForeground(SECONDARY_COLOR);
                lblNewQuantity.setForeground(SECONDARY_COLOR);
                return;
            }
            
            BigDecimal inputQuantity = new BigDecimal(quantityText);
            StockLog.ChangeType transactionType = (StockLog.ChangeType) cmbTransactionType.getSelectedItem();
            
            BigDecimal quantityChange = BigDecimal.ZERO;
            
            // Calculate quantity change based on transaction type
            switch (transactionType) {
                case EXPORT:
                case WASTE:
                    // These operations decrease stock (negative change)
                    quantityChange = inputQuantity.negate();
                    break;
                case ADJUST:
                    // For adjust, input can be positive or negative
                    quantityChange = inputQuantity;
                    break;
                case RETURN:
                    // Return increases stock (positive change)
                    quantityChange = inputQuantity;
                    break;
            }
            
            BigDecimal newQuantity = material.getQuantity().add(quantityChange);
            
            // Update labels
            lblQuantityChange.setText(quantityChange.toString());
            lblNewQuantity.setText(newQuantity.toString());
            
            // Set colors based on change direction and result
            if (quantityChange.compareTo(BigDecimal.ZERO) > 0) {
                lblQuantityChange.setForeground(SUCCESS_COLOR);
                lblQuantityChange.setText("+" + quantityChange.toString());
            } else if (quantityChange.compareTo(BigDecimal.ZERO) < 0) {
                lblQuantityChange.setForeground(DANGER_COLOR);
            } else {
                lblQuantityChange.setForeground(SECONDARY_COLOR);
            }
            
            // Color new quantity based on stock level
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                lblNewQuantity.setForeground(DANGER_COLOR);
                lblNewQuantity.setText(newQuantity.toString() + " (Âm!)");
            } else if (newQuantity.compareTo(material.getThreshold()) <= 0) {
                lblNewQuantity.setForeground(WARNING_COLOR);
            } else {
                lblNewQuantity.setForeground(SUCCESS_COLOR);
            }
            
        } catch (NumberFormatException e) {
            lblQuantityChange.setText("Không hợp lệ");
            lblNewQuantity.setText("Không hợp lệ");
            lblQuantityChange.setForeground(DANGER_COLOR);
            lblNewQuantity.setForeground(DANGER_COLOR);
        }
    }
    
    /**
     * Save transaction
     */
    private void saveTransaction() {
        if (!validateInput()) {
            return;
        }

        try {
            // Kiểm tra user đăng nhập trước khi thực hiện giao dịch
            Employee currentUser = Session.getInstance().getCurrentUser();
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this,
                    "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String quantityText = txtQuantity.getText().trim();
            BigDecimal inputQuantity = new BigDecimal(quantityText);
            StockLog.ChangeType transactionType = (StockLog.ChangeType) cmbTransactionType.getSelectedItem();
            String reason = txtReason.getText().trim();

            // Calculate actual quantity change
            BigDecimal quantityChange = BigDecimal.ZERO;
            switch (transactionType) {
                case EXPORT:
                case WASTE:
                    quantityChange = inputQuantity.negate();
                    break;
                case ADJUST:
                case RETURN:
                    quantityChange = inputQuantity;
                    break;
            }

            BigDecimal quantityBefore = material.getQuantity();
            BigDecimal quantityAfter = quantityBefore.add(quantityChange);

            // Check if resulting quantity is negative
            if (quantityAfter.compareTo(BigDecimal.ZERO) < 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Số lượng sau giao dịch sẽ là " + quantityAfter + " (âm).\n" +
                    "Bạn có chắc chắn muốn tiếp tục?",
                    "Cảnh báo", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Create stock transaction - Sử dụng currentUser đã kiểm tra null
            boolean success = inventoryController.createStockTransaction(
                material.getId(),
                transactionType,
                quantityChange,
                quantityBefore,
                quantityAfter,
                reason,
                currentUser.getId()  // An toàn với null check
            );

            if (success) {
                dataChanged = true;

                // Show success message with transaction details
                String message = String.format(
                    "Đã lưu giao dịch thành công!\n\n" +
                    "Nguyên liệu: %s\n" +
                    "Loại giao dịch: %s\n" +
                    "Thay đổi: %s %s\n" +
                    "Số lượng trước: %s %s\n" +
                    "Số lượng sau: %s %s\n" +
                    "Người thực hiện: %s",
                    material.getName(),
                    transactionType.getDisplayName(),
                    quantityChange,
                    material.getUnit(),
                    quantityBefore,
                    material.getUnit(),
                    quantityAfter,
                    material.getUnit(),
                    currentUser.getName()  // Thêm thông tin người thực hiện
                );

                JOptionPane.showMessageDialog(this,
                    message,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi lưu giao dịch!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Số lượng không hợp lệ. Vui lòng nhập số!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtQuantity.requestFocus();
        } catch (Exception e) {
            String detail = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi lưu giao dịch: " + detail,
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Validate input data
     */
    private boolean validateInput() {
        // Check quantity
        String quantityText = txtQuantity.getText().trim();
        if (quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập số lượng!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtQuantity.requestFocus();
            return false;
        }

        try {
            BigDecimal quantity = new BigDecimal(quantityText);
            if (quantity.compareTo(BigDecimal.ZERO) == 0) {
                JOptionPane.showMessageDialog(this,
                    "Số lượng không thể bằng 0!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                txtQuantity.requestFocus();
                return false;
            }

            // Kiểm tra số lượng không quá lớn
            if (quantity.abs().compareTo(new BigDecimal("999999")) > 0) {
                JOptionPane.showMessageDialog(this,
                    "Số lượng không được vượt quá 999,999!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                txtQuantity.requestFocus();
                return false;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Số lượng phải là một số hợp lệ!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtQuantity.requestFocus();
            return false;
        }

        // Check reason
        String reason = txtReason.getText().trim();
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập lý do giao dịch!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtReason.requestFocus();
            return false;
        }

        if (reason.length() > 500) {
            JOptionPane.showMessageDialog(this,
                "Lý do không được vượt quá 500 ký tự!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtReason.requestFocus();
            return false;
        }

        // Check transaction type
        StockLog.ChangeType transactionType = (StockLog.ChangeType) cmbTransactionType.getSelectedItem();
        if (transactionType == null) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn loại giao dịch!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            cmbTransactionType.requestFocus();
            return false;
        }

        return true;
    }
    /**
    * Kiểm tra và lấy current user an toàn
    * @return Employee hiện tại hoặc null nếu chưa đăng nhập
    */
    private Employee getCurrentUserSafely() {
        try {
            return Session.getInstance().getCurrentUser();
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            return null;
        }
    }

   /**
    * Kiểm tra xem user có đăng nhập không
    * @return true nếu đã đăng nhập, false nếu chưa
    */
    private boolean isUserLoggedIn() {
        Employee user = getCurrentUserSafely();
        return user != null;
    }
    // ==================== CUSTOM RENDERERS ====================
    
    /**
     * Transaction Type ComboBox Renderer
     */
    private class TransactionTypeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof StockLog.ChangeType) {
                StockLog.ChangeType type = (StockLog.ChangeType) value;
                setText(type.getDisplayName());
                
                // Set icon color based on transaction type
                switch (type) {
                    case EXPORT:
                        setForeground(isSelected ? Color.WHITE : WARNING_COLOR);
                        break;
                    case ADJUST:
                        setForeground(isSelected ? Color.WHITE : PRIMARY_COLOR);
                        break;
                    case WASTE:
                        setForeground(isSelected ? Color.WHITE : DANGER_COLOR);
                        break;
                    case RETURN:
                        setForeground(isSelected ? Color.WHITE : SUCCESS_COLOR);
                        break;
                    default:
                        setForeground(isSelected ? Color.WHITE : Color.BLACK);
                        break;
                }
            }
            
            return this;
        }
    }
    
    // ==================== GETTERS ====================
    
    public boolean isDataChanged() {
        return dataChanged;
    }
    
    public Material getMaterial() {
        return material;
    }
}