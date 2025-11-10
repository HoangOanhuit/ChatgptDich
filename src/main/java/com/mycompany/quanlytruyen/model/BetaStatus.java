package com.mycompany.quanlytruyen.model;

/**
 * Enum cho trạng thái beta của chương
 */
public enum BetaStatus {
    NOT_BETA("Chưa beta", "not_beta"),
    DONE_BETA("Đã beta", "done_beta"), 
    DONE_POST("Đã đăng", "done_post");
    
    private final String displayName;
    private final String dbValue;
    
    BetaStatus(String displayName, String dbValue) {
        this.displayName = displayName;
        this.dbValue = dbValue;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDbValue() {
        return dbValue;
    }
    
    /**
     * Convert from database string value to enum
     */
    public static BetaStatus fromDbValue(String dbValue) {
        if (dbValue == null) {
            return NOT_BETA; // Default value
        }
        
        for (BetaStatus status : values()) {
            if (status.dbValue.equals(dbValue)) {
                return status;
            }
        }
        
        return NOT_BETA; // Default fallback
    }
    
    /**
     * Get all display names as array (for ComboBox)
     */
    public static String[] getDisplayNames() {
        BetaStatus[] statuses = values();
        String[] names = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            names[i] = statuses[i].displayName;
        }
        return names;
    }
    /**
     * Convert from display name to enum
     */
    public static BetaStatus fromDisplayName(String displayName) {
        if (displayName == null) {
            return NOT_BETA;
        }
        for (BetaStatus status : values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        return NOT_BETA;
    }    
    @Override
    public String toString() {
        return displayName;
    }
}