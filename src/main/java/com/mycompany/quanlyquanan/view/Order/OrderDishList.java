/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.view.Order;

/**
 *
 * @author Tyler
 */
import com.mycompany.quanlyquanan.controller.DishController;
import com.mycompany.quanlyquanan.controller.OrderController;
import com.mycompany.quanlyquanan.controller.OrderItemController;
import com.mycompany.quanlyquanan.model.Category;
import com.mycompany.quanlyquanan.model.Dish;
import com.mycompany.quanlyquanan.model.Order;
import com.mycompany.quanlyquanan.model.OrderItem;
import com.mycompany.quanlyquanan.model.Table;
import com.mycompany.quanlyquanan.service.CategoryService;
import com.mycompany.quanlyquanan.service.DishService;
import com.mycompany.quanlyquanan.service.TableService;
import com.mycompany.quanlyquanan.utils.RedisPublisher;
import com.mycompany.quanlyquanan.utils.RedisSubscriber1;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import redis.clients.jedis.JedisPubSub;

public class OrderDishList extends JFrame {
    
    private JTabbedPane rightPanel;
    private int tableId;
    private JTabbedPane tabPane;
    private ConfirmPane currentRightPane;
    private Map<Integer, ConfirmPane> confirmPaneList;
    private JTabbedPane conPane;
    private JTabbedPane servingPane;
//    private List<Order> servingList;
    private JTabbedPane payingPane;
    private JPanel canceledPane;

    public OrderDishList() throws SQLException{
        setSize(1000,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel on the right   
        tabPane = createTableAndDishPane();
        rightPanel = new JTabbedPane();

        // Split between the left (tables and foods) and the right
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPane, rightPanel);
        splitPane.setResizeWeight(1);
        splitPane.setDividerLocation(getWidth() - 480);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        
        add(splitPane);
        confirmPaneList = new HashMap<>();
        
        startListeningForOIUpdate();
    }
    
    // Tạo JTabbedPane để thể hiện bàn và các món ăn
    private JTabbedPane createTableAndDishPane() throws SQLException {
        JTabbedPane tabPane = new JTabbedPane();
        servingPane = createServingPane();
        payingPane = createPayingPane();
        conPane = createConPane();
        
        
        tabPane.add("Bàn", createTablePane());
        tabPane.add("Món ăn", createDishPane());
        tabPane.add("Chờ xác nhận", conPane);
        tabPane.add("Chờ phục vụ", servingPane);
        tabPane.add("Chờ thanh toán", payingPane);
        
        JScrollPane cancelWrapper = createCanceledPane();
        tabPane.add("Đã hủy", cancelWrapper);
        return tabPane;
    }
    
    
    // Tab thể hiện các bàn có thể chọn
    private JPanel createTablePane() throws SQLException {
        JPanel tablePane = new JPanel();
        List<Table> tableList = this.getAllTables();
        
        // Mang về, không bị giới hạn số tab tạo
        JButton mangVeBtn = new JButton("Mang về");
        mangVeBtn.addActionListener(e -> {
            this.tableId = 0;
            ConfirmPane rightPane = new ConfirmPane(this.tableId, conPane);
            addClosableTab(rightPanel, "Mang về", rightPane);
            tabPane.setSelectedIndex(1);
            rightPanel.setSelectedComponent(rightPane);
            this.currentRightPane = rightPane;
        });
        
        // Theo bàn, giới hạn bởi Id của bàn
        tablePane.add(mangVeBtn);
        for(Table t : tableList) {
            JButton tableBtn = new JButton(t.getName());
            tablePane.add(tableBtn);
            tableBtn.addActionListener(e -> {
                this.tableId = t.getId();
                
                // Create new tabs for each table
                if (confirmPaneList.containsKey(this.tableId)) {
                    rightPanel.setSelectedComponent(confirmPaneList.get(this.tableId));
                    tabPane.setSelectedIndex(1);
                    this.currentRightPane = confirmPaneList.get(this.tableId);
                }
                else {
                    ConfirmPane rightPane = new ConfirmPane(this.tableId, conPane);
                    String tabTitle = "Bàn " + String.valueOf(t.getId());
                    addClosableTab(rightPanel, tabTitle, rightPane);
                    tabPane.setSelectedIndex(1);
                    confirmPaneList.put(tableId, rightPane);
                    rightPanel.setSelectedComponent(rightPane);
                    this.currentRightPane = rightPane;
                }
            });
            
            // Disable các bàn không available
            if (!"available".equals(t.getStatus()))
                tableBtn.setEnabled(false);
        }
        
        return tablePane;
    }
    
    // Form tabs with close on top
    public void addClosableTab(JTabbedPane tabPane, String title, ConfirmPane content) {
        tabPane.add(content); // add the actual content
        int index = tabPane.indexOfComponent(content);

        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabHeader.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        JButton closeButton = new JButton("x");

        closeButton.setMargin(new Insets(0, 5, 0, 5));
        closeButton.setBorder(null);
        closeButton.setFocusable(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setForeground(Color.RED);
        
        // Đóng tab khi bấm nút "x"
        closeButton.addActionListener(e -> {
            int closedIndex = tabPane.indexOfComponent(content);
            tabPane.remove(content);
            confirmPaneList.entrySet().removeIf(entry -> entry.getValue() == content);
            
            // Handle selection of next tab
            int tabCount = tabPane.getTabCount();
            if (tabCount > 0) {
                // Try to select the next tab, or the previous one if last was removed
                int nextIndex = Math.min(closedIndex, tabCount - 1);
                tabPane.setSelectedIndex(nextIndex);
                Component nextComponent = tabPane.getComponentAt(nextIndex);

                if (nextComponent instanceof ConfirmPane) {
                    currentRightPane = (ConfirmPane) nextComponent;
                } else {
                    currentRightPane = null;
                }
            } else {
                // No tabs left
                currentRightPane = null;
                System.out.println("All tabs closed.");
            }

        });

        tabHeader.add(titleLabel);
        tabHeader.add(closeButton);
        
        // Add mouse listener to detect clicks on the tab header
        tabHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Tab header clicked: " + title);
                rightPanel.setSelectedComponent(content);
                currentRightPane = content;
            }
        });


        tabPane.setTabComponentAt(index, tabHeader);
    }
    
    // Display all the dishes to choose
    private JTabbedPane createDishPane(){
        JTabbedPane tabPane = new JTabbedPane();
        
        List<Category> catList = this.getAllCategories();
        catList.sort(Comparator.comparingInt(Category::getId));
        for (Category cat : catList) {
            tabPane.add(cat.getName(),creatingDishPanel(cat.getId()));
        }
        
        return tabPane;
    }
    
    // Tạo conPane
    private JTabbedPane createConPane(){
        JTabbedPane pane = new JTabbedPane();
        OrderController oc = new OrderController();
        List<Order> ol = oc.getAllNewOrder();
        for(Order o : ol) {
            JPanel p = createConPanel(o);
            pane.add("Order " + o.getId(), p);
        }
        
        return pane;
    }
    
    private JPanel createConPanel(Order o) {
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
    // Tạo servingPane
    private JTabbedPane createServingPane() {
        JTabbedPane pane = new JTabbedPane();
        OrderController oc = new OrderController();
        List<Order> ol = oc.getAllPrepDone();
        for(Order o : ol) {
            JPanel p = orderInServing(o);
            pane.add("Order " + o.getId(), p);
        }
        
        return pane;
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
        OrderController oc = new OrderController();
        List<OrderItem> ol = oic.getByOrderId(o.getId());
        for(OrderItem oi : ol) {
            JPanel p = new JPanel();
            DishController dc = new DishController();
            Dish d = dc.getById(oi.getDish_id());
            
            JLabel dishLabel = new JLabel(
                    d.getName() + " x" + oi.getQuantity() + " — " + oi.getStatus()
            );
            dishLabel.setName("oi_" + oi.getId());
            p.add(dishLabel);
            
            
            // Nút cancel của servingPane
            JButton cancel = new JButton("Cancel Dish");
            p.add(cancel);
            JButton serve = new JButton("Serve");
            p.add(serve);
            serve.setVisible(false);
            
            if(!oi.getStatus().equals("canceled"))
                cancel.setVisible(true);
            if(oi.getStatus().equals("done")) {
                serve.setVisible(true);
                cancel.setVisible(false);
            }
                
            
            cancel.addActionListener((ActionEvent e) -> {
                oi.setStatus("canceled");
                oic.update(oi);
                cancel.setVisible(false);
                serve.setVisible(false);
                List<OrderItem> ol1 = oic.getByOrderId(o.getId());
                if(checkIfAllOICanceled(ol1)) {
                    canceledOrderProcedure(o, pane);
                } else if (checkIfAllOIServed(ol1)) {
                    servedOrderProcedure(o, pane);
                } else {
                    p.remove(dishLabel);
                    p.add(new JLabel(
                        d.getName() + " x" + oi.getQuantity() + " — " + oi.getStatus()
                    ));
                    p.revalidate();
                    p.repaint();
                }
                RedisPublisher.publish("cancel_oi_channel1", String.valueOf(oi.getId()));
            });
            
            
            serve.addActionListener(e -> {
                oi.setStatus("served");
                oic.update(oi);
                cancel.setVisible(false);
                serve.setVisible(false);
                List<OrderItem> ol1 = oic.getByOrderId(o.getId());
                if (checkIfAllOIServed(ol1)) 
                    servedOrderProcedure(o, pane);
                else {
                    p.remove(dishLabel);
                    p.add(new JLabel(
                        d.getName() + " x" + oi.getQuantity() + " — " + oi.getStatus()
                    ));
                    p.revalidate();
                    p.repaint();
                }
            });
            
            
            pane.add(p);
        }
        
        JButton update = new JButton("Update");
        JButton cancel = new JButton("Cancel");
        pane.add(update);
        pane.add(cancel);
        return pane;
    }
    
    // Tạo Payment Pane
    private JTabbedPane createPayingPane() {
        JTabbedPane pane = new JTabbedPane();
        OrderController oc = new OrderController();
        List<Order> ol = oc.getUnpaid();
        for(Order o : ol) {
            JPanel p = unpaidDetail(o);
            pane.add("Order " + o.getId(), p);
        }
        
        return pane;
    }
    
    private JPanel unpaidDetail(Order o) {
        JPanel pane = new JPanel();
        
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        String orderName = "Order " + o.getId() + " ___ Dish List"; 
        pane.setBorder(BorderFactory.createTitledBorder(orderName));
        
        String banName = o.getTable_id()  == 0 ? "Mang về " : "Bàn " + o.getTable_id();
        pane.add(new JLabel(banName));
        pane.add(new JLabel("Status: " + o.getStatus()));
        OrderItemController oic = new OrderItemController();
        OrderController oc = new OrderController();
        List<OrderItem> ol = oic.getByOrderId(o.getId());
        for(OrderItem oi : ol) {
            JPanel p = new JPanel();
            DishController dc = new DishController();
            Dish d = dc.getById(oi.getDish_id());
            
            JLabel dishLabel = new JLabel(
                    d.getName() + " x" + oi.getQuantity() + " — " + oi.getStatus()
            );
            dishLabel.setName("oi_" + oi.getId());
            p.add(dishLabel);
            pane.add(p);
        }
        
        JButton payment = new JButton("Payment");
        payment.addActionListener(e -> {
            o.setPaid(true);
            payingPane.remove(pane);
            payingPane.revalidate();
            payingPane.repaint();
            List<OrderItem> allServedOrderItem = oic.allServedByOrderId(o.getId());
            BigDecimal amount = BigDecimal.ZERO;
            for(OrderItem oi : allServedOrderItem) {
                DishController ds = new DishController();
                Dish d = ds.getById(oi.getDish_id());
                BigDecimal da = d.getPrice();
                int qty = oi.getQuantity();
                amount = amount.add(da.multiply(BigDecimal.valueOf(qty)));
            }
            
            o.setTotal_amount(amount);
            oc.udpate(o);
           
        });
        pane.add(payment);
        
        return pane;
    }
    
    // Tạo canceled detail
    private JPanel createCancel(Order o) {
        JPanel pane = new JPanel();
        
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        String orderName = "Order " + o.getId() + " ___ Dish List"; 
        pane.setBorder(BorderFactory.createTitledBorder(orderName));
        
        String banName = o.getTable_id()  == 0 ? "Mang về " : "Bàn " + o.getTable_id();
        pane.add(new JLabel(banName));
        pane.add(new JLabel("Status: " + o.getStatus()));
        OrderItemController oic = new OrderItemController();
        OrderController oc = new OrderController();
        List<OrderItem> ol = oic.getByOrderId(o.getId());
        for(OrderItem oi : ol) {
            JPanel p = new JPanel();
            DishController dc = new DishController();
            Dish d = dc.getById(oi.getDish_id());
            
            JLabel dishLabel = new JLabel(
                    d.getName() + " x" + oi.getQuantity() + " — " + oi.getStatus()
            );
            dishLabel.setName("oi_" + oi.getId());
            p.add(dishLabel);
            pane.add(p);
        }
        
        return pane;
    }
    
    private void servedOrderProcedure(Order o, JPanel p) {
        OrderController oc = new OrderController();
        o.setStatus("served");
        oc.udpate(o);
        
        servingPane.remove(p);
        servingPane.revalidate();
        servingPane.repaint();
        
        JPanel paymentDetail = unpaidDetail(o);
        payingPane.add("Order" + o.getId(), paymentDetail);
        payingPane.revalidate();
        payingPane.repaint();
    }
    
    private void canceledOrderProcedure(Order o, JPanel p) {
        OrderController oc = new OrderController();
        o.setStatus("canceled");
        oc.udpate(o);
        
        servingPane.remove(p);
        servingPane.revalidate();
        servingPane.repaint();
        
        JPanel doneDetail = createCancel(o);
        canceledPane.add(doneDetail);
        canceledPane.revalidate();
        canceledPane.repaint();
    }
    
    private List<Category> getAllCategories() {
        CategoryService cs = new CategoryService();
        return cs.getAllCategories();
    }
    
    private List<Table> getAllTables() throws SQLException {
        TableService ts = new TableService();
        return ts.getAllTables();
    }
    
    // Tạo từng dòng thông tin thức ăn bên phải
    private JPanel creatingDishPanel(int cat_id) {
        
        DishService ds = new DishService();
        List<Dish> dishList = ds.getDishesByCategory(cat_id);
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        for (Dish dish : dishList) {
            // Chức năng khi bấm vào từng món ăn
            JButton button = new JButton(dish.getName());
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<DishOrder> orderList = currentRightPane.getOrderList();
                    Map<DishOrder, LinePanel> panelList = currentRightPane.getPanelList();
                    JButton confirmBtn = currentRightPane.getConfirmBtn();
                    JButton deleteAllBtn = currentRightPane.getDeleteAllBtn();
                    if (!isDishInOrderList(dish, orderList)) {
                        DishOrder dishOrder = new DishOrder(dish, dish.getCategoryId(), 1);
                        orderList.add(dishOrder);
                        JPanel currentRight = currentRightPane.getRightPanel();
                        LinePanel p1 = new LinePanel(dish, currentRight, orderList, panelList, confirmBtn, deleteAllBtn);
                        panelList.put(dishOrder, p1);
                        
                        currentRight.add(p1);
                        currentRight.revalidate();
                        currentRight.repaint();
                        confirmBtn.setEnabled(true);
                        deleteAllBtn.setEnabled(true);
                    }
                    else {
                        int qty = getDishQuantity(dish, orderList);
                        LinePanel p2 = getLinePanel(dish, panelList);
                        p2.setQuantity(qty + 1);
                        setDishQuantity(dish, orderList, qty + 1);
                    }
                }
            });
            p.add(button);
        }
        return p;
    }
    
    private boolean isDishInOrderList(Dish targetDish, List<DishOrder> ol) {
        for(DishOrder dishMap : ol) {
            if(dishMap.getDish().equals(targetDish)) {
                return true;
            }
        }
        
        return false;
    }
    
    private Integer getDishQuantity(Dish targetDish, List<DishOrder> ol) {
        for(DishOrder dishOrder : ol) {
            if(dishOrder.getDish().equals(targetDish))
                return dishOrder.getQuantity();
        }
        
        return null;
    }
    
    private void setDishQuantity(Dish targetDish, List<DishOrder> ol, int qty) {
        for(DishOrder dishMap : ol) {
            if(dishMap.getDish().equals(targetDish))
                dishMap.setQuantity(qty);
        }
    }
    
    private LinePanel getLinePanel(Dish targetDish, Map<DishOrder, LinePanel> pl) {
        for (Map.Entry<DishOrder, LinePanel> entry : pl.entrySet()) {
            DishOrder dishOrder = entry.getKey();
            if (dishOrder.getDish().equals(targetDish)) {
                return entry.getValue(); // the matching LinePanel
            }
        }
        return null; // not found
    }    
    
    // Tạo private 
    private JScrollPane createCanceledPane() {
        // Create a container panel to hold all done order panels
        canceledPane = new JPanel();
        canceledPane.setLayout(new BoxLayout(canceledPane, BoxLayout.Y_AXIS)); // vertical stacking

        JScrollPane scrollPane = new JScrollPane(canceledPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }
    
    // Kiểm tra xem tất cả OrderItem trong order đã thành canceled hoặc done hết chưa
    private boolean checkIfAllOICanceled(List<OrderItem> ol) {
        for(OrderItem o : ol) {
            String status = o.getStatus();
            if (!status.equals("canceled"))
                return false;
        }
        
        return true;
    }
    
    private boolean checkIfAllOIServed(List<OrderItem> ol) {
        for(OrderItem o : ol) {
            String status = o.getStatus();
            if(!status.equals("served") && !status.equals("canceled"))
                return false;
        }
        
        return true;
    }
    
    private void startListeningForOIUpdate() {
        JedisPubSub listener = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if ("cooking_oi_channel".equals(channel)) {
                    int oiId = Integer.parseInt(message);
                    System.out.println("Update cooking cho order item: " + oiId);
                    OrderItemController oic = new OrderItemController();
                    OrderItem oi = oic.getById(oiId);
                    SwingUtilities.invokeLater(() -> {
                        updateOrderItemStatusInServingPane(oiId, oi.getStatus(), Color.BLUE);
                    });
                }
                if ("cancel_oi_channel".equals(channel)) {
                    int oiId = Integer.parseInt(message);
                    System.out.println("Update cancel cho order item: " + oiId);
                    OrderItemController oic = new OrderItemController();
                    OrderItem oi = oic.getById(oiId);
                    SwingUtilities.invokeLater(() -> {
                        updateOrderItemStatusInServingPane(oiId, oi.getStatus(), Color.RED);
                        
                    });
                }
                if ("done_oi_channel".equals(channel)) {
                    int oiId = Integer.parseInt(message);
                    System.out.println("Update done cho order item: " + oiId);
                    OrderItemController oic = new OrderItemController();
                    OrderItem oi = oic.getById(oiId);
                    SwingUtilities.invokeLater(() -> {
                        updateOrderItemStatusInServingPane(oiId, oi.getStatus(), Color.GREEN);
                    });
                }
                
                if("confirm_order_channel".equals(channel)) {
                    int orderId = Integer.parseInt(message);
                    System.out.println("Confirm order: " + orderId);
                    SwingUtilities.invokeLater(() -> {
                        updateOrderToServingPane(orderId);
                    });
                }
            }
        };
        
        RedisSubscriber1 subscriber = new RedisSubscriber1();
        subscriber.subscribe(listener, "cooking_oi_channel", "cancel_oi_channel", "done_oi_channel", "confirm_order_channel");
    }
    
   
    private void updateOrderItemStatusInServingPane(int orderItemId, String newStatus, Color color) {
        for (int i = 0; i < servingPane.getTabCount(); i++) {
            Component tabContent = servingPane.getComponentAt(i);
            if (tabContent instanceof JPanel) {
                JPanel panel = (JPanel) tabContent;
                for (Component comp : panel.getComponents()) {
                    if (comp instanceof JPanel) {
                        JPanel dishPanel = (JPanel) comp;
                        for (Component inner : dishPanel.getComponents()) {
                            if (inner instanceof JLabel && ("oi_" + orderItemId).equals(inner.getName())) {
                                JLabel label = (JLabel) inner;
                                String oldText = label.getText();
                                String updatedText = oldText.replaceAll("—.*", "— " + newStatus);
                                label.setText(updatedText);
                                label.setForeground(color);
                                if(newStatus.equals("canceled")) {
                                    System.out.println("canceled");
                                    for(Component comp1 : dishPanel.getComponents()) {
                                        if(comp1 instanceof JButton) {
                                            JButton btn = (JButton) comp1;
                                            if(btn.getText().equals("Cancel Dish")) {
                                            
                                                btn.setVisible(false);
                                                System.out.println("found it");
                                                OrderItemController oic = new OrderItemController();
                                                OrderItem oi = oic.getById(orderItemId);
                                                oi.setStatus("canceled");
                                                oic.update(oi);
                                                int orderId = oi.getOrder_id();
                                                OrderController oc = new OrderController();
                                                Order o = oc.getOrderById(orderId);
                                                List<OrderItem> ol = oic.getByOrderId(orderId);
                                                System.out.println(checkIfAllOICanceled(ol));
                                                if(checkIfAllOICanceled(ol))
                                                    canceledOrderProcedure(o, panel);
                                            }
                                                
                                        }
                                    }
                                }
                                if(newStatus.equals("done")) {
                                    System.out.println("done");
                                    for(Component comp1 : dishPanel.getComponents()) {
                                        if(comp1 instanceof JButton) {
                                            JButton btn = (JButton) comp1;
                                            if(btn.getText().equals("Serve")) {
                                            
                                                btn.setVisible(true);
                     
                                            }
                                            
                                            if(btn.getText().equals("Cancel Dish"))
                                                btn.setVisible(false);
                                                
                                        }
                                    }
                                } 
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void updateOrderToServingPane(int orderId) {
        String targetTitle = "Order " + orderId;

        for (int i = 0; i < conPane.getTabCount(); i++) {
            String tabTitle = conPane.getTitleAt(i);
            if (tabTitle.equals(targetTitle)) {
                Component tabContent = conPane.getComponentAt(i);

                // Now you can update the tab content here
                if (tabContent instanceof JPanel) {
                    JPanel panel = (JPanel) tabContent;
                    // Example: refresh or update status
                    conPane.remove(panel);
                    conPane.revalidate();
                    conPane.repaint();
                    OrderController oc = new OrderController();
                    Order o = oc.getOrderById(orderId);
                    panel = orderInServing(o);
                    servingPane.add("Order " + orderId, panel);
                    servingPane.revalidate();
                    servingPane.repaint();
                }
                
                break;
            }
        }

    }
    public static void main (String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new OrderDishList().setVisible(true);
            } catch (SQLException ex) {
                Logger.getLogger(OrderDishList.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
}
