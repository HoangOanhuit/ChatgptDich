/*
 * Model class cho MaterialImport (Phiếu nhập kho)
 * Quản lý các phiếu nhập nguyên vật liệu vào kho
 */
package com.mycompany.quanlyquanan.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model class cho MaterialImport - Phiếu nhập kho
 * @author Admin
 */
public class MaterialImport {
    private int id;
    private int materialId;
    private BigDecimal importQuantity;
    private BigDecimal importPrice;
    private BigDecimal pricePerUnit;
    private String supplierName;
    private LocalDateTime importDate;
    private int createdBy;
    private String note;
    private String invoiceNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Thông tin bổ sung để hiển thị
    private String materialName;
    private String materialUnit;
    private String createdByName;

    // Constructor không tham số
    public MaterialImport() {
        this.importQuantity = BigDecimal.ZERO;
        this.importPrice = BigDecimal.ZERO;
        this.pricePerUnit = BigDecimal.ZERO;
        this.importDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.invoiceNumber = null;
    }

    // Constructor đầy đủ tham số
    public MaterialImport(int id, int materialId, BigDecimal importQuantity, BigDecimal importPrice,
                         BigDecimal pricePerUnit, String supplierName, LocalDateTime importDate,
                         int createdBy, String note, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.materialId = materialId;
        this.importQuantity = importQuantity;
        this.importPrice = importPrice;
        this.pricePerUnit = pricePerUnit;
        this.supplierName = supplierName;
        this.importDate = importDate;
        this.createdBy = createdBy;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.invoiceNumber = null;
    }

    // Constructor cho tạo mới (không có id)
    public MaterialImport(int materialId, BigDecimal importQuantity, BigDecimal pricePerUnit,
                         String supplierName, int createdBy, String note) {
        this.materialId = materialId;
        this.importQuantity = importQuantity;
        this.pricePerUnit = pricePerUnit;
        this.importPrice = importQuantity.multiply(pricePerUnit);
        this.supplierName = supplierName;
        this.createdBy = createdBy;
        this.note = note;
        this.importDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor với thông tin bổ sung
    public MaterialImport(int id, int materialId, BigDecimal importQuantity, BigDecimal importPrice,
                         String supplierName, LocalDateTime importDate, String materialName,
                         String materialUnit, String createdByName) {
        this.id = id;
        this.materialId = materialId;
        this.importQuantity = importQuantity;
        this.importPrice = importPrice;
        this.supplierName = supplierName;
        this.importDate = importDate;
        this.materialName = materialName;
        this.materialUnit = materialUnit;
        this.createdByName = createdByName;
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getImportQuantity() {
        return importQuantity;
    }

    public void setImportQuantity(BigDecimal importQuantity) {
        this.importQuantity = importQuantity;
        // Tự động tính lại import price nếu có price per unit
        if (this.pricePerUnit != null && this.pricePerUnit.compareTo(BigDecimal.ZERO) > 0) {
            this.importPrice = importQuantity.multiply(this.pricePerUnit);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
        // Tự động tính lại import price
        if (this.importQuantity != null && this.importQuantity.compareTo(BigDecimal.ZERO) > 0) {
            this.importPrice = this.importQuantity.multiply(pricePerUnit);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDateTime importDate) {
        this.importDate = importDate;
        this.updatedAt = LocalDateTime.now();
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
        this.updatedAt = LocalDateTime.now();
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Thông tin bổ sung
    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialUnit() {
        return materialUnit;
    }

    public void setMaterialUnit(String materialUnit) {
        this.materialUnit = materialUnit;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    // Utility methods
    public String getImportQuantityWithUnit() {
        if (materialUnit != null && importQuantity != null) {
            return importQuantity + " " + materialUnit;
        }
        return importQuantity != null ? importQuantity.toString() : "0";
    }

    public String getFormattedImportPrice() {
        if (importPrice != null) {
            return String.format("%,.0f VNĐ", importPrice);
        }
        return "0 VNĐ";
    }

    public String getFormattedPricePerUnit() {
        if (pricePerUnit != null) {
            return String.format("%,.0f VNĐ/%s", pricePerUnit, materialUnit != null ? materialUnit : "đơn vị");
        }
        return "0 VNĐ/đơn vị";
    }

    public String getImportSummary() {
        return String.format("%s - %s (%s) - %s", 
                materialName != null ? materialName : "N/A",
                getImportQuantityWithUnit(),
                supplierName != null ? supplierName : "Không rõ",
                getFormattedImportPrice());
    }

    // Validation methods
    public boolean isValid() {
        return materialId > 0 
            && importQuantity != null && importQuantity.compareTo(BigDecimal.ZERO) > 0
            && pricePerUnit != null && pricePerUnit.compareTo(BigDecimal.ZERO) > 0
            && importDate != null
            && createdBy > 0;
    }
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getValidationError() {
        if (materialId <= 0) return "Chưa chọn nguyên liệu";
        if (importQuantity == null || importQuantity.compareTo(BigDecimal.ZERO) <= 0) 
            return "Số lượng nhập phải lớn hơn 0";
        if (pricePerUnit == null || pricePerUnit.compareTo(BigDecimal.ZERO) <= 0) 
            return "Giá đơn vị phải lớn hơn 0";
        if (importDate == null) return "Chưa chọn ngày nhập";
        if (createdBy <= 0) return "Chưa xác định người tạo phiếu";
        return null;
    }

    @Override
    public String toString() {
        return "MaterialImport{" +
                "id=" + id +
                ", materialId=" + materialId +
                ", importQuantity=" + importQuantity +
                ", importPrice=" + importPrice +
                ", pricePerUnit=" + pricePerUnit +
                ", supplierName='" + supplierName + '\'' +
                ", importDate=" + importDate +
                ", createdBy=" + createdBy +
                ", note='" + note + '\'' +
                ", materialName='" + materialName + '\'' +
                ", materialUnit='" + materialUnit + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MaterialImport that = (MaterialImport) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    public void setNotes(String notes) {
        setNote(notes);
    }

    public void setInvoiceNumber(String invoiceNumber) {
       this.invoiceNumber = invoiceNumber;
       this.updatedAt = LocalDateTime.now();
    }

    public void setSupplier(String supplier) {
        setSupplierName(supplier);
    }
}