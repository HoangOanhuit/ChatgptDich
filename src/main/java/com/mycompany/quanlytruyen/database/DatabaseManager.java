package com.mycompany.quanlytruyen.database;

import java.sql.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý database SQLite cho ứng dụng dịch truyện
 */
public class DatabaseManager {
    private static final String DB_NAME = "translations.db";
    private Connection connection;
    
    /**
     * Khởi tạo database và tạo bảng nếu chưa có
     */
    public void initDatabase() throws SQLException {
        try {
            // Tạo connection đến SQLite
            String url = "jdbc:sqlite:" + DB_NAME;
            connection = DriverManager.getConnection(url);
            
            System.out.println("✓ Kết nối database thành công: " + DB_NAME);
            
            // Tạo các bảng
            createTables();
            
        } catch (SQLException e) {
            System.err.println("✗ Lỗi khởi tạo database: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Tạo các bảng trong database
     */
    private void createTables() throws SQLException {
        String createTranslationsTable = """
            CREATE TABLE IF NOT EXISTS translations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                chapter_number INTEGER NOT NULL UNIQUE,
                original_text TEXT NOT NULL,
                translated_text TEXT NOT NULL,
                translation_rules TEXT,
                character_table TEXT,
                status VARCHAR(20) DEFAULT 'completed',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;
        
        String createIndexes = """
            CREATE INDEX IF NOT EXISTS idx_chapter_number 
            ON translations(chapter_number);
            
            CREATE INDEX IF NOT EXISTS idx_status 
            ON translations(status);
            
            CREATE INDEX IF NOT EXISTS idx_created_at 
            ON translations(created_at);
        """;
        
        String createMetadataTable = """
            CREATE TABLE IF NOT EXISTS translation_metadata (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                novel_name VARCHAR(255),
                author VARCHAR(255),
                total_chapters INTEGER,
                translated_chapters INTEGER DEFAULT 0,
                start_date TIMESTAMP,
                last_updated TIMESTAMP,
                notes TEXT
            );
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTranslationsTable);
            stmt.executeUpdate(createIndexes);
            stmt.executeUpdate(createMetadataTable);
            System.out.println("✓ Các bảng đã được tạo/kiểm tra");
        }
    }
    
    /**
     * Lưu bản dịch mới hoặc cập nhật nếu đã tồn tại
     */
    public void saveTranslation(int chapterNumber, String originalText, String translatedText) 
            throws SQLException {
        saveTranslation(chapterNumber, originalText, translatedText, null, null);
    }
    
    /**
     * Lưu bản dịch với đầy đủ thông tin
     */
    public void saveTranslation(int chapterNumber, String originalText, String translatedText,
                                String translationRules, String characterTable) 
            throws SQLException {
        
        // Kiểm tra xem chương đã tồn tại chưa
        if (chapterExists(chapterNumber)) {
            updateTranslation(chapterNumber, translatedText, translationRules, characterTable);
        } else {
            insertTranslation(chapterNumber, originalText, translatedText, 
                            translationRules, characterTable);
        }
    }
    
    /**
     * Kiểm tra chương đã tồn tại chưa
     */
    private boolean chapterExists(int chapterNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM translations WHERE chapter_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chapterNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }
    
    /**
     * Thêm bản dịch mới
     */
    private void insertTranslation(int chapterNumber, String originalText, String translatedText,
                                   String translationRules, String characterTable) 
            throws SQLException {
        String sql = """
            INSERT INTO translations 
            (chapter_number, original_text, translated_text, translation_rules, character_table, status)
            VALUES (?, ?, ?, ?, ?, 'completed')
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chapterNumber);
            pstmt.setString(2, originalText);
            pstmt.setString(3, translatedText);
            pstmt.setString(4, translationRules);
            pstmt.setString(5, characterTable);
            
            pstmt.executeUpdate();
            System.out.println("✓ Đã lưu chương " + chapterNumber + " vào database");
        }
    }
    
    /**
     * Cập nhật bản dịch đã có
     */
    private void updateTranslation(int chapterNumber, String translatedText,
                                   String translationRules, String characterTable) 
            throws SQLException {
        String sql = """
            UPDATE translations 
            SET translated_text = ?, 
                translation_rules = ?,
                character_table = ?,
                updated_at = CURRENT_TIMESTAMP,
                status = 'completed'
            WHERE chapter_number = ?
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, translatedText);
            pstmt.setString(2, translationRules);
            pstmt.setString(3, characterTable);
            pstmt.setInt(4, chapterNumber);
            
            pstmt.executeUpdate();
            System.out.println("✓ Đã cập nhật chương " + chapterNumber + " trong database");
        }
    }
    
    /**
     * Lấy bản dịch theo số chương
     */
    public String getTranslation(int chapterNumber) throws SQLException {
        String sql = "SELECT translated_text FROM translations WHERE chapter_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chapterNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("translated_text");
            }
            return null;
        }
    }
    
    /**
     * Lấy văn bản gốc theo số chương
     */
    public String getOriginalText(int chapterNumber) throws SQLException {
        String sql = "SELECT original_text FROM translations WHERE chapter_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chapterNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("original_text");
            }
            return null;
        }
    }
    
    /**
     * Lấy thông tin đầy đủ của một chương
     */
    public ChapterInfo getChapterInfo(int chapterNumber) throws SQLException {
        String sql = """
            SELECT chapter_number, original_text, translated_text, 
                   translation_rules, character_table, status, 
                   created_at, updated_at
            FROM translations 
            WHERE chapter_number = ?
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chapterNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                ChapterInfo info = new ChapterInfo();
                info.chapterNumber = rs.getInt("chapter_number");
                info.originalText = rs.getString("original_text");
                info.translatedText = rs.getString("translated_text");
                info.translationRules = rs.getString("translation_rules");
                info.characterTable = rs.getString("character_table");
                info.status = rs.getString("status");
                info.createdAt = rs.getString("created_at");
                info.updatedAt = rs.getString("updated_at");
                return info;
            }
            return null;
        }
    }
    
    /**
     * Lấy danh sách tất cả các chương đã dịch
     */
    public List<Integer> getAllTranslatedChapters() throws SQLException {
        List<Integer> chapters = new ArrayList<>();
        String sql = "SELECT chapter_number FROM translations ORDER BY chapter_number";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                chapters.add(rs.getInt("chapter_number"));
            }
        }
        
        return chapters;
    }
    
    /**
     * Đếm số chương đã dịch
     */
    public int getTranslatedChapterCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM translations WHERE status = 'completed'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    /**
     * Export bản dịch ra file
     */
    public void exportToFile(int chapterNumber, String filePath) throws SQLException {
        String translation = getTranslation(chapterNumber);
        
        if (translation != null) {
            try {
                File file = new File(filePath);
                file.getParentFile().mkdirs(); // Tạo thư mục nếu chưa có
                
                java.nio.file.Files.writeString(
                    file.toPath(), 
                    translation,
                    java.nio.charset.StandardCharsets.UTF_8
                );
                
                System.out.println("✓ Đã export chương " + chapterNumber + " ra: " + filePath);
                
            } catch (Exception e) {
                System.err.println("✗ Lỗi export file: " + e.getMessage());
                throw new SQLException("Không thể export file", e);
            }
        } else {
            System.err.println("✗ Không tìm thấy chương " + chapterNumber + " trong database");
        }
    }
    
    /**
     * Export tất cả các chương ra thư mục
     */
    public void exportAllChapters(String outputDir) throws SQLException {
        List<Integer> chapters = getAllTranslatedChapters();
        
        for (int chapterNum : chapters) {
            String fileName = String.format("Chapter_%03d_VN.txt", chapterNum);
            String filePath = outputDir + File.separator + fileName;
            exportToFile(chapterNum, filePath);
        }
        
        System.out.println("✓ Đã export " + chapters.size() + " chương vào: " + outputDir);
    }
    
    /**
     * Xóa một chương
     */
    public void deleteChapter(int chapterNumber) throws SQLException {
        String sql = "DELETE FROM translations WHERE chapter_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chapterNumber);
            int affected = pstmt.executeUpdate();
            
            if (affected > 0) {
                System.out.println("✓ Đã xóa chương " + chapterNumber);
            } else {
                System.out.println("⚠ Không tìm thấy chương " + chapterNumber);
            }
        }
    }
    
    /**
     * Xóa tất cả dữ liệu
     */
    public void clearAllData() throws SQLException {
        String sql = "DELETE FROM translations";
        
        try (Statement stmt = connection.createStatement()) {
            int affected = stmt.executeUpdate(sql);
            System.out.println("✓ Đã xóa " + affected + " bản dịch");
        }
    }
    
    /**
     * Lấy thống kê
     */
    public DatabaseStats getStatistics() throws SQLException {
        DatabaseStats stats = new DatabaseStats();
        
        String sql = """
            SELECT 
                COUNT(*) as total_chapters,
                SUM(LENGTH(original_text)) as total_original_chars,
                SUM(LENGTH(translated_text)) as total_translated_chars,
                MIN(created_at) as first_translation,
                MAX(updated_at) as last_translation
            FROM translations
        """;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                stats.totalChapters = rs.getInt("total_chapters");
                stats.totalOriginalChars = rs.getLong("total_original_chars");
                stats.totalTranslatedChars = rs.getLong("total_translated_chars");
                stats.firstTranslation = rs.getString("first_translation");
                stats.lastTranslation = rs.getString("last_translation");
            }
        }
        
        return stats;
    }
    
    /**
     * Đóng kết nối database
     */
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("✓ Đã đóng kết nối database");
        }
    }
    
    /**
     * Kiểm tra kết nối
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    // ============ Inner Classes ============
    
    /**
     * Class chứa thông tin chi tiết của một chương
     */
    public static class ChapterInfo {
        public int chapterNumber;
        public String originalText;
        public String translatedText;
        public String translationRules;
        public String characterTable;
        public String status;
        public String createdAt;
        public String updatedAt;
        
        @Override
        public String toString() {
            return String.format("Chương %d - Status: %s - Created: %s", 
                                chapterNumber, status, createdAt);
        }
    }
    
    /**
     * Class chứa thống kê database
     */
    public static class DatabaseStats {
        public int totalChapters;
        public long totalOriginalChars;
        public long totalTranslatedChars;
        public String firstTranslation;
        public String lastTranslation;
        
        @Override
        public String toString() {
            return String.format(
                "=== THỐNG KÊ DATABASE ===\n" +
                "Tổng số chương: %d\n" +
                "Tổng ký tự gốc: %,d\n" +
                "Tổng ký tự dịch: %,d\n" +
                "Bản dịch đầu: %s\n" +
                "Bản dịch cuối: %s",
                totalChapters, totalOriginalChars, totalTranslatedChars,
                firstTranslation, lastTranslation
            );
        }
    }
    
    // ============ Test Main ============
    
    /**
     * Test database manager
     */
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        
        try {
            // 1. Khởi tạo
            System.out.println("=== Test DatabaseManager ===\n");
            dbManager.initDatabase();
            
            // 2. Thêm dữ liệu test
            System.out.println("\n1. Thêm dữ liệu test:");
            dbManager.saveTranslation(1, "第一章：开始", "Chương 1: Khởi đầu", 
                                     "Dịch tự nhiên", "Nhân vật A, B, C");
            dbManager.saveTranslation(2, "第二章：冒险", "Chương 2: Phiêu lưu", 
                                     "Dịch tự nhiên", "Nhân vật A, B, C");
            
            // 3. Lấy dữ liệu
            System.out.println("\n2. Lấy bản dịch chương 1:");
            String translation = dbManager.getTranslation(1);
            System.out.println("   " + translation);
            
            // 4. Lấy thông tin chi tiết
            System.out.println("\n3. Thông tin chi tiết chương 1:");
            ChapterInfo info = dbManager.getChapterInfo(1);
            System.out.println("   " + info);
            
            // 5. Liệt kê tất cả
            System.out.println("\n4. Danh sách các chương:");
            List<Integer> chapters = dbManager.getAllTranslatedChapters();
            System.out.println("   " + chapters);
            
            // 6. Thống kê
            System.out.println("\n5. Thống kê:");
            DatabaseStats stats = dbManager.getStatistics();
            System.out.println(stats);
            
            // 7. Export
            System.out.println("\n6. Export files:");
            dbManager.exportAllChapters("output/test_export");
            
            // 8. Đóng
            dbManager.close();
            
            System.out.println("\n=== Test hoàn tất ===");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}