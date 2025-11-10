package com.mycompany.quanlytruyen.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Enhanced Book model with new status fields and revenue tracking
 */
public class Book {
    private Long id;
    private String title;
    private String author;
    private String slug;
    private LocalDateTime createdAt;
    
    // New fields for book managementHoàn
    private RawStatus rawStatus;
    private TranslateStatus translateStatus; 
    private PostStatus postStatus;
    private BigDecimal totalRevenue;
    
    // Translation metadata
    private String guidelines;
    private String nameTable;
    private String modelUsed;
    private LocalDateTime lastResumedAt;

    // Enums for status fields
    public enum RawStatus {
        FULL("full", "Hoàn"),
        NOT_FULL("not_full", "Chưa Hoàn");
        
        private final String value;
        private final String displayName;
        
        RawStatus(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
        
        public static RawStatus fromString(String value) {
            for (RawStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            return NOT_FULL; // default
        }
    }
    
    public enum TranslateStatus {
        HOAN("hoan", "Dịch Hoàn thành"),
        NOT_HOAN("not_hoan", "Dịch Chưa hoàn thành");
        
        private final String value;
        private final String displayName;
        
        TranslateStatus(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
        
        public static TranslateStatus fromString(String value) {
            for (TranslateStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            return NOT_HOAN; // default
        }
    }
    
    public enum PostStatus {
        HOAN("hoan", "Post Hoàn"),
        NOT_HOAN("not_hoan", "Post Chưa Hoàn");
        
        private final String value;
        private final String displayName;
        
        PostStatus(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
        
        public static PostStatus fromString(String value) {
            for (PostStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            return NOT_HOAN; // default
        }
    }

    // Constructors
    public Book() {
        this.rawStatus = RawStatus.NOT_FULL;
        this.translateStatus = TranslateStatus.NOT_HOAN;
        this.postStatus = PostStatus.NOT_HOAN;
        this.totalRevenue = BigDecimal.ZERO;
    }
    
    public Book(String title) {
        this();
        this.title = title;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public RawStatus getRawStatus() { return rawStatus; }
    public void setRawStatus(RawStatus rawStatus) { this.rawStatus = rawStatus; }

    public TranslateStatus getTranslateStatus() { return translateStatus; }
    public void setTranslateStatus(TranslateStatus translateStatus) { this.translateStatus = translateStatus; }

    public PostStatus getPostStatus() { return postStatus; }
    public void setPostStatus(PostStatus postStatus) { this.postStatus = postStatus; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { 
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO; 
    }

    public String getGuidelines() { return guidelines; }
    public void setGuidelines(String guidelines) { this.guidelines = guidelines; }

    public String getNameTable() { return nameTable; }
    public void setNameTable(String nameTable) { this.nameTable = nameTable; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    public LocalDateTime getLastResumedAt() { return lastResumedAt; }
    public void setLastResumedAt(LocalDateTime lastResumedAt) { this.lastResumedAt = lastResumedAt; }

    // Utility methods
    public String getFormattedRevenue() {
        if (totalRevenue == null) return "0 VNĐ";
        return String.format("%,d VNĐ", totalRevenue.longValue());
    }
    
    public void addRevenue(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (this.totalRevenue == null) {
                this.totalRevenue = BigDecimal.ZERO;
            }
            this.totalRevenue = this.totalRevenue.add(amount);
        }
    }
    
    public boolean isCompletelyFinished() {
        return rawStatus == RawStatus.FULL && 
               translateStatus == TranslateStatus.HOAN && 
               postStatus == PostStatus.HOAN;
    }
    
    public double getCompletionScore() {
        double score = 0;
        if (rawStatus == RawStatus.FULL) score += 0.3;
        if (translateStatus == TranslateStatus.HOAN) score += 0.4;
        if (postStatus == PostStatus.HOAN) score += 0.3;
        return score;
    }
    
    public String getOverallStatus() {
        if (isCompletelyFinished()) {
            return "Hoàn thành";
        } else if (rawStatus == RawStatus.FULL && translateStatus == TranslateStatus.HOAN) {
            return "Chờ đăng";
        } else if (rawStatus == RawStatus.FULL) {
            return "Đang dịch";
        } else {
            return "Chưa hoàn thiện";
        }
    }

    @Override
    public String toString() {
        return String.format("Book{id=%d, title='%s', overall=%s, revenue=%s}", 
            id, title, getOverallStatus(), getFormattedRevenue());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Book book = (Book) obj;
        return id != null && id.equals(book.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}