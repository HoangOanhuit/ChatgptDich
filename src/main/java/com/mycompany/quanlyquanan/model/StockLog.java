/*
 * Model class cho StockLog (Lịch sử xuất nhập kho)
 * Ghi lại mọi thay đổi về số lượng nguyên vật liệu trong kho
 */
package com.mycompany.quanlyquanan.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model class cho StockLog - Lịch sử xuất nhập kho
 * @author Admin
 */
public class StockLog {

    public void setPreviousQuantity(BigDecimal currentQuantity) {
        setQuantityBefore(currentQuantity);
    }

    public void setNewQuantity(BigDecimal newQuantity) {
        setQuantityAfter(newQuantity);
    }
    
    // Enum cho các loại thay đổi kho
    public enum ChangeType {
        IMPORT("Nhập kho"),
        CONSUME("Tiêu thụ"),
        ADJUST("Điều chỉnh"),
        EXPORT("Xuất kho"),
        RETURN("Hoàn trả"),
        WASTE("Hao hụt");
        
        private final String displayName;
        
        ChangeType(String displayName) {
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
    private int materialId;
    private ChangeType changeType;
    private BigDecimal quantityChange;
    private BigDecimal quantityBefore;
    private BigDecimal quantityAfter;
    private Integer referenceId; // order_id, import_id, adjust_id
    private String referenceType; // "order", "import", "adjust", "export"
    private String note;
    private int createdBy;
    private LocalDateTime createdAt;
    
    // Thông tin bổ sung để hiển thị
    private String materialName;
    private String materialUnit;
    private String createdByName;

    // Constructor không tham số
    public StockLog() {
        this.quantityChange = BigDecimal.ZERO;
        this.quantityBefore = BigDecimal.ZERO;
        this.quantityAfter = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor đầy đủ tham số
    public StockLog(int id, int materialId, ChangeType changeType, BigDecimal quantityChange,
                   BigDecimal quantityBefore, BigDecimal quantityAfter, Integer referenceId,
                   String referenceType, String note, int createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.materialId = materialId;
        this.changeType = changeType;
        this.quantityChange = quantityChange;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.note = note;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    // Constructor cho tạo mới (không có id)
    public StockLog(int materialId, ChangeType changeType, BigDecimal quantityChange,
                   BigDecimal quantityBefore, BigDecimal quantityAfter, int createdBy) {
        this.materialId = materialId;
        this.changeType = changeType;
        this.quantityChange = quantityChange;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor với reference
    public StockLog(int materialId, ChangeType changeType, BigDecimal quantityChange,
                   BigDecimal quantityBefore, BigDecimal quantityAfter, Integer referenceId,
                   String referenceType, String note, int createdBy) {
        this.materialId = materialId;
        this.changeType = changeType;
        this.quantityChange = quantityChange;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.note = note;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor với thông tin bổ sung
    public StockLog(int id, int materialId, ChangeType changeType, BigDecimal quantityChange,
                   LocalDateTime createdAt, String materialName, String materialUnit, 
                   String createdByName) {
        this.id = id;
        this.materialId = materialId;
        this.changeType = changeType;
        this.quantityChange = quantityChange;
        this.createdAt = createdAt;
        this.materialName = materialName;
        this.materialUnit = materialUnit;
        this.createdByName = createdByName;
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public BigDecimal getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(BigDecimal quantityChange) {
        this.quantityChange = quantityChange;
    }

    public BigDecimal getQuantityBefore() {
        return quantityBefore;
    }

    public void setQuantityBefore(BigDecimal quantityBefore) {
        this.quantityBefore = quantityBefore;
    }

    public BigDecimal getQuantityAfter() {
        return quantityAfter;
    }

    public void setQuantityAfter(BigDecimal quantityAfter) {
        this.quantityAfter = quantityAfter;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Thông tin bổ sung
    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialUnit() {
        return materialUnit;
    }

    public void setMaterialUnit(String materialUnit) {
        this.materialUnit = materialUnit;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    // Utility methods
    public String getQuantityChangeWithUnit() {
        if (materialUnit != null && quantityChange != null) {
            String sign = quantityChange.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
            return sign + quantityChange + " " + materialUnit;
        }
        return quantityChange != null ? quantityChange.toString() : "0";
    }

    public String getQuantityBeforeWithUnit() {
        if (materialUnit != null && quantityBefore != null) {
            return quantityBefore + " " + materialUnit;
        }
        return quantityBefore != null ? quantityBefore.toString() : "0";
    }

    public String getQuantityAfterWithUnit() {
        if (materialUnit != null && quantityAfter != null) {
            return quantityAfter + " " + materialUnit;
        }
        return quantityAfter != null ? quantityAfter.toString() : "0";
    }

    public String getChangeTypeDisplayName() {
        return changeType != null ? changeType.getDisplayName() : "Không xác định";
    }

    public String getReferenceInfo() {
        if (referenceId != null && referenceType != null) {
            switch (referenceType.toLowerCase()) {
                case "order":
                    return "Đơn hàng #" + referenceId;
                case "import":
                    return "Phiếu nhập #" + referenceId;
                case "adjust":
                    return "Điều chỉnh #" + referenceId;
                default:
                    return "Tham chiếu #" + referenceId;
            }
        }
        return "Không có tham chiếu";
    }

    public boolean isIncrease() {
        return quantityChange != null && quantityChange.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isDecrease() {
        return quantityChange != null && quantityChange.compareTo(BigDecimal.ZERO) < 0;
    }

    public String getLogSummary() {
        return String.format("%s: %s %s (%s → %s)", 
                getChangeTypeDisplayName(),
                materialName != null ? materialName : "N/A",
                getQuantityChangeWithUnit(),
                getQuantityBeforeWithUnit(),
                getQuantityAfterWithUnit());
    }

    // Validation methods
    public boolean isValid() {
        return materialId > 0 
            && changeType != null
            && quantityChange != null
            && quantityBefore != null && quantityBefore.compareTo(BigDecimal.ZERO) >= 0
            && quantityAfter != null && quantityAfter.compareTo(BigDecimal.ZERO) >= 0
            && createdBy > 0
            && createdAt != null;
    }

    public String getValidationError() {
        if (materialId <= 0) return "ID nguyên liệu không hợp lệ";
        if (changeType == null) return "Chưa xác định loại thay đổi";
        if (quantityChange == null) return "Chưa xác định số lượng thay đổi";
        if (quantityBefore == null || quantityBefore.compareTo(BigDecimal.ZERO) < 0) 
            return "Số lượng trước không hợp lệ";
        if (quantityAfter == null || quantityAfter.compareTo(BigDecimal.ZERO) < 0) 
            return "Số lượng sau không hợp lệ";
        if (createdBy <= 0) return "Chưa xác định người thực hiện";
        if (createdAt == null) return "Chưa có thời gian tạo";
        return null;
    }

    @Override
    public String toString() {
        return "StockLog{" +
                "id=" + id +
                ", materialId=" + materialId +
                ", changeType=" + changeType +
                ", quantityChange=" + quantityChange +
                ", quantityBefore=" + quantityBefore +
                ", quantityAfter=" + quantityAfter +
                ", referenceId=" + referenceId +
                ", referenceType='" + referenceType + '\'' +
                ", note='" + note + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", materialName='" + materialName + '\'' +
                ", materialUnit='" + materialUnit + '\'' +
                ", createdByName='" + createdByName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StockLog stockLog = (StockLog) obj;
        return id == stockLog.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}