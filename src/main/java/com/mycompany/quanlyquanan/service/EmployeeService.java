/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;
import com.mycompany.quanlyquanan.dao.EmployeeDAO;
import com.mycompany.quanlyquanan.model.Employee;

import java.sql.Connection;
import java.util.List;
/**
 *
 * @author Admin
 */
public class EmployeeService {
     private final EmployeeDAO dao;

    public EmployeeService() {
        this.dao = new EmployeeDAO();
    }

    public List<Employee> getAllEmployees() {
        return dao.getAll();
    }

    public Employee getEmployeeById(int id) {
        return dao.getById(id);
    }

    public boolean createEmployee(Employee e) {
        return dao.insert(e);
    }

    public boolean updateEmployee(Employee e) {
        return dao.update(e);
    }
    
    public boolean updateStatus(Employee e){
        return dao.updateActiveStatus(e);
    }
    
     public boolean updatePass(Employee e){
        return dao.updatePass(e);
    }
    
    
    public List<Employee> search(String keyword) {
    return dao.search(keyword);
    }
    
    public boolean deleteEmployee(int id) {
        return dao.delete(id);
    }
}
