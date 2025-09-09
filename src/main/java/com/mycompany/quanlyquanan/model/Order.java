/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author Tyler
 */
public class Order {
    private int id;
    private int table_id;
    private int employee_id;
    private String status;
    private final String[] statuses = {"new", "preparing", "done", "served"};
    private Date created_at;
    private BigDecimal total_amount;
    private boolean paid;
    
    public Order(){
        this.status = statuses[0];
        LocalDate localDate = LocalDate.now();
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.created_at = date;
        this.total_amount = new BigDecimal(0);
        this.paid = false;
    };
    
    public Order(int id, int table_id, int employee_id, String status, BigDecimal total_amount) {
       this.id = id;
       this.table_id = table_id;
       this.employee_id = employee_id;
       if (Arrays.asList(statuses).contains(status))
           this.status = status;
       else
           this.status = statuses[0];
       LocalDate localDate = LocalDate.now();
       Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
       this.created_at = date;
       this.total_amount = total_amount;
       this.paid = false;
    }
    
    public Order(int id, int table_id, int employee_id) {
       this.id = id;
       this.table_id = table_id;
       this.employee_id = employee_id;
       this.status = statuses[0];
       LocalDate localDate = LocalDate.now();
       Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
       this.created_at = date;
       this.total_amount = new BigDecimal(0);
       this.paid = false;
    }
    
    public Order(int id, int table_id, int employee_id, String status, Date created_at, BigDecimal total_amount, boolean paid) {
        this.id = id;
        this.table_id = table_id;
        this.employee_id = employee_id;
        this.created_at = created_at;
        this.status = status;
        this.paid = paid;
        this.total_amount = total_amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTable_id() {
        return table_id;
    }

    public void setTable_id(int table_id) {
        this.table_id = table_id;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public BigDecimal getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(BigDecimal total_amount) {
        this.total_amount = total_amount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
