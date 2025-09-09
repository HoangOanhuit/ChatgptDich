/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.dao.OrderItemDAO;
import com.mycompany.quanlyquanan.model.OrderItem;
import java.util.List;

/**
 *
 * @author Tyler
 */
public class OrderItemService {
    private final OrderItemDAO dao;
    
    public OrderItemService() {
        this.dao = new OrderItemDAO();
    }
    
    public List<OrderItem> getAllOrderItem() {
        return dao.getAll();
    }
    
    public List<OrderItem> getAllOrderItemByOrderId(int orderId) {
        return dao.getAllByOrderId(orderId);
    }
    
    public List<OrderItem> getAllServedItemByOrderId(int orderId) {
        return dao.getAllServedByOrderId(orderId);
    }
    
    public boolean createOrderItem(OrderItem orderItem) {
        return dao.insert(orderItem);
    }
    
    public boolean deleteOrderItem(OrderItem orderItem) {
        return dao.delete(orderItem.getId());
    }
    
    public boolean updateOrderItemStatus(OrderItem o, String status) {
        o.setStatus(status);
        return dao.update(o);
    }
    
    public boolean updateOrderItem(OrderItem o) {
        return dao.update(o);
    }
    
    public OrderItem getById(int id) {
        return dao.getById(id);
    }
}
