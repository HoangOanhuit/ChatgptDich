package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.dao.CategoryDAO;
import com.mycompany.quanlyquanan.model.Category;
import com.mycompany.quanlyquanan.utils.Session;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;

/**
 * Service class cho Category - ĐÃ SỬA LỖI VÀ HOÀN THIỆN
 * @author Admin
 */
public class CategoryService {
    
    private final CategoryDAO dao;

    public CategoryService() {
        this.dao = new CategoryDAO();
    }

    public CategoryService(CategoryDAO dao) {
        this.dao = dao;
    }

    /**
     * Lấy tất cả danh mục
     */
    public List<Category> getAllCategories() {
        return dao.getAll();
    }

    /**
     * Lấy tất cả danh mục đang hoạt động
     */
    public List<Category> getAllActiveCategories() {
        return dao.getAllActive();
    }

    /**
     * Lấy danh mục theo ID
     */
    public Category getCategoryById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ");
        }
        return dao.getById(id);
    }

    /**
     * Tạo danh mục mới
     */
    public boolean createCategory(Category category) {
        // Validation
        validateCategory(category, true);
        
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền tạo danh mục");
        }
        
        // Kiểm tra tên đã tồn tại chưa
        if (dao.isNameExists(category.getName(), 0)) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }
        
        return dao.insert(category);
    }
    /**
    * Map ResultSet thành Category object
    */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setActive(rs.getBoolean("is_active"));
        category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        category.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return category;
    }
    /**
     * Cập nhật danh mục
     */
    public boolean updateCategory(Category category) {
        // Validation
        validateCategory(category, false);
        
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền cập nhật danh mục");
        }
        
        // Kiểm tra danh mục có tồn tại không
        Category existingCategory = dao.getById(category.getId());
        if (existingCategory == null) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }
        
        // Kiểm tra tên đã tồn tại chưa (ngoại trừ chính nó)
        if (dao.isNameExists(category.getName(), category.getId())) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }
        
        return dao.update(category);
    }

    /**
     * Xóa danh mục (soft delete)
     */
    public boolean deleteCategory(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ");
        }
        
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền xóa danh mục");
        }
        
        // Kiểm tra danh mục có tồn tại không
        Category category = dao.getById(id);
        if (category == null) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }
        
        // Kiểm tra danh mục có đang được sử dụng không
        if (dao.hasDishesByCategory(id)) {
            throw new IllegalArgumentException("Không thể xóa danh mục đang có món ăn");
        }
        
        return dao.delete(id);
    }

    /**
     * Cập nhật trạng thái hoạt động của danh mục
     */
    public boolean updateActiveStatus(int id, boolean isActive) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ");
        }
        
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền thay đổi trạng thái danh mục");
        }
        
        // Kiểm tra danh mục có tồn tại không
        Category category = dao.getById(id);
        if (category == null) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }
        
        // Nếu tạm ngưng danh mục, kiểm tra có món ăn đang hoạt động không
        if (!isActive && dao.hasActiveDishes(id)) {
            throw new IllegalArgumentException("Không thể tạm ngưng danh mục có món ăn đang hoạt động");
        }
        
        return dao.updateActiveStatus(id, isActive);
    }

    /**
     * Tìm kiếm danh mục theo tên
     */
    public List<Category> searchCategories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveCategories();
        }
        return dao.search(keyword.trim());
    }

    /**
     * Lấy danh mục theo tên
     */
    public Category getCategoryByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }
        return dao.getByName(name.trim());
    }

    /**
     * Đếm số lượng danh mục
     */
    public long countCategories() {
        return dao.count();
    }

    /**
     * Đếm số lượng danh mục đang hoạt động
     */
    public long countActiveCategories() {
        return dao.countActive();
    }

    /**
     * Kiểm tra danh mục có món ăn không
     */
    public boolean hasDishesByCategory(int categoryId) {
        if (categoryId <= 0) {
            return false;
        }
        return dao.hasDishesByCategory(categoryId);
    }

    /**
     * Lấy số lượng món ăn theo danh mục
     */
    public int getDishCountByCategory(int categoryId) {
        if (categoryId <= 0) {
            return 0;
        }
        return dao.getDishCount(categoryId);
    }

    /**
     * ✅ FIXED: Validation cho danh mục - Method đã hoàn thiện
     */
    private void validateCategory(Category category, boolean isNew) {
        if (category == null) {
            throw new IllegalArgumentException("Danh mục không được null");
        }
        
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }
        
        if (category.getName().length() > 100) {
            throw new IllegalArgumentException("Tên danh mục không được quá 100 ký tự");
        }
        
        if (category.getDescription() != null && category.getDescription().length() > 500) {
            throw new IllegalArgumentException("Mô tả không được quá 500 ký tự");
        }
        
        if (!isNew && category.getId() <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ cho việc cập nhật");
        }
    }

    /**
     * Sao chép danh mục
     */
    public boolean duplicateCategory(int categoryId, String newName) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ");
        }
        
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục mới không được để trống");
        }
        
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền sao chép danh mục");
        }
        
        // Lấy danh mục gốc
        Category originalCategory = dao.getById(categoryId);
        if (originalCategory == null) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }
        
        // Kiểm tra tên mới đã tồn tại chưa
        if (dao.isNameExists(newName.trim(), 0)) {
            throw new IllegalArgumentException("Tên danh mục mới đã tồn tại");
        }
        
        // Tạo danh mục mới
        Category newCategory = new Category();
        newCategory.setName(newName.trim());
        newCategory.setDescription("Sao chép từ: " + originalCategory.getName() + 
                                 (originalCategory.getDescription() != null ? "\n" + originalCategory.getDescription() : ""));
        newCategory.setActive(false); // Tạm thời chưa hoạt động
        
        return dao.insert(newCategory);
    }

    /**
     * Lấy danh mục phổ biến nhất (có nhiều món ăn nhất)
     */
    public Category getMostPopularCategory() {
        return dao.getMostPopular();
    }

    /**
     * Lấy danh sách danh mục theo số lượng món ăn (giảm dần)
     */
    public List<Category> getCategoriesByDishCount() {
        return dao.getByDishCountDesc();
    }

    /**
     * Cập nhật mô tả danh mục
     */
    public boolean updateDescription(int id, String description) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ");
        }
        
        // Kiểm tra quyền
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền cập nhật mô tả danh mục");
        }
        
        // Kiểm tra danh mục có tồn tại không
        Category category = dao.getById(id);
        if (category == null) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }
        
        // Validate mô tả
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("Mô tả không được quá 500 ký tự");
        }
        
        return dao.updateDescription(id, description);
    }

    /**
     * Kiểm tra tên danh mục đã tồn tại chưa
     */
    public boolean isNameExists(String name, int excludeId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return dao.isNameExists(name.trim(), excludeId);
    }

    /**
     * Lấy danh mục có ít món ăn nhất
     */
    public Category getLeastPopularCategory() {
        return dao.getLeastPopular();
    }

    /**
     * Thống kê tổng quan danh mục
     */
    public CategoryStats getCategoryStats() {
        long totalCategories = dao.count();
        long activeCategories = dao.countActive();
        long inactiveCategories = totalCategories - activeCategories;
        Category mostPopular = dao.getMostPopular();
        Category leastPopular = dao.getLeastPopular();
        
        return new CategoryStats(totalCategories, activeCategories, inactiveCategories,
                               mostPopular, leastPopular);
    }

    public boolean permanentDeleteCategory(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ");
        }
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền xóa vĩnh viễn danh mục");
        }
        Category category = dao.getById(id);
        if (category == null) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }
        return dao.hardDelete(id);
    }

    public boolean toggleCategoryStatus(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ");
        }
        if (!Session.getInstance().isAdmin()) {
            throw new SecurityException("Chỉ admin mới có quyền cập nhật trạng thái danh mục");
        }
        Category category = dao.getById(id);
        if (category == null) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }
        if (dao.hasDishesByCategory(id)) {
            throw new IllegalArgumentException("Không thể xóa vĩnh viễn danh mục đang có món ăn");
        }        
        return dao.updateActiveStatus(id, !category.isActive());
    }

    public boolean isCategoryNameExists(String name, int excludeId) {
     return isNameExists(name, excludeId);
    }

    public boolean canDeleteCategory(int id) {
        if (id <= 0) {
            return false;
        }
        return !dao.hasDishesByCategory(id);    
    }

    /**
     * Inner class cho thống kê danh mục
     */
    public static class CategoryStats {
        private final long totalCategories;
        private final long activeCategories;
        private final long inactiveCategories;
        private final Category mostPopular;
        private final Category leastPopular;

        public CategoryStats(long totalCategories, long activeCategories, long inactiveCategories,
                           Category mostPopular, Category leastPopular) {
            this.totalCategories = totalCategories;
            this.activeCategories = activeCategories;
            this.inactiveCategories = inactiveCategories;
            this.mostPopular = mostPopular;
            this.leastPopular = leastPopular;
        }

        // Getters
        public long getTotalCategories() { return totalCategories; }
        public long getActiveCategories() { return activeCategories; }
        public long getInactiveCategories() { return inactiveCategories; }
        public Category getMostPopular() { return mostPopular; }
        public Category getLeastPopular() { return leastPopular; }
        
        public double getActivePercentage() {
            return totalCategories > 0 ? (double) activeCategories / totalCategories * 100 : 0;
        }
    }
}
