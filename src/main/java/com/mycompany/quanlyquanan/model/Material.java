package com.mycompany.quanlyquanan.model;

import java.math.BigDecimal;

import java.time.LocalDateTime;

/**
 * Model class cho Material (Nguyên liệu)
 * @author Admin
 */
public class Material {
    private int id;
    private String name;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal threshold;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    //private String category; 
    //private LocalDate expiryDate; // Ngày hết hạn
    //private String description; // Mô tả nguyên liệu
    private int createdBy;
    private int updatedBy;
    //private String supplier;


    // Constructor không tham số
    public Material() {
        this.quantity = BigDecimal.ZERO;
        this.pricePerUnit = BigDecimal.ZERO;
        this.threshold = BigDecimal.TEN; // Default threshold = 10
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdBy = 0;
        this.updatedBy = 0;
        this.createdBy = 0;
        this.updatedBy = 0;
    }

    // Constructor đầy đủ tham số

    /**
     *
     * @param id
     * @param name
     * @param unit
     * @param quantity
     * @param pricePerUnit
     * @param threshold
     * @param isActive
     * @param createdAt
     * @param updatedAt
     */
    public Material(int id, String name, String unit, BigDecimal quantity,
                   BigDecimal pricePerUnit, BigDecimal threshold, boolean isActive,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.threshold = threshold;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = 0;
        this.updatedBy = 0;
    }


    // Constructor cho tạo mới (không có id)
    public Material(String name, String unit, BigDecimal quantity,
                   BigDecimal pricePerUnit, BigDecimal threshold) {
        this.name = name;
        this.unit = unit;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.threshold = threshold;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdBy = 0;
        this.updatedBy = 0;        
    }

    // Constructor đơn giản
    public Material(String name, String unit, BigDecimal pricePerUnit) {
        this.name = name;
        this.unit = unit;
        this.quantity = BigDecimal.ZERO;
        this.pricePerUnit = pricePerUnit;
        this.threshold = BigDecimal.TEN;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdBy = 0;
        this.updatedBy = 0;        
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getUnit() {
        return unit;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }
    


    public void setUnit(String unit) {
        this.unit = unit;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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


    public String getDisplayName() {
        return isActive ? name : name + " (Tạm ngưng)";
    }

    public void addQuantity(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.quantity = this.quantity.add(amount);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void subtractQuantity(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0 && 
            this.quantity.compareTo(amount) >= 0) {
            this.quantity = this.quantity.subtract(amount);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public boolean hasEnoughQuantity(BigDecimal requiredAmount) {
        return this.quantity.compareTo(requiredAmount) >= 0;
    }
  
    /*
    public String getCategory() {
        return category;
    }


    public void setCategory(String category) {
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }
    */

    /**
     * Lấy ngày hết hạn
     */
    /*
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    */

    /**
     * Set ngày hết hạn
     */
    /*
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        this.updatedAt = LocalDateTime.now();
    }
    */
    
    /*

    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    */

    // ==================== METHOD BỊ THIẾU ====================

    /**
     * Lấy giá đơn vị (alias cho getPricePerUnit)
     */
    public BigDecimal getUnitPrice() {
        return this.pricePerUnit;
    }

    /**
     * Set giá đơn vị (alias cho setPricePerUnit)
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        this.pricePerUnit = unitPrice;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Lấy tổng giá trị tồn kho (quantity * unitPrice)
     */
    public BigDecimal getTotalValue() {
        if (quantity == null || pricePerUnit == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(pricePerUnit);
    }

    /**
     * Kiểm tra có phải low stock không
     */
    public boolean isLowStock() {
        if (quantity == null || threshold == null) {
            return false;
        }
        return quantity.compareTo(threshold) <= 0;
    }

    /**
     * Kiểm tra có phải out of stock không
     */
    public boolean isOutOfStock() {
        return quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Kiểm tra sắp hết hạn (trong vòng X ngày)
     */
    /*
    public boolean isExpiringSoon(int days) {
        if (expiryDate == null) {
            return false;
        }
        LocalDate checkDate = LocalDate.now().plusDays(days);
        return expiryDate.isBefore(checkDate) || expiryDate.isEqual(checkDate);
    }
    /*
    /**
     * Kiểm tra đã hết hạn
     */
    /*
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.isBefore(LocalDate.now());
    }
    */
    /**
     * Format quantity với unit
     */
    public String getQuantityWithUnit() {
        if (quantity == null || unit == null) {
            return "0";
        }
        return quantity.toString() + " " + unit;
    }

    /**
     * Format price với currency
     */
    public String getFormattedPrice() {
        if (pricePerUnit == null) {
            return "0 ₫";
        }
        return String.format("%,d ₫", pricePerUnit.intValue());
    }

    /**
     * Format total value
     */
    public String getFormattedTotalValue() {
        BigDecimal total = getTotalValue();
        return String.format("%,d ₫", total.intValue());
    }

    /**
     * Get stock status as string
     */
    public String getStockStatus() {
        if (isOutOfStock()) {
            return "HẾT HÀNG";
        } else if (isLowStock()) {
            return "SẮP HẾT";
        } else {
            return "CÒN HÀNG";
        }
    }

    /**
     * Get stock status color
     */
    public String getStockStatusColor() {
        if (isOutOfStock()) {
            return "#dc3545"; // Red
        } else if (isLowStock()) {
            return "#ffc107"; // Yellow/Orange
        } else {
            return "#28a745"; // Green
        }
    }

    /**
     * Validate material data
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty()
               && unit != null && !unit.trim().isEmpty()
               && pricePerUnit != null && pricePerUnit.compareTo(BigDecimal.ZERO) >= 0
               && quantity != null && quantity.compareTo(BigDecimal.ZERO) >= 0
               && threshold != null && threshold.compareTo(BigDecimal.ZERO) > 0;
    }
    

    /**
     * Get validation errors
     */
    public String getValidationError() {
        if (name == null || name.trim().isEmpty()) {
            return "Tên nguyên liệu không được để trống";
        }
        if (unit == null || unit.trim().isEmpty()) {
            return "Đơn vị tính không được để trống";
        }
        if (pricePerUnit == null || pricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            return "Giá đơn vị phải >= 0";
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            return "Số lượng phải >= 0";
        }
        if (threshold == null || threshold.compareTo(BigDecimal.ZERO) <= 0) {
            return "Ngưỡng cảnh báo phải > 0";
        }
        return null;
    }
    
    public void setCreatedBy(int id) {
        this.createdBy = id;
    }

    public void setUpdatedBy(int id) {
        this.updatedBy = id;
        this.updatedAt = LocalDateTime.now();
    }
    
    /*
    public void setSupplier(String supplier) {
        this.supplier = supplier;
        this.updatedAt = LocalDateTime.now();
    }
    */
    
    /**
     * Clone material
     */
    public Material clone() {
        Material cloned = new Material(
            this.id, this.name, /*this.category,*/ this.unit, this.quantity,
            this.pricePerUnit, this.threshold, this.isActive,
            /*this.expiryDate,*/ /*this.description,*/ this.createdAt, this.updatedAt
        );
        cloned.setCreatedBy(this.createdBy);
        cloned.setUpdatedBy(this.updatedBy);
        //cloned.setSupplier(this.supplier);
        return cloned;
    }
    /**
    * toString for debugging
    */
    @Override
    public String toString() {
        return name != null ? name : "Nguyên liệu chưa đặt tên";
    }
    
    public String toDebugString() {
        return "Material{" +
               "id=" + id +
               ", name='" + name + '\'' +
               //", category='" + category + '\'' +
               ", quantity=" + quantity +
               ", unit='" + unit + '\'' +
               ", unitPrice=" + pricePerUnit +
               ", isLowStock=" + isLowStock() +
               ", isActive=" + isActive +
               '}';
    }

    /**
     * equals based on id
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Material material = (Material) obj;
        return id == material.id;
    }

    /**
     * hashCode based on id
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    /*
    public String getSupplier() {
        return supplier;
    }
    */

   
}