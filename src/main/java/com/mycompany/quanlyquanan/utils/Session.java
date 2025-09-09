/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.utils;
import com.mycompany.quanlyquanan.model.Employee;

/**
 * @author Admin
 */
public class Session {
    private static Session instance;
    private Employee currentUser;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public Employee getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Employee user) {
        this.currentUser = user;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    // ===================== PHÂN QUYỀN THEO ROLE =====================
    
    /**
     * Kiểm tra có phải Admin không
     */
    public boolean isAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    /**
     * Kiểm tra có phải Nhân viên phục vụ không
     */
    public boolean isNhanVien() {
        return currentUser != null && "Nhân viên".equalsIgnoreCase(currentUser.getRole());
    }
    
    /**
     * Kiểm tra có phải Thu ngân không
     */
    public boolean isThuNgan() {
        return currentUser != null && "Thu Ngân".equalsIgnoreCase(currentUser.getRole());
    }
    
    /**
     * Kiểm tra có phải Bếp không
     */
    public boolean isBep() {
        return currentUser != null && "Bếp".equalsIgnoreCase(currentUser.getRole());
    }
    
    /**
     * Kiểm tra có phải Thủ kho không
     */
    public boolean isThuKho() {
        return currentUser != null && "Thủ Kho".equalsIgnoreCase(currentUser.getRole());
    }
    
    // ===================== PHÂN QUYỀN THEO CHỨC NĂNG =====================
    
    /**
     * Kiểm tra có quyền quản lý inventory (Admin hoặc Thủ kho)
     */
    public boolean canManageInventory() {
        return isLoggedIn() && (isAdmin() || isThuKho());
    }
    
    /**
     * Kiểm tra có quyền quản lý nhân viên (Chỉ Admin)
     */
    public boolean canManageEmployees() {
        return isAdmin();
    }
    
    /**
     * Kiểm tra có quyền tạo đơn hàng (Admin, Nhân viên)
     */
    public boolean canCreateOrders() {
        return isLoggedIn() && (isAdmin() || isNhanVien());
    }
    
    /**
     * Kiểm tra có quyền thanh toán (Admin, Thu ngân, Nhân viên)
     */
    public boolean canProcessPayments() {
        return isLoggedIn() && (isAdmin() || isThuNgan() || isNhanVien());
    }
    
    /**
     * Kiểm tra có quyền quản lý bếp (Admin, Bếp)
     */
    public boolean canManageKitchen() {
        return isLoggedIn() && (isAdmin() || isBep());
    }
    
    /**
     * Kiểm tra có quyền quản lý bàn (Admin, Nhân viên)
     */
    public boolean canManageTables() {
        return isLoggedIn() && (isAdmin() || isNhanVien());
    }
    
    /**
     * Kiểm tra có quyền xem báo cáo (Chỉ Admin)
     */
    public boolean canViewReports() {
        return isAdmin();
    }
    
    /**
     * Kiểm tra có quyền quản lý menu (Chỉ Admin)
     */
    public boolean canManageMenu() {
        return isAdmin();
    }
    
    /**
     * Lấy ID user hiện tại
     */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
    
    /**
     * Lấy tên user hiện tại
     */
    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getName() : "Unknown";
    }
    
    /**
     * Lấy username hiện tại
     */
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "anonymous";
    }
    
    /**
     * Lấy role hiện tại
     */
    public String getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : "none";
    }
}