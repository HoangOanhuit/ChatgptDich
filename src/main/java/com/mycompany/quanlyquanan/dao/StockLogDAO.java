/*
 * DAO class cho StockLog (Lịch sử xuất nhập kho)
 * Xử lý các thao tác database liên quan đến lịch sử thay đổi số lượng kho
 */
package com.mycompany.quanlyquanan.dao;

import com.mycompany.quanlyquanan.model.StockLog;
import com.mycompany.quanlyquanan.utils.DatabaseConnector;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO class cho StockLog - Lịch sử xuất nhập kho
 * @author Admin
 */
public class StockLogDAO {
    
    private final Connection conn;

    public StockLogDAO() {
        this.conn = DatabaseConnector.getConnection();
    }

    /**
     * Thêm stock log mới
     */

public boolean insert(StockLog stockLog) {
    String sql = """
        INSERT INTO stock_logs (
            material_id, change_type, quantity_change, quantity_before, quantity_after,
            reference_id, reference_type, note, created_by, log_date, created_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        System.out.println("Preparing SQL: " + sql);
        
        int idx = 1;
        stmt.setInt(idx++, stockLog.getMaterialId());
        stmt.setString(idx++, normalizeChangeType(stockLog.getChangeType()));
        stmt.setBigDecimal(idx++, stockLog.getQuantityChange());
        stmt.setBigDecimal(idx++, stockLog.getQuantityBefore());
        stmt.setBigDecimal(idx++, stockLog.getQuantityAfter());
        
        if (stockLog.getReferenceId() != null) {
            stmt.setInt(idx++, stockLog.getReferenceId());
        } else {
            stmt.setNull(idx++, Types.INTEGER);
        }
        
        stmt.setString(idx++, normalizeReferenceType(stockLog.getReferenceType()));
        stmt.setString(idx++, stockLog.getNote());
        stmt.setInt(idx++, stockLog.getCreatedBy());
        stmt.setTimestamp(idx++, Timestamp.valueOf(LocalDateTime.now())); // log_date
        stmt.setTimestamp(idx++, Timestamp.valueOf(stockLog.getCreatedAt())); // created_at
        
        System.out.println("SQL parameters set successfully");
        
        int result = stmt.executeUpdate();
        System.out.println("SQL executed, affected rows: " + result);
        
        if (result > 0) {
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int newId = keys.getInt(1);
                stockLog.setId(newId);
                System.out.println("StockLog ID generated: " + newId);
            }
            return true;
        }
    } catch (SQLException e) {
        System.err.println("❌ SQL Error in StockLog insert: " + e.getMessage());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
        e.printStackTrace();
        throw new RuntimeException("Không thể tạo lịch sử kho: " + e.getMessage(), e);
    }
    return false;
}

// HELPER METHODS
private String normalizeChangeType(StockLog.ChangeType changeType) {
    if (changeType == null) return "import";
    return changeType.name().toLowerCase();
}
/*
private String normalizeReferenceType(String referenceType) {
    if (referenceType == null) return "material_import";
    return referenceType.toLowerCase();
}
*/    
    //Lấy tất cả stock logs
    
    public List<StockLog> getAll() {
        List<StockLog> logs = new ArrayList<>();
        String sql = """
            SELECT sl.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM stock_logs sl
            LEFT JOIN materials m ON sl.material_id = m.id
            LEFT JOIN employees e ON sl.created_by = e.id
            ORDER BY sl.created_at DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                logs.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all stock logs: " + e.getMessage());
            e.printStackTrace();
        }
        return logs;
    }


    private String normalizeReferenceType(String referenceType) {
        if (referenceType == null) return "manual_adjust";

        switch (referenceType.toLowerCase()) {
            case "material_import":
                return "material_import";
            case "order_item":
            case "order":
                return "order_item";
            case "manual_adjust":
            case "adjust":
                return "manual_adjust";
            case "dish_preparation":
                return "dish_preparation";
            case "export":
            case "manual_export":
                return "manual_export";  // Giờ đã có trong database
            case "waste":
                return "waste_disposal"; // Giờ đã có trong database
            default:
                return "manual_adjust";
        }
    }    
    
    /**
     * Lấy logs với phân trang
     */
    public List<StockLog> getWithPagination(int offset, int limit) {
        List<StockLog> logs = new ArrayList<>();
        String sql = """
            SELECT sl.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM stock_logs sl
            LEFT JOIN materials m ON sl.material_id = m.id
            LEFT JOIN employees e ON sl.created_by = e.id
            ORDER BY sl.created_at DESC
            LIMIT ? OFFSET ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting stock logs with pagination: " + e.getMessage());
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Lấy logs theo nguyên liệu
     */
    public List<StockLog> getByMaterialId(int materialId) {
        List<StockLog> logs = new ArrayList<>();
        String sql = """
            SELECT sl.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM stock_logs sl
            LEFT JOIN materials m ON sl.material_id = m.id
            LEFT JOIN employees e ON sl.created_by = e.id
            WHERE sl.material_id = ?
            ORDER BY sl.created_at DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, materialId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting stock logs by material: " + e.getMessage());
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Lấy timeline hoạt động của nguyên liệu
     */
    public List<Map<String, Object>> getMaterialActivityTimeline(int materialId, int limit) {
        List<Map<String, Object>> timeline = new ArrayList<>();
        String sql = """
            SELECT sl.*, e.name as created_by_name
            FROM stock_logs sl
            LEFT JOIN employees e ON sl.created_by = e.id
            WHERE sl.material_id = ?
            ORDER BY sl.created_at DESC
            LIMIT ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, materialId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", rs.getInt("id"));
                activity.put("change_type", rs.getString("change_type"));
                activity.put("quantity_change", rs.getBigDecimal("quantity_change"));
                activity.put("quantity_before", rs.getBigDecimal("quantity_before"));
                activity.put("quantity_after", rs.getBigDecimal("quantity_after"));
                activity.put("reference_id", rs.getObject("reference_id"));
                activity.put("reference_type", rs.getString("reference_type"));
                activity.put("note", rs.getString("note"));
                activity.put("created_at", rs.getTimestamp("created_at"));
                activity.put("created_by_name", rs.getString("created_by_name"));
                timeline.add(activity);
            }
        } catch (SQLException e) {
            System.err.println("Error getting material activity timeline: " + e.getMessage());
            e.printStackTrace();
        }
        return timeline;
    }

    /**
     * Lấy stock log theo ID
     */
    public StockLog getById(int id) {
        String sql = """
            SELECT sl.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM stock_logs sl
            LEFT JOIN materials m ON sl.material_id = m.id
            LEFT JOIN employees e ON sl.created_by = e.id
            WHERE sl.id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetWithDetails(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting stock log by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật note của stock log
     */
    public boolean update(StockLog stockLog) {
        String sql = """
            UPDATE stock_logs 
            SET note = ?
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, stockLog.getNote());
            stmt.setInt(2, stockLog.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating stock log: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa stock log (chỉ trong trường hợp đặc biệt)
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM stock_logs WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting stock log: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // LOGGING METHODS
    /**
     * Ghi log nhập kho
     */
    public boolean logImport(int materialId, BigDecimal quantity, BigDecimal quantityBefore, 
                           BigDecimal quantityAfter, int importId, int createdBy, String note) {

        // *** THÊM DEBUG LOGGING ***
        System.out.println("=== DEBUG StockLogDAO.logImport ===");
        System.out.println("MaterialID: " + materialId);
        System.out.println("Quantity: " + quantity);
        System.out.println("QuantityBefore: " + quantityBefore);
        System.out.println("QuantityAfter: " + quantityAfter);
        System.out.println("ImportID: " + importId);
        System.out.println("CreatedBy: " + createdBy);
        System.out.println("Note: " + note);

        // Kiểm tra tham số đầu vào
        if (materialId <= 0) {
            System.err.println("ERROR: MaterialID không hợp lệ: " + materialId);
            return false;
        }

        if (importId <= 0) {
            System.err.println("ERROR: ImportID không hợp lệ: " + importId);
            return false;
        }

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("ERROR: Quantity không hợp lệ: " + quantity);
            return false;
        }

        // Tạo StockLog object
        StockLog log = new StockLog();
        log.setMaterialId(materialId);
        log.setChangeType(StockLog.ChangeType.IMPORT);
        log.setQuantityChange(quantity);
        log.setQuantityBefore(quantityBefore);
        log.setQuantityAfter(quantityAfter);
        log.setReferenceId(importId);
        log.setReferenceType("material_import");
        log.setNote(note);
        log.setCreatedBy(createdBy);
        log.setCreatedAt(LocalDateTime.now());

        System.out.println("StockLog object created: " + log.toString());

        // Gọi phương thức insert
        boolean result = insert(log);

        if (result) {
            System.out.println("✅ StockLog inserted successfully with ID: " + log.getId());
        } else {
            System.err.println("❌ Failed to insert StockLog");
        }

        System.out.println("=== END DEBUG StockLogDAO.logImport ===");

        return result;
    }

    /**
     * Ghi log tiêu thụ (consume)
     */
    public boolean logConsume(int materialId, BigDecimal quantity, BigDecimal quantityBefore,
                            BigDecimal quantityAfter, int orderId, int createdBy, String note) {
        StockLog log = new StockLog(
            materialId, StockLog.ChangeType.CONSUME, quantity.negate(),
            quantityBefore, quantityAfter, orderId, "order", note, createdBy
        );
        return insert(log);
    }

    /**
     * Ghi log điều chỉnh
     */
    public boolean logAdjust(int materialId, BigDecimal quantityChange, BigDecimal quantityBefore,
                           BigDecimal quantityAfter, int createdBy, String reason) {
        StockLog log = new StockLog(
            materialId, StockLog.ChangeType.ADJUST, quantityChange,
            quantityBefore, quantityAfter, null, "adjust", reason, createdBy
        );
        return insert(log);
    }

    /**
     * Ghi log xuất kho
     */
    /*
    public boolean logExport(int materialId, BigDecimal quantity, BigDecimal quantityBefore,
                           BigDecimal quantityAfter, int createdBy, String note) {
        StockLog log = new StockLog(
            materialId, StockLog.ChangeType.EXPORT, quantity.negate(),
            quantityBefore, quantityAfter, 0, "export", note, createdBy
        );
        return insert(log);
    }
    */
    public boolean logExport(int materialId, BigDecimal quantity, BigDecimal quantityBefore,
                           BigDecimal quantityAfter, int createdBy, String note) {
        StockLog log = new StockLog(
            materialId, StockLog.ChangeType.EXPORT, quantity.negate(),
            quantityBefore, quantityAfter, null, "manual_export", note, createdBy  // Dùng "manual_export"
        );
        return insert(log);
    }
    /**
     * Ghi log hoàn trả
     */
    public boolean logReturn(int materialId, BigDecimal quantity, BigDecimal quantityBefore,
                           BigDecimal quantityAfter, int orderId, int createdBy, String note) {
        StockLog log = new StockLog(
            materialId, StockLog.ChangeType.RETURN, quantity,
            quantityBefore, quantityAfter, orderId, "order", note, createdBy
        );
        return insert(log);
    }

    /**
     * Ghi log hao hụt
     */
    /*
    public boolean logWaste(int materialId, BigDecimal quantity, BigDecimal quantityBefore,
                          BigDecimal quantityAfter, int createdBy, String reason) {
        StockLog log = new StockLog(
            materialId, StockLog.ChangeType.WASTE, quantity.negate(),
            quantityBefore, quantityAfter, null, "waste", reason, createdBy
        );
        return insert(log);
    }
    */
    public boolean logWaste(int materialId, BigDecimal quantity, BigDecimal quantityBefore,
                          BigDecimal quantityAfter, int createdBy, String reason) {
        StockLog log = new StockLog(
            materialId, StockLog.ChangeType.WASTE, quantity.negate(),
            quantityBefore, quantityAfter, null, "waste_disposal", reason, createdBy  // Dùng "waste_disposal"
        );
        return insert(log);
    }    
    /**
     * Tìm kiếm stock logs
     */
    public List<StockLog> search(String keyword) {
        List<StockLog> logs = new ArrayList<>();
        String sql = """
            SELECT sl.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM stock_logs sl
            LEFT JOIN materials m ON sl.material_id = m.id
            LEFT JOIN employees e ON sl.created_by = e.id
            WHERE m.name LIKE ? OR sl.note LIKE ? OR e.name LIKE ?
            ORDER BY sl.created_at DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching stock logs: " + e.getMessage());
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Lấy thống kê thay đổi theo nguyên liệu
     */
    public Map<String, Object> getMaterialChangeStats(int materialId, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        String sql = """
            SELECT 
                change_type,
                SUM(CASE WHEN quantity_change > 0 THEN quantity_change ELSE 0 END) as total_increase,
                SUM(CASE WHEN quantity_change < 0 THEN ABS(quantity_change) ELSE 0 END) as total_decrease,
                COUNT(*) as change_count
            FROM stock_logs
            WHERE material_id = ? AND created_at >= ? AND created_at <= ?
            GROUP BY change_type
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, materialId);
            stmt.setTimestamp(2, Timestamp.valueOf(startDate));
            stmt.setTimestamp(3, Timestamp.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String changeType = rs.getString("change_type");
                Map<String, Object> changeStats = new HashMap<>();
                changeStats.put("total_increase", rs.getBigDecimal("total_increase"));
                changeStats.put("total_decrease", rs.getBigDecimal("total_decrease"));
                changeStats.put("change_count", rs.getInt("change_count"));
                stats.put(changeType, changeStats);
            }
        } catch (SQLException e) {
            System.err.println("Error getting material change stats: " + e.getMessage());
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * Map ResultSet cơ bản thành StockLog object (không có join)
     */
    private StockLog mapResultSet(ResultSet rs) throws SQLException {
        StockLog stockLog = new StockLog();
        stockLog.setId(rs.getInt("id"));
        stockLog.setMaterialId(rs.getInt("material_id"));
        
        // Map change_type từ string sang enum
        String changeTypeStr = rs.getString("change_type");
        try {
            if ("manual_export".equalsIgnoreCase(changeTypeStr)) {
                stockLog.setChangeType(StockLog.ChangeType.EXPORT);
            } else {
                stockLog.setChangeType(StockLog.ChangeType.valueOf(changeTypeStr.toUpperCase()));
            }
        } catch (IllegalArgumentException e) {
            stockLog.setChangeType(StockLog.ChangeType.ADJUST);
        }
        
        stockLog.setQuantityChange(rs.getBigDecimal("quantity_change"));
        stockLog.setQuantityBefore(rs.getBigDecimal("quantity_before"));
        stockLog.setQuantityAfter(rs.getBigDecimal("quantity_after"));
        
        // Handle nullable reference_id
        int referenceId = rs.getInt("reference_id");
        if (!rs.wasNull()) {
            stockLog.setReferenceId(referenceId);
        }
        
        stockLog.setReferenceType(rs.getString("reference_type"));
        stockLog.setNote(rs.getString("note"));
        stockLog.setCreatedBy(rs.getInt("created_by"));
        
        // Handle timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            stockLog.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return stockLog;
    }

    /**
     * Map ResultSet thành StockLog object với thông tin chi tiết (có join)
     */
    private StockLog mapResultSetWithDetails(ResultSet rs) throws SQLException {
        StockLog stockLog = mapResultSet(rs);
        
        // Thêm thông tin từ join
        try {
            stockLog.setMaterialName(rs.getString("material_name"));
            stockLog.setMaterialUnit(rs.getString("material_unit"));
            stockLog.setCreatedByName(rs.getString("created_by_name"));
        } catch (SQLException e) {
            // Ignore if columns don't exist (basic query without join)
        }
        
        return stockLog;
    }

    /**
     * Đếm tổng số stock logs
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM stock_logs";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting stock logs: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Đếm stock logs theo nguyên liệu
     */
    public long countByMaterialId(int materialId) {
        String sql = "SELECT COUNT(*) FROM stock_logs WHERE material_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, materialId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting stock logs by material: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public boolean create(StockLog stockLog) {
        return insert(stockLog);
    }
}