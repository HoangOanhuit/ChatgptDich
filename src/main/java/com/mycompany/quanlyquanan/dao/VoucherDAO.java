package com.mycompany.quanlyquanan.dao;

import com.mycompany.quanlyquanan.model.ModelVoucher;
import com.mycompany.quanlyquanan.utils.DatabaseConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoucherDAO {

    private Connection conn;

    public VoucherDAO() {
        this.conn = DatabaseConnector.getConnection();
    }

    public List<ModelVoucher> getAll() {
        List<ModelVoucher> vouchers = new ArrayList<>();
        String sql = "SELECT * FROM vouchers ORDER BY id DESC";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery(sql);
            while (rs.next()) {
                ModelVoucher vc = new ModelVoucher();
                vc.setId(rs.getInt("id"));
                vc.setVoucherCode(rs.getString("voucher_code"));
                vc.setDescription(rs.getString("description"));
                vc.setPercentage(rs.getDouble("percentage"));
                vc.setQuantity(rs.getInt("quantity"));
                vc.setStartDate(rs.getDate("start_date"));
                vc.setEndDate(rs.getDate("end_date"));
                vouchers.add(vc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vouchers;
    }

    public ModelVoucher getVoucherById(int id) {
        String sql = "SELECT * FROM vouchers WHERE id = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                ModelVoucher vc = new ModelVoucher();
                vc.setId(rs.getInt("id"));
                vc.setVoucherCode(rs.getString("voucher_code"));
                vc.setDescription(rs.getString("description"));
                vc.setPercentage(rs.getDouble("percentage"));
                vc.setQuantity(rs.getInt("quantity"));
                vc.setStartDate(rs.getDate("start_date"));
                vc.setEndDate(rs.getDate("end_date"));
                return vc;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ModelVoucher> getVoucherByVoucherCode(String text) {
        List<ModelVoucher> vouchers = new ArrayList<>();
        String sql = "SELECT * FROM vouchers WHERE voucher_code LIKE ?";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, "%" + text + "%");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                ModelVoucher vc = new ModelVoucher();
                vc.setId(rs.getInt("id"));
                vc.setVoucherCode(rs.getString("voucher_code"));
                vc.setDescription(rs.getString("description"));
                vc.setPercentage(rs.getDouble("percentage"));
                vc.setQuantity(rs.getInt("quantity"));
                vc.setStartDate(rs.getDate("start_date"));
                vc.setEndDate(rs.getDate("end_date"));
                vouchers.add(vc);
           
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return vouchers;
    }

    public void addVoucher(ModelVoucher voucher) {
        String sql = "INSERT INTO vouchers (voucher_code, description, percentage, quantity, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, voucher.getVoucherCode());
            preparedStatement.setString(2, voucher.getDescription());
            preparedStatement.setDouble(3, voucher.getPercentage());
            preparedStatement.setInt(4, voucher.getQuantity());
            preparedStatement.setDate(5, new java.sql.Date(voucher.getStartDate().getTime()));
            preparedStatement.setDate(6, new java.sql.Date(voucher.getEndDate().getTime()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateVoucher(ModelVoucher voucher) {
        String sql = "UPDATE vouchers SET voucher_code = ?, description = ?, percentage = ?, quantity = ?, start_date = ?, end_date = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, voucher.getVoucherCode());
            preparedStatement.setString(2, voucher.getDescription());
            preparedStatement.setDouble(3, voucher.getPercentage());
            preparedStatement.setInt(4, voucher.getQuantity());
            preparedStatement.setDate(5, new java.sql.Date(voucher.getStartDate().getTime()));
            preparedStatement.setDate(6, new java.sql.Date(voucher.getEndDate().getTime()));
            preparedStatement.setInt(7, voucher.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteVoucher(int id) {
        String sql = "DELETE FROM vouchers WHERE id = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isVoucherCodeExists(String vcCode) {
        String sql = "SELECT 1 FROM vouchers WHERE voucher_code = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, vcCode);
            ResultSet rs = preparedStatement.executeQuery();
            return rs.next(); // Nếu có kết quả trả về, voucher code đã tồn tại
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
