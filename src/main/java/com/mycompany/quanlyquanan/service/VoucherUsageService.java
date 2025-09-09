package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.dao.VoucherUsageDAO;

public class VoucherUsageService {
    private VoucherUsageDAO voucherUsageDAO;


    public VoucherUsageService() {
        this.voucherUsageDAO = new VoucherUsageDAO();
    }

    // Lấy số lượng voucher đã sử dụng theo voucherId
    public int countVoucherUsageById(int voucherId) {
        return voucherUsageDAO.countVoucherUsageById(voucherId);
    }
}
