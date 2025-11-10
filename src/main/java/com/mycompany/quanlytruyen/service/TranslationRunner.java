package com.mycompany.quanlytruyen.service;

import com.mycompany.quanlytruyen.dao.ChapterDao;
import com.mycompany.quanlytruyen.utils.FileUtils;
import com.mycompany.quanlytruyen.utils.PromptBuilder;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class TranslationRunner {
    public record Progress(int done, int total, String message) {}

    public interface ProgressListener { 
        void onProgress(Progress p); 
    }

    private final AIClient aiClient;
    private final ChapterDao chapterDao;
    private final ProgressListener listener;

    public TranslationRunner(AIClient aiClient, ChapterDao chapterDao, ProgressListener listener) {
        this.aiClient = aiClient;
        this.chapterDao = chapterDao;
        this.listener = listener;
    }

    /**
     * Process files for translation
     */
    public void processFiles(long bookId, List<File> chapterFiles, String guidelines, String nameTableBlock, String model) {
        int total = chapterFiles.size();
        int done = 0;
        
        for (File f : chapterFiles) {
            int chNo = FileUtils.extractChapterNumber(f.getName());
            
            try {
                // Read source content
                String src = Files.readString(f.toPath());
                
                // Insert/update pending record with source content
                chapterDao.upsertPending(bookId, chNo, f.getName());
                
                // Build prompt
                String prompt = PromptBuilder.build(guidelines, nameTableBlock, src);
                
                // Translate with retry
                String translated = callOpenAIWithRetry(model, prompt, 3);
                
                // Calculate cost estimate
                double estimatedCost = aiClient.calculateEstimatedCost(src.length(), model);
                
                // Save result
                chapterDao.updateDone(bookId, chNo, translated, FileUtils.wordCount(translated), estimatedCost);
                
                done++;
                listener.onProgress(new Progress(done, total, "DONE C" + chNo));
                
                // Rate limiting delay
                Thread.sleep(500);
                
            } catch (Exception ex) {
                try { 
                    chapterDao.updateError(bookId, chNo, ex.getMessage()); 
                } catch (Exception ignore) {
                    // Log but don't fail the whole process
                    System.err.println("Failed to update error status: " + ignore.getMessage());
                }
                
                listener.onProgress(new Progress(done, total, "ERROR C" + chNo + " -> " + ex.getMessage()));
            }
        }
    }

    /**
     * Resume translation for pending or error chapters
     */
    public void processPendingOrError(long bookId, String guidelines, String nameTableBlock, String model) {
        try {
            List<ChapterDao.ResumeChapter> chapters = chapterDao.listPendingOrErrorChapters(bookId);
            int total = chapters.size();
            int done = 0;
            
            for (ChapterDao.ResumeChapter chapter : chapters) {
                try {
                    // Check if we have source content
                    if (!chapter.hasSourceContent()) {
                        listener.onProgress(new Progress(done, total, 
                            "SKIP C" + chapter.chapterNumber + " - No source content"));
                        continue;
                    }
                    
                    // Build prompt
                    String prompt = PromptBuilder.build(guidelines, nameTableBlock, chapter.sourceContent);
                    
                    // Translate with retry
                    String translated = callOpenAIWithRetry(model, prompt, 3);
                    
                    // Calculate cost estimate
                    double estimatedCost = aiClient.calculateEstimatedCost(chapter.sourceContent.length(), model);
                    
                    // Save result
                    chapterDao.updateDone(bookId, chapter.chapterNumber, translated, 
                        FileUtils.wordCount(translated), estimatedCost);
                    
                    done++;
                    listener.onProgress(new Progress(done, total, "RESUMED C" + chapter.chapterNumber));
                    
                    // Rate limiting delay
                    Thread.sleep(500);
                    
                } catch (Exception ex) {
                    try { 
                        chapterDao.updateError(bookId, chapter.chapterNumber, ex.getMessage()); 
                    } catch (Exception ignore) {
                        System.err.println("Failed to update error status: " + ignore.getMessage());
                    }
                    
                    listener.onProgress(new Progress(done, total, 
                        "ERROR C" + chapter.chapterNumber + " -> " + ex.getMessage()));
                }
            }
            
        } catch (Exception e) {
            listener.onProgress(new Progress(0, 0, "Resume failed: " + e.getMessage()));
        }
    }

    /**
     * Call OpenAI API with retry mechanism
     */
    private String callOpenAIWithRetry(String model, String prompt, int maxRetry) throws Exception {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxRetry) {
            try {
                return aiClient.translate(model, prompt);
            } catch (Exception ex) {
                lastException = ex;
                attempt++;
                
                if (attempt < maxRetry) {
                    long backoff = (long) (Math.pow(2, attempt) * 500L);
                    listener.onProgress(new Progress(0, 0, 
                        "Retry " + attempt + " after " + backoff + "ms: " + ex.getMessage()));
                    Thread.sleep(backoff);
                } else {
                    listener.onProgress(new Progress(0, 0, 
                        "Failed after " + maxRetry + " attempts: " + ex.getMessage()));
                }
            }
        }
        
        throw new Exception("Translation failed after " + maxRetry + " attempts", lastException);
    }
}