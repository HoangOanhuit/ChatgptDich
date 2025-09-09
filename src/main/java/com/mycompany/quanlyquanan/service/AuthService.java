/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;

/**
 *
 * @author Admin
 */
import com.mycompany.quanlyquanan.dao.EmployeeDAO;
import com.mycompany.quanlyquanan.model.Employee;
import com.mycompany.quanlyquanan.utils.Session;
import com.mycompany.quanlyquanan.utils.HashUtil;

public class AuthService {
    private final EmployeeDAO dao;

    public AuthService(EmployeeDAO dao) {
        this.dao = dao;
    }

    public boolean login(String username, String password) {
        Employee user = dao.findByUsername(username);
        if (user != null && HashUtil.checkPassword(password, user.getPass())) {
            Session.getInstance().setCurrentUser(user);
            return true;
        }
        return false;
    }

    public void logout() {
        Session.getInstance().logout();
    }
}