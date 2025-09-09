/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.dao;

import com.mycompany.quanlyquanan.model.Category;
import com.mycompany.quanlyquanan.utils.DatabaseConnector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Admin
 */
public class CategoryDAO {
    
    private final Connection conn;

    public CategoryDAO() {
        this.conn = DatabaseConnector.getConnection();
    }

    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all categories: " + e.getMessage());
            e.printStackTrace();
        }
        return categories;
    }

    public List<Category> getAllActive() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE is_active = 1 ORDER BY name";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting active categories: " + e.getMessage());
            e.printStackTrace();
        }
        return categories;
    }

    public Category getById(int id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting category by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Category getByName(String name) {
        String sql = "SELECT * FROM categories WHERE name = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting category by name: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(Category category) {
        String sql = "INSERT INTO categories (name, description, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setBoolean(3, category.isActive());
            stmt.setTimestamp(4, Timestamp.valueOf(category.getCreatedAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(category.getUpdatedAt()));
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, is_active = ?, updated_at = ? WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setBoolean(3, category.isActive());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, category.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        // Soft delete - chỉ đánh dấu is_active = false
        String sql = "UPDATE categories SET is_active = 0, updated_at = ? WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, id);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean hardDelete(int id) {
        // Hard delete - xóa hoàn toàn khỏi database
        String sql = "DELETE FROM categories WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error hard deleting category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isNameExists(String name, int excludeId) {
        String sql = "SELECT COUNT(*) FROM categories WHERE name = ? AND id != ?";
        
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

    public boolean hasActiveDishes(int categoryId) {
        String sql = "SELECT COUNT(*) FROM dishes WHERE category_id = ? AND is_available = 1";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking active dishes: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Category> search(String keyword) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE (name LIKE ? OR description LIKE ?) AND is_active = 1 ORDER BY name";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching categories: " + e.getMessage());
            e.printStackTrace();
        }
        return categories;
    }

    private Category mapResultSet(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setActive(rs.getBoolean("is_active"));
        
        // Handle null timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            category.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            category.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return category;
    }

    public boolean updateDescription(int id, String description) {
        String sql = "UPDATE categories SET description = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, description);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating category description: " + e.getMessage());
            e.printStackTrace();
        }
        return false;    
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM categories";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting categories: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public long countActive() {
        String sql = "SELECT COUNT(*) FROM categories WHERE is_active = 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting active categories: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public Category getMostPopular() {
        String sql = """
            SELECT c.*, COUNT(d.id) as dish_count
            FROM categories c
            LEFT JOIN dishes d ON c.id = d.category_id
            GROUP BY c.id
            ORDER BY dish_count DESC
            LIMIT 1
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting most popular category: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Category getLeastPopular() {
        String sql = """
            SELECT c.*, COUNT(d.id) as dish_count
            FROM categories c
            LEFT JOIN dishes d ON c.id = d.category_id
            GROUP BY c.id
            ORDER BY dish_count ASC
            LIMIT 1
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting least popular category: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Category> getByDishCountDesc() {
        List<Category> categories = new ArrayList<>();
        String sql = """
            SELECT c.*, COUNT(d.id) as dish_count
            FROM categories c
            LEFT JOIN dishes d ON c.id = d.category_id
            GROUP BY c.id
            ORDER BY dish_count DESC
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting categories by dish count: " + e.getMessage());
            e.printStackTrace();
        }
        return categories;
    }

    public boolean hasDishesByCategory(int categoryId) {
        String sql = "SELECT COUNT(*) FROM dishes WHERE category_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking dishes by category: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public int getDishCount(int categoryId) {
        String sql = "SELECT COUNT(*) FROM dishes WHERE category_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting dish count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateActiveStatus(int id, boolean active) {
        String sql = "UPDATE categories SET is_active = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating category status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}