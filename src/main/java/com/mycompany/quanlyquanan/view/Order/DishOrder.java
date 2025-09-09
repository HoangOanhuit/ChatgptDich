/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.view.Order;

import com.mycompany.quanlyquanan.model.Dish;

/**
 *
 * @author Tyler
 */
public class DishOrder {
    private Dish dish;
    private int categoryId;
    private int quantity;
    
    public DishOrder(Dish dish, int categoryId, int quantity) {
        this.dish = dish;
        this.categoryId = categoryId;
        this.quantity = quantity;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public String toString() {
        return "{dish: " + dish.getName() + ", category_id: " + categoryId + ", quantity: " + quantity;
    }
}
