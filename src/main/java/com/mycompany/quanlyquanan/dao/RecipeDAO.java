/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.dao;

import com.mycompany.quanlyquanan.model.Recipe;
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
public class RecipeDAO {
    
    private final Connection conn;

    public RecipeDAO() {
        this.conn = DatabaseConnector.getConnection();
    }

    public List<Recipe> getAll() {
        List<Recipe> recipes = new ArrayList<>();
        String sql = """
            SELECT r.*, d.name as dish_name, m.name as material_name, 
                   m.unit as material_unit, m.price_per_unit as material_price_per_unit
            FROM recipes r
            LEFT JOIN dishes d ON r.dish_id = d.id
            LEFT JOIN materials m ON r.material_id = m.id
            ORDER BY d.name, m.name
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                recipes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all recipes: " + e.getMessage());
            e.printStackTrace();
        }
        return recipes;
    }

    public List<Recipe> getByDishId(int dishId) {
        List<Recipe> recipes = new ArrayList<>();
        String sql = """
            SELECT r.*, d.name as dish_name, m.name as material_name, 
                   m.unit as material_unit, m.price_per_unit as material_price_per_unit
            FROM recipes r
            LEFT JOIN dishes d ON r.dish_id = d.id
            LEFT JOIN materials m ON r.material_id = m.id
            WHERE r.dish_id = ?
            ORDER BY m.name
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dishId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                recipes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting recipes by dish id: " + e.getMessage());
            e.printStackTrace();
        }
        return recipes;
    }

    public List<Recipe> getByMaterialId(int materialId) {
        List<Recipe> recipes = new ArrayList<>();
        String sql = """
            SELECT r.*, d.name as dish_name, m.name as material_name, 
                   m.unit as material_unit, m.price_per_unit as material_price_per_unit
            FROM recipes r
            LEFT JOIN dishes d ON r.dish_id = d.id
            LEFT JOIN materials m ON r.material_id = m.id
            WHERE r.material_id = ?
            ORDER BY d.name
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, materialId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                recipes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting recipes by material id: " + e.getMessage());
            e.printStackTrace();
        }
        return recipes;
    }

    public Recipe getById(int id) {
        String sql = """
            SELECT r.*, d.name as dish_name, m.name as material_name, 
                   m.unit as material_unit, m.price_per_unit as material_price_per_unit
            FROM recipes r
            LEFT JOIN dishes d ON r.dish_id = d.id
            LEFT JOIN materials m ON r.material_id = m.id
            WHERE r.id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting recipe by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Recipe getByDishAndMaterial(int dishId, int materialId) {
        String sql = """
            SELECT r.*, d.name as dish_name, m.name as material_name, 
                   m.unit as material_unit, m.price_per_unit as material_price_per_unit
            FROM recipes r
            LEFT JOIN dishes d ON r.dish_id = d.id
            LEFT JOIN materials m ON r.material_id = m.id
            WHERE r.dish_id = ? AND r.material_id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dishId);
            stmt.setInt(2, materialId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting recipe by dish and material: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(Recipe recipe) {
        String sql = "INSERT INTO recipes (dish_id, material_id, quantity, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recipe.getDishId());
            stmt.setInt(2, recipe.getMaterialId());
            stmt.setBigDecimal(3, recipe.getQuantity());
            stmt.setTimestamp(4, Timestamp.valueOf(recipe.getCreatedAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(recipe.getUpdatedAt()));
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting recipe: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Recipe recipe) {
        String sql = "UPDATE recipes SET dish_id = ?, material_id = ?, quantity = ?, updated_at = ? WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recipe.getDishId());
            stmt.setInt(2, recipe.getMaterialId());
            stmt.setBigDecimal(3, recipe.getQuantity());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, recipe.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating recipe: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM recipes WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting recipe: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteByDishId(int dishId) {
        String sql = "DELETE FROM recipes WHERE dish_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dishId);
            return stmt.executeUpdate() >= 0; // >= 0 vì có thể món không có recipe nào
        } catch (SQLException e) {
            System.err.println("Error deleting recipes by dish id: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteByMaterialId(int materialId) {
        String sql = "DELETE FROM recipes WHERE material_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, materialId);
            return stmt.executeUpdate() >= 0; // >= 0 vì có thể nguyên liệu không có recipe nào
        } catch (SQLException e) {
            System.err.println("Error deleting recipes by material id: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isRecipeExists(int dishId, int materialId, int excludeId) {
        String sql = "SELECT COUNT(*) FROM recipes WHERE dish_id = ? AND material_id = ? AND id != ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dishId);
            stmt.setInt(2, materialId);
            stmt.setInt(3, excludeId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking recipe exists: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public BigDecimal getTotalCostByDishId(int dishId) {
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
                BigDecimal totalCost = rs.getBigDecimal("total_cost");
                return totalCost != null ? totalCost : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("Error getting total cost by dish id: " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    public List<Recipe> getRecipesWithLowStockMaterials() {
        List<Recipe> recipes = new ArrayList<>();
        String sql = """
            SELECT r.*, d.name as dish_name, m.name as material_name, 
                   m.unit as material_unit, m.price_per_unit as material_price_per_unit
            FROM recipes r
            LEFT JOIN dishes d ON r.dish_id = d.id
            LEFT JOIN materials m ON r.material_id = m.id
            WHERE m.quantity <= m.threshold AND m.is_active = 1
            ORDER BY d.name, m.name
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                recipes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting recipes with low stock materials: " + e.getMessage());
            e.printStackTrace();
        }
        return recipes;
    }

    public boolean updateRecipesForDish(int dishId, List<Recipe> newRecipes) {
        Connection connection = null;
        try {
            connection = conn;
            connection.setAutoCommit(false);
            
            // 1. Xóa tất cả recipes cũ của món này
            String deleteSql = "DELETE FROM recipes WHERE dish_id = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, dishId);
                deleteStmt.executeUpdate();
            }
            
            // 2. Thêm recipes mới
            String insertSql = "INSERT INTO recipes (dish_id, material_id, quantity, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                for (Recipe recipe : newRecipes) {
                    insertStmt.setInt(1, dishId);
                    insertStmt.setInt(2, recipe.getMaterialId());
                    insertStmt.setBigDecimal(3, recipe.getQuantity());
                    insertStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    insertStmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
            
            connection.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error updating recipes for dish: " + e.getMessage());
            e.printStackTrace();
            
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            return false;
            
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error resetting auto commit: " + e.getMessage());
                }
            }
        }
    }

    private Recipe mapResultSet(ResultSet rs) throws SQLException {
        Recipe recipe = new Recipe();
        recipe.setId(rs.getInt("id"));
        recipe.setDishId(rs.getInt("dish_id"));
        recipe.setMaterialId(rs.getInt("material_id"));
        recipe.setQuantity(rs.getBigDecimal("quantity"));
        
        // Set additional information
        recipe.setDishName(rs.getString("dish_name"));
        recipe.setMaterialName(rs.getString("material_name"));
        recipe.setMaterialUnit(rs.getString("material_unit"));
        recipe.setMaterialPricePerUnit(rs.getBigDecimal("material_price_per_unit"));
        
        // Handle null timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            recipe.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            recipe.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return recipe;
    }
    
    /**
     * Kiểm tra xem nguyên liệu có đang được sử dụng trong bất kỳ công thức nào
     * hay không.
     *
     * @param id id của nguyên liệu cần kiểm tra
     * @return true nếu tồn tại công thức sử dụng nguyên liệu này, ngược lại false
     */

    public boolean isUsedInRecipes(int id) {
        String sql = "SELECT COUNT(*) FROM recipes WHERE material_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking material usage in recipes: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

   
}