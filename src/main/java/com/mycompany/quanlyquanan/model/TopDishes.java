package com.mycompany.quanlyquanan.model;

public class TopDishes {
    private int id;
    private String name;
    private int totalQuantity;

    public TopDishes() {

    }
    public TopDishes(int id, String name, int totalQuantity) {
        this.id = id;
        this.name = name;
        this.totalQuantity = totalQuantity;
    }

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
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
