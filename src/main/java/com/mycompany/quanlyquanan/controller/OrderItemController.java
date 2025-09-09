/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.controller;

import com.mycompany.quanlyquanan.model.OrderItem;
import com.mycompany.quanlyquanan.service.OrderItemService;
import java.util.List;

/**
 *
 * @author Tyler
 */
public class OrderItemController {
    private final OrderItemService oIService;
    
    public OrderItemController() {
        this.oIService = new OrderItemService();
    }
    
    public List<OrderItem> getAllOrderItem() {
        return oIService.getAllOrderItem();
    }
    
    public List<OrderItem> getByOrderId(int orderId) {
        return oIService.getAllOrderItemByOrderId(orderId);
    }
    
    public List<OrderItem> allServedByOrderId(int orderId) {
        return oIService.getAllServedItemByOrderId(orderId);
    }
    
    public boolean create(OrderItem orderItem) {
        return oIService.createOrderItem(orderItem);
    }
    
    public boolean delete(OrderItem orderItem) {
        return oIService.deleteOrderItem(orderItem);
    }
    
    public boolean update(OrderItem o) {
        return oIService.updateOrderItem(o);
    }
    
    public boolean setStatus(OrderItem o, String status) {
        return oIService.updateOrderItemStatus(o, status);
    }
    
    
    
    public OrderItem getById(int id) {
        return oIService.getById(id);
    }
}
