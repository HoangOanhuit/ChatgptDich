/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;



import com.mycompany.quanlyquanan.dao.MaterialDAO;
import com.mycompany.quanlyquanan.dao.RecipeDAO;
import com.mycompany.quanlyquanan.model.Material;
import com.mycompany.quanlyquanan.utils.Session;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Service class cho Material - ĐÃ SỬA LỖI VÀ HOÀN THIỆN
 * @author Admin
 */
public class MaterialService {
    
    private final MaterialDAO materialDAO;
    private final RecipeDAO recipeDAO;

    public MaterialService() {
        this.materialDAO = new MaterialDAO();
        this.recipeDAO = new RecipeDAO();
    }

    public MaterialService(MaterialDAO materialDAO, RecipeDAO recipeDAO) {
        this.materialDAO = materialDAO;
        this.recipeDAO = recipeDAO;
    }

    /**
     * Lấy tất cả nguyên liệu
     */
    public List<Material> getAllMaterials() {
        return materialDAO.getAll();
    }

    /**
     * Lấy tất cả nguyên liệu đang hoạt động
     */
    public List<Material> getAllActiveMaterials() {
        return materialDAO.getAllActive();
    }

    /**
     * Lấy nguyên liệu sắp hết
     */
    public List<Material> getLowStockMaterials() {
        return materialDAO.getLowStockMaterials();
    }

    /**
     * Lấy nguyên liệu hết hàng
     */
    public List<Material> getOutOfStockMaterials() {
        return materialDAO.getOutOfStockMaterials();
    }

    /**
     * Lấy nguyên liệu theo ID
     */
    public Material getMaterialById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }
        return materialDAO.getById(id);
    }

    /**
     * Lấy nguyên liệu theo tên
     */
    public Material getMaterialByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nguyên liệu không được để trống");
        }
        return materialDAO.getByName(name.trim());
    }

    /**
     * Tạo nguyên liệu mới
     */
    public boolean createMaterial(Material material) {
        try {
            // Validation
            validateMaterial(material, true);

            // Kiểm tra quyền (admin hoặc thủ kho)
            if (!Session.getInstance().canManageInventory()) {
                throw new SecurityException("Không có quyền tạo nguyên liệu");
            }

            // Kiểm tra tên đã tồn tại chưa
            if (materialDAO.isNameExists(material.getName().trim(), 0)) {
                throw new IllegalArgumentException("Tên nguyên liệu đã tồn tại");
            }

            int userId = Session.getInstance().getCurrentUserId();
            material.setCreatedBy(userId);
            material.setCreatedAt(LocalDateTime.now());
            material.setUpdatedBy(userId);
            material.setUpdatedAt(LocalDateTime.now());

            return materialDAO.insert(material);

        } catch (IllegalArgumentException | SecurityException e) {
            // Ném lại exception để UI có thể bắt và hiển thị message
            throw e;
        } catch (Exception e) {
            // Log lỗi và ném exception với message user-friendly
            System.err.println("Error creating material: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể tạo nguyên liệu. Vui lòng thử lại.");
        }
    }

    /**
     * Cập nhật nguyên liệu
     */
    public boolean updateMaterial(Material material) {
        try {
            // Validation
            validateMaterial(material, false);

            // Kiểm tra quyền
            if (!Session.getInstance().canManageInventory()) {
                throw new SecurityException("Không có quyền cập nhật nguyên liệu");
            }

            // Kiểm tra nguyên liệu có tồn tại không
            Material existingMaterial = materialDAO.getById(material.getId());
            if (existingMaterial == null) {
                throw new IllegalArgumentException("Nguyên liệu không tồn tại");
            }

            // Kiểm tra tên đã tồn tại chưa (ngoại trừ chính nó)
            if (materialDAO.isNameExists(material.getName().trim(), material.getId())) {
                throw new IllegalArgumentException("Tên nguyên liệu đã tồn tại");
            }

            material.setUpdatedBy(Session.getInstance().getCurrentUserId());
            material.setUpdatedAt(LocalDateTime.now());

            return materialDAO.update(material);

        } catch (IllegalArgumentException | SecurityException e) {
            // Ném lại exception để UI có thể bắt và hiển thị message
            throw e;
        } catch (Exception e) {
            // Log lỗi và ném exception với message user-friendly
            System.err.println("Error updating material: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể cập nhật nguyên liệu. Vui lòng thử lại.");
        }
    }

    /**
     * Xóa nguyên liệu (soft delete)
     */
    public boolean deleteMaterial(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }
        
        // Kiểm tra quyền
        if (!Session.getInstance().canManageInventory()) {
            throw new SecurityException("Không có quyền xóa nguyên liệu");
        }
        
        // Kiểm tra nguyên liệu có tồn tại không
        Material material = materialDAO.getById(id);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        // Kiểm tra nguyên liệu có đang được sử dụng trong công thức không
        if (recipeDAO.isUsedInRecipes(id)) {
            throw new IllegalArgumentException("Không thể xóa nguyên liệu đang được sử dụng trong công thức");
        }
        
        return materialDAO.delete(id);
    }

    /**
     * Nhập kho (thêm số lượng)
     */
    public boolean importMaterial(int materialId, BigDecimal quantity, String note) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }
        
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        }
        
        // Kiểm tra quyền
        if (!Session.getInstance().canManageInventory()) {
            throw new SecurityException("Không có quyền nhập kho");
        }
        
        // Kiểm tra nguyên liệu có tồn tại không
        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        if (!material.isActive()) {
            throw new IllegalArgumentException("Nguyên liệu đã bị tạm ngưng");
        }
        
        // Thêm số lượng
        boolean success = materialDAO.addQuantity(materialId, quantity);
        
        if (success) {
            // TODO: Ghi log nhập kho
            // stockLogDAO.logImport(materialId, quantity, Session.getCurrentUserId(), note);
        }
        
        return success;
    }

    /**
     * Xuất kho (trừ số lượng)
     */
    public boolean exportMaterial(int materialId, BigDecimal quantity, String note) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }
        
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0");
        }
        
        // Kiểm tra quyền
        if (!Session.getInstance().canManageInventory()) {
            throw new SecurityException("Không có quyền xuất kho");
        }
        
        // Kiểm tra nguyên liệu có tồn tại không
        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        // Kiểm tra đủ số lượng để xuất không
        if (!materialDAO.hasEnoughQuantity(materialId, quantity)) {
            throw new IllegalArgumentException("Không đủ số lượng để xuất. Tồn kho: " + 
                                             material.getQuantityWithUnit());
        }
        
        // Trừ số lượng
        boolean success = materialDAO.subtractQuantity(materialId, quantity);
        
        if (success) {
            // TODO: Ghi log xuất kho
            // stockLogDAO.logExport(materialId, quantity, Session.getCurrentUserId(), note);
        }
        
        return success;
    }

    /**
     * Điều chỉnh số lượng tồn kho
     */
    public boolean adjustStock(int materialId, BigDecimal newQuantity, String reason) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }
        
        if (newQuantity == null || newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số lượng mới không được âm");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do điều chỉnh không được để trống");
        }
        
        // Kiểm tra quyền
        if (!Session.getInstance().canManageInventory()) {
            throw new SecurityException("Không có quyền điều chỉnh tồn kho");
        }
        
        // Kiểm tra nguyên liệu có tồn tại không
        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        BigDecimal oldQuantity = material.getQuantity();
        boolean success = materialDAO.updateQuantity(materialId, newQuantity);
        
        if (success) {
            // TODO: Ghi log điều chỉnh
            // stockLogDAO.logAdjustment(materialId, oldQuantity, newQuantity, 
            //                          Session.getCurrentUserId(), reason);
        }
        
        return success;
    }

    /**
     * Cập nhật giá đơn vị
     */
    public boolean updatePrice(int materialId, BigDecimal newPrice) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }
        
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá đơn vị phải lớn hơn 0");
        }
        
        // Kiểm tra quyền
        if (!Session.getInstance().canManageInventory()) {
            throw new SecurityException("Không có quyền cập nhật giá");
        }
        
        // Kiểm tra nguyên liệu có tồn tại không
        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        return materialDAO.updatePricePerUnit(materialId, newPrice);
    }

    /**
     * Cập nhật ngưỡng cảnh báo
     */
    public boolean updateThreshold(int materialId, BigDecimal newThreshold) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }
        
        if (newThreshold == null || newThreshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Ngưỡng cảnh báo không được âm");
        }
        
        // Kiểm tra quyền
        if (!Session.getInstance().canManageInventory()) {
            throw new SecurityException("Không có quyền cập nhật ngưỡng cảnh báo");
        }
        
        // Kiểm tra nguyên liệu có tồn tại không
        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }
        
        return materialDAO.updateThreshold(materialId, newThreshold);
    }

    /**
     * Tìm kiếm nguyên liệu
     */
    public List<Material> searchMaterials(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveMaterials();
        }
        return materialDAO.search(keyword.trim());
    }

    /**
     * Kiểm tra có đủ nguyên liệu để làm món không
     */
    public boolean canMakeDish(int dishId, int quantity) {
        if (dishId <= 0 || quantity <= 0) {
            return false;
        }
        
        var recipes = recipeDAO.getByDishId(dishId);
        
        for (var recipe : recipes) {
            BigDecimal requiredQuantity = recipe.getQuantity().multiply(BigDecimal.valueOf(quantity));
            if (!materialDAO.hasEnoughQuantity(recipe.getMaterialId(), requiredQuantity)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Tiêu thụ nguyên liệu khi làm món
     */
    public boolean consumeMaterialsForDish(int dishId, int quantity) {
        if (dishId <= 0 || quantity <= 0) {
            throw new IllegalArgumentException("Thông tin món ăn và số lượng không hợp lệ");
        }
        
        var recipes = recipeDAO.getByDishId(dishId);
        
        // Kiểm tra đủ nguyên liệu trước
        if (!canMakeDish(dishId, quantity)) {
            throw new IllegalArgumentException("Không đủ nguyên liệu để làm món");
        }
        
        // Tiêu thụ nguyên liệu
        for (var recipe : recipes) {
            BigDecimal requiredQuantity = recipe.getQuantity().multiply(BigDecimal.valueOf(quantity));
            boolean success = materialDAO.subtractQuantity(recipe.getMaterialId(), requiredQuantity);
            
            if (!success) {
                // TODO: Rollback previous operations
                throw new RuntimeException("Lỗi khi tiêu thụ nguyên liệu: " + recipe.getMaterialName());
            }
            
            // TODO: Ghi log tiêu thụ
            // stockLogDAO.logConsume(recipe.getMaterialId(), requiredQuantity, dishId, quantity);
        }
        
        return true;
    }

    /**
     * Lấy thống kê kho
     */
    public InventoryStats getInventoryStats() {
        long totalMaterials = materialDAO.count();
        long lowStockCount = materialDAO.countLowStock();
        BigDecimal totalValue = materialDAO.getTotalInventoryValue();
        
        return new InventoryStats(totalMaterials, lowStockCount, totalValue);
    }

    /**
     * ✅ FIXED: Validation cho nguyên liệu - Method đã hoàn thiện
     */
    private void validateMaterial(Material material, boolean isNew) {
        if (material == null) {
            throw new IllegalArgumentException("Thông tin nguyên liệu không được null");
        }
        
        // Validate name
        if (material.getName() == null || material.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nguyên liệu không được để trống");
        }
        
        if (material.getName().length() > 100) {
            throw new IllegalArgumentException("Tên nguyên liệu không được quá 100 ký tự");
        }
        
        // Validate unit
        if (material.getUnit() == null || material.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Đơn vị tính không được để trống");
        }
        
        if (material.getUnit().length() > 20) {
            throw new IllegalArgumentException("Đơn vị tính không được quá 20 ký tự");
        }
        
        // Validate quantity
        if (material.getQuantity() == null || material.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số lượng không được âm");
        }
        
        // Validate price per unit
        if (material.getPricePerUnit() == null || material.getPricePerUnit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá đơn vị phải lớn hơn 0");
        }
        
        if (material.getPricePerUnit().compareTo(BigDecimal.valueOf(100000000)) > 0) {
            throw new IllegalArgumentException("Giá đơn vị không được quá 100,000,000 VNĐ");
        }
        
        // Validate threshold
        if (material.getThreshold() == null || material.getThreshold().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Ngưỡng cảnh báo không được âm");
        }
        
        // Validate ID for update
        if (!isNew && material.getId() <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ cho việc cập nhật");
        }
    }

    public boolean updateMaterialQuantity(int materialId, BigDecimal newQuantity) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }

        if (newQuantity == null || newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }

        if (!Session.getInstance().canManageInventory()) {
            throw new SecurityException("Không có quyền cập nhật số lượng nguyên liệu");
        }

        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }

        return materialDAO.updateQuantity(materialId, newQuantity);
    }

    
    public boolean addMaterialStock(int materialId, BigDecimal addQuantity) {
        if (materialId <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }

        if (addQuantity == null || addQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        }

        if (!Session.getInstance().canManageInventory()) {
            throw new SecurityException("Không có quyền nhập kho");
        }

        Material material = materialDAO.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }

        return materialDAO.addQuantity(materialId, addQuantity);
    }

    public boolean toggleMaterialStatus(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID nguyên liệu không hợp lệ");
        }

        if (!Session.getInstance().canManageInventory()) {
            throw new SecurityException("Không có quyền cập nhật trạng thái nguyên liệu");
        }

        Material material = materialDAO.getById(id);
        if (material == null) {
            throw new IllegalArgumentException("Nguyên liệu không tồn tại");
        }

        material.setActive(!material.isActive());
        return materialDAO.update(material);
    }

    
    public BigDecimal getTotalInventoryValue() {
        return materialDAO.getTotalInventoryValue();
    }

    public boolean isMaterialNameExists(String name, int excludeId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nguyên liệu không được để trống");
        }
        return materialDAO.isNameExists(name.trim(), excludeId);
    }

    public boolean canDeleteMaterial(int id) {
        if (id <= 0) {
            return false;
        }

        Material material = materialDAO.getById(id);
        if (material == null) {
            return false;
        }

        return !recipeDAO.isUsedInRecipes(id);
    }

    /**
     * Inner class cho thống kê kho
     */
    public static class InventoryStats {
        private final long totalMaterials;
        private final long lowStockCount;
        private final BigDecimal totalValue;

        public InventoryStats(long totalMaterials, long lowStockCount, BigDecimal totalValue) {
            this.totalMaterials = totalMaterials;
            this.lowStockCount = lowStockCount;
            this.totalValue = totalValue;
        }

        // Getters
        public long getTotalMaterials() { return totalMaterials; }
        public long getLowStockCount() { return lowStockCount; }
        public BigDecimal getTotalValue() { return totalValue; }
        
        public double getLowStockPercentage() {
            return totalMaterials > 0 ? (double) lowStockCount / totalMaterials * 100 : 0;
        }
    }
}