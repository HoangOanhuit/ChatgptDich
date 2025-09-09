/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.controller;

import com.mycompany.quanlyquanan.model.Material;
import com.mycompany.quanlyquanan.service.MaterialService;

import javax.swing.JOptionPane;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Admin
 */
public class MaterialController {
    
    private final MaterialService service;

    public MaterialController() {
        this.service = new MaterialService();
    }

    public MaterialController(MaterialService service) {
        this.service = service;
    }

    /**
     * Lấy tất cả nguyên liệu
     */
    public List<Material> getAll() {
        try {
            return service.getAllMaterials();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải danh sách nguyên liệu: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy tất cả nguyên liệu đang hoạt động
     */
    public List<Material> getAllActive() {
        try {
            List<Material> result = service.getAllActiveMaterials();
            System.out.println("MaterialController.getAllActive() returned: " + 
                              (result != null ? result.size() : "null") + " items");
            return result;
        } catch (Exception e) {
            System.err.println("Error in MaterialController.getAllActive(): " + e.getMessage());
            showErrorMessage("Lỗi khi tải danh sách nguyên liệu: " + e.getMessage());
            return List.of(); // Trả về list rỗng thay vì null
        }
    }

    /**
     * Lấy nguyên liệu sắp hết
     */
    public List<Material> getLowStock() {
        try {
            return service.getLowStockMaterials();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải nguyên liệu sắp hết: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy nguyên liệu theo ID
     */
    public Material getById(int id) {
        try {
            return service.getMaterialById(id);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return null;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải thông tin nguyên liệu: " + e.getMessage());
            return null;
        }
    }

    /**
     * Tạo nguyên liệu mới
     */
    public boolean create(Material material) {
        try {
            boolean result = service.createMaterial(material);
            if (!result) {
                showErrorMessage("Không thể thêm nguyên liệu. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException e) {
            // Hiển thị message lỗi cụ thể từ validation
            showErrorMessage(e.getMessage());
            return false;
        } catch (RuntimeException e) {
            // Hiển thị message lỗi từ service
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            // Lỗi không mong đợi
            showErrorMessage("Đã xảy ra lỗi không mong đợi: " + e.getMessage());
            System.err.println("Unexpected error in MaterialController.create: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật nguyên liệu
     */
    public boolean update(Material material) {
        try {
            boolean result = service.updateMaterial(material);
            if (!result) {
                showErrorMessage("Không thể cập nhật nguyên liệu. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException e) {
            // Hiển thị message lỗi cụ thể từ validation
            showErrorMessage(e.getMessage());
            return false;
        } catch (RuntimeException e) {
            // Hiển thị message lỗi từ service
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            // Lỗi không mong đợi
            showErrorMessage("Đã xảy ra lỗi không mong đợi: " + e.getMessage());
            System.err.println("Unexpected error in MaterialController.update: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa nguyên liệu (soft delete)
     */
    public boolean delete(int id) {
        try {
            Material material = service.getMaterialById(id);
            if (material == null) {
                showErrorMessage("Không tìm thấy nguyên liệu");
                return false;
            }

            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc chắn muốn xóa nguyên liệu '" + material.getName() + "'?\n" +
                "Nguyên liệu sẽ được đánh dấu là không hoạt động.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = service.deleteMaterial(id);
            if (!result) {
                showErrorMessage("Không thể xóa nguyên liệu. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi xóa nguyên liệu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật số lượng nguyên liệu
     */
    public boolean updateQuantity(int materialId, BigDecimal newQuantity) {
        try {
            Material material = service.getMaterialById(materialId);
            if (material == null) {
                showErrorMessage("Không tìm thấy nguyên liệu");
                return false;
            }

            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc chắn muốn cập nhật số lượng nguyên liệu '" + material.getName() + "'?\n" +
                "Từ: " + material.getQuantityWithUnit() + "\n" +
                "Thành: " + newQuantity + " " + material.getUnit(),
                "Xác nhận cập nhật số lượng",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = service.updateMaterialQuantity(materialId, newQuantity);
            if (!result) {
                showErrorMessage("Không thể cập nhật số lượng. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi cập nhật số lượng: " + e.getMessage());
            return false;
        }
    }

    /**
     * Nhập thêm nguyên liệu
     */
    public boolean addStock(int materialId, BigDecimal addQuantity) {
        try {
            Material material = service.getMaterialById(materialId);
            if (material == null) {
                showErrorMessage("Không tìm thấy nguyên liệu");
                return false;
            }

            BigDecimal newTotal = material.getQuantity().add(addQuantity);
            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Xác nhận nhập nguyên liệu '" + material.getName() + "'?\n" +
                "Số lượng nhập: " + addQuantity + " " + material.getUnit() + "\n" +
                "Tồn kho hiện tại: " + material.getQuantityWithUnit() + "\n" +
                "Tồn kho sau nhập: " + newTotal + " " + material.getUnit(),
                "Xác nhận nhập kho",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = service.addMaterialStock(materialId, addQuantity);
            if (!result) {
                showErrorMessage("Không thể nhập kho. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi nhập kho: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tìm kiếm nguyên liệu
     */
    public List<Material> search(String keyword) {
        try {
            return service.searchMaterials(keyword);
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tìm kiếm nguyên liệu: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Kích hoạt/vô hiệu hóa nguyên liệu
     */
    public boolean toggleStatus(int id) {
        try {
            Material material = service.getMaterialById(id);
            if (material == null) {
                showErrorMessage("Không tìm thấy nguyên liệu");
                return false;
            }

            String action = material.isActive() ? "vô hiệu hóa" : "kích hoạt";
            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc chắn muốn " + action + " nguyên liệu '" + material.getName() + "'?",
                "Xác nhận " + action,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = service.toggleMaterialStatus(id);
            if (!result) {
                showErrorMessage("Không thể thay đổi trạng thái nguyên liệu. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi thay đổi trạng thái nguyên liệu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy tổng giá trị kho
     */
    public BigDecimal getTotalInventoryValue() {
        try {
            return service.getTotalInventoryValue();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tính tổng giá trị kho: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Lấy nguyên liệu theo tên
     */
    public Material getByName(String name) {
        try {
            return service.getMaterialByName(name);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return null;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tìm nguyên liệu theo tên: " + e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra tên nguyên liệu đã tồn tại chưa
     */
    public boolean isNameExists(String name, int excludeId) {
        try {
            return service.isMaterialNameExists(name, excludeId);
        } catch (Exception e) {
            showErrorMessage("Lỗi khi kiểm tra tên nguyên liệu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra nguyên liệu có thể xóa được không
     */
    public boolean canDelete(int id) {
        try {
            return service.canDeleteMaterial(id);
        } catch (Exception e) {
            showErrorMessage("Lỗi khi kiểm tra khả năng xóa nguyên liệu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate dữ liệu trước khi submit
     */
    public boolean validateMaterial(Material material, boolean isNew) {
        if (material == null) {
            showErrorMessage("Thông tin nguyên liệu không hợp lệ");
            return false;
        }

        // Validate tên
        if (material.getName() == null || material.getName().trim().isEmpty()) {
            showErrorMessage("Tên nguyên liệu không được để trống");
            return false;
        }

        if (material.getName().trim().length() > 100) {
            showErrorMessage("Tên nguyên liệu không được vượt quá 100 ký tự");
            return false;
        }

        // Validate đơn vị
        if (material.getUnit() == null || material.getUnit().trim().isEmpty()) {
            showErrorMessage("Đơn vị không được để trống");
            return false;
        }

        if (material.getUnit().trim().length() > 20) {
            showErrorMessage("Đơn vị không được vượt quá 20 ký tự");
            return false;
        }

        // Validate giá
        if (material.getPricePerUnit() == null || material.getPricePerUnit().compareTo(BigDecimal.ZERO) <= 0) {
            showErrorMessage("Giá phải lớn hơn 0");
            return false;
        }

        if (material.getPricePerUnit().compareTo(new BigDecimal("999999999")) > 0) {
            showErrorMessage("Giá quá lớn");
            return false;
        }

        // Validate số lượng
        if (material.getQuantity() != null && material.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
            showErrorMessage("Số lượng không được âm");
            return false;
        }

        // Validate ngưỡng cảnh báo
        if (material.getThreshold() != null && material.getThreshold().compareTo(BigDecimal.ZERO) < 0) {
            showErrorMessage("Ngưỡng cảnh báo không được âm");
            return false;
        }
        /*
        // Validate mô tả
        if (material.getDescription() != null && material.getDescription().length() > 500) {
            showErrorMessage("Mô tả không được vượt quá 500 ký tự");
            return false;
        }
        */

        // Kiểm tra trùng tên (chỉ validate ở client side, service sẽ kiểm tra lại)
        int excludeId = isNew ? 0 : material.getId();
        try {
            if (isNameExists(material.getName().trim(), excludeId)) {
                showErrorMessage("Tên nguyên liệu đã tồn tại");
                return false;
            }
        } catch (Exception e) {
            // Không block validation nếu không check được, service sẽ kiểm tra lại
            System.err.println("Warning: Could not check name existence: " + e.getMessage());
        }

        return true;
    }

    /**
     * Tạo nguyên liệu mới với validation
     */
    public boolean createWithValidation(String name, String unit, BigDecimal pricePerUnit, 
                                       BigDecimal threshold/*, String description*/) {
        Material material = new Material();
        material.setName(name);
        material.setUnit(unit);
        material.setPricePerUnit(pricePerUnit);
        material.setThreshold(threshold != null ? threshold : BigDecimal.valueOf(10.0));
        //material.setDescription(description);
        material.setQuantity(BigDecimal.ZERO); // Số lượng ban đầu = 0
        material.setActive(true); // Mặc định active

        if (!validateMaterial(material, true)) {
            return false;
        }

        return create(material);
    }

    /**
     * Cập nhật nguyên liệu với validation
     */
    public boolean updateWithValidation(int id, String name, String unit, BigDecimal pricePerUnit, 
                                        BigDecimal quantity, BigDecimal threshold, /*String description,*/ boolean isActive) {
        Material material = getById(id);
        if (material == null) {
            showErrorMessage("Không tìm thấy nguyên liệu cần cập nhật");
            return false;
        }

        material.setName(name);
        material.setUnit(unit);
        material.setPricePerUnit(pricePerUnit);
        material.setQuantity(quantity);
        material.setThreshold(threshold);
        //material.setDescription(description);
        material.setActive(isActive);

        if (!validateMaterial(material, false)) {
            return false;
        }

        return update(material);
    }

    /**
     * Format số lượng với đơn vị
     */
    public String formatQuantityWithUnit(BigDecimal quantity, String unit) {
        if (quantity == null) {
            return "0";
        }
        if (unit == null || unit.trim().isEmpty()) {
            return quantity.stripTrailingZeros().toPlainString();
        }
        return quantity.stripTrailingZeros().toPlainString() + " " + unit;
    }

    /**
     * Format giá tiền
     */
    public String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0 ₫";
        }
        return String.format("%,.0f ₫", price);
    }

    /**
     * Format giá trị tổng
     */
    public String formatTotalValue(Material material) {
        if (material == null || material.getQuantity() == null || material.getPricePerUnit() == null) {
            return "0 ₫";
        }
        BigDecimal totalValue = material.getTotalValue();
        return formatPrice(totalValue);
    }

    /**
     * Kiểm tra nguyên liệu có sắp hết không
     */
    public boolean isLowStock(Material material) {
        return material != null && material.isLowStock();
    }

    /**
     * Lấy trạng thái tồn kho
     */
    public String getStockStatus(Material material) {
        if (material == null) {
            return "Không xác định";
        }

        if (material.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            return "Hết hàng";
        } else if (material.isLowStock()) {
            return "Sắp hết";
        } else {
            return "Còn hàng";
        }
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
}