package com.mycompany.quanlyquanan.controller;

import com.mycompany.quanlyquanan.dao.VoucherDAO;
import com.mycompany.quanlyquanan.service.VoucherService;
import com.mycompany.quanlyquanan.service.VoucherUsageService;

public class VoucherUsageController {
    private VoucherUsageService vcUsageService;

    public VoucherUsageController() {
        this.vcUsageService = new VoucherUsageService();
    }

    public int countVoucherUsageById(int voucherId) {
        return vcUsageService.countVoucherUsageById(voucherId);
    }


}
