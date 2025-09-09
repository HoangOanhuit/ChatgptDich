package com.mycompany.quanlyquanan.dao;

import com.mycompany.quanlyquanan.utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VoucherUsageDAO {
    private Connection conn;

    public VoucherUsageDAO() {
        this.conn = DatabaseConnector.getConnection();
    }

    // Các phương thức để tương tác với bảng voucher_usage sẽ được thêm vào đây
    // lấy số lượng voucher đã sử dụng theo voucherId
    public int countVoucherUsageById(int voucherId) {
        String sql = "SELECT COUNT(*) FROM voucher_usage WHERE voucher_id = ?";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, voucherId);
            ResultSet rs = preparedStatement.executeQuery(); // <-- chỉ gọi executeQuery() không truyền sql
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}