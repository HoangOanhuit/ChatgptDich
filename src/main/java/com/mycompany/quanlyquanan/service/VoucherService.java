package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.dao.VoucherDAO;
import com.mycompany.quanlyquanan.model.ModelVoucher;

import java.util.List;

public class VoucherService {
    private VoucherDAO voucherDAO;

    public VoucherService() {
        this.voucherDAO = new VoucherDAO();
    }

    public List<ModelVoucher> getAllVouchers() {
        return voucherDAO.getAll();
    }

    public ModelVoucher getVoucherById(int id) {
        return voucherDAO.getVoucherById(id);
    }

    public List<ModelVoucher> getVoucherByVoucherCode(String text) {
        return voucherDAO.getVoucherByVoucherCode(text);
    }

    public void addVoucher(ModelVoucher voucher) {
        if (voucher.getVoucherCode() == null || voucher.getVoucherCode().isEmpty()) {
            throw new IllegalArgumentException("Voucher code cannot be null or empty");
        } else if (voucher.getDescription() == null || voucher.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        } else if (voucher.getPercentage() < 0 || voucher.getPercentage() > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        } else if (voucher.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        } else if (voucher.getStartDate() == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        } else if (voucher.getEndDate() == null) {
            throw new IllegalArgumentException("End date cannot be null");
        } else if (voucher.getEndDate().before(voucher.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        } else if (isVoucherCodeExists(voucher.getVoucherCode())) {
            throw new IllegalArgumentException("Voucher code exists already");
        } else {
            voucherDAO.addVoucher(voucher);
        }
    }

    public void updateVoucher(ModelVoucher voucher) {
        if (voucher.getVoucherCode() == null || voucher.getVoucherCode().isEmpty()) {
            throw new IllegalArgumentException("Voucher code cannot be null or empty");
        } else if (voucher.getDescription() == null || voucher.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        } else if (voucher.getPercentage() < 0 || voucher.getPercentage() > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        } else if (voucher.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        } else if (voucher.getStartDate() == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        } else if (voucher.getEndDate() == null) {
            throw new IllegalArgumentException("End date cannot be null");
        } else if (voucher.getEndDate().before(voucher.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        } else {
            voucherDAO.updateVoucher(voucher);
        }
    }

    public void deleteVoucher(int id) {
        if (id <= 0 || voucherDAO.getVoucherById(id) == null) {
            throw new IllegalArgumentException("Invalid voucher ID");
        } else {
            voucherDAO.deleteVoucher(id);
        }
    }

    public boolean isVoucherCodeExists(String vcCode) {
        return voucherDAO.isVoucherCodeExists(vcCode);
    }
}
