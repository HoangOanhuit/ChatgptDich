/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.dao.CategoryDAO;
import com.mycompany.quanlyquanan.dao.DishDAO;
import com.mycompany.quanlyquanan.dao.RecipeDAO;
import com.mycompany.quanlyquanan.model.Category;
import com.mycompany.quanlyquanan.model.Dish;
import com.mycompany.quanlyquanan.model.Recipe;
import com.mycompany.quanlyquanan.utils.Session;
import static com.mysql.cj.conf.PropertyKey.logger;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Admin
 */
public class DishService {
    
    private final DishDAO dishDAO;
    private final CategoryDAO categoryDAO;
    private final RecipeDAO recipeDAO;
    
    private static final Logger logger = Logger.getLogger(DishService.class.getName());

    public DishService() {
        
        this.dishDAO = new DishDAO();
        this.categoryDAO = new CategoryDAO();
        this.recipeDAO = new RecipeDAO();
    }

    public DishService(DishDAO dishDAO, CategoryDAO categoryDAO, RecipeDAO recipeDAO) {
        this.dishDAO = dishDAO;
        this.categoryDAO = categoryDAO;
        this.recipeDAO = recipeDAO;
    }

    /**
     * Lấy tất cả món ăn
     */
    public List<Dish> getAllDishes() {
        return dishDAO.getAll();
    }

    /**
     * Lấy tất cả món ăn đang có sẵn
     */
    public List<Dish> getAllAvailableDishes() {
        return dishDAO.getAllAvailable();
    }

    /**
     * Lấy món ăn theo danh mục
     */
    public List<Dish> getDishesByCategory(int categoryId) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ");
        }
        
        // Kiểm tra danh mục có tồn tại không
        Category category = categoryDAO.getById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }
        
        return dishDAO.getByCategory(categoryId);
    }

    /**
     * Lấy món ăn theo ID
     */
    public Dish getDishById(int id) {
    if (id <= 0) {
        throw new IllegalArgumentException("ID món ăn không hợp lệ");
    }
    return dishDAO.getById(id);
}

    /**
     * Tạo món ăn mới
     */
    public boolean createDish(Dish dish) {
        // Validation
        validateDish(dish, true);
        
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền tạo món ăn");
        }
        
        // Kiểm tra tên món đã tồn tại chưa
        if (dishDAO.isNameExists(dish.getName(), 0)) {
            throw new IllegalArgumentException("Tên món ăn đã tồn tại");
        }
        
        // Kiểm tra danh mục có tồn tại và đang hoạt động không
        Category category = categoryDAO.getById(dish.getCategoryId());
        if (category == null || !category.isActive()) {
            throw new IllegalArgumentException("Danh mục không tồn tại hoặc đã bị vô hiệu hóa");
        }
        
        return dishDAO.insert(dish);
    }

    /**
     * Cập nhật món ăn
     */
    public boolean updateDish(Dish dish) {
        // Validation
        validateDish(dish, false);
        
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền cập nhật món ăn");
        }
        
        // Kiểm tra món ăn có tồn tại không
        Dish existingDish = dishDAO.getById(dish.getId());
        if (existingDish == null) {
            throw new IllegalArgumentException("Món ăn không tồn tại");
        }
        
        // Kiểm tra tên món đã tồn tại chưa (trừ chính nó)
        if (dishDAO.isNameExists(dish.getName(), dish.getId())) {
            throw new IllegalArgumentException("Tên món ăn đã tồn tại");
        }
        
        // Kiểm tra danh mục có tồn tại và đang hoạt động không
        Category category = categoryDAO.getById(dish.getCategoryId());
        if (category == null || !category.isActive()) {
            throw new IllegalArgumentException("Danh mục không tồn tại hoặc đã bị vô hiệu hóa");
        }
        
        return dishDAO.update(dish);
    }

    /**
     * Xóa món ăn (soft delete)
     */
    public boolean deleteDish(int id) {
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền xóa món ăn");
        }
        
        if (id <= 0) {
            throw new IllegalArgumentException("ID món ăn không hợp lệ");
        }
        
        // Kiểm tra món ăn có tồn tại không
        Dish dish = dishDAO.getById(id);
        if (dish == null) {
            throw new IllegalArgumentException("Món ăn không tồn tại");
        }
        
        // TODO: Kiểm tra món ăn có trong đơn hàng đang xử lý không
        // if (hasActiveOrders(id)) {
        //     throw new IllegalStateException("Không thể xóa món ăn đang có trong đơn hàng");
        // }
        
        return dishDAO.delete(id);
    }

    /**
     * Xóa vĩnh viễn món ăn và công thức của nó
     */
    public boolean permanentDeleteDish(int id) {
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền xóa vĩnh viễn món ăn");
        }
        
        if (id <= 0) {
            throw new IllegalArgumentException("ID món ăn không hợp lệ");
        }
        
        // TODO: Kiểm tra món ăn có trong đơn hàng nào không
        // if (hasAnyOrders(id)) {
        //     throw new IllegalStateException("Không thể xóa vĩnh viễn món ăn đã có đơn hàng");
        // }
        
        // Xóa công thức trước
        recipeDAO.deleteByDishId(id);
        
        // Sau đó xóa món ăn
        return dishDAO.hardDelete(id);
    }

    /**
    * Tìm kiếm món ăn theo từ khóa
    */
    public List<Dish> searchDishes(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return dishDAO.getAll();
        }
        return dishDAO.search(keyword);
    }

    /**
     * Lấy món ăn theo khoảng giá
     */
    public List<Dish> getDishesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null) minPrice = BigDecimal.ZERO;
        if (maxPrice == null) maxPrice = new BigDecimal("999999999");
        
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Giá tối thiểu không được lớn hơn giá tối đa");
        }
        
        return dishDAO.getByPriceRange(minPrice, maxPrice);
    }

    /**
     * Kích hoạt/vô hiệu hóa món ăn
     */
    public boolean toggleDishAvailability(int id) {
        // Kiểm tra quyền (nhân viên cũng có thể thay đổi tính có sẵn của món)
        if (!Session.getInstance().isAdmin() && !Session.getInstance().isNhanVien()) {
            throw new SecurityException("Không có quyền thay đổi tính có sẵn của món ăn");
        }
        
        Dish dish = dishDAO.getById(id);
        if (dish == null) {
            throw new IllegalArgumentException("Món ăn không tồn tại");
        }
        
        dish.setAvailable(!dish.isAvailable());
        return dishDAO.update(dish);
    }

    /**
     * Cập nhật giá vốn từ công thức
     */
    public boolean updateCostPriceFromRecipe(int dishId) {
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền cập nhật giá vốn");
        }
        
        Dish dish = dishDAO.getById(dishId);
        if (dish == null) {
            throw new IllegalArgumentException("Món ăn không tồn tại");
        }
        
        // Tính giá vốn từ công thức
        BigDecimal calculatedCostPrice = dishDAO.calculateCostPriceFromRecipes(dishId);
        dish.setCostPrice(calculatedCostPrice);
        
        return dishDAO.update(dish);
    }

    /**
     * Lấy công thức của món ăn
     */
    public List<Recipe> getDishRecipes(int dishId) {
        if (dishId <= 0) {
            throw new IllegalArgumentException("ID món ăn không hợp lệ");
        }
        
        return recipeDAO.getByDishId(dishId);
    }

    /**
     * Kiểm tra tên món ăn đã tồn tại chưa
     */
    public boolean isDishNameExists(String name, int excludeId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        return dishDAO.isNameExists(name.trim(), excludeId);
    }

    /**
     * Tính lợi nhuận của món ăn
     */
    public BigDecimal calculateDishProfit(int dishId) {
        Dish dish = dishDAO.getById(dishId);
        if (dish == null) {
            return BigDecimal.ZERO;
        }
        
        return dish.getProfitMargin();
    }

    /**
     * Tính % lợi nhuận của món ăn
     */
    public double calculateDishProfitPercentage(int dishId) {
        Dish dish = dishDAO.getById(dishId);
        if (dish == null) {
            return 0.0;
        }
        
        return dish.getProfitPercentage();
    }

    /**
     * Lấy những món ăn có lợi nhuận cao nhất
     */
    public List<Dish> getHighestProfitDishes(int limit) {
        List<Dish> allDishes = getAllAvailableDishes();
        
        // Sắp xếp theo lợi nhuận giảm dần
        allDishes.sort((d1, d2) -> d2.getProfitMargin().compareTo(d1.getProfitMargin()));
        
        // Lấy top limit
        return allDishes.subList(0, Math.min(limit, allDishes.size()));
    }

    /**
     * Lấy những món ăn có lợi nhuận thấp nhất (cần xem xét)
     */
    public List<Dish> getLowestProfitDishes(int limit) {
        List<Dish> allDishes = getAllAvailableDishes();
        
        // Sắp xếp theo lợi nhuận tăng dần
        allDishes.sort((d1, d2) -> d1.getProfitMargin().compareTo(d2.getProfitMargin()));
        
        // Lấy bottom limit
        return allDishes.subList(0, Math.min(limit, allDishes.size()));
    }

    /**
     * Validate thông tin món ăn
     */
    private void validateDish(Dish dish, boolean isNew) {
        if (dish == null) {
            throw new IllegalArgumentException("Thông tin món ăn không được null");
        }
        
        // Validate ID cho update
        if (!isNew && dish.getId() <= 0) {
            throw new IllegalArgumentException("ID món ăn không hợp lệ");
        }
        
        // Validate tên
        if (dish.getName() == null || dish.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên món ăn không được để trống");
        }
        
        if (dish.getName().trim().length() > 150) {
            throw new IllegalArgumentException("Tên món ăn không được vượt quá 150 ký tự");
        }
        
        // Validate category
        if (dish.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Danh mục không hợp lệ");
        }
        
        // Validate giá
        if (dish.getPrice() == null || dish.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn 0");
        }
        
        if (dish.getPrice().compareTo(new BigDecimal("99999999")) > 0) {
            throw new IllegalArgumentException("Giá bán không được vượt quá 99,999,999");
        }
        
        // Validate giá vốn
        if (dish.getCostPrice() != null && dish.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá vốn không được âm");
        }
        
        if (dish.getCostPrice() != null && dish.getCostPrice().compareTo(dish.getPrice()) > 0) {
            throw new IllegalArgumentException("Giá vốn không được lớn hơn giá bán");
        }
        
        // Validate thời gian chế biến
        if (dish.getPrepTime() <= 0) {
            throw new IllegalArgumentException("Thời gian chế biến phải lớn hơn 0");
        }
        
        if (dish.getPrepTime() > 999) {
            throw new IllegalArgumentException("Thời gian chế biến không được vượt quá 999 phút");
        }
        
        // Validate mô tả
        if (dish.getDescription() != null && dish.getDescription().length() > 1000) {
            throw new IllegalArgumentException("Mô tả không được vượt quá 1000 ký tự");
        }
        
        // Validate URL ảnh
        if (dish.getImageUrl() != null && dish.getImageUrl().length() > 500) {
            throw new IllegalArgumentException("URL ảnh không được vượt quá 500 ký tự");
        }
        
        // Trim và set lại các giá trị
        dish.setName(dish.getName().trim());
        if (dish.getDescription() != null) {
            dish.setDescription(dish.getDescription().trim());
        }
        if (dish.getImageUrl() != null) {
            dish.setImageUrl(dish.getImageUrl().trim());
        }
    }
    /**
    * Kiểm tra xem món ăn có đang được sử dụng không
    */
    private boolean isDishInUse(int dishId) {
        String sql = "SELECT COUNT(*) FROM order_items WHERE dish_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dishId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error checking dish usage: " + e.getMessage(), e);
        }

        return false;
    }

    // Lấy thức ăn theo từ khóa tìm kiếm
    public List<Dish> getBySearch(String searchString) {
        return dishDAO.getBySearch(searchString);
    }

}