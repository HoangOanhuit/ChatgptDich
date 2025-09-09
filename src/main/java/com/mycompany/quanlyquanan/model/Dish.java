/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class Dish {
    private int id;
    private String name;
    private int categoryId;
    private BigDecimal price;
    private BigDecimal costPrice;
    private String imageUrl;
    private int prepTime;
    private String description;
    private boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Thông tin category (để hiển thị)
    private String categoryName;

    // Constructor không tham số
    public Dish() {
        this.isAvailable = true;
        this.prepTime = 15;
        this.price = BigDecimal.ZERO;
        this.costPrice = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor đầy đủ tham số
    public Dish(int id, String name, int categoryId, BigDecimal price, BigDecimal costPrice,
               String imageUrl, int prepTime, String description, boolean isAvailable,
               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.price = price;
        this.costPrice = costPrice;
        this.imageUrl = imageUrl;
        this.prepTime = prepTime;
        this.description = description;
        this.isAvailable = isAvailable;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor cho tạo mới (không có id)
    public Dish(String name, int categoryId, BigDecimal price, BigDecimal costPrice,
               String imageUrl, int prepTime, String description) {
        this.name = name;
        this.categoryId = categoryId;
        this.price = price;
        this.costPrice = costPrice;
        this.imageUrl = imageUrl;
        this.prepTime = prepTime;
        this.description = description;
        this.isAvailable = true;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
        this.updatedAt = LocalDateTime.now();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public int getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    // Utility methods
    public BigDecimal getProfitMargin() {
        if (price != null && costPrice != null && costPrice.compareTo(BigDecimal.ZERO) > 0) {
            return price.subtract(costPrice);
        }
        return BigDecimal.ZERO;
    }

    public double getProfitPercentage() {
        if (price != null && costPrice != null && costPrice.compareTo(BigDecimal.ZERO) > 0) {
            return getProfitMargin().divide(costPrice, 4, BigDecimal.ROUND_HALF_UP)
                   .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return name != null ? name : "Món ăn chưa đặt tên";
    }
    
    public String toDebugString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", price=" + price +
                ", costPrice=" + costPrice +
                ", imageUrl='" + imageUrl + '\'' +
                ", prepTime=" + prepTime +
                ", description='" + description + '\'' +
                ", isAvailable=" + isAvailable +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Dish dish = (Dish) obj;
        return id == dish.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    public void setCost(BigDecimal cost) {
        setCostPrice(cost);
    }

    public void setActive(boolean selected) {
        setAvailable(selected);
    }

    public void setImagePath(String savedImagePath) {
        setImageUrl(savedImagePath);
    }

    public BigDecimal getCost() {
        return getCostPrice();
    }

    public Object getImagePath() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public boolean isActive() {
        return isAvailable();
    }
}