/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.model;

/**
 *
 * @author Tyler
 */
public class OrderItem {
    private int id;
    private int order_id;
    private int dish_id ;
    private int quantity;
    private String[] statuses = {"new", "cooking", "done", "served"};
    private String status;
    
    public OrderItem(){};
    
    public OrderItem(int id, int order_id, int dish_id, int quantity) {
        this.id = id;
        this.order_id = order_id;
        this.dish_id = dish_id;
        this.quantity = quantity;
        this.status = statuses[0];
    }
    
    public OrderItem(int order_id, int dish_id, int quantity) {
        this.order_id = order_id;
        this.dish_id = dish_id;
        this.quantity = quantity;
        this.status = statuses[0];
    }
    
    public OrderItem(int id, int order_id, int dish_id, int quantity, String status) {
        this.id = id;
        this.order_id = order_id;
        this.dish_id = dish_id;
        this.quantity = quantity;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public int getDish_id() {
        return dish_id;
    }

    public void setDish_id(int dish_id) {
        this.dish_id = dish_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String[] getStatuses() {
        return statuses;
    }

    public void setStatuses(String[] statuses) {
        this.statuses = statuses;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    
    
}
