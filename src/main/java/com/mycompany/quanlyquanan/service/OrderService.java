/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.dao.OrderDAO;
import com.mycompany.quanlyquanan.model.Order;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tyler
 */
public class OrderService {
    private final OrderDAO dao;
    
    public OrderService(){
        this.dao = new OrderDAO();
    }
    
    public List<Order> getAllOrder() {
        return dao.getAll();
    }
    
    public List<Order> getAllNewOrder() {
        return dao.getAllNewOrder();
    }
    
    public List<Order> getAllPreparingOrder() {
        return dao.getAllPreparingOrder();
    }
    
    public List<Order> getAllDoneOrder() {
        return dao.getAllDoneOrder();
    }
    
    public List<Order> getAllServedOrder() {
        return dao.getAllServedOrder();
    }
    
    public List<Order> getAllCanceledOrder() {
        return dao.getAllServedOrder();
    }
    
    public List<Order> getAllDoneAndCanceledOrder() {
        return dao.getAllDoneAndCanceledOrder();
    }
    
    public List<Order> getAllNewPrep() {
        return dao.getAllNewPrep();
    }
    
    public List<Order> getAllUnPaid() {
        return dao.getAllUnpaidServedOrder();
    }
    
    public List<Order> getNPDSUOrder() {
        return dao.getNPDSUOrder();
    }
    
    public List<Order> getAllPrepDone() {
        return dao.getAllPrepDoneOrder();
    }
    
    public Order getOrderById(int id) {
        return dao.getById(id);
    }
    
    public Order getLastestOrder() {
        List<Order> ol = getAllOrder();
        return ol.stream()
                .max(Comparator
                .comparing(Order::getId)).orElse(null);
    }
    
    public boolean createOrder(Order o) {
        return dao.insert(o);
    }
    
    public boolean updateOrder(Order o) {
        return dao.update(o);
    }
    
    public boolean deleteOrder(Order o) {
        return dao.delete(o);
    }
    
    public boolean updateStatus(Order o, String status) {
        o.setStatus(status);
        return dao.update(o);
    }
    public double getTodayTotalAmount() {
        return dao.getTodayTotalAmount();
    }

    public double getTotalAmountByDay(LocalDate date) {
        return dao.getTotalAmountByDay(date);
    }

    public Map<LocalDate, Double> getTotalAmountByDays(LocalDate startDate, LocalDate endDate) {
        return dao.getTotalAmountByDays(startDate, endDate);

    }

    public List<Order> getDayTopOrderByAmount(LocalDate date) {
        return dao.getDayTopOrderByAmount(date);
    }

    public double getTotalAmountByMonth(int month, int year) {

        return dao.getTotalAmountByMonth(month, year);
    }

    public Map<Integer, Double> getTotalAmountByMonths(LocalDate startDate, LocalDate endDate) {
        return dao.getTotalAmountByMonths(startDate, endDate);
    }

    public List<Order> getMonthTopOrderByAmount(int month, int year) {
        return dao.getMonthTopOrderByAmount(month, year);
    }

    public double getTotalAmountByYear(int year) {
        return dao.getTotalAmountByYear(year);
    }

    public Map<Integer, Double> getTotalAmountByYears(int startYear, int endYear) {
        return dao.getTotalAmountByYears(startYear, endYear);
    }

    public List<Order> getYearTopOrderByAmount(int year) {
        return dao.getYearTopOrderByAmount(year);
    }
}
