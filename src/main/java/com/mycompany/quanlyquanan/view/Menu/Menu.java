package com.mycompany.quanlyquanan.view.Menu;

import com.mycompany.quanlyquanan.model.ModelMenu;
import com.mycompany.quanlyquanan.utils.Session;
import com.mycompany.quanlyquanan.view.Employee.QuanLyNhanVienPanel;
import com.mycompany.quanlyquanan.view.Reports.TabbedPane2;
import com.mycompany.quanlyquanan.view.VoucherMgt.VoucherPanel3;
import com.mycompany.quanlyquanan.view.category.QuanLyDanhMucPanel;
import com.mycompany.quanlyquanan.view.dish.QuanLyMonAnPanel;
import com.mycompany.quanlyquanan.view.inventory.QuanLyKhoPanel;
import com.mycompany.quanlyquanan.view.material.QuanLyNguyenLieuPanel;
import com.mycompany.quanlyquanan.view.recipe.QuanLyCongThucPanel;
import com.mycompany.quanlyquanan.view.table.QuanLyBanPanel;
import com.mycompany.quanlyquanan.view.userProfile.UserProfilePanel;
import com.mycompany.quanlyquanan.view.waitingQueue.WaitingQueuePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Menu extends javax.swing.JPanel {

    private EventMenuSelected event;
    String role = Session.getInstance().getCurrentRole();

    public void addEventMenuSelected(EventMenuSelected event) {
        this.event = event;
        listMenu1.addEventMenuSelected(event);
    }

    /**
     * Creates new form Menu
     */
    public Menu() {
        initComponents();
        setOpaque(false);
        listMenu1.setOpaque(false);
        initMenuItems();
        listMenu1.setSelectedIndex(0);
    }

    private void initMenuItems() {

        switch (role) {
            case "Admin":
                listMenu1.addItem(new ModelMenu("table", "Quản Lý Bàn"));
                listMenu1.addItem(new ModelMenu("order1", "Danh Sách Chờ"));
                listMenu1.addItem(new ModelMenu("pos", "POS"));
                listMenu1.addItem(new ModelMenu("order", "Quản lý Đơn hàng"));
                listMenu1.addItem(new ModelMenu("cook", "Quản Lý Bếp"));
                listMenu1.addItem(new ModelMenu("category", "Danh Mục"));
                listMenu1.addItem(new ModelMenu("dishes1", "Món Ăn"));
                listMenu1.addItem(new ModelMenu("recipe", "Công Thức"));
                listMenu1.addItem(new ModelMenu("dishes", "Nguyên Liệu"));
                listMenu1.addItem(new ModelMenu("inventory1", "Kho Hàng"));
                listMenu1.addItem(new ModelMenu("voucher1", "Khuyến Mãi"));
                listMenu1.addItem(new ModelMenu("staff", "Nhân Viên"));
                listMenu1.addItem(new ModelMenu("user1", "Hồ Sơ Cá Nhân"));
                listMenu1.addItem(new ModelMenu("dashboard", "Báo Cáo Tổng Quan"));
                listMenu1.addItem(new ModelMenu("signout", "Thoát"));
                break;
            case "Nhân Viên":
                listMenu1.addItem(new ModelMenu("table", "Quản Lý Bàn"));
                listMenu1.addItem(new ModelMenu("order1", "Danh Sách Chờ"));
                listMenu1.addItem(new ModelMenu("pos", "POS"));
                listMenu1.addItem(new ModelMenu("order", "Quản lý Đơn hàng"));
                listMenu1.addItem(new ModelMenu("user1", "Hồ Sơ Cá Nhân"));
                listMenu1.addItem(new ModelMenu("signout", "Thoát"));
                break;
            case "Bếp":
                listMenu1.addItem(new ModelMenu("cook", "Quản Lý Bếp"));
                listMenu1.addItem(new ModelMenu("recipe", "Công Thức"));
                listMenu1.addItem(new ModelMenu("dishes", "Nguyên Liệu"));
                listMenu1.addItem(new ModelMenu("user1", "Hồ Sơ Cá Nhân"));
                listMenu1.addItem(new ModelMenu("signout", "Thoát"));
                break;
            case "Thu Ngân":
                listMenu1.addItem(new ModelMenu("pos", "POS"));
                listMenu1.addItem(new ModelMenu("order", "Quản lý Đơn hàng"));
                listMenu1.addItem(new ModelMenu("user1", "Hồ Sơ Cá Nhân"));
                listMenu1.addItem(new ModelMenu("signout", "Thoát"));
                break;
            default:
                throw new IllegalArgumentException("Role không hợp lệ: " + role);
        }

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMoving = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        listMenu1 = new com.mycompany.quanlyquanan.view.Menu.ListMenu<>();

        setPreferredSize(new java.awt.Dimension(260, 800));

        panelMoving.setBackground(new java.awt.Color(204, 204, 255));
        panelMoving.setOpaque(false);
        panelMoving.setPreferredSize(new java.awt.Dimension(260, 90));

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/restaurant96_1.png"))); // NOI18N
        jLabel1.setText("Restaurant ");

        javax.swing.GroupLayout panelMovingLayout = new javax.swing.GroupLayout(panelMoving);
        panelMoving.setLayout(panelMovingLayout);
        panelMovingLayout.setHorizontalGroup(
            panelMovingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMovingLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelMovingLayout.setVerticalGroup(
            panelMovingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMovingLayout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        listMenu1.setPreferredSize(new java.awt.Dimension(260, 700));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMoving, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(listMenu1, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelMoving, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(listMenu1, javax.swing.GroupLayout.DEFAULT_SIZE, 1226, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @Override
    protected void paintChildren(java.awt.Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //https://uigradients.com/#SublimeLight
        GradientPaint gp = new GradientPaint(0, 0, Color.decode("#FC5C7D"), 0, getHeight(), Color.decode("#6A82FB"));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight()); // tô màu nền
//        g2.fillRo undRect(0, 0, getWidth(), getHeight(), 15, 15); // vẽ hình chữ nhật bo góc
        super.paintChildren(g);
    }

    // Dùng để di chuyển frame thay cho menubar mặc định của JFrame (khi setUndecorated(true))
    private int x, y;

    public void initMoving(JFrame frame) {
        panelMoving.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                x = evt.getX();
                y = evt.getY();
            }
        });
        panelMoving.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent evt) {
                frame.setLocation(frame.getX() + evt.getX() - x, frame.getY() + evt.getY() - y);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private com.mycompany.quanlyquanan.view.Menu.ListMenu<String> listMenu1;
    private javax.swing.JPanel panelMoving;
    // End of variables declaration//GEN-END:variables

}
