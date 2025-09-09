/*
 * Model class cho ExpenseReceipt (Phiếu chi)
 * Quản lý các khoản chi phí của nhà hàng
 */
package com.mycompany.quanlyquanan.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model class cho ExpenseReceipt - Phiếu chi
 * @author Admin
 */
public class ExpenseReceipt {

    public void setSupplier(String supplier) {
        this.vendor = supplier;
        this.updatedAt = LocalDateTime.now();
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.receiptNumber = invoiceNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public void setNotes(String notes) {
        this.description = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void setExpenseDate(LocalDateTime expenseDate) {
        this.receiptDate = expenseDate;
        this.updatedAt = LocalDateTime.now();
    }

        public void setExpenseType(String expenseType) {
        if (expenseType != null) {
            try {
                this.category = ExpenseCategory.valueOf(expenseType.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.category = ExpenseCategory.OTHER;
            }
        } else {
            this.category = null;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    // Enum cho các loại chi phí
    public enum ExpenseCategory {
        MATERIAL_IMPORT("Nhập nguyên liệu"),
        SALARY("Lương nhân viên"),
        UTILITIES("Điện nước"),
        MAINTENANCE("Bảo trì sửa chữa"),
        RENT("Tiền thuê mặt bằng"),
        MARKETING("Marketing quảng cáo"),
        EQUIPMENT("Thiết bị dụng cụ"),
        OTHER("Chi phí khác");
        
        private final String displayName;
        
        ExpenseCategory(String displayName) {
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
    
    private int id;
    private ExpenseCategory category;
    private BigDecimal amount;
    private Integer relatedImportId; // Liên kết với material_imports nếu là chi phí nhập hàng
    private String description;
    private LocalDateTime receiptDate;
    private int createdBy;
    private String receiptNumber; // Số phiếu chi (có thể tự generate)
    private String vendor; // Nhà cung cấp/đối tác
    private String paymentMethod; // Tiền mặt, chuyển khoản, etc.
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Thông tin bổ sung để hiển thị
    private String createdByName;
    private String relatedMaterialName;

    // Constructor không tham số
    public ExpenseReceipt() {
        this.amount = BigDecimal.ZERO;
        this.receiptDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.paymentMethod = "Tiền mặt";
    }

    // Constructor đầy đủ tham số
    public ExpenseReceipt(int id, ExpenseCategory category, BigDecimal amount, Integer relatedImportId,
                         String description, LocalDateTime receiptDate, int createdBy,
                         String receiptNumber, String vendor, String paymentMethod,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.relatedImportId = relatedImportId;
        this.description = description;
        this.receiptDate = receiptDate;
        this.createdBy = createdBy;
        this.receiptNumber = receiptNumber;
        this.vendor = vendor;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor cho tạo mới (không có id)
    public ExpenseReceipt(ExpenseCategory category, BigDecimal amount, String description,
                         int createdBy, String vendor) {
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.createdBy = createdBy;
        this.vendor = vendor;
        this.receiptDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.paymentMethod = "Tiền mặt";
    }

    // Constructor cho chi phí nhập hàng
    public ExpenseReceipt(ExpenseCategory category, BigDecimal amount, int relatedImportId,
                         String description, int createdBy, String vendor) {
        this.category = category;
        this.amount = amount;
        this.relatedImportId = relatedImportId;
        this.description = description;
        this.createdBy = createdBy;
        this.vendor = vendor;
        this.receiptDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.paymentMethod = "Tiền mặt";
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getRelatedImportId() {
        return relatedImportId;
    }

    public void setRelatedImportId(Integer relatedImportId) {
        this.relatedImportId = relatedImportId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(LocalDateTime receiptDate) {
        this.receiptDate = receiptDate;
        this.updatedAt = LocalDateTime.now();
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
        this.updatedAt = LocalDateTime.now();
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Thông tin bổ sung
    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getRelatedMaterialName() {
        return relatedMaterialName;
    }

    public void setRelatedMaterialName(String relatedMaterialName) {
        this.relatedMaterialName = relatedMaterialName;
    }

    // Utility methods
    public String getFormattedAmount() {
        if (amount != null) {
            return String.format("%,.0f VNĐ", amount);
        }
        return "0 VNĐ";
    }

    public String getCategoryDisplayName() {
        return category != null ? category.getDisplayName() : "Không xác định";
    }

    public String getReceiptSummary() {
        return String.format("%s - %s - %s", 
                receiptNumber != null ? receiptNumber : "N/A",
                getCategoryDisplayName(),
                getFormattedAmount());
    }

    public boolean isRelatedToImport() {
        return relatedImportId != null && relatedImportId > 0;
    }

    public String getVendorOrSupplier() {
        return vendor != null && !vendor.trim().isEmpty() ? vendor : "Không rõ nhà cung cấp";
    }

    // Auto-generate receipt number if not set
    public void generateReceiptNumber() {
        if (receiptNumber == null || receiptNumber.trim().isEmpty()) {
            String prefix = "PC"; // Phiếu Chi
            String timestamp = String.valueOf(System.currentTimeMillis() % 1000000);
            this.receiptNumber = prefix + timestamp;
        }
    }

    // Validation methods
    public boolean isValid() {
        return category != null
            && amount != null && amount.compareTo(BigDecimal.ZERO) > 0
            && receiptDate != null
            && createdBy > 0
            && description != null && !description.trim().isEmpty();
    }

    public String getValidationError() {
        if (category == null) return "Chưa chọn loại chi phí";
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) 
            return "Số tiền chi phải lớn hơn 0";
        if (receiptDate == null) return "Chưa chọn ngày chi";
        if (createdBy <= 0) return "Chưa xác định người tạo phiếu";
        if (description == null || description.trim().isEmpty()) 
            return "Mô tả chi phí không được để trống";
        return null;
    }

    // Static helper methods
    public static ExpenseReceipt createMaterialImportExpense(int importId, BigDecimal amount, 
                                                           String materialName, String supplierName, 
                                                           int createdBy) {
        ExpenseReceipt expense = new ExpenseReceipt();
        expense.setCategory(ExpenseCategory.MATERIAL_IMPORT);
        expense.setAmount(amount);
        expense.setRelatedImportId(importId);
        expense.setDescription("Nhập " + materialName + " từ " + supplierName);
        expense.setVendor(supplierName);
        expense.setCreatedBy(createdBy);
        expense.generateReceiptNumber();
        return expense;
    }

    public static ExpenseReceipt createSalaryExpense(BigDecimal amount, String employeeName, 
                                                   String period, int createdBy) {
        ExpenseReceipt expense = new ExpenseReceipt();
        expense.setCategory(ExpenseCategory.SALARY);
        expense.setAmount(amount);
        expense.setDescription("Lương nhân viên " + employeeName + " - " + period);
        expense.setCreatedBy(createdBy);
        expense.generateReceiptNumber();
        return expense;
    }

    public static ExpenseReceipt createUtilityExpense(BigDecimal amount, String utilityType, 
                                                    String period, int createdBy) {
        ExpenseReceipt expense = new ExpenseReceipt();
        expense.setCategory(ExpenseCategory.UTILITIES);
        expense.setAmount(amount);
        expense.setDescription("Chi phí " + utilityType + " - " + period);
        expense.setCreatedBy(createdBy);
        expense.generateReceiptNumber();
        return expense;
    }

    @Override
    public String toString() {
        return "ExpenseReceipt{" +
                "id=" + id +
                ", category=" + category +
                ", amount=" + amount +
                ", relatedImportId=" + relatedImportId +
                ", description='" + description + '\'' +
                ", receiptDate=" + receiptDate +
                ", createdBy=" + createdBy +
                ", receiptNumber='" + receiptNumber + '\'' +
                ", vendor='" + vendor + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdByName='" + createdByName + '\'' +
                ", relatedMaterialName='" + relatedMaterialName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExpenseReceipt that = (ExpenseReceipt) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}