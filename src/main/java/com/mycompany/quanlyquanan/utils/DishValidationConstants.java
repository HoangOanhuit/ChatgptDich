/*
 * DishValidationConstants.java
 * Constants and enums for dish validation
 */
package com.mycompany.quanlyquanan.utils;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Constants and enums for dish validation
 * @author Administrator
 */
public final class DishValidationConstants {
    
    // Private constructor to prevent instantiation
    private DishValidationConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // ========== FIELD LENGTH CONSTRAINTS ==========
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final int MAX_SERVING_SIZE_LENGTH = 50;
    
    // ========== NUMERIC CONSTRAINTS ==========
    public static final BigDecimal MIN_PRICE = BigDecimal.ZERO;
    public static final BigDecimal MAX_PRICE = new BigDecimal("999999999.99");
    public static final int MAX_PRICE_DECIMAL_PLACES = 2;
    
    public static final int MIN_PREP_TIME = 0;
    public static final int MAX_PREP_TIME = 999; // minutes
    
    public static final int MIN_PORTION_COUNT = 1;
    public static final int MAX_PORTION_COUNT = 20;
    
    public static final int MIN_CALORIES = 0;
    public static final int MAX_CALORIES = 9999;
    
    public static final BigDecimal MIN_NUTRITION = BigDecimal.ZERO;
    public static final BigDecimal MAX_NUTRITION = new BigDecimal("999.9");
    public static final int MAX_NUTRITION_DECIMAL_PLACES = 1;
    
    // ========== FILE CONSTRAINTS ==========
    public static final long MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    );
    
    public static final int MAX_DIETARY_TAGS = 5;
    
    // ========== DIETARY TAGS ==========
    public static final Set<String> VALID_DIETARY_TAGS = Set.of(
        "Chay",           // Vegetarian
        "Thuần chay",     // Vegan
        "Không gluten",   // Gluten-free
        "Không lactose",  // Lactose-free
        "Ít muối",        // Low sodium
        "Ít đường",       // Low sugar
        "Ít béo",         // Low fat
        "Cay",            // Spicy
        "Không cay",      // Non-spicy
        "Halal",          // Halal
        "Kosher",         // Kosher
        "Organic",        // Organic
        "Địa phương",     // Local ingredients
        "Nhập khẩu",      // Imported ingredients
        "Đặc sản"         // Specialty dish
    );
    
    // ========== ERROR MESSAGES ==========
    public static final class ErrorMessages {
        // Name validation errors
        public static final String NAME_REQUIRED = "Tên món ăn không được để trống";
        public static final String NAME_TOO_SHORT = "Tên món ăn phải có ít nhất " + MIN_NAME_LENGTH + " ký tự";
        public static final String NAME_TOO_LONG = "Tên món ăn không được vượt quá " + MAX_NAME_LENGTH + " ký tự";
        public static final String NAME_INVALID_CHARS = "Tên món ăn không được chứa ký tự đặc biệt: < > \" ' &";
        public static final String NAME_ALREADY_EXISTS = "Tên món ăn '%s' đã tồn tại";
        
        // Category validation errors
        public static final String CATEGORY_REQUIRED = "Vui lòng chọn danh mục";
        public static final String CATEGORY_INVALID = "Danh mục được chọn không hợp lệ";
        public static final String NO_CATEGORIES_AVAILABLE = "Không có danh mục nào. Vui lòng tạo danh mục trước";
        
        // Price validation errors
        public static final String PRICE_REQUIRED = "Giá không được để trống";
        public static final String PRICE_INVALID = "Giá không hợp lệ. Vui lòng nhập số";
        public static final String PRICE_NEGATIVE = "Giá không được âm";
        public static final String PRICE_TOO_HIGH = "Giá không được vượt quá " + MAX_PRICE;
        public static final String PRICE_TOO_MANY_DECIMALS = "Giá chỉ được có tối đa " + MAX_PRICE_DECIMAL_PLACES + " chữ số thập phân";
        
        // Preparation time validation errors
        public static final String PREP_TIME_REQUIRED = "Thời gian chuẩn bị không được để trống";
        public static final String PREP_TIME_INVALID = "Thời gian chuẩn bị không hợp lệ. Vui lòng nhập số nguyên";
        public static final String PREP_TIME_NEGATIVE = "Thời gian chuẩn bị không được âm";
        public static final String PREP_TIME_TOO_HIGH = "Thời gian chuẩn bị không được vượt quá " + MAX_PREP_TIME + " phút";
        
        // Description validation errors
        public static final String DESCRIPTION_TOO_LONG = "Mô tả không được vượt quá " + MAX_DESCRIPTION_LENGTH + " ký tự";
        
        // Image validation errors
        public static final String IMAGE_NOT_EXISTS = "File hình ảnh không tồn tại";
        public static final String IMAGE_NOT_FILE = "Đường dẫn hình ảnh không phải là file";
        public static final String IMAGE_TOO_LARGE = "File hình ảnh không được vượt quá " + (MAX_IMAGE_SIZE_BYTES / 1024 / 1024) + "MB";
        public static final String IMAGE_INVALID_FORMAT = "File hình ảnh phải có định dạng: " + String.join(", ", ALLOWED_IMAGE_EXTENSIONS).toUpperCase();
        
        // Business logic errors
        public static final String COST_HIGHER_THAN_PRICE = "Cảnh báo: Giá vốn cao hơn giá bán. Bạn có chắc chắn muốn tiếp tục?";
        public static final String DISH_HAS_ACTIVE_ORDERS = "Không thể ngừng bán món ăn đang có trong đơn hàng chưa hoàn thành";
        
        // Nutritional info errors
        public static final String CALORIES_INVALID = "Calories không hợp lệ";
        public static final String CALORIES_OUT_OF_RANGE = "Calories phải từ " + MIN_CALORIES + " đến " + MAX_CALORIES;
        public static final String NUTRITION_INVALID = "Thông tin dinh dưỡng không hợp lệ";
        public static final String NUTRITION_OUT_OF_RANGE = "Thông tin dinh dưỡng phải từ " + MIN_NUTRITION + " đến " + MAX_NUTRITION + "g";
        
        // Serving info errors
        public static final String SERVING_SIZE_TOO_LONG = "Mô tả khẩu phần không được vượt quá " + MAX_SERVING_SIZE_LENGTH + " ký tự";
        public static final String PORTION_COUNT_INVALID = "Số khẩu phần không hợp lệ";
        public static final String PORTION_COUNT_OUT_OF_RANGE = "Số khẩu phần phải từ " + MIN_PORTION_COUNT + " đến " + MAX_PORTION_COUNT;
        
        // Dietary tags errors
        public static final String DIETARY_TAG_EMPTY = "Tag không được để trống";
        public static final String DIETARY_TAG_INVALID = "Tag '%s' không hợp lệ";
        public static final String TOO_MANY_DIETARY_TAGS = "Không được chọn quá " + MAX_DIETARY_TAGS + " tag";
        
        // Material validation errors
        public static final String MATERIAL_ID_INVALID = "ID nguyên liệu không hợp lệ";
        public static final String MATERIAL_QUANTITY_INVALID = "Số lượng nguyên liệu phải lớn hơn 0";
        public static final String MATERIAL_QUANTITY_TOO_MANY_DECIMALS = "Số lượng nguyên liệu chỉ được có tối đa 3 chữ số thập phân";
        
        // Private constructor to prevent instantiation
        private ErrorMessages() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
    }
    
    // ========== SUCCESS MESSAGES ==========
    public static final class SuccessMessages {
        public static final String DISH_CREATED = "Tạo món ăn thành công!";
        public static final String DISH_UPDATED = "Cập nhật món ăn thành công!";
        public static final String DISH_DELETED = "Xóa món ăn thành công!";
        public static final String DISH_STATUS_CHANGED = "Thay đổi trạng thái món ăn thành công!";
        public static final String IMAGE_UPLOADED = "Tải hình ảnh lên thành công!";
        
        // Private constructor to prevent instantiation
        private SuccessMessages() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
    }
    
    // ========== VALIDATION TYPES ==========
    public enum ValidationType {
        CREATE,     // For creating new dish
        UPDATE,     // For updating existing dish
        DELETE,     // For deleting dish
        STATUS_CHANGE // For changing dish status
    }
    
    // ========== FIELD TYPES FOR VALIDATION ==========
    public enum FieldType {
        NAME("Tên món ăn"),
        CATEGORY("Danh mục"),
        PRICE("Giá bán"),
        COST_PRICE("Giá vốn"),
        PREP_TIME("Thời gian chuẩn bị"),
        DESCRIPTION("Mô tả"),
        IMAGE("Hình ảnh"),
        SERVING_SIZE("Khẩu phần"),
        PORTION_COUNT("Số khẩu phần"),
        DIETARY_TAGS("Thẻ dinh dưỡng"),
        CALORIES("Calories"),
        PROTEIN("Protein"),
        CARBS("Carbohydrates"),
        FAT("Fat"),
        STATUS("Trạng thái");
        
        private final String displayName;
        
        FieldType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Get display text for file size
     * @param sizeInBytes Size in bytes
     * @return Human readable size string
     */
    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        }
        
        int exp = (int) (Math.log(sizeInBytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", sizeInBytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * Check if file extension is allowed for images
     * @param filename Filename to check
     * @return true if extension is allowed
     */
    public static boolean isAllowedImageExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        String lowerFilename = filename.toLowerCase();
        return ALLOWED_IMAGE_EXTENSIONS.stream()
            .anyMatch(lowerFilename::endsWith);
    }
    
    /**
     * Check if dietary tag is valid
     * @param tag Tag to validate
     * @return true if valid
     */
    public static boolean isValidDietaryTag(String tag) {
        return tag != null && VALID_DIETARY_TAGS.contains(tag.trim());
    }
    
    /**
     * Get all valid dietary tags as a comma-separated string
     * @return Comma-separated string of valid tags
     */
    public static String getValidDietaryTagsString() {
        return String.join(", ", VALID_DIETARY_TAGS);
    }
}