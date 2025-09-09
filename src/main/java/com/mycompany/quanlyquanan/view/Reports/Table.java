package com.mycompany.quanlyquanan.view.Reports;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 *
 * @author macos
 */
public class Table extends JTable {
    public Table() {
        setShowHorizontalLines(true);
        setGridColor(new Color(230, 230, 230)); // Màu lưới
        setRowHeight(30); // Chiều cao dòng
        getTableHeader().setReorderingAllowed(false);// Không cho phép sắp xếp lại cột
        getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                TableHeader header = new TableHeader(value + "");
                if(column==4){
                    header.setHorizontalAlignment(JLabel.CENTER);
                }
                return header;
            }
        });
        setBorder(BorderFactory.createEmptyBorder()); // Không viền
    }

    @Override
    public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (!isCellSelected(row, column)) {
            if (row % 2 == 0) {
                c.setBackground(Color.WHITE);
            } else {
                c.setBackground(new Color(250, 249, 249)); // Màu xám nhạt
            }
        } else {
            c.setBackground(getSelectionBackground());
        }
        return c;
    }
    public void fixTable(JScrollPane scroll){
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setVerticalScrollBar(new JScrollBar());
        scroll.setBorder(null); // Loại bỏ viền của JScrollPane
        JPanel p = new JPanel(); //
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, p);
        scroll.setBorder(new EmptyBorder(5, 10, 5, 10));
    }

}
