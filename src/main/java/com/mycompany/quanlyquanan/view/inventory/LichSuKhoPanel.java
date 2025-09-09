/*
 * Panel for viewing inventory transaction history
 * Displays all stock movements with filtering and search capabilities
 * Shows detailed information about imports, exports, adjustments, and waste
 */
package com.mycompany.quanlyquanan.view.inventory;

import com.mycompany.quanlyquanan.model.Material;
import com.mycompany.quanlyquanan.model.StockLog;
import com.mycompany.quanlyquanan.service.InventoryService;
import com.mycompany.quanlyquanan.service.MaterialService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Panel for viewing inventory transaction history
 * @author Admin
 */

public class LichSuKhoPanel extends JPanel {
    
    // Services
    private final InventoryService inventoryService;
    private final MaterialService materialService;
    
    // UI Components - Filters
    private JTextField txtSearch;
    private JComboBox<String> cmbMaterialFilter;
    private JComboBox<StockLog.ChangeType> cmbTransactionTypeFilter;
    private JComboBox<String> cmbDateRangeFilter;
    private JButton btnFilter;
    private JButton btnClearFilter;
    private JButton btnExport;
    
    private JButton btnRefresh;
    
    // UI Components - Table
    private JTable tableHistory;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // UI Components - Statistics
    private JLabel lblTotalTransactions;
    private JLabel lblTotalIncrease;
    private JLabel lblTotalDecrease;
    private JLabel lblDateRange;
    
    // Data
    private List<StockLog> stockLogs;
    private List<Material> materials;
    
    // Constants
    private static final Color PRIMARY_COLOR = new Color(52, 168, 183);
    private static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Constructor
     */
    public LichSuKhoPanel() {
        this.inventoryService = new InventoryService();
        this.materialService = new MaterialService();
        LichSuKhoPanelManager.registerPanel(this);
        
        
        
        initComponents();
        setupEventHandlers();
        setupTableAppearance();
        loadData();
        
        // Increase default width slightly for better initial visibility
        Dimension preferred = getPreferredSize();
        preferred.width += 150;
        setPreferredSize(preferred);
    }
    
    /**
     * Initialize all UI components
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);
        
        // Filter panel
        add(createFilterPanel(), BorderLayout.NORTH);
        
        // Main content with table and stats
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(createTablePanel());
        splitPane.setBottomComponent(createStatsPanel());
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.8);
        add(splitPane, BorderLayout.CENTER);
    }
        /**
     * PHƯƠNG THỨC MỚI: Refresh dữ liệu khi có thay đổi
     */
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Reload materials và stock logs
                materials = materialService.getAllActiveMaterials();
                updateMaterialFilter();

                stockLogs = inventoryService.getAllStockLogs();
                updateTableData();
                updateStatistics();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi cập nhật dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }
    
    public class LichSuKhoPanelManager {
        private static final Set<LichSuKhoPanel> activePanels = new HashSet<>();

        public static void registerPanel(LichSuKhoPanel panel) {
            activePanels.add(panel);
        }

        public static void unregisterPanel(LichSuKhoPanel panel) {
            activePanels.remove(panel);
        }

        public static void refreshAllPanels() {
            activePanels.forEach(panel -> {
                if (panel != null && panel.isDisplayable()) {
                    panel.refreshData();
                }
            });
            // Cleanup panels không còn hiển thị
            activePanels.removeIf(panel -> panel == null || !panel.isDisplayable());
        }
    }
    /**
     * Create filter panel
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            "Bộ Lọc Tìm Kiếm",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12),
            PRIMARY_COLOR
        ));
        
        // Search and filter controls
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Search text
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        controlsPanel.add(new JLabel("Tìm kiếm:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 12));
        txtSearch.setToolTipText("Tìm theo tên nguyên liệu, ghi chú, người tạo...");
        controlsPanel.add(txtSearch, gbc);
        
        // Material filter
        gbc.gridx = 2; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        controlsPanel.add(new JLabel("Nguyên liệu:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbMaterialFilter = new JComboBox<>();
        cmbMaterialFilter.setFont(new Font("SansSerif", Font.PLAIN, 12));
        controlsPanel.add(cmbMaterialFilter, gbc);
        
        // Transaction type filter
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        controlsPanel.add(new JLabel("Loại GD:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbTransactionTypeFilter = new JComboBox<>();
        cmbTransactionTypeFilter.setFont(new Font("SansSerif", Font.PLAIN, 12));
        setupTransactionTypeFilter();
        controlsPanel.add(cmbTransactionTypeFilter, gbc);
        
        // Date range filter
        gbc.gridx = 2; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        controlsPanel.add(new JLabel("Thời gian:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbDateRangeFilter = new JComboBox<>();
        cmbDateRangeFilter.setFont(new Font("SansSerif", Font.PLAIN, 12));
        setupDateRangeFilter();
        controlsPanel.add(cmbDateRangeFilter, gbc);
        
        panel.add(controlsPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createFilterButtonPanel();
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Setup transaction type filter
     */
    private void setupTransactionTypeFilter() {
        cmbTransactionTypeFilter.addItem(null); // All types
        cmbTransactionTypeFilter.addItem(StockLog.ChangeType.IMPORT);
        cmbTransactionTypeFilter.addItem(StockLog.ChangeType.EXPORT);
        cmbTransactionTypeFilter.addItem(StockLog.ChangeType.ADJUST);
        cmbTransactionTypeFilter.addItem(StockLog.ChangeType.CONSUME);
        cmbTransactionTypeFilter.addItem(StockLog.ChangeType.WASTE);
        cmbTransactionTypeFilter.addItem(StockLog.ChangeType.RETURN);
        
        // Custom renderer for transaction types
        cmbTransactionTypeFilter.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if (value == null) {
                    setText("-- Tất cả loại giao dịch --");
                } else if (value instanceof StockLog.ChangeType) {
                    setText(((StockLog.ChangeType) value).getDisplayName());
                }
                
                return this;
            }
        });
    }
    
    /**
     * Setup date range filter
     */
    private void setupDateRangeFilter() {
        cmbDateRangeFilter.addItem("Tất cả");
        cmbDateRangeFilter.addItem("Hôm nay");
        cmbDateRangeFilter.addItem("7 ngày qua");
        cmbDateRangeFilter.addItem("30 ngày qua");
        cmbDateRangeFilter.addItem("90 ngày qua");
        cmbDateRangeFilter.addItem("Năm nay");
    }
    
    /**
     * Create filter button panel
     */
    private JPanel createFilterButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(Color.WHITE);
        
        btnFilter = createButton("Lọc", PRIMARY_COLOR, null);
        btnClearFilter = createButton("Xóa Lọc", SECONDARY_COLOR, null);
        btnExport = createButton("Xuất Excel", SUCCESS_COLOR, null);
        
        panel.add(btnFilter);
        panel.add(btnClearFilter);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnExport);
        
        return panel;
    }
    
    /**
     * Create table panel
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            "Lịch Sử Giao Dịch",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12),
            PRIMARY_COLOR
        ));
        
        // Create table
        createHistoryTable();
        JScrollPane scrollPane = new JScrollPane(tableHistory);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create history table
     */
    private void createHistoryTable() {
        String[] columnNames = {
            "ID", "Thời Gian", "Nguyên Liệu", "Loại GD", "Thay Đổi", 
            "Trước", "Sau", "Đơn Vị", "Người Thực Hiện", "Ghi Chú"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only table
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Integer.class;
                    case 4: case 5: case 6: return BigDecimal.class;
                    default: return String.class;
                }
            }
        };
        
        tableHistory = new JTable(tableModel);
        tableHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableHistory.setRowHeight(25);
        tableHistory.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        int[] columnWidths = {50, 120, 150, 100, 80, 80, 80, 60, 120, 200};
        for (int i = 0; i < columnWidths.length && i < tableHistory.getColumnCount(); i++) {
            tableHistory.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
        
        // Add sorting
        sorter = new TableRowSorter<>(tableModel);
        tableHistory.setRowSorter(sorter);
    }
    
    /**
     * Create statistics panel
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            "Thống Kê",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12),
            PRIMARY_COLOR
        ));
        
        // Stats grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        statsGrid.setBorder(new EmptyBorder(15, 15, 15, 15));
        statsGrid.setBackground(Color.WHITE);
        
        // Total transactions
        JPanel totalCard = createStatCard("Tổng GD", "0", PRIMARY_COLOR);
        lblTotalTransactions = (JLabel) ((JPanel) totalCard.getComponent(1)).getComponent(1);
        
        // Total increase
        JPanel increaseCard = createStatCard("Tăng", "0", SUCCESS_COLOR);
        lblTotalIncrease = (JLabel) ((JPanel) increaseCard.getComponent(1)).getComponent(1);
        
        // Total decrease
        JPanel decreaseCard = createStatCard("Giảm", "0", DANGER_COLOR);
        lblTotalDecrease = (JLabel) ((JPanel) decreaseCard.getComponent(1)).getComponent(1);
        
        // Date range
        JPanel dateCard = createStatCard("Thời gian", "Tất cả", SECONDARY_COLOR);
        lblDateRange = (JLabel) ((JPanel) dateCard.getComponent(1)).getComponent(1);
        
        statsGrid.add(totalCard);
        statsGrid.add(increaseCard);
        statsGrid.add(decreaseCard);
        statsGrid.add(dateCard);
        
        panel.add(statsGrid, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create stat card
     */
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(LIGHT_GRAY);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Icon placeholder
        JLabel iconLabel = new JLabel("📊");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(iconLabel, BorderLayout.WEST);
        
        // Text content
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        titleLabel.setForeground(SECONDARY_COLOR);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        valueLabel.setForeground(color);
        
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(valueLabel, BorderLayout.CENTER);
        
        card.add(textPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Create styled button
     */
    private JButton createButton(String text, Color color, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 30));
        
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
     * Setup table appearance
     */
    private void setupTableAppearance() {
        // Header styling
        tableHistory.getTableHeader().setBackground(PRIMARY_COLOR);
        tableHistory.getTableHeader().setForeground(Color.WHITE);
        tableHistory.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        
        // Custom cell renderer
        tableHistory.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : LIGHT_GRAY);
                    
                    // Color transaction type column
                    if (column == 3 && value != null) { // Transaction type column
                        String transactionType = value.toString();
                        if (transactionType.contains("Nhập")) {
                            c.setForeground(SUCCESS_COLOR);
                        } else if (transactionType.contains("Xuất") || transactionType.contains("Hao hụt")) {
                            c.setForeground(DANGER_COLOR);
                        } else if (transactionType.contains("Điều chỉnh")) {
                            c.setForeground(WARNING_COLOR);
                        } else {
                            c.setForeground(PRIMARY_COLOR);
                        }
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (column == 4 && value instanceof BigDecimal) { // Quantity change column
                        BigDecimal change = (BigDecimal) value;
                        if (change.compareTo(BigDecimal.ZERO) > 0) {
                            c.setForeground(SUCCESS_COLOR);
                        } else if (change.compareTo(BigDecimal.ZERO) < 0) {
                            c.setForeground(DANGER_COLOR);
                        } else {
                            c.setForeground(SECONDARY_COLOR);
                        }
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(Color.BLACK);
                        setFont(getFont().deriveFont(Font.PLAIN));
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
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Search functionality
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    applyFilters();
                }
            }
        });
        
        // Button listeners
        btnFilter.addActionListener(e -> applyFilters());
        btnClearFilter.addActionListener(e -> clearFilters());
        btnExport.addActionListener(e -> exportToExcel());
    }
    
    /**
     * Load data from database
     */
    private void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Load materials for filter
                materials = materialService.getAllActiveMaterials();
                updateMaterialFilter();
                
                // Load stock logs
                stockLogs = inventoryService.getAllStockLogs();
                updateTableData();
                updateStatistics();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Update material filter combo box
     */
    private void updateMaterialFilter() {
        cmbMaterialFilter.removeAllItems();
        cmbMaterialFilter.addItem("-- Tất cả nguyên liệu --");
        
        if (materials != null) {
            for (Material material : materials) {
                cmbMaterialFilter.addItem(material.getName());
            }
        }
    }
    
    /**
     * Update table data
     */
    private void updateTableData() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        if (stockLogs == null) return;
        
        // Add stock log data to table
        for (StockLog log : stockLogs) {
            Object[] row = {
                log.getId(),
                log.getCreatedAt() != null ? log.getCreatedAt().format(DATETIME_FORMATTER) : "",
                log.getMaterialName() != null ? log.getMaterialName() : "N/A",
                log.getChangeType().getDisplayName(),
                log.getQuantityChange(),
                log.getQuantityBefore(),
                log.getQuantityAfter(),
                log.getMaterialUnit() != null ? log.getMaterialUnit() : "",
                log.getCreatedByName() != null ? log.getCreatedByName() : "System",
                log.getNote() != null ? log.getNote() : ""
            };
            
            tableModel.addRow(row);
        }
    }
    
    /**
     * Update statistics
     */
    private void updateStatistics() {
        if (stockLogs == null) {
            lblTotalTransactions.setText("0");
            lblTotalIncrease.setText("0");
            lblTotalDecrease.setText("0");
            lblDateRange.setText("Không có dữ liệu");
            return;
        }
        
        int totalTransactions = stockLogs.size();
        BigDecimal totalIncrease = BigDecimal.ZERO;
        BigDecimal totalDecrease = BigDecimal.ZERO;
        
        LocalDateTime minDate = null;
        LocalDateTime maxDate = null;
        
        for (StockLog log : stockLogs) {
            BigDecimal change = log.getQuantityChange();
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                totalIncrease = totalIncrease.add(change);
            } else {
                totalDecrease = totalDecrease.add(change.abs());
            }
            
            // Track date range
            if (log.getCreatedAt() != null) {
                if (minDate == null || log.getCreatedAt().isBefore(minDate)) {
                    minDate = log.getCreatedAt();
                }
                if (maxDate == null || log.getCreatedAt().isAfter(maxDate)) {
                    maxDate = log.getCreatedAt();
                }
            }
        }
        
        // Update labels
        lblTotalTransactions.setText(String.valueOf(totalTransactions));
        lblTotalIncrease.setText(String.format("%,.0f", totalIncrease));
        lblTotalDecrease.setText(String.format("%,.0f", totalDecrease));
        
        // Update date range
        if (minDate != null && maxDate != null) {
            if (minDate.toLocalDate().equals(maxDate.toLocalDate())) {
                lblDateRange.setText(minDate.format(DATE_FORMATTER));
            } else {
                lblDateRange.setText(minDate.format(DATE_FORMATTER) + " - " + maxDate.format(DATE_FORMATTER));
            }
        } else {
            lblDateRange.setText("Không có dữ liệu");
        }
    }
    
    /**
     * Apply filters
     */
    private void applyFilters() {
        try {
            // Get filter criteria
            String searchText = txtSearch.getText().trim();
            String selectedMaterial = (String) cmbMaterialFilter.getSelectedItem();
            StockLog.ChangeType selectedTransactionType = (StockLog.ChangeType) cmbTransactionTypeFilter.getSelectedItem();
            String selectedDateRange = (String) cmbDateRangeFilter.getSelectedItem();
            
            // Apply date range filter first
            LocalDateTime startDate = null;
            LocalDateTime endDate = LocalDateTime.now();
            
            switch (selectedDateRange) {
                case "Hôm nay":
                    startDate = LocalDate.now().atStartOfDay();
                    break;
                case "7 ngày qua":
                    startDate = LocalDate.now().minusDays(7).atStartOfDay();
                    break;
                case "30 ngày qua":
                    startDate = LocalDate.now().minusDays(30).atStartOfDay();
                    break;
                case "90 ngày qua":
                    startDate = LocalDate.now().minusDays(90).atStartOfDay();
                    break;
                case "Năm nay":
                    startDate = LocalDate.now().withDayOfYear(1).atStartOfDay();
                    break;
                default: // "Tất cả"
                    startDate = null;
                    break;
            }
            
            // Get filtered stock logs
            List<StockLog> filteredLogs = inventoryService.getFilteredStockLogs(
                searchText.isEmpty() ? null : searchText,
                selectedMaterial != null && !selectedMaterial.startsWith("--") ? selectedMaterial : null,
                selectedTransactionType,
                startDate,
                endDate
            );
            
            // Update display with filtered data
            stockLogs = filteredLogs;
            updateTableData();
            updateStatistics();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi áp dụng bộ lọc: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Clear all filters
     */
    private void clearFilters() {
        txtSearch.setText("");
        cmbMaterialFilter.setSelectedIndex(0);
        cmbTransactionTypeFilter.setSelectedIndex(0);
        cmbDateRangeFilter.setSelectedIndex(0);
        
        // Reload all data
        loadData();
    }
    
    /**
     * Export data to Excel
     */
    private void exportToExcel() {
        try {
            if (stockLogs == null || stockLogs.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Không có dữ liệu để xuất!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // File chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu báo cáo lịch sử kho");
            fileChooser.setSelectedFile(new java.io.File("LichSuKho_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmm")) + ".xlsx"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                
                // TODO: Implement Excel export functionality
                // For now, show a message that feature is coming
                JOptionPane.showMessageDialog(this,
                    "Chức năng xuất Excel đang được phát triển!\n" +
                    "File sẽ được lưu tại: " + filePath,
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi xuất file: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // ==================== GETTERS ====================
    
    public List<StockLog> getStockLogs() {
        return stockLogs;
    }
    
    public StockLog getSelectedStockLog() {
        int selectedRow = tableHistory.getSelectedRow();
        if (selectedRow != -1 && stockLogs != null) {
            int modelRowIndex = tableHistory.convertRowIndexToModel(selectedRow);
            if (modelRowIndex < stockLogs.size()) {
                return stockLogs.get(modelRowIndex);
            }
        }
        return null;
    }
}