/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.view.Order;

/**
 *
 * @author Tyler
 */
import com.google.gson.Gson;
import com.mycompany.quanlyquanan.controller.DishController;
import com.mycompany.quanlyquanan.controller.OrderController;
import com.mycompany.quanlyquanan.controller.OrderItemController;
import com.mycompany.quanlyquanan.model.Dish;
import com.mycompany.quanlyquanan.model.Order;
import com.mycompany.quanlyquanan.model.OrderItem;
import com.mycompany.quanlyquanan.service.OrderItemService;
import com.mycompany.quanlyquanan.service.OrderService;
import com.mycompany.quanlyquanan.utils.RedisConnector;
import com.mycompany.quanlyquanan.utils.RedisPublisher;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import redis.clients.jedis.Jedis;

public class ConfirmPane extends JPanel {
    private JPanel rightPanel;
    private JButton confirmBtn;
    private JButton deleteAllBtn;
    private List<DishOrder> orderList;
    private Map<DishOrder, LinePanel> panelList;
    private int tableId;
    private int orderId;
    private JPanel summaryPanel;
    public ConfirmPane () {};
    
    public ConfirmPane(int tableId, JTabbedPane conPane){
        this.tableId = tableId;
        this.orderList = new ArrayList<>();
        this.panelList = new HashMap<>();
        
        this.setMinimumSize(new Dimension(480, 600));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setMinimumSize(new Dimension(450, 500));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Confirm Panel");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(label);

        JScrollPane scrollPane = new JScrollPane(rightPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        confirmBtn = new JButton("Confirm");
        confirmBtn.setEnabled(false);
        confirmBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmBtn.addActionListener(e -> {
            Order o = new Order();
            o.setTable_id(this.tableId);
            o.setTotal_amount(BigDecimal.ZERO);
            o.setEmployee_id(0); // Phải cập nhật id nhân viên ở đây
            OrderController os = new OrderController();
            // Bước 1. Tạo Order khi bấm Confirm
            os.create(o);
            Order order = os.getLastestOrder();
            this.orderId = order.getId();
            
            // Bước 2. Tạo order_items của order vừa trên
            createOrderItems(orderList, orderId);
            
            // Bước 3. Trừ nguyên vật liệu từ materials theo công thức món
            
            // Bước 4. Ghi stock_logs với change_type = 'consume'
            
            // Tự động thông báo và hiển thị lên bếp -> Bước 5. Thông báo cho bếp
            // Các bước còn lại sẽ được thực hiện bên bếp
            // Update to Redis after confirm (báo cho bên bếp)
            RedisPublisher.publish("new_order_channel", String.valueOf(orderId));
            
            // Disable the buttons
            confirmBtn.setEnabled(false);
            deleteAllBtn.setEnabled(false);
            disablePlusMinus();
            
            // Create a summary pane
            if(summaryPanel != null && this.isAncestorOf(summaryPanel))
                remove(summaryPanel);
            
            
            summaryPanel = createOrderSummaryPanel();
            add(summaryPanel);
            revalidate();
            repaint();
            
            // Để order lên conPane
            sendOrderToConPane(order, conPane);
            
        });

        deleteAllBtn = new JButton("Delete All");
        deleteAllBtn.setEnabled(false);
        deleteAllBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteAllBtn.addActionListener(e -> {
            deleteAll();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(deleteAllBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(confirmBtn);
        buttonPanel.add(Box.createHorizontalGlue());

        add(scrollPane);
        add(buttonPanel);
    }
    
    public JPanel getRightPanel() {
        return rightPanel;
    }
    
    // Delete all current items in the confirm panel
    private void deleteAll(){
        for(LinePanel pl : panelList.values()) {
            rightPanel.remove(pl);
        }
        
        orderList.clear();
        panelList.clear();
        deleteAllBtn.setEnabled(false);
        confirmBtn.setEnabled(false);
        
        rightPanel.revalidate();
        rightPanel.repaint();
    }
    
    private void disablePlusMinus() {
        for(LinePanel pl : panelList.values()) {
            pl.getPlus().setEnabled(false);
            pl.getMinus().setEnabled(false);
            pl.getQuantityField().setEnabled(false);
            pl.getDelete().setEnabled(false);
        }
    }
    
    // Calculate total amount of each order
    private BigDecimal calculateTotalAmount(List<DishOrder> doList) {
        BigDecimal amount = BigDecimal.ZERO;
        for(DishOrder dishOrder : doList) {
            BigDecimal price = dishOrder.getDish().getPrice();
            BigDecimal qty = BigDecimal.valueOf(dishOrder.getQuantity());
            
            amount = amount.add(price.multiply(qty));
        }
        
        return amount;
    }
    
    // Create orderItem(s)of each order
    private void createOrderItems(List<DishOrder> doList, int orderId) {
        for(DishOrder dishOrder : doList) {
            OrderItemService ois = new OrderItemService();
            ois.createOrderItem(new OrderItem(orderId, dishOrder.getDish().getId(), dishOrder.getQuantity()));
        }
    }
    
    // Create order summary for each confirmation
    private JPanel createOrderSummaryPanel() {
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Order Summary"));

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (DishOrder dishOrder : orderList) {
            Dish dish = dishOrder.getDish();
            int quantity = dishOrder.getQuantity();
            BigDecimal price = dish.getPrice();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(lineTotal);

            JLabel itemLabel = new JLabel(
                dish.getName() + " x" + quantity + " — " + lineTotal + " VND"
            );
            summaryPanel.add(itemLabel);
        }

        JLabel totalLabel = new JLabel("Total: " + totalAmount + " VND");
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        summaryPanel.add(Box.createVerticalStrut(10));
        summaryPanel.add(totalLabel);

        return summaryPanel;
    }

    // Gửi lên servingPane
    private void sendOrderToConPane(Order o, JTabbedPane conPane) {
       JPanel pane = orderInServing(o);
       conPane.add("Order " + o.getId(), pane);
    }
    
    private JPanel orderInServing(Order o) {
        JPanel pane = new JPanel();
        
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        String orderName = "Order " + o.getId() + " ___ Dish List"; 
        pane.setBorder(BorderFactory.createTitledBorder(orderName));
        
        String banName = o.getTable_id()  == 0 ? "Mang về " : "Bàn " + o.getTable_id();
        pane.add(new JLabel(banName));
        pane.add(new JLabel("Status: " + o.getStatus()));
        OrderItemController oic = new OrderItemController();
        List<OrderItem> ol = oic.getByOrderId(o.getId());
        for(OrderItem oi : ol) {
            JPanel p = new JPanel();
            DishController dc = new DishController();
            Dish d = dc.getById(oi.getDish_id());
            
            JLabel dishLabel = new JLabel(
                    d.getName() + " x" + oi.getQuantity() + " — " + o.getStatus()
            );
            
            p.add(dishLabel);
            
            JButton cancel = new JButton("Cancel Dish");
            p.add(cancel);
            pane.add(p);
        }
        
        JButton update = new JButton("Update");
        JButton cancel = new JButton("Cancel");
        pane.add(update);
        pane.add(cancel);
        return pane;
    }
    
    public JButton getConfirmBtn() {
        return confirmBtn;
    }

    public void setConfirmBtn(JButton confirmBtn) {
        this.confirmBtn = confirmBtn;
    }

    public JButton getDeleteAllBtn() {
        return deleteAllBtn;
    }

    public void setDeleteAllBtn(JButton deleteAllBtn) {
        this.deleteAllBtn = deleteAllBtn;
    }

    public List<DishOrder> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<DishOrder> orderList) {
        this.orderList = orderList;
    }

    public Map<DishOrder, LinePanel> getPanelList() {
        return panelList;
    }

    public void setPanelList(Map<DishOrder, LinePanel> panelList) {
        this.panelList = panelList;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    
}
