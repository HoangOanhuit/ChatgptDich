
/*
 * Dialog for importing materials into inventory
 * Supports importing multiple materials in one transaction
 * Automatically creates stock logs and expense records
 */
package com.mycompany.quanlyquanan.view.inventory;

import com.mycompany.quanlyquanan.controller.InventoryController;
import com.mycompany.quanlyquanan.model.Material;
import com.mycompany.quanlyquanan.model.MaterialImport;
import com.mycompany.quanlyquanan.service.InventoryService;
import com.mycompany.quanlyquanan.service.MaterialService;
import com.mycompany.quanlyquanan.utils.ComponentStyleUtil;
import com.mycompany.quanlyquanan.utils.Session;
import com.mycompany.quanlyquanan.controller.InventoryController.ImportItem;
import com.mycompany.quanlyquanan.model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for importing materials into inventory
 * @author Admin
 */
public class NhapKhoDialog extends JDialog {
    
    // Services
    private final InventoryService inventoryService;
    private final MaterialService materialService;
    private final InventoryController inventoryController;
    
    // UI Components
    private JTextField txtSupplier;
    private JTextField txtInvoiceNumber;
    private JTextArea txtNotes;
    private JLabel lblTotalAmount;
    
    // Import table
    private JTable tableImports;
    private DefaultTableModel tableModel;
    private JButton btnAddMaterial;
    private JButton btnRemoveMaterial;
    
    // Action buttons
    private JButton btnSave;
    private JButton btnCancel;
    
    // Data
    private List<Material> availableMaterials;
    private List<InventoryController.ImportItem> importItems;
    private boolean dataChanged = false;
    
    // Constants
    private static final Color PRIMARY_COLOR = new Color(52, 168, 183);
    private static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    
    /**
     * Constructor
     */
    public NhapKhoDialog(JFrame parent) {
        super(parent, "Nhập Kho Nguyên Vật Liệu", true);
        
        this.inventoryService = new InventoryService();
        this.materialService = new MaterialService();
        this.inventoryController = new InventoryController();
        this.importItems = new ArrayList<InventoryController.ImportItem>();
        
        initComponents();
        setupEventHandlers();
        loadAvailableMaterials();
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(parent);
        ComponentStyleUtil.styleMainButton(btnSave);
        btnSave.setPreferredSize(new Dimension(150, 40));
        ComponentStyleUtil.styleSecondButton(btnCancel);

    }
    
    /**
     * Initialize all UI components
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header panel
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Main content panel
        add(createMainPanel(), BorderLayout.CENTER);
        
        // Button panel
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * Create header panel with import info
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            "Thông Tin Nhập Kho",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12),
            PRIMARY_COLOR
        ));
        
        // Info form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Supplier
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Nhà cung cấp:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtSupplier = new JTextField(20);
        txtSupplier.setFont(new Font("SansSerif", Font.PLAIN, 12));
        formPanel.add(txtSupplier, gbc);
        
        // Invoice number
        gbc.gridx = 2; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Số hóa đơn:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtInvoiceNumber = new JTextField(15);
        txtInvoiceNumber.setFont(new Font("SansSerif", Font.PLAIN, 12));
        formPanel.add(txtInvoiceNumber, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        formPanel.add(new JLabel("Ghi chú:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtNotes = new JTextArea(3, 30);
        txtNotes.setFont(new Font("SansSerif", Font.PLAIN, 12));
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(txtNotes);
        notesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formPanel.add(notesScroll, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Total amount panel
        JPanel totalPanel = createTotalPanel();
        panel.add(totalPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create total amount panel
     */
    private JPanel createTotalPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SECONDARY_COLOR));
        
        JLabel lblTotalText = new JLabel("Tổng giá trị nhập:");
        lblTotalText.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTotalText.setForeground(SECONDARY_COLOR);
        
        lblTotalAmount = new JLabel("0 VND");
        lblTotalAmount.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTotalAmount.setForeground(PRIMARY_COLOR);
        
        panel.add(lblTotalText);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(lblTotalAmount);
        
        return panel;
    }
    
    /**
     * Create main panel with import table
     */
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Table panel
        JPanel tablePanel = createTablePanel();
        panel.add(tablePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create table panel
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            "Danh Sách Nguyên Liệu Nhập",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12),
            PRIMARY_COLOR
        ));
        
        // Create table
        createImportTable();
        JScrollPane scrollPane = new JScrollPane(tableImports);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Table control buttons
        JPanel controlPanel = createTableControlPanel();
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create import table
     */
    private void createImportTable() {
        String[] columnNames = {
            "Nguyên Liệu", "Số Lượng", "Đơn Vị", "Giá/Đơn Vị", "Thành Tiền", "Hạn Sử Dụng", "Xóa"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // All columns except unit and total are editable
                return column != 2 && column != 4; // Unit and total columns are not editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 1: case 3: case 4: return BigDecimal.class;
                    case 6: return Boolean.class;
                    default: return String.class;
                }
            }
        };
        
        tableImports = new JTable(tableModel) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == 6) { // Delete button column
                    return new DeleteButtonRenderer();
                }
                return super.getCellRenderer(row, column);
            }
            
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == 0) { // Material column
                    return new MaterialComboBoxEditor();
                } else if (column == 6) { // Delete button column
                    return new DeleteButtonEditor();
                }
                return super.getCellEditor(row, column);
            }
        };
        
        tableImports.setRowHeight(30);
        tableImports.getTableHeader().setBackground(PRIMARY_COLOR);
        tableImports.getTableHeader().setForeground(Color.WHITE);
        tableImports.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        
        // Set column widths
        int[] columnWidths = {200, 80, 60, 100, 120, 120, 60};
        for (int i = 0; i < columnWidths.length && i < tableImports.getColumnCount(); i++) {
            tableImports.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
        

        // Add table model listener to update row totals and overall total
        tableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();

            if (column == 1 || column == 3) {
                updateRowTotal(row);
            }

            updateTotalAmount();
        });
    }
    
    /**
     * Create table control panel
     */
    private JPanel createTableControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        btnAddMaterial = new JButton("Thêm Nguyên Liệu");
        btnAddMaterial.setBackground(SUCCESS_COLOR);
        btnAddMaterial.setForeground(Color.WHITE);
        btnAddMaterial.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnAddMaterial.setFocusPainted(false);
        btnAddMaterial.setBorderPainted(false);
        btnAddMaterial.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnRemoveMaterial = new JButton("Xóa Dòng Được Chọn");
        btnRemoveMaterial.setBackground(DANGER_COLOR);
        btnRemoveMaterial.setForeground(Color.WHITE);
        btnRemoveMaterial.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRemoveMaterial.setFocusPainted(false);
        btnRemoveMaterial.setBorderPainted(false);
        btnRemoveMaterial.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRemoveMaterial.setEnabled(false);
        
        panel.add(btnAddMaterial);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(btnRemoveMaterial);
        
        return panel;
    }
    
    /**
     * Create button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SECONDARY_COLOR));
        
        btnSave = new JButton("Lưu Phiếu Nhập");
        btnSave.setBackground(SUCCESS_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSave.setPreferredSize(new Dimension(200, 40));
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
        // Table selection listener
        tableImports.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnRemoveMaterial.setEnabled(tableImports.getSelectedRow() != -1);
            }
        });
        
        // Button listeners
        btnAddMaterial.addActionListener(e -> addMaterialRow());
        btnRemoveMaterial.addActionListener(e -> removeSelectedRow());
        btnSave.addActionListener(e -> saveImport());
        btnCancel.addActionListener(e -> dispose());
    }
    
    /**
     * Load available materials
     */
    private void loadAvailableMaterials() {
        try {
            availableMaterials = materialService.getAllActiveMaterials();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải danh sách nguyên liệu: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            availableMaterials = new ArrayList<>();
        }
    }
    
    /**
     * Add new material row to table
     */
    private void addMaterialRow() {
        if (availableMaterials.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Không có nguyên liệu nào trong hệ thống!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Add new row with default values
        Object[] row = {
            "-- Chọn nguyên liệu --",  // Material
            BigDecimal.ZERO,          // Quantity
            "",                       // Unit
            BigDecimal.ZERO,          // Unit price
            BigDecimal.ZERO,          // Total price
            "",                       // Expiry date
            "Xóa"                     // Delete button
        };
        
        tableModel.addRow(row);
        
        // Start editing the material column of the new row
        int newRowIndex = tableModel.getRowCount() - 1;
        tableImports.setRowSelectionInterval(newRowIndex, newRowIndex);
        tableImports.editCellAt(newRowIndex, 0);
    }
    
    /**
     * Remove selected row from table
     */
    private void removeSelectedRow() {
        int selectedRow = tableImports.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
            updateTotalAmount();
        }
    }
    
    /**
     * Update total amount
     */
    private void updateTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object totalValue = tableModel.getValueAt(i, 4); // Total column
            if (totalValue instanceof BigDecimal) {
                total = total.add((BigDecimal) totalValue);
            }
        }
        
        lblTotalAmount.setText(String.format("%,.0f VND", total));
    }
    
    /**
     * Calculate row total when quantity or unit price changes
     */
    private void updateRowTotal(int row) {
        try {
            Object quantityObj = tableModel.getValueAt(row, 1);
            Object unitPriceObj = tableModel.getValueAt(row, 3);
            
            BigDecimal quantity = BigDecimal.ZERO;
            BigDecimal unitPrice = BigDecimal.ZERO;
            
            if (quantityObj instanceof BigDecimal) {
                quantity = (BigDecimal) quantityObj;
            } else if (quantityObj != null) {
                quantity = new BigDecimal(quantityObj.toString());
            }
            
            if (unitPriceObj instanceof BigDecimal) {
                unitPrice = (BigDecimal) unitPriceObj;
            } else if (unitPriceObj != null) {
                unitPrice = new BigDecimal(unitPriceObj.toString());
            }
            
            BigDecimal total = quantity.multiply(unitPrice);
            tableModel.setValueAt(total, row, 4);
            
        } catch (NumberFormatException e) {
            tableModel.setValueAt(BigDecimal.ZERO, row, 4);
        }
    }
    
    /**
     * Save import to database
     */
    private void saveImport() {
        if (!validateInput()) {
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        btnSave.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Employee currentUser = Session.getInstance().getCurrentUser();
                if (currentUser == null) {
                    throw new IllegalStateException("Phiên đăng nhập đã hết hạn");
                }

                List<InventoryController.ImportItem> items = collectImportItems();
                if (items.isEmpty()) {
                    throw new IllegalArgumentException("Phải có ít nhất một nguyên liệu để nhập");
                }

                return inventoryController.createImportTransaction(
                    txtSupplier.getText().trim(),
                    txtInvoiceNumber.getText().trim(),
                    txtNotes.getText().trim(),
                    items,
                    currentUser.getId()
                );
            }

            @Override
            protected void done() {
                try {
                    setCursor(Cursor.getDefaultCursor());
                    btnSave.setEnabled(true);

                    Boolean success = get();
                    if (success != null && success) {
                        dataChanged = true; // QUAN TRỌNG
                        
                        LichSuKhoPanel.LichSuKhoPanelManager.refreshAllPanels();                        
                        
                        LichSuKhoPanel.LichSuKhoPanelManager.refreshAllPanels();
                        JOptionPane.showMessageDialog(NhapKhoDialog.this,
                            "Đã lưu phiếu nhập kho thành công!\n" +
                            "Đã cập nhật số lượng nguyên liệu và tạo lịch sử giao dịch.",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(NhapKhoDialog.this,
                            "Không thể lưu phiếu nhập kho!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(NhapKhoDialog.this,
                        "Lỗi khi lưu: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }
    
    /**
     * Validate input data
     */
    private boolean validateInput() {
        // Check supplier
        if (txtSupplier.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập tên nhà cung cấp!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtSupplier.requestFocus();
            return false;
        }
        
        // Check table data
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng thêm ít nhất một nguyên liệu!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Validate each row
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            // Check material selection
            Object material = tableModel.getValueAt(i, 0);
            if (material == null || material.toString().equals("-- Chọn nguyên liệu --")) {
                JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn nguyên liệu cho dòng " + (i + 1) + "!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // Check quantity
            Object quantityObj = tableModel.getValueAt(i, 1);
            try {
                BigDecimal quantity = new BigDecimal(quantityObj.toString());
                if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "Số lượng phải lớn hơn 0 cho dòng " + (i + 1) + "!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Số lượng không hợp lệ ở dòng " + (i + 1) + "!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // Check unit price
            Object unitPriceObj = tableModel.getValueAt(i, 3);
            try {
                BigDecimal unitPrice = new BigDecimal(unitPriceObj.toString());
                if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(this,
                        "Giá đơn vị không được âm ở dòng " + (i + 1) + "!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Giá đơn vị không hợp lệ ở dòng " + (i + 1) + "!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Collect import items from table
     */
    private List<ImportItem> collectImportItems() {
        List<ImportItem> items = new ArrayList<>();
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String materialName = tableModel.getValueAt(i, 0).toString();
            
            // Find material by name
            Material material = availableMaterials.stream()
                .filter(m -> m.getName().equals(materialName))
                .findFirst()
                .orElse(null);
            
            if (material != null) {
                BigDecimal quantity = new BigDecimal(tableModel.getValueAt(i, 1).toString());
                BigDecimal unitPrice = new BigDecimal(tableModel.getValueAt(i, 3).toString());
                String expiryDate = tableModel.getValueAt(i, 5) != null ? 
                    tableModel.getValueAt(i, 5).toString() : "";
                
                ImportItem item = new ImportItem();
                item.setMaterialId(material.getId());
                item.setMaterialName(material.getName());
                item.setQuantity(quantity);
                item.setUnitPrice(unitPrice);
                item.setTotalPrice(quantity.multiply(unitPrice));
                item.setExpiryDate(expiryDate);
                
                items.add(item);
            }
        }
        
        return items;
    }
    
    // ==================== CUSTOM TABLE COMPONENTS ====================
    
    /**
     * Material ComboBox Editor
     */
    private class MaterialComboBoxEditor extends DefaultCellEditor {
        private JComboBox<String> comboBox;
        
        public MaterialComboBoxEditor() {
            super(new JComboBox<>());
            comboBox = (JComboBox<String>) getComponent();
            comboBox.setEditable(false);
            
            // Populate combo box
            comboBox.addItem("-- Chọn nguyên liệu --");
            for (Material material : availableMaterials) {
                comboBox.addItem(material.getName());
            }
            
            // Add listener to update unit when material is selected
            comboBox.addActionListener(e -> {
                String selectedMaterial = (String) comboBox.getSelectedItem();
                if (selectedMaterial != null && !selectedMaterial.equals("-- Chọn nguyên liệu --")) {
                    Material material = availableMaterials.stream()
                        .filter(m -> m.getName().equals(selectedMaterial))
                        .findFirst()
                        .orElse(null);
                    
                    if (material != null) {
                        int row = tableImports.getSelectedRow();
                        if (row != -1) {
                            tableModel.setValueAt(material.getUnit(), row, 2);
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Delete Button Renderer
     */
    private class DeleteButtonRenderer extends JButton implements TableCellRenderer {
        public DeleteButtonRenderer() {
            setText("Xóa");
            setFont(new Font("SansSerif", Font.BOLD, 10));
            setBackground(DANGER_COLOR);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }
    
    /**
     * Delete Button Editor
     */
    private class DeleteButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean clicked;
        private int row;
        
        public DeleteButtonEditor() {
            super(new JCheckBox());
            button = new JButton("Xóa");
            button.setFont(new Font("SansSerif", Font.BOLD, 10));
            button.setBackground(DANGER_COLOR);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.row = row;
            clicked = true;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                tableModel.removeRow(row);
                updateTotalAmount();
            }
            clicked = false;
            return "Xóa";
        }
        
        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }
    
    // ==================== HELPER CLASSES ====================
    
    /**
     * Import Item data class
     */
    /*
    public static class ImportItem {
        private int materialId;
        private String materialName;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String expiryDate;
        
        // Getters and setters
        public int getMaterialId() { return materialId; }
        public void setMaterialId(int materialId) { this.materialId = materialId; }
        
        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }
        
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        
        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    }
    */
    // ==================== GETTERS ====================
    
    public boolean isDataChanged() {
        return dataChanged;
    }
}