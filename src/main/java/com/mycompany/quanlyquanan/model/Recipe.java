
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class Recipe {
    private int id;
    private int dishId;
    private int materialId;
    private BigDecimal quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Thông tin bổ sung để hiển thị
    private String dishName;
    private String materialName;
    private String materialUnit;
    private BigDecimal materialPricePerUnit;

    // Constructor không tham số
    public Recipe() {
        this.quantity = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor đầy đủ tham số
    public Recipe(int id, int dishId, int materialId, BigDecimal quantity,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.dishId = dishId;
        this.materialId = materialId;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor cho tạo mới (không có id)
    public Recipe(int dishId, int materialId, BigDecimal quantity) {
        this.dishId = dishId;
        this.materialId = materialId;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor với thông tin bổ sung
    public Recipe(int id, int dishId, int materialId, BigDecimal quantity,
                 String dishName, String materialName, String materialUnit, 
                 BigDecimal materialPricePerUnit) {
        this.id = id;
        this.dishId = dishId;
        this.materialId = materialId;
        this.quantity = quantity;
        this.dishName = dishName;
        this.materialName = materialName;
        this.materialUnit = materialUnit;
        this.materialPricePerUnit = materialPricePerUnit;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDishId() {
        return dishId;
    }

    public void setDishId(int dishId) {
        this.dishId = dishId;
        this.updatedAt = LocalDateTime.now();
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
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

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

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

    public BigDecimal getMaterialPricePerUnit() {
        return materialPricePerUnit;
    }

    public void setMaterialPricePerUnit(BigDecimal materialPricePerUnit) {
        this.materialPricePerUnit = materialPricePerUnit;
    }

    // Utility methods
    public BigDecimal getTotalCost() {
        if (materialPricePerUnit != null && quantity != null) {
            return materialPricePerUnit.multiply(quantity);
        }
        return BigDecimal.ZERO;
    }

    public String getQuantityWithUnit() {
        if (materialUnit != null && quantity != null) {
            return quantity + " " + materialUnit;
        }
        return quantity != null ? quantity.toString() : "0";
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", dishId=" + dishId +
                ", materialId=" + materialId +
                ", quantity=" + quantity +
                ", dishName='" + dishName + '\'' +
                ", materialName='" + materialName + '\'' +
                ", materialUnit='" + materialUnit + '\'' +
                ", materialPricePerUnit=" + materialPricePerUnit +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Recipe recipe = (Recipe) obj;
        return id == recipe.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}