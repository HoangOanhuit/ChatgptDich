package com.mycompany.quanlytruyen.service;

import com.mycompany.quanlytruyen.dao.ChapterDao;
import com.mycompany.quanlytruyen.model.ProgressEvent;
import com.mycompany.quanlytruyen.model.ProgressEvent.ProgressStatus;
import com.mycompany.quanlytruyen.utils.FileUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TranslationService {
    private final AIClient aiClient;
    private final ChapterDao chapterDao;
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    
    // Constants
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final long RATE_LIMIT_DELAY_MS = 2000; // Delay between API calls
    
    public TranslationService(AIClient aiClient, ChapterDao chapterDao) {
        this.aiClient = aiClient;
        this.chapterDao = chapterDao;
    }
    
    /**
     * Process multiple chapter files for translation
     */
    public void processChapters(long bookId, List<File> chapterFiles, 
                              String guidelines, String nameTable, String model,
                              Consumer<ProgressEvent> progressCallback) {
        
        isCancelled.set(false);
        int total = chapterFiles.size();
        int completed = 0;
        
        try {
            // Send starting event
            progressCallback.accept(new ProgressEvent(0, total, 
                "Bắt đầu dịch " + total + " chương...", ProgressStatus.STARTING));
            
            for (File file : chapterFiles) {
                if (isCancelled.get()) {
                    progressCallback.accept(new ProgressEvent(completed, total, 
                        "Quá trình dịch đã bị hủy", ProgressStatus.CANCELLED));
                    return;
                }
                
                try {
                    int chapterNumber = FileUtils.extractChapterNumber(file.getName());
                    String chapterName = "Chương " + chapterNumber;
                    
                    // Update progress - starting chapter
                    progressCallback.accept(new ProgressEvent(completed, total, 
                        "Đang dịch " + chapterName + "...", ProgressStatus.IN_PROGRESS, chapterName, null));
                    
                    // Read source file content
                    String sourceText = FileUtils.readUtf8(file);
                    
                    // Insert/update pending record with source content for resume support
                    chapterDao.upsertPending(bookId, chapterNumber, file.getName());
                    
                    // Translate with retry mechanism
                    String translatedText = translateWithRetry(model, guidelines, nameTable, sourceText, progressCallback);

                    // Normalize paragraph spacing
                    translatedText = normalizeParagraphSpacing(translatedText);                    
                    
                    // Calculate word count and estimated cost
                    int wordCount = countWords(translatedText);
                    double estimatedCost = aiClient.calculateEstimatedCost(sourceText.length(), model);
                    
                    // Save translation result - beta_status will be automatically set to 'not_beta'
                    chapterDao.updateDone(bookId, chapterNumber, translatedText, wordCount, estimatedCost);
                    
                    completed++;
                    
                    // Update progress - chapter completed
                    progressCallback.accept(new ProgressEvent(completed, total, 
                        "Hoàn thành " + chapterName + " (" + wordCount + " từ, $" + String.format("%.4f", estimatedCost) + ")", 
                        ProgressStatus.IN_PROGRESS, chapterName, null));
                    
                    // Rate limiting delay between API calls
                    if (completed < total) {
                        Thread.sleep(RATE_LIMIT_DELAY_MS);
                    }
                    
                } catch (Exception e) {
                    // Update error status in database
                    try {
                        int chapterNumber = FileUtils.extractChapterNumber(file.getName());
                        chapterDao.updateError(bookId, chapterNumber, e.getMessage());
                    } catch (SQLException dbError) {
                        // Log database error but continue processing
                        System.err.println("Database error while updating error status: " + dbError.getMessage());
                    }
                    
                    String chapterName = "Chương " + FileUtils.extractChapterNumber(file.getName());
                    progressCallback.accept(new ProgressEvent(completed, total, 
                        "Lỗi khi dịch " + chapterName + ": " + e.getMessage(), 
                        ProgressStatus.IN_PROGRESS, chapterName, e));
                    
                    // Continue with next chapter even if current one fails
                }
            }
            
            // Send completion event
            if (!isCancelled.get()) {
                progressCallback.accept(new ProgressEvent(completed, total, 
                    "Hoàn thành dịch " + completed + "/" + total + " chương. Tất cả chương đã dịch xong sẽ có trạng thái 'Chưa beta'.", 
                    ProgressStatus.COMPLETED));
            }
            
        } catch (Exception e) {
            progressCallback.accept(new ProgressEvent(completed, total, 
                "Lỗi nghiêm trọng: " + e.getMessage(), ProgressStatus.ERROR, null, e));
        }
    }
    /**
     * Resume translation for pending/error chapters
     */
    public void resumeTranslation(long bookId, String guidelines, String nameTable, String model,
                                Consumer<ProgressEvent> progressCallback) {
        isCancelled.set(false);
        
        try {
            // Update book's last resumed timestamp (if this method exists in ChapterDao)
            try {
                chapterDao.updateBookResumedAt(bookId);
            } catch (Exception e) {
                // Method might not exist, continue anyway
                System.out.println("updateBookResumedAt not available: " + e.getMessage());
            }
            
            // Get chapters that need to be resumed
            List<ChapterDao.ResumeChapter> resumeChapters = chapterDao.listPendingOrErrorChapters(bookId);
            
            if (resumeChapters.isEmpty()) {
                progressCallback.accept(new ProgressEvent(0, 0, 
                    "Không có chương nào cần dịch tiếp", ProgressStatus.COMPLETED));
                return;
            }
            
            // Filter chapters with and without source content
            List<ChapterDao.ResumeChapter> validChapters = resumeChapters.stream()
                .filter(ChapterDao.ResumeChapter::hasSourceContent)
                .toList();
            
            List<ChapterDao.ResumeChapter> missingSourceChapters = resumeChapters.stream()
                .filter(ch -> !ch.hasSourceContent())
                .toList();
            
            // Warn about chapters missing source content
            if (!missingSourceChapters.isEmpty()) {
                String missingChapters = missingSourceChapters.stream()
                    .map(ch -> "C" + ch.chapterNumber)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
                    
                progressCallback.accept(new ProgressEvent(0, resumeChapters.size(),
                    "Cảnh báo: " + missingSourceChapters.size() + " chương thiếu source content: " + missingChapters,
                    ProgressStatus.IN_PROGRESS));
            }
            
            int total = validChapters.size();
            int completed = 0;
            
            if (total == 0) {
                progressCallback.accept(new ProgressEvent(0, 0,
                    "Không có chương hợp lệ nào để dịch tiếp (tất cả đều thiếu source content)",
                    ProgressStatus.COMPLETED));
                return;
            }
            
            progressCallback.accept(new ProgressEvent(0, total,
                "Bắt đầu resume dịch " + total + " chương...", ProgressStatus.STARTING));
            
            // Process each valid chapter
            for (ChapterDao.ResumeChapter chapter : validChapters) {
                if (isCancelled.get()) {
                    progressCallback.accept(new ProgressEvent(completed, total,
                        "Quá trình resume đã bị hủy", ProgressStatus.CANCELLED));
                    return;
                }
                
                try {
                    String chapterName = "Chương " + chapter.chapterNumber;
                    
                    // Update progress - starting chapter
                    progressCallback.accept(new ProgressEvent(completed, total,
                        "Đang resume dịch " + chapterName + "...", ProgressStatus.IN_PROGRESS, chapterName, null));
                    
                    // Translate with retry mechanism
                    String translatedText = translateWithRetry(model, guidelines, nameTable,
                        chapter.sourceContent, progressCallback);

                    // Normalize paragraph spacing
                    translatedText = normalizeParagraphSpacing(translatedText);                    
                    
                    // Calculate word count and estimated cost
                    int wordCount = countWords(translatedText);
                    double estimatedCost = aiClient.calculateEstimatedCost(chapter.sourceContent.length(), model);
                    
                    // Save result - beta_status will be automatically set to 'not_beta'
                    chapterDao.updateDone(bookId, chapter.chapterNumber, translatedText, wordCount, estimatedCost);
                    
                    completed++;
                    
                    progressCallback.accept(new ProgressEvent(completed, total,
                        "Hoàn thành resume " + chapterName + " (" + wordCount + " từ, $" + String.format("%.4f", estimatedCost) + ")",
                        ProgressStatus.IN_PROGRESS, chapterName, null));
                    
                    // Rate limiting delay
                    if (completed < total) {
                        Thread.sleep(RATE_LIMIT_DELAY_MS);
                    }
                    
                } catch (Exception e) {
                    // Update error status
                    try {
                        chapterDao.updateError(bookId, chapter.chapterNumber, e.getMessage());
                    } catch (SQLException dbError) {
                        System.err.println("Database error while updating error status: " + dbError.getMessage());
                    }
                    
                    String chapterName = "Chương " + chapter.chapterNumber;
                    progressCallback.accept(new ProgressEvent(completed, total,
                        "Lỗi khi resume " + chapterName + ": " + e.getMessage(),
                        ProgressStatus.IN_PROGRESS, chapterName, e));
                    
                    // Continue with next chapter
                }
            }
            
            // Send completion event
            if (!isCancelled.get()) {
                progressCallback.accept(new ProgressEvent(completed, total,
                    "Hoàn thành resume " + completed + "/" + total + " chương. Các chương đã dịch xong sẽ có trạng thái 'Chưa beta'.",
                    ProgressStatus.COMPLETED));
            }
            
        } catch (Exception e) {
            progressCallback.accept(new ProgressEvent(0, 0,
                "Lỗi nghiêm trọng khi resume: " + e.getMessage(), ProgressStatus.ERROR, null, e));
        }
    }
    
    /**
     * Cancel ongoing translation
     */
    public void cancelTranslation() {
        isCancelled.set(true);
    }
    
    /**
     * Check if translation is currently cancelled
     */
    public boolean isCancelled() {
        return isCancelled.get();
    }
    
    /**
     * Translate with retry mechanism
     */
    private String translateWithRetry(String model, String guidelines, String nameTable, 
                                    String sourceText, Consumer<ProgressEvent> progressCallback) throws Exception {
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                // Call AI API with native chapter translation support so it can
                // manage long inputs (chunking, token budgeting, ...)
                String translatedText = aiClient.translateChapter(model, guidelines, nameTable, sourceText);

                
                // Validate translation result
                if (translatedText == null || translatedText.trim().isEmpty()) {
                    throw new RuntimeException("API trả về kết quả rỗng");
                }
                
                return translatedText;
                
            } catch (Exception e) {
                lastException = e;
                
                if (attempt < MAX_RETRIES) {
                    // Not the last attempt, wait and retry
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Quá trình dịch bị gián đoạn", ie);
                    }
                    
                    // Optional: notify about retry
                    if (progressCallback != null) {
                        progressCallback.accept(new ProgressEvent(0, 0,
                            "Thử lại lần " + attempt + "/" + MAX_RETRIES + " do lỗi: " + e.getMessage(),
                            ProgressStatus.IN_PROGRESS));
                    }
                }
            }
        }
        
        // All retries failed
        throw new RuntimeException("Dịch thất bại sau " + MAX_RETRIES + " lần thử: " + 
            (lastException != null ? lastException.getMessage() : "Lỗi không xác định"));
    } 

    /**
     * Normalize paragraph spacing so that paragraphs are separated by exactly
     * one empty line.
     */
    private String normalizeParagraphSpacing(String text) {
        if (text == null) {
            return null;
        }
        // Trim leading/trailing whitespace and ensure exactly two newlines between paragraphs
        return text.trim().replaceAll("\\r?\\n+", "\n\n");
    }    
    /**
     * Count words in Vietnamese text
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        
        // Simple word count - split by whitespace and filter non-empty
        return text.trim().split("\\s+").length;
    }
    
    /**
     * Estimate total cost for translation
     */
    public double estimateCost(List<File> chapterFiles, String model) {
        try {
            int totalLength = 0;
            for (File file : chapterFiles) {
                String content = FileUtils.readUtf8(file);
                totalLength += content.length();
            }
            return aiClient.calculateEstimatedCost(totalLength, model);
        } catch (Exception e) {
            return 0.0; // Return 0 if cannot estimate
        }
    }
}