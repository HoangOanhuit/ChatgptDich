package com.mycompany.quanlytruyen.model;

/**
 * Enum for chapter translation status
 */
public enum ChapterStatus {
    PENDING("PENDING"),
    DONE("DONE"), 
    ERROR("ERROR");
    
    private final String value;
    
    ChapterStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    public static ChapterStatus fromString(String value) {
        for (ChapterStatus status : ChapterStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}