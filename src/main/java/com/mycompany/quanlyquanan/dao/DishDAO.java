/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.dao;

import com.mycompany.quanlyquanan.model.Dish;
import com.mycompany.quanlyquanan.utils.DatabaseConnector;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Admin
 */
public class DishDAO {
    
    private final Connection conn;

    public DishDAO() {
        this.conn = DatabaseConnector.getConnection();
    }

    public List<Dish> getAll() {
        List<Dish> dishes = new ArrayList<>();
        String sql = """
            SELECT d.*, c.name as category_name 
            FROM dishes d 
            LEFT JOIN categories c ON d.category_id = c.id 
            ORDER BY d.name
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                dishes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all dishes: " + e.getMessage());
            e.printStackTrace();
        }
        return dishes;
    }

    public List<Dish> getAllAvailable() {
        List<Dish> dishes = new ArrayList<>();
        String sql = """
            SELECT d.*, c.name as category_name 
            FROM dishes d 
            LEFT JOIN categories c ON d.category_id = c.id 
            WHERE d.is_available = 1 
            ORDER BY d.name
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                dishes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting available dishes: " + e.getMessage());
            e.printStackTrace();
        }
        return dishes;
    }

    public List<Dish> getByCategory(int categoryId) {
        List<Dish> dishes = new ArrayList<>();
        String sql = """
            SELECT d.*, c.name as category_name 
            FROM dishes d 
            LEFT JOIN categories c ON d.category_id = c.id 
            WHERE d.category_id = ? AND d.is_available = 1 
            ORDER BY d.name
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                dishes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting dishes by category: " + e.getMessage());
            e.printStackTrace();
        }
        return dishes;
    }

    public Dish getById(int id) {
        String sql = """
            SELECT d.*, c.name as category_name 
            FROM dishes d 
            LEFT JOIN categories c ON d.category_id = c.id 
            WHERE d.id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting dish by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(Dish dish) {
        String sql = """
            INSERT INTO dishes (name, category_id, price, cost_price, image_url, prep_time, description, is_available, created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setStatementParameters(stmt, dish);
            stmt.setTimestamp(9, Timestamp.valueOf(dish.getCreatedAt()));
            stmt.setTimestamp(10, Timestamp.valueOf(dish.getUpdatedAt()));
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting dish: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Dish dish) {
        String sql = """
            UPDATE dishes SET 
                name = ?, category_id = ?, price = ?, cost_price = ?, 
                image_url = ?, prep_time = ?, description = ?, is_available = ?, updated_at = ? 
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setStatementParameters(stmt, dish);
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(10, dish.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating dish: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        // Soft delete - chỉ đánh dấu is_available = false
        String sql = "UPDATE dishes SET is_available = 0, updated_at = ? WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, id);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting dish: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean hardDelete(int id) {
        // Hard delete - xóa hoàn toàn khỏi database
        String sql = "DELETE FROM dishes WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error hard deleting dish: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Dish> search(String keyword) {
        List<Dish> dishes = new ArrayList<>();
        String sql = """
            SELECT d.*, c.name as category_name 
            FROM dishes d 
            LEFT JOIN categories c ON d.category_id = c.id 
            WHERE (d.name LIKE ? OR d.description LIKE ? OR c.name LIKE ?) AND d.is_available = 1 
            ORDER BY d.name
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dishes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching dishes: " + e.getMessage());
            e.printStackTrace();
        }
        return dishes;
    }
    
    
    public List<Dish> getByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Dish> dishes = new ArrayList<>();
        String sql = """
            SELECT d.*, c.name as category_name 
            FROM dishes d 
            LEFT JOIN categories c ON d.category_id = c.id 
            WHERE d.price BETWEEN ? AND ? AND d.is_available = 1 
            ORDER BY d.price
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, minPrice);
            stmt.setBigDecimal(2, maxPrice);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dishes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting dishes by price range: " + e.getMessage());
            e.printStackTrace();
        }
        return dishes;
    }

    public boolean isNameExists(String name, int excludeId) {
        String sql = "SELECT COUNT(*) FROM dishes WHERE name = ? AND id != ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, excludeId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking name exists: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

        /**
     * Cập nhật giá vốn của món ăn
     */
    public boolean updateCostPrice(int dishId, BigDecimal costPrice) {
        String sql = "UPDATE dishes SET cost_price = ?, updated_at = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, costPrice);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, dishId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating cost price: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public BigDecimal calculateCostPriceFromRecipes(int dishId) {
        String sql = """
            SELECT SUM(r.quantity * m.price_per_unit) as total_cost
            FROM recipes r
            JOIN materials m ON r.material_id = m.id
            WHERE r.dish_id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dishId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("total_cost");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating cost price: " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    private void setStatementParameters(PreparedStatement stmt, Dish dish) throws SQLException {
        stmt.setString(1, dish.getName());
        stmt.setInt(2, dish.getCategoryId());
        stmt.setBigDecimal(3, dish.getPrice());
        stmt.setBigDecimal(4, dish.getCostPrice());
        stmt.setString(5, dish.getImageUrl());
        stmt.setInt(6, dish.getPrepTime());
        stmt.setString(7, dish.getDescription());
        stmt.setBoolean(8, dish.isAvailable());
    }

    private Dish mapResultSet(ResultSet rs) throws SQLException {
        Dish dish = new Dish();
        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setCategoryId(rs.getInt("category_id"));
        dish.setPrice(rs.getBigDecimal("price"));
        dish.setCostPrice(rs.getBigDecimal("cost_price"));
        dish.setImageUrl(rs.getString("image_url"));
        dish.setPrepTime(rs.getInt("prep_time"));
        dish.setDescription(rs.getString("description"));
        dish.setAvailable(rs.getBoolean("is_available"));
        
        // Set category name if available
        dish.setCategoryName(rs.getString("category_name"));
        
        // Handle null timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            dish.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            dish.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return dish;
    }

    public List<Dish> getBySearch(String searchString) {
        List<Dish> dishes = new ArrayList<>();
        String sql = """
            SELECT d.*, c.name as category_name 
            FROM dishes d 
            LEFT JOIN categories c ON d.category_id = c.id 
            WHERE d.name LIKE ? AND d.is_available = 1 
            ORDER BY d.name
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, searchString);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                dishes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting dishes by category: " + e.getMessage());
            e.printStackTrace();
        }
        return dishes;
    }
}