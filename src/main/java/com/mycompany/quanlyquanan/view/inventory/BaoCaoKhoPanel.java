/*
 * Panel for inventory reports and analytics
 * Provides comprehensive inventory analysis with charts and detailed reports
 * Supports multiple report types: stock level, valuation, movement analysis
 */
package com.mycompany.quanlyquanan.view.inventory;

import com.mycompany.quanlyquanan.model.Material;
import com.mycompany.quanlyquanan.service.InventoryService;
import com.mycompany.quanlyquanan.service.MaterialService;
import com.mycompany.quanlyquanan.utils.ComponentStyleUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Panel for comprehensive inventory reports
 * @author Admin
 */
public class BaoCaoKhoPanel extends JPanel {
    
    // Services
    private final InventoryService inventoryService;
    private final MaterialService materialService;
    
    // UI Components - Report Options
    private JComboBox<String> cmbReportType;
    //private JComboBox<String> cmbCategory;
    private JComboBox<String> cmbStockStatus;
    private JButton btnGenerateReport;
    private JButton btnExportReport;
    private JButton btnPrintReport;
    
    // UI Components - Summary Cards
    private JLabel lblTotalItems;
    private JLabel lblTotalValue;
    private JLabel lblLowStockItems;
    private JLabel lblOutOfStockItems;
    
    // UI Components - Main Content
    private JTabbedPane tabbedPane;
    
    // Tables for different report types
    private JTable tableStockLevel;
    private DefaultTableModel stockLevelModel;
    private JTable tableValuation;
    private DefaultTableModel valuationModel;
    private JTable tableMovement;
    private DefaultTableModel movementModel;
    
    // Chart panel placeholder
    private JPanel chartPanel;
    
    // Data
    private List<Material> materials;
    private Map<String, Object> inventoryDashboard;
    
    // Constants
    private static final Color PRIMARY_COLOR = new Color(52, 168, 183);
    private static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color INFO_COLOR = new Color(23, 162, 184);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Constructor
     */
    public BaoCaoKhoPanel() {
        this.inventoryService = new InventoryService();
        this.materialService = new MaterialService();
        
        initComponents();
        setupEventHandlers();
        setupTableAppearance();
        loadData();
    }
    
    /**
     * Initialize all UI components
     */
    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);
        
        // Header with report options and summary
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Main content with tabbed reports
        add(createMainPanel(), BorderLayout.CENTER);
        
        // Bottom action panel
        add(createActionPanel(), BorderLayout.SOUTH);

        ComponentStyleUtil.styleMainButton(btnExportReport);
        ComponentStyleUtil.styleSecondButton(btnPrintReport);
        btnExportReport.setPreferredSize(new Dimension(150, 40));
        btnPrintReport.setPreferredSize(new Dimension(150, 40));
    }
    
    /**
     * Create header panel with options and summary
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Report options
        JPanel optionsPanel = createReportOptionsPanel();
        panel.add(optionsPanel, BorderLayout.NORTH);
        
        // Summary cards
        JPanel summaryPanel = createSummaryPanel();
        panel.add(summaryPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create report options panel
     */
    private JPanel createReportOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            "Tùy Chọn Báo Cáo",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12),
            PRIMARY_COLOR
        ));
        
        // Report type
        panel.add(new JLabel("Loại báo cáo:"));
        cmbReportType = new JComboBox<>();
        cmbReportType.addItem("Tồn kho theo mức độ");
        cmbReportType.addItem("Định giá kho hàng");
        cmbReportType.addItem("Phân tích xuất nhập");
        cmbReportType.addItem("Báo cáo tổng hợp");
        cmbReportType.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cmbReportType.setPreferredSize(new Dimension(180, 25));
        panel.add(cmbReportType);
        
        panel.add(Box.createHorizontalStrut(20));
        /*
        // Category filter
        panel.add(new JLabel("Danh mục:"));
        cmbCategory = new JComboBox<>();
        cmbCategory.addItem("-- Tất cả danh mục --");
        cmbCategory.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cmbCategory.setPreferredSize(new Dimension(150, 25));
        panel.add(cmbCategory);
        */
        panel.add(Box.createHorizontalStrut(20));
        
        // Stock status filter
        panel.add(new JLabel("Trạng thái:"));
        cmbStockStatus = new JComboBox<>();
        cmbStockStatus.addItem("-- Tất cả trạng thái --");
        cmbStockStatus.addItem("Đủ hàng");
        cmbStockStatus.addItem("Sắp hết");
        cmbStockStatus.addItem("Hết hàng");
        cmbStockStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cmbStockStatus.setPreferredSize(new Dimension(130, 25));
        panel.add(cmbStockStatus);
        
        panel.add(Box.createHorizontalStrut(20));
        
        // Generate button
        btnGenerateReport = new JButton("Tạo Báo Cáo");
        btnGenerateReport.setBackground(PRIMARY_COLOR);
        btnGenerateReport.setForeground(Color.WHITE);
        btnGenerateReport.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnGenerateReport.setFocusPainted(false);
        btnGenerateReport.setBorderPainted(false);
        btnGenerateReport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGenerateReport.setPreferredSize(new Dimension(120, 25));
        panel.add(btnGenerateReport);
        
        return panel;
    }
    
    /**
     * Create summary panel with key metrics
     */
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            "Tóm Tắt Kho Hàng",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12),
            PRIMARY_COLOR
        ));
        panel.setBackground(Color.WHITE);
        
        // Total items card
        JPanel totalItemsCard = createSummaryCard("Tổng Số Mặt Hàng", "0", PRIMARY_COLOR, "📦");
        lblTotalItems = getSummaryValueLabel(totalItemsCard);
        
        // Total value card
        JPanel totalValueCard = createSummaryCard("Tổng Giá Trị", "0 VND", SUCCESS_COLOR, "💰");
        lblTotalValue = getSummaryValueLabel(totalValueCard);
        
        // Low stock card
        JPanel lowStockCard = createSummaryCard("Sắp Hết Hàng", "0", WARNING_COLOR, "⚠️");
        lblLowStockItems = getSummaryValueLabel(lowStockCard);
        
        // Out of stock card
        JPanel outOfStockCard = createSummaryCard("Hết Hàng", "0", DANGER_COLOR, "🚫");
        lblOutOfStockItems = getSummaryValueLabel(outOfStockCard);
        
        panel.add(totalItemsCard);
        panel.add(totalValueCard);
        panel.add(lowStockCard);
        panel.add(outOfStockCard);
        
        return panel;
    }
    
    /**
     * Create summary card
     */
    private JPanel createSummaryCard(String title, String value, Color color, String emoji) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(LIGHT_GRAY);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Icon
        JLabel iconLabel = new JLabel(emoji);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(iconLabel, BorderLayout.WEST);
        
        // Text content
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        titleLabel.setForeground(SECONDARY_COLOR);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        valueLabel.setForeground(color);
        
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(valueLabel, BorderLayout.CENTER);
        
        card.add(textPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Get value label from summary card
     */
    private JLabel getSummaryValueLabel(JPanel summaryCard) {
        JPanel textPanel = (JPanel) summaryCard.getComponent(1);
        return (JLabel) textPanel.getComponent(1);
    }
    
    /**
     * Create main panel with tabbed reports
     */
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        // Stock level report tab
        tabbedPane.addTab("📊 Báo Cáo Tồn Kho", createStockLevelPanel());
        
        // Valuation report tab
        tabbedPane.addTab("💰 Định Giá Kho Hàng", createValuationPanel());
        
        // Movement analysis tab
        tabbedPane.addTab("📈 Phân Tích Xuất Nhập", createMovementPanel());
        
        // Chart analysis tab
        tabbedPane.addTab("📊 Biểu Đồ", createChartPanel());
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create stock level report panel
     */
    private JPanel createStockLevelPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table
        String[] columnNames = {
            "Mã NL", "Tên Nguyên Liệu", /*"Danh Mục",*/ "Số Lượng", "Đơn Vị", 
            "Ngưỡng TT", "Trạng Thái", "Ngày Cập Nhật"
        };
        
        stockLevelModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableStockLevel = new JTable(stockLevelModel);
        tableStockLevel.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(tableStockLevel);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create valuation report panel
     */
    private JPanel createValuationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table
        String[] columnNames = {
            "Mã NL", "Tên Nguyên Liệu", "Số Lượng", "Đơn Vị", 
            "Giá/Đơn Vị", "Tổng Giá Trị", "% Tổng", "Danh Mục"
        };
        
        valuationModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableValuation = new JTable(valuationModel);
        tableValuation.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(tableValuation);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create movement analysis panel
     */
    private JPanel createMovementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table
        String[] columnNames = {
            "Mã NL", "Tên Nguyên Liệu", "SL Đầu Kỳ", "Tổng Nhập", 
            "Tổng Xuất", "SL Cuối Kỳ", "Tỷ Lệ Luân Chuyển", "Ghi Chú"
        };
        
        movementModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableMovement = new JTable(movementModel);
        tableMovement.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(tableMovement);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create chart analysis panel
     */
    private JPanel createChartPanel() {
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        
        // Placeholder for charts
        JLabel placeholderLabel = new JLabel("Biểu đồ phân tích đang được phát triển", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
        placeholderLabel.setForeground(SECONDARY_COLOR);
        
        JPanel placeholderPanel = new JPanel(new GridBagLayout());
        placeholderPanel.setBackground(LIGHT_GRAY);
        placeholderPanel.add(placeholderLabel);
        
        chartPanel.add(placeholderPanel, BorderLayout.CENTER);
        
        return chartPanel;
    }
    
    /**
     * Create action panel
     */
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SECONDARY_COLOR));
        
        btnExportReport = createButton("Xuất Excel", SUCCESS_COLOR);
        btnPrintReport = createButton("In Báo Cáo", INFO_COLOR);
        
        panel.add(btnExportReport);
        panel.add(btnPrintReport);
        
        return panel;
    }
    
    /**
     * Create styled button
     */
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    /**
     * Setup table appearance for all tables
     */
    private void setupTableAppearance() {
        setupSingleTableAppearance(tableStockLevel);
        setupSingleTableAppearance(tableValuation);
        setupSingleTableAppearance(tableMovement);
    }
    
    /**
     * Setup appearance for a single table
     */
    private void setupSingleTableAppearance(JTable table) {
        if (table == null) return;
        
        // Header styling
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        table.getTableHeader().setReorderingAllowed(false);
        
        // Row styling
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : LIGHT_GRAY);
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(PRIMARY_COLOR.brighter());
                    c.setForeground(Color.BLACK);
                }
                
                return c;
            }
        });
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        btnGenerateReport.addActionListener(e -> generateReport());
        btnExportReport.addActionListener(e -> exportReport());
        btnPrintReport.addActionListener(e -> printReport());
        
        // Report type change listener
        cmbReportType.addActionListener(e -> updateReportView());
    }
    
    /**
     * Load data from database
     */
    private void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Load materials and dashboard data
                materials = materialService.getAllActiveMaterials();
                inventoryDashboard = inventoryService.getInventoryDashboard();
                
                // Update categories filter
                //updateCategoryFilter();
                
                // Update summary cards
                updateSummaryCards();
                
                // Generate initial report
                generateReport();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
    
    /*/
    private void updateCategoryFilter() {
        cmbCategory.removeAllItems();
        cmbCategory.addItem("-- Tất cả danh mục --");
        
        if (materials != null) {
            // Get unique categories
            materials.stream()
                .map(m -> m.getCategory() != null ? m.getCategory() : "Khác")
                .distinct()
                .sorted()
                .forEach(category -> cmbCategory.addItem(category));
        }
    }
    */
    /**
     * Update summary cards with current data
     */
    private void updateSummaryCards() {
        if (inventoryDashboard != null) {
            // Total items
            Object totalItems = inventoryDashboard.get("active_materials");
            lblTotalItems.setText(totalItems != null ? totalItems.toString() : "0");
            
            // Total value
            Object totalValue = inventoryDashboard.get("total_inventory_value");
            if (totalValue instanceof BigDecimal) {
                lblTotalValue.setText(String.format("%,.0f VND", (BigDecimal) totalValue));
            } else {
                lblTotalValue.setText("0 VND");
            }
            
            // Low stock
            Object lowStock = inventoryDashboard.get("low_stock_count");
            lblLowStockItems.setText(lowStock != null ? lowStock.toString() : "0");
            
            // Out of stock
            Object outOfStock = inventoryDashboard.get("out_of_stock_count");
            lblOutOfStockItems.setText(outOfStock != null ? outOfStock.toString() : "0");
        }
    }
    
    /**
     * Generate report based on selected options
     */
    private void generateReport() {
        try {
            String reportType = (String) cmbReportType.getSelectedItem();
            //String selectedCategory = (String) cmbCategory.getSelectedItem();
            String selectedStatus = (String) cmbStockStatus.getSelectedItem();
            
            // Filter materials based on selections
            List<Material> filteredMaterials = filterMaterials(/*selectedCategory, */selectedStatus);
            
            // Generate appropriate report
            switch (reportType) {
                case "Tồn kho theo mức độ":
                    generateStockLevelReport(filteredMaterials);
                    tabbedPane.setSelectedIndex(0);
                    break;
                case "Định giá kho hàng":
                    generateValuationReport(filteredMaterials);
                    tabbedPane.setSelectedIndex(1);
                    break;
                case "Phân tích xuất nhập":
                    generateMovementReport(filteredMaterials);
                    tabbedPane.setSelectedIndex(2);
                    break;
                case "Báo cáo tổng hợp":
                    generateAllReports(filteredMaterials);
                    break;
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tạo báo cáo: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    

    private List<Material> filterMaterials(String selectedCategory) {
        if (materials == null) return List.of();
        String selectedStatus = (String) cmbStockStatus.getSelectedItem();
        return materials.stream()
            .filter(material -> {
                /*
                // Category filter
                if (selectedCategory != null && !selectedCategory.startsWith("--")) {
                    String materialCategory = material.getCategory() != null ? material.getCategory() : "Khác";
                    if (!materialCategory.equals(selectedCategory)) {
                        return false;
                    }
                }
                */
                // Status filter
                if (selectedStatus != null && !selectedStatus.startsWith("--")) {
                    String status = getStockStatus(material);
                    if (!status.equals(selectedStatus)) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }
 
    /**
     * Get stock status of material
     */
    private String getStockStatus(Material material) {
        if (material.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            return "Hết hàng";
        } else if (material.getQuantity().compareTo(material.getThreshold()) <= 0) {
            return "Sắp hết";
        } else {
            return "Đủ hàng";
        }
    }
    
    /**
     * Generate stock level report
     */
    private void generateStockLevelReport(List<Material> filteredMaterials) {
        stockLevelModel.setRowCount(0);
        
        for (Material material : filteredMaterials) {
            String status = getStockStatus(material);
            
            Object[] row = {
                material.getId(),
                material.getName(),
                //material.getCategory() != null ? material.getCategory() : "Khác",
                material.getQuantity(),
                material.getUnit(),
                material.getThreshold(),
                status,
                material.getUpdatedAt() != null ? 
                    material.getUpdatedAt().format(DATETIME_FORMATTER) : "N/A"
            };
            
            stockLevelModel.addRow(row);
        }
        
        // Apply status-based row coloring
        tableStockLevel.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    // Get status from the status column
                    Object statusObj = table.getValueAt(row, 6);
                    if (statusObj != null) {
                        String status = statusObj.toString();
                        if (column == 6) { // Status column
                            if (status.equals("Hết hàng")) {
                                c.setForeground(DANGER_COLOR);
                            } else if (status.equals("Sắp hết")) {
                                c.setForeground(WARNING_COLOR);
                            } else {
                                c.setForeground(SUCCESS_COLOR);
                            }
                            setFont(getFont().deriveFont(Font.BOLD));
                        } else {
                            c.setForeground(Color.BLACK);
                        }
                        
                        // Row background based on status
                        if (status.equals("Hết hàng")) {
                            c.setBackground(new Color(255, 235, 235));
                        } else if (status.equals("Sắp hết")) {
                            c.setBackground(new Color(255, 248, 225));
                        } else {
                            c.setBackground(row % 2 == 0 ? Color.WHITE : LIGHT_GRAY);
                        }
                    }
                } else {
                    c.setBackground(PRIMARY_COLOR.brighter());
                    c.setForeground(Color.BLACK);
                }
                
                return c;
            }
        });
    }
    
    /**
     * Generate valuation report
     */
    private void generateValuationReport(List<Material> filteredMaterials) {
        valuationModel.setRowCount(0);
        
        // Calculate total value for percentage calculation
        BigDecimal totalInventoryValue = filteredMaterials.stream()
            .map(m -> m.getQuantity().multiply(m.getUnitPrice()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Sort by value descending
        filteredMaterials.sort((m1, m2) -> {
            BigDecimal value1 = m1.getQuantity().multiply(m1.getUnitPrice());
            BigDecimal value2 = m2.getQuantity().multiply(m2.getUnitPrice());
            return value2.compareTo(value1);
        });
        
        for (Material material : filteredMaterials) {
            BigDecimal materialValue = material.getQuantity().multiply(material.getUnitPrice());
            
            // Calculate percentage
            double percentage = totalInventoryValue.compareTo(BigDecimal.ZERO) > 0 ?
                materialValue.divide(totalInventoryValue, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100)).doubleValue() : 0.0;
            
            Object[] row = {
                material.getId(),
                material.getName(),
                material.getQuantity(),
                material.getUnit(),
                material.getUnitPrice(),
                materialValue,
                String.format("%.2f%%", percentage),
                //material.getCategory() != null ? material.getCategory() : "Khác"
            };
            
            valuationModel.addRow(row);
        }
    }
    
    /**
     * Generate movement analysis report
     */
    private void generateMovementReport(List<Material> filteredMaterials) {
        movementModel.setRowCount(0);
        
        for (Material material : filteredMaterials) {
            // TODO: Implement actual movement analysis with stock logs
            // For now, show placeholder data
            
            Object[] row = {
                material.getId(),
                material.getName(),
                "N/A", // Beginning quantity
                "N/A", // Total imports
                "N/A", // Total exports
                material.getQuantity(), // Current quantity
                "N/A", // Turnover ratio
                "Dữ liệu đang được phát triển"
            };
            
            movementModel.addRow(row);
        }
    }
    
    /**
     * Generate all reports
     */
    private void generateAllReports(List<Material> filteredMaterials) {
        generateStockLevelReport(filteredMaterials);
        generateValuationReport(filteredMaterials);
        generateMovementReport(filteredMaterials);
        
        JOptionPane.showMessageDialog(this,
            "Đã tạo tất cả báo cáo thành công!\nXem các tab để xem từng loại báo cáo.",
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Update report view when report type changes
     */
    private void updateReportView() {
        // This method can be used to update UI elements based on report type
        // Currently just triggers regenerate
        if (materials != null && !materials.isEmpty()) {
            generateReport();
        }
    }
    
    /**
     * Export report to Excel
     */
    private void exportReport() {
        try {
            // Get current tab
            int selectedTab = tabbedPane.getSelectedIndex();
            String reportName = "";
            
            switch (selectedTab) {
                case 0: reportName = "BaoCaoTonKho"; break;
                case 1: reportName = "DinhGiaKhoHang"; break;
                case 2: reportName = "PhanTichXuatNhap"; break;
                default: reportName = "BaoCaoTongHop"; break;
            }
            
            // File chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu báo cáo");
            fileChooser.setSelectedFile(new java.io.File(reportName + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmm")) + ".xlsx"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                
                // TODO: Implement Excel export functionality
                JOptionPane.showMessageDialog(this,
                    "Chức năng xuất Excel đang được phát triển!\n" +
                    "File sẽ được lưu tại: " + filePath,
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi xuất báo cáo: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Print report
     */
    private void printReport() {
        try {
            JTable currentTable = getCurrentTable();
            if (currentTable != null) {
                // TODO: Implement print functionality
                JOptionPane.showMessageDialog(this,
                    "Chức năng in báo cáo đang được phát triển!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi in báo cáo: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Get current table based on selected tab
     */
    private JTable getCurrentTable() {
        int selectedTab = tabbedPane.getSelectedIndex();
        switch (selectedTab) {
            case 0: return tableStockLevel;
            case 1: return tableValuation;
            case 2: return tableMovement;
            default: return null;
        }
    }
    
    // ==================== GETTERS ====================
    
    public List<Material> getMaterials() {
        return materials;
    }
    
    public Map<String, Object> getInventoryDashboard() {
        return inventoryDashboard;
    }
    
    public String getCurrentReportType() {
        return (String) cmbReportType.getSelectedItem();
    }
}