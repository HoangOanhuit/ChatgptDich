/*
 * Controller class for Inventory Management
 * Handles business logic for inventory operations, material imports/exports
 * Coordinates between UI layer and Service/DAO layers
 */
package com.mycompany.quanlyquanan.controller;

import com.mycompany.quanlyquanan.dao.ExpenseReceiptDAO;
import com.mycompany.quanlyquanan.dao.MaterialImportDAO;
import com.mycompany.quanlyquanan.dao.StockLogDAO;
import com.mycompany.quanlyquanan.model.*;
import com.mycompany.quanlyquanan.service.InventoryService;
import com.mycompany.quanlyquanan.service.MaterialService;
import com.mycompany.quanlyquanan.utils.Session;

import javax.swing.JOptionPane;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.mycompany.quanlyquanan.dao.RecipeDAO;
import com.mycompany.quanlyquanan.dao.DishDAO;
import java.util.ArrayList;

/**
 * Controller for inventory management operations
 * @author Admin
 */
public class InventoryController {
    
    private final InventoryService inventoryService;
    private final MaterialService materialService;
    private final ExpenseReceiptDAO expenseReceiptDAO;
    private final MaterialImportDAO materialImportDAO;
    private final StockLogDAO stockLogDAO;
    private final RecipeDAO recipeDAO;
    private final DishDAO dishDAO;
    
    /**
     * Constructor
     */
    public InventoryController() {
        this.inventoryService = new InventoryService();
        this.materialService = new MaterialService();
        this.expenseReceiptDAO = new ExpenseReceiptDAO();
        this.materialImportDAO = new MaterialImportDAO();
        this.stockLogDAO = new StockLogDAO();
        this.recipeDAO = new RecipeDAO();
        this.dishDAO = new DishDAO();
    }
    
    /**
     * Constructor with dependency injection for testing
     */
    public InventoryController(InventoryService inventoryService, MaterialService materialService, 
                              ExpenseReceiptDAO expenseReceiptDAO, MaterialImportDAO materialImportDAO,
                              StockLogDAO stockLogDAO, RecipeDAO recipeDAO, DishDAO dishDAO) {
        this.inventoryService = inventoryService;
        this.materialService = materialService;
        this.expenseReceiptDAO = expenseReceiptDAO;
        this.materialImportDAO = materialImportDAO;
        this.stockLogDAO = stockLogDAO;
        this.recipeDAO = recipeDAO;
        this.dishDAO = dishDAO;
    }
    
    // ==================== MATERIAL MANAGEMENT ====================
    
    /**
     * Get all active materials
     */
    public List<Material> getAllMaterials() {
        try {
            return materialService.getAllActiveMaterials();
        } catch (Exception e) {
            System.err.println("Error getting all materials: " + e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách nguyên liệu", e);
        }
    }
    
    /**
     * Get material by ID
     */
    public Material getMaterialById(int id) {
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
            }
            
            Material material = materialService.getMaterialById(id);
            if (material == null) {
                throw new IllegalArgumentException("Không tìm thấy nguyên liệu với ID: " + id);
            }
            
            return material;
        } catch (Exception e) {
            System.err.println("Error getting material by ID: " + e.getMessage());
            throw new RuntimeException("Không thể lấy thông tin nguyên liệu", e);
        }
    }
    
    /**
     * Create new material
     */
    /*
    public boolean createMaterial(Material material) {
        try {
            // Validate material data
            validateMaterial(material);
            
            // Set creator
            if (Session.getCurrentUser() != null) {
                material.setCreatedBy(Session.getCurrentUser().getId());
                material.setUpdatedBy(Session.getCurrentUser().getId());
            }
            
            // Set timestamps
            material.setCreatedAt(LocalDateTime.now());
            material.setUpdatedAt(LocalDateTime.now());
            
            return materialService.createMaterial(material);
        } catch (Exception e) {
            System.err.println("Error creating material: " + e.getMessage());
            throw new RuntimeException("Không thể tạo nguyên liệu mới", e);
        }
    }
    */
    
    /**
     * Update existing material
     */
    /*
    public boolean updateMaterial(Material material) {
        try {
            // Validate material data
            validateMaterial(material);
            
            // Check if material exists
            Material existingMaterial = materialService.getMaterialById(material.getId());
            if (existingMaterial == null) {
                throw new IllegalArgumentException("Nguyên liệu không tồn tại");
            }
            
            // Set updater
            if (Session.getCurrentUser() != null) {
                material.setUpdatedBy(Session.getCurrentUser().getId());
            }
            material.setUpdatedAt(LocalDateTime.now());
            
            return materialService.updateMaterial(material);
        } catch (Exception e) {
            System.err.println("Error updating material: " + e.getMessage());
            throw new RuntimeException("Không thể cập nhật nguyên liệu", e);
        }
    }
    */
    
    /**
     * Delete material (soft delete)
     */
    public boolean deleteMaterial(int materialId) {
        try {
            if (materialId <= 0) {
                throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
            }
            
            // Check if material exists
            Material material = materialService.getMaterialById(materialId);
            if (material == null) {
                throw new IllegalArgumentException("Nguyên liệu không tồn tại");
            }
            
            // Check if material has recent transactions
            boolean hasRecentTransactions = hasRecentTransactions(materialId, 30); // 30 days
            if (hasRecentTransactions) {
                throw new IllegalStateException("Không thể xóa nguyên liệu có giao dịch trong 30 ngày qua");
            }
            
            return materialService.deleteMaterial(materialId);
        } catch (Exception e) {
            System.err.println("Error deleting material: " + e.getMessage());
            throw new RuntimeException("Không thể xóa nguyên liệu", e);
        }
    }
    
    /**
     * Check if material has recent transactions
     */
    private boolean hasRecentTransactions(int materialId, int days) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            List<StockLog> recentLogs = stockLogDAO.getByMaterialId(materialId);
            
            return recentLogs.stream()
                .anyMatch(log -> log.getCreatedAt() != null && log.getCreatedAt().isAfter(cutoffDate));
        } catch (Exception e) {
            System.err.println("Error checking recent transactions: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate material data
     */
    private void validateMaterial(Material material) {
        if (material == null) {
            throw new IllegalArgumentException("Dữ liệu nguyên liệu không được null");
        }
        
        if (material.getName() == null || material.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nguyên liệu không được để trống");
        }
        
        if (material.getUnit() == null || material.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Đơn vị không được để trống");
        }
        
        if (material.getUnitPrice() == null || material.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá đơn vị không được âm");
        }
        
        if (material.getThreshold() == null || material.getThreshold().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Ngưỡng tối thiểu không được âm");
        }
        
        if (material.getQuantity() == null) {
            material.setQuantity(BigDecimal.ZERO);
        }
    }

    private void validateImportItems(List<ImportItem> items, String supplier) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    // ==================== IMPORT OPERATIONS ====================
    
    /**
     * Import Item data class - used for import transactions
     */
    public static class ImportItem {
        private int materialId;
        private String materialName;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String expiryDate;
        
        // Constructors
        public ImportItem() {}
        
        public ImportItem(int materialId, String materialName, BigDecimal quantity, 
                         BigDecimal unitPrice, BigDecimal totalPrice) {
            this.materialId = materialId;
            this.materialName = materialName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }
        
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
    
    /**
     * Create import transaction with multiple materials
     */
    public boolean createImportTransaction(String supplier, String invoiceNumber, 
                                         String notes, List<ImportItem> importItems, int createdBy) {
        try {
            // Validate import data
            validateImportTransaction(supplier, importItems);

            // Create material imports and update quantities
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (ImportItem item : importItems) {
                // Get material
                Material material = materialService.getMaterialById(item.getMaterialId());
                if (material == null) {
                    throw new IllegalArgumentException("Nguyên liệu không tồn tại: " + item.getMaterialName());
                }

                // Create material import record
                MaterialImport materialImport = new MaterialImport();
                materialImport.setMaterialId(item.getMaterialId());
                materialImport.setImportQuantity(item.getQuantity());
                materialImport.setImportPrice(item.getTotalPrice());
                materialImport.setPricePerUnit(item.getUnitPrice());
                materialImport.setSupplier(supplier); // Lưu supplier vào bảng material_imports
                materialImport.setInvoiceNumber(invoiceNumber);
                materialImport.setNotes(notes);
                materialImport.setCreatedBy(createdBy);
                materialImport.setImportDate(LocalDateTime.now());

                // Save import record
                boolean importSaved = materialImportDAO.insert(materialImport);
                if (!importSaved) {
                    throw new RuntimeException("Không thể lưu phiếu nhập cho: " + item.getMaterialName());
                }

                // Update material quantity và tính lại giá trung bình
                BigDecimal quantityBefore = material.getQuantity();
                BigDecimal quantityAfter = quantityBefore.add(item.getQuantity());
                BigDecimal avgPrice = materialImportDAO.getAveragePricePerUnitByMaterialId(item.getMaterialId());

                // Update material - BỎ supplier vì bảng materials không có cột này
                material.setQuantity(quantityAfter);
                material.setPricePerUnit(avgPrice); 
                material.setUpdatedAt(LocalDateTime.now());

                boolean materialUpdated = materialService.updateMaterial(material);
                if (!materialUpdated) {
                    throw new RuntimeException("Không thể cập nhật số lượng cho: " + item.getMaterialName());
                }

                // Cập nhật giá vốn các món ăn có sử dụng nguyên liệu này
                updateDishCostsForMaterial(item.getMaterialId());
                
                // Create stock log
                boolean stockLogCreated = stockLogDAO.logImport(
                    item.getMaterialId(),
                    item.getQuantity(),
                    quantityBefore,
                    quantityAfter,
                    materialImport.getId(),
                    createdBy,
                    "Nhập kho từ " + supplier + (invoiceNumber != null ? 
                        " (HĐ: " + invoiceNumber + ")" : "")
                );

                if (!stockLogCreated) {
                    System.err.println("Warning: Could not create stock log for material: " + item.getMaterialName());
                }

                totalAmount = totalAmount.add(item.getTotalPrice());
            }

            // Create expense receipt
            createImportExpenseReceipt(supplier, invoiceNumber, totalAmount, notes, createdBy);

            return true;

        } catch (Exception e) {
            System.err.println("Error creating import transaction: " + e.getMessage());
            throw new RuntimeException("Không thể tạo phiếu nhập kho", e);
        }
    }         
    
    /**
     * Validate import transaction
     */
    private void validateImportTransaction(String supplier, List<ImportItem> importItems) {
        if (supplier == null || supplier.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống");
        }

        if (importItems == null || importItems.isEmpty()) {
            throw new IllegalArgumentException("Phải có ít nhất một nguyên liệu để nhập");
        }

        for (ImportItem item : importItems) {
            if (item.getMaterialId() <= 0) {
                throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
            }

            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0 cho: " + item.getMaterialName());
            }

            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Giá đơn vị không được âm cho: " + item.getMaterialName());
            }
        }
    }
    
    private void updateDishCostsForMaterial(int materialId) {
        try {
            List<Recipe> recipes = recipeDAO.getByMaterialId(materialId);
            if (recipes == null || recipes.isEmpty()) {
                return;
            }

            for (int dishId : recipes.stream()
                                     .map(Recipe::getDishId)
                                     .collect(Collectors.toSet())) {
                BigDecimal totalCost = recipeDAO.getTotalCostByDishId(dishId);
                dishDAO.updateCostPrice(dishId, totalCost);
            }
        } catch (Exception e) {
            System.err.println("Error updating dish cost for material " + materialId + ": " + e.getMessage());
        }
    }
    
    /**
     * Create expense receipt for import
     */
    private void createImportExpenseReceipt(String supplier, String invoiceNumber, 
                                          BigDecimal totalAmount, String notes, int createdBy) {
        try {
            ExpenseReceipt expenseReceipt = new ExpenseReceipt();
            expenseReceipt.setExpenseType("material_import"); // Use existing field
            expenseReceipt.setAmount(totalAmount);
            expenseReceipt.setDescription("Nhập kho từ " + supplier + 
                (invoiceNumber != null ? " - Hóa đơn: " + invoiceNumber : ""));
            expenseReceipt.setSupplier(supplier);
            expenseReceipt.setInvoiceNumber(invoiceNumber);
            expenseReceipt.setNotes(notes);
            expenseReceipt.setCreatedBy(createdBy);
            expenseReceipt.setExpenseDate(LocalDateTime.now());
            
            boolean expenseCreated = expenseReceiptDAO.insert(expenseReceipt);
            if (!expenseCreated) {
                System.err.println("Warning: Could not create expense receipt for import");
            }
            
        } catch (Exception e) {
            System.err.println("Error creating import expense receipt: " + e.getMessage());
            // Don't throw exception here as import is already completed
        }
    }
    
    // ==================== EXPORT/ADJUSTMENT OPERATIONS ====================
    
    /**
     * Create stock transaction (export, adjust, waste, return)
     */
    public boolean createStockTransaction(int materialId, StockLog.ChangeType transactionType,
                                        BigDecimal quantityChange, BigDecimal quantityBefore,
                                        BigDecimal quantityAfter, String reason, int createdBy) {
        try {
            // Validate transaction data
            validateStockTransaction(materialId, transactionType, quantityChange, reason);
            
            // Get material
            Material material = materialService.getMaterialById(materialId);
            if (material == null) {
                throw new IllegalArgumentException("Nguyên liệu không tồn tại");
            }
            
            // Verify current quantity matches expected
            if (material.getQuantity().compareTo(quantityBefore) != 0) {
                throw new IllegalStateException("Số lượng hiện tại không khớp với dự kiến. " +
                    "Vui lòng làm mới và thử lại.");
            }
            
            // Update material quantity
            material.setQuantity(quantityAfter);
            material.setUpdatedBy(createdBy);
            material.setUpdatedAt(LocalDateTime.now());
            
            boolean materialUpdated = materialService.updateMaterial(material);
            if (!materialUpdated) {
                throw new RuntimeException("Không thể cập nhật số lượng nguyên liệu");
            }
            
            // Create stock log based on transaction type
            String logNote = formatStockLogNote(transactionType, reason);
            boolean stockLogCreated = false;
            
            switch (transactionType) {
                case EXPORT:
                    stockLogCreated = stockLogDAO.logExport(materialId, quantityChange.abs(), 
                        quantityBefore, quantityAfter, createdBy, logNote);
                    break;
                case ADJUST:
                    stockLogCreated = stockLogDAO.logAdjust(materialId, quantityChange, 
                        quantityBefore, quantityAfter, createdBy, logNote);
                    break;
                case WASTE:
                    stockLogCreated = stockLogDAO.logWaste(materialId, quantityChange.abs(), 
                        quantityBefore, quantityAfter, createdBy, logNote);
                    break;
                case RETURN:
                    stockLogCreated = stockLogDAO.logReturn(materialId, quantityChange, 
                        quantityBefore, quantityAfter, 0, createdBy, logNote); // orderId = 0 for manual return
                    break;
                default:
                    throw new IllegalArgumentException("Loại giao dịch không được hỗ trợ: " + transactionType);
            }
            
            if (!stockLogCreated) {
                // Rollback material update if stock log creation fails
                material.setQuantity(quantityBefore);
                materialService.updateMaterial(material);
                throw new RuntimeException("Không thể tạo lịch sử giao dịch");
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error creating stock transaction: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
    * Get stock logs by date range
    */
    public List<StockLog> getStockLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
            }

            return getFilteredStockLogs(null, null, null, startDate, endDate);
        } catch (Exception e) {
            System.err.println("Error getting stock logs by date range: " + e.getMessage());
            throw new RuntimeException("Không thể lấy lịch sử theo khoảng thời gian", e);
        }
    }

    /**
     * Calculate total inventory value by category
     */
    /*
    public Map<String, BigDecimal> getInventoryValueByCategory() {
        try {
            List<Material> materials = materialService.getAllActiveMaterials();

            return materials.stream()
                .collect(Collectors.groupingBy(
                    m -> m.getCategory() != null ? m.getCategory() : "Khác",
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        m -> m.getQuantity().multiply(m.getUnitPrice()),
                        BigDecimal::add
                    )
                ));
        } catch (Exception e) {
            System.err.println("Error calculating inventory value by category: " + e.getMessage());
            return new HashMap<>();
        }
    }
    */
    /**
     * Validate stock transaction
     */
    private void validateStockTransaction(int materialId, StockLog.ChangeType transactionType,
                                        BigDecimal quantityChange, String reason) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }
        
        if (transactionType == null) {
            throw new IllegalArgumentException("Loại giao dịch không được null");
        }
        
        if (quantityChange == null || quantityChange.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Số lượng thay đổi không được bằng 0");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do giao dịch không được để trống");
        }
    }
    
    /**
     * Format stock log note based on transaction type
     */
    private String formatStockLogNote(StockLog.ChangeType transactionType, String reason) {
        String prefix = "";
        
        switch (transactionType) {
            case EXPORT:
                prefix = "Xuất kho: ";
                break;
            case ADJUST:
                prefix = "Điều chỉnh: ";
                break;
            case WASTE:
                prefix = "Hao hụt: ";
                break;
            case RETURN:
                prefix = "Hoàn trả: ";
                break;
            default:
                prefix = "Giao dịch: ";
                break;
        }
        
        return prefix + reason;
    }
    
    // ==================== INVENTORY OVERVIEW ====================
    
    /**
     * Get inventory dashboard data
     */
    public Map<String, Object> getInventoryDashboard() {
        try {
            return inventoryService.getInventoryDashboard();
        } catch (Exception e) {
            System.err.println("Error getting inventory dashboard: " + e.getMessage());
            throw new RuntimeException("Không thể lấy tổng quan kho hàng", e);
        }
    }
    
    /**
     * Get inventory overview
     */
    public InventoryService.InventoryOverview getInventoryOverview() {
        try {
            return inventoryService.getInventoryOverview();
        } catch (Exception e) {
            System.err.println("Error getting inventory overview: " + e.getMessage());
            throw new RuntimeException("Không thể lấy tổng quan chi tiết kho hàng", e);
        }
    }
    
    /**
     * Get low stock materials
     */
    public List<Material> getLowStockMaterials() {
        try {
            return materialService.getLowStockMaterials();
        } catch (Exception e) {
            System.err.println("Error getting low stock materials: " + e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách nguyên liệu sắp hết", e);
        }
    }
    
    /**
     * Get out of stock materials
     */
    public List<Material> getOutOfStockMaterials() {
        try {
            return materialService.getOutOfStockMaterials();
        } catch (Exception e) {
            System.err.println("Error getting out of stock materials: " + e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách nguyên liệu hết hàng", e);
        }
    }
    
    // ==================== STOCK LOGS ====================
    
    /**
     * Get all stock logs
     */
    public List<StockLog> getAllStockLogs() {
        try {
            return stockLogDAO.getAll();
        } catch (Exception e) {
            System.err.println("Error getting all stock logs: " + e.getMessage());
            throw new RuntimeException("Không thể lấy lịch sử giao dịch", e);
        }
    }
    
    /**
     * Get filtered stock logs
     */
    public List<StockLog> getFilteredStockLogs(String searchKeyword, String materialName,
                                             StockLog.ChangeType changeType, 
                                             LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<StockLog> allLogs = stockLogDAO.getAll();
            
            return allLogs.stream()
                .filter(log -> {
                    // Search keyword filter
                    if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                        String keyword = searchKeyword.toLowerCase();
                        boolean matchesKeyword = 
                            (log.getMaterialName() != null && log.getMaterialName().toLowerCase().contains(keyword)) ||
                            (log.getNote() != null && log.getNote().toLowerCase().contains(keyword)) ||
                            (log.getCreatedByName() != null && log.getCreatedByName().toLowerCase().contains(keyword));
                        if (!matchesKeyword) return false;
                    }
                    
                    // Material name filter
                    if (materialName != null && !materialName.trim().isEmpty()) {
                        if (log.getMaterialName() == null || !log.getMaterialName().equals(materialName)) {
                            return false;
                        }
                    }
                    
                    // Change type filter
                    if (changeType != null) {
                        if (log.getChangeType() != changeType) {
                            return false;
                        }
                    }
                    
                    // Date range filter
                    if (startDate != null && log.getCreatedAt() != null) {
                        if (log.getCreatedAt().isBefore(startDate)) {
                            return false;
                        }
                    }
                    
                    if (endDate != null && log.getCreatedAt() != null) {
                        if (log.getCreatedAt().isAfter(endDate)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("Error getting filtered stock logs: " + e.getMessage());
            throw new RuntimeException("Không thể lọc lịch sử giao dịch", e);
        }
    }
    
    /**
     * Get stock logs by material
     */
    public List<StockLog> getStockLogsByMaterial(int materialId) {
        try {
            if (materialId <= 0) {
                throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
            }
            
            return stockLogDAO.getByMaterialId(materialId);
        } catch (Exception e) {
            System.err.println("Error getting stock logs by material: " + e.getMessage());
            throw new RuntimeException("Không thể lấy lịch sử giao dịch của nguyên liệu", e);
        }
    }
    
    // ==================== MATERIAL IMPORTS ====================
    
    /**
     * Get all material imports
     */
    public List<MaterialImport> getAllMaterialImports() {
        try {
            return materialImportDAO.getAll();
        } catch (Exception e) {
            System.err.println("Error getting all material imports: " + e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách phiếu nhập", e);
        }
    }
    
    /**
     * Get recent material imports
     */
    public List<MaterialImport> getRecentMaterialImports(int limit) {
        try {
            return materialImportDAO.getRecent(limit);
        } catch (Exception e) {
            System.err.println("Error getting recent material imports: " + e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách phiếu nhập gần đây", e);
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Check if material name exists (for validation)
     */
    public boolean isMaterialNameExists(String name, int excludeId) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return false;
            }
            
            List<Material> materials = materialService.getAllActiveMaterials();
            return materials.stream()
                .anyMatch(m -> m.getId() != excludeId && 
                         m.getName().trim().equalsIgnoreCase(name.trim()));
        } catch (Exception e) {
            System.err.println("Error checking material name existence: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Calculate total inventory value
     */
    public BigDecimal getTotalInventoryValue() {
        try {
            List<Material> materials = materialService.getAllActiveMaterials();
            return materials.stream()
                .map(m -> m.getQuantity().multiply(m.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            System.err.println("Error calculating total inventory value: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Get inventory statistics
     */
    public Map<String, Object> getInventoryStatistics() {
        try {
            return inventoryService.getInventoryDashboard(); // Use existing method
        } catch (Exception e) {
            System.err.println("Error getting inventory statistics: " + e.getMessage());
            throw new RuntimeException("Không thể lấy thống kê kho hàng", e);
        }
    }
    public Map<String, Object> createMaterialImport(List<ImportItem> items, String supplier, 
                                               String invoiceNumber, String notes) {
        Map<String, Object> result = new HashMap<>();
        int createdBy = Session.getInstance().getCurrentUserId();
        List<String> errors = new ArrayList<>();
        List<MaterialImport> createdImports = new ArrayList<>();

        try {
            // Validation logic giữ nguyên...
            validateImportItems(items, supplier);

            // Process each import item
            for (ImportItem item : items) {
                try {
                    // Get material
                    Material material = materialService.getMaterialById(item.getMaterialId());
                    if (material == null) {
                        throw new RuntimeException("Không tìm thấy nguyên liệu: " + item.getMaterialName());
                    }

                    // Create MaterialImport object
                    MaterialImport materialImport = new MaterialImport();
                    materialImport.setMaterialId(item.getMaterialId());
                    materialImport.setImportQuantity(item.getQuantity());
                    materialImport.setImportPrice(item.getTotalPrice());
                    materialImport.setPricePerUnit(item.getUnitPrice());
                    materialImport.setSupplierName(supplier);
                    materialImport.setNote(notes);
                    materialImport.setCreatedBy(createdBy);
                    materialImport.setImportDate(LocalDateTime.now());
                    materialImport.setCreatedAt(LocalDateTime.now());
                    materialImport.setUpdatedAt(LocalDateTime.now());

                    // *** BƯỚC 1: SAVE IMPORT RECORD TRƯỚC ***
                    boolean importSaved = materialImportDAO.insert(materialImport);
                    if (!importSaved) {
                        throw new RuntimeException("Không thể lưu phiếu nhập cho: " + item.getMaterialName());
                    }

                    // *** KIỂM TRA ID ĐÃ ĐƯỢC SET CHƯA ***
                    if (materialImport.getId() <= 0) {
                        throw new RuntimeException("Lỗi: ID phiếu nhập không được tạo đúng");
                    }

                    // *** BƯỚC 2: UPDATE MATERIAL QUANTITY ***
                    BigDecimal quantityBefore = material.getQuantity();
                    BigDecimal quantityAfter = quantityBefore.add(item.getQuantity());

                    material.setQuantity(quantityAfter);
                    material.setPricePerUnit(item.getUnitPrice()); // Update latest price
                    material.setUpdatedAt(LocalDateTime.now());

                    boolean materialUpdated = materialService.updateMaterial(material);
                    if (!materialUpdated) {
                        throw new RuntimeException("Không thể cập nhật số lượng cho: " + item.getMaterialName());
                    }

                    // *** BƯỚC 3: CREATE STOCK LOG VỚI ID ĐÚNG ***
                    boolean stockLogCreated = stockLogDAO.logImport(
                        item.getMaterialId(),
                        item.getQuantity(),
                        quantityBefore,
                        quantityAfter,
                        materialImport.getId(),  // ← GIỜ ĐÂY SẼ CÓ ID ĐÚNG
                        createdBy,
                        "Nhập kho từ " + supplier + 
                        (invoiceNumber != null ? " - Hóa đơn: " + invoiceNumber : "") +
                        (notes != null ? " - " + notes : "")
                    );

                    if (!stockLogCreated) {
                        System.err.println("WARNING: Không thể tạo stock log cho MaterialImport ID: " + 
                                         materialImport.getId());
                        // Log thêm thông tin debug
                        System.err.println("Debug info - MaterialID: " + item.getMaterialId() + 
                                         ", Quantity: " + item.getQuantity() + 
                                         ", ImportID: " + materialImport.getId());
                    }

                    createdImports.add(materialImport);

                } catch (Exception e) {
                    String error = "Lỗi xử lý " + item.getMaterialName() + ": " + e.getMessage();
                    errors.add(error);
                    System.err.println(error);
                    e.printStackTrace();
                }
            }

            // Prepare result
            result.put("success", errors.isEmpty());
            result.put("createdImports", createdImports);
            result.put("errors", errors);
            result.put("message", errors.isEmpty() ? 
                "Nhập kho thành công " + createdImports.size() + " mặt hàng" :
                "Có " + errors.size() + " lỗi trong quá trình nhập kho");

            return result;

        } catch (Exception e) {
            System.err.println("Error in createMaterialImport: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Lỗi hệ thống: " + e.getMessage());
            result.put("errors", List.of(e.getMessage()));
            return result;
        }
    }
}