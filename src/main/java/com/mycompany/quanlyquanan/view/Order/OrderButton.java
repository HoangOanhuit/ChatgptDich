/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.quanlyquanan.view.Order;

import com.mycompany.quanlyquanan.controller.DishController;
import com.mycompany.quanlyquanan.controller.MaterialController;
import com.mycompany.quanlyquanan.controller.OrderController;
import com.mycompany.quanlyquanan.controller.OrderItemController;
import com.mycompany.quanlyquanan.controller.RecipeController;
import com.mycompany.quanlyquanan.controller.TableController;
import com.mycompany.quanlyquanan.model.Dish;
import com.mycompany.quanlyquanan.model.Material;
import com.mycompany.quanlyquanan.model.Order;
import com.mycompany.quanlyquanan.model.OrderItem;
import com.mycompany.quanlyquanan.model.Recipe;
import com.mycompany.quanlyquanan.model.Table;
import com.mycompany.quanlyquanan.utils.RedisPublisher;
import com.mycompany.quanlyquanan.utils.Session;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Tyler
 */
public class OrderButton extends javax.swing.JPanel {

    /**
     * Creates new form NewJPanel
     */
    private int orderId;
    boolean isAdmin = Session.getInstance().isAdmin();
    
    public OrderButton() {
        initComponents();
    }
    
    public OrderButton(Order o, JPanel pane, List<Order> canceledList, List<Order> servedList, JTable oTable, String place) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 250));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 2, true));

        // 🔹 oTitle: Top section
        oTitle = new JPanel();
        oTitle.setLayout(new BoxLayout(oTitle, BoxLayout.Y_AXIS));
        oTitle.setBackground(new Color(0, 51, 102)); // Dark blue
        oTitle.setOpaque(true);
        oTitle.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        oTitle.setPreferredSize(new Dimension(200, 80)); // Fixed height

        JLabel orderId = new JLabel("Order: " + o.getId());
        JLabel created = new JLabel("Created: " + o.getCreated_at());
        TableController tc = new TableController();
        Table t = tc.getTableById(o.getTable_id());
        JLabel table = new JLabel("Table: " + (o.getTable_id() == 0 ? "Mang về" : t.getName()));

        for (JLabel label : new JLabel[]{orderId, created, table}) {
            label.setForeground(Color.WHITE);
            label.setFont(new Font("SansSerif", Font.PLAIN, 14));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            oTitle.add(label);
            oTitle.add(Box.createVerticalStrut(5));
        }

        // 🔹 oInfo: Scrollable middle section
        oInfo = new JPanel();
        oInfo.setLayout(new BoxLayout(oInfo, BoxLayout.Y_AXIS));
        oInfo.setBackground(Color.WHITE);
        oInfo.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        OrderItemController oic = new OrderItemController();
        List<OrderItem> items = oic.getByOrderId(o.getId());
        DishController dc = new DishController();

        for (OrderItem oi : items) {
            Dish d = dc.getById(oi.getDish_id());
            JLabel dishLabel = new JLabel(d.getName() + " × " + oi.getQuantity());
            dishLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            dishLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            oInfo.add(dishLabel);
        }

        JScrollPane infoScroll = new JScrollPane(oInfo);
        infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroll.setBorder(null);
        infoScroll.setPreferredSize(new Dimension(200, 120)); // Adjust height as needed
        
        infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        infoScroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        infoScroll.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
        // 🔹 oBtn: Bottom button
        oBtn = new JButton("Cancel");
        oBtn.setBackground(Color.RED);
        oBtn.setForeground(Color.WHITE);
        oBtn.setFocusPainted(false);
        oBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        oFooter = new JPanel();
        oFooter.setLayout(new BoxLayout(oFooter, BoxLayout.Y_AXIS));
        oFooter.setBackground(Color.WHITE);
        oFooter.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        setUpOFooter(o, pane, canceledList, servedList, oTable, place);
        // 🔹 Assemble layout
        add(oTitle, BorderLayout.NORTH);
        add(infoScroll, BorderLayout.CENTER);
        add(oFooter, BorderLayout.SOUTH);
        
        this.orderId = o.getId();
    }

    public int getOrderId() {
        return orderId;
    }
    
    
    private void setUpOFooter(Order o, JPanel pane, List<Order> canceledList, List<Order> servedList, JTable oTable, String place){
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        statusPanel.setOpaque(false);
        oBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        oStatus = new JLabel("Status: " + o.getStatus());
        oStatus.setHorizontalAlignment(SwingConstants.RIGHT);
        oStatus.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        switch (o.getStatus().toLowerCase()) {
        case "new":
            oStatus.setForeground(new Color(0, 102, 204)); // Blue
            oBtn.setBackground(Color.red);
            oBtn.addActionListener(e -> {
                cancelOrder(o, pane, canceledList, oTable, place);
            });
            
            break;
        case "preparing":
            oStatus.setForeground(new Color(255, 153, 0));
            oBtn.setBackground(Color.red);
            oBtn.addActionListener(e -> {
                cancelOrder(o, pane, canceledList, oTable, place);
            });// Orange
            
            break;
        case "done":
            oStatus.setForeground(new Color(0, 153, 0)); // Green
            oBtn.setBackground(Color.GREEN);
            oBtn.setText("Serve");
            oBtn.addActionListener(e -> {
                serveOrder(o, pane, servedList, oTable);
            });
            if(place.equals("kitchen"))
                oBtn.setVisible(false);
            break;
        case "served":
            oStatus.setForeground(new Color(102, 0, 204));
            oBtn.setVisible(true);// Purple
            oBtn.setBackground(new Color(102, 0, 204));
            oBtn.setText("Pay");
            oBtn.addActionListener(e -> {
                DishController dc = new DishController();
                Map<Dish, Integer> dishMap = new HashMap<>();
                OrderItemController oic = new OrderItemController();
                List<OrderItem> ol = oic.getByOrderId(o.getId());
                for(OrderItem oi : ol) {
                    Dish d = dc.getById(oi.getDish_id());
                    dishMap.put(d, oi.getQuantity());
                }
                showPaymentConfirmationDialog(o.getId(), dishMap, o, pane, servedList);
                
            });
            break;
        case "canceled":
            oStatus.setForeground(Color.RED);
            oBtn.setVisible(false);
            revalidate();
            repaint();
            break;
        default:
            oStatus.setForeground(Color.GRAY); // Unknown status
            break;
    }
        statusPanel.add(oStatus);
        oFooter.add(statusPanel);
        oFooter.add(oBtn);
    };
    private void showPaymentConfirmationDialog(int orderId, Map<Dish, Integer> dishMap, Order o, JPanel pane, List<Order> servedList) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Xác nhận thanh toán", true);
        dialog.setLayout(new BorderLayout(10, 10));
        BigDecimal total = BigDecimal.ZERO;

        // 🔹 Header
        JLabel header = new JLabel("Xác nhận thanh toán cho đơn hàng #" + orderId);
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(header, BorderLayout.NORTH);

        // 🔹 Table of dishes
        String[] columns = { "Tên món", "Số lượng", "Đơn giá", "Thành tiền" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (Map.Entry<Dish, Integer> entry : dishMap.entrySet()) {
            Dish dish = entry.getKey();
            int qty = entry.getValue();
            BigDecimal price = dish.getPrice();
            BigDecimal amount = price.multiply(BigDecimal.valueOf(qty));
            total = total.add(amount);

            model.addRow(new Object[] {
                dish.getName(),
                qty,
                String.format("%,.0f₫", price),
                String.format("%,.0f₫", amount)
            });
        }

        model.addRow(new Object[] {
            "Tổng tiền",
            "",
            "",
            String.format("%,.0f₫", total)
        });

        JTable table = new JTable(model);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Check if this is the last row (Tổng tiền)
                if (row == table.getRowCount() - 1) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    c.setForeground(new Color(0, 102, 204)); // Deep blue or any color you like
                } else {
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });
        table.setEnabled(false);
        table.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(table);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // 🔹 Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton confirmBtn = new JButton("Xác nhận thanh toán");
        JButton cancelBtn = new JButton("Hủy");

        confirmBtn.addActionListener(e -> {
            dialog.dispose();
            payOrder(o, pane, servedList); 
            
            
            
            new Thread(() -> {
            if (checkTableUse(o)) {
                if(o.getTable_id() != 0) {
                    TableController tc = new TableController();
                    Table t = tc.getTableById(o.getTable_id());
                    t.setStatus("available");
                    tc.updateTable(t);
                } 
            }
        }).start(); 
        
            
//            // Handle recipe and material
//            deductFromMaterial(o);
        });


        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private boolean checkTableUse(Order o) {
        if(o.getTable_id() != 0) {
            OrderController oc = new OrderController();
            List<Order> ol = oc.getNPDSUOrder();

            for(Order o1: ol) {
                if (o1.getTable_id() == o.getTable_id())
                    return false;
            }
        }
        
        return true;
    }
   
    
    // Cancel order
    private void cancelOrder(Order o, JPanel pane, List<Order> canceledList, JTable oTable, String place) {
        OrderController oc = new OrderController();
        OrderItemController oic = new OrderItemController();
        o.setStatus("canceled");
        oc.udpate(o);
        List<OrderItem> ol = oic.getByOrderId(o.getId());
        for(OrderItem oi : ol) {
            oi.setStatus("canceled");
            oic.update(oi);
        }
        pane.remove(this);
        pane.revalidate();
        pane.repaint();
        canceledList.add(o);
        clearOTable(oTable);
        
        
        new Thread(() -> {
            if (checkTableUse(o)) {
                if(o.getTable_id() != 0) {
                    TableController tc = new TableController();
                    Table t = tc.getTableById(o.getTable_id());
                    t.setStatus("available");
                    tc.updateTable(t);
                } 
            }
        }).start(); 
        
        if (place.equals("order") && !isAdmin) {
            RedisPublisher.publish("cancel_order_channel1", String.valueOf(o.getId()));
        }
            
        if (place.equals("kitchen") && !isAdmin) {
            RedisPublisher.publish("cancel_order_channel", String.valueOf(o.getId()));
        } 
        
    }
    
//    private boolean checkIfTableStillInUse(Order o){
//        OrderController oc = new OrderController();
//
//        if (o.getTable_id() != 0){
//            List<Order> ol = oc.getNPDSUOrder();
//            for(Order o1 : ol){
//                if(o1.getTable_id() == o.getTable_id())
//                    return true;
//            }
//        }
//        
//        return false;
//    }
    
    // Trừ công thức sản phẩm từ materials
    private void deductFromMaterial(Order o) {
        RecipeController rc = new RecipeController();
        MaterialController mc = new MaterialController();
        OrderItemController oic = new OrderItemController();
        List<OrderItem> ol = oic.getByOrderId(o.getId());
        for(OrderItem oi : ol) {
            List<Recipe> rl = rc.getByDishId(oi.getDish_id());
            for(Recipe r : rl) {
                Material m = mc.getById(r.getMaterialId());
                BigDecimal newAmount = m.getQuantity().subtract(r.getQuantity());
                m.setQuantity(newAmount);
                mc.update(m);
            }
        }
    }
    
    public void updateOrder(Order updatedOrder) {
    // Update internal data
        this.orderId = updatedOrder.getId(); // if you store it
        

        // Update UI components
        oStatus.setText("Status: " + updatedOrder.getStatus());
        getStatusColor(updatedOrder.getStatus());

        // Optionally update dish list, table number, etc.
        // You may need to clear and repopulate oInfo or oTitle

        revalidate();
        repaint();
    }
    
    // get status color
    private void getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "new":
                oStatus.setForeground(new Color(0, 102, 204)); // Blue
                break;
            case "preparing":
                oStatus.setForeground(new Color(255, 153, 0));
                break;
            case "done":
                oStatus.setForeground(new Color(0, 153, 0)); // Green
                break;
            case "served":
                oStatus.setForeground(new Color(102, 0, 204));
                break;
            case "canceled":
                oStatus.setForeground(Color.RED);
                break;
            default:
                oStatus.setForeground(Color.GRAY); // Unknown status
                break;
        }
        
    }
    
    // Serve order
    private void serveOrder(Order o, JPanel pane, List<Order> servedList, JTable oTable) {
        OrderController oc = new OrderController();
        OrderItemController oic = new OrderItemController();
        o.setStatus("served");
        oc.udpate(o);
        List<OrderItem> ol = oic.getByOrderId(o.getId());
        for(OrderItem oi : ol) {
            oi.setStatus("served");
            oic.update(oi);
        }
        clearOTable(oTable);
        pane.remove(this);
        pane.revalidate();
        pane.repaint();
        servedList.add(o);
    }
    
    
    
    private void clearOTable(JTable oTable){
        DefaultTableModel model = (DefaultTableModel) oTable.getModel();
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col = 0; col < model.getColumnCount(); col++) {
                model.setValueAt("", row, col);
            }
        }
    }
    private void payOrder(Order o, JPanel pane,List<Order> servedList) {
        OrderController oc = new OrderController();
        o.setPaid(true);
        oc.udpate(o);
        
        
        pane.remove(this);
        pane.revalidate();
        pane.repaint();
        servedList.remove(o);
    };
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        oTitle = new javax.swing.JPanel();
        oBtnScroll = new javax.swing.JScrollPane();
        oWrapper = new javax.swing.JPanel();
        oInfo = new javax.swing.JPanel();
        oFooter = new javax.swing.JPanel();
        oBtn = new javax.swing.JButton();
        oStatus = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        oTitle.setBackground(new java.awt.Color(0, 0, 255));
        oTitle.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout oTitleLayout = new javax.swing.GroupLayout(oTitle);
        oTitle.setLayout(oTitleLayout);
        oTitleLayout.setHorizontalGroup(
            oTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        oTitleLayout.setVerticalGroup(
            oTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        oBtnScroll.setBackground(new java.awt.Color(255, 255, 255));

        oWrapper.setBackground(new java.awt.Color(255, 255, 255));

        oInfo.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout oInfoLayout = new javax.swing.GroupLayout(oInfo);
        oInfo.setLayout(oInfoLayout);
        oInfoLayout.setHorizontalGroup(
            oInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 392, Short.MAX_VALUE)
        );
        oInfoLayout.setVerticalGroup(
            oInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout oWrapperLayout = new javax.swing.GroupLayout(oWrapper);
        oWrapper.setLayout(oWrapperLayout);
        oWrapperLayout.setHorizontalGroup(
            oWrapperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, oWrapperLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(oInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        oWrapperLayout.setVerticalGroup(
            oWrapperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(oInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        oBtnScroll.setViewportView(oWrapper);

        oFooter.setBackground(new java.awt.Color(255, 255, 255));

        oBtn.setBackground(new java.awt.Color(255, 102, 102));
        oBtn.setForeground(new java.awt.Color(255, 255, 255));
        oBtn.setText("jButton1");

        oStatus.setText("jLabel1");

        javax.swing.GroupLayout oFooterLayout = new javax.swing.GroupLayout(oFooter);
        oFooter.setLayout(oFooterLayout);
        oFooterLayout.setHorizontalGroup(
            oFooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(oFooterLayout.createSequentialGroup()
                .addGap(126, 126, 126)
                .addComponent(oBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(128, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, oFooterLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(oStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        oFooterLayout.setVerticalGroup(
            oFooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, oFooterLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(oStatus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(oBtn)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(oTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(oBtnScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(oFooter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(oTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(oBtnScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(oFooter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton oBtn;
    private javax.swing.JScrollPane oBtnScroll;
    private javax.swing.JPanel oFooter;
    private javax.swing.JPanel oInfo;
    private javax.swing.JLabel oStatus;
    private javax.swing.JPanel oTitle;
    private javax.swing.JPanel oWrapper;
    // End of variables declaration//GEN-END:variables
}
