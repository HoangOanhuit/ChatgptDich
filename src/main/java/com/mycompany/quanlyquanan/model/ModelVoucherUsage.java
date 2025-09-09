package com.mycompany.quanlyquanan.model;

import java.util.Date;

public class ModelVoucherUsage {
    private int id;
    private int voucher_id;
    private String order_id;
    private Date used_at;
    private double discount_amount;

    // Constructor không tham số


    public ModelVoucherUsage(double discount_amount, Date used_at, String order_id, int voucherId, int id) {
        this.discount_amount = discount_amount;
        this.used_at = used_at;
        this.order_id = order_id;
        this.voucher_id = voucherId;
        this.id = id;
    }

    public ModelVoucherUsage() {
    }

    // Getter & Setter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVoucher_id() {
        return voucher_id;
    }

    public void setVoucher_id(int voucher_id) {
        this.voucher_id = voucher_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public Date getUsed_at() {
        return used_at;
    }

    public void setUsed_at(Date used_at) {
        this.used_at = used_at;
    }

    public double getDiscount_amount() {
        return discount_amount;
    }

    public void setDiscount_amount(double discount_amount) {
        this.discount_amount = discount_amount;
    }
}
