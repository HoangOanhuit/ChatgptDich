package com.mycompany.quanlyquanan.view.Menu;

import com.mycompany.quanlyquanan.model.ModelMenu;
import com.mycompany.quanlyquanan.utils.Session;
import com.mycompany.quanlyquanan.view.Employee.QuanLyNhanVienPanel;
import com.mycompany.quanlyquanan.view.Order.KitchenPanel;
import com.mycompany.quanlyquanan.view.Order.OrderPanel;
import com.mycompany.quanlyquanan.view.Order.POSPanel;
import com.mycompany.quanlyquanan.view.Reports.ReportPanel;
import com.mycompany.quanlyquanan.view.Reports.TabbedPane2;

import java.awt.BorderLayout;

import com.mycompany.quanlyquanan.view.VoucherMgt.VoucherPanel3;
import com.mycompany.quanlyquanan.view.category.QuanLyDanhMucPanel;
import com.mycompany.quanlyquanan.view.dish.QuanLyMonAnPanel;
import com.mycompany.quanlyquanan.view.inventory.QuanLyKhoPanel;
import com.mycompany.quanlyquanan.view.material.QuanLyNguyenLieuPanel;
import com.mycompany.quanlyquanan.view.recipe.QuanLyCongThucPanel;
import com.mycompany.quanlyquanan.view.table.QuanLyBanPanel;
import com.mycompany.quanlyquanan.view.userProfile.UserProfilePanel;
import com.mycompany.quanlyquanan.view.waitingQueue.WaitingQueuePanel;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import net.miginfocom.swing.MigLayout;

import java.awt.event.MouseAdapter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

public class OpenMenu extends javax.swing.JFrame {

    private final MigLayout layout;
    private final MainForm mainForm;
    private final MenuLayout menuLayout;
    private String role = Session.getInstance().getCurrentRole();

    private final Map<Integer, JPanel> panelCache = new HashMap<>(); // Thêm dòng này

    public OpenMenu() {
        initComponents();
        IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
        layout = new MigLayout("fill", "0[fill]0", "0[fill]0");
        mainForm = new MainForm();
        menuLayout = new MenuLayout();
        initLayout();

    }

    private void initLayout() {
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(mainForm);
        JPanel glassPanel = new JPanel(layout);

        glassPanel.setOpaque(false);
        glassPanel.add(menuLayout, "pos 0 0 200 400"); //
        setGlassPane(glassPanel);
        glassPanel.setVisible(true);

        menuLayout.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
            }
        });

        menuLayout.getMenu().addEventMenuSelected(new EventMenuSelected() {
            @Override
            public void selected(int index) {

                // Xử lý các trường hợp đặc biệt không cần panel như "Thoát"
                if (isExitOption(index)) { // Tạo một hàm isExitOption để kiểm tra cho gọn
                    System.exit(0);
                    return;
                }

                // Kiểm tra xem panel đã có trong cache chưa
                JPanel panelToShow = panelCache.get(index);

                if (panelToShow == null) {
                    // Nếu chưa có, tạo mới panel dựa trên index
                    System.out.println("Creating panel for index: " + index); // In ra để kiểm tra
                    panelToShow = createPanelForIndex(index);

                    // Lưu panel vừa tạo vào cache để dùng lại lần sau
                    if (panelToShow != null) {
                        panelCache.put(index, panelToShow);
                    }
                } else {
                    System.out.println("Reusing panel for index: " + index); // In ra để kiểm tra
                }

                // Hiển thị panel
                if (panelToShow != null) {
                    mainForm.show(panelToShow);
                }
            }
        });

    }

    private boolean isExitOption(int index) {
        return switch (role) {
            case "Admin" ->
                index == 14;
            case "Nhân Viên" ->
                index == 5;
            case "Thu Ngân" ->
                index == 3;
            case "Bếp" ->
                index == 4;
            default ->
                false;
        };
    }

    private JPanel createPanelForIndex(int index) {
        return switch (role) {
            case "Admin" ->
                switch (index) {
                    case 0 ->
                        new QuanLyBanPanel();
                    case 1 ->
                        new WaitingQueuePanel();
                    case 2 ->
                        new POSPanel();
                    case 3 -> 
                        new OrderPanel();
                    case 4 ->
                        new KitchenPanel(); 
                    case 5 ->
                        new QuanLyDanhMucPanel();
                    case 6 ->
                        new QuanLyMonAnPanel();
                    case 7 ->
                        new QuanLyCongThucPanel();
                    case 8 ->
                        new QuanLyNguyenLieuPanel();
                    case 9 ->
                        new QuanLyKhoPanel();
                    case 10 ->
                        new VoucherPanel3();
                    case 11 ->
                        new QuanLyNhanVienPanel();
                    case 12 ->
                        new UserProfilePanel();
                    case 13 -> 
                        new ReportPanel();
                    default ->
                        null;
                };
            case "Nhân Viên" ->
                switch (index) {
                    case 0 ->
                        new QuanLyBanPanel();
                    case 1 ->
                        new WaitingQueuePanel();
                    case 2 ->
                        new POSPanel();
                    case 3 ->
                        new OrderPanel();
                    case 4 ->
                        new UserProfilePanel();
                    default ->
                        null;
                };
            case "Bếp" ->
                switch (index) {
                    case 0 ->
                        new KitchenPanel();
                    case 1 ->
                        new QuanLyCongThucPanel();
                    case 2 ->
                        new QuanLyNguyenLieuPanel();
                    case 3 ->
                        new UserProfilePanel();
                    default ->
                        null;
                };
            case "Thu Ngân" ->
                switch (index) {
                    case 0 ->
                        new POSPanel();
                    case 1 ->
                        new OrderPanel();
                    case 2 ->
                        new UserProfilePanel();
                    default ->
                        null;
                };
            default ->
                null;
        };
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JLayeredPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1350, 850));

        mainPanel.setBackground(new java.awt.Color(250, 250, 250));
        mainPanel.setOpaque(true);
        mainPanel.setPreferredSize(new java.awt.Dimension(1350, 850));

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
                mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 1300, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
                mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 930, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 930, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(OpenMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(OpenMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(OpenMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(OpenMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new OpenMenu().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLayeredPane mainPanel;
    // End of variables declaration//GEN-END:variables
}
