package com.mycompany.quanlyquanan.view.VoucherMgt;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * @author macos
 */
public class MyTable extends JTable {
    public MyTable() {
        setShowHorizontalLines(true);
        setShowVerticalLines(false); // Chỉ line ngang
        setGridColor(new Color(220, 220, 220)); // Màu line phân cách
        setRowHeight(40);
        setShowGrid(true);


        getTableHeader().setReorderingAllowed(false);// Không cho phép sắp xếp lại cột
        getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                MyTableHeader header = new MyTableHeader(value + "");
                if (column == 4) {
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

        // Chỉ dùng màu trắng cho tất cả các dòng, không zebra striping
        TableColorUtils.applyRowColor(c, row, isCellSelected(row, column));

        return c;
    }

    public void fixTable(JScrollPane scroll) {
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setVerticalScrollBar(new JScrollBar());
        scroll.setBorder(null); // Loại bỏ viền của JScrollPane
//        this.setBorder(null); // Loại bỏ viền của JTable
        JPanel p = new JPanel();
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, p);
        scroll.setBorder(new EmptyBorder(5, 10, 5, 10));
    }

    // Method để set chiều rộng cột
    public void setColumnWidths() {
        if (getColumnModel().getColumnCount() >= 9) {
            // ID - cột hẹp
            getColumnModel().getColumn(0).setPreferredWidth(50);
            getColumnModel().getColumn(0).setMaxWidth(60);
            getColumnModel().getColumn(0).setMinWidth(40);

            // Voucher Code - cột trung bình
            getColumnModel().getColumn(1).setPreferredWidth(200);
            getColumnModel().getColumn(1).setMaxWidth(200);
            getColumnModel().getColumn(1).setMinWidth(100);

            // Description - cột rộng nhất
            getColumnModel().getColumn(2).setPreferredWidth(400);
            getColumnModel().getColumn(2).setMaxWidth(300);
            getColumnModel().getColumn(2).setMinWidth(150);

            // Percentage - cột hẹp
            getColumnModel().getColumn(3).setPreferredWidth(100);
            getColumnModel().getColumn(3).setMaxWidth(100);
            getColumnModel().getColumn(3).setMinWidth(70);

            // Quantity - cột hẹp
            getColumnModel().getColumn(4).setPreferredWidth(100);
            getColumnModel().getColumn(4).setMaxWidth(100);
            getColumnModel().getColumn(4).setMinWidth(70);

            // Start Date - cột trung bình
            getColumnModel().getColumn(5).setPreferredWidth(150);
            getColumnModel().getColumn(5).setMaxWidth(150);
            getColumnModel().getColumn(5).setMinWidth(90);

            // End Date - cột trung bình
            getColumnModel().getColumn(6).setPreferredWidth(150);
            getColumnModel().getColumn(6).setMaxWidth(150);
            getColumnModel().getColumn(6).setMinWidth(90);

            // Remaing - cột hẹp
            getColumnModel().getColumn(7).setPreferredWidth(100);
            getColumnModel().getColumn(7).setMaxWidth(100);
            getColumnModel().getColumn(7).setMinWidth(70);

            // Action - cột trung bình
            getColumnModel().getColumn(8).setPreferredWidth(150);
            getColumnModel().getColumn(8).setMaxWidth(150);
            getColumnModel().getColumn(8).setMinWidth(100);
        }
    }


    // Canh trái text trong ô
    public static TableCellRenderer getLeftRenderer(int leftPadding) {
        return new TableCellRenderer() {
            private final JLabel label = new JLabel();
            {
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setBorder(new EmptyBorder(0, leftPadding, 0, 0));
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                          boolean hasFocus, int row, int column) {
                label.setText(value == null ? "" : value.toString());
                label.setOpaque(true);
                // Sử dụng TableColorUtils thay vì hardcode màu
                TableColorUtils.applyRowColor(label, row, isSelected);
                label.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                return label;
            }
        };
    }

    // Canh phải text trong ô
    public static TableCellRenderer getRightRenderer(int rightPadding) {
        return new TableCellRenderer() {
            private final JLabel label = new JLabel();
            {
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                label.setBorder(new EmptyBorder(0, 0, 0, rightPadding));
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                          boolean hasFocus, int row, int column) {
                label.setText(value == null ? "" : value.toString());
                label.setOpaque(true);
                // Sử dụng TableColorUtils thay vì hardcode màu
                TableColorUtils.applyRowColor(label, row, isSelected);
                label.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                return label;
            }
        };
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Vẽ đường viền dưới cùng
        g.setColor(new Color(220, 220, 220));
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        // vẽ đường phân cách giữa các hàng
        g.setColor(new Color(242, 242, 242));
        for (int i = 0; i < getRowCount(); i++) {
            int y = (i + 1) * getRowHeight();
            g.drawLine(0, y - 1, getWidth(), y - 1);
        }
    }
}
