/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.dao;

import com.mycompany.quanlyquanan.model.WaitingQueue;
import com.mycompany.quanlyquanan.utils.DatabaseConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Admin
 */
public class WaitingQueueDAO {

    public void insert(WaitingQueue queue) {
        String sql = "INSERT INTO waiting_queues "
                + "(customer_name, phone, guest_count, queue_number, suggested_tables, status, requested_at, notified_at, seated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, queue.getCustomerName());
            ps.setString(2, queue.getPhone());
            ps.setInt(3, queue.getGuestCount());
            ps.setInt(4, queue.getQueueNumber());
            ps.setString(5, queue.getSuggestedTables());
            ps.setString(6, queue.getStatus());
            ps.setTimestamp(7, queue.getRequestedAt() != null ? Timestamp.valueOf(queue.getRequestedAt()) : null);
            ps.setTimestamp(8, queue.getNotifiedAt() != null ? Timestamp.valueOf(queue.getNotifiedAt()) : null);
            ps.setTimestamp(9, queue.getSeatedAt() != null ? Timestamp.valueOf(queue.getSeatedAt()) : null);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // hoặc log
        }
    }

    public List<WaitingQueue> getAll() {
        List<WaitingQueue> list = new ArrayList<>();
        String sql = "SELECT * FROM waiting_queues  WHERE  DATE(requested_at) = CURDATE()";
        try (Connection conn = DatabaseConnector.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<WaitingQueue> search(String keyword) {
        List<WaitingQueue> list = new ArrayList<>();
        String sql = """
        SELECT * FROM waiting_queues
        WHERE  DATE(requested_at) = CURDATE() 
        AND (customer_name LIKE ? OR phone LIKE ? OR queue_number LIKE ? OR status LIKE ?)
        """;

        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ps.setString(4, kw);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Optional<WaitingQueue> getById(int id) {
        String sql = """
            SELECT
              *
            FROM waiting_queues
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void updateStatus(int id, String newStatus) {
        String sql = "UPDATE waiting_queues SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSuggestTables(int id, String suggestedTable) {
        String sql = "UPDATE waiting_queues SET suggested_tables = ?, notified_at = NOW(),status = 'w-checkin', expire_at = DATE_ADD(NOW(), INTERVAL 20 SECOND) WHERE id = ?";
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, suggestedTable);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM waiting_queues WHERE id = ?";
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private WaitingQueue mapResultSet(ResultSet rs) throws SQLException {
        WaitingQueue q = new WaitingQueue();
        q.setId(rs.getInt("id"));
        q.setCustomerName(rs.getString("customer_name"));
        q.setPhone(rs.getString("phone"));
        q.setGuestCount(rs.getInt("guest_count"));
        q.setQueueNumber(rs.getInt("queue_number"));
        q.setSuggestedTables(rs.getString("suggested_tables"));
        q.setStatus(rs.getString("status"));
        q.setRequestedAt(getDateTime(rs, "requested_at"));
        q.setNotifiedAt(getDateTime(rs, "notified_at"));
        q.setSeatedAt(getDateTime(rs, "seated_at"));
        return q;
    }

    private LocalDateTime getDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    public int getLatestQueueNumberToday() {
        int latest = 0;
        String sql = """
            SELECT COALESCE(MAX(queue_number), 0) AS max_queue
            FROM waiting_queues
            WHERE DATE(requested_at) = CURDATE()
            """;

        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                latest = rs.getInt("max_queue");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return latest;
    }

    public boolean update(WaitingQueue queue) {
        String sql = """
            UPDATE waiting_queues
            SET customer_name = ?, phone = ?, guest_count = ?, status = ?
            WHERE id = ?
            """;
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, queue.getCustomerName());
            ps.setString(2, queue.getPhone());
            ps.setInt(3, queue.getGuestCount());
            ps.setString(4, queue.getStatus());
            ps.setInt(5, queue.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean autoCancelExpired() throws SQLException {
        String sql = "UPDATE waiting_queues "
                + "SET status = 'canceled' "
                + "WHERE status = 'w-checkin' AND expire_at IS NOT NULL AND expire_at < NOW()";
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int updated = ps.executeUpdate();
            if (updated > 0) {
                System.out.println("Da tu dong huy " + updated + " hang cho het han.");
                return true;
            }
            return false;
        }
    }
}
