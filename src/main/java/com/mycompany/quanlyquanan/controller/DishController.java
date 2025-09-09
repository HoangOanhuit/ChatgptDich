/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.controller;

import com.mycompany.quanlyquanan.model.Category;
import com.mycompany.quanlyquanan.model.Dish;
import com.mycompany.quanlyquanan.model.Recipe;
import com.mycompany.quanlyquanan.service.CategoryService;
import com.mycompany.quanlyquanan.service.DishService;
import static com.mysql.cj.conf.PropertyKey.logger;

import javax.swing.JOptionPane;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Administrator
 */
public class DishController {
    private final DishService dishService;
    private final CategoryService categoryService;

    public DishController() {
        this.dishService = new DishService();
        this.categoryService = new CategoryService();
    }

    public DishController(DishService dishService, CategoryService categoryService) {
        this.dishService = dishService;
        this.categoryService = categoryService;
    }

    /**
     * Lấy tất cả món ăn
     */
    public List<Dish> getAll() {
        try {
            return dishService.getAllDishes();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải danh sách món ăn: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy tất cả món ăn có sẵn
     */
    public List<Dish> getAllAvailable() {
        try {
            return dishService.getAllAvailableDishes();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải danh sách món ăn: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy món ăn theo danh mục
     */
    public List<Dish> getByCategory(int categoryId) {
        try {
            return dishService.getDishesByCategory(categoryId);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return List.of();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải món ăn theo danh mục: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy món ăn theo ID
     */
    public Dish getById(int id) {
        try {
            return dishService.getDishById(id);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return null;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải thông tin món ăn: " + e.getMessage());
            return null;
        }
    }
    


    /**
     * Tạo món ăn mới
     */
    public boolean create(Dish dish) {
        try {
            boolean result = dishService.createDish(dish);
            if (!result) {
                showErrorMessage("Không thể tạo món ăn. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tạo món ăn: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật món ăn
     */
    public boolean update(Dish dish) {
        try {
            boolean result = dishService.updateDish(dish);
            if (!result) {
                showErrorMessage("Không thể cập nhật món ăn. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi cập nhật món ăn: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa món ăn (soft delete)
     */
    public boolean delete(int id) {
        try {
            Dish dish = dishService.getDishById(id);
            if (dish == null) {
                showErrorMessage("Không tìm thấy món ăn");
                return false;
            }

            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc chắn muốn xóa món ăn '" + dish.getName() + "'?\n" +
                "Món ăn sẽ được đánh dấu là không có sẵn.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = dishService.deleteDish(id);
            if (!result) {
                showErrorMessage("Không thể xóa món ăn. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi xóa món ăn: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa vĩnh viễn món ăn (hard delete)
     */
    public boolean permanentDelete(int id) {
        try {
            Dish dish = dishService.getDishById(id);
            if (dish == null) {
                showErrorMessage("Không tìm thấy món ăn");
                return false;
            }

            int confirm = JOptionPane.showConfirmDialog(
                null,
                "⚠️ CẢNH BÁO: Bạn có chắc chắn muốn XÓA VĨNH VIỄN món ăn '" + dish.getName() + "'?\n" +
                "Hành động này sẽ xóa cả công thức và KHÔNG THỂ HOÀN TÁC!\n\n" +
                "Nhấn YES để xóa vĩnh viễn, NO để hủy bỏ.",
                "⚠️ Xác nhận xóa vĩnh viễn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = dishService.permanentDeleteDish(id);
            if (!result) {
                showErrorMessage("Không thể xóa vĩnh viễn món ăn. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi xóa vĩnh viễn món ăn: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tìm kiếm món ăn
     */
    public List<Dish> search(String keyword) {
        try {
            return dishService.searchDishes(keyword);
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tìm kiếm món ăn: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy món ăn theo khoảng giá
     */
    public List<Dish> getByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        try {
            return dishService.getDishesByPriceRange(minPrice, maxPrice);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return List.of();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tìm món ăn theo giá: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Kích hoạt/vô hiệu hóa món ăn
     */
    public boolean toggleAvailability(int id) {
        try {
            Dish dish = dishService.getDishById(id);
            if (dish == null) {
                showErrorMessage("Không tìm thấy món ăn");
                return false;
            }

            String action = dish.isAvailable() ? "đánh dấu không có sẵn" : "đánh dấu có sẵn";
            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc chắn muốn " + action + " món ăn '" + dish.getName() + "'?",
                "Xác nhận thay đổi",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = dishService.toggleDishAvailability(id);
            if (!result) {
                showErrorMessage("Không thể thay đổi tình trạng món ăn. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi thay đổi tình trạng món ăn: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật giá vốn từ công thức
     */
    public boolean updateCostPriceFromRecipe(int dishId) {
        try {
            boolean result = dishService.updateCostPriceFromRecipe(dishId);
            if (!result) {
                showErrorMessage("Không thể cập nhật giá vốn. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi cập nhật giá vốn: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy công thức của món ăn
     */
    public List<Recipe> getDishRecipes(int dishId) {
        try {
            return dishService.getDishRecipes(dishId);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return List.of();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải công thức món ăn: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy các danh mục đang hoạt động (cho dropdown)
     */
    public List<Category> getActiveCategories() {
        try {
            return categoryService.getAllActiveCategories();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải danh sách danh mục: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Kiểm tra tên món ăn đã tồn tại chưa
     */
    public boolean isNameExists(String name, int excludeId) {
        try {
            return dishService.isDishNameExists(name, excludeId);
        } catch (Exception e) {
            showErrorMessage("Lỗi khi kiểm tra tên món ăn: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tính lợi nhuận của món ăn
     */
    public BigDecimal calculateProfit(int dishId) {
        try {
            return dishService.calculateDishProfit(dishId);
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tính lợi nhuận: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Tính % lợi nhuận của món ăn
     */
    public double calculateProfitPercentage(int dishId) {
        try {
            return dishService.calculateDishProfitPercentage(dishId);
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tính % lợi nhuận: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Lấy món ăn có lợi nhuận cao nhất
     */
    public List<Dish> getHighestProfitDishes(int limit) {
        try {
            return dishService.getHighestProfitDishes(limit);
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải món ăn lợi nhuận cao: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Validate dữ liệu trước khi submit
     */
    public boolean validateDish(Dish dish, boolean isNew) {
        if (dish == null) {
            showErrorMessage("Thông tin món ăn không hợp lệ");
            return false;
        }

        // Validate tên
        if (dish.getName() == null || dish.getName().trim().isEmpty()) {
            showErrorMessage("Tên món ăn không được để trống");
            return false;
        }

        if (dish.getName().trim().length() > 150) {
            showErrorMessage("Tên món ăn không được vượt quá 150 ký tự");
            return false;
        }

        // Validate category
        if (dish.getCategoryId() <= 0) {
            showErrorMessage("Vui lòng chọn danh mục");
            return false;
        }

        // Validate giá
        if (dish.getPrice() == null || dish.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            showErrorMessage("Giá bán phải lớn hơn 0");
            return false;
        }

        if (dish.getPrice().compareTo(new BigDecimal("99999999")) > 0) {
            showErrorMessage("Giá bán quá lớn");
            return false;
        }

        // Validate giá vốn
        if (dish.getCostPrice() != null) {
            if (dish.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
                showErrorMessage("Giá vốn không được âm");
                return false;
            }

            if (dish.getCostPrice().compareTo(dish.getPrice()) > 0) {
                showErrorMessage("Giá vốn không được lớn hơn giá bán");
                return false;
            }
        }

        // Validate thời gian chế biến
        if (dish.getPrepTime() <= 0) {
            showErrorMessage("Thời gian chế biến phải lớn hơn 0");
            return false;
        }

        if (dish.getPrepTime() > 999) {
            showErrorMessage("Thời gian chế biến quá lớn");
            return false;
        }

        // Validate mô tả
        if (dish.getDescription() != null && dish.getDescription().length() > 1000) {
            showErrorMessage("Mô tả không được vượt quá 1000 ký tự");
            return false;
        }

        // Kiểm tra trùng tên
        int excludeId = isNew ? 0 : dish.getId();
        if (isNameExists(dish.getName().trim(), excludeId)) {
            showErrorMessage("Tên món ăn đã tồn tại");
            return false;
        }

        // Kiểm tra danh mục có tồn tại và đang hoạt động
        try {
            Category category = categoryService.getCategoryById(dish.getCategoryId());
            if (category == null || !category.isActive()) {
                showErrorMessage("Danh mục không tồn tại hoặc đã bị vô hiệu hóa");
                return false;
            }
        } catch (Exception e) {
            showErrorMessage("Lỗi khi kiểm tra danh mục: " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Tạo món ăn mới với validation
     */
    public boolean createWithValidation(String name, int categoryId, BigDecimal price, 
                                      BigDecimal costPrice, String imageUrl, int prepTime, String description) {
        Dish dish = new Dish();
        dish.setName(name);
        dish.setCategoryId(categoryId);
        dish.setPrice(price);
        dish.setCostPrice(costPrice);
        dish.setImageUrl(imageUrl);
        dish.setPrepTime(prepTime);
        dish.setDescription(description);

        if (!validateDish(dish, true)) {
            return false;
        }

        return create(dish);
    }

    /**
     * Cập nhật món ăn với validation
     */
    public boolean updateWithValidation(int id, String name, int categoryId, BigDecimal price, 
                                      BigDecimal costPrice, String imageUrl, int prepTime, 
                                      String description, boolean isAvailable) {
        Dish dish = getById(id);
        if (dish == null) {
            return false;
        }

        dish.setName(name);
        dish.setCategoryId(categoryId);
        dish.setPrice(price);
        dish.setCostPrice(costPrice);
        dish.setImageUrl(imageUrl);
        dish.setPrepTime(prepTime);
        dish.setDescription(description);
        dish.setAvailable(isAvailable);

        if (!validateDish(dish, false)) {
            return false;
        }

        return update(dish);
    }

    /**
     * Format giá tiền để hiển thị
     */
    public String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0 ₫";
        }
        return String.format("%,.0f ₫", price);
    }

    /**
     * Format thời gian chế biến
     */
    public String formatPrepTime(int minutes) {
        if (minutes < 60) {
            return minutes + " phút";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + " giờ";
            } else {
                return hours + "h " + remainingMinutes + "p";
            }
        }
    }

    /**
     * Format lợi nhuận
     */
    public String formatProfitPercentage(double percentage) {
        if (percentage == 0.0) {
            return "0%";
        }
        return String.format("%.1f%%", percentage);
    }
    
    /**
    * Cập nhật giá vốn cho món ăn
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

    /**
     * Cập nhật giá vốn hàng loạt
     */
    public int updateCostPriceBatch(Map<Integer, BigDecimal> dishCosts) {
        int successCount = 0;

        for (Map.Entry<Integer, BigDecimal> entry : dishCosts.entrySet()) {
            if (entry.getValue() != null) {
                if (updateCostPrice(entry.getKey(), entry.getValue())) {
                    successCount++;
                }
            }
        }

        return successCount;
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
    public boolean updateDish(Dish dish) {
        try {
            boolean result = dishService.updateDish(dish);
            if (!result) {
                showErrorMessage("Không thể cập nhật món ăn. Vui lòng thử lại.");
            }
            return result;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi cập nhật món ăn: " + e.getMessage());
            return false;
        }  
    }
    
     public List<Dish> getBySearch(String searchString) {
        return dishService.getBySearch(searchString);
    }

}

