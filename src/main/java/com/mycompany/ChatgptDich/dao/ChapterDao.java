/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ChatgptDich.dao;

import javax.sql.DataSource;
import java.sql.*;

public class ChapterDao {
  private final DataSource ds;
  public ChapterDao(DataSource ds) { this.ds = ds; }

  public long ensureBook(String title) throws SQLException {
    try (Connection c = ds.getConnection()) {
      // thử tìm
      try (PreparedStatement ps = c.prepareStatement("SELECT id FROM books WHERE title=?")) {
        ps.setString(1, title);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getLong(1);
      }
      // chưa có thì tạo
      try (PreparedStatement ps = c.prepareStatement("INSERT INTO books(title) VALUES(?)", Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, title);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        return rs.getLong(1);
      }
    }
  }

  public void upsertPending(long bookId, int chNo, String fileName) throws SQLException {
    try (Connection c = ds.getConnection()) {
      try (PreparedStatement ps = c.prepareStatement("""
          INSERT INTO chapters(book_id, chapter_number, chapter_title, source_filename, status)
          VALUES (?, ?, NULL, ?, 'PENDING')
          ON DUPLICATE KEY UPDATE source_filename=VALUES(source_filename), status='PENDING'
          """)) {
        ps.setLong(1, bookId); ps.setInt(2, chNo); ps.setString(3, fileName);
        ps.executeUpdate();
      }
    }
  }

  public void updateDone(long bookId, int chNo, String content, int wordCount) throws SQLException {
    try (Connection c = ds.getConnection()) {
      try (PreparedStatement ps = c.prepareStatement("""
          UPDATE chapters SET content=?, word_count=?, status='DONE', error_message=NULL
          WHERE book_id=? AND chapter_number=?
          """)) {
        ps.setString(1, content); ps.setInt(2, wordCount);
        ps.setLong(3, bookId); ps.setInt(4, chNo);
        ps.executeUpdate();
      }
    }
  }

  public void updateError(long bookId, int chNo, String err) throws SQLException {
    try (Connection c = ds.getConnection()) {
      try (PreparedStatement ps = c.prepareStatement("""
          UPDATE chapters SET status='ERROR', error_message=? WHERE book_id=? AND chapter_number=?
          """)) {
        ps.setString(1, err); ps.setLong(2, bookId); ps.setInt(3, chNo);
        ps.executeUpdate();
      }
    }
  }

  public java.util.List<Integer> listPendingOrError(long bookId) throws SQLException {
    java.util.ArrayList<Integer> out = new java.util.ArrayList<>();
    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement("""
            SELECT chapter_number FROM chapters
            WHERE book_id=? AND status IN ('PENDING','ERROR') ORDER BY chapter_number ASC
         """)) {
      ps.setLong(1, bookId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) out.add(rs.getInt(1));
    }
    return out;
  }
}