package com.mycompany.quanlytruyen.dao;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.quanlytruyen.model.BetaStatus;
import com.mycompany.quanlytruyen.model.Chapter;
import com.mycompany.quanlytruyen.model.ChapterStatus;

public class ChapterDao {
    private final DataSource ds;
    private static volatile boolean schemaEnsured = false;
    
    public ChapterDao(DataSource ds) {
        
        this.ds = ds;
        ensureSchema();
    }

    private void ensureSchema() {
        if (schemaEnsured) {
            return;
        }

        synchronized (ChapterDao.class) {
            if (schemaEnsured) {
                return;
            }

            try (Connection connection = ds.getConnection()) {
                if (!columnExists(connection, "chapters", "beta_status")) {
                    try (Statement stmt = connection.createStatement()) {
                        stmt.executeUpdate("""
                            ALTER TABLE chapters
                            ADD COLUMN beta_status VARCHAR(20) NOT NULL DEFAULT 'not_beta'
                        """);
                    }
                }

                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate("UPDATE chapters SET beta_status='not_beta' WHERE beta_status IS NULL");
                }

                schemaEnsured = true;
            } catch (SQLException e) {
                throw new IllegalStateException("Không thể đảm bảo schema cho bảng chapters: " + e.getMessage(), e);
            }
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = ?
              AND COLUMN_NAME = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Ensure book exists - simplified version without metadata initially
     */
    public long ensureBook(String title) throws SQLException {
        try (Connection c = ds.getConnection()) {
            // First try to find existing book
            try (PreparedStatement ps = c.prepareStatement("""
                SELECT id FROM books WHERE title = ?
                """)) {
                ps.setString(1, title);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("id");
                    }
                }
            }

            // Create new book
            try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO books (title, created_at) VALUES (?, NOW())
                """, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, title);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        }

        throw new SQLException("Không thể tạo book record");
    }


    /**
     * Insert hoặc update chapter với beta status
     */
    public void upsertPending(long bookId, int chapterNumber, String fileName) throws SQLException {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO chapters(
                    book_id, chapter_number, chapter_title, source_filename,
                    status, beta_status, retry_count, created_at, updated_at
                )
                VALUES (?, ?, NULL, ?, 'PENDING', 'not_beta', 0, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    source_filename=VALUES(source_filename),
                    status='PENDING',
                    retry_count=0,
                    updated_at=NOW()
                """)) {
                ps.setLong(1, bookId);
                ps.setInt(2, chapterNumber);
                ps.setString(3, fileName);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Update chapter as DONE with translated content
     */
    public void updateDone(long bookId, int chapterNumber, String content, int wordCount, double estimatedCost) throws SQLException {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE chapters SET 
                    content=?, 
                    word_count=?, 
                    estimated_cost=?,
                    status='DONE',
                    beta_status='not_beta',
                    error_message=NULL,
                    updated_at=NOW()
                WHERE book_id=? AND chapter_number=?
                """)) {
                ps.setString(1, content);
                ps.setInt(2, wordCount);
                ps.setDouble(3, estimatedCost);
                ps.setLong(4, bookId);
                ps.setInt(5, chapterNumber);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Update beta status của một chương
     */
    public void updateBetaStatus(long bookId, int chapterNumber, BetaStatus betaStatus) throws SQLException {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE chapters SET 
                    beta_status=?,
                    updated_at=NOW()
                WHERE book_id=? AND chapter_number=? AND status='DONE'
                """)) {
                ps.setString(1, betaStatus.getDbValue());
                ps.setLong(2, bookId);
                ps.setInt(3, chapterNumber);
                int updated = ps.executeUpdate();
                
                if (updated == 0) {
                    throw new SQLException("Không thể cập nhật beta status. Chương có thể chưa được dịch hoặc không tồn tại.");
                }
            }
        }
    }
    /**
     * Đánh dấu các chương kể từ một số thứ tự nhất định là đã đăng
     */
    public void markChaptersAsPostedFrom(long bookId, int fromChapterNumber) throws SQLException {
        if (fromChapterNumber <= 0) {
            return;
        }

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 UPDATE chapters
                 SET beta_status = ?, updated_at = NOW()
                 WHERE book_id = ? AND chapter_number >= ?
             """)) {
            ps.setString(1, BetaStatus.DONE_POST.getDbValue());
            ps.setLong(2, bookId);
            ps.setInt(3, fromChapterNumber);
            ps.executeUpdate();
        }
    }
    /**
     * Đánh dấu các chương có số thứ tự nhỏ hơn hoặc bằng giới hạn là đã đăng
     */
    public void markChaptersAsPostedUpTo(long bookId, int toChapterNumber) throws SQLException {
        if (toChapterNumber <= 0) {
            return;
        }

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 UPDATE chapters
                 SET beta_status = ?, updated_at = NOW()
                 WHERE book_id = ? AND chapter_number <= ?
             """)) {
            ps.setString(1, BetaStatus.DONE_POST.getDbValue());
            ps.setLong(2, bookId);
            ps.setInt(3, toChapterNumber);
            ps.executeUpdate();
        }
    }    
    /**
     * Cập nhật toàn bộ thông tin của một chương
     */
    public void updateChapter(Chapter chapter) throws SQLException {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE chapters SET
                    book_id = ?,
                    chapter_number = ?,
                    chapter_title = ?,
                    content = ?,
                    beta_status = ?,
                    updated_at = NOW()
                WHERE id = ?
                """)) {
                ps.setLong(1, chapter.getBookId());
                ps.setInt(2, chapter.getChapterNumber());
                ps.setString(3, chapter.getChapterTitle());
                ps.setString(4, chapter.getContent());
                ps.setString(5, chapter.getBetaStatus() != null ? chapter.getBetaStatus().getDbValue() : null);
                ps.setLong(6, chapter.getId());
                ps.executeUpdate();
            }
        }
    }

    public void updateChapterBetaContent(long bookId, int chapterNumber, String chapterTitle, String content) throws SQLException {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE chapters SET
                    chapter_title = ?,
                    content = ?,
                    beta_status = ?,
                    updated_at = NOW()
                WHERE book_id = ? AND chapter_number = ?
            """)) {
                ps.setString(1, chapterTitle);
                ps.setString(2, content);
                ps.setString(3, BetaStatus.DONE_BETA.getDbValue());
                ps.setLong(4, bookId);
                ps.setInt(5, chapterNumber);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    throw new SQLException("Không tìm thấy chương số " + chapterNumber + " để cập nhật");
                }
            }
        }
    }    
    /**
     * Update beta status cho nhiều chương cùng lúc
     */
    public void updateBetaStatusBatch(long bookId, List<Integer> chapterNumbers, BetaStatus betaStatus) throws SQLException {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE chapters SET 
                    beta_status=?,
                    updated_at=NOW()
                WHERE book_id=? AND chapter_number=? AND status='DONE'
                """)) {
                
                for (Integer chapterNumber : chapterNumbers) {
                    ps.setString(1, betaStatus.getDbValue());
                    ps.setLong(2, bookId);
                    ps.setInt(3, chapterNumber);
                    ps.addBatch();
                }
                
                ps.executeBatch();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }



    /**
     * Update chapter status to ERROR with retry tracking
     */
    public void updateError(long bookId, int chapterNumber, String errorMessage) throws SQLException {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE chapters SET 
                    status='ERROR',
                    error_message=?,
                    retry_count=retry_count+1,
                    last_error_at=NOW(),
                    updated_at=NOW()
                WHERE book_id=? AND chapter_number=?
                """)) {
                ps.setString(1, errorMessage);
                ps.setLong(2, bookId);
                ps.setInt(3, chapterNumber);
                ps.executeUpdate();
            }
        }
    }
    
    /**
    * Update book's last resumed timestamp
    */
   public void updateBookResumedAt(long bookId) throws SQLException {
       try (Connection c = ds.getConnection()) {
           try (PreparedStatement ps = c.prepareStatement("""
               UPDATE books SET 
                   last_resumed_at=NOW()
               WHERE id=?
               """)) {
               ps.setLong(1, bookId);
               ps.executeUpdate();
           }
       }
   }
   /**
     * Lấy tất cả chapters của một book với beta status
     */
    public List<Chapter> findByBookId(long bookId) throws SQLException {
        List<Chapter> chapters = new ArrayList<>();
        
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                SELECT id, book_id, chapter_number, chapter_title, source_filename, 
                       content, source_content, word_count, status, beta_status,
                       error_message, retry_count, created_at, updated_at, 
                       last_error_at, estimated_cost
                FROM chapters 
                WHERE book_id = ? 
                ORDER BY chapter_number
                """)) {
                ps.setLong(1, bookId);
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Chapter chapter = mapResultSetToChapter(rs);
                        chapters.add(chapter);
                    }
                }
            }
        }
        
        return chapters;
    }
    /**
     * Lấy chapters theo beta status
     */
    public List<Chapter> findByBookIdAndBetaStatus(long bookId, BetaStatus betaStatus) throws SQLException {
        List<Chapter> chapters = new ArrayList<>();
        
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                SELECT id, book_id, chapter_number, chapter_title, source_filename, 
                       content, source_content, word_count, status, beta_status,
                       error_message, retry_count, created_at, updated_at, 
                       last_error_at, estimated_cost
                FROM chapters 
                WHERE book_id = ? AND beta_status = ?
                ORDER BY chapter_number
                """)) {
                ps.setLong(1, bookId);
                ps.setString(2, betaStatus.getDbValue());
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Chapter chapter = mapResultSetToChapter(rs);
                        chapters.add(chapter);
                    }
                }
            }
        }
        
        return chapters;
    }

    /**
     * Lấy các chương cần beta (đã dịch xong nhưng chưa beta)
     */
    public List<Chapter> findChaptersNeedBeta(long bookId) throws SQLException {
        return findByBookIdAndBetaStatus(bookId, BetaStatus.NOT_BETA);
    }

    /**
     * Lấy các chương cần đăng (đã beta xong nhưng chưa đăng)
     */
    public List<Chapter> findChaptersNeedPost(long bookId) throws SQLException {
        return findByBookIdAndBetaStatus(bookId, BetaStatus.DONE_BETA);
    }

    /**
     * Thống kê beta status của một book
     */
    public BetaStatistics getBetaStatistics(long bookId) throws SQLException {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                SELECT 
                    COUNT(*) as total_chapters,
                    SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) as completed_chapters,
                    SUM(CASE WHEN beta_status = 'not_beta' THEN 1 ELSE 0 END) as not_beta,
                    SUM(CASE WHEN beta_status = 'done_beta' THEN 1 ELSE 0 END) as done_beta,
                    SUM(CASE WHEN beta_status = 'done_post' THEN 1 ELSE 0 END) as done_post
                FROM chapters 
                WHERE book_id = ?
                """)) {
                ps.setLong(1, bookId);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new BetaStatistics(
                            rs.getInt("total_chapters"),
                            rs.getInt("completed_chapters"),
                            rs.getInt("not_beta"),
                            rs.getInt("done_beta"),
                            rs.getInt("done_post")
                        );
                    }
                }
            }
        }
        
        return new BetaStatistics(0, 0, 0, 0, 0);
    }

    /**
     * Map ResultSet to Chapter object
     */
    private Chapter mapResultSetToChapter(ResultSet rs) throws SQLException {
        Chapter chapter = new Chapter();
        
        chapter.setId(rs.getLong("id"));
        chapter.setBookId(rs.getLong("book_id"));
        chapter.setChapterNumber(rs.getInt("chapter_number"));
        chapter.setChapterTitle(rs.getString("chapter_title"));
        chapter.setSourceFilename(rs.getString("source_filename"));
        chapter.setContent(rs.getString("content"));
        chapter.setSourceContent(rs.getString("source_content"));
        chapter.setWordCount(rs.getInt("word_count"));
        chapter.setStatus(ChapterStatus.valueOf(rs.getString("status")));
        chapter.setBetaStatus(BetaStatus.fromDbValue(rs.getString("beta_status")));
        chapter.setErrorMessage(rs.getString("error_message"));
        chapter.setRetryCount(rs.getInt("retry_count"));
        chapter.setEstimatedCost(rs.getDouble("estimated_cost"));
        
        // Handle timestamps
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            chapter.setCreatedAt(created.toLocalDateTime());
        }
        
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            chapter.setUpdatedAt(updated.toLocalDateTime());
        }
        
        Timestamp lastError = rs.getTimestamp("last_error_at");
        if (lastError != null) {
            chapter.setLastErrorAt(lastError.toLocalDateTime());
        }
        
        return chapter;
    }

    /**
     * Inner class để chứa thống kê beta
     */
    public static class BetaStatistics {
        public final int totalChapters;
        public final int completedChapters;
        public final int notBeta;
        public final int doneBeta;
        public final int donePost;
        
        public BetaStatistics(int totalChapters, int completedChapters, int notBeta, int doneBeta, int donePost) {
            this.totalChapters = totalChapters;
            this.completedChapters = completedChapters;
            this.notBeta = notBeta;
            this.doneBeta = doneBeta;
            this.donePost = donePost;
        }
        
        public double getBetaCompletionPercentage() {
            return completedChapters > 0 ? (double)(doneBeta + donePost) / completedChapters * 100 : 0;
        }
        
        public double getPostCompletionPercentage() {
            return totalChapters > 0 ? (double)donePost / totalChapters * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format("BetaStats{total=%d, completed=%d, notBeta=%d, doneBeta=%d, donePost=%d}", 
                totalChapters, completedChapters, notBeta, doneBeta, donePost);
        }
    }    
    /**
     * Get list of chapter numbers that are PENDING or ERROR - legacy method
     */
    public List<Integer> listPendingOrError(long bookId) throws SQLException {
        List<Integer> result = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("""
                SELECT chapter_number
                FROM chapters
                WHERE book_id=? AND status IN ('PENDING','ERROR') 
                ORDER BY chapter_number ASC
             """)) {
            ps.setLong(1, bookId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt("chapter_number"));
            }
        }
        return result;
    }
    /**
     * Delete a chapter by its ID
     */
    public void deleteChapter(long chapterId) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM chapters WHERE id=?")) {
            ps.setLong(1, chapterId);
            ps.executeUpdate();
        }
    }
    /**
     * Get list of chapter details that are PENDING or ERROR for resume
     */
    public List<ResumeChapter> listPendingOrErrorChapters(long bookId) throws SQLException {
        List<ResumeChapter> chapters = new ArrayList<>();

        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                SELECT 
                    id, chapter_number, source_filename, source_content,
                    status, beta_status, error_message, retry_count
                FROM chapters 
                WHERE book_id = ? 
                AND (status = 'PENDING' OR status = 'ERROR')
                ORDER BY chapter_number
                """)) {
                ps.setLong(1, bookId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ResumeChapter chapter = new ResumeChapter();
                        chapter.id = rs.getLong("id");
                        chapter.chapterNumber = rs.getInt("chapter_number");
                        chapter.sourceFilename = rs.getString("source_filename");
                        chapter.sourceContent = rs.getString("source_content");
                        chapter.status = rs.getString("status");
                        chapter.betaStatus = rs.getString("beta_status");
                        chapter.errorMessage = rs.getString("error_message");
                        chapter.retryCount = rs.getInt("retry_count");
                        chapters.add(chapter);
                    }
                }
            }
        }

        return chapters;
    }

    /**
     * Get all books available for resume
     */
    public List<ResumableBook> getResumableBooks() throws SQLException {
        List<ResumableBook> books = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("""
                SELECT 
                    b.id, b.title, 
                    COALESCE(b.guidelines, '') as guidelines, 
                    COALESCE(b.name_table, '') as name_table, 
                    COALESCE(b.model_used, 'gpt-4o') as model_used,
                    b.created_at, b.last_resumed_at,
                    COUNT(c.id) as total_chapters,
                    SUM(CASE WHEN c.status = 'DONE' THEN 1 ELSE 0 END) as completed_chapters,
                    SUM(CASE WHEN c.status = 'PENDING' THEN 1 ELSE 0 END) as pending_chapters,
                    SUM(CASE WHEN c.status = 'ERROR' THEN 1 ELSE 0 END) as error_chapters
                FROM books b
                LEFT JOIN chapters c ON b.id = c.book_id
                WHERE b.id IN (
                    SELECT DISTINCT book_id FROM chapters 
                    WHERE status IN ('PENDING', 'ERROR')
                )
                GROUP BY b.id
                ORDER BY b.created_at DESC
             """)) {
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ResumableBook book = new ResumableBook();
                book.id = rs.getLong("id");
                book.title = rs.getString("title");
                book.guidelines = rs.getString("guidelines");
                book.nameTable = rs.getString("name_table");
                book.modelUsed = rs.getString("model_used");
                book.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                
                Timestamp lastResumed = rs.getTimestamp("last_resumed_at");
                book.lastResumedAt = lastResumed != null ? lastResumed.toLocalDateTime() : null;
                
                book.totalChapters = rs.getInt("total_chapters");
                book.completedChapters = rs.getInt("completed_chapters");
                book.pendingChapters = rs.getInt("pending_chapters");
                book.errorChapters = rs.getInt("error_chapters");
                
                books.add(book);
            }
        }
        return books;
    }

    /**
     * Update book's last resumed timestamp
     */
    /*
    public void updateBookResumedAt(long bookId) throws SQLException {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE books SET 
                    last_resumed_at=NOW()
                WHERE id=?
                """)) {
                ps.setLong(1, bookId);
                ps.executeUpdate();
            }
        }
    }
    */

    /**
     * Update book metadata (guidelines, name table, model)
     */
    public void updateBookMetadata(long bookId, String guidelines, String nameTable, String model) throws SQLException {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE books SET 
                    guidelines=?,
                    name_table=?,
                    model_used=?
                WHERE id=?
                """)) {
                ps.setString(1, guidelines);
                ps.setString(2, nameTable);
                ps.setString(3, model);
                ps.setLong(4, bookId);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Get book details by ID
     */
    public BookDetails getBookDetails(long bookId) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("""
                SELECT id, title, 
                       COALESCE(guidelines, '') as guidelines, 
                       COALESCE(name_table, '') as name_table, 
                       COALESCE(model_used, 'gpt-4o') as model_used,
                       created_at, last_resumed_at
                FROM books WHERE id=?
             """)) {
            ps.setLong(1, bookId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                BookDetails book = new BookDetails();
                book.id = rs.getLong("id");
                book.title = rs.getString("title");
                book.guidelines = rs.getString("guidelines");
                book.nameTable = rs.getString("name_table");
                book.modelUsed = rs.getString("model_used");
                book.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                
                Timestamp lastResumed = rs.getTimestamp("last_resumed_at");
                book.lastResumedAt = lastResumed != null ? lastResumed.toLocalDateTime() : null;
                
                return book;
            }
            return null;
        }
    }

    /**
     * Helper method to create URL-friendly slug from title
     */
    private String createSlug(String title) {
        return title.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }

    // Data classes for resume functionality
    public static class ResumeChapter {
        public long id;
        public int chapterNumber;
        public String sourceFilename;
        public String sourceContent;
        public String status;
        public String betaStatus;
        public String errorMessage;
        public int retryCount;

        public boolean hasSourceContent() {
            return sourceContent != null && !sourceContent.trim().isEmpty();
        }

        @Override
        public String toString() {
            return String.format("ResumeChapter{chapter=%d, status=%s, betaStatus=%s, hasSource=%s}", 
                chapterNumber, status, betaStatus, hasSourceContent());
        }
    }

    public static class ResumableBook {
        public long id;
        public String title;
        public String guidelines;
        public String nameTable;
        public String modelUsed;
        public java.time.LocalDateTime createdAt;
        public java.time.LocalDateTime lastResumedAt;
        public int totalChapters;
        public int completedChapters;
        public int pendingChapters;
        public int errorChapters;
        
        public double getCompletionPercentage() {
            return totalChapters > 0 ? (completedChapters * 100.0 / totalChapters) : 0.0;
        }
        
        public int getIncompleteChapters() {
            return pendingChapters + errorChapters;
        }
        
        @Override
        public String toString() {
            return String.format("%s - %d/%d chapters (%.1f%% complete)", 
                title, completedChapters, totalChapters, getCompletionPercentage());
        }
    }

    public static class BookDetails {
        public long id;
        public String title;
        public String guidelines;
        public String nameTable;
        public String modelUsed;
        public java.time.LocalDateTime createdAt;
        public java.time.LocalDateTime lastResumedAt;
    }
}