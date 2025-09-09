/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.dao;

import com.mycompany.quanlyquanan.model.Table;
import com.mycompany.quanlyquanan.utils.DatabaseConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Admin
 */
public class TableDAO {

    private final Connection conn;

    public TableDAO() {
        this.conn = DatabaseConnector.getConnection();
    }

    public List<Table> getAll() throws SQLException {
        List<Table> list = new ArrayList<>();
        String sql = """
            SELECT t.*,
                   e.name AS employee_name,
                   m.name AS merged_into_table_name
            FROM tables t
            LEFT JOIN employees e ON t.employee_id = e.id
            LEFT JOIN tables m ON t.merged_into_id = m.id
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetWithJoins(rs));
            }
        }
        return list;
    }

    public Table getById(int id) throws SQLException {
        String sql = """
            SELECT t.*,
                   e.name AS employee_name,
                   m.name AS merged_into_table_name
            FROM tables t
            LEFT JOIN employees e ON t.employee_id = e.id
            LEFT JOIN tables m ON t.merged_into_id = m.id
            WHERE t.id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetWithJoins(rs);
                }
            }
        }
        return null;
    }

    public boolean insert(Table t) throws SQLException {
        String sql = "INSERT INTO tables (name, capacity, status, can_merge, location_group, serve_start_time, employee_id, merged_into_id) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setPreparedStatement(ps, t);
            return ps.executeUpdate() > 0;
        }
        
    }

    public boolean update(Table t) throws SQLException {
        String sql = "UPDATE tables SET name=?, capacity=?, status=?, can_merge=?, location_group=?, serve_start_time=?, employee_id=?, merged_into_id=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setPreparedStatement(ps, t);
            ps.setInt(9, t.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Table> search(String keyword) {
        List<Table> list = new ArrayList<>();
        String sql = """
                     SELECT t.*,
                                        e.name AS employee_name,
                                        m.name AS merged_into_table_name
                                 FROM tables t
                                 LEFT JOIN employees e ON t.employee_id = e.id
                                 LEFT JOIN tables m ON t.merged_into_id = m.id
                                 WHERE t.name LIKE ? OR e.name LIKE ? OR m.name LIKE ? 
                                 OR t.capacity LIKE ? OR  t.status LIKE ? OR t.location_group LIKE ?
                     """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String likeKeyword = "%" + keyword + "%";
            stmt.setString(1, likeKeyword);
            stmt.setString(2, likeKeyword);
            stmt.setString(3, likeKeyword);
            stmt.setString(4, likeKeyword);
            stmt.setString(5, likeKeyword);
            stmt.setString(6, likeKeyword);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetWithJoins(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM tables WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /// merge
    
    public Table findByIdForUpdate(Connection con, int id) throws SQLException {
        String sql = "SELECT * FROM tables WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetBasic(rs) : null;
            }
        }

    }
    
    public void updateStatusByName(String tableName, String status) {
    String sql = "UPDATE tables SET status = ? WHERE name = ?";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, status);
        ps.setString(2, tableName);
        ps.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    



    public List<Table> findByIdsForUpdate(Connection con, List<Integer> ids) throws SQLException {
        if (ids.isEmpty()) {
            return List.of();
        }
        String in = ids.stream().map(i -> "?").collect(java.util.stream.Collectors.joining(","));
        String sql = "SELECT * FROM tables WHERE id IN (" + in + ") FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Table> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapResultSetBasic(rs));
                }
                return list;
            }
        }
    }

    public boolean updateTableStatus(int tableId, String newStatus)throws SQLException {
        String sql = "UPDATE tables SET status = ? WHERE id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, newStatus);
            statement.setInt(2, tableId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateAsMergedChild(Connection con, int childId, int rootId, Integer employeeId) throws SQLException {
        String sql = """
            UPDATE tables
               SET merged_into_id = ?,
                   status = 'merged',
                   employee_id = COALESCE(?, employee_id),
                   serve_start_time = NULL
             WHERE id = ?
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, rootId);
            if (employeeId == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, employeeId);
            }
            ps.setInt(3, childId);
            ps.executeUpdate();
        }
    }

    public void updateRootOnMerge(Connection con, int rootId, String status, Integer employeeId, LocalDateTime start) throws SQLException {
        String sql = """
            UPDATE tables
               SET status = ?,
                   employee_id = COALESCE(?, employee_id),
                   serve_start_time = COALESCE(serve_start_time, ?)
                 
             WHERE id = ?
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);

            if (employeeId == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, employeeId);
            }
            ps.setTimestamp(3, start != null ? Timestamp.valueOf(start) : null);
            ps.setInt(4, rootId);
            ps.executeUpdate();
        }
    }

    public List<Table> searchWithGroupCapacity(String keyword) {
        List<Table> list = new ArrayList<>();
        String sql = """
        SELECT  t.*,
                e.name AS employee_name,
                t2.name AS merged_into_table_name,
                CASE
                  WHEN t.merged_into_id IS NULL AND agg.cnt > 1 THEN agg.group_capacity
                  ELSE NULL
                END AS group_capacity
        FROM `tables` t
        LEFT JOIN employees e ON e.id = t.employee_id
        LEFT JOIN `tables` t2 ON t2.id = t.merged_into_id
        LEFT JOIN (
            SELECT COALESCE(merged_into_id, id) AS root_id,
                   SUM(capacity) AS group_capacity,
                   COUNT(*) AS cnt
            FROM `tables`
            GROUP BY COALESCE(merged_into_id, id)
        ) agg ON agg.root_id = t.id
        WHERE (
            t.name LIKE ?
         OR e.name LIKE ?
         OR t2.name LIKE ?
         OR CAST(t.capacity AS CHAR) LIKE ?
         OR t.status LIKE ?
         OR t.location_group LIKE ?
        )
        ORDER BY t.id
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            for (int i = 1; i <= 6; i++) {
                ps.setString(i, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetWithJoins(rs)); // đã xử lý group_capacity
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Table> getAllWithGroupCapacity() throws SQLException {
        List<Table> list = new ArrayList<>();

        String sql
                = """
        SELECT  t.*,
                e.name AS employee_name,
                t2.name AS merged_into_table_name,
                CASE
                  WHEN t.merged_into_id IS NULL AND agg.cnt > 1 THEN agg.group_capacity
                  ELSE NULL
                END AS group_capacity
        FROM `tables` t
        LEFT JOIN employees e ON e.id = t.employee_id
        LEFT JOIN `tables` t2 ON t2.id = t.merged_into_id
        LEFT JOIN (
            SELECT COALESCE(merged_into_id, id) AS root_id,
                   SUM(capacity) AS group_capacity,
                   COUNT(*) AS cnt
            FROM `tables`
            GROUP BY COALESCE(merged_into_id, id)
        ) agg ON agg.root_id = t.id""";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Table t = mapResultSetWithJoins(rs);
                list.add(t);
            }
        }
        return list;
    }

    public List<Table> getAvailableTables() {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM tables WHERE status = 'available'";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Table t = mapResultSetBasic(rs);
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Helper method: Map ResultSet -> Tables object
    private Table mapResultSetWithJoins(ResultSet rs) throws SQLException {
        Table table;
        table = new Table(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("capacity"),
                rs.getString("status"),
                rs.getBoolean("can_merge"),
                rs.getString("location_group"),
                rs.getTimestamp("serve_start_time") != null ? rs.getTimestamp("serve_start_time").toLocalDateTime() : null,
                rs.getObject("employee_id") != null ? rs.getInt("employee_id") : null,
                rs.getObject("merged_into_id") != null ? rs.getInt("merged_into_id") : null,
                rs.getString("employee_name"),
                rs.getString("merged_into_table_name")
        );

        // Xử lý cột group_capacity nếu có
        try {
            Object obj = rs.getObject("group_capacity");
            table.setGroupCapacity(obj != null ? rs.getInt("group_capacity") : null);
        } catch (SQLException e) {
            // Nếu cột không tồn tại thì để null
            table.setGroupCapacity(null);
        }

        return table;
    }

    private Table mapResultSetBasic(ResultSet rs) throws SQLException {
        return new Table(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("capacity"),
                rs.getString("status"),
                rs.getBoolean("can_merge"),
                rs.getString("location_group"),
                rs.getTimestamp("serve_start_time") != null ? rs.getTimestamp("serve_start_time").toLocalDateTime() : null,
                rs.getObject("employee_id") != null ? rs.getInt("employee_id") : null,
                rs.getObject("merged_into_id") != null ? rs.getInt("merged_into_id") : null,
                null,
                null,
                null
        );
    }

    public void unmergeChildren(Connection con, int rootId, List<Integer> childIds) throws SQLException {
        if (childIds.isEmpty()) {
            return;
        }
        String in = childIds.stream().map(i -> "?").collect(java.util.stream.Collectors.joining(","));
        String sql = "UPDATE tables SET merged_into_id = NULL, status = 'available'"
                + "WHERE merged_into_id = ? AND id IN (" + in + ")";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, rootId);
            for (int i = 0; i < childIds.size(); i++) {
                ps.setInt(i + 2, childIds.get(i));
            }
            ps.executeUpdate();
        }
    }

    // Helper method: set PreparedStatement params
    private void setPreparedStatement(PreparedStatement ps, Table t) throws SQLException {
        ps.setString(1, t.getName());
        ps.setInt(2, t.getCapacity());
        ps.setString(3, t.getStatus());
        ps.setBoolean(4, t.isCanMerge());
        ps.setString(5, t.getLocationGroup());
        if (t.getServeStartTime() != null) {
            ps.setTimestamp(6, Timestamp.valueOf(t.getServeStartTime()));
        } else {
            ps.setNull(6, Types.TIMESTAMP);
        }
        if (t.getEmployeeId() != null) {
            ps.setInt(7, t.getEmployeeId());
        } else {
            ps.setNull(7, Types.INTEGER);
        }
        if (t.getMergedIntoId() != null) {
            ps.setInt(8, t.getMergedIntoId());
        } else {
            ps.setNull(8, Types.INTEGER);
        }
    }

    public boolean hasChildTables(int parentTableId) {
        String sql = "SELECT COUNT(*) FROM tables WHERE merged_into_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, parentTableId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
