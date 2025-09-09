/*
 * DAO class cho MaterialImport (Phiếu nhập kho)
 * Xử lý các thao tác database liên quan đến phiếu nhập nguyên vật liệu
 */
package com.mycompany.quanlyquanan.dao;

import com.mycompany.quanlyquanan.model.MaterialImport;
import com.mycompany.quanlyquanan.utils.DatabaseConnector;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO class cho MaterialImport - Phiếu nhập kho
 * @author Admin
 */
public class MaterialImportDAO {
    
    private final Connection conn;

    public MaterialImportDAO() {
        this.conn = DatabaseConnector.getConnection();
    }

    /**
     * Lấy tất cả phiếu nhập kho
     */
    public List<MaterialImport> getAll() {
        List<MaterialImport> imports = new ArrayList<>();
        String sql = """
            SELECT mi.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM material_imports mi
            LEFT JOIN materials m ON mi.material_id = m.id
            LEFT JOIN employees e ON mi.created_by = e.id
            ORDER BY mi.import_date DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                imports.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all material imports: " + e.getMessage());
            e.printStackTrace();
        }
        return imports;
    }

    /**
     * Lấy phiếu nhập theo khoảng thời gian
     */
    public List<MaterialImport> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<MaterialImport> imports = new ArrayList<>();
        String sql = """
            SELECT mi.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM material_imports mi
            LEFT JOIN materials m ON mi.material_id = m.id
            LEFT JOIN employees e ON mi.created_by = e.id
            WHERE mi.import_date >= ? AND mi.import_date <= ?
            ORDER BY mi.import_date DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                imports.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting imports by date range: " + e.getMessage());
            e.printStackTrace();
        }
        return imports;
    }
    /**
     * Lấy phiếu nhập theo ID
     */
    public MaterialImport getById(int id) {
        String sql = """
            SELECT mi.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM material_imports mi
            LEFT JOIN materials m ON mi.material_id = m.id
            LEFT JOIN employees e ON mi.created_by = e.id
            WHERE mi.id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetWithDetails(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting import by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Thêm phiếu nhập mới
     */
    public boolean insert(MaterialImport materialImport) {
    String sql = """
        INSERT INTO material_imports (
            material_id, import_quantity, import_price, price_per_unit,
            supplier_name, import_date, created_by, note, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        setStatementParameters(stmt, materialImport);
        stmt.setTimestamp(9, Timestamp.valueOf(materialImport.getCreatedAt()));
        stmt.setTimestamp(10, Timestamp.valueOf(materialImport.getUpdatedAt()));
        
        int affectedRows = stmt.executeUpdate();
        if (affectedRows > 0) {
            // *** PHẦN QUAN TRỌNG: LẤY ID VỪA TẠO VÀ CẬP NHẬT VÀO OBJECT ***
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    materialImport.setId(newId);  // ← CẬP NHẬT ID VÀO OBJECT
                    System.out.println("MaterialImport created with ID: " + newId);
                }
            }
            return true;
        }
    } catch (SQLException e) {
        System.err.println("Error inserting material import: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
    return false;
}

// PHƯƠNG THỨC HỖ TRỢ SET PARAMETERS
private void setStatementParameters(PreparedStatement stmt, MaterialImport materialImport) throws SQLException {
    int idx = 1;
    stmt.setInt(idx++, materialImport.getMaterialId());
    stmt.setBigDecimal(idx++, materialImport.getImportQuantity());
    stmt.setBigDecimal(idx++, materialImport.getImportPrice());
    stmt.setBigDecimal(idx++, materialImport.getPricePerUnit());
    stmt.setString(idx++, materialImport.getSupplierName());
    stmt.setTimestamp(idx++, Timestamp.valueOf(materialImport.getImportDate()));
    stmt.setInt(idx++, materialImport.getCreatedBy());
    stmt.setString(idx++, materialImport.getNote());
}

    /**
     * Thêm phiếu nhập và lấy ID tự sinh (sử dụng cho stored procedure)
     */
    public int insertAndGetId(MaterialImport materialImport) {
        String sql = """
            INSERT INTO material_imports (
                material_id, import_quantity, import_price, price_per_unit,
                supplier_name, import_date, created_by, note, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(stmt, materialImport);
            stmt.setTimestamp(9, Timestamp.valueOf(materialImport.getCreatedAt()));
            stmt.setTimestamp(10, Timestamp.valueOf(materialImport.getUpdatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting material import with ID: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Cập nhật phiếu nhập
     */
    public boolean update(MaterialImport materialImport) {
        String sql = """
            UPDATE material_imports SET 
                material_id = ?, import_quantity = ?, import_price = ?, price_per_unit = ?,
                supplier_name = ?, import_date = ?, note = ?, updated_at = ?
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int idx = 1;
            stmt.setInt(idx++, materialImport.getMaterialId());
            stmt.setBigDecimal(idx++, materialImport.getImportQuantity());
            stmt.setBigDecimal(idx++, materialImport.getImportPrice());
            stmt.setBigDecimal(idx++, materialImport.getPricePerUnit());
            stmt.setString(idx++, materialImport.getSupplierName());
            stmt.setTimestamp(idx++, Timestamp.valueOf(materialImport.getImportDate()));
            stmt.setString(idx++, materialImport.getNote());
            stmt.setTimestamp(idx++, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(idx++, materialImport.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating material import: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa phiếu nhập (chỉ nên xóa nếu chưa có tác động đến kho)
     */
    public boolean delete(int id) {
        // Kiểm tra xem có stock_logs liên quan không
        if (hasRelatedStockLogs(id)) {
            System.err.println("Cannot delete import with related stock logs");
            return false;
        }
        
        String sql = "DELETE FROM material_imports WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting material import: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy thống kê nhập hàng theo nguyên liệu
     */
    public List<MaterialImport> getImportStatsByMaterial() {
        List<MaterialImport> stats = new ArrayList<>();
        String sql = """
            SELECT 
                mi.material_id,
                m.name as material_name,
                m.unit as material_unit,
                SUM(mi.import_quantity) as total_quantity,
                SUM(mi.import_price) as total_price,
                COUNT(*) as import_count,
                AVG(mi.price_per_unit) as avg_price_per_unit,
                MAX(mi.import_date) as last_import_date
            FROM material_imports mi
            JOIN materials m ON mi.material_id = m.id
            GROUP BY mi.material_id, m.name, m.unit
            ORDER BY total_price DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                MaterialImport stat = new MaterialImport();
                stat.setMaterialId(rs.getInt("material_id"));
                stat.setMaterialName(rs.getString("material_name"));
                stat.setMaterialUnit(rs.getString("material_unit"));
                stat.setImportQuantity(rs.getBigDecimal("total_quantity"));
                stat.setImportPrice(rs.getBigDecimal("total_price"));
                stat.setPricePerUnit(rs.getBigDecimal("avg_price_per_unit"));
                stat.setImportDate(rs.getTimestamp("last_import_date").toLocalDateTime());
                // Sử dụng note để lưu thông tin thống kê
                stat.setNote("Số lần nhập: " + rs.getInt("import_count"));
                stats.add(stat);
            }
        } catch (SQLException e) {
            System.err.println("Error getting import stats by material: " + e.getMessage());
            e.printStackTrace();
        }
        return stats;
    }

    

    /**
     * Kiểm tra xem có stock logs liên quan không
     */
    private boolean hasRelatedStockLogs(int importId) {
        String sql = """
            SELECT COUNT(*) as count 
            FROM stock_logs
            WHERE reference_id = ? AND reference_type = 'IMPORT'
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, importId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking related stock logs: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
    * Lấy danh sách phiếu nhập gần đây
    */
    public List<MaterialImport> getRecent(int limit) {
        List<MaterialImport> imports = new ArrayList<>();
        String sql = """
            SELECT mi.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM material_imports mi
            LEFT JOIN materials m ON mi.material_id = m.id
            LEFT JOIN employees e ON mi.created_by = e.id
            ORDER BY mi.import_date DESC, mi.created_at DESC
            LIMIT ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                imports.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting recent material imports: " + e.getMessage());
            e.printStackTrace();
        }
        return imports;
    }

    /**
     * Đếm số phiếu nhập trong khoảng thời gian
     */
    public long countByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT COUNT(*) FROM material_imports 
            WHERE import_date >= ? AND import_date <= ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting material imports by date range: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy tổng giá trị nhập trong khoảng thời gian
     */
    public BigDecimal getTotalImportValueInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT COALESCE(SUM(import_price), 0) as total_value
            FROM material_imports 
            WHERE import_date >= ? AND import_date <= ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("total_value");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total import value in period: " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Lấy phiếu nhập theo nguyên liệu
     */
    public List<MaterialImport> getByMaterialId(int materialId) {
        List<MaterialImport> imports = new ArrayList<>();
        String sql = """
            SELECT mi.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM material_imports mi
            LEFT JOIN materials m ON mi.material_id = m.id
            LEFT JOIN employees e ON mi.created_by = e.id
            WHERE mi.material_id = ?
            ORDER BY mi.import_date DESC
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, materialId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                imports.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting material imports by material ID: " + e.getMessage());
            e.printStackTrace();
        }
        return imports;
    }

    /**
     * Lấy phiếu nhập theo nhà cung cấp
     */
    public List<MaterialImport> getBySupplier(String supplierName) {
        List<MaterialImport> imports = new ArrayList<>();
        String sql = """
            SELECT mi.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM material_imports mi
            LEFT JOIN materials m ON mi.material_id = m.id
            LEFT JOIN employees e ON mi.created_by = e.id
            WHERE mi.supplier_name LIKE ?
            ORDER BY mi.import_date DESC
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + supplierName + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                imports.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting material imports by supplier: " + e.getMessage());
            e.printStackTrace();
        }
        return imports;
    }
    
    /**
     * Tính giá trung bình cho một nguyên liệu dựa trên các phiếu nhập
     */
    public BigDecimal getAveragePricePerUnitByMaterialId(int materialId) {
        String sql = """
            SELECT
                CASE
                    WHEN SUM(import_quantity) > 0 THEN SUM(import_price) / SUM(import_quantity)
                    ELSE 0
                END AS avg_price
            FROM material_imports
            WHERE material_id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, materialId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                BigDecimal avg = rs.getBigDecimal("avg_price");
                return avg != null ? avg : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average price per unit: " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
    

    /**
     * Lấy thống kê nhập kho theo tháng
     */
    public List<Map<String, Object>> getMonthlyImportStats(int year) {
        List<Map<String, Object>> stats = new ArrayList<>();
        String sql = """
            SELECT 
                MONTH(import_date) as month,
                COUNT(*) as import_count,
                SUM(import_price) as total_value,
                SUM(import_quantity) as total_quantity
            FROM material_imports 
            WHERE YEAR(import_date) = ?
            GROUP BY MONTH(import_date)
            ORDER BY month
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> monthStat = new HashMap<>();
                monthStat.put("month", rs.getInt("month"));
                monthStat.put("import_count", rs.getInt("import_count"));
                monthStat.put("total_value", rs.getBigDecimal("total_value"));
                monthStat.put("total_quantity", rs.getBigDecimal("total_quantity"));
                stats.add(monthStat);
            }
        } catch (SQLException e) {
            System.err.println("Error getting monthly import stats: " + e.getMessage());
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * Lấy danh sách nhà cung cấp
     */
    public List<String> getAllSuppliers() {
        List<String> suppliers = new ArrayList<>();
        String sql = """
            SELECT DISTINCT supplier_name 
            FROM material_imports 
            WHERE supplier_name IS NOT NULL AND supplier_name != ''
            ORDER BY supplier_name
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                suppliers.add(rs.getString("supplier_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all suppliers: " + e.getMessage());
            e.printStackTrace();
        }
        return suppliers;
    }

    /**
     * Tìm kiếm phiếu nhập
     */
    public List<MaterialImport> search(String keyword) {
        List<MaterialImport> imports = new ArrayList<>();
        String sql = """
            SELECT mi.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM material_imports mi
            LEFT JOIN materials m ON mi.material_id = m.id
            LEFT JOIN employees e ON mi.created_by = e.id
            WHERE m.name LIKE ? OR mi.supplier_name LIKE ? OR mi.note LIKE ?
            ORDER BY mi.import_date DESC
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                imports.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching material imports: " + e.getMessage());
            e.printStackTrace();
        }
        return imports;
    }

    /**
     * Lấy phiếu nhập với phân trang
     */
    public List<MaterialImport> getWithPagination(int offset, int limit) {
        List<MaterialImport> imports = new ArrayList<>();
        String sql = """
            SELECT mi.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM material_imports mi
            LEFT JOIN materials m ON mi.material_id = m.id
            LEFT JOIN employees e ON mi.created_by = e.id
            ORDER BY mi.import_date DESC
            LIMIT ? OFFSET ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                imports.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting material imports with pagination: " + e.getMessage());
            e.printStackTrace();
        }
        return imports;
    }

 
    /**
     * Map ResultSet thành MaterialImport object với thông tin chi tiết
     */
    private MaterialImport mapResultSetWithDetails(ResultSet rs) throws SQLException {
        MaterialImport materialImport = mapResultSet(rs);

        // Thêm thông tin từ JOIN
        try {
            materialImport.setMaterialName(rs.getString("material_name"));
            materialImport.setMaterialUnit(rs.getString("material_unit"));
            materialImport.setCreatedByName(rs.getString("created_by_name"));
        } catch (SQLException e) {
            // Ignore missing joined columns
        }

        return materialImport;
    }

    /**
     * Map ResultSet cơ bản thành MaterialImport object
     */
    private MaterialImport mapResultSet(ResultSet rs) throws SQLException {
        MaterialImport materialImport = new MaterialImport();
        materialImport.setId(rs.getInt("id"));
        materialImport.setMaterialId(rs.getInt("material_id"));
        materialImport.setImportQuantity(rs.getBigDecimal("import_quantity"));
        materialImport.setImportPrice(rs.getBigDecimal("import_price"));
        materialImport.setPricePerUnit(rs.getBigDecimal("price_per_unit"));
        materialImport.setSupplierName(rs.getString("supplier_name"));
        materialImport.setCreatedBy(rs.getInt("created_by"));
        materialImport.setNote(rs.getString("note"));

        // Handle timestamps
        Timestamp importDate = rs.getTimestamp("import_date");
        if (importDate != null) {
            materialImport.setImportDate(importDate.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            materialImport.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            materialImport.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return materialImport;
    }
    /**
     * Đếm tổng số phiếu nhập
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM material_imports";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting material imports: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy phiếu nhập gần đây nhất cho nguyên liệu
     */
    public MaterialImport getLatestImportForMaterial(int materialId) {
        String sql = """
            SELECT mi.*, m.name as material_name, m.unit as material_unit, 
                   e.name as created_by_name
            FROM material_imports mi
            LEFT JOIN materials m ON mi.material_id = m.id
            LEFT JOIN employees e ON mi.created_by = e.id
            WHERE mi.material_id = ?
            ORDER BY mi.import_date DESC
            LIMIT 1
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, materialId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetWithDetails(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting latest import for material: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(MaterialImport materialImport) {
        return insert(materialImport);
    }
}