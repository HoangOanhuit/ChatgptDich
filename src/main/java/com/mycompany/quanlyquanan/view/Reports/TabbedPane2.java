/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.quanlyquanan.view.Reports;

import com.mycompany.quanlyquanan.model.ModelCard;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import com.mycompany.quanlyquanan.model.ModelChart;
import com.mycompany.quanlyquanan.utils.ComponentStyleUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;

/**
 *
 * @author macos
 */
public class TabbedPane2 extends javax.swing.JPanel {

    public TabbedPane2() {
        initComponents();

        initComboMonthYear();
        IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());

        tabs.setOpaque(false);
        tabs.setBackground(new Color(0,0,0,0)); // transparent background

        // Custom UI for tab color
        tabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement,
                                              int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                // Đổi màu nền tab được chọn và chưa chọn
                g.setColor(isSelected ? new Color(255, 235, 238) : new Color(230,230,230));
                g.fillRect(x, y, w, h);
            }
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement,
                                          int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                // No border
            }
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement,
                                              int selectedIndex) {
                // No border
            }

        });

        tabs.setFocusable(false);



        // Force repaint when tab selection changes
        tabs.addChangeListener(e -> tabs.repaint());

        // Đổi màu chữ cho tab được chọn và chưa chọn
        tabs.addChangeListener(e -> {
            for (int i = 0; i < tabs.getTabCount(); i++) {
                tabs.setForegroundAt(i, i == tabs.getSelectedIndex() ? new Color(205,0,51) : Color.BLACK);
            }
        });

        // Đặt tab mặc định là Dashboard (index 0)
        tabs.setSelectedIndex(0);
        // Cập nhật màu chữ cho tab được chọn ngay khi khởi tạo
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setForegroundAt(i, i == 0 ? new Color(205,0,51) : Color.BLACK);
        }

        initData();
    }

    private void initComboMonthYear() {

        comboMonth.removeAllItems();
        String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        for (int i = 0; i < 12; i++) {
            comboMonth.addItem(months[i]);

        }
        comboYear.removeAllItems();
        for (int y = 2020; y <= 2030; y++) {
            comboYear.addItem(String.valueOf(y));
        }
        // Set default selected month/year to current
        LocalDate now = LocalDate.now();
        comboMonth.setSelectedIndex(now.getMonthValue() - 1); // 0-based index
        comboYear.setSelectedItem(String.valueOf(now.getYear()));
        ComponentStyleUtil.styleComboBox(comboMonth);
        ComponentStyleUtil.styleComboBox(comboYear);
    }

    public void initData(){
        initTodayReportData();
        initDailyReportData();
        initMonthlyReportData();
        initAnnualReportData();
        
    }

    private void initTodayReportData() {
//        initTodayCardData();

        initTodayTableData();
        initTodayChartData();
        dayTable1.fixTable(todayScroll1);
        dayTable1.fixTable(todayScroll2);

    }

//    private void initTodayCardData() {
//        Icon icon1 = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCOUNT_BALANCE_WALLET, 60, new Color(255,255,255,100));
//        card1.setData(new ModelCard("Today's Income", 5100, icon1));
//        Icon icon2 = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.MONETIZATION_ON, 60, new Color(255,255,255,100));
//        card2.setData(new ModelCard("Today's Expense", 4000, icon2));
//        Icon icon3 = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SHOW_CHART, 60, new Color(255,255,255,100));
//        card3.setData(new ModelCard("Today's Revenue", 1100, icon3));
//
//    }

    private void initTodayTableData() {
        // [TODO] - cần lấy dữ liệu từ database
        // todayTable1.addRowSelectionInterval(new ModelVoucher);
        // todayTable2.addRowSelectionInterval(new ModelVoucher);
    }

    private void initTodayChartData() {
        // [TODO] - cần lấy dữ liệu từ database
        monthChart1.addChartData(new ModelChart("11/8", new double[]{500, 200, 80}));
        monthChart1.addChartData(new ModelChart("12/8", new double[]{600, 200, 80}));
        monthChart1.addChartData(new ModelChart("13/8", new double[]{500, 600, 100}));
        monthChart1.addChartData(new ModelChart("14/8", new double[]{300, 300, 80}));
        monthChart1.addChartData(new ModelChart("15/8", new double[]{500, 200, 60}));
        monthChart1.addChartData(new ModelChart("16/8", new double[]{500, 200, 60}));

    }


    // Khởi tạo dữ liệu cho tab Daily Report
    private void initDailyReportData() {
        // [TODO] - lấy dữ liệu cho Daily Report
        initDailyCardData();
        initDailyTable1Data();
        initDailyChartData();
        dayTable1.fixTable(jScrollPane2);
        dayTable2.fixTable(jScrollPane4);
        LocalDate today = LocalDate.now();
        datePicker1.setDate(today); // Set current date to DatePicker
//        datePicker1.setForeground(new Color(193, 37, 37));
//        datePicker1.setFont(new Font("Helvetica Neue", Font.PLAIN, 15));
        datePicker1.getComponentDateTextField().setFont(ComponentStyleUtil.COMMON_FONT);
        datePicker1.getComponentDateTextField().setBackground(Color.WHITE);
        datePicker1.getComponentDateTextField().setForeground(ComponentStyleUtil.TEXT_COLOR);

    }

    private void initDailyChartData() {
        // [TODO] - cần lấy dữ liệu từ database
        dayChart.addChartData(new ModelChart("10/8", new double[]{500, 200, 80}));
        dayChart.addChartData(new ModelChart("11/8", new double[]{600, 200, 80}));
        dayChart.addChartData(new ModelChart("12/8", new double[]{500, 600, 100}));
        dayChart.addChartData(new ModelChart("13/8", new double[]{300, 300, 80}));
        dayChart.addChartData(new ModelChart("14/8", new double[]{500, 200, 60}));
        dayChart.addChartData(new ModelChart("17/8", new double[]{500, 200, 60}));
    }

    private void initDailyTable1Data() {
        // [TODO] - cần lấy dữ liệu từ database
        // dayTable.addRowSelectionInterval(new ModelVoucher);
    }

    private void initDailyCardData() {
        Icon icon1 = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCOUNT_BALANCE_WALLET, 60, new Color(112, 69, 246,50));
        dayCard1.setData(new ModelCard("Income", 5100, icon1));
        dayCard1.setBottomBorderColor(new GradientPaint(
                0, 0, new Color(205, 139, 237), // purple
                getWidth(), getHeight(), new Color(255, 153, 153))); // pink
        dayCard1.setMoneycolor(new Color(49, 174, 40));

        Icon icon2 = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.MONETIZATION_ON, 60, new Color(0,191,255,50));
        dayCard2.setData(new ModelCard("Expense", 4000, icon2));
        dayCard2.setBottomBorderColor(new GradientPaint(
                0, 0, new Color(0, 191, 255), // blue
                getWidth(), getHeight(), new Color(255, 204, 255))); // green
        dayCard2.setMoneycolor(new Color(248, 35, 74));

        Icon icon3 = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SHOW_CHART, 60, new Color(255,140,0,50));
        dayCard3.setData(new ModelCard("Revenue", -1100, icon3));
        dayCard3.setBottomBorderColor(new GradientPaint(
                0, 0, new Color(255, 140, 0), // orange
                getWidth(), getHeight(), new Color(255, 204, 204))); // red
        if(dayCard3.getValue() < 0){
            dayCard3.setMoneycolor(new Color(248, 35, 74));
        } else {
            dayCard3.setMoneycolor(new Color(49, 174, 40));
        }
    }


    // Kh��i tạo dữ liệu cho tab Monthly Report
    private void initMonthlyReportData() {
        initMonthlyCardData();
        initMonthlyTable1Data();
        initMonthlyChartData();
        monthTable1.fixTable(jScrollPane1);
        monthTable2.fixTable(jScrollPane3);


        // monthChart.setData(...);
    }

    // Khởi tạo dữ liệu cho tab Annual Report
    private void initAnnualReportData() {
        // [TODO] - lấy dữ liệu cho Annual Report
        // Có thể thêm các card, bảng, biểu đồ cho tab này nếu cần
    }

    // [TODO] - cần lấy dữ liệu từ database
    public void initMonthlyTable1Data(){
//        table1.addRowSelectionInterval(new ModelVoucher);
    }

    public void initMonthlyChartData(){
        //[TODO] - cần lấy dữ liệu từ database
        monthChart.addChartData(new ModelChart("January",new double[]{500,200,80}));
        monthChart.addChartData(new ModelChart("February",new double[]{600,200,80}));
        monthChart.addChartData(new ModelChart("March",new double[]{500,600,100}));
        monthChart.addChartData(new ModelChart("April",new double[]{300,300,80}));
        monthChart.addChartData(new ModelChart("May",new double[]{500,200,60}));
        monthChart.addChartData(new ModelChart("June",new double[]{1000,200,30}));
    }

    // [TODO] - c���n lấy dữ liệu từ database
    public void initMonthlyCardData(){
        Icon icon1 = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCOUNT_BALANCE_WALLET, 60, new Color(112, 69, 246,50));
        monthCard1.setData(new ModelCard("Income", 5100, icon1));
        monthCard1.setBottomBorderColor(new GradientPaint(
                0, 0, new Color(205, 139, 237), // purple
                getWidth(), getHeight(), new Color(255, 153, 153))) ;// pink);
        monthCard1.setMoneycolor(new Color(49, 174, 40));

        Icon icon2 = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.MONETIZATION_ON, 60, new Color(0,191,255,50));
        monthCard2.setData(new ModelCard("Expense", 4000, icon2));
        monthCard2.setBottomBorderColor(new GradientPaint(
                0, 0, new Color(0, 191, 255), // blue
                getWidth(), getHeight(), new Color(255, 204, 255) // green
        ));
        monthCard2.setMoneycolor(new Color(248, 35, 74));

        Icon icon3 = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SHOW_CHART, 60, new Color(255,140,0,50));
        monthCard3.setData(new ModelCard("Revenue", -1100, icon3));
        monthCard3.setBottomBorderColor(new GradientPaint(
                0, 0, new Color(255, 140, 0), // orange
                getWidth(), getHeight(), new Color(255, 204, 204) // red
        ));

        if(monthCard3.getValue() < 0){
            monthCard3.setMoneycolor(new Color(248, 35, 74));
        } else {
            monthCard3.setMoneycolor(new Color(49, 174, 40));
        }
    }



    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        card1 = new com.mycompany.quanlyquanan.view.Reports.CardGradient();
        tabs = new javax.swing.JTabbedPane();
        dashboard = new javax.swing.JPanel();
        monthChart1 = new com.mycompany.quanlyquanan.view.Reports.ChartPanel();
        cardPanel2 = new com.mycompany.quanlyquanan.view.Reports.CardPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        todayScroll1 = new javax.swing.JScrollPane();
        todayTable1 = new com.mycompany.quanlyquanan.view.Reports.Table();
        todayScroll2 = new javax.swing.JScrollPane();
        todayTable2 = new com.mycompany.quanlyquanan.view.Reports.Table();
        jLabel16 = new javax.swing.JLabel();
        dayTab = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        dayCard1 = new com.mycompany.quanlyquanan.view.Reports.Card();
        dayCard2 = new com.mycompany.quanlyquanan.view.Reports.Card();
        dayCard3 = new com.mycompany.quanlyquanan.view.Reports.Card();
        jScrollPane2 = new javax.swing.JScrollPane();
        dayTable1 = new com.mycompany.quanlyquanan.view.Reports.Table();
        dayChart = new com.mycompany.quanlyquanan.view.Reports.ChartPanel();
        datePicker1 = new com.github.lgooddatepicker.components.DatePicker();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        dayTable2 = new com.mycompany.quanlyquanan.view.Reports.Table();
        monthTab = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        monthCard1 = new com.mycompany.quanlyquanan.view.Reports.Card();
        monthCard2 = new com.mycompany.quanlyquanan.view.Reports.Card();
        monthCard3 = new com.mycompany.quanlyquanan.view.Reports.Card();
        jScrollPane1 = new javax.swing.JScrollPane();
        monthTable1 = new com.mycompany.quanlyquanan.view.Reports.Table();
        monthChart = new com.mycompany.quanlyquanan.view.Reports.ChartPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        monthTable2 = new com.mycompany.quanlyquanan.view.Reports.Table();
        comboMonth = new javax.swing.JComboBox<>();
        comboYear = new javax.swing.JComboBox<>();
        jScrollPane5 = new javax.swing.JScrollPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        yearTab = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(1050, 940));

        tabs.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setBackground(new java.awt.Color(255, 255, 255));
        tabs.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tabs.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        tabs.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        tabs.setPreferredSize(new java.awt.Dimension(1050, 940));

        dashboard.setBackground(new java.awt.Color(244, 244, 244));

        jLabel10.setText("Sales by Day");
        jLabel10.setBackground(new java.awt.Color(255, 255, 255));
        jLabel10.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        jLabel11.setText("Top 10 Most Order Dishes");
        jLabel11.setBackground(new java.awt.Color(255, 255, 255));
        jLabel11.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        todayTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "#", "Name", "Quantity"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        todayTable1.setOpaque(false);
        todayScroll1.setViewportView(todayTable1);
        if (todayTable1.getColumnModel().getColumnCount() > 0) {
            todayTable1.getColumnModel().getColumn(0).setResizable(false);
            todayTable1.getColumnModel().getColumn(1).setResizable(false);
            todayTable1.getColumnModel().getColumn(2).setResizable(false);
        }

        todayTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "#", "Name", "Quantity"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        todayTable2.setOpaque(false);
        todayScroll2.setViewportView(todayTable2);
        if (todayTable2.getColumnModel().getColumnCount() > 0) {
            todayTable2.getColumnModel().getColumn(0).setResizable(false);
            todayTable2.getColumnModel().getColumn(1).setResizable(false);
            todayTable2.getColumnModel().getColumn(2).setResizable(false);
        }

        jLabel16.setText("Top 10 Most Order Dishes");
        jLabel16.setBackground(new java.awt.Color(255, 255, 255));
        jLabel16.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        javax.swing.GroupLayout dashboardLayout = new javax.swing.GroupLayout(dashboard);
        dashboard.setLayout(dashboardLayout);
        dashboardLayout.setHorizontalGroup(
            dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(cardPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(monthChart1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(dashboardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(dashboardLayout.createSequentialGroup()
                .addGroup(dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dashboardLayout.createSequentialGroup()
                        .addComponent(todayScroll1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(dashboardLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 334, Short.MAX_VALUE)))
                .addGroup(dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(todayScroll2, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                    .addGroup(dashboardLayout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        dashboardLayout.setVerticalGroup(
            dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cardPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(monthChart1, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(todayScroll2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(todayScroll1, javax.swing.GroupLayout.PREFERRED_SIZE, 367, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        tabs.addTab("Dashboard", dashboard);

        dayTab.setBackground(new java.awt.Color(244, 244, 244));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("Select Day");
        jLabel2.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(90, 18));

        dayTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "#", "Name", "Quantity"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        dayTable1.setOpaque(false);
        jScrollPane2.setViewportView(dayTable1);
        if (dayTable1.getColumnModel().getColumnCount() > 0) {
            dayTable1.getColumnModel().getColumn(0).setResizable(false);
            dayTable1.getColumnModel().getColumn(1).setResizable(false);
            dayTable1.getColumnModel().getColumn(2).setResizable(false);
        }

        datePicker1.setBackground(new java.awt.Color(244, 244, 244));
        datePicker1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel6.setText("Sales by Day");
        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        jLabel7.setText("Top 10 Most Order Dishes");
        jLabel7.setBackground(new java.awt.Color(255, 255, 255));
        jLabel7.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        jLabel8.setText("Top 10 Most Order Dishes");
        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        dayTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "#", "Name", "Quantity"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        dayTable2.setOpaque(false);
        jScrollPane4.setViewportView(dayTable2);
        if (dayTable2.getColumnModel().getColumnCount() > 0) {
            dayTable2.getColumnModel().getColumn(0).setResizable(false);
            dayTable2.getColumnModel().getColumn(1).setResizable(false);
            dayTable2.getColumnModel().getColumn(2).setResizable(false);
        }

        javax.swing.GroupLayout dayTabLayout = new javax.swing.GroupLayout(dayTab);
        dayTab.setLayout(dayTabLayout);
        dayTabLayout.setHorizontalGroup(
            dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dayTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dayTabLayout.createSequentialGroup()
                        .addComponent(dayCard1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dayCard2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dayCard3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(6, 6, 6))
                    .addGroup(dayTabLayout.createSequentialGroup()
                        .addComponent(dayChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(dayTabLayout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(dayTabLayout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(datePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel6))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(dayTabLayout.createSequentialGroup()
                        .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(dayTabLayout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(dayTabLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 328, Short.MAX_VALUE)))
                        .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                            .addGroup(dayTabLayout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        dayTabLayout.setVerticalGroup(
            dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dayTabLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(datePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dayCard2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dayCard3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dayCard1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dayChart, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabs.addTab("Daily Report", dayTab);

        monthTab.setBackground(new java.awt.Color(244, 244, 244));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("Select Month");
        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(90, 18));

        monthTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "#", "Name", "Quantity"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(monthTable1);
        if (monthTable1.getColumnModel().getColumnCount() > 0) {
            monthTable1.getColumnModel().getColumn(0).setResizable(false);
            monthTable1.getColumnModel().getColumn(1).setResizable(false);
            monthTable1.getColumnModel().getColumn(2).setResizable(false);
        }

        jLabel3.setText("Top 10 Most Order Dishes");
        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        jLabel4.setText("Sales by Month");
        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        jLabel5.setText("Top 10 Categories");
        jLabel5.setBackground(new java.awt.Color(255, 255, 255));
        jLabel5.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        monthTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "#", "Name", "Quantity"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(monthTable2);
        if (monthTable2.getColumnModel().getColumnCount() > 0) {
            monthTable2.getColumnModel().getColumn(0).setResizable(false);
            monthTable2.getColumnModel().getColumn(1).setResizable(false);
            monthTable2.getColumnModel().getColumn(2).setResizable(false);
        }

        comboMonth.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboMonth.setMinimumSize(new java.awt.Dimension(72, 25));
        comboMonth.setPreferredSize(new java.awt.Dimension(72, 25));
        comboMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboMonthActionPerformed(evt);
            }
        });

        comboYear.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboYear.setMinimumSize(new java.awt.Dimension(72, 25));
        comboYear.setPreferredSize(new java.awt.Dimension(100, 25));

        javax.swing.GroupLayout monthTabLayout = new javax.swing.GroupLayout(monthTab);
        monthTab.setLayout(monthTabLayout);
        monthTabLayout.setHorizontalGroup(
            monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(monthTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(monthTabLayout.createSequentialGroup()
                        .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(monthChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(monthTabLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel4)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(monthTabLayout.createSequentialGroup()
                                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(monthTabLayout.createSequentialGroup()
                                        .addGap(9, 9, 9)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(comboMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(comboYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(monthCard1, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(monthTabLayout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(monthCard2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(monthCard3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(monthTabLayout.createSequentialGroup()
                        .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(monthTabLayout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(monthTabLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                            .addGroup(monthTabLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jLabel5)
                                .addContainerGap())))))
        );
        monthTabLayout.setVerticalGroup(
            monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(monthTabLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(comboMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(comboYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(monthCard1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(monthCard2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(monthCard3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(monthChart, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        tabs.addTab("Monthly Report", monthTab);

        javax.swing.GroupLayout yearTabLayout = new javax.swing.GroupLayout(yearTab);
        yearTab.setLayout(yearTabLayout);
        yearTabLayout.setHorizontalGroup(
            yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1036, Short.MAX_VALUE)
        );
        yearTabLayout.setVerticalGroup(
            yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 835, Short.MAX_VALUE)
        );

        tabs.addTab("Annual Report", yearTab);

        jLabel9.setText("Dashboard");
        jLabel9.setFont(new java.awt.Font("Helvetica Neue", 1, 24)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(204, 0, 51));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 1038, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel9)
                .addGap(15, 15, 15)
                .addComponent(tabs, javax.swing.GroupLayout.PREFERRED_SIZE, 872, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void comboMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboMonthActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboMonthActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mycompany.quanlyquanan.view.Reports.CardGradient card1;
    private com.mycompany.quanlyquanan.view.Reports.CardPanel cardPanel2;
    private javax.swing.JComboBox<String> comboMonth;
    private javax.swing.JComboBox<String> comboYear;
    private javax.swing.JPanel dashboard;
    private com.github.lgooddatepicker.components.DatePicker datePicker1;
    private com.mycompany.quanlyquanan.view.Reports.Card dayCard1;
    private com.mycompany.quanlyquanan.view.Reports.Card dayCard2;
    private com.mycompany.quanlyquanan.view.Reports.Card dayCard3;
    private com.mycompany.quanlyquanan.view.Reports.ChartPanel dayChart;
    private javax.swing.JPanel dayTab;
    private com.mycompany.quanlyquanan.view.Reports.Table dayTable1;
    private com.mycompany.quanlyquanan.view.Reports.Table dayTable2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private com.mycompany.quanlyquanan.view.Reports.Card monthCard1;
    private com.mycompany.quanlyquanan.view.Reports.Card monthCard2;
    private com.mycompany.quanlyquanan.view.Reports.Card monthCard3;
    private com.mycompany.quanlyquanan.view.Reports.ChartPanel monthChart;
    private com.mycompany.quanlyquanan.view.Reports.ChartPanel monthChart1;
    private javax.swing.JPanel monthTab;
    private com.mycompany.quanlyquanan.view.Reports.Table monthTable1;
    private com.mycompany.quanlyquanan.view.Reports.Table monthTable2;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JScrollPane todayScroll1;
    private javax.swing.JScrollPane todayScroll2;
    private com.mycompany.quanlyquanan.view.Reports.Table todayTable1;
    private com.mycompany.quanlyquanan.view.Reports.Table todayTable2;
    private javax.swing.JPanel yearTab;
    // End of variables declaration//GEN-END:variables

  
}
