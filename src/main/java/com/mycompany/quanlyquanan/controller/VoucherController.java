package com.mycompany.quanlyquanan.controller;

import com.mycompany.quanlyquanan.Main;
import com.mycompany.quanlyquanan.model.ModelVoucher;
import com.mycompany.quanlyquanan.service.VoucherService;

import java.util.List;

public class VoucherController {
    private VoucherService service;

    public VoucherController() {
        this.service = new VoucherService();
    }

    public List<ModelVoucher> getAll() {
        return service.getAllVouchers();
    }

    public ModelVoucher getVoucherById(int id) {
        return service.getVoucherById(id);
    }

    public List<ModelVoucher> getVoucherByVoucherCode(String text) {
        return service.getVoucherByVoucherCode(text);
    }

    public void addVoucher(ModelVoucher vc) {
        service.addVoucher(vc);
    }

    public void updateVoucher(ModelVoucher vc) {
        service.updateVoucher(vc);

    }

    public void deleteVoucher(int id) {
        service.deleteVoucher(id);

    }

    public boolean isVoucherCodeExists(String vcCode) {
        return service.isVoucherCodeExists(vcCode);
    }



}
