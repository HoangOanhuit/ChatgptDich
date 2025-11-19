package com.mycompany.quanlytruyen.dao;

import com.mycompany.quanlytruyen.model.Post;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO thao tác với bảng posts (lịch đăng chương).
 */
public class PostDao {
    private final DataSource dataSource;

    public PostDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Post> findByAccountId(long accountId) throws SQLException {
        String sql = """
            SELECT id, account_id, chapter_order, hour, minute, date_schedule
            FROM posts
            WHERE account_id = ?
            ORDER BY chapter_order ASC
            """;
        List<Post> posts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapRow(rs));
                }
            }
        }
        return posts;
    }

    public void replaceAccountPosts(long accountId, List<Post> posts) throws SQLException {
        String deleteSql = "DELETE FROM posts WHERE account_id = ?";
        String insertSql = """
            INSERT INTO posts (account_id, chapter_order, hour, minute, date_schedule)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement deletePs = conn.prepareStatement(deleteSql);
             PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
            conn.setAutoCommit(false);
            try {
                deletePs.setLong(1, accountId);
                deletePs.executeUpdate();

                if (posts != null) {
                    for (Post post : posts) {
                        insertPs.setLong(1, accountId);
                        insertPs.setInt(2, post.getChapterOrder());
                        insertPs.setInt(3, post.getHour());
                        insertPs.setInt(4, post.getMinute());
                        if (post.getDateSchedule() != null) {
                            insertPs.setTimestamp(5, Timestamp.valueOf(post.getDateSchedule()));
                        } else {
                            insertPs.setTimestamp(5, null);
                        }
                        insertPs.addBatch();
                    }
                    insertPs.executeBatch();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Post mapRow(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getLong("id"));
        long accountId = rs.getLong("account_id");
        if (!rs.wasNull()) {
            post.setAccountId(accountId);
        }
        post.setChapterOrder(rs.getInt("chapter_order"));
        post.setHour(rs.getInt("hour"));
        post.setMinute(rs.getInt("minute"));
        Timestamp ts = rs.getTimestamp("date_schedule");
        if (ts != null) {
            post.setDateSchedule(ts.toLocalDateTime());
        }
        return post;
    }
}