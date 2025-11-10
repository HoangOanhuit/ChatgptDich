package com.mycompany.quanlytruyen.model;

import java.time.LocalDateTime;

/**
 * Chapter model với BetaStatus
 * @author trang
 */
public class Chapter {
    private Long id;
    private Long bookId;
    private Integer chapterNumber;
    private String chapterTitle;
    private String sourceFilename;
    private String content;
    private String sourceContent;
    private Integer wordCount;
    private ChapterStatus status;
    private BetaStatus betaStatus; // Thêm cột mới
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastErrorAt;
    private Double estimatedCost;

    // Constructor mặc định
    public Chapter() {
        this.betaStatus = BetaStatus.NOT_BETA; // Giá trị mặc định
    }
    
    // Constructor với các thông tin cơ bản
    public Chapter(Long bookId, Integer chapterNumber, String sourceFilename) {
        this.bookId = bookId;
        this.chapterNumber = chapterNumber;
        this.sourceFilename = sourceFilename;
        this.status = ChapterStatus.PENDING;
        this.betaStatus = BetaStatus.NOT_BETA;
        this.retryCount = 0;
    }

    // Getters và Setters cũ
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Integer getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(Integer chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public String getSourceFilename() {
        return sourceFilename;
    }

    public void setSourceFilename(String sourceFilename) {
        this.sourceFilename = sourceFilename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSourceContent() {
        return sourceContent;
    }

    public void setSourceContent(String sourceContent) {
        this.sourceContent = sourceContent;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public ChapterStatus getStatus() {
        return status;
    }

    public void setStatus(ChapterStatus status) {
        this.status = status;
    }

    // Getter và Setter cho BetaStatus mới
    public BetaStatus getBetaStatus() {
        return betaStatus;
    }

    public void setBetaStatus(BetaStatus betaStatus) {
        this.betaStatus = betaStatus != null ? betaStatus : BetaStatus.NOT_BETA;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Integer getRetryCount() {
        return retryCount != null ? retryCount : 0;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
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
    
    public LocalDateTime getLastErrorAt() {
        return lastErrorAt;
    }

    public void setLastErrorAt(LocalDateTime lastErrorAt) {
        this.lastErrorAt = lastErrorAt;
    }

    public Double getEstimatedCost() {
        return estimatedCost != null ? estimatedCost : 0.0;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }
    
    // Utility methods
    public boolean hasSourceContent() {
        return sourceContent != null && !sourceContent.trim().isEmpty();
    }
    
    public boolean isCompleted() {
        return status == ChapterStatus.DONE;
    }
    
    public boolean hasError() {
        return status == ChapterStatus.ERROR;
    }
    
    public boolean isPending() {
        return status == ChapterStatus.PENDING;
    }
    
    // Beta status utility methods
    public boolean isBetaDone() {
        return betaStatus == BetaStatus.DONE_BETA || betaStatus == BetaStatus.DONE_POST;
    }
    
    public boolean isPosted() {
        return betaStatus == BetaStatus.DONE_POST;
    }
    
    public boolean needsBeta() {
        return betaStatus == BetaStatus.NOT_BETA && isCompleted();
    }
    
    @Override
    public String toString() {
        return String.format("Chapter{id=%d, bookId=%d, chapter=%d, status=%s, betaStatus=%s}", 
            id, bookId, chapterNumber, status, betaStatus);
    }
}