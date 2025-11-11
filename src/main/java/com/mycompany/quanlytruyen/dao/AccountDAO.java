package com.mycompany.quanlytruyen.dao;

import com.mycompany.quanlytruyen.model.Account;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Administrator
 * DAO thao tác với bảng accounts trong cơ sở dữ liệu.
 */
public class AccountDAO {
    

    private static final String DEFAULT_TABLE = "accounts";

    private final DataSource dataSource;
    private final String tableName;

    public AccountDAO(DataSource dataSource) {
        this(dataSource, DEFAULT_TABLE);
    }

    public AccountDAO(DataSource dataSource, String tableName) {
        this.dataSource = dataSource;
        this.tableName = tableName == null || tableName.isBlank() ? DEFAULT_TABLE : tableName;
    }

    public List<Account> getAllAccounts() throws SQLException {
        String sql = "SELECT account_id, ten_tk, email, mat_khau, uuid, registered, activation_key, auth, version_ios, ghi_chu, total_book, posted_hoan, created_at, updated_at, status " +
            "FROM " + tableName + " ORDER BY ten_tk";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Account> accounts = new ArrayList<>();
            while (rs.next()) {
                accounts.add(mapAccount(rs));
            }
            return accounts;
        }
    }

    public List<Account> searchAccounts(String keyword) throws SQLException {
        String sql = "SELECT account_id, ten_tk, email, mat_khau, uuid, registered, activation_key, auth, version_ios, ghi_chu, total_book, posted_hoan, created_at, updated_at, status " +
            "FROM " + tableName + " WHERE ten_tk LIKE ? OR email LIKE ? OR uuid LIKE ? ORDER BY ten_tk";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                List<Account> accounts = new ArrayList<>();
                while (rs.next()) {
                    accounts.add(mapAccount(rs));
                }
                return accounts;
            }
        }
    }

    public long insertAccount(Account account) throws SQLException {
        boolean hasCustomId = account.getId() != null;
        String columns = "ten_tk, email, mat_khau, uuid, registered, activation_key, auth, version_ios, ghi_chu, total_book, posted_hoan, status";
        String placeholders = "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";
        String sql = "INSERT INTO " + tableName +
            (hasCustomId
                ? " (account_id, " + columns + ") VALUES (?, " + placeholders + ")"
                : " (" + columns + ") VALUES (" + placeholders + ")");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindAccountParams(ps, account);
            int index = 1;
            if (hasCustomId) {
                ps.setLong(index++, account.getId());
            }
            bindAccountParams(ps, account, index);
            ps.executeUpdate();
            if (hasCustomId) {
                return account.getId();
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    account.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Không thể lấy ID của tài khoản vừa thêm");
    }

    public void updateAccount(Account account, Long originalId) throws SQLException {
        if (originalId == null) {
            throw new SQLException("Thiếu account_id gốc để cập nhật");
        }
        String sql = "UPDATE " + tableName +
            " WHERE account_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            if (account.getId() == null) {
                throw new SQLException("Thiếu account_id mới để cập nhật");
            }
            ps.setLong(index++, account.getId());
            index = bindAccountParams(ps, account, index);
            ps.setLong(index, originalId);
            ps.executeUpdate();
        }
    }


    private int bindAccountParams(PreparedStatement ps, Account account) throws SQLException {
        return bindAccountParams(ps, account, 1);
    }

    private int bindAccountParams(PreparedStatement ps, Account account, int index) throws SQLException {
        ps.setString(index++, safeString(account.getUsername()));
        ps.setString(index++, safeString(account.getEmail()));
        ps.setString(index++, safeString(account.getPassword()));
        ps.setString(index++, safeString(account.getUuid()));
        setTimestamp(ps, index++, account.getRegistered(), true);
        ps.setString(index++, safeString(account.getActivationKey()));
        ps.setString(index++, safeString(account.getAuth()));
        ps.setString(index++, safeString(account.getVersionIos()));
        ps.setString(index++, safeString(account.getNote()));
        setInteger(ps, index++, account.getTotalBook(), 0);
        setInteger(ps, index++, account.getPostedHoan(), 0);
        ps.setString(index++, safeString(defaultStatus(account.getStatus())));
        return index;
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getLong("account_id"));
        account.setUsername(rs.getString("ten_tk"));
        account.setEmail(rs.getString("email"));
        account.setPassword(rs.getString("mat_khau"));
        account.setUuid(rs.getString("uuid"));
        account.setRegistered(timestampToString(rs.getTimestamp("registered")));
        account.setActivationKey(rs.getString("activation_key"));
        account.setAuth(rs.getString("auth"));
        account.setVersionIos(rs.getString("version_ios"));
        account.setNote(rs.getString("ghi_chu"));
        account.setTotalBook(getNullableInt(rs, "total_book"));
        account.setPostedHoan(getNullableInt(rs, "posted_hoan"));
        account.setCreatedAt(timestampToString(rs.getTimestamp("created_at")));
        account.setUpdatedAt(timestampToString(rs.getTimestamp("updated_at")));
        account.setStatus(rs.getString("status"));
        return account;
    }

    private String safeString(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
    private void setTimestamp(PreparedStatement ps, int index, String value, boolean defaultNow) throws SQLException {
        if (value == null || value.isBlank()) {
            if (defaultNow) {
                ps.setTimestamp(index, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                ps.setNull(index, Types.TIMESTAMP);
            }
            return;
        }
        try {
            ps.setTimestamp(index, Timestamp.valueOf(value.trim()));
        } catch (IllegalArgumentException ex) {
            throw new SQLException("Định dạng thời gian không hợp lệ: " + value, ex);
        }
    }

    private void setInteger(PreparedStatement ps, int index, Integer value, Integer defaultValue) throws SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else if (defaultValue != null) {
            ps.setInt(index, defaultValue);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    private String timestampToString(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime().toString();
    }

    private Integer getNullableInt(ResultSet rs, String columnLabel) throws SQLException {
        int value = rs.getInt(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    private String defaultStatus(String status) {
        return status == null || status.isBlank() ? "active" : status.trim();
    }    

    public void deleteAccount(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
