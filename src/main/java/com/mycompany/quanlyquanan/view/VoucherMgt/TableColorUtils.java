package com.mycompany.quanlyquanan.view.VoucherMgt;

import java.awt.Color;
import java.awt.Component;

/**
 * Utility class để quản lý màu sắc của table một cách consistent
 */
public class TableColorUtils {

    // Định nghĩa màu sắc cho table
    public static final Color EVEN_ROW_COLOR = Color.WHITE;
    public static final Color ODD_ROW_COLOR = new Color(245, 245, 245);
    // chọn màu mặc định cho dòng được chọn giống các table khác
    public static final Color SELECTED_ROW_COLOR = new Color(248, 196, 196);
//    public static final Color SELECTED_ROW_COLOR = new Color(239, 88, 114, 255);
    public static final Color HOVER_ROW_COLOR = new Color(248, 196, 196);

    /**
     * Áp dụng màu sắc cho component dựa trên trạng thái của row
     * @param component Component cần set màu
     * @param row Chỉ số row
     * @param isSelected Row có được chọn không
     * @param isHovered Row có được hover không (optional)
     */
    public static void applyRowColor(Component component, int row, boolean isSelected, boolean isHovered) {
        if (isSelected) {
            component.setBackground(SELECTED_ROW_COLOR);

        } else {
            // Chỉ dùng màu trắng cho tất cả các dòng
            component.setBackground(EVEN_ROW_COLOR); // Color.WHITE
            // Chỉ setOpaque nếu là JLabel để tránh lỗi với các component khác
            if (component instanceof javax.swing.JLabel) {
                ((javax.swing.JLabel) component).setOpaque(true);
            }
        }
    }

    /**
     * Áp dụng màu sắc cơ bản (không có hover)
     */
    public static void applyRowColor(Component component, int row, boolean isSelected) {
        applyRowColor(component, row, isSelected, false);
    }

    /**
     * Lấy màu nền cho row dựa trên trạng thái
     */
    public static Color getRowColor(int row, boolean isSelected, boolean isHovered) {
        if (isSelected) {
            return SELECTED_ROW_COLOR;
        } else if (isHovered) {
            return HOVER_ROW_COLOR;
        } else {
            return (row % 2 == 0) ? EVEN_ROW_COLOR : ODD_ROW_COLOR;
        }
    }

    /**
     * Lấy màu nền cho row dựa trên trạng thái (không có hover)
     */
    public static Color getRowColor(int row, boolean isSelected) {
        return getRowColor(row, isSelected, false);
    }
}
