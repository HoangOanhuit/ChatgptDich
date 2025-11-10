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
    
    private Long accountId;              // ✨ MỚI - ID account đăng truyện
    private String shortTitle;           // ✨ MỚI - Tên viết tắt
    private Integer posted;              // ✨ MỚI - Chương cuối đã đăng
    private BigDecimal price; 
    
    // Translation metadata
    private String guidelines;
    private String nameTable;
    private String modelUsed;
    private LocalDateTime lastResumedAt;

    // Statistics (computed fields, not in database)
    private Integer totalChapters;
    private Integer completedChapters;
    private Integer pendingChapters;
    private Integer errorChapters;
    private Double completionPercentage;    

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
        this.posted = 0;
    }
    
    public Book(String title) {
        this();
        this.title = title;
    }

    /**
     * Constructor đầy đủ
     */
    public Book(Long id, String title, String author, String slug, 
                String guidelines, String nameTable, String modelUsed,
                Long accountId, String shortTitle, Integer posted, BigDecimal price) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.slug = slug;
        this.guidelines = guidelines;
        this.nameTable = nameTable;
        this.modelUsed = modelUsed;
        this.accountId = accountId;
        this.shortTitle = shortTitle;
        this.posted = posted != null ? posted : 0;
        this.price = price != null ? price : BigDecimal.ZERO;
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

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    public Integer getPosted() {
        return posted;
    }

    public void setPosted(Integer posted) {
        this.posted = posted;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }   
// STATISTICS FIELDS
    
    public Integer getTotalChapters() {
        return totalChapters;
    }

    public void setTotalChapters(Integer totalChapters) {
        this.totalChapters = totalChapters;
    }

    public Integer getCompletedChapters() {
        return completedChapters;
    }

    public void setCompletedChapters(Integer completedChapters) {
        this.completedChapters = completedChapters;
    }

    public Integer getPendingChapters() {
        return pendingChapters;
    }

    public void setPendingChapters(Integer pendingChapters) {
        this.pendingChapters = pendingChapters;
    }

    public Integer getErrorChapters() {
        return errorChapters;
    }

    public void setErrorChapters(Integer errorChapters) {
        this.errorChapters = errorChapters;
    }

    public Double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(Double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }    
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
    /**
     * Kiểm tra xem book có được gán cho account nào chưa
     * @return true nếu đã gán account
     */
    public boolean hasAccount() {
        return accountId != null;
    }
    
    /**
     * Kiểm tra xem đã đăng chương nào chưa
     * @return true nếu đã đăng ít nhất 1 chương
     */
    public boolean hasPostedChapters() {
        return posted != null && posted > 0;
    }
    
    /**
     * Kiểm tra xem có giá chưa
     * @return true nếu đã set giá
     */
    public boolean hasPrice() {
        return price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Lấy tên hiển thị (ưu tiên short_title, fallback về title)
     * @return Tên hiển thị
     */
    public String getDisplayName() {
        if (shortTitle != null && !shortTitle.trim().isEmpty()) {
            return shortTitle;
        }
        return title;
    }
    
    /**
     * Format giá tiền theo định dạng VNĐ
     * @return Chuỗi giá đã format (ví dụ: "15,000 VNĐ")
     */
    public String getFormattedPrice() {
        if (price == null) {
            return "0 VNĐ";
        }
        return String.format("%,.0f VNĐ", price);
    }
 
    /**
     * Lấy trạng thái hoàn thành dịch dưới dạng phần trăm
     * @return Phần trăm hoàn thành
     */
    public String getCompletionStatus() {
        if (totalChapters == null || totalChapters == 0) {
            return "0%";
        }
        if (completedChapters == null) {
            return "0%";
        }
        double percentage = (completedChapters * 100.0) / totalChapters;
        return String.format("%.1f%%", percentage);
    }
    
    /**
     * Kiểm tra xem đã dịch xong chưa
     * @return true nếu tất cả chương đã dịch xong
     */
    public boolean isTranslationComplete() {
        return "hoan".equals(translateStatus) ||
               (totalChapters != null && completedChapters != null && 
                totalChapters.equals(completedChapters));
    }
    
    /**
     * Kiểm tra xem đã đăng xong chưa
     * @return true nếu tất cả chương đã đăng
     */
    public boolean isPostingComplete() {
        return "hoan".equals(postStatus) ||
               (totalChapters != null && posted != null && 
                totalChapters.equals(posted));
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