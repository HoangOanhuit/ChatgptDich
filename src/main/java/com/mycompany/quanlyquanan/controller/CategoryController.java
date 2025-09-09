
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.controller;

import com.mycompany.quanlyquanan.model.Category;
import com.mycompany.quanlyquanan.service.CategoryService;

import javax.swing.JOptionPane;
import java.util.List;

/**
 *
 * @author Admin
 */
public class CategoryController {
    
    private final CategoryService service;

    public CategoryController() {
        this.service = new CategoryService();
    }

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    /**
     * Lấy tất cả danh mục
     */
    public List<Category> getAllCategories() {
        try {
            return service.getAllCategories();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải danh sách danh mục: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy tất cả danh mục đang hoạt động
     */
    public List<Category> getAllActive() {
        try {
            return service.getAllActiveCategories();
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải danh sách danh mục: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy danh mục theo ID
     */
    public Category getById(int id) {
        try {
            return service.getCategoryById(id);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return null;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tải thông tin danh mục: " + e.getMessage());
            return null;
        }
    }

    /**
     * Tạo danh mục mới
     */
    public boolean create(Category category) {
        try {
            boolean result = service.createCategory(category);
/*            if (result) {
                showSuccessMessage("Tạo danh mục thành công!");
            } else {
*/
            if (!result) {
                showErrorMessage("Không thể tạo danh mục. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tạo danh mục: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật danh mục
     */
    public boolean update(Category category) {
        try {
            boolean result = service.updateCategory(category);
/*            if (result) {
                showSuccessMessage("Cập nhật danh mục thành công!");
            } else {
*/
            if (!result) {
                showErrorMessage("Không thể cập nhật danh mục. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi cập nhật danh mục: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa danh mục (soft delete)
     */
    public boolean delete(int id) {
        try {
            // Xác nhận trước khi xóa
            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc chắn muốn xóa danh mục này?\nDanh mục sẽ được đánh dấu là không hoạt động.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = service.deleteCategory(id);
/*            if (result) {
                showSuccessMessage("Xóa danh mục thành công!");
            } else {
*/
            if (!result) {
                showErrorMessage("Không thể xóa danh mục. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi xóa danh mục: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa vĩnh viễn danh mục (hard delete)
     */
    public boolean permanentDelete(int id) {
        try {
            // Xác nhận mạnh mẽ hơn cho permanent delete
            int confirm = JOptionPane.showConfirmDialog(
                null,
                "⚠️ CẢNH BÁO: Bạn có chắc chắn muốn XÓA VĨNH VIỄN danh mục này?\n" +
                "Hành động này KHÔNG THỂ HOÀN TÁC!\n\n" +
                "Nhấn YES để xóa vĩnh viễn, NO để hủy bỏ.",
                "⚠️ Xác nhận xóa vĩnh viễn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = service.permanentDeleteCategory(id);
/*            if (result) {
                showSuccessMessage("Xóa vĩnh viễn danh mục thành công!");
            } else {
*/
            if (!result) {
                showErrorMessage("Không thể xóa vĩnh viễn danh mục. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi xóa vĩnh viễn danh mục: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tìm kiếm danh mục
     */
    public List<Category> search(String keyword) {
        try {
            return service.searchCategories(keyword);
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tìm kiếm danh mục: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Kích hoạt/vô hiệu hóa danh mục
     */
    public boolean toggleStatus(int id) {
        try {
            Category category = service.getCategoryById(id);
            if (category == null) {
                showErrorMessage("Không tìm thấy danh mục");
                return false;
            }

            String action = category.isActive() ? "vô hiệu hóa" : "kích hoạt";
            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc chắn muốn " + action + " danh mục '" + category.getName() + "'?",
                "Xác nhận " + action,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean result = service.toggleCategoryStatus(id);
/*            
            if (result) {
                showSuccessMessage("Thay đổi trạng thái danh mục thành công!");
            } else {
*/
            if (!result) {
                showErrorMessage("Không thể thay đổi trạng thái danh mục. Vui lòng thử lại.");
            }
            return result;
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            showErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi thay đổi trạng thái danh mục: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh mục theo tên
     */
    public Category getByName(String name) {
        try {
            return service.getCategoryByName(name);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return null;
        } catch (Exception e) {
            showErrorMessage("Lỗi khi tìm danh mục theo tên: " + e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra tên danh mục đã tồn tại chưa
     */
    public boolean isNameExists(String name, int excludeId) {
        try {
            return service.isCategoryNameExists(name, excludeId);
        } catch (Exception e) {
            showErrorMessage("Lỗi khi kiểm tra tên danh mục: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra danh mục có thể xóa được không
     */
    public boolean canDelete(int id) {
        try {
            return service.canDeleteCategory(id);
        } catch (Exception e) {
            showErrorMessage("Lỗi khi kiểm tra khả năng xóa danh mục: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate dữ liệu trước khi submit
     */
    public boolean validateCategory(Category category, boolean isNew) {
        if (category == null) {
            showErrorMessage("Thông tin danh mục không hợp lệ");
            return false;
        }

        // Validate tên
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            showErrorMessage("Tên danh mục không được để trống");
            return false;
        }

        if (category.getName().trim().length() > 100) {
            showErrorMessage("Tên danh mục không được vượt quá 100 ký tự");
            return false;
        }

        // Validate mô tả
        if (category.getDescription() != null && category.getDescription().length() > 500) {
            showErrorMessage("Mô tả không được vượt quá 500 ký tự");
            return false;
        }

        // Kiểm tra trùng tên
        int excludeId = isNew ? 0 : category.getId();
        if (isNameExists(category.getName().trim(), excludeId)) {
            showErrorMessage("Tên danh mục đã tồn tại");
            return false;
        }

        return true;
    }

    /**
     * Tạo danh mục mới với validation
     */
    public boolean createWithValidation(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        if (!validateCategory(category, true)) {
            return false;
        }

        return create(category);
    }

    /**
     * Cập nhật danh mục với validation
     */
    public boolean updateWithValidation(int id, String name, String description, boolean isActive) {
        Category category = getById(id);
        if (category == null) {
            return false;
        }

        category.setName(name);
        category.setDescription(description);
        category.setActive(isActive);

        if (!validateCategory(category, false)) {
            return false;
        }

        return update(category);
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