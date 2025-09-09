/*
 * Service class cho quản lý kho tổng hợp (Inventory Management)
 * Kết hợp Material, StockLog và các chức năng inventory khác
 */
package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.dao.MaterialDAO;
import com.mycompany.quanlyquanan.dao.StockLogDAO;
import com.mycompany.quanlyquanan.dao.MaterialImportDAO;
import com.mycompany.quanlyquanan.dao.RecipeDAO;
import com.mycompany.quanlyquanan.dao.DishDAO;
import com.mycompany.quanlyquanan.model.Material;
import com.mycompany.quanlyquanan.model.StockLog;
import com.mycompany.quanlyquanan.model.MaterialImport;
import com.mycompany.quanlyquanan.utils.Session;
import com.mycompany.quanlyquanan.model.Recipe;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class cho quản lý kho tổng hợp
 * @author Admin
 */
public class InventoryService {
    
    private final MaterialDAO materialDAO;
    private final StockLogDAO stockLogDAO;
    private final MaterialImportDAO materialImportDAO;
    private final RecipeDAO recipeDAO;
    private final DishDAO dishDAO;

    public InventoryService() {
        this.materialDAO = new MaterialDAO();
        this.stockLogDAO = new StockLogDAO();
        this.materialImportDAO = new MaterialImportDAO();
        this.recipeDAO = new RecipeDAO();
        this.dishDAO = new DishDAO();
    }

    public InventoryService(MaterialDAO materialDAO, StockLogDAO stockLogDAO, MaterialImportDAO materialImportDAO, RecipeDAO recipeDAO, DishDAO dishDAO) {
        this.materialDAO = materialDAO;
        this.stockLogDAO = stockLogDAO;
        this.materialImportDAO = materialImportDAO;
        this.recipeDAO = recipeDAO;
        this.dishDAO = dishDAO;
    }

    // ==================== INVENTORY OVERVIEW ====================
    /**
    * Lấy tất cả stock logs
    */
    public List<StockLog> getAllStockLogs() {
        try {
            return stockLogDAO.getAll();
        } catch (Exception e) {
            System.err.println("Error getting all stock logs: " + e.getMessage());
            throw new RuntimeException("Không thể lấy lịch sử giao dịch kho", e);
        }
    }
    /**
    * Lấy stock logs có lọc
    */
    public List<StockLog> getFilteredStockLogs(String searchKeyword, String materialName,
                                             StockLog.ChangeType changeType, 
                                             LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<StockLog> allLogs = stockLogDAO.getAll();

            return allLogs.stream()
                .filter(log -> {
                    // Lọc theo từ khóa tìm kiếm
                    if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                        String keyword = searchKeyword.toLowerCase().trim();
                        boolean matchesKeyword = 
                            (log.getMaterialName() != null && log.getMaterialName().toLowerCase().contains(keyword)) ||
                            (log.getNote() != null && log.getNote().toLowerCase().contains(keyword)) ||
                            (log.getCreatedByName() != null && log.getCreatedByName().toLowerCase().contains(keyword));

                        if (!matchesKeyword) return false;
                    }

                    // Lọc theo tên nguyên liệu
                    if (materialName != null && !materialName.trim().isEmpty()) {
                        if (log.getMaterialName() == null || !log.getMaterialName().equals(materialName)) {
                            return false;
                        }
                    }

                    // Lọc theo loại thay đổi
                    if (changeType != null) {
                        if (log.getChangeType() != changeType) {
                            return false;
                        }
                    }

                    // Lọc theo ngày bắt đầu
                    if (startDate != null && log.getCreatedAt() != null) {
                        if (log.getCreatedAt().isBefore(startDate)) {
                            return false;
                        }
                    }

                    // Lọc theo ngày kết thúc
                    if (endDate != null && log.getCreatedAt() != null) {
                        if (log.getCreatedAt().isAfter(endDate)) {
                            return false;
                        }
                    }

                    return true;
                })
                .sorted((log1, log2) -> {
                    // Sắp xếp theo thời gian tạo (mới nhất trước)
                    if (log1.getCreatedAt() == null && log2.getCreatedAt() == null) return 0;
                    if (log1.getCreatedAt() == null) return 1;
                    if (log2.getCreatedAt() == null) return -1;
                    return log2.getCreatedAt().compareTo(log1.getCreatedAt());
                })
                .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting filtered stock logs: " + e.getMessage());
            throw new RuntimeException("Không thể lọc lịch sử giao dịch", e);
        }
    }

   /**
    * Lấy tất cả nguyên liệu đang hoạt động
    */
    public List<Material> getAllActiveMaterials() {
        try {
            return materialDAO.getAll().stream()
                .filter(Material::isActive)
                .sorted((m1, m2) -> {
                    // Sắp xếp theo tên
                    if (m1.getName() == null && m2.getName() == null) return 0;
                    if (m1.getName() == null) return 1;
                    if (m2.getName() == null) return -1;
                    return m1.getName().compareToIgnoreCase(m2.getName());
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting all active materials: " + e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách nguyên liệu đang hoạt động", e);
        }
    }
    /**
     * Lấy tổng quan tình trạng kho
     */
    public InventoryOverview getInventoryOverview() {
        List<Material> allMaterials = materialDAO.getAllActive();
        
        long totalMaterials = allMaterials.size();
        long lowStockCount = materialDAO.countLowStock();
        long outOfStockCount = materialDAO.countOutOfStock();
        BigDecimal totalValue = materialDAO.getTotalInventoryValue();
        
        /*
        // Tính toán categories
        Map<String, Long> categoryCount = allMaterials.stream()
            .collect(Collectors.groupingBy(
                material -> material.getCategory() != null ? material.getCategory() : "Khác",
                Collectors.counting()
            ));
        */
        // Top materials by value
        List<Material> topValueMaterials = allMaterials.stream()
            .sorted((m1, m2) -> {
                BigDecimal value1 = m1.getQuantity().multiply(m1.getUnitPrice());
                BigDecimal value2 = m2.getQuantity().multiply(m2.getUnitPrice());
                return value2.compareTo(value1);
            })
            .limit(10)
            .collect(Collectors.toList());
        
        return new InventoryOverview(
            totalMaterials, lowStockCount, outOfStockCount, totalValue,
             topValueMaterials
        );
    }

    /**
     * Lấy dashboard summary cho kho
     */
    public Map<String, Object> getInventoryDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Basic stats
        dashboard.put("total_materials", materialDAO.count());
        dashboard.put("active_materials", materialDAO.countActive());
        dashboard.put("low_stock_count", materialDAO.countLowStock());
        dashboard.put("out_of_stock_count", materialDAO.countOutOfStock());
        dashboard.put("total_inventory_value", materialDAO.getTotalInventoryValue());
        
        // Recent activities
        dashboard.put("recent_stock_logs", stockLogDAO.getWithPagination(0, 10));
        dashboard.put("recent_imports", materialImportDAO.getRecent(10));
        
        // Alerts
        dashboard.put("low_stock_materials", materialDAO.getLowStockMaterials());
        dashboard.put("expired_materials", getExpiringMaterials(30)); // Materials expiring in 30 days
        
        // Statistics by period
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        
        dashboard.put("week_import_count", materialImportDAO.countByDateRange(weekStart, LocalDateTime.now()));
        dashboard.put("month_import_count", materialImportDAO.countByDateRange(monthStart, LocalDateTime.now()));
        dashboard.put("week_export_value", getExportValueInPeriod(weekStart, LocalDateTime.now()));
        dashboard.put("month_export_value", getExportValueInPeriod(monthStart, LocalDateTime.now()));
        
        return dashboard;
    }

    // ==================== STOCK OPERATIONS ====================

    /**
     * Nhập kho với logging đầy đủ
     */
    public boolean importStock(int materialId, BigDecimal quantity, BigDecimal unitCost,
                             String supplier, int importId, int userId, String note) {
        validateStockOperation(materialId, quantity, "nhập");
        
        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        BigDecimal quantityBefore = material.getQuantity();
        BigDecimal quantityAfter = quantityBefore.add(quantity);
        
        try {
            // Cập nhật số lượng
            boolean success = materialDAO.addQuantity(materialId, quantity);
            
            if (success) {
                BigDecimal avgPrice = materialImportDAO.getAveragePricePerUnitByMaterialId(materialId);
                materialDAO.updateUnitPrice(materialId, avgPrice);

                // cập nhật giá vốn các món liên quan
                List<Recipe> recipes = recipeDAO.getByMaterialId(materialId);
                for (int dishId : recipes.stream().map(Recipe::getDishId).collect(Collectors.toSet())) {
                    BigDecimal totalCost = recipeDAO.getTotalCostByDishId(dishId);
                    dishDAO.updateCostPrice(dishId, totalCost);
                }
                

                // Ghi log
                stockLogDAO.logImport(materialId, quantity, quantityBefore, quantityAfter,
                                    importId, userId, note);               

                System.out.println("Nhập kho thành công: " + material.getName() +
                                 " - Số lượng: " + quantity + " " + material.getUnit());
            }
            
            return success;
        } catch (Exception e) {
            System.err.println("Lỗi khi nhập kho: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xuất kho với logging đầy đủ
     */
    public boolean exportStock(int materialId, BigDecimal quantity, int userId, String note) {
        validateStockOperation(materialId, quantity, "xuất");
        
        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        // Kiểm tra đủ số lượng
        if (material.getQuantity().compareTo(quantity) < 0) {
            throw new IllegalArgumentException("Không đủ số lượng để xuất. " +
                "Tồn kho hiện tại: " + material.getQuantityWithUnit());
        }
        
        BigDecimal quantityBefore = material.getQuantity();
        BigDecimal quantityAfter = quantityBefore.subtract(quantity);
        
        try {
            boolean success = materialDAO.subtractQuantity(materialId, quantity);
            
            if (success) {
                // Ghi log
                stockLogDAO.logExport(materialId, quantity, quantityBefore, quantityAfter, 
                                    userId, note);
                
                System.out.println("Xuất kho thành công: " + material.getName() + 
                                 " - Số lượng: " + quantity + " " + material.getUnit());
            }
            
            return success;
        } catch (Exception e) {
            System.err.println("Lỗi khi xuất kho: " + e.getMessage());
            return false;
        }
    }

    /**
     * Điều chỉnh tồn kho với logging
     */
    public boolean adjustStock(int materialId, BigDecimal newQuantity, String reason, int userId) {
        if (!Session.getInstance().canManageInventory()) {
            throw new SecurityException("Không có quyền điều chỉnh tồn kho");
        }
        
        validateStockOperation(materialId, newQuantity, "điều chỉnh");
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do điều chỉnh không được để trống");
        }
        
        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        BigDecimal quantityBefore = material.getQuantity();
        BigDecimal quantityChange = newQuantity.subtract(quantityBefore);
        
        try {
            boolean success = materialDAO.updateQuantity(materialId, newQuantity);
            
            if (success) {
                // Ghi log
                stockLogDAO.logAdjust(materialId, quantityChange, quantityBefore, 
                                    newQuantity, userId, reason);
                
                System.out.println("Điều chỉnh tồn kho thành công: " + material.getName() + 
                                 " - Từ: " + quantityBefore + " sang: " + newQuantity + " " + material.getUnit());
            }
            
            return success;
        } catch (Exception e) {
            System.err.println("Lỗi khi điều chỉnh tồn kho: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tiêu thụ nguyên liệu khi làm món ăn
     */
    public boolean consumeMaterialsForDish(int dishId, int dishQuantity, int orderId, int userId) {
        if (dishId <= 0 || dishQuantity <= 0) {
            throw new IllegalArgumentException("Thông tin món ăn không hợp lệ");
        }
        
        // Lấy công thức món ăn
        var recipes = recipeDAO.getByDishId(dishId);
        if (recipes.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy công thức cho món ăn này");
        }
        
        // Kiểm tra đủ nguyên liệu trước
        List<String> insufficientMaterials = new ArrayList<>();
        for (var recipe : recipes) {
            BigDecimal requiredQuantity = recipe.getQuantity().multiply(BigDecimal.valueOf(dishQuantity));
            Material material = materialDAO.getById(recipe.getMaterialId());
            
            if (material == null || material.getQuantity().compareTo(requiredQuantity) < 0) {
                insufficientMaterials.add(recipe.getMaterialName() + 
                    " (cần: " + requiredQuantity + ", còn: " + 
                    (material != null ? material.getQuantity() : "0") + ")");
            }
        }
        
        if (!insufficientMaterials.isEmpty()) {
            throw new IllegalArgumentException("Không đủ nguyên liệu: " + 
                String.join(", ", insufficientMaterials));
        }
        
        // Tiêu thụ nguyên liệu
        boolean allSuccess = true;
        for (var recipe : recipes) {
            BigDecimal requiredQuantity = recipe.getQuantity().multiply(BigDecimal.valueOf(dishQuantity));
            Material material = materialDAO.getById(recipe.getMaterialId());
            
            BigDecimal quantityBefore = material.getQuantity();
            BigDecimal quantityAfter = quantityBefore.subtract(requiredQuantity);
            
            boolean success = materialDAO.subtractQuantity(recipe.getMaterialId(), requiredQuantity);
            
            if (success) {
                // Ghi log tiêu thụ
                String note = "Làm món: " + recipe.getDishName() + " (x" + dishQuantity + ")";
                stockLogDAO.logConsume(recipe.getMaterialId(), requiredQuantity, 
                                     quantityBefore, quantityAfter, orderId, userId, note);
            } else {
                allSuccess = false;
                System.err.println("Lỗi khi tiêu thụ nguyên liệu: " + recipe.getMaterialName());
                // TODO: Implement rollback mechanism
            }
        }
        
        return allSuccess;
    }


    // ==================== INVENTORY ANALYSIS ====================

    /**
     * Phân tích ABC cho nguyên liệu (dựa trên giá trị tồn kho)
     */
    public Map<String, List<Material>> getABCAnalysis() {
        List<Material> materials = materialDAO.getAllActive();
        
        // Tính tổng giá trị
        BigDecimal totalValue = materials.stream()
            .map(m -> m.getQuantity().multiply(m.getUnitPrice()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Sắp xếp theo giá trị giảm dần
        materials.sort((m1, m2) -> {
            BigDecimal value1 = m1.getQuantity().multiply(m1.getUnitPrice());
            BigDecimal value2 = m2.getQuantity().multiply(m2.getUnitPrice());
            return value2.compareTo(value1);
        });
        
        Map<String, List<Material>> abcGroups = new HashMap<>();
        abcGroups.put("A", new ArrayList<>());
        abcGroups.put("B", new ArrayList<>());
        abcGroups.put("C", new ArrayList<>());
        
        BigDecimal cumulativeValue = BigDecimal.ZERO;
        for (Material material : materials) {
            BigDecimal materialValue = material.getQuantity().multiply(material.getUnitPrice());
            cumulativeValue = cumulativeValue.add(materialValue);
            
            double percentage = cumulativeValue.divide(totalValue, 4, BigDecimal.ROUND_HALF_UP)
                .doubleValue() * 100;
            
            if (percentage <= 80) {
                abcGroups.get("A").add(material);
            } else if (percentage <= 95) {
                abcGroups.get("B").add(material);
            } else {
                abcGroups.get("C").add(material);
            }
        }
        
        return abcGroups;
    }


    /**
     * Phân tích xu hướng tồn kho
     */
    public Map<String, Object> getStockTrendAnalysis(int materialId, int days) {
        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<StockLog> logs = stockLogDAO.getByMaterialId(materialId);
        
        // Filter logs within date range
        List<StockLog> filteredLogs = logs.stream()
            .filter(log -> log.getCreatedAt().isAfter(startDate))
            .sorted(Comparator.comparing(StockLog::getCreatedAt))
            .collect(Collectors.toList());
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("material", material);
        analysis.put("period_days", days);
        analysis.put("total_changes", filteredLogs.size());
        
        if (!filteredLogs.isEmpty()) {
            BigDecimal totalIncrease = filteredLogs.stream()
                .filter(log -> log.getQuantityChange().compareTo(BigDecimal.ZERO) > 0)
                .map(StockLog::getQuantityChange)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalDecrease = filteredLogs.stream()
                .filter(log -> log.getQuantityChange().compareTo(BigDecimal.ZERO) < 0)
                .map(log -> log.getQuantityChange().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            analysis.put("total_increase", totalIncrease);
            analysis.put("total_decrease", totalDecrease);
            analysis.put("net_change", totalIncrease.subtract(totalDecrease));
            analysis.put("starting_quantity", filteredLogs.get(0).getQuantityBefore());
            analysis.put("ending_quantity", material.getQuantity());
            analysis.put("daily_average_change", 
                totalIncrease.subtract(totalDecrease).divide(BigDecimal.valueOf(days), 2, BigDecimal.ROUND_HALF_UP));
        }
        
        analysis.put("change_history", filteredLogs);
        
        return analysis;
    }

    /**
     * Dự đoán nhu cầu nguyên liệu
     */
    public Map<String, Object> predictMaterialDemand(int materialId, int forecastDays) {
        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        // Lấy dữ liệu tiêu thụ 30 ngày qua
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        List<StockLog> consumptionLogs = stockLogDAO.getByMaterialId(materialId).stream()
            .filter(log -> log.getCreatedAt().isAfter(startDate))
            .filter(log -> log.getChangeType() == StockLog.ChangeType.CONSUME)
            .collect(Collectors.toList());
        
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("material", material);
        prediction.put("forecast_days", forecastDays);
        prediction.put("current_stock", material.getQuantity());
        
        if (consumptionLogs.isEmpty()) {
            prediction.put("average_daily_consumption", BigDecimal.ZERO);
            prediction.put("predicted_consumption", BigDecimal.ZERO);
            prediction.put("stock_out_date", null);
            prediction.put("recommendation", "Không có dữ liệu tiêu thụ để dự đoán");
        } else {
            BigDecimal totalConsumption = consumptionLogs.stream()
                .map(log -> log.getQuantityChange().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal averageDailyConsumption = totalConsumption.divide(
                BigDecimal.valueOf(30), 2, BigDecimal.ROUND_HALF_UP);
            
            BigDecimal predictedConsumption = averageDailyConsumption.multiply(
                BigDecimal.valueOf(forecastDays));
            
            prediction.put("average_daily_consumption", averageDailyConsumption);
            prediction.put("predicted_consumption", predictedConsumption);
            
            // Dự đoán ngày hết hàng
            if (averageDailyConsumption.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal daysUntilStockOut = material.getQuantity().divide(
                    averageDailyConsumption, 0, BigDecimal.ROUND_UP);
                
                LocalDateTime stockOutDate = LocalDateTime.now().plusDays(daysUntilStockOut.longValue());
                prediction.put("stock_out_date", stockOutDate);
                prediction.put("days_until_stock_out", daysUntilStockOut.intValue());
                
                // Khuyến nghị
                if (daysUntilStockOut.intValue() <= 7) {
                    prediction.put("recommendation", "CẦN NHẬP KHẨN CẤP - Sẽ hết hàng trong " + 
                        daysUntilStockOut.intValue() + " ngày");
                } else if (daysUntilStockOut.intValue() <= 14) {
                    prediction.put("recommendation", "Nên nhập hàng sớm - Sẽ hết hàng trong " + 
                        daysUntilStockOut.intValue() + " ngày");
                } else {
                    prediction.put("recommendation", "Tình trạng tồn kho ổn định");
                }
            } else {
                prediction.put("stock_out_date", null);
                prediction.put("recommendation", "Không có tiêu thụ gần đây");
            }
        }
        
        return prediction;
    }

    // ==================== ALERTS & NOTIFICATIONS ====================

    /**
     * Lấy danh sách cảnh báo kho
     */
    public List<InventoryAlert> getInventoryAlerts() {
        List<InventoryAlert> alerts = new ArrayList<>();
        
        // Cảnh báo hết hàng - sử dụng method từ materialDAO
        List<Material> outOfStock = materialDAO.getOutOfStockMaterials();
        for (Material material : outOfStock) {
            alerts.add(new InventoryAlert(
                InventoryAlert.AlertType.OUT_OF_STOCK,
                InventoryAlert.Priority.HIGH,
                "Nguyên liệu hết hàng: " + material.getName(),
                material.getId()
            ));
        }
        
        // Cảnh báo sắp hết hàng
        List<Material> lowStock = materialDAO.getLowStockMaterials();
        for (Material material : lowStock) {
            if (!outOfStock.contains(material)) { // Tránh trùng lặp
                alerts.add(new InventoryAlert(
                    InventoryAlert.AlertType.LOW_STOCK,
                    InventoryAlert.Priority.MEDIUM,
                    "Nguyên liệu sắp hết: " + material.getName() + 
                    " (còn " + material.getQuantityWithUnit() + ")",
                    material.getId()
                ));
            }
        }
        
        // Cảnh báo sắp hết hạn
        List<Material> expiring = getExpiringMaterials(7);
        for (Material material : expiring) {
            alerts.add(new InventoryAlert(
                InventoryAlert.AlertType.EXPIRING_SOON,
                InventoryAlert.Priority.MEDIUM,
                "Nguyên liệu sắp hết hạn: " + material.getName(),
                material.getId()
            ));
        }
        
        // Sắp xếp theo độ ưu tiên
        alerts.sort((a1, a2) -> a2.getPriority().compareTo(a1.getPriority()));
        
        return alerts;
    }

    /**
     * Kiểm tra tình trạng kho và gửi thông báo
     */
    public void checkInventoryStatus() {
        List<InventoryAlert> alerts = getInventoryAlerts();
        
        for (InventoryAlert alert : alerts) {
            if (alert.getPriority() == InventoryAlert.Priority.HIGH) {
                // TODO: Gửi notification khẩn cấp
                System.out.println("CẢNH BÁO KHẨN CẤP: " + alert.getMessage());
            }
        }
        
        // Log tình trạng tổng quan
        InventoryOverview overview = getInventoryOverview();
        System.out.println("=== BÁO CÁO TÌNH TRẠNG KHO ===");
        System.out.println("Tổng nguyên liệu: " + overview.getTotalMaterials());
        System.out.println("Sắp hết hàng: " + overview.getLowStockCount());
        System.out.println("Hết hàng: " + overview.getOutOfStockCount());
        System.out.println("Tổng giá trị kho: " + overview.getTotalValue());
        System.out.println("Số cảnh báo: " + alerts.size());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Validate các thao tác kho
     */
    private void validateStockOperation(int materialId, BigDecimal quantity, String operation) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }
        
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số lượng " + operation + " phải lớn hơn 0");
        }
        
        if (!Session.getInstance().canManageInventory()) {
            throw new SecurityException("Không có quyền " + operation + " kho");
        }
    }

    /**
     * Lấy nguyên liệu sắp hết hạn
     */
    private List<Material> getExpiringMaterials(int days) {
        // TODO: Implement based on expiry date field in Material model
        // For now, return empty list
        return new ArrayList<>();
    }

    /**
     * Lấy số lượng nhập kho trong khoảng thời gian
     */
    private long getImportCountInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return materialImportDAO.countByDateRange(startDate, endDate);
    }

    /**
     * Lấy giá trị xuất kho trong khoảng thời gian
     */
    private BigDecimal getExportValueInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        // Calculate export value from stock logs
        List<StockLog> exportLogs = stockLogDAO.getAll().stream()
            .filter(log -> log.getChangeType() == StockLog.ChangeType.EXPORT)
            .filter(log -> log.getCreatedAt().isAfter(startDate) && log.getCreatedAt().isBefore(endDate))
            .collect(Collectors.toList());
        
        BigDecimal totalValue = BigDecimal.ZERO;
        for (StockLog log : exportLogs) {
            Material material = materialDAO.getById(log.getMaterialId());
            if (material != null) {
                BigDecimal value = log.getQuantityChange().abs().multiply(material.getUnitPrice());
                totalValue = totalValue.add(value);
            }
        }
        
        return totalValue;
    }

    // ==================== INNER CLASSES ====================

    /**
     * Class cho tổng quan kho
     */
    public static class InventoryOverview {
        private final long totalMaterials;
        private final long lowStockCount;
        private final long outOfStockCount;
        private final BigDecimal totalValue;
        //private final Map<String, Long> categoryCount;
        private final List<Material> topValueMaterials;

        public InventoryOverview(long totalMaterials, long lowStockCount, long outOfStockCount, BigDecimal totalValue, List<Material> topValueMaterials) {
            this.totalMaterials = totalMaterials;
            this.lowStockCount = lowStockCount;
            this.outOfStockCount = outOfStockCount;
            this.totalValue = totalValue;
            //this.categoryCount = categoryCount;
            this.topValueMaterials = topValueMaterials;
        }

        // Getters
        public long getTotalMaterials() { return totalMaterials; }
        public long getLowStockCount() { return lowStockCount; }
        public long getOutOfStockCount() { return outOfStockCount; }
        public BigDecimal getTotalValue() { return totalValue; }
       // public Map<String, Long> getCategoryCount() { return categoryCount; }
        public List<Material> getTopValueMaterials() { return topValueMaterials; }
        
        public double getLowStockPercentage() {
            return totalMaterials > 0 ? (double) lowStockCount / totalMaterials * 100 : 0;
        }
        
        public double getOutOfStockPercentage() {
            return totalMaterials > 0 ? (double) outOfStockCount / totalMaterials * 100 : 0;
        }
    }

    /**
     * Class cho cảnh báo kho
     */
    public static class InventoryAlert {
        public enum AlertType {
            OUT_OF_STOCK, LOW_STOCK, EXPIRING_SOON, OVERSTOCK, PRICE_CHANGE
        }
        
        public enum Priority {
            LOW, MEDIUM, HIGH, CRITICAL
        }
        
        private final AlertType alertType;
        private final Priority priority;
        private final String message;
        private final int materialId;
        private final LocalDateTime createdAt;

        public InventoryAlert(AlertType alertType, Priority priority, String message, int materialId) {
            this.alertType = alertType;
            this.priority = priority;
            this.message = message;
            this.materialId = materialId;
            this.createdAt = LocalDateTime.now();
        }

        // Getters
        public AlertType getAlertType() { return alertType; }
        public Priority getPriority() { return priority; }
        public String getMessage() { return message; }
        public int getMaterialId() { return materialId; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}