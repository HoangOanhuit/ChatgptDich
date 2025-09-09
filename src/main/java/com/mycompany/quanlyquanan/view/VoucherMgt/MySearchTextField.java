/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.view.VoucherMgt;

import static com.mysql.cj.x.protobuf.MysqlxExpr.Expr.Type.PLACEHOLDER;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;

import org.w3c.dom.events.DocumentEvent;

/**
 * @author macos
 */
public class MySearchTextField extends JTextField {
    private Color backgroundColor = Color.WHITE;
    private String hint = "Tìm mã voucher...";
    private final Icon iconSearch;
    public String getHint() {
        return hint;
    }
    public void setHint(String hint) {
        this.hint = hint;
    }

    public MySearchTextField() {

        setBackground(new Color(255, 255, 255, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10)); // Giảm khoảng cách bên phải từ 50 về 10
        setFont(new java.awt.Font("Helvetica Neue", 0, 14));
        setSelectionColor(new Color(80, 199, 255));
        iconSearch = new ImageIcon(getClass().getResource("/assets/images/icons/search.png"));


    }

    @Override
    protected void paintComponent(Graphics g) {
        int height = getHeight();
        int width = getWidth();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // để làm mịn viền
        g2.setColor(backgroundColor); // Màu nền của trường văn bản
        g2.fillRoundRect(0, 0, width, height, 20, 20); // vẽ hình chữ nhật bo góc

        // Màu viền border (đổi thành màu xám)
        g2.setColor(new Color(180, 180, 180)); // màu xám
        g2.setStroke(new BasicStroke(1)); // Độ dày viền
        g2.drawRoundRect(0, 0, width - 1, height - 1, 20, 20); // vẽ viền xám bo góc

        super.paintComponent(g);

        //  Create Button
        int marginButton = 5;
        int buttonSize = height - marginButton * 2;
        g2.setPaint(new Color(248, 196, 196));
        g2.fillOval(width - height + 3, marginButton, buttonSize, buttonSize); // vẽ hình tròn bo góc

        //  Create Button Icon
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)); //  Set to default
        int marginImage = 5;
        int imageSize = buttonSize - marginImage * 2;
        Image image;

        image = ((ImageIcon) iconSearch).getImage();

        g2.drawImage(image, width - height + marginImage + 3, marginButton + marginImage, imageSize, imageSize, null);
        g2.dispose();
    }

    @Override
    public void paint(Graphics grphcs) {
        super.paint(grphcs);
        Graphics2D g2 = (Graphics2D) grphcs.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintHint(g2);
        g2.dispose(); // Giải phóng tài nguyên đồ họa
    }

    // Placeholder Text ( hint) vào trường văn bản nếu trường văn bản trống
    private void paintHint(Graphics2D g2) {
        if (getText().length() == 0) {
            int h = getHeight(); // Lấy chiều cao của trường văn bản
            Insets ins = getInsets(); // Lấy khoảng cách lề của trường văn bản
            FontMetrics fm = g2.getFontMetrics(); // Lấy thông tin phông chữ
            int c0 = getBackground().getRGB(); // Lấy màu nền của trường văn bản
            int c1 = getForeground().getRGB(); // Lấy màu chữ của trường văn bản
            int m = 0xfefefefe; //
            int c2 = ((c0 & m) >>> 1) + ((c1 & m) >>> 1); // Tính màu chữ mờ dựa trên màu nền và màu chữ
            g2.setColor(new Color(c2, true)); // Thiết lập màu chữ mờ
            g2.drawString(hint, ins.left, h / 2 + fm.getAscent() / 2 - 2); // Vẽ chuỗi hint vào trường văn bản
        }
    }

}
