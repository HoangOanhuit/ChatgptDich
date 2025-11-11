package com.mycompany.quanlytruyen.dao;

import com.mycompany.quanlytruyen.model.Book;
import com.mycompany.quanlytruyen.model.Book.*;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced BookDao with new status management and revenue tracking
 */
public class BookDao {
    private final DataSource dataSource;
    
    public BookDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Get all books with enhanced information
     */
    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = """
            SELECT id, title, author, slug, created_at,
                   raw_status, translate_status, post_status, total_revenue,
                   guidelines, name_table, model_used, last_resumed_at,
                                      account_id, short_title, posted, price
                               FROM books 
            ORDER BY created_at DESC
            """;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        }
        return books;
    }
    
    /**
     * Get book by ID
     */
    public Book getBookById(long bookId) throws SQLException {
        String sql = """
            SELECT id, title, author, slug, created_at,
                   raw_status, translate_status, post_status, total_revenue,
                   guidelines, name_table, model_used, last_resumed_at,
                                      account_id, short_title, posted, price
                               FROM books
            WHERE id = ?
            """;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Create new book
     */
    public long createBook(Book book) throws SQLException {
        String sql = """
            INSERT INTO books (title, author, slug, raw_status, translate_status,
                             post_status, total_revenue, guidelines, name_table,
                             model_used, account_id, short_title, posted, price,
                             created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
            """;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getSlug() != null ? book.getSlug() : createSlug(book.getTitle()));
            ps.setString(4, book.getRawStatus().getValue());
            ps.setString(5, book.getTranslateStatus().getValue());
            ps.setString(6, book.getPostStatus().getValue());
            ps.setBigDecimal(7, book.getTotalRevenue());
            ps.setString(8, book.getGuidelines());
            ps.setString(9, book.getNameTable());
            ps.setString(10, book.getModelUsed());

            if (book.getAccountId() != null) {
                ps.setLong(11, book.getAccountId());
            } else {
                ps.setNull(11, Types.BIGINT);
            }
            ps.setString(12, book.getShortTitle());
            if (book.getPosted() != null) {
                ps.setInt(13, book.getPosted());
            } else {
                ps.setNull(13, Types.INTEGER);
            }
            if (book.getPrice() != null) {
                ps.setBigDecimal(14, book.getPrice());
            } else {
                ps.setNull(14, Types.DECIMAL);
            }
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    book.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to create book, no ID obtained");
    }
    
    /**
     * Update existing book
     */
    public void updateBook(Book book) throws SQLException {
        String sql = """
            UPDATE books SET
                title = ?, author = ?, raw_status = ?, translate_status = ?,
                post_status = ?, total_revenue = ?, guidelines = ?, 
                post_status = ?, total_revenue = ?, guidelines = ?,
                name_table = ?, model_used = ?, account_id = ?,
                short_title = ?, posted = ?, price = ?
            WHERE id = ?
            """;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getRawStatus().getValue());
            ps.setString(4, book.getTranslateStatus().getValue());
            ps.setString(5, book.getPostStatus().getValue());
            ps.setBigDecimal(6, book.getTotalRevenue());
            ps.setString(7, book.getGuidelines());
            ps.setString(8, book.getNameTable());
            ps.setString(9, book.getModelUsed());
            if (book.getAccountId() != null) {
                ps.setLong(10, book.getAccountId());
            } else {
                ps.setNull(10, Types.BIGINT);
            }
            ps.setString(11, book.getShortTitle());
            if (book.getPosted() != null) {
                ps.setInt(12, book.getPosted());
            } else {
                ps.setNull(12, Types.INTEGER);
            }
            if (book.getPrice() != null) {
                ps.setBigDecimal(13, book.getPrice());
            } else {
                ps.setNull(13, Types.DECIMAL);
            }
            ps.setLong(14, book.getId());
            
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Book not found with id: " + book.getId());
            }
        }
    }
    
    /**
     * Update book status
     */
    public void updateBookStatus(long bookId, RawStatus rawStatus, 
                               TranslateStatus translateStatus, PostStatus postStatus) throws SQLException {
        String sql = """
            UPDATE books SET 
                raw_status = ?, translate_status = ?, post_status = ?
            WHERE id = ?
            """;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, rawStatus.getValue());
            ps.setString(2, translateStatus.getValue());
            ps.setString(3, postStatus.getValue());
            ps.setLong(4, bookId);
            
            ps.executeUpdate();
        }
    }
    
    /**
     * Update book revenue
     */
    public void updateBookRevenue(long bookId, BigDecimal newRevenue) throws SQLException {
        updateBookRevenue(bookId, newRevenue, null);
    }
    
    /**
     * Update book revenue with reason tracking
     */
    public void updateBookRevenue(long bookId, BigDecimal newRevenue, String reason) throws SQLException {
        String selectSql = "SELECT total_revenue FROM books WHERE id = ?";
        String updateSql = "UPDATE books SET total_revenue = ? WHERE id = ?";
        String historySql = """
            INSERT INTO revenue_history (book_id, old_revenue, new_revenue, change_reason, changed_by)
            VALUES (?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            BigDecimal oldRevenue = BigDecimal.ZERO;
            
            // Get current revenue
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setLong(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        oldRevenue = rs.getBigDecimal("total_revenue");
                        if (oldRevenue == null) oldRevenue = BigDecimal.ZERO;
                    }
                }
            }
            
            // Update revenue
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setBigDecimal(1, newRevenue);
                ps.setLong(2, bookId);
                ps.executeUpdate();
            }
            
            // Record history
            try (PreparedStatement ps = conn.prepareStatement(historySql)) {
                ps.setLong(1, bookId);
                ps.setBigDecimal(2, oldRevenue);
                ps.setBigDecimal(3, newRevenue);
                ps.setString(4, reason != null ? reason : "Manual update");
                ps.setString(5, System.getProperty("user.name", "system"));
                ps.executeUpdate();
            }
            
            conn.commit();
        } catch (SQLException e) {
            try (Connection conn = dataSource.getConnection()) {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw e;
        }
    }
    
    /**
     * Delete book (with confirmation)
     */
    public void deleteBook(long bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, bookId);
            int affected = ps.executeUpdate();
            
            if (affected == 0) {
                throw new SQLException("Book not found with id: " + bookId);
            }
        }
    }
    
    /**
     * Get books by status
     */
    public List<Book> getBooksByStatus(RawStatus rawStatus, TranslateStatus translateStatus, 
                                     PostStatus postStatus) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT id, title, author, slug, created_at,
                   raw_status, translate_status, post_status, total_revenue,
                   guidelines, name_table, model_used, last_resumed_at
            FROM books WHERE 1=1
            """);
        
        List<Object> params = new ArrayList<>();
        
        if (rawStatus != null) {
            sql.append(" AND raw_status = ?");
            params.add(rawStatus.getValue());
        }
        if (translateStatus != null) {
            sql.append(" AND translate_status = ?");
            params.add(translateStatus.getValue());
        }
        if (postStatus != null) {
            sql.append(" AND post_status = ?");
            params.add(postStatus.getValue());
        }
        
        sql.append(" ORDER BY created_at DESC");
        
        List<Book> books = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        }
        
        return books;
    }
    
    /**
     * Get revenue statistics
     */
    public RevenueStatistics getRevenueStatistics() throws SQLException {
        String sql = """
            SELECT 
                COUNT(*) as total_books,
                SUM(total_revenue) as total_revenue,
                AVG(total_revenue) as avg_revenue,
                MAX(total_revenue) as max_revenue,
                SUM(CASE WHEN translate_status = 'hoan' THEN total_revenue ELSE 0 END) as completed_revenue,
                COUNT(CASE WHEN translate_status = 'hoan' THEN 1 END) as completed_books
            FROM books
            """;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                RevenueStatistics stats = new RevenueStatistics();
                stats.totalBooks = rs.getInt("total_books");
                stats.totalRevenue = rs.getBigDecimal("total_revenue");
                stats.avgRevenue = rs.getBigDecimal("avg_revenue");
                stats.maxRevenue = rs.getBigDecimal("max_revenue");
                stats.completedRevenue = rs.getBigDecimal("completed_revenue");
                stats.completedBooks = rs.getInt("completed_books");
                
                // Handle nulls
                if (stats.totalRevenue == null) stats.totalRevenue = BigDecimal.ZERO;
                if (stats.avgRevenue == null) stats.avgRevenue = BigDecimal.ZERO;
                if (stats.maxRevenue == null) stats.maxRevenue = BigDecimal.ZERO;
                if (stats.completedRevenue == null) stats.completedRevenue = BigDecimal.ZERO;
                
                return stats;
            }
        }
        return new RevenueStatistics();
    }
    
    /**
     * Map ResultSet to Book object
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setSlug(rs.getString("slug"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            book.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // Status fields
        book.setRawStatus(RawStatus.fromString(rs.getString("raw_status")));
        book.setTranslateStatus(TranslateStatus.fromString(rs.getString("translate_status")));
        book.setPostStatus(PostStatus.fromString(rs.getString("post_status")));
        
        // Revenue
        book.setTotalRevenue(rs.getBigDecimal("total_revenue"));
        
        // Translation metadata
        book.setGuidelines(rs.getString("guidelines"));
        book.setNameTable(rs.getString("name_table"));
        book.setModelUsed(rs.getString("model_used"));
        

        Timestamp lastResumed = rs.getTimestamp("last_resumed_at");
        if (lastResumed != null) {
            book.setLastResumedAt(lastResumed.toLocalDateTime());
        }
        

        long accountId = rs.getLong("account_id");
        if (!rs.wasNull()) {
            book.setAccountId(accountId);
        }
        book.setShortTitle(rs.getString("short_title"));

        int posted = rs.getInt("posted");
        if (rs.wasNull()) {
            book.setPosted(null);
        } else {
            book.setPosted(posted);
        }
        book.setPrice(rs.getBigDecimal("price"));

        return book;
    }
    /**
     * Create URL-friendly slug from title
     */
    private String createSlug(String title) {
        if (title == null) return "";
        return title.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }
    
    /**
     * Revenue statistics data class
     */
    public static class RevenueStatistics {
        public int totalBooks;
        public BigDecimal totalRevenue = BigDecimal.ZERO;
        public BigDecimal avgRevenue = BigDecimal.ZERO;
        public BigDecimal maxRevenue = BigDecimal.ZERO;
        public BigDecimal completedRevenue = BigDecimal.ZERO;
        public int completedBooks;
        
        public String getFormattedTotalRevenue() {
            return String.format("%,d VNĐ", totalRevenue.longValue());
        }
        
        public String getFormattedAvgRevenue() {
            return String.format("%,d VNĐ", avgRevenue.longValue());
        }
        
        public double getCompletionRate() {
            return totalBooks > 0 ? (completedBooks * 100.0 / totalBooks) : 0.0;
        }
    }
}