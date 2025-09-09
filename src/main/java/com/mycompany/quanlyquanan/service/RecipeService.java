/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.dao.DishDAO;
import com.mycompany.quanlyquanan.dao.MaterialDAO;
import com.mycompany.quanlyquanan.dao.RecipeDAO;
import com.mycompany.quanlyquanan.model.Dish;
import com.mycompany.quanlyquanan.model.Material;
import com.mycompany.quanlyquanan.model.Recipe;
import com.mycompany.quanlyquanan.utils.Session;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Admin
 */
public class RecipeService {
    
    private final RecipeDAO recipeDAO;
    private final DishDAO dishDAO;
    private final MaterialDAO materialDAO;

    public RecipeService() {
        this.recipeDAO = new RecipeDAO();
        this.dishDAO = new DishDAO();
        this.materialDAO = new MaterialDAO();
    }

    public RecipeService(RecipeDAO recipeDAO, DishDAO dishDAO, MaterialDAO materialDAO) {
        this.recipeDAO = recipeDAO;
        this.dishDAO = dishDAO;
        this.materialDAO = materialDAO;
    }

    /**
     * Lấy tất cả công thức
     */
    public List<Recipe> getAllRecipes() {
        return recipeDAO.getAll();
    }

    /**
     * Lấy công thức theo ID món ăn
     */
    public List<Recipe> getRecipesByDishId(int dishId) {
        if (dishId <= 0) {
            throw new IllegalArgumentException("ID món ăn không hợp lệ");
        }
        
        // Kiểm tra món ăn có tồn tại không
        Dish dish = dishDAO.getById(dishId);
        if (dish == null) {
            throw new IllegalArgumentException("Món ăn không tồn tại");
        }
        
        return recipeDAO.getByDishId(dishId);
    }

    /**
     * Lấy công thức theo ID nguyên liệu
     */
    public List<Recipe> getRecipesByMaterialId(int materialId) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }
        
        // Kiểm tra nguyên liệu có tồn tại không
        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        return recipeDAO.getByMaterialId(materialId);
    }

    /**
     * Lấy công thức theo ID
     */
    public Recipe getRecipeById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID công thức không hợp lệ");
        }
        return recipeDAO.getById(id);
    }

    /**
     * Tạo công thức mới
     */
    public boolean createRecipe(Recipe recipe) {
        // Validation
        validateRecipe(recipe, true);
        
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền tạo công thức");
        }
        
        // Kiểm tra món ăn có tồn tại không
        Dish dish = dishDAO.getById(recipe.getDishId());
        if (dish == null) {
            throw new IllegalArgumentException("Món ăn không tồn tại");
        }
        
        // Kiểm tra nguyên liệu có tồn tại và đang hoạt động không
        Material material = materialDAO.getById(recipe.getMaterialId());
        if (material == null || !material.isActive()) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại hoặc đã bị vô hiệu hóa");
        }
        
        // Kiểm tra công thức đã tồn tại chưa (món + nguyên liệu)
        if (recipeDAO.isRecipeExists(recipe.getDishId(), recipe.getMaterialId(), 0)) {
            throw new IllegalArgumentException("Công thức cho món ăn và nguyên liệu này đã tồn tại");
        }
        
        boolean inserted = recipeDAO.insert(recipe);
        if (inserted) {
            updateDishCost(recipe.getDishId());
        }
        return inserted;
    }

    /**
     * Cập nhật công thức
     */
    public boolean updateRecipe(Recipe recipe) {
        // Validation
        validateRecipe(recipe, false);
        
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền cập nhật công thức");
        }
        
        // Kiểm tra công thức có tồn tại không
        Recipe existingRecipe = recipeDAO.getById(recipe.getId());
        if (existingRecipe == null) {
            throw new IllegalArgumentException("Công thức không tồn tại");
        }
        
        // Kiểm tra món ăn có tồn tại không
        Dish dish = dishDAO.getById(recipe.getDishId());
        if (dish == null) {
            throw new IllegalArgumentException("Món ăn không tồn tại");
        }
        
        // Kiểm tra nguyên liệu có tồn tại và đang hoạt động không
        Material material = materialDAO.getById(recipe.getMaterialId());
        if (material == null || !material.isActive()) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại hoặc đã bị vô hiệu hóa");
        }
        
        // Kiểm tra công thức trùng lặp (trừ chính nó)
        if (recipeDAO.isRecipeExists(recipe.getDishId(), recipe.getMaterialId(), recipe.getId())) {
            throw new IllegalArgumentException("Công thức cho món ăn và nguyên liệu này đã tồn tại");
        }
        
        boolean updated = recipeDAO.update(recipe);
        if (updated) {
            updateDishCost(recipe.getDishId());
        }
        return updated;
    }

    /**
     * Xóa công thức
     */
    public boolean deleteRecipe(int id) {
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền xóa công thức");
        }
        
        if (id <= 0) {
            throw new IllegalArgumentException("ID công thức không hợp lệ");
        }
        
        // Kiểm tra công thức có tồn tại không
        Recipe recipe = recipeDAO.getById(id);
        if (recipe == null) {
            throw new IllegalArgumentException("Công thức không tồn tại");
        }
        
        boolean deleted = recipeDAO.delete(id);
        if (deleted) {
            updateDishCost(recipe.getDishId());
        }
        return deleted;
    }

    /**
     * Xóa tất cả công thức của một món ăn
     */
    public boolean deleteRecipesByDishId(int dishId) {
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền xóa công thức");
        }
        
        if (dishId <= 0) {
            throw new IllegalArgumentException("ID món ăn không hợp lệ");
        }
        
        boolean deleted = recipeDAO.deleteByDishId(dishId);
        if (deleted) {
            updateDishCost(dishId);
        }
        return deleted;
    }

    /**
     * Xóa tất cả công thức sử dụng nguyên liệu
     */
    public boolean deleteRecipesByMaterialId(int materialId) {
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền xóa công thức");
        }
        
        if (materialId <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }
        
        List<Recipe> recipes = recipeDAO.getByMaterialId(materialId);
        boolean deleted = recipeDAO.deleteByMaterialId(materialId);
        if (deleted) {
            Set<Integer> dishIds = recipes.stream()
                .map(Recipe::getDishId)
                .collect(Collectors.toSet());
            for (int dishId : dishIds) {
                updateDishCost(dishId);
            }
        }
        return deleted;
    }

    /**
     * Cập nhật toàn bộ công thức cho một món ăn
     */
    public boolean updateDishRecipes(int dishId, List<Recipe> newRecipes) {
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền cập nhật công thức");
        }
        
        if (dishId <= 0) {
            throw new IllegalArgumentException("ID món ăn không hợp lệ");
        }
        
        // Kiểm tra món ăn có tồn tại không
        Dish dish = dishDAO.getById(dishId);
        if (dish == null) {
            throw new IllegalArgumentException("Món ăn không tồn tại");
        }
        
        // Validate từng công thức mới
        if (newRecipes != null) {
            for (Recipe recipe : newRecipes) {
                validateRecipe(recipe, true);
                
                // Kiểm tra nguyên liệu có tồn tại và đang hoạt động không
                Material material = materialDAO.getById(recipe.getMaterialId());
                if (material == null || !material.isActive()) {
                    throw new IllegalArgumentException("Nguyên liệu ID " + recipe.getMaterialId() + " không tồn tại hoặc đã bị vô hiệu hóa");
                }
            }
        }
        
        boolean success = recipeDAO.updateRecipesForDish(dishId, newRecipes != null ? newRecipes : List.of());
        if (success) {
            updateDishCost(dishId);
        }
        return success;
    }

    /**
     * Tính tổng giá vốn của một món ăn từ công thức
     */
    public BigDecimal calculateDishCostFromRecipes(int dishId) {
        if (dishId <= 0) {
            throw new IllegalArgumentException("ID món ăn không hợp lệ");
        }
        
        return recipeDAO.getTotalCostByDishId(dishId);
    }

    private void updateDishCost(int dishId) {
        BigDecimal totalCost = recipeDAO.getTotalCostByDishId(dishId);
        dishDAO.updateCostPrice(dishId, totalCost);
    }
    /**
     * Lấy các công thức có nguyên liệu sắp hết
     */
    public List<Recipe> getRecipesWithLowStockMaterials() {
        return recipeDAO.getRecipesWithLowStockMaterials();
    }

    /**
     * Kiểm tra có thể chế biến món ăn không (đủ nguyên liệu)
     */
    public boolean canPrepareDish(int dishId, int quantity) {
        if (dishId <= 0 || quantity <= 0) {
            throw new IllegalArgumentException("ID món ăn và số lượng phải lớn hơn 0");
        }
        
        List<Recipe> recipes = recipeDAO.getByDishId(dishId);
        if (recipes.isEmpty()) {
            return false; // Không có công thức
        }
        
        // Kiểm tra từng nguyên liệu
        for (Recipe recipe : recipes) {
            Material material = materialDAO.getById(recipe.getMaterialId());
            if (material == null || !material.isActive()) {
                return false; // Nguyên liệu không tồn tại hoặc không hoạt động
            }
            
            BigDecimal requiredQuantity = recipe.getQuantity().multiply(BigDecimal.valueOf(quantity));
            if (material.getQuantity().compareTo(requiredQuantity) < 0) {
                return false; // Không đủ nguyên liệu
            }
        }
        
        return true;
    }

    /**
     * Trừ nguyên liệu khi chế biến món ăn
     */
    public boolean consumeMaterialsForDish(int dishId, int quantity) {
        // Kiểm tra quyền (nhân viên bếp và admin)
        if (!Session.getInstance().isAdmin() && !Session.getInstance().isBep()) {
            throw new SecurityException("Chỉ bếp và admin mới có quyền trừ nguyên liệu");
        }
        
        if (!canPrepareDish(dishId, quantity)) {
            throw new IllegalStateException("Không thể chế biến món ăn này (không đủ nguyên liệu hoặc không có công thức)");
        }
        
        List<Recipe> recipes = recipeDAO.getByDishId(dishId);
        boolean success = true;
        
        // Trừ từng nguyên liệu
        for (Recipe recipe : recipes) {
            BigDecimal requiredQuantity = recipe.getQuantity().multiply(BigDecimal.valueOf(quantity));
            if (!materialDAO.subtractQuantity(recipe.getMaterialId(), requiredQuantity)) {
                success = false;
                break;
            }
        }
        
        // TODO: Ghi log stock_logs với change_type = 'consume'
        
        return success;
    }

    /**
     * Lấy danh sách nguyên liệu cần thiết cho món ăn
     */
    public List<Material> getRequiredMaterialsForDish(int dishId) {
        if (dishId <= 0) {
            throw new IllegalArgumentException("ID món ăn không hợp lệ");
        }
        
        return materialDAO.getMaterialsForDish(dishId);
    }

    /**
     * Kiểm tra công thức có trùng lặp không
     */
    public boolean isRecipeExists(int dishId, int materialId, int excludeId) {
        return recipeDAO.isRecipeExists(dishId, materialId, excludeId);
    }

    /**
     * Lấy công thức theo món ăn và nguyên liệu
     */
    public Recipe getRecipeByDishAndMaterial(int dishId, int materialId) {
        if (dishId <= 0 || materialId <= 0) {
            throw new IllegalArgumentException("ID món ăn và nguyên liệu không hợp lệ");
        }
        
        return recipeDAO.getByDishAndMaterial(dishId, materialId);
    }

    /**
     * Tính tổng chi phí nguyên liệu cho nhiều món ăn
     */
    public BigDecimal calculateTotalCostForDishes(List<Integer> dishIds, List<Integer> quantities) {
        if (dishIds == null || quantities == null || dishIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Danh sách món ăn và số lượng không hợp lệ");
        }
        
        BigDecimal totalCost = BigDecimal.ZERO;
        
        for (int i = 0; i < dishIds.size(); i++) {
            int dishId = dishIds.get(i);
            int quantity = quantities.get(i);
            
            if (quantity > 0) {
                BigDecimal dishCost = calculateDishCostFromRecipes(dishId);
                totalCost = totalCost.add(dishCost.multiply(BigDecimal.valueOf(quantity)));
            }
        }
        
        return totalCost;
    }

    /**
     * Validate thông tin công thức
     */
    private void validateRecipe(Recipe recipe, boolean isNew) {
        if (recipe == null) {
            throw new IllegalArgumentException("Thông tin công thức không được null");
        }
        
        // Validate ID cho update
        if (!isNew && recipe.getId() <= 0) {
            throw new IllegalArgumentException("ID công thức không hợp lệ");
        }
        
        // Validate dish ID
        if (recipe.getDishId() <= 0) {
            throw new IllegalArgumentException("ID món ăn không hợp lệ");
        }
        
        // Validate material ID
        if (recipe.getMaterialId() <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }
        
        // Validate quantity
        if (recipe.getQuantity() == null || recipe.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        
        if (recipe.getQuantity().compareTo(new BigDecimal("999999.999")) > 0) {
            throw new IllegalArgumentException("Số lượng quá lớn");
        }
        
        // Kiểm tra scale (số chữ số thập phân)
        if (recipe.getQuantity().scale() > 3) {
            throw new IllegalArgumentException("Số lượng không được có quá 3 chữ số thập phân");
        }
    }
}