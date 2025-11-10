package com.mycompany.quanlytruyen.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * File utilities for chapter processing
 * Compatible with Java 21
 */
public class FileUtils {
    
    /**
     * List files with filtering and sorting
     * @param dir Directory to scan
     * @param filter Filter predicate (can be null)
     * @param sorter Comparator for sorting (can be null)
     * @return Sorted list of files
     */
    public static List<File> listFilesSorted(File dir, FileFilter filter, FileComparator sorter) {
        if (dir == null || !dir.isDirectory()) {
            return new ArrayList<>();
        }
        
        File[] arr = dir.listFiles((File f) -> f.isFile() && (filter == null || filter.accept(f)));
        if (arr == null) return new ArrayList<>();
        
        List<File> list = new ArrayList<>(Arrays.asList(arr));
        if (sorter != null) {
            list.sort((Comparator<? super File>) sorter);
        }
        return list;
    }
    
    /**
     * Alternative method using lambda-style interface
     */
    public static List<File> listFilesSorted(File dir, FilePredicate filter, FileComparator sorter) {
        if (dir == null || !dir.isDirectory()) {
            return new ArrayList<>();
        }
        
        File[] arr = dir.listFiles((File f) -> f.isFile() && (filter == null || filter.test(f)));
        if (arr == null) return new ArrayList<>();
        
        List<File> list = new ArrayList<>(Arrays.asList(arr));
        if (sorter != null) {
            list.sort((Comparator<? super File>) sorter);
        }
        return list;
    }

    /**
     * Extract chapter number from filename like "C123.txt"
     * @param filename File name to parse
     * @return Chapter number, or -1 if not found
     */
    public static int extractChapterNumber(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return -1;
        }
        
        Matcher m = Pattern.compile("C(\\d+)\\.txt", Pattern.CASE_INSENSITIVE).matcher(filename);
        if (m.matches()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Count words in text (basic implementation)
     * @param text Text to count words in
     * @return Number of words
     */
    public static int wordCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        
        return trimmed.split("\\s+").length;
    }

    /**
     * Read file content as UTF-8 string
     * @param f File to read
     * @return File content as string
     * @throws IOException If file cannot be read
     */
    public static String readUtf8(File f) throws IOException {
        if (f == null || !f.exists() || !f.isFile()) {
            throw new IOException("File does not exist or is not a file: " + f);
        }
        
        return Files.readString(f.toPath());
    }
    
    /**
     * Check if file is a valid chapter file
     * @param file File to check
     * @return true if valid chapter file
     */
    public static boolean isValidChapterFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        // Check filename pattern
        if (extractChapterNumber(file.getName()) <= 0) {
            return false;
        }
        
        // Check file size (between 10 bytes and 10MB)
        long size = file.length();
        if (size < 10 || size > 10 * 1024 * 1024) {
            return false;
        }
        
        return file.canRead();
    }
    
    /**
     * Get file size in human-readable format
     * @param file File to check
     * @return Formatted size string
     */
    public static String getFormattedFileSize(File file) {
        if (file == null || !file.exists()) {
            return "0 B";
        }
        
        long bytes = file.length();
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    /**
     * Sort files by chapter number
     * @param files List of files to sort
     * @return New sorted list
     */
    public static List<File> sortByChapterNumber(List<File> files) {
        if (files == null) return new ArrayList<>();
        
        List<File> sorted = new ArrayList<>(files);
        sorted.sort((Comparator<? super File>) new ChapterNumberComparator());
        return sorted;
    }
    
    // =====================================================
    // Custom interfaces and classes to avoid import issues
    // =====================================================
    
    /**
     * Custom file filter interface
     */
    @FunctionalInterface
    public interface FileFilter {
        boolean accept(File file);
    }
    
    /**
     * Custom predicate interface for files
     */
    @FunctionalInterface
    public interface FilePredicate {
        boolean test(File file);
    }
    
    /**
     * Custom comparator interface for files
     */
    @FunctionalInterface
    public interface FileComparator {
        int compare(File f1, File f2);
        
        default void sort(List<File> list) {
            list.sort(this::compare);
        }
    }
    
    /**
     * Comparator for sorting files by chapter number
     */
    public static class ChapterNumberComparator implements FileComparator {
        @Override
        public int compare(File f1, File f2) {
            int ch1 = extractChapterNumber(f1.getName());
            int ch2 = extractChapterNumber(f2.getName());
            return Integer.compare(ch1, ch2);
        }
    }
    
    /**
     * Comparator for sorting files by name
     */
    public static class FileNameComparator implements FileComparator {
        @Override
        public int compare(File f1, File f2) {
            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    }
    
    /**
     * Comparator for sorting files by size
     */
    public static class FileSizeComparator implements FileComparator {
        @Override
        public int compare(File f1, File f2) {
            return Long.compare(f1.length(), f2.length());
        }
    }
    
    // =====================================================
    // Static instances for common comparators
    // =====================================================
    
    public static final FileComparator BY_CHAPTER_NUMBER = new ChapterNumberComparator();
    public static final FileComparator BY_NAME = new FileNameComparator();
    public static final FileComparator BY_SIZE = new FileSizeComparator();
    
    // =====================================================
    // Utility methods for common file operations
    // =====================================================
    
    /**
     * Get all chapter files from directory
     * @param directory Directory to scan
     * @return List of chapter files sorted by number
     */
    public static List<File> getChapterFiles(File directory) {
        if (directory == null || !directory.isDirectory()) {
            return new ArrayList<>();
        }
        
        List<File> chapterFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (isValidChapterFile(file)) {
                    chapterFiles.add(file);
                }
            }
        }
        
        return sortByChapterNumber(chapterFiles);
    }
    
    /**
     * Validate a list of chapter files
     * @param files Files to validate
     * @return Validation result with errors and warnings
     */
    public static FileValidationResult validateChapterFiles(List<File> files) {
        FileValidationResult result = new FileValidationResult();
        
        if (files == null || files.isEmpty()) {
            result.addError("No files provided");
            return result;
        }
        
        Set<Integer> chapterNumbers = new HashSet<>();
        for (File file : files) {
            if (!isValidChapterFile(file)) {
                result.addError("Invalid chapter file: " + file.getName());
                continue;
            }
            
            int chapterNum = extractChapterNumber(file.getName());
            if (chapterNumbers.contains(chapterNum)) {
                result.addError("Duplicate chapter number " + chapterNum + ": " + file.getName());
            } else {
                chapterNumbers.add(chapterNum);
                result.addValidFile(file);
            }
        }
        
        return result;
    }
    
    /**
     * Simple validation result class
     */
    public static class FileValidationResult {
        private final List<File> validFiles = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addValidFile(File file) { validFiles.add(file); }
        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
        
        public boolean isValid() { return errors.isEmpty() && !validFiles.isEmpty(); }
        public List<File> getValidFiles() { return new ArrayList<>(validFiles); }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
        
        public String getErrorsAsString() { return String.join("\n", errors); }
        public String getWarningsAsString() { return String.join("\n", warnings); }
    }
}