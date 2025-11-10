package com.mycompany.quanlytruyen.model;

import java.time.LocalDateTime;

public class ProgressEvent {
    private final int completed;
    private final int total;
    private final String message;
    private final ProgressStatus status;
    private final String currentChapter;
    private final LocalDateTime timestamp;
    private final Exception error;
    
    public ProgressEvent(int completed, int total, String message) {
        this(completed, total, message, ProgressStatus.IN_PROGRESS, null, null);
    }
    
    public ProgressEvent(int completed, int total, String message, ProgressStatus status) {
        this(completed, total, message, status, null, null);
    }
    
    public ProgressEvent(int completed, int total, String message, ProgressStatus status, String currentChapter, Exception error) {
        this.completed = completed;
        this.total = total;
        this.message = message;
        this.status = status;
        this.currentChapter = currentChapter;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public int getCompleted() { return completed; }
    public int getTotal() { return total; }
    public String getMessage() { return message; }
    public ProgressStatus getStatus() { return status; }
    public String getCurrentChapter() { return currentChapter; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Exception getError() { return error; }
    
    // Utility methods
    public double getProgressPercentage() {
        return total > 0 ? (completed * 100.0 / total) : 0.0;
    }
    
    public boolean isCompleted() {
        return status == ProgressStatus.COMPLETED;
    }
    
    public boolean hasError() {
        return status == ProgressStatus.ERROR || error != null;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %d/%d (%.1f%%) - %s", 
            status, completed, total, getProgressPercentage(), message);
    }
    
    public enum ProgressStatus {
        STARTING,
        IN_PROGRESS,
        COMPLETED,
        ERROR,
        CANCELLED
    }
}