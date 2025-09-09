/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.dao;

import com.mycompany.quanlyquanan.model.ExpenseReceipt;
import com.mycompany.quanlyquanan.utils.DatabaseConnector;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO class cho ExpenseReceipt - Quản lý phiếu chi
 * @author trang
 */
public class ExpenseReceiptDAO {

    private final Connection conn;
    private final String categoryColumn;

    public ExpenseReceiptDAO() {
        this.conn = DatabaseConnector.getConnection();
        this.categoryColumn = detectCategoryColumn();
    }

    private String detectCategoryColumn() {
        try {
            DatabaseMetaData meta = conn.getMetaData();

            // Prefer the modern `category` column
            try (ResultSet rs = meta.getColumns(null, null, "expense_receipts", "category")) {
                if (rs.next()) {
                    return "category";
                }
            }

            // Fall back to legacy `expense_type` column if present
            try (ResultSet rs = meta.getColumns(null, null, "expense_receipts", "expense_type")) {
                if (rs.next()) {
                    return "expense_type";
                }
            }
        } catch (SQLException ignored) {
            // Ignore and use default below
        }    
        // Default to `category` which matches the current database schema
        return "category";
    }

    /**
     * Lấy tất cả phiếu chi
     */
    public List<ExpenseReceipt> getAll() {
        List<ExpenseReceipt> receipts = new ArrayList<>();
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            ORDER BY er.receipt_date DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                receipts.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all expense receipts: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Lấy phiếu chi theo khoảng thời gian - FIXED VERSION
     */
    public List<ExpenseReceipt> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<ExpenseReceipt> receipts = new ArrayList<>();
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            WHERE er.receipt_date >= ? AND er.receipt_date <= ?
            ORDER BY er.receipt_date DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                receipts.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense receipts by date range: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Lấy phiếu chi theo loại chi phí
     */
    public List<ExpenseReceipt> getByCategory(ExpenseReceipt.ExpenseCategory category) {
        List<ExpenseReceipt> receipts = new ArrayList<>();
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            WHERE er.category = ?
            ORDER BY er.receipt_date DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.name().toLowerCase());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                receipts.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense receipts by category: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Lấy phiếu chi theo nhà cung cấp
     */
    public List<ExpenseReceipt> getByVendor(String vendor) {
        List<ExpenseReceipt> receipts = new ArrayList<>();
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            WHERE er.vendor LIKE ?
            ORDER BY er.receipt_date DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + vendor + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                receipts.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense receipts by vendor: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Lấy phiếu chi theo ID
     */
    public ExpenseReceipt getById(int id) {
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            WHERE er.id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetWithDetails(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense receipt by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy phiếu chi theo số phiếu
     */
    public ExpenseReceipt getByReceiptNumber(String receiptNumber) {
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            WHERE er.receipt_number = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, receiptNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetWithDetails(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense receipt by receipt number: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Thêm phiếu chi mới
     */
    public boolean insert(ExpenseReceipt expenseReceipt) {
        // Auto-generate receipt number if not set
        if (expenseReceipt.getReceiptNumber() == null || expenseReceipt.getReceiptNumber().trim().isEmpty()) {
            expenseReceipt.setReceiptNumber(generateNextReceiptNumber());
        }
        
        // Set timestamps if not set
        LocalDateTime now = LocalDateTime.now();
        if (expenseReceipt.getCreatedAt() == null) {
            expenseReceipt.setCreatedAt(now);
        }
        if (expenseReceipt.getUpdatedAt() == null) {
            expenseReceipt.setUpdatedAt(now);
        }
        
        String sql = String.format("""
            INSERT INTO expense_receipts (
                %s, amount, related_import_id, description, receipt_date,
                            receipt_number, vendor, payment_method, created_by, created_at, updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, categoryColumn);
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(stmt, expenseReceipt);
            stmt.setTimestamp(10, Timestamp.valueOf(expenseReceipt.getCreatedAt()));
            stmt.setTimestamp(11, Timestamp.valueOf(expenseReceipt.getUpdatedAt()));
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    expenseReceipt.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting expense receipt: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Thêm phiếu chi và lấy ID tự sinh
     */
    public int insertAndGetId(ExpenseReceipt expenseReceipt) {
        // Auto-generate receipt number if not set
        if (expenseReceipt.getReceiptNumber() == null || expenseReceipt.getReceiptNumber().trim().isEmpty()) {
            expenseReceipt.setReceiptNumber(generateNextReceiptNumber());
        }
        
        // Set timestamps if not set
        LocalDateTime now = LocalDateTime.now();
        if (expenseReceipt.getCreatedAt() == null) {
            expenseReceipt.setCreatedAt(now);
        }
        if (expenseReceipt.getUpdatedAt() == null) {
            expenseReceipt.setUpdatedAt(now);
        }
        
        String sql = String.format("""
            INSERT INTO expense_receipts (
                %s, amount, related_import_id, description, receipt_date,
                                receipt_number, vendor, payment_method, created_by, created_at, updated_at
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, categoryColumn);
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(stmt, expenseReceipt);
            stmt.setTimestamp(10, Timestamp.valueOf(expenseReceipt.getCreatedAt()));
            stmt.setTimestamp(11, Timestamp.valueOf(expenseReceipt.getUpdatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting expense receipt with ID: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Cập nhật phiếu chi
     */
    public boolean update(ExpenseReceipt expenseReceipt) {
        String sql = String.format("""
            UPDATE expense_receipts SET
                %s = ?, amount = ?, related_import_id = ?, description = ?,
                receipt_date = ?, receipt_number = ?, vendor = ?, payment_method = ?,
                updated_at = ?
            WHERE id = ?
            """, categoryColumn);
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setStatementParameters(stmt, expenseReceipt);
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(10, expenseReceipt.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating expense receipt: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa phiếu chi
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM expense_receipts WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting expense receipt: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tạo phiếu chi cho nhập hàng (tự động)
     */
    public boolean createForMaterialImport(int importId, BigDecimal amount, String materialName, 
                                         String supplierName, int createdBy) {
        ExpenseReceipt expense = ExpenseReceipt.createMaterialImportExpense(
            importId, amount, materialName, supplierName, createdBy
        );
        return insert(expense);
    }

    /**
     * Tìm kiếm phiếu chi
     */
    public List<ExpenseReceipt> search(String keyword) {
        List<ExpenseReceipt> receipts = new ArrayList<>();
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            WHERE er.description LIKE ? OR er.vendor LIKE ? OR er.receipt_number LIKE ?
               OR e.name LIKE ?
            ORDER BY er.receipt_date DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                receipts.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching expense receipts: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Lấy tổng chi phí theo loại trong khoảng thời gian
     */
    public Map<ExpenseReceipt.ExpenseCategory, BigDecimal> getExpenseSummaryByCategory(
            LocalDateTime startDate, LocalDateTime endDate) {
        Map<ExpenseReceipt.ExpenseCategory, BigDecimal> summary = new HashMap<>();
        String sql = """
            SELECT category, SUM(amount) as total_amount
            FROM expense_receipts
            WHERE receipt_date >= ? AND receipt_date <= ?
            GROUP BY category
            ORDER BY total_amount DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String categoryStr = rs.getString("category");
                BigDecimal amount = rs.getBigDecimal("total_amount");
                
                try {
                    ExpenseReceipt.ExpenseCategory category = 
                        ExpenseReceipt.ExpenseCategory.valueOf(categoryStr.toUpperCase());
                    summary.put(category, amount);
                } catch (IllegalArgumentException e) {
                    // Skip unknown categories
                    System.err.println("Unknown expense category: " + categoryStr);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense summary by category: " + e.getMessage());
            e.printStackTrace();
        }
        return summary;
    }

    /**
     * Lấy tổng chi phí trong khoảng thời gian
     */
    public BigDecimal getTotalExpenseInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) as total_expense
            FROM expense_receipts 
            WHERE receipt_date >= ? AND receipt_date <= ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal("total_expense");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total expense in period: " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Lấy thống kê chi phí theo nhà cung cấp
     */
    public List<Map<String, Object>> getExpenseStatsByVendor(LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> stats = new ArrayList<>();
        String sql = """
            SELECT 
                vendor,
                COUNT(*) as receipt_count,
                SUM(amount) as total_amount,
                AVG(amount) as avg_amount,
                MIN(receipt_date) as first_expense_date,
                MAX(receipt_date) as last_expense_date
            FROM expense_receipts
            WHERE receipt_date >= ? AND receipt_date <= ?
              AND vendor IS NOT NULL AND vendor != ''
            GROUP BY vendor
            ORDER BY total_amount DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("vendor", rs.getString("vendor"));
                stat.put("receipt_count", rs.getInt("receipt_count"));
                stat.put("total_amount", rs.getBigDecimal("total_amount"));
                stat.put("avg_amount", rs.getBigDecimal("avg_amount"));
                stat.put("first_expense_date", rs.getTimestamp("first_expense_date"));
                stat.put("last_expense_date", rs.getTimestamp("last_expense_date"));
                stats.add(stat);
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense stats by vendor: " + e.getMessage());
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * Lấy chi phí theo tháng (cho biểu đồ)
     */
    public List<Map<String, Object>> getMonthlyExpenseChart(int year) {
        List<Map<String, Object>> chartData = new ArrayList<>();
        String sql = """
            SELECT 
                MONTH(receipt_date) as month,
                SUM(amount) as total_amount,
                COUNT(*) as receipt_count
            FROM expense_receipts
            WHERE YEAR(receipt_date) = ?
            GROUP BY MONTH(receipt_date)
            ORDER BY month
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("month", rs.getInt("month"));
                data.put("total_amount", rs.getBigDecimal("total_amount"));
                data.put("receipt_count", rs.getInt("receipt_count"));
                chartData.add(data);
            }
        } catch (SQLException e) {
            System.err.println("Error getting monthly expense chart: " + e.getMessage());
            e.printStackTrace();
        }
        return chartData;
    }

    /**
     * Lấy danh sách nhà cung cấp
     */
    public List<String> getAllVendors() {
        List<String> vendors = new ArrayList<>();
        String sql = """
            SELECT DISTINCT vendor 
            FROM expense_receipts 
            WHERE vendor IS NOT NULL AND vendor != ''
            ORDER BY vendor
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                vendors.add(rs.getString("vendor"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting vendors: " + e.getMessage());
            e.printStackTrace();
        }
        return vendors;
    }

    /**
     * Lấy danh sách phương thức thanh toán
     */
    public List<String> getAllPaymentMethods() {
        List<String> methods = new ArrayList<>();
        String sql = """
            SELECT DISTINCT payment_method 
            FROM expense_receipts 
            WHERE payment_method IS NOT NULL AND payment_method != ''
            ORDER BY payment_method
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                methods.add(rs.getString("payment_method"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting payment methods: " + e.getMessage());
            e.printStackTrace();
        }
        return methods;
    }

    /**
     * Kiểm tra số phiếu có tồn tại không
     */
    public boolean isReceiptNumberExists(String receiptNumber, int excludeId) {
        String sql = "SELECT COUNT(*) FROM expense_receipts WHERE receipt_number = ? AND id != ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, receiptNumber);
            stmt.setInt(2, excludeId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking receipt number exists: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Tạo số phiếu tự động tiếp theo
     */
    public String generateNextReceiptNumber() {
        String sql = """
            SELECT receipt_number FROM expense_receipts 
            WHERE receipt_number LIKE 'PC%' 
            ORDER BY id DESC LIMIT 1
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                String lastNumber = rs.getString("receipt_number");
                if (lastNumber != null && lastNumber.startsWith("PC")) {
                    try {
                        String numberPart = lastNumber.substring(2);
                        int nextNumber = Integer.parseInt(numberPart) + 1;
                        return String.format("PC%06d", nextNumber);
                    } catch (NumberFormatException e) {
                        // Fallback to timestamp-based generation
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating next receipt number: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Fallback: generate based on timestamp
        return "PC" + String.valueOf(System.currentTimeMillis() % 1000000);
    }

    /**
     * Lấy phiếu chi gần đây
     */
    public List<ExpenseReceipt> getRecentExpenses(int limit) {
        List<ExpenseReceipt> receipts = new ArrayList<>();
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            ORDER BY er.created_at DESC
            LIMIT ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                receipts.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting recent expenses: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Đếm tổng số phiếu chi
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM expense_receipts";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting expense receipts: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Đếm phiếu chi theo loại
     */
    public long countByCategory(ExpenseReceipt.ExpenseCategory category) {
        String sql = "SELECT COUNT(*) FROM expense_receipts WHERE category = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.name().toLowerCase());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting expense receipts by category: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy phiếu chi có liên quan đến phiếu nhập
     */
    public List<ExpenseReceipt> getByImportId(int importId) {
        List<ExpenseReceipt> receipts = new ArrayList<>();
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            WHERE er.related_import_id = ?
            ORDER BY er.receipt_date DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, importId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                receipts.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense receipts by import id: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Lấy phiếu chi với phân trang
     */
    public List<ExpenseReceipt> getWithPagination(int offset, int limit) {
        List<ExpenseReceipt> receipts = new ArrayList<>();
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            ORDER BY er.receipt_date DESC
            LIMIT ? OFFSET ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                receipts.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense receipts with pagination: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Lấy chi phí theo nhiều điều kiện
     */
    public List<ExpenseReceipt> getByMultipleConditions(ExpenseReceipt.ExpenseCategory category,
                                                       LocalDateTime startDate, LocalDateTime endDate,
                                                       String vendor, BigDecimal minAmount, BigDecimal maxAmount,
                                                       int limit, int offset) {
        List<ExpenseReceipt> receipts = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            WHERE 1=1
            """);
        
        List<Object> params = new ArrayList<>();
        
        if (category != null) {
            sql.append(" AND er.category = ?");
            params.add(category.name().toLowerCase());
        }
        
        if (startDate != null) {
            sql.append(" AND er.receipt_date >= ?");
            params.add(Timestamp.valueOf(startDate));
        }
        
        if (endDate != null) {
            sql.append(" AND er.receipt_date <= ?");
            params.add(Timestamp.valueOf(endDate));
        }
        
        if (vendor != null && !vendor.trim().isEmpty()) {
            sql.append(" AND er.vendor LIKE ?");
            params.add("%" + vendor + "%");
        }
        
        if (minAmount != null) {
            sql.append(" AND er.amount >= ?");
            params.add(minAmount);
        }
        
        if (maxAmount != null) {
            sql.append(" AND er.amount <= ?");
            params.add(maxAmount);
        }
        
        sql.append(" ORDER BY er.receipt_date DESC");
        
        if (limit > 0) {
            sql.append(" LIMIT ?");
            params.add(limit);
            
            if (offset > 0) {
                sql.append(" OFFSET ?");
                params.add(offset);
            }
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                receipts.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense receipts by multiple conditions: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Lấy chi phí lớn nhất trong khoảng thời gian
     */
    public BigDecimal getMaxExpenseInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT COALESCE(MAX(amount), 0) as max_expense
            FROM expense_receipts 
            WHERE receipt_date >= ? AND receipt_date <= ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal("max_expense");
            }
        } catch (SQLException e) {
            System.err.println("Error getting max expense in period: " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Lấy chi phí trung bình theo loại
     */
    public BigDecimal getAverageExpenseByCategory(ExpenseReceipt.ExpenseCategory category,
                                                 LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT COALESCE(AVG(amount), 0) as avg_expense
            FROM expense_receipts 
            WHERE category = ? AND receipt_date >= ? AND receipt_date <= ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.name().toLowerCase());
            stmt.setTimestamp(2, Timestamp.valueOf(startDate));
            stmt.setTimestamp(3, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal("avg_expense");
            }
        } catch (SQLException e) {
            System.err.println("Error getting average expense by category: " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Lấy danh sách năm có dữ liệu
     */
    public List<Integer> getAvailableYears() {
        List<Integer> years = new ArrayList<>();
        String sql = """
            SELECT DISTINCT YEAR(receipt_date) as year 
            FROM expense_receipts 
            ORDER BY year DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                years.add(rs.getInt("year"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting available years: " + e.getMessage());
            e.printStackTrace();
        }
        return years;
    }

    /**
     * Lấy chi phí hằng ngày trong tháng (cho biểu đồ)
     */
    public List<Map<String, Object>> getDailyExpenseChart(int year, int month) {
        List<Map<String, Object>> chartData = new ArrayList<>();
        String sql = """
            SELECT 
                DAY(receipt_date) as day,
                SUM(amount) as total_amount,
                COUNT(*) as receipt_count
            FROM expense_receipts
            WHERE YEAR(receipt_date) = ? AND MONTH(receipt_date) = ?
            GROUP BY DAY(receipt_date)
            ORDER BY day
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("day", rs.getInt("day"));
                data.put("total_amount", rs.getBigDecimal("total_amount"));
                data.put("receipt_count", rs.getInt("receipt_count"));
                chartData.add(data);
            }
        } catch (SQLException e) {
            System.err.println("Error getting daily expense chart: " + e.getMessage());
            e.printStackTrace();
        }
        return chartData;
    }

    /**
     * So sánh chi phí giữa các khoảng thời gian
     */
    public Map<String, Object> compareExpensePeriods(LocalDateTime period1Start, LocalDateTime period1End,
                                                   LocalDateTime period2Start, LocalDateTime period2End) {
        Map<String, Object> comparison = new HashMap<>();
        
        BigDecimal period1Total = getTotalExpenseInPeriod(period1Start, period1End);
        BigDecimal period2Total = getTotalExpenseInPeriod(period2Start, period2End);
        
        comparison.put("period1_total", period1Total);
        comparison.put("period2_total", period2Total);
        comparison.put("difference", period2Total.subtract(period1Total));
        
        if (period1Total.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal changePercent = period2Total.subtract(period1Total)
                                     .multiply(BigDecimal.valueOf(100))
                                     .divide(period1Total, 2, BigDecimal.ROUND_HALF_UP);
            comparison.put("change_percent", changePercent);
        } else {
            comparison.put("change_percent", BigDecimal.ZERO);
        }
        
        comparison.put("is_increase", period2Total.compareTo(period1Total) > 0);
        
        return comparison;
    }

    /**
     * Lấy top vendors theo chi phí
     */
    public List<Map<String, Object>> getTopVendorsByExpense(int limit, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> topVendors = new ArrayList<>();
        String sql = """
            SELECT 
                vendor,
                SUM(amount) as total_expense,
                COUNT(*) as receipt_count,
                AVG(amount) as avg_expense,
                MIN(amount) as min_expense,
                MAX(amount) as max_expense
            FROM expense_receipts
            WHERE receipt_date >= ? AND receipt_date <= ?
              AND vendor IS NOT NULL AND vendor != ''
            GROUP BY vendor
            ORDER BY total_expense DESC
            LIMIT ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> vendor = new HashMap<>();
                vendor.put("vendor", rs.getString("vendor"));
                vendor.put("total_expense", rs.getBigDecimal("total_expense"));
                vendor.put("receipt_count", rs.getInt("receipt_count"));
                vendor.put("avg_expense", rs.getBigDecimal("avg_expense"));
                vendor.put("min_expense", rs.getBigDecimal("min_expense"));
                vendor.put("max_expense", rs.getBigDecimal("max_expense"));
                topVendors.add(vendor);
            }
        } catch (SQLException e) {
            System.err.println("Error getting top vendors by expense: " + e.getMessage());
            e.printStackTrace();
        }
        return topVendors;
    }

    /**
     * Lấy thống kê chi phí theo phương thức thanh toán
     */
    public Map<String, BigDecimal> getExpenseByPaymentMethod(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, BigDecimal> result = new HashMap<>();
        String sql = """
            SELECT payment_method, SUM(amount) as total_amount
            FROM expense_receipts
            WHERE receipt_date >= ? AND receipt_date <= ?
            GROUP BY payment_method
            ORDER BY total_amount DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String method = rs.getString("payment_method");
                BigDecimal amount = rs.getBigDecimal("total_amount");
                result.put(method != null ? method : "Không xác định", amount);
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense by payment method: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Kiểm tra có thể xóa phiếu chi không
     */
    public boolean canDeleteExpenseReceipt(int id) {
        // Kiểm tra xem có liên quan đến material_imports không
        String sql = """
            SELECT COUNT(*) FROM expense_receipts er
            WHERE er.id = ? AND er.related_import_id IS NOT NULL
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) == 0; // Có thể xóa nếu không liên quan đến import
            }
        } catch (SQLException e) {
            System.err.println("Error checking if expense receipt can be deleted: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy expense receipts đã hết hạn (quá X ngày)
     */
    public List<ExpenseReceipt> getExpiredReceipts(int daysOld) {
        List<ExpenseReceipt> receipts = new ArrayList<>();
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            WHERE er.receipt_date < DATE_SUB(NOW(), INTERVAL ? DAY)
            ORDER BY er.receipt_date ASC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, daysOld);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                receipts.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting expired receipts: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Cập nhật hàng loạt payment method
     */
    public boolean updatePaymentMethodBatch(List<Integer> receiptIds, String newPaymentMethod) {
        if (receiptIds == null || receiptIds.isEmpty()) {
            return false;
        }
        
        StringBuilder sql = new StringBuilder("UPDATE expense_receipts SET payment_method = ?, updated_at = ? WHERE id IN (");
        for (int i = 0; i < receiptIds.size(); i++) {
            sql.append("?");
            if (i < receiptIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");
        
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setString(1, newPaymentMethod);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            
            for (int i = 0; i < receiptIds.size(); i++) {
                stmt.setInt(i + 3, receiptIds.get(i));
            }
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating payment method batch: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy expense receipt theo created_by
     */
    public List<ExpenseReceipt> getByCreatedBy(int createdBy) {
        List<ExpenseReceipt> receipts = new ArrayList<>();
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            WHERE er.created_by = ?
            ORDER BY er.receipt_date DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, createdBy);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                receipts.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense receipts by created by: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Lấy dashboard summary cho expense
     */
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Tổng số phiếu chi
        summary.put("total_receipts", count());
        
        // Tổng chi phí hôm nay
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = todayStart.plusDays(1).minusSeconds(1);
        summary.put("today_total", getTotalExpenseInPeriod(todayStart, todayEnd));
        
        // Tổng chi phí tháng này
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);
        summary.put("month_total", getTotalExpenseInPeriod(monthStart, monthEnd));
        
        // Chi phí theo category hôm nay
        Map<ExpenseReceipt.ExpenseCategory, BigDecimal> todayByCategory = getExpenseSummaryByCategory(todayStart, todayEnd);
        summary.put("today_by_category", todayByCategory);
        
        // Top 5 vendors tháng này
        List<Map<String, Object>> topVendors = getTopVendorsByExpense(5, monthStart, monthEnd);
        summary.put("top_vendors", topVendors);
        
        return summary;
    }

    /**
     * Lấy chi phí theo khoảng số tiền
     */
    public List<ExpenseReceipt> getByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        List<ExpenseReceipt> receipts = new ArrayList<>();
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            WHERE er.amount >= ? AND er.amount <= ?
            ORDER BY er.amount DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, minAmount);
            stmt.setBigDecimal(2, maxAmount);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                receipts.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense receipts by amount range: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Lấy chi phí theo phương thức thanh toán
     */
    public List<ExpenseReceipt> getByPaymentMethod(String paymentMethod) {
        List<ExpenseReceipt> receipts = new ArrayList<>();
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            WHERE er.payment_method = ?
            ORDER BY er.receipt_date DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paymentMethod);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                receipts.add(mapResultSetWithDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense receipts by payment method: " + e.getMessage());
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Đếm phiếu chi theo điều kiện
     */
    public long countByCondition(String whereClause, Object... params) {
        String sql = "SELECT COUNT(*) FROM expense_receipts er " +
                    "LEFT JOIN employees e ON er.created_by = e.id ";
        
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            sql += "WHERE " + whereClause;
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting expense receipts by condition: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Đếm chi phí trong khoảng thời gian
     */
    public long countByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT COUNT(*) FROM expense_receipts WHERE receipt_date >= ? AND receipt_date <= ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting expense receipts by date range: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy chi phí lớn nhất và nhỏ nhất trong khoảng thời gian
     */
    public Map<String, BigDecimal> getMinMaxExpenseInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, BigDecimal> result = new HashMap<>();
        String sql = """
            SELECT 
                COALESCE(MIN(amount), 0) as min_expense,
                COALESCE(MAX(amount), 0) as max_expense
            FROM expense_receipts 
            WHERE receipt_date >= ? AND receipt_date <= ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                result.put("min_expense", rs.getBigDecimal("min_expense"));
                result.put("max_expense", rs.getBigDecimal("max_expense"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting min/max expense in period: " + e.getMessage());
            e.printStackTrace();
            result.put("min_expense", BigDecimal.ZERO);
            result.put("max_expense", BigDecimal.ZERO);
        }
        return result;
    }

    /**
     * Lấy expense receipt có amount lớn nhất
     */
    public ExpenseReceipt getHighestExpenseReceipt() {
        String sql = """
            SELECT er.*, e.name as created_by_name,
                   CASE WHEN er.related_import_id IS NOT NULL 
                        THEN (SELECT CONCAT(m.name, ' - ', mi.supplier_name) 
                              FROM material_imports mi 
                              JOIN materials m ON mi.material_id = m.id 
                              WHERE mi.id = er.related_import_id)
                        ELSE NULL
                   END as related_material_info
            FROM expense_receipts er
            LEFT JOIN employees e ON er.created_by = e.id
            ORDER BY er.amount DESC
            LIMIT 1
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return mapResultSetWithDetails(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting highest expense receipt: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Kiểm tra vendor có chi phí không
     */
    public boolean hasExpenseForVendor(String vendor) {
        String sql = "SELECT COUNT(*) FROM expense_receipts WHERE vendor = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vendor);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking expense for vendor: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Archive old expense receipts (move to archive table)
     */
    public boolean archiveOldReceipts(LocalDateTime beforeDate) {
        String sql = """
            INSERT INTO expense_receipts_archive 
            SELECT * FROM expense_receipts 
            WHERE receipt_date < ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(beforeDate));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error archiving old receipts: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa expense receipts cũ sau khi archive
     */
    public int deleteArchivedReceipts(LocalDateTime beforeDate) {
        String sql = "DELETE FROM expense_receipts WHERE receipt_date < ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(beforeDate));
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting archived receipts: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Validate expense receipt data before insert/update
     */
    public boolean validateExpenseReceipt(ExpenseReceipt expenseReceipt, boolean isUpdate) {
        // Basic validation
        if (!expenseReceipt.isValid()) {
            return false;
        }
        
        // Check receipt number uniqueness
        if (expenseReceipt.getReceiptNumber() != null) {
            int excludeId = isUpdate ? expenseReceipt.getId() : 0;
            if (isReceiptNumberExists(expenseReceipt.getReceiptNumber(), excludeId)) {
                return false;
            }
        }
        
        // Additional business rules validation can be added here
        return true;
    }

    /**
     * Validate with detailed error message
     */
    public String validateExpenseReceiptData(ExpenseReceipt expenseReceipt) {
        if (expenseReceipt == null) {
            return "Expense receipt không được null";
        }
        
        if (expenseReceipt.getAmount() == null || expenseReceipt.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "Số tiền phải lớn hơn 0";
        }
        
        if (expenseReceipt.getCategory() == null) {
            return "Phải chọn loại chi phí";
        }
        
        if (expenseReceipt.getReceiptDate() == null) {
            return "Ngày chi không được để trống";
        }
        
        if (expenseReceipt.getCreatedBy() <= 0) {
            return "Phải có người tạo phiếu chi";
        }
        
        if (expenseReceipt.getDescription() == null || expenseReceipt.getDescription().trim().isEmpty()) {
            return "Mô tả không được để trống";
        }
        
        // Validate receipt number uniqueness nếu có
        if (expenseReceipt.getReceiptNumber() != null && !expenseReceipt.getReceiptNumber().trim().isEmpty()) {
            int excludeId = expenseReceipt.getId() > 0 ? expenseReceipt.getId() : 0;
            if (isReceiptNumberExists(expenseReceipt.getReceiptNumber(), excludeId)) {
                return "Số phiếu đã tồn tại";
            }
        }
        
        return null; // No validation errors
    }

    /**
     * Enhanced insert với validation
     */
    public boolean insertWithValidation(ExpenseReceipt expenseReceipt) {
        // Validate trước khi insert
        String validationError = validateExpenseReceiptData(expenseReceipt);
        if (validationError != null) {
            System.err.println("Validation failed: " + validationError);
            return false;
        }
        
        return insert(expenseReceipt);
    }

    /**
     * Enhanced update với validation
     */
    public boolean updateWithValidation(ExpenseReceipt expenseReceipt) {
        if (expenseReceipt.getId() <= 0) {
            System.err.println("Cannot update expense receipt: invalid ID");
            return false;
        }
        
        // Validate data
        String validationError = validateExpenseReceiptData(expenseReceipt);
        if (validationError != null) {
            System.err.println("Validation failed: " + validationError);
            return false;
        }
        
        // Set updated timestamp
        expenseReceipt.setUpdatedAt(LocalDateTime.now());
        
        return update(expenseReceipt);
    }

    /**
     * Transaction-safe delete với dependency check
     */
    public boolean safeDelete(int id) {
        try {
            // Start transaction
            conn.setAutoCommit(false);
            
            // Check if can delete
            if (!canDeleteExpenseReceipt(id)) {
                System.err.println("Cannot delete expense receipt - has dependencies");
                conn.rollback();
                return false;
            }
            
            // Perform delete
            boolean deleted = delete(id);
            
            if (deleted) {
                conn.commit();
                System.out.println("Expense receipt deleted successfully");
                return true;
            } else {
                conn.rollback();
                return false;
            }
            
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            System.err.println("Error in safe delete: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error restoring auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Bulk insert expense receipts
     */
    public boolean bulkInsert(List<ExpenseReceipt> expenseReceipts) {
        if (expenseReceipts == null || expenseReceipts.isEmpty()) {
            return false;
        }
        
        String sql = """
            INSERT INTO expense_receipts (
                category, amount, related_import_id, description, receipt_date,
                receipt_number, vendor, payment_method, created_by, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            
            for (ExpenseReceipt expense : expenseReceipts) {
                if (expense.getReceiptNumber() == null || expense.getReceiptNumber().trim().isEmpty()) {
                    expense.setReceiptNumber(generateNextReceiptNumber());
                }
                
                LocalDateTime now = LocalDateTime.now();
                if (expense.getCreatedAt() == null) {
                    expense.setCreatedAt(now);
                }
                if (expense.getUpdatedAt() == null) {
                    expense.setUpdatedAt(now);
                }
                
                setStatementParameters(stmt, expense);
                stmt.setTimestamp(10, Timestamp.valueOf(expense.getCreatedAt()));
                stmt.setTimestamp(11, Timestamp.valueOf(expense.getUpdatedAt()));
                
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            conn.commit();
            
            // Check if all inserts were successful
            for (int result : results) {
                if (result == Statement.EXECUTE_FAILED) {
                    return false;
                }
            }
            return true;
            
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            System.err.println("Error bulk inserting expense receipts: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get expense trend analysis
     */
    public Map<String, Object> getExpenseTrendAnalysis(LocalDateTime startDate, LocalDateTime endDate, 
                                                     String groupBy) {
        Map<String, Object> analysis = new HashMap<>();
        
        String dateFormat;
        switch (groupBy.toLowerCase()) {
            case "day":
                dateFormat = "%Y-%m-%d";
                break;
            case "week":
                dateFormat = "%Y-%u";
                break;
            case "month":
                dateFormat = "%Y-%m";
                break;
            case "year":
                dateFormat = "%Y";
                break;
            default:
                dateFormat = "%Y-%m-%d";
        }
        
        String sql = String.format("""
            SELECT 
                DATE_FORMAT(receipt_date, '%s') as period,
                SUM(amount) as total_amount,
                COUNT(*) as receipt_count,
                AVG(amount) as avg_amount
            FROM expense_receipts 
            WHERE receipt_date >= ? AND receipt_date <= ?
            GROUP BY DATE_FORMAT(receipt_date, '%s')
            ORDER BY period
            """, dateFormat, dateFormat);
        
        List<Map<String, Object>> periodData = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> period = new HashMap<>();
                period.put("period", rs.getString("period"));
                period.put("total_amount", rs.getBigDecimal("total_amount"));
                period.put("receipt_count", rs.getInt("receipt_count"));
                period.put("avg_amount", rs.getBigDecimal("avg_amount"));
                
                periodData.add(period);
                totalAmount = totalAmount.add(rs.getBigDecimal("total_amount"));
                totalCount += rs.getInt("receipt_count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense trend analysis: " + e.getMessage());
            e.printStackTrace();
        }
        
        analysis.put("period_data", periodData);
        analysis.put("total_amount", totalAmount);
        analysis.put("total_count", totalCount);
        analysis.put("overall_average", 
                    totalCount > 0 ? totalAmount.divide(BigDecimal.valueOf(totalCount), 2, BigDecimal.ROUND_HALF_UP) 
                                   : BigDecimal.ZERO);
        analysis.put("group_by", groupBy);
        analysis.put("start_date", startDate);
        analysis.put("end_date", endDate);
        
        return analysis;
    }

    /**
     * Get summary statistics
     */
    public Map<String, Object> getSummaryStatistics() {
        Map<String, Object> summary = new HashMap<>();
        
        // Total count
        summary.put("total_receipts", count());
        
        // Total amount all time
        String totalAmountSql = "SELECT COALESCE(SUM(amount), 0) FROM expense_receipts";
        try (PreparedStatement stmt = conn.prepareStatement(totalAmountSql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                summary.put("total_amount_all_time", rs.getBigDecimal(1));
            }
        } catch (SQLException e) {
            System.err.println("Error getting total amount: " + e.getMessage());
            summary.put("total_amount_all_time", BigDecimal.ZERO);
        }
        
        // Average amount
        String avgSql = "SELECT COALESCE(AVG(amount), 0) FROM expense_receipts";
        try (PreparedStatement stmt = conn.prepareStatement(avgSql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                summary.put("average_amount", rs.getBigDecimal(1));
            }
        } catch (SQLException e) {
            System.err.println("Error getting average amount: " + e.getMessage());
            summary.put("average_amount", BigDecimal.ZERO);
        }
        
        // Count by category
        Map<String, Long> categoryCount = new HashMap<>();
        for (ExpenseReceipt.ExpenseCategory category : ExpenseReceipt.ExpenseCategory.values()) {
            categoryCount.put(category.name(), countByCategory(category));
        }
        summary.put("count_by_category", categoryCount);
        
        // Recent activity (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime now = LocalDateTime.now();
        summary.put("this_week_total", getTotalExpenseInPeriod(weekAgo, now));
        summary.put("this_week_count", countByDateRange(weekAgo, now));
        
        return summary;
    }

    // PRIVATE HELPER METHODS

    /**
     * Set parameters cho PreparedStatement - Enhanced với better error handling
     */
    private void setStatementParameters(PreparedStatement stmt, ExpenseReceipt expenseReceipt) throws SQLException {
        if (expenseReceipt == null) {
            throw new SQLException("ExpenseReceipt cannot be null");
        }
        
        if (expenseReceipt.getCategory() == null) {
            throw new SQLException("Category cannot be null");
        }
        
        stmt.setString(1, expenseReceipt.getCategory().name().toLowerCase());
        stmt.setBigDecimal(2, expenseReceipt.getAmount());
        
        if (expenseReceipt.getRelatedImportId() != null) {
            stmt.setInt(3, expenseReceipt.getRelatedImportId());
        } else {
            stmt.setNull(3, Types.INTEGER);
        }
        
        stmt.setString(4, expenseReceipt.getDescription());
        stmt.setTimestamp(5, Timestamp.valueOf(expenseReceipt.getReceiptDate()));
        stmt.setString(6, expenseReceipt.getReceiptNumber());
        stmt.setString(7, expenseReceipt.getVendor());
        stmt.setString(8, expenseReceipt.getPaymentMethod());
        stmt.setInt(9, expenseReceipt.getCreatedBy());
    }

    /**
     * Map ResultSet thành ExpenseReceipt object với thông tin chi tiết - Enhanced Version
     */
    private ExpenseReceipt mapResultSetWithDetails(ResultSet rs) throws SQLException {
        ExpenseReceipt expenseReceipt = new ExpenseReceipt();
        expenseReceipt.setId(rs.getInt("id"));
        
        // Map category từ string sang enum với better error handling
        String categoryStr = rs.getString("category");
        try {
            expenseReceipt.setCategory(ExpenseReceipt.ExpenseCategory.valueOf(categoryStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Unknown expense category '" + categoryStr + "', defaulting to OTHER");
            expenseReceipt.setCategory(ExpenseReceipt.ExpenseCategory.OTHER);
        }
        
        expenseReceipt.setAmount(rs.getBigDecimal("amount"));
        
        // Handle nullable related_import_id
        int relatedImportId = rs.getInt("related_import_id");
        if (!rs.wasNull()) {
            expenseReceipt.setRelatedImportId(relatedImportId);
        }
        
        expenseReceipt.setDescription(rs.getString("description"));
        expenseReceipt.setReceiptNumber(rs.getString("receipt_number"));
        expenseReceipt.setVendor(rs.getString("vendor"));
        expenseReceipt.setPaymentMethod(rs.getString("payment_method"));
        expenseReceipt.setCreatedBy(rs.getInt("created_by"));
        
        // Handle timestamps với null checking
        Timestamp receiptDate = rs.getTimestamp("receipt_date");
        if (receiptDate != null) {
            expenseReceipt.setReceiptDate(receiptDate.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            expenseReceipt.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            expenseReceipt.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        // Thêm thông tin bổ sung từ JOIN - với error handling
        try {
            String createdByName = rs.getString("created_by_name");
            if (createdByName != null) {
                expenseReceipt.setCreatedByName(createdByName);
            }
            
            String relatedMaterialInfo = rs.getString("related_material_info");
            if (relatedMaterialInfo != null) {
                expenseReceipt.setRelatedMaterialName(relatedMaterialInfo);
            }
        } catch (SQLException e) {
            // Ignore missing joined columns - they might not exist in all queries
            System.out.println("Note: Some joined columns not available in this query");
        }
        
        return expenseReceipt;
    }

    /**
     * Map ResultSet cơ bản thành ExpenseReceipt object (không có join)
     */
    private ExpenseReceipt mapResultSet(ResultSet rs) throws SQLException {
        ExpenseReceipt expenseReceipt = new ExpenseReceipt();
        expenseReceipt.setId(rs.getInt("id"));
        
        // Map category từ string sang enum
        String categoryStr = rs.getString("category");
        try {
            expenseReceipt.setCategory(ExpenseReceipt.ExpenseCategory.valueOf(categoryStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            expenseReceipt.setCategory(ExpenseReceipt.ExpenseCategory.OTHER);
        }
        
        expenseReceipt.setAmount(rs.getBigDecimal("amount"));
        
        // Handle nullable related_import_id
        int relatedImportId = rs.getInt("related_import_id");
        if (!rs.wasNull()) {
            expenseReceipt.setRelatedImportId(relatedImportId);
        }
        
        expenseReceipt.setDescription(rs.getString("description"));
        expenseReceipt.setReceiptNumber(rs.getString("receipt_number"));
        expenseReceipt.setVendor(rs.getString("vendor"));
        expenseReceipt.setPaymentMethod(rs.getString("payment_method"));
        expenseReceipt.setCreatedBy(rs.getInt("created_by"));
        
        // Handle timestamps
        Timestamp receiptDate = rs.getTimestamp("receipt_date");
        if (receiptDate != null) {
            expenseReceipt.setReceiptDate(receiptDate.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            expenseReceipt.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            expenseReceipt.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return expenseReceipt;
    }
    
    public double getTodayTotalExpense() {
        String sql = "SELECT SUM(amount) AS total FROM expense_receipts WHERE DATE(created_at) = CURDATE()";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalExpenseByDay(LocalDate date) {
        String sql = "SELECT SUM(amount) AS total FROM expense_receipts WHERE DATE(created_at) = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<LocalDate, Double> getTotalExpenseByDays(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT DATE(created_at) as expense_date, SUM(amount) AS total FROM expense_receipts WHERE DATE (created_at) BETWEEN ? AND ? GROUP BY DATE(created_at)";
        Map<LocalDate, Double> expenseMap = new HashMap<>(); // dùng map để lưu trữ ngày và tổng chi phí, để sau này nếu có ngày không có value thì có thề set = 0
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(startDate));
            preparedStatement.setDate(2, java.sql.Date.valueOf(endDate));
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                LocalDate date = rs.getDate("expense_date").toLocalDate();
                double total = rs.getDouble("total");
                expenseMap.put(date, total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return expenseMap;
    }


    public double getTotalExpenseByMonth(int month, int year) {
        String sql = "SELECT SUM(amount) AS total FROM expense_receipts WHERE MONTH(created_at) = ? AND YEAR(created_at) = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, month);
            preparedStatement.setInt(2, year);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<Integer, Double> getTotalExpenseByMonths(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT MONTH(created_at) as expense_month, SUM(amount) AS total FROM expense_receipts " +
                "WHERE DATE (created_at) >= ? AND DATE (created_at) < ? GROUP BY expense_month";

        Map<Integer, Double> expenseMap = new HashMap<>();
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(startDate));
            preparedStatement.setDate(2, java.sql.Date.valueOf(endDate));
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                int month = rs.getInt("expense_month");
                double total = rs.getDouble("total");
                expenseMap.put(month, total);
            }
            return expenseMap;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public double getTotalExpenseByYear(int year) {
        String sql = "SELECT SUM(amount) AS total FROM expense_receipts WHERE YEAR(created_at) = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, year);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<Integer, Double> getTotalExpenseByYears(int startYear, int endYear) {
        String sql = "SELECT YEAR(created_at) as expense_year, SUM(amount) AS total FROM expense_receipts " +
                "WHERE YEAR(created_at) >= ? AND YEAR(created_at) <= ? GROUP BY expense_year";

        Map<Integer, Double> expenseMap = new HashMap<>();
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, startYear);
            preparedStatement.setInt(2, endYear);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                int year = rs.getInt("expense_year");
                double total = rs.getDouble("total");
                expenseMap.put(year, total);
            }
            return expenseMap;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
