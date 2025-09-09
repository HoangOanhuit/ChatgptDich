package com.mycompany.quanlyquanan.model;



import java.util.Date;

public class ModelVoucher {
    private int id;
    private String voucherCode;
    private String description;
    private double percentage; //Phần trăm giảm giá
    private int quantity; //Số lượng voucher
    private Date startDate;
    private Date endDate;

    //


    // Constructor không tham số
    public ModelVoucher() {
    }

    // Constructor đầy đủ tham số
    public ModelVoucher(int id, String voucherCode, String description, double percentage, int quantity, Date startDate, Date endDate) {
        this.id = id;
        this.voucherCode = voucherCode;
        this.description = description;
        this.percentage = percentage;
        this.quantity = quantity;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
