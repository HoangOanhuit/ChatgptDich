/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ChatgptDich.utils;

import com.opencsv.CSVReader;
import java.io.StringReader;
import java.util.StringJoiner;

/**
 Nhận dữ liệu CSV (hoặc TXT) và trả thành block để nhét vào prompt. */
public class CsvNameTableFormatter {
  public static String toBlock(String raw, boolean isCsv) {
    if (!isCsv) { // TXT: trả nguyên nội dung (bạn đã biên soạn dạng bảng rồi)
      return raw.trim();
    }
    try (CSVReader r = new CSVReader(new StringReader(raw))) {
      StringJoiner sj = new StringJoiner("\n");
      String[] row;
      // nếu hàng đầu là header -> giữ lại
      boolean first = true;
      while ((row = r.readNext()) != null) {
        if (row.length == 0) continue;
        String line = String.join(" | ", row);
        if (first) {
          sj.add(line);
          // gạch phân cách markdown
          StringJoiner dash = new StringJoiner(" | ");
          for (String ignored : row) dash.add("---");
          sj.add(dash.toString());
          first = false;
        } else {
          sj.add(line);
        }
      }
      return sj.toString();
    } catch (Exception e) {
      return raw.trim();
    }
  }
}