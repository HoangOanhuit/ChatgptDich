/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ChatgptDich.utils;

public class PromptBuilder {
  public static String build(String guidelines, String nameTableBlock, String sourceText) {
    // Một prompt "thuần văn bản" cho Responses API (model text-out).
    // Bạn có thể tách thành system/user nếu muốn dùng cấu trúc vai trò – nhưng kiểu chuỗi gộp vẫn OK.
    return """
      [ROLE: system]
      Bạn là dịch giả tiếng Việt. Hãy dịch sát nghĩa, mạch lạc, tuân thủ nghiêm ngặt quy tắc sau.

      [YÊU CẦU DỊCH]
      %s

      [BẢNG TÊN & XƯNG HÔ]
      %s

      [ĐỊNH DẠNG ĐẦU RA]
      Trả về CHỈ NỘI DUNG DỊCH (plain text). Không thêm chú thích, không tiêu đề.

      [ROLE: user]
      %s
      """.formatted(guidelines, nameTableBlock, sourceText);
  }
}