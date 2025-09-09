/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.controller;

import com.mycompany.quanlyquanan.dao.OrderDAO;
import com.mycompany.quanlyquanan.model.Order;
import com.mycompany.quanlyquanan.service.OrderService;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tyler
 */
public class OrderController {
    private final OrderService orderService;
    
    public OrderController() {
        this.orderService = new OrderService();
    }
    
    
    public List<Order> getAllOrder() {
        return orderService.getAllOrder();
    }
    
    public List<Order> getAllNewOrder() {
        return orderService.getAllNewOrder();
    }
    
    public List<Order> getAllPreparingOrder() {
        return orderService.getAllPreparingOrder();
    }
    
    public List<Order> getAllDoneOrder() {
        return orderService.getAllDoneOrder();
    }
    
    public List<Order> getAllServedOrder() {
        return orderService.getAllServedOrder();
    }
    
    public List<Order> getAllCanceledOrder() {
        return orderService.getAllServedOrder();
    }
    
    public List<Order> getAllDoneAndCanceledOrder() {
        return orderService.getAllDoneAndCanceledOrder();
    }
    
    public List<Order> getAllNewAndPrep() {
        return orderService.getAllNewPrep();
    }
    
    public List<Order> getAllPrepDone() {
        return orderService.getAllPrepDone();
    }
    
    public List<Order> getUnpaid() {
        return orderService.getAllUnPaid();
    }
    
    public List<Order> getNPDSUOrder() {
        return orderService.getNPDSUOrder();
    }
    
    
    public Order getOrderById(int id) {
        return orderService.getOrderById(id);
    }
    
    public Order getLastestOrder() {     
        return orderService.getLastestOrder();
    }
    
    public boolean create(Order o) {
        return orderService.createOrder(o);
    }
    
    public boolean udpate(Order o) {
        return orderService.updateOrder(o);
    }
    
    public boolean delete(Order o) {
        return orderService.deleteOrder(o);
    }
    
    public boolean setStatus(Order o, String status) {
        return orderService.updateStatus(o, status);
    }
    
     public double getTodayTotalAmount() {
        return orderService.getTodayTotalAmount();
    }

    public double getTotalAmountByDay(LocalDate date) {
        return orderService.getTotalAmountByDay(date);
    }

    public Map<LocalDate, Double> getTotalAmountByDays(LocalDate startDate, LocalDate endDate) {
        return orderService.getTotalAmountByDays(startDate, endDate);
    }

    public List<Order> getDayTopOrderByAmount(LocalDate date) {
        return orderService.getDayTopOrderByAmount(date);
    }

    public double getTotalAmountByMonth(int month, int year) {
        return orderService.getTotalAmountByMonth(month, year);
    }

    public Map<Integer, Double> getTotalAmountByMonths(LocalDate startDate, LocalDate endDate) {
        return orderService.getTotalAmountByMonths(startDate, endDate);
    }

    public List<Order> getMonthTopOrderByAmount(int month, int year) {
        return orderService.getMonthTopOrderByAmount(month, year);
    }

    public double getTotalAmountByYear(int year) {
        return orderService.getTotalAmountByYear(year);
    }

    public Map<Integer, Double> getTotalAmountByYears(int startYear, int endYear) {
        return orderService.getTotalAmountByYears(startYear, endYear);
    }

    public List<Order> getYearTopOrderByAmount(int year) {
        return orderService.getYearTopOrderByAmount(year);
    }

}
