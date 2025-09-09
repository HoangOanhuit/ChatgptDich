/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.dao;

/**
 *
 * @author Tyler
 */
import com.mycompany.quanlyquanan.model.OrderItem;
import java.sql.*;
import com.mycompany.quanlyquanan.utils.DatabaseConnector;
import java.util.ArrayList;
import java.util.List;


public class OrderItemDAO {
    private final Connection con;
    
    public OrderItemDAO() {
        this.con = DatabaseConnector.getConnection();
    }
    
    public List<OrderItem> getAll() {
        List<OrderItem> orderItemsList = new ArrayList<>();
        String sql = "SELECT * FROM order_items";
        try(PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                orderItemsList.add(mapResultSet(rs));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return orderItemsList;
    }
    
    public List<OrderItem> getAllByOrderId(int orderId) {
        List<OrderItem> orderItemList = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        try(PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                orderItemList.add(mapResultSet(rs));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        
        return orderItemList;
    }
    
    public List<OrderItem> getAllServedByOrderId(int orderId) {
        List<OrderItem> orderItemList = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ? AND status = 'served'";
        try(PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                orderItemList.add(mapResultSet(rs));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        
        return orderItemList;
    }
    
    public Boolean insert(OrderItem oi) {
        String sql = "INSERT INTO order_items(order_id, dish_id, quantity, status) VALUES(?, ?, ?, ?)";
        try(PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, oi.getOrder_id());
            ps.setInt(2, oi.getDish_id());
            ps.setInt(3, oi.getQuantity());
            ps.setString(4, oi.getStatus());
            
            return ps.executeUpdate() > 0;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Boolean update(OrderItem oi) {
        String sql = "UPDATE order_items SET order_id = ?, dish_id=?, quantity=?, status=? WHERE id=?";
        try(PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, oi.getOrder_id());
            ps.setInt(2, oi.getDish_id());
            ps.setInt(3, oi.getQuantity());
            ps.setString(4, oi.getStatus());
            ps.setInt(5, oi.getId());
            return ps.executeUpdate() > 0;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public OrderItem getById(int id) {
        String s = "SELECT * FROM order_items WHERE id = ?";
        try(PreparedStatement ps = con.prepareStatement(s)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public Boolean delete(int id) {
        String sql = "DELETE FROM order_items WHERE id=?";
        try(PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    
    private OrderItem mapResultSet(ResultSet rs) throws SQLException {
        return new OrderItem(
            rs.getInt("id"), 
            rs.getInt("order_id"),
            rs.getInt("dish_id"),
            rs.getInt("quantity"),
            rs.getString("status")
        );
    }
    
}
