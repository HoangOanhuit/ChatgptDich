/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.quanlyquanan.view.Reports;

import com.mycompany.quanlyquanan.model.ModelCard;
import java.awt.Color;
import java.awt.GradientPaint;
import javax.swing.Icon;

import com.mycompany.quanlyquanan.model.ModelChart;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import java.time.LocalDate;

/**
 *
 * @author macos
 */
public class TabbedPane extends javax.swing.JPanel {

 
    public TabbedPane() {
        initComponents();
        initComboMonthYear();
        IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
        initData();
    }

    private void initComboMonthYear() {
        comboMonth.removeAllItems();
        for (int i = 1; i <= 12; i++) {
            comboMonth.addItem("Tháng " + i);
        }
        comboYear.removeAllItems();
        for (int y = 2020; y <= 2030; y++) {
            comboYear.addItem(String.valueOf(y));
        }
        // Set default selected month/year to current
        LocalDate now = LocalDate.now();
        comboMonth.setSelectedIndex(now.getMonthValue() - 1); // 0-based index
        comboYear.setSelectedItem(String.valueOf(now.getYear()));
    }

    public void initData(){
        initDailyReportData();
        initMonthlyReportData();
        initAnnualReportData();
    }

    // Khởi tạo dữ liệu cho tab Daily Report
    private void initDailyReportData() {
        // [TODO] - lấy dữ liệu cho Daily Report
        initDailyCardData();
        initDailyTable1Data();
        initDailyChartData();
        dayTable1.fixTable(jScrollPane2);
        dayTable2.fixTable(jScrollPane4);

    }

    private void initDailyChartData() {
        // [TODO] - cần lấy dữ liệu từ database
        dayChart.addChartData(new ModelChart("Breakfast", new double[]{500, 200, 80}));
        dayChart.addChartData(new ModelChart("Lunch", new double[]{600, 200, 80}));
        dayChart.addChartData(new ModelChart("Dinner", new double[]{500, 600, 100}));
        dayChart.addChartData(new ModelChart("Snacks", new double[]{300, 300, 80}));
        dayChart.addChartData(new ModelChart("Drinks", new double[]{500, 200, 60}));
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


    // Khởi tạo dữ liệu cho tab Monthly Report
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

    // [TODO] - cần lấy dữ liệu từ database
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

        tabs = new javax.swing.JTabbedPane();
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
        comboYear = new javax.swing.JComboBox<>();
        comboMonth = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        monthTable2 = new com.mycompany.quanlyquanan.view.Reports.Table();
        yearTab = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));

        tabs.setBackground(new java.awt.Color(255, 255, 255));
        tabs.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tabs.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        tabs.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        tabs.setPreferredSize(new java.awt.Dimension(829, 1013));

        dayTab.setBackground(new java.awt.Color(244, 244, 244));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("Select Day");
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel2.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

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
        jScrollPane2.setViewportView(dayTable1);
        if (dayTable1.getColumnModel().getColumnCount() > 0) {
            dayTable1.getColumnModel().getColumn(0).setResizable(false);
            dayTable1.getColumnModel().getColumn(1).setResizable(false);
            dayTable1.getColumnModel().getColumn(2).setResizable(false);
        }

        datePicker1.setBackground(new java.awt.Color(244, 244, 244));
        datePicker1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel6.setText("Sales by Month");
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
                        .addGap(18, 18, 18)
                        .addComponent(dayCard3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(6, 6, 6))
                    .addGroup(dayTabLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(dayTabLayout.createSequentialGroup()
                        .addComponent(dayChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(dayTabLayout.createSequentialGroup()
                        .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(dayTabLayout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(datePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(dayTabLayout.createSequentialGroup()
                                .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jScrollPane2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane4)
                                    .addGroup(dayTabLayout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addGap(0, 0, Short.MAX_VALUE)))))
                        .addContainerGap())))
        );
        dayTabLayout.setVerticalGroup(
            dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dayTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(datePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(dayTabLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                    .addComponent(jScrollPane4)
                    .addComponent(jScrollPane2))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        tabs.addTab("Daily Report", dayTab);

        monthTab.setBackground(new java.awt.Color(244, 244, 244));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("Select Month");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

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

        comboYear.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        comboMonth.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboMonthActionPerformed(evt);
            }
        });

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
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, monthTabLayout.createSequentialGroup()
                                .addComponent(monthCard1, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(monthCard2, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(monthCard3, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
                            .addGroup(monthTabLayout.createSequentialGroup()
                                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(monthTabLayout.createSequentialGroup()
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(comboMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(comboYear, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel4))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(monthTabLayout.createSequentialGroup()
                        .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jScrollPane1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3)
                            .addComponent(jLabel5)))))
        );
        monthTabLayout.setVerticalGroup(
            monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(monthTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(comboMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(comboYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(monthCard1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(monthCard2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(monthCard3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(monthChart, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        tabs.addTab("Monthly Report", monthTab);

        javax.swing.GroupLayout yearTabLayout = new javax.swing.GroupLayout(yearTab);
        yearTab.setLayout(yearTabLayout);
        yearTabLayout.setHorizontalGroup(
            yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1006, Short.MAX_VALUE)
        );
        yearTabLayout.setVerticalGroup(
            yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 976, Short.MAX_VALUE)
        );

        tabs.addTab("Annual Report", yearTab);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void comboMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboMonthActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboMonthActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> comboMonth;
    private javax.swing.JComboBox<String> comboYear;
    private com.github.lgooddatepicker.components.DatePicker datePicker1;
    private com.mycompany.quanlyquanan.view.Reports.Card dayCard1;
    private com.mycompany.quanlyquanan.view.Reports.Card dayCard2;
    private com.mycompany.quanlyquanan.view.Reports.Card dayCard3;
    private com.mycompany.quanlyquanan.view.Reports.ChartPanel dayChart;
    private javax.swing.JPanel dayTab;
    private com.mycompany.quanlyquanan.view.Reports.Table dayTable1;
    private com.mycompany.quanlyquanan.view.Reports.Table dayTable2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private com.mycompany.quanlyquanan.view.Reports.Card monthCard1;
    private com.mycompany.quanlyquanan.view.Reports.Card monthCard2;
    private com.mycompany.quanlyquanan.view.Reports.Card monthCard3;
    private com.mycompany.quanlyquanan.view.Reports.ChartPanel monthChart;
    private javax.swing.JPanel monthTab;
    private com.mycompany.quanlyquanan.view.Reports.Table monthTable1;
    private com.mycompany.quanlyquanan.view.Reports.Table monthTable2;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JPanel yearTab;
    // End of variables declaration//GEN-END:variables
}
