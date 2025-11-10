package com.mycompany.quanlytruyen.service;

import com.mycompany.quanlytruyen.utils.FileUtils;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class FileService {
    
    private static final Pattern CHAPTER_PATTERN = Pattern.compile("^C\\d+\\.txt$", Pattern.CASE_INSENSITIVE);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MIN_FILE_SIZE = 10; // 10 bytes
    
    /**
     * Open file chooser to select chapter files
     */
    public List<File> selectChapterFiles(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn các file chương (C1.txt, C2.txt, ...)");
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // Add file filter for .txt files
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Text files (*.txt)", "txt");
        fileChooser.setFileFilter(filter);
        
        // Set current directory to user's last used directory if available
        String lastDir = System.getProperty("user.home");
        fileChooser.setCurrentDirectory(new File(lastDir));
        
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            List<File> chapterFiles = new ArrayList<>();
            
            for (File file : selectedFiles) {
                if (isValidChapterFile(file)) {
                    chapterFiles.add(file);
                }
            }
            
            // Sort files by chapter number
            chapterFiles.sort(Comparator.comparingInt(f -> 
                FileUtils.extractChapterNumber(f.getName())));
            
            return chapterFiles;
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Select a single text file (for guidelines or name table)
     */
    public File selectTextFile(Component parent, String title) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(title);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // Add file filter
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Text files (*.txt)", "txt");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        
        return null;
    }
    
    /**
     * Get all chapter files from a directory
     */
    public List<File> getChapterFilesFromDirectory(File directory) {
        List<File> chapterFiles = new ArrayList<>();
        
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return chapterFiles;
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (isValidChapterFile(file)) {
                    chapterFiles.add(file);
                }
            }
        }
        
        // Sort files by chapter number
        chapterFiles.sort(Comparator.comparingInt(f -> 
            FileUtils.extractChapterNumber(f.getName())));
        
        return chapterFiles;
    }
    
    /**
     * Validate if file is a valid chapter file
     */
    public boolean isValidChapterFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        // Check filename pattern
        if (!CHAPTER_PATTERN.matcher(file.getName()).matches()) {
            return false;
        }
        
        // Check file size
        long size = file.length();
        if (size < MIN_FILE_SIZE || size > MAX_FILE_SIZE) {
            return false;
        }
        
        // Check if file is readable
        if (!file.canRead()) {
            return false;
        }
        
        try {
            // Quick check if file content is text (not binary)
            String content = FileUtils.readUtf8(file);
            return content != null && !content.trim().isEmpty();
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Validate chapter files and return validation result
     */
    public FileValidationResult validateChapterFiles(List<File> files) {
        FileValidationResult result = new FileValidationResult();
        
        if (files == null || files.isEmpty()) {
            result.addError("Chưa chọn file nào");
            return result;
        }
        
        List<Integer> chapterNumbers = new ArrayList<>();
        long totalSize = 0;
        
        for (File file : files) {
            // Validate individual file
            if (!isValidChapterFile(file)) {
                result.addError("File không hợp lệ: " + file.getName());
                continue;
            }
            
            // Extract chapter number
            int chapterNum = FileUtils.extractChapterNumber(file.getName());
            if (chapterNum <= 0) {
                result.addError("Không thể xác định số chương từ file: " + file.getName());
                continue;
            }
            
            // Check for duplicates
            if (chapterNumbers.contains(chapterNum)) {
                result.addError("Trùng lặp chương " + chapterNum + ": " + file.getName());
                continue;
            }
            
            chapterNumbers.add(chapterNum);
            totalSize += file.length();
            result.addValidFile(file);
        }
        
        // Check chapter sequence
        chapterNumbers.sort(Integer::compareTo);
        for (int i = 1; i < chapterNumbers.size(); i++) {
            int current = chapterNumbers.get(i);
            int previous = chapterNumbers.get(i - 1);
            if (current != previous + 1) {
                result.addWarning("Thiếu chương " + (previous + 1) + " đến " + (current - 1));
            }
        }
        
        // Check total size
        if (totalSize > 50 * 1024 * 1024) { // 50MB
            result.addWarning("Tổng kích thước files lớn: " + formatFileSize(totalSize));
        }
        
        result.setTotalSize(totalSize);
        result.setChapterCount(result.getValidFiles().size());
        
        return result;
    }
    
    /**
     * Read text file content
     */
    public String readTextFile(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File không tồn tại");
        }
        
        return FileUtils.readUtf8(file);
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    /**
     * File validation result class
     */
    public static class FileValidationResult {
        private final List<File> validFiles = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private long totalSize = 0;
        private int chapterCount = 0;
        
        public boolean isValid() {
            return errors.isEmpty() && !validFiles.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public void addValidFile(File file) {
            validFiles.add(file);
        }
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        // Getters
        public List<File> getValidFiles() { return new ArrayList<>(validFiles); }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
        public long getTotalSize() { return totalSize; }
        public int getChapterCount() { return chapterCount; }
        
        public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
        public void setChapterCount(int chapterCount) { this.chapterCount = chapterCount; }
        
        public String getErrorsAsString() {
            return String.join("\n", errors);
        }
        
        public String getWarningsAsString() {
            return String.join("\n", warnings);
        }
        
        public String getSummary() {
            return String.format("%d chương hợp lệ, tổng %.1f MB", 
                chapterCount, totalSize / (1024.0 * 1024.0));
        }
    }
}