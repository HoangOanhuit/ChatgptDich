/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlytruyen.dao;

import com.mycompany.quanlytruyen.model.Account;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        String sql = "SELECT id, username, email, password, note, user_id, uuid, registed, activation_key, auth2, version_ios " +
            "FROM " + tableName + " ORDER BY username";
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
        String sql = "SELECT id, username, email, password, note, user_id, uuid, registed, activation_key, auth2, version_ios " +
            "FROM " + tableName + " WHERE username LIKE ? OR email LIKE ? OR user_id LIKE ? ORDER BY username";
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
        String sql = "INSERT INTO " + tableName +
            " (username, email, password, note, user_id, uuid, registed, activation_key, auth2, version_ios) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindAccountParams(ps, account);
            ps.executeUpdate();
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

    public void updateAccount(Account account) throws SQLException {
        String sql = "UPDATE " + tableName +
            " SET username = ?, email = ?, password = ?, note = ?, user_id = ?, uuid = ?, registed = ?, activation_key = ?, auth2 = ?, version_ios = ?" +
            " WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindAccountParams(ps, account);
            ps.setLong(11, account.getId());
            ps.executeUpdate();
        }
    }

    public void deleteAccount(long id) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private void bindAccountParams(PreparedStatement ps, Account account) throws SQLException {
        ps.setString(1, safeString(account.getUsername()));
        ps.setString(2, safeString(account.getEmail()));
        ps.setString(3, safeString(account.getPassword()));
        ps.setString(4, safeString(account.getNote()));
        ps.setString(5, safeString(account.getUserId()));
        ps.setString(6, safeString(account.getUuid()));
        ps.setString(7, safeString(account.getRegisted()));
        ps.setString(8, safeString(account.getActivationKey()));
        ps.setString(9, safeString(account.getAuth2()));
        ps.setString(10, safeString(account.getVersionIos()));
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getLong("id"));
        account.setUsername(rs.getString("username"));
        account.setEmail(rs.getString("email"));
        account.setPassword(rs.getString("password"));
        account.setNote(rs.getString("note"));
        account.setUserId(rs.getString("user_id"));
        account.setUuid(rs.getString("uuid"));
        account.setRegisted(rs.getString("registed"));
        account.setActivationKey(rs.getString("activation_key"));
        account.setAuth2(rs.getString("auth2"));
        account.setVersionIos(rs.getString("version_ios"));
        return account;
    }

    private String safeString(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
