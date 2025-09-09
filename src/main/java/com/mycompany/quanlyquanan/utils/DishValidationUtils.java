/*
 * DishValidationUtils.java
 * Utility class for dish input validation
 */
package com.mycompany.quanlyquanan.utils;

import java.math.BigDecimal;

/**
 * Utility class for validating dish input data
 * @author Administrator
 */
public class DishValidationUtils {
    
    // Constants for validation
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final BigDecimal MAX_PRICE = new BigDecimal("999999999.99");
    public static final int MAX_PREP_TIME = 999; // minutes
    
    /**
     * Validate category selection
     * @param categoryIndex Selected category index
     * @param categoriesCount Total number of categories
     * @return Error message if invalid, null if valid
     */
    public static String validateCategory(int categoryIndex, int categoriesCount) {
        if (categoryIndex < 0) {
            return "Vui lòng chọn danh mục";
        }
        
        if (categoriesCount == 0) {
            return "Không có danh mục nào. Vui lòng tạo danh mục trước";
        }
        
        if (categoryIndex >= categoriesCount) {
            return "Danh mục được chọn không hợp lệ";
        }
        
        return null; // Valid
    }
    /**
     * Validate image file
     * @param imageFile Image file to validate
     * @return Error message if invalid, null if valid
     */
    public static String validateImageFile(java.io.File imageFile) {
        if (imageFile == null) {
            // Image is optional
            return null;
        }
        
        if (!imageFile.exists()) {
            return "File hình ảnh không tồn tại";
        }
        
        if (!imageFile.isFile()) {
            return "Đường dẫn hình ảnh không phải là file";
        }
        
        // Check file size (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB in bytes
        if (imageFile.length() > maxSize) {
            return "File hình ảnh không được vượt quá 10MB";
        }
        
        // Check file extension
        String fileName = imageFile.getName().toLowerCase();
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
        boolean isValidExtension = false;
        
        for (String ext : allowedExtensions) {
            if (fileName.endsWith(ext)) {
                isValidExtension = true;
                break;
            }
        }
        
        if (!isValidExtension) {
            return "File hình ảnh phải có định dạng: JPG, JPEG, PNG, GIF hoặc BMP";
        }
        
        return null; // Valid
    }
        /**
     * Validate dish name
     * @param name Dish name to validate
     * @return Error message if invalid, null if valid
     */
    public static String validateDishName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Tên món ăn không được để trống";
        }
        
        String trimmedName = name.trim();
        if (trimmedName.length() < MIN_NAME_LENGTH) {
            return "Tên món ăn phải có ít nhất " + MIN_NAME_LENGTH + " ký tự";
        }
        
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            return "Tên món ăn không được vượt quá " + MAX_NAME_LENGTH + " ký tự";
        }
        
        // Check for invalid characters
        if (trimmedName.matches(".*[<>\"'&].*")) {
            return "Tên món ăn không được chứa ký tự đặc biệt: < > \" ' &";
        }
        
        return null; // Valid
    }
    /**
     * Comprehensive validation for all dish fields
     * @param name Dish name
     * @param categoryIndex Selected category index
     * @param categoriesCount Total categories count
     * @param priceStr Selling price string
     * @param costPriceStr Cost price string
     * @param prepTimeStr Preparation time string
     * @param description Description
     * @param imageFile Image file (can be null)
     * @return First error message found, null if all valid
     */
    public static String validateAllFields(String name, int categoryIndex, int categoriesCount,
            String priceStr, String costPriceStr, String prepTimeStr, 
            String description, java.io.File imageFile) {
        
        String error;
        
        // Validate required fields first
        error = validateDishName(name);
        if (error != null) return error;
        
        error = validateCategory(categoryIndex, categoriesCount);
        if (error != null) return error;
        
        error = validatePrice(priceStr);
        if (error != null) return error;
        
        error = validatePrepTime(prepTimeStr);
        if (error != null) return error;
        
        // Validate optional fields
        error = validateCostPrice(costPriceStr);
        if (error != null) return error;
        
        error = validateDescription(description);
        if (error != null) return error;
        
        error = validateImageFile(imageFile);
        if (error != null) return error;
        
        // Validate business logic
        error = validatePriceLogic(priceStr, costPriceStr);
        if (error != null) return error;
        
        return null; // All valid
    }
    /**
     * Validate description
     * @param description Description to validate
     * @return Error message if invalid, null if valid
     */
    public static String validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            // Description is optional
            return null;
        }
        
        String trimmedDesc = description.trim();
        if (trimmedDesc.length() > MAX_DESCRIPTION_LENGTH) {
            return "Mô tả không được vượt quá " + MAX_DESCRIPTION_LENGTH + " ký tự";
        }
        
        return null; // Valid
    }
    /**
     * Comprehensive validation for dish creation (overloaded method)
     * @param dish Dish object to validate
     * @param categoriesCount Total categories count
     * @param imageFile Image file (can be null)
     * @return First error message found, null if all valid
     */
    public static String validateAllFields(com.mycompany.quanlyquanan.model.Dish dish, 
                                         int categoriesCount, java.io.File imageFile) {
        if (dish == null) {
            return "Đối tượng món ăn không được null";
        }
        
        return validateAllFields(
            dish.getName(),
            dish.getCategoryId() - 1, // Convert to index (assuming 1-based ID)
            categoriesCount,
            dish.getPrice() != null ? dish.getPrice().toString() : "",
            dish.getCostPrice() != null ? dish.getCostPrice().toString() : "",
            String.valueOf(dish.getPrepTime()),
            dish.getDescription(),
            imageFile
        );
    }
    /**
     * Validate price logic (selling price should be higher than cost price)
     * @param sellingPriceStr Selling price string
     * @param costPriceStr Cost price string
     * @return Error message if invalid, null if valid
     */
    public static String validatePriceLogic(String sellingPriceStr, String costPriceStr) {
        if (sellingPriceStr == null || sellingPriceStr.trim().isEmpty() ||
            costPriceStr == null || costPriceStr.trim().isEmpty()) {
            // Can't validate logic if either price is missing
            return null;
        }
        
        try {
            BigDecimal sellingPrice = new BigDecimal(sellingPriceStr.trim());
            BigDecimal costPrice = new BigDecimal(costPriceStr.trim());
            
            if (costPrice.compareTo(sellingPrice) > 0) {
                return "Cảnh báo: Giá vốn cao hơn giá bán. Bạn có chắc chắn muốn tiếp tục?";
            }
            
        } catch (NumberFormatException e) {
            // If either price is invalid, let individual validation catch it
            return null;
        }
        
        return null; // Valid
    }
    /**
     * Quick validation for required fields only
     * @param name Dish name
     * @param categoryIndex Selected category index
     * @param categoriesCount Total categories count
     * @param priceStr Selling price string
     * @param prepTimeStr Preparation time string
     * @return First error message found, null if all valid
     */
    public static String validateRequiredFields(String name, int categoryIndex, int categoriesCount,
                                              String priceStr, String prepTimeStr) {
        String error;
        
        error = validateDishName(name);
        if (error != null) return error;
        
        error = validateCategory(categoryIndex, categoriesCount);
        if (error != null) return error;
        
        error = validatePrice(priceStr);
        if (error != null) return error;
        
        error = validatePrepTime(prepTimeStr);
        if (error != null) return error;
        
        return null; // All required fields valid
    }
        /**
     * Validate preparation time
     * @param prepTimeStr Preparation time string to validate
     * @return Error message if invalid, null if valid
     */
    public static String validatePrepTime(String prepTimeStr) {
        if (prepTimeStr == null || prepTimeStr.trim().isEmpty()) {
            return "Thời gian chuẩn bị không được để trống";
        }
        return null;
    }
    /**
     * Validate fields with context (for different operations)
     * @param name Dish name
     * @param categoryIndex Selected category index
     * @param categoriesCount Total categories count
     * @param priceStr Selling price string
     * @param costPriceStr Cost price string
     * @param prepTimeStr Preparation time string
     * @param description Description
     * @param imageFile Image file (can be null)
     * @param validationType Type of validation (CREATE, UPDATE, etc.)
     * @return First error message found, null if all valid
     */
    /*
    public static String validateAllFieldsWithContext(String name, int categoryIndex, int categoriesCount,
            String priceStr, String costPriceStr, String prepTimeStr, String description, 
            java.io.File imageFile, DishValidationConstants.ValidationType validationType) {
        
        // Basic validation first
        String error = validateAllFields(name, categoryIndex, categoriesCount, 
                                       priceStr, costPriceStr, prepTimeStr, description, imageFile);
        if (error != null) return error;
        
        // Context-specific validation
        switch (validationType) {
            case CREATE:
                // For create operation, all validations already covered
                break;
                
            case UPDATE:
                // For update operation, might have additional checks
                // e.g., check if dish exists, etc.
                break;
                
            case DELETE:
                // For delete operation, minimal validation needed
                return validateDishName(name);
                
            case STATUS_CHANGE:
                // For status change, only name validation needed
                return validateDishName(name);
                
            default:
                break;
        }
        
        return null; // All valid for context
    }
    */
    
    
    

    
    /**
     * Validate price
     * @param priceStr Price string to validate
     * @return Error message if invalid, null if valid
     */
    public static String validatePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return "Giá không được để trống";
        }
        
        try {
            BigDecimal price = new BigDecimal(priceStr.trim());
            
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                return "Giá không được âm";
            }
            
            if (price.compareTo(MAX_PRICE) > 0) {
                return "Giá không được vượt quá " + MAX_PRICE;
            }
            
            // Check decimal places (max 2)
            if (price.scale() > 2) {
                return "Giá chỉ được có tối đa 2 chữ số thập phân";
            }
            
        } catch (NumberFormatException e) {
            return "Giá không hợp lệ. Vui lòng nhập số";
        }
        
        return null; // Valid
    }
    
    /**
     * Validate cost price
     * @param costPriceStr Cost price string to validate
     * @return Error message if invalid, null if valid
     */
    public static String validateCostPrice(String costPriceStr) {
        if (costPriceStr == null || costPriceStr.trim().isEmpty()) {
            // Cost price is optional
            return null;
        }
        
        return validatePrice(costPriceStr); // Same validation as regular price
    }
    
}
