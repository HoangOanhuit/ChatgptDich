/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.model;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 *
 * @author Admin
 */
public class WaitingQueue {
    private int id;
    private String customerName;
    private String phone;
    private int guestCount;
    private int queueNumber;
    private String suggestedTables;
    private String status; // waiting, notified, seated, cancelled
    private LocalDateTime requestedAt;
    private LocalDateTime notifiedAt;
    private LocalDateTime seatedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public WaitingQueue() {
    }

    public WaitingQueue(int id, String customerName, String phone, int guestCount, int queueNumber, String suggestedTables, String status, LocalDateTime requestedAt, LocalDateTime notifiedAt, LocalDateTime seatedAt) {
        this.id = id;
        this.customerName = customerName;
        this.phone = phone;
        this.guestCount = guestCount;
        this.queueNumber = queueNumber;
        this.suggestedTables = suggestedTables;
        this.status = status;
        this.requestedAt = requestedAt;
        this.notifiedAt = notifiedAt;
        this.seatedAt = seatedAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(int guestCount) {
        this.guestCount = guestCount;
    }

    public int getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(int queueNumber) {
        this.queueNumber = queueNumber;
    }

    public String getSuggestedTables() {
        return suggestedTables;
    }

    public void setSuggestedTables(String suggestedTables) {
        this.suggestedTables = suggestedTables;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public LocalDateTime getSeatedAt() {
        return seatedAt;
    }

    public void setSeatedAt(LocalDateTime seatedAt) {
        this.seatedAt = seatedAt;
    }
    
   
}
