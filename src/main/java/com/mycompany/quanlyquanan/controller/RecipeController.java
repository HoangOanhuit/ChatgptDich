/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.controller;

import com.mycompany.quanlyquanan.model.Dish;
import com.mycompany.quanlyquanan.model.Material;
import com.mycompany.quanlyquanan.model.Recipe;
import com.mycompany.quanlyquanan.service.DishService;
import com.mycompany.quanlyquanan.service.MaterialService;
import com.mycompany.quanlyquanan.service.RecipeService;
import static com.mysql.cj.conf.PropertyKey.logger;

import javax.swing.JOptionPane;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Admin
 */
public class RecipeController {
    
    private final RecipeService recipeService;
    private final DishService dishService;
    private final MaterialService materialService;

    public RecipeController() {
        this.recipeService = new RecipeService();
        this.dishService = new DishService();
        this.materialService = new MaterialService();
    }

    public RecipeController(RecipeService recipeService, DishService dishService, MaterialService materialService) {
        this.recipeService = recipeService;
        this.dishService = dishService;
        this.materialService = materialService;
    }
    
    /**
    * Lấy tất cả nguyên liệu đang hoạt động
    */
    public List<Material> getAllActive() {
        try {
            return materialService.getAllActiveMaterials();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải danh sách nguyên liệu: " + e.getMessage());
            return List.of();
        }
    }
    /**
     * Lấy tất cả công thức
     */
    public List<Recipe> getAll() {
        try {
            return recipeService.getAllRecipes();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải danh sách công thức: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy công thức theo ID nguyên liệu
     */
    public List<Recipe> getByMaterialId(int materialId) {
        try {
            return recipeService.getRecipesByMaterialId(materialId);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return List.of();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải công thức theo nguyên liệu: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy công thức theo ID
     */
    public Recipe getById(int id) {
        try {
            return recipeService.getRecipeById(id);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return null;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải thông tin công thức: " + e.getMessage());
            return null;
        }
    }

    /**
     * Tạo công thức mới
     */
    public boolean create(Recipe recipe) {
        try {
            boolean result = recipeService.createRecipe(recipe);
            if (!result) {
                showErrorMessage("Không thể tạo công thức. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tạo công thức: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật công thức
     */
    public boolean update(Recipe recipe) {
        try {
            boolean result = recipeService.updateRecipe(recipe);
            if (!result) {
                showErrorMessage("Không thể cập nhật công thức. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi cập nhật công thức: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa công thức
     */
    public boolean delete(int id) {
        try {
            Recipe recipe = recipeService.getRecipeById(id);
            if (recipe == null) {
                showErrorMessage("Không tìm thấy công thức");
                return false;
            }

            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc chắn muốn xóa công thức:\n" +
                "Món: " + recipe.getDishName() + "\n" +
                "Nguyên liệu: " + recipe.getMaterialName() + " (" + 
                recipe.getQuantityWithUnit() + ")?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = recipeService.deleteRecipe(id);
            if (!result) {
                showErrorMessage("Không thể xóa công thức. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi xóa công thức: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa tất cả công thức của một món ăn
     */
    public boolean deleteByDishId(int dishId) {
        try {
            Dish dish = dishService.getDishById(dishId);
            if (dish == null) {
                showErrorMessage("Không tìm thấy món ăn");
                return false;
            }

            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc chắn muốn xóa TẤT CẢ công thức của món ăn '" + dish.getName() + "'?\n" +
                "Hành động này sẽ xóa toàn bộ công thức hiện có.",
                "Xác nhận xóa tất cả công thức",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = recipeService.deleteRecipesByDishId(dishId);
            if (!result) {
                showErrorMessage("Không thể xóa công thức. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi xóa công thức: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật toàn bộ công thức cho một món ăn
     */
    public boolean updateDishRecipes(int dishId, List<Recipe> newRecipes) {
        try {
            Dish dish = dishService.getDishById(dishId);
            if (dish == null) {
                showErrorMessage("Không tìm thấy món ăn");
                return false;
            }

            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc chắn muốn cập nhật công thức cho món ăn '" + dish.getName() + "'?\n" +
                "Công thức cũ sẽ được thay thế hoàn toàn bằng công thức mới.",
                "Xác nhận cập nhật công thức",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = recipeService.updateDishRecipes(dishId, newRecipes);
            if (result) {
                // Tự động cập nhật giá vốn
                dishService.updateCostPriceFromRecipe(dishId);
            } else {
                showErrorMessage("Không thể cập nhật công thức. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi cập nhật công thức: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy các công thức có nguyên liệu sắp hết
     */
    public List<Recipe> getRecipesWithLowStock() {
        try {
            return recipeService.getRecipesWithLowStockMaterials();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải công thức có nguyên liệu sắp hết: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Kiểm tra có thể chế biến món ăn không
     */
    public boolean canPrepareDish(int dishId, int quantity) {
        try {
            return recipeService.canPrepareDish(dishId, quantity);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi kiểm tra khả năng chế biến: " + e.getMessage());
            return false;
        }
    }

    /**
     * Trừ nguyên liệu khi chế biến món ăn
     */
    public boolean consumeMaterials(int dishId, int quantity) {
        try {
            Dish dish = dishService.getDishById(dishId);
            if (dish == null) {
                showErrorMessage("Không tìm thấy món ăn");
                return false;
            }

            if (!canPrepareDish(dishId, quantity)) {
                showErrorMessage("Không thể chế biến món ăn này (không đủ nguyên liệu hoặc không có công thức)");
                return false;
            }

            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Xác nhận chế biến " + quantity + " phần '" + dish.getName() + "'?\n" +
                "Hành động này sẽ trừ nguyên liệu từ kho.",
                "Xác nhận chế biến",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = recipeService.consumeMaterialsForDish(dishId, quantity);
            if (!result) {
                showErrorMessage("Không thể trừ nguyên liệu. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi trừ nguyên liệu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh sách món ăn có sẵn (cho dropdown)
     */
    public List<Dish> getAvailableDishes() {
        try {
            return dishService.getAllAvailableDishes();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải danh sách món ăn: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy danh sách nguyên liệu hoạt động (cho dropdown)
     */
    public List<Material> getActiveMaterials() {
        try {
            return materialService.getAllActiveMaterials();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải danh sách nguyên liệu: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy nguyên liệu cần thiết cho món ăn
     */
    public List<Material> getRequiredMaterials(int dishId) {
        try {
            return recipeService.getRequiredMaterialsForDish(dishId);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return List.of();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải nguyên liệu cần thiết: " + e.getMessage());
            return List.of();
        }
    }

    

    /**
     * Validate dữ liệu trước khi submit
     */
    public boolean validateRecipe(Recipe recipe, boolean isNew) {
        if (recipe == null) {
            showErrorMessage("Thông tin công thức không hợp lệ");
            return false;
        }

        // Validate dish ID
        if (recipe.getDishId() <= 0) {
            showErrorMessage("Vui lòng chọn món ăn");
            return false;
        }

        // Validate material ID
        if (recipe.getMaterialId() <= 0) {
            showErrorMessage("Vui lòng chọn nguyên liệu");
            return false;
        }

        // Validate quantity
        if (recipe.getQuantity() == null || recipe.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            showErrorMessage("Số lượng phải lớn hơn 0");
            return false;
        }

        if (recipe.getQuantity().compareTo(new BigDecimal("999999.999")) > 0) {
            showErrorMessage("Số lượng quá lớn");
            return false;
        }

        if (recipe.getQuantity().scale() > 3) {
            showErrorMessage("Số lượng không được có quá 3 chữ số thập phân");
            return false;
        }

        // Kiểm tra trùng lặp
        int excludeId = isNew ? 0 : recipe.getId();
        if (isRecipeExists(recipe.getDishId(), recipe.getMaterialId(), excludeId)) {
            showErrorMessage("Công thức cho món ăn và nguyên liệu này đã tồn tại");
            return false;
        }

        // Kiểm tra món ăn và nguyên liệu có tồn tại không
        try {
            Dish dish = dishService.getDishById(recipe.getDishId());
            if (dish == null) {
                showErrorMessage("Món ăn không tồn tại");
                return false;
            }

            Material material = materialService.getMaterialById(recipe.getMaterialId());
            if (material == null || !material.isActive()) {
                showErrorMessage("Nguyên liệu không tồn tại hoặc đã bị vô hiệu hóa");
                return false;
            }
        } catch (Exception e) {
            showErrorMessage("Lỗi khi kiểm tra món ăn/nguyên liệu: " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Tạo công thức mới với validation
     */
    public boolean createWithValidation(int dishId, int materialId, BigDecimal quantity) {
        Recipe recipe = new Recipe();
        recipe.setDishId(dishId);
        recipe.setMaterialId(materialId);
        recipe.setQuantity(quantity);

        if (!validateRecipe(recipe, true)) {
            return false;
        }

        return create(recipe);
    }

    /**
     * Cập nhật công thức với validation
     */
    public boolean updateWithValidation(int id, int dishId, int materialId, BigDecimal quantity) {
        Recipe recipe = getById(id);
        if (recipe == null) {
            return false;
        }

        recipe.setDishId(dishId);
        recipe.setMaterialId(materialId);
        recipe.setQuantity(quantity);

        if (!validateRecipe(recipe, false)) {
            return false;
        }

        return update(recipe);
    }

    /**
     * Format số lượng với đơn vị
     */
    public String formatQuantityWithUnit(BigDecimal quantity, String unit) {
        if (quantity == null) {
            return "0";
        }
        if (unit == null || unit.trim().isEmpty()) {
            return quantity.toString();
        }
        return quantity.stripTrailingZeros().toPlainString() + " " + unit;
    }

    /**
     * Format chi phí
     */
    public String formatCost(BigDecimal cost) {
        if (cost == null) {
            return "0 ₫";
        }
        return String.format("%,.0f ₫", cost);
    }

    /**
     * Hiển thị thông báo lỗi
     */
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(
            null,
            message,
            "Lỗi",
            JOptionPane.ERROR_MESSAGE
        );
    }

    /*

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(
            null,
            message,
            "Thành công",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
*/    
    /**
    * Kiểm tra công thức có tồn tại không
    */
    public boolean isRecipeExists(int dishId, int materialId, int excludeId) {
        try {
            return recipeService.isRecipeExists(dishId, materialId, excludeId);
        } catch (Exception e) {
            showErrorMessage("Lỗi khi kiểm tra công thức: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy công thức theo ID món ăn
     */
    public List<Recipe> getByDishId(int dishId) {
        try {
            return recipeService.getRecipesByDishId(dishId);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return List.of();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải công thức: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Tính giá vốn món ăn từ công thức
     */
    public BigDecimal calculateDishCost(int dishId) {
        try {
            return recipeService.calculateDishCostFromRecipes(dishId);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return BigDecimal.ZERO;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tính giá vốn: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // ===== DishController.java - Bổ sung method =====

    /**
     * Cập nhật giá vốn cho món ăn (method mới cần thêm)
     */
    public boolean updateCostPrice(int dishId, BigDecimal costPrice) {
        try {
            if (dishId <= 0) {
                showErrorMessage("ID món ăn không hợp lệ");
                return false;
            }

            if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) < 0) {
                showErrorMessage("Giá vốn không hợp lệ");
                return false;
            }

            Dish dish = dishService.getDishById(dishId);
            if (dish == null) {
                showErrorMessage("Không tìm thấy món ăn");
                return false;
            }

            // Cập nhật giá vốn
            dish.setCostPrice(costPrice);
            boolean result = dishService.updateDish(dish);

            if (result) {
                System.out.println("Updated cost price for dish: " + dish.getName() + " = " + costPrice);
            } else {
                showErrorMessage("Không thể cập nhật giá vốn");
            }

            return result;
        } catch (Exception e) {
            System.err.println("Error updating cost price: " + e.getMessage());
            showErrorMessage("Lỗi khi cập nhật giá vốn: " + e.getMessage());
            return false;
        }
    }
}