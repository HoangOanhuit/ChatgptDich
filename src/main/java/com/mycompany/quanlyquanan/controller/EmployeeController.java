package com.mycompany.quanlyquanan.controller;
import com.mycompany.quanlyquanan.model.Employee;
import com.mycompany.quanlyquanan.service.EmployeeService;
import java.util.List;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Admin
 */
public class EmployeeController {
    private final EmployeeService service;

    public EmployeeController() {
        this.service = new EmployeeService();
    }

    public List<Employee> getAll() {
        return service.getAllEmployees();
    }

    public Employee getById(int id) {
        return service.getEmployeeById(id);
    }

    public boolean save(Employee e) {
        return service.createEmployee(e);
    }

    public boolean update(Employee e) {
        return service.updateEmployee(e);
    }
    
    public boolean updateStatus(Employee e)
    {
        return service.updateStatus(e);
    }
    
    public boolean updatePass(Employee e){
        return service.updatePass(e);
    }
    public List<Employee> search(String keyword)
    {
        return service.search(keyword);
    }
    
    public boolean delete(int id) {
        return service.deleteEmployee(id);
    }

}
