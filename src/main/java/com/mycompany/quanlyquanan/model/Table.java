/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.model;

import java.time.LocalDateTime;

/**
 *
 * @author Admin
 */
public class Table {

    private int id;
    private String name;
    private int capacity;
    private String status; // available, serving, waiting_payment, merged
    private boolean canMerge;
    private String locationGroup;
    private LocalDateTime serveStartTime;
    private Integer employeeId;     // Có thể null
    private Integer mergedIntoId;   // Có thể null
    private String employeeName;
    private String mergedIntoTableName;
    private Integer groupCapacity;

    // Constructors
    public Table() {
    }

    public Table(int id, String name, int capacity, String status, boolean canMerge,
            String locationGroup, LocalDateTime serveStartTime,
            Integer employeeId, Integer mergedIntoId, String employeeName, String mergedIntoTableName, Integer groupCapacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.status = status;
        this.canMerge = canMerge;
        this.locationGroup = locationGroup;
        this.serveStartTime = serveStartTime;
        this.employeeId = employeeId;
        this.mergedIntoId = mergedIntoId;
        this.employeeName = employeeName;
        this.mergedIntoTableName = mergedIntoTableName;
        this.groupCapacity = groupCapacity;
    }

    public Table(int id, String name, int capacity, String status, boolean canMerge,
            String locationGroup, LocalDateTime serveStartTime,
            Integer employeeId, Integer mergedIntoId,
            String employeeName, String mergedIntoTableName) {
        this(id, name, capacity, status, canMerge, locationGroup, serveStartTime,
                employeeId, mergedIntoId, employeeName, mergedIntoTableName, null);
    }

    public Integer getGroupCapacity() {
        return groupCapacity;
    }

    public void setGroupCapacity(Integer groupCapacity) {
        this.groupCapacity = groupCapacity;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getMergedIntoTableName() {
        return mergedIntoTableName;
    }

    public void setMergedIntoTableName(String mergedIntoTableName) {
        this.mergedIntoTableName = mergedIntoTableName;
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCanMerge() {
        return canMerge;
    }

    public void setCanMerge(boolean canMerge) {
        this.canMerge = canMerge;
    }

    public String getLocationGroup() {
        return locationGroup;
    }

    public void setLocationGroup(String locationGroup) {
        this.locationGroup = locationGroup;
    }

    public LocalDateTime getServeStartTime() {
        return serveStartTime;
    }

    public void setServeStartTime(LocalDateTime serveStartTime) {
        this.serveStartTime = serveStartTime;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getMergedIntoId() {
        return mergedIntoId;
    }

    public void setMergedIntoId(Integer mergedIntoId) {
        this.mergedIntoId = mergedIntoId;
    }

}
