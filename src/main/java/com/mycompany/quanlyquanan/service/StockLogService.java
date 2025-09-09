/*
 * Service class cho StockLog - Quản lý lịch sử xuất nhập kho
 * Xử lý business logic liên quan đến tracking và phân tích stock movements
 */
package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.dao.StockLogDAO;
import com.mycompany.quanlyquanan.dao.MaterialDAO;
import com.mycompany.quanlyquanan.dao.EmployeeDAO;
import com.mycompany.quanlyquanan.model.StockLog;
import com.mycompany.quanlyquanan.model.Material;
import com.mycompany.quanlyquanan.model.Employee;
import com.mycompany.quanlyquanan.utils.Session;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class cho StockLog - Quản lý lịch sử xuất nhập kho
 * @author Admin
 */
public class StockLogService {
    
    private final StockLogDAO stockLogDAO;
    private final MaterialDAO materialDAO;
    private final EmployeeDAO employeeDAO;

    public StockLogService() {
        this.stockLogDAO = new StockLogDAO();
        this.materialDAO = new MaterialDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    public StockLogService(StockLogDAO stockLogDAO, MaterialDAO materialDAO, EmployeeDAO employeeDAO) {
        this.stockLogDAO = stockLogDAO;
        this.materialDAO = materialDAO;
        this.employeeDAO = employeeDAO;
    }

    // ==================== BASIC CRUD OPERATIONS ====================

    /**
     * Lấy tất cả stock logs
     */
    public List<StockLog> getAllStockLogs() {
        return stockLogDAO.getAll();
    }

    /**
     * Lấy stock logs với phân trang
     */
    public List<StockLog> getStockLogsWithPagination(int page, int pageSize) {
        if (page < 1 || pageSize < 1) {
            throw new IllegalArgumentException("Page và pageSize phải lớn hơn 0");
        }
        
        int offset = (page - 1) * pageSize;
        return stockLogDAO.getWithPagination(offset, pageSize);
    }

    /**
     * Lấy stock logs theo material ID
     */
    public List<StockLog> getStockLogsByMaterial(int materialId) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("Material ID không hợp lệ");
        }
        
        return stockLogDAO.getByMaterialId(materialId);
    }

    /**
     * Tìm kiếm stock logs
     */
    public List<StockLog> searchStockLogs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStockLogs();
        }
        
        return stockLogDAO.search(keyword.trim());
    }

    /**
     * Lấy stock log theo ID
     */
    public StockLog getStockLogById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID không hợp lệ");
        }
        
        return stockLogDAO.getById(id);
    }

    // ==================== LOGGING OPERATIONS ====================

    /**
     * Ghi log nhập kho
     */
    public boolean logImport(int materialId, BigDecimal quantity, BigDecimal quantityBefore,
                           BigDecimal quantityAfter, int importId, int userId, String note) {
        validateLogParameters(materialId, quantity, quantityBefore, quantityAfter, userId);
        
        return stockLogDAO.logImport(materialId, quantity, quantityBefore, quantityAfter, 
                                   importId, userId, note);
    }

    /**
     * Ghi log xuất kho
     */
    public boolean logExport(int materialId, BigDecimal quantity, BigDecimal quantityBefore,
                           BigDecimal quantityAfter, int userId, String note) {
        validateLogParameters(materialId, quantity, quantityBefore, quantityAfter, userId);
        
        return stockLogDAO.logExport(materialId, quantity, quantityBefore, quantityAfter, 
                                   userId, note);
    }

    /**
     * Ghi log tiêu thụ
     */
    public boolean logConsume(int materialId, BigDecimal quantity, BigDecimal quantityBefore,
                            BigDecimal quantityAfter, int orderId, int userId, String note) {
        validateLogParameters(materialId, quantity, quantityBefore, quantityAfter, userId);
        
        return stockLogDAO.logConsume(materialId, quantity, quantityBefore, quantityAfter, 
                                    orderId, userId, note);
    }

    /**
     * Ghi log điều chỉnh
     */
    public boolean logAdjust(int materialId, BigDecimal quantityChange, BigDecimal quantityBefore,
                           BigDecimal quantityAfter, int userId, String reason) {
        validateLogParameters(materialId, quantityChange.abs(), quantityBefore, quantityAfter, userId);
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do điều chỉnh không được để trống");
        }
        
        return stockLogDAO.logAdjust(materialId, quantityChange, quantityBefore, quantityAfter, 
                                   userId, reason);
    }

    /**
     * Ghi log hoàn trả
     */
    public boolean logReturn(int materialId, BigDecimal quantity, BigDecimal quantityBefore,
                           BigDecimal quantityAfter, int orderId, int userId, String note) {
        validateLogParameters(materialId, quantity, quantityBefore, quantityAfter, userId);
        
        return stockLogDAO.logReturn(materialId, quantity, quantityBefore, quantityAfter, 
                                   orderId, userId, note);
    }

    /**
     * Ghi log hao hụt
     */
    public boolean logWaste(int materialId, BigDecimal quantity, BigDecimal quantityBefore,
                          BigDecimal quantityAfter, int userId, String reason) {
        validateLogParameters(materialId, quantity, quantityBefore, quantityAfter, userId);
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do hao hụt không được để trống");
        }
        
        return stockLogDAO.logWaste(materialId, quantity, quantityBefore, quantityAfter, 
                                  userId, reason);
    }

    // ==================== ANALYSIS & REPORTING ====================

    /**
     * Phân tích thay đổi stock theo material trong khoảng thời gian
     */
    public Map<String, Object> getMaterialStockAnalysis(int materialId, LocalDateTime startDate, LocalDateTime endDate) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("Material ID không hợp lệ");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        
        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        return stockLogDAO.getMaterialChangeStats(materialId, startDate, endDate);
    }

    /**
     * Lấy timeline hoạt động của material
     */
    public List<Map<String, Object>> getMaterialActivityTimeline(int materialId, int limit) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("Material ID không hợp lệ");
        }
        
        if (limit <= 0) {
            limit = 50; // Default limit
        }
        
        return stockLogDAO.getMaterialActivityTimeline(materialId, limit);
    }

    /**
     * Phân tích xu hướng stock movements theo ngày
     */
    public Map<String, Object> getDailyStockMovementTrend(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        
        List<StockLog> logs = stockLogDAO.getAll().stream()
            .filter(log -> log.getCreatedAt().isAfter(startDate) && log.getCreatedAt().isBefore(endDate))
            .collect(Collectors.toList());
        
        Map<String, Object> trend = new HashMap<>();
        
        // Group by date
        Map<LocalDate, List<StockLog>> dailyLogs = logs.stream()
            .collect(Collectors.groupingBy(log -> log.getCreatedAt().toLocalDate()));
        
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        
        for (Map.Entry<LocalDate, List<StockLog>> entry : dailyLogs.entrySet()) {
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", entry.getKey());
            
            List<StockLog> dayLogs = entry.getValue();
            dayStat.put("total_movements", dayLogs.size());
            
            // Count by change type
            Map<StockLog.ChangeType, Long> typeCount = dayLogs.stream()
                .collect(Collectors.groupingBy(StockLog::getChangeType, Collectors.counting()));
            
            dayStat.put("movements_by_type", typeCount);
            
            // Calculate total quantity changes
            BigDecimal totalIncrease = dayLogs.stream()
                .filter(log -> log.getQuantityChange().compareTo(BigDecimal.ZERO) > 0)
                .map(StockLog::getQuantityChange)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalDecrease = dayLogs.stream()
                .filter(log -> log.getQuantityChange().compareTo(BigDecimal.ZERO) < 0)
                .map(log -> log.getQuantityChange().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            dayStat.put("total_increase", totalIncrease);
            dayStat.put("total_decrease", totalDecrease);
            dayStat.put("net_change", totalIncrease.subtract(totalDecrease));
            
            dailyStats.add(dayStat);
        }
        
        // Sort by date
        dailyStats.sort((a, b) -> ((LocalDate) a.get("date")).compareTo((LocalDate) b.get("date")));
        
        trend.put("daily_statistics", dailyStats);
        trend.put("analysis_period", Map.of("start", startDate, "end", endDate));
        trend.put("total_days", dailyStats.size());
        
        return trend;
    }

    /**
     * Phân tích stock movements theo loại thay đổi
     */
    public Map<String, Object> getStockMovementAnalysisByType(LocalDateTime startDate, LocalDateTime endDate) {
        List<StockLog> logs = stockLogDAO.getAll().stream()
            .filter(log -> log.getCreatedAt().isAfter(startDate) && log.getCreatedAt().isBefore(endDate))
            .collect(Collectors.toList());
        
        Map<String, Object> analysis = new HashMap<>();
        
        // Group by change type
        Map<StockLog.ChangeType, List<StockLog>> logsByType = logs.stream()
            .collect(Collectors.groupingBy(StockLog::getChangeType));
        
        Map<String, Map<String, Object>> typeAnalysis = new HashMap<>();
        
        for (StockLog.ChangeType type : StockLog.ChangeType.values()) {
            List<StockLog> typeLogs = logsByType.getOrDefault(type, new ArrayList<>());
            
            Map<String, Object> typeStats = new HashMap<>();
            typeStats.put("count", typeLogs.size());
            
            if (!typeLogs.isEmpty()) {
                BigDecimal totalQuantity = typeLogs.stream()
                    .map(log -> log.getQuantityChange().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal averageQuantity = totalQuantity.divide(
                    BigDecimal.valueOf(typeLogs.size()), 2, BigDecimal.ROUND_HALF_UP);
                
                typeStats.put("total_quantity", totalQuantity);
                typeStats.put("average_quantity", averageQuantity);
                
                // Most active materials for this type
                Map<Integer, Long> materialCount = typeLogs.stream()
                    .collect(Collectors.groupingBy(StockLog::getMaterialId, Collectors.counting()));
                
                Optional<Map.Entry<Integer, Long>> mostActive = materialCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue());
                
                if (mostActive.isPresent()) {
                    Material material = materialDAO.getById(mostActive.get().getKey());
                    typeStats.put("most_active_material", 
                        material != null ? material.getName() : "Unknown");
                    typeStats.put("most_active_count", mostActive.get().getValue());
                }
            } else {
                typeStats.put("total_quantity", BigDecimal.ZERO);
                typeStats.put("average_quantity", BigDecimal.ZERO);
            }
            
            typeAnalysis.put(type.getDisplayName(), typeStats);
        }
        
        analysis.put("movement_by_type", typeAnalysis);
        analysis.put("total_movements", logs.size());
        analysis.put("analysis_period", Map.of("start", startDate, "end", endDate));
        
        return analysis;
    }

    /**
     * Lấy top materials có nhiều movements nhất
     */
    public List<Map<String, Object>> getTopMaterialsByMovements(int limit, LocalDateTime startDate, LocalDateTime endDate) {
        List<StockLog> logs = stockLogDAO.getAll().stream()
            .filter(log -> log.getCreatedAt().isAfter(startDate) && log.getCreatedAt().isBefore(endDate))
            .collect(Collectors.toList());
        
        // Group by material
        Map<Integer, List<StockLog>> logsByMaterial = logs.stream()
            .collect(Collectors.groupingBy(StockLog::getMaterialId));
        
        List<Map<String, Object>> topMaterials = new ArrayList<>();
        
        for (Map.Entry<Integer, List<StockLog>> entry : logsByMaterial.entrySet()) {
            int materialId = entry.getKey();
            List<StockLog> materialLogs = entry.getValue();
            
            Material material = materialDAO.getById(materialId);
            if (material == null) continue;
            
            Map<String, Object> materialStats = new HashMap<>();
            materialStats.put("material_id", materialId);
            materialStats.put("material_name", material.getName());
            materialStats.put("material_unit", material.getUnit());
            materialStats.put("total_movements", materialLogs.size());
            
            // Calculate total quantities by type
            Map<StockLog.ChangeType, BigDecimal> quantityByType = new HashMap<>();
            for (StockLog.ChangeType type : StockLog.ChangeType.values()) {
                BigDecimal total = materialLogs.stream()
                    .filter(log -> log.getChangeType() == type)
                    .map(log -> log.getQuantityChange().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                quantityByType.put(type, total);
            }
            
            materialStats.put("quantity_by_type", quantityByType);
            
            // Most recent activity
            Optional<StockLog> lastLog = materialLogs.stream()
                .max(Comparator.comparing(StockLog::getCreatedAt));
            
            if (lastLog.isPresent()) {
                materialStats.put("last_activity", lastLog.get().getCreatedAt());
                materialStats.put("last_activity_type", lastLog.get().getChangeType().getDisplayName());
            }
            
            topMaterials.add(materialStats);
        }
        
        // Sort by total movements and limit
        return topMaterials.stream()
            .sorted((a, b) -> Integer.compare(
                (Integer) b.get("total_movements"), 
                (Integer) a.get("total_movements")))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Lấy user activities - ai thực hiện nhiều thao tác nhất
     */
    public List<Map<String, Object>> getUserActivityRanking(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        List<StockLog> logs = stockLogDAO.getAll().stream()
            .filter(log -> log.getCreatedAt().isAfter(startDate) && log.getCreatedAt().isBefore(endDate))
            .collect(Collectors.toList());
        
        // Group by user
        Map<Integer, List<StockLog>> logsByUser = logs.stream()
            .collect(Collectors.groupingBy(StockLog::getCreatedBy));
        
        List<Map<String, Object>> userActivities = new ArrayList<>();
        
        for (Map.Entry<Integer, List<StockLog>> entry : logsByUser.entrySet()) {
            int userId = entry.getKey();
            List<StockLog> userLogs = entry.getValue();
            
            Employee employee = employeeDAO.getById(userId);
            if (employee == null) continue;
            
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("user_id", userId);
            userStats.put("user_name", employee.getName());
            userStats.put("user_position", employee.getPosition());
            userStats.put("total_activities", userLogs.size());
            
            // Activities by type
            Map<StockLog.ChangeType, Long> activitiesByType = userLogs.stream()
                .collect(Collectors.groupingBy(StockLog::getChangeType, Collectors.counting()));
            
            userStats.put("activities_by_type", activitiesByType);
            
            // Most recent activity
            Optional<StockLog> lastActivity = userLogs.stream()
                .max(Comparator.comparing(StockLog::getCreatedAt));
            
            if (lastActivity.isPresent()) {
                userStats.put("last_activity", lastActivity.get().getCreatedAt());
                userStats.put("last_activity_type", lastActivity.get().getChangeType().getDisplayName());
            }
            
            userActivities.add(userStats);
        }
        
        // Sort by total activities and limit
        return userActivities.stream()
            .sorted((a, b) -> Integer.compare(
                (Integer) b.get("total_activities"), 
                (Integer) a.get("total_activities")))
            .limit(limit)
            .collect(Collectors.toList());
    }

    // ==================== DASHBOARD & SUMMARY ====================

    /**
     * Lấy dashboard summary cho stock logs
     */
    public Map<String, Object> getStockLogDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Basic counts
        dashboard.put("total_logs", stockLogDAO.count());
        
        // Today's activities
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = todayStart.plusDays(1).minusSeconds(1);
        
        List<StockLog> todayLogs = stockLogDAO.getAll().stream()
            .filter(log -> log.getCreatedAt().isAfter(todayStart) && log.getCreatedAt().isBefore(todayEnd))
            .collect(Collectors.toList());
        
        dashboard.put("today_activities", todayLogs.size());
        
        // Today's activities by type
        Map<StockLog.ChangeType, Long> todayByType = todayLogs.stream()
            .collect(Collectors.groupingBy(StockLog::getChangeType, Collectors.counting()));
        
        dashboard.put("today_by_type", todayByType);
        
        // Recent activities
        dashboard.put("recent_activities", stockLogDAO.getWithPagination(0, 10));
        
        // Most active materials today
        if (!todayLogs.isEmpty()) {
            Map<Integer, Long> materialActivity = todayLogs.stream()
                .collect(Collectors.groupingBy(StockLog::getMaterialId, Collectors.counting()));
            
            Optional<Map.Entry<Integer, Long>> mostActive = materialActivity.entrySet().stream()
                .max(Map.Entry.comparingByValue());
            
            if (mostActive.isPresent()) {
                Material material = materialDAO.getById(mostActive.get().getKey());
                if (material != null) {
                    dashboard.put("most_active_material_today", material.getName());
                    dashboard.put("most_active_count_today", mostActive.get().getValue());
                }
            }
        }
        
        return dashboard;
    }

    /**
     * Tạo báo cáo stock movement summary
     */
    public Map<String, Object> generateStockMovementReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();
        
        List<StockLog> logs = stockLogDAO.getAll().stream()
            .filter(log -> log.getCreatedAt().isAfter(startDate) && log.getCreatedAt().isBefore(endDate))
            .collect(Collectors.toList());
        
        report.put("report_period", Map.of("start", startDate, "end", endDate));
        report.put("total_movements", logs.size());
        
        // Summary by type
        Map<StockLog.ChangeType, Long> countByType = logs.stream()
            .collect(Collectors.groupingBy(StockLog::getChangeType, Collectors.counting()));
        
        report.put("movements_by_type", countByType);
        
        // Top materials
        report.put("top_materials", getTopMaterialsByMovements(10, startDate, endDate));
        
        // User activities
        report.put("user_activities", getUserActivityRanking(startDate, endDate, 10));
        
        // Daily trend
        report.put("daily_trend", getDailyStockMovementTrend(startDate, endDate));
        
        report.put("generated_at", LocalDateTime.now());
        report.put("generated_by", Session.getInstance().getCurrentUser().getName());
        
        return report;
    }

    // ==================== UTILITY & VALIDATION ====================

    /**
     * Validate log parameters
     */
    private void validateLogParameters(int materialId, BigDecimal quantity, BigDecimal quantityBefore, 
                                     BigDecimal quantityAfter, int userId) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("Material ID không hợp lệ");
        }
        
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        
        if (quantityBefore == null || quantityBefore.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số lượng trước không hợp lệ");
        }
        
        if (quantityAfter == null || quantityAfter.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số lượng sau không hợp lệ");
        }
        
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID không hợp lệ");
        }
        
        // Verify material exists
        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        // Verify user exists
        Employee employee = employeeDAO.getById(userId);
        if (employee == null) {
            throw new IllegalArgumentException("Người dùng không tồn tại");
        }
    }

    /**
     * Cập nhật note của stock log
     */
    public boolean updateStockLogNote(int logId, String newNote) {
        if (logId <= 0) {
            throw new IllegalArgumentException("Log ID không hợp lệ");
        }
        
        if (!Session.getInstance().canManageInventory()) {
            throw new SecurityException("Không có quyền chỉnh sửa stock log");
        }
        
        StockLog existingLog = stockLogDAO.getById(logId);
        if (existingLog == null) {
            throw new IllegalArgumentException("Stock log không tồn tại");
        }
        
        existingLog.setNote(newNote);
        return stockLogDAO.update(existingLog);
    }

    /**
     * Xóa stock log (chỉ trong trường hợp đặc biệt)
     */
    public boolean deleteStockLog(int logId) {
        if (logId <= 0) {
            throw new IllegalArgumentException("Log ID không hợp lệ");
        }
        
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền xóa stock log");
        }
        
        StockLog existingLog = stockLogDAO.getById(logId);
        if (existingLog == null) {
            throw new IllegalArgumentException("Stock log không tồn tại");
        }
        
        System.out.println("WARNING: Deleting stock log ID " + logId + 
                         " - Material: " + existingLog.getMaterialName() +
                         " - Type: " + existingLog.getChangeType());
        
        return stockLogDAO.delete(logId);
    }

    /**
     * Đếm tổng số stock logs
     */
    public long getTotalStockLogsCount() {
        return stockLogDAO.count();
    }

    /**
     * Đếm stock logs theo material
     */
    public long getStockLogsCountByMaterial(int materialId) {
        return stockLogDAO.countByMaterialId(materialId);
    }
}