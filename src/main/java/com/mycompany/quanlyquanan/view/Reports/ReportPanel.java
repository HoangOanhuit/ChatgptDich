package com.mycompany.quanlyquanan.view.Reports;

import com.mycompany.quanlyquanan.controller.ExpenseReceiptController;

import com.mycompany.quanlyquanan.controller.OrderController;
import com.mycompany.quanlyquanan.controller.TopDishesController;
import com.mycompany.quanlyquanan.model.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;

import com.mycompany.quanlyquanan.utils.ComponentStyleUtil;
import com.mycompany.quanlyquanan.view.VoucherMgt.MyTable;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author macos
 */
public class ReportPanel extends javax.swing.JPanel {
    ExpenseReceiptController expenseController;
    OrderController orderController;
    TopDishesController topDishesController;

    DefaultTableModel yearOrderTableModel, monthOrderTableModel, dayOrderTableModel, todayOrderTableModel;
    DefaultTableModel yearDishesTableModel, monthDishesTableModel, dayDishesTableModel, todayDishesTableModel; // Table 2 tab Daily, Monthly, Annual Report
    LocalDate selectedDate; // Dùng cho tab Daily Report và Today Report lấy từ field datePicker1
    int selectedMonth, selectedYear; // Dùng cho tab Monthly Report lấy từ field comboMonth, comboYear
    int selectedYearAnnualReport; // Dùng cho tab Annual Report lấy từ field comboYear1

    Icon incomeIcon = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ACCOUNT_BALANCE_WALLET, 60, new Color(112, 69, 246, 50));
    Icon expenseIcon = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.MONETIZATION_ON, 60, new Color(0, 191, 255, 50));
    Icon revenueIcon = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SHOW_CHART, 60, new Color(255, 140, 0, 50));


    public ReportPanel() {
        initComponents();
        IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
        orderController = new OrderController();
        expenseController = new ExpenseReceiptController();
        topDishesController = new TopDishesController();

        styleTabUIs(); // Custom UI for tab color
        initData(); // Load data for all tabs
    }

    private void styleTabUIs() {
        tabs.setOpaque(false);
        tabs.setBackground(new Color(0, 0, 0, 0)); // transparent background
        tabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement,
                                              int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                // Đổi màu nền tab được chọn và chưa chọn
                g.setColor(isSelected ? new Color(255, 235, 238) : new Color(230, 230, 230));
                g.fillRect(x, y, w, h);
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement,
                                          int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement,
                                              int selectedIndex) {
            }
        });

        tabs.setFocusable(false);
        tabs.setSelectedIndex(0);// Đặt tab mặc định là Dashboard (index 0)
        tabs.addChangeListener(e -> tabs.repaint());  // Force repaint when tab selection changes

        // Đổi màu chữ cho tab được chọn và chưa chọn
        tabs.addChangeListener(e -> {
            for (int i = 0; i < tabs.getTabCount(); i++) {
                tabs.setForegroundAt(i, i == tabs.getSelectedIndex() ? new Color(205, 0, 51) : Color.BLACK);
            }
        });

        // Cập nhật màu chữ cho tab được chọn ngay khi khởi tạo
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setForegroundAt(i, i == 0 ? new Color(205, 0, 51) : Color.BLACK);
        }
    }

    // Khởi tạo dữ liệu cho tất cả các tab
    public void initData() {
        initTodayReportData();
        initDailyReportData();
        initMonthlyReportData();
        initAnnualReportData();
    }

    // Khởi tạo dữ liệu cho tab Dashboard (Today Report)
    private void initTodayReportData() {
//        initTodayCardData(); // TodayCardData lấy từ dashboardCard (cardPanel) nên không nằm ở đây
        initTodayOrderTableData();
        initTodayDishesTableData();
        initTodayChartData();
        todayTable1.fixTable(todayScroll1);
        todayTable2.fixTable(todayScroll2);
        ComponentStyleUtil.styleMainButton(refreshButton);
    }

    private void initTodayDishesTableData() {
        // Format table
        todayDishesTableModel = setUpDishTable(todayTable2);

        // Fetch top highest orders today
        List<TopDishes> dishes = topDishesController.getDayTopDishes(LocalDate.now());
        setTodayDishesTableData(todayDishesTableModel, dishes);
    }

    // Dùng chung cho cả 4 tabs Daily, Monthly, Annual Report và Today Report
    private DefaultTableModel setUpDishTable(Table table) {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Vô hiệu hóa chỉnh sửa ô
            }
        };

        // Set column names
        tableModel.addColumn("ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Quantity");

        // Set the model to the table
        table.setModel(tableModel);
        table.getColumn("ID").setCellRenderer(MyTable.getLeftRenderer(12));
        table.getColumn("Name").setCellRenderer(MyTable.getLeftRenderer(12));
        table.getColumn("Quantity").setCellRenderer(MyTable.getRightRenderer(12));
        return tableModel;
    }

    private void setTodayDishesTableData(DefaultTableModel tableModel, List<TopDishes> dishes) {
        tableModel.setRowCount(0);
        for (TopDishes dish : dishes) {
            tableModel.addRow(new Object[]{
                    dish.getId(),
                    dish.getName(),
                    dish.getTotalQuantity(),
            });
        }

    }

    private void initTodayOrderTableData() {
        // Format table
        todayOrderTableModel = setUpOrderTable(todayTable1);

        // Fetch top highest orders today
        List<Order> orders = orderController.getDayTopOrderByAmount(LocalDate.now());
        setTodayOrderTableData(todayOrderTableModel, orders);
    }

    // Dùng chung cho cả 4 tabs Daily, Monthly, Annual Report và Today Report
    private DefaultTableModel setUpOrderTable(Table table) {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Vô hiệu hóa chỉnh sửa ô
            }
        };

        // Set column names
        tableModel.addColumn("ID");
        tableModel.addColumn("Created At");
        tableModel.addColumn("Total Amount");

        // set size for each column
        table.getColumnModel().getColumn(0).setPreferredWidth(10);
        table.getColumnModel().getColumn(0).setMaxWidth(10);
        table.getColumnModel().getColumn(0).setMinWidth(10);


        // Set the model to the table
        table.setModel(tableModel);
        table.getColumn("ID").setCellRenderer(MyTable.getLeftRenderer(12));
        table.getColumn("Created At").setCellRenderer(MyTable.getLeftRenderer(12));
        table.getColumn("Total Amount").setCellRenderer(MyTable.getRightRenderer(12));
        return tableModel;
    }

    private void setTodayOrderTableData(DefaultTableModel tableModel, List<Order> orders) {
        tableModel.setRowCount(0);
        for (Order order : orders) {
            tableModel.addRow(new Object[]{
                    order.getId(),
                    order.getCreated_at(),
                    order.getTotal_amount(),
            });
        }
    }

    private void initTodayChartData() {
        // [TODO] - cần lấy dữ liệu từ database
//        setDailyChartData(selectedDate);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(5);
        Map<LocalDate, Double> expenseMap = expenseController.getTotalExpenseByDays(startDate, endDate);
        Map<LocalDate, Double> incomeMap = orderController.getTotalAmountByDays(startDate, endDate);

        for (int i = 0; i < 6; i++) {
            LocalDate date = startDate.plusDays(i);
            double todayExpense = expenseMap.getOrDefault(date, 0.0);
            double todayIncome = incomeMap.getOrDefault(date, 0.0);
            double todayRevenue = todayIncome - todayExpense;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String formattedDate = date.format(formatter);
            dashboardChart.addChartData(new ModelChart(formattedDate, new double[]{todayIncome, todayExpense, todayRevenue}));
        }


    }

    // Khởi tạo dữ liệu cho tab Daily Report
    private void initDailyReportData() {
        // Format table
        dayTable1.fixTable(dayScroll1);
        dayTable2.fixTable(dayScroll2);

        // Format DatePicker
        datePicker1.getComponentDateTextField().setFont(ComponentStyleUtil.COMMON_FONT);
        datePicker1.getComponentDateTextField().setBackground(Color.WHITE);
        datePicker1.getComponentDateTextField().setForeground(ComponentStyleUtil.TEXT_COLOR);

        // Set default date in DatePicker to today
        if (selectedDate == null) {
            selectedDate = LocalDate.now();
        }
        datePicker1.setDate(selectedDate);

        // Load data for first time
        initDailyCardData();
        initDailyOrderTableData();
        initDailyDishesTableData();
        initDailyChartData();

        // Refresh data when date is changed
        datePicker1.addDateChangeListener(e -> {
            selectedDate = datePicker1.getDate(); // Luôn cập nhật selectedDate
            refreshDailyReportData(selectedDate);
        });
    }

    // Method to refresh data when date is changed
    private void refreshDailyReportData(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
        setDailyCardData();
        updateDailyChartData(); // Cập nhật dữ liệu cho Chart
        List<Order> orders = orderController.getDayTopOrderByAmount(selectedDate);
        List<TopDishes> dishes = topDishesController.getDayTopDishes(selectedDate);
        setDailyOrderTableData(dayOrderTableModel, orders);// Cập nhật dữ liệu cho Table 1 (bổ sung dòng này nếu chưa có)
        setDailyDishesTableData(dayDishesTableModel, dishes);
        System.out.println("Selected Date: " + selectedDate); // Debug line
        stylePlainCardUI(dayCard1, dayCard2, dayCard3); // format lại UI để Đổi màu tiền lãi/lỗ
    }

    private void initDailyCardData() {
        setDailyCardData();
        stylePlainCardUI(dayCard1,dayCard2,dayCard3);// Format CardUI
    }

    private void setDailyCardData() {
        double dayIncome = orderController.getTotalAmountByDay(selectedDate);
        double dayExpense = expenseController.getTotalExpenseByDay(selectedDate);
        double dayRevenue = dayIncome - dayExpense;
        dayCard1.setData(new ModelCard("Doanh thu", dayIncome, incomeIcon));
        dayCard2.setData(new ModelCard("Chi phí", dayExpense, expenseIcon));
        dayCard3.setData(new ModelCard("Lợi nhuận", dayRevenue, revenueIcon));
    }

    private void initDailyChartData() {
        LocalDate endDate = selectedDate;
        LocalDate startDate = endDate.minusDays(5);
        Map<LocalDate, Double> expenseMap = expenseController.getTotalExpenseByDays(startDate, endDate);
        Map<LocalDate, Double> incomeMap = orderController.getTotalAmountByDays(startDate, endDate);

        for (int i = 0; i < 6; i++) {
            LocalDate date = startDate.plusDays(i);
            double dailyExpense = expenseMap.getOrDefault(date, 0.0);
            double dailyIncome = incomeMap.getOrDefault(date, 0.0);
            double dailyRevenue = dailyIncome - dailyExpense;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String formatedDate = date.format(formatter);
            dayChart.addChartData(new ModelChart(formatedDate, new double[]{dailyIncome, dailyExpense, dailyRevenue}));
        }
    }

    private void updateDailyChartData() {
        dayChart.clearChartData();
        initDailyChartData();
    }

    private void initDailyDishesTableData() {
        dayDishesTableModel = setUpDishTable(dayTable2);
        List<TopDishes> dishes = topDishesController.getDayTopDishes(selectedDate);
        setDailyDishesTableData(dayDishesTableModel,dishes);
    }

    private void setDailyDishesTableData(DefaultTableModel tableModel, List<TopDishes> dishes) {
        tableModel.setRowCount(0);
        for (TopDishes dish  : dishes) {
            tableModel.addRow(new Object[]{
                    dish.getId(),
                    dish.getName(),
                    dish.getTotalQuantity()
            });
        }

    }


    private void initDailyOrderTableData() {
        dayOrderTableModel = setUpOrderTable(dayTable1);
        List<Order> orders = orderController.getDayTopOrderByAmount(selectedDate);
        setDailyOrderTableData(dayOrderTableModel,orders);
    }

    private void setDailyOrderTableData(DefaultTableModel tableModel, List<Order> orders) {
        tableModel.setRowCount(0);
        for (Order order : orders) {
            tableModel.addRow(new Object[]{
                    order.getId(),
                    order.getCreated_at(),
                    order.getTotal_amount(),
            });
        }
    }

    // Dùng chung cho cả 3 tabs Daily, Monthly, Annual Report
    private void stylePlainCardUI(Card card1, Card card2, Card card3) {
        card1.setMoneycolor(new Color(49, 174, 40));
        card1.setBottomBorderColor(new GradientPaint(
                0, 0, new Color(205, 139, 237), getWidth(), getHeight(), new Color(255, 153, 153))); // pink

        card2.setMoneycolor(new Color(248, 35, 74));
        card2.setBottomBorderColor(new GradientPaint(
                0, 0, new Color(0, 191, 255), getWidth(), getHeight(), new Color(255, 204, 255))); // green

        card3.setBottomBorderColor(new GradientPaint(
                0, 0, new Color(255, 140, 0), getWidth(), getHeight(), new Color(255, 204, 204))); // red

        // Đổi màu tiền lãi/lỗ
        if (card3.getValue() < 0) {
            card3.setMoneycolor(new Color(248, 35, 74));
        } else {
            card3.setMoneycolor(new Color(49, 174, 40));
        }
    }

    // Khoi tạo dữ liệu cho tab Monthly Report
    private void initMonthlyReportData() {
        initComboMonthYear();
        initMonthlyCardData();
        initMonthlyDishesTableData();
        initMonthlyOrderTableData();
        initMonthlyChartData();

        monthTable1.fixTable(monthScroll1);
        monthTable2.fixTable(monthScroll2);

        //Add change listener cho combobox tháng và năm
        comboMonth.addActionListener(e -> {
            selectedMonth = comboMonth.getSelectedIndex() + 1; // comboBox tháng bắt đầu từ 0
            selectedYear = (Integer) comboYear.getSelectedItem();
            refreshMonthlyReportData();
        });

        comboYear.addActionListener(e -> {
            selectedMonth = comboMonth.getSelectedIndex() + 1; // comboBox tháng bắt đầu từ 0
            selectedYear = (Integer) comboYear.getSelectedItem();
            refreshMonthlyReportData();
        });
    }

    private void refreshMonthlyReportData() {
        setMonthlyCardData(); // Cập nhật dữ liệu cho Card

        List<Order> monthOrders = orderController.getMonthTopOrderByAmount(selectedMonth, selectedYear);
        setMonthlyOrderTableData(monthOrderTableModel, monthOrders); // Cập nhật dữ liệu cho Table 1

        List<TopDishes> monthDishes = topDishesController.getMonthTopDishes(selectedMonth, selectedYear);
        setMonthlyDishesTableData(monthDishesTableModel, monthDishes); // Cập nhật dữ liệu cho Table 2

        updateMonthlyChartData(); // Cập nhật dữ liệu cho Chart
        stylePlainCardUI(monthCard1, monthCard2, monthCard3); // format lại UI để Đổi màu tiền lãi/lỗ
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
        for (int y = 2020; y <= 2050; y++) {
            comboYear.addItem(y);
        }
        // Set default selected month/year to current
        LocalDate now = LocalDate.now();
        selectedMonth = now.getMonthValue() ;
        selectedYear = now.getYear();
        comboMonth.setSelectedIndex(selectedMonth-1); // 0-based index
        comboYear.setSelectedItem(selectedYear);
        ComponentStyleUtil.styleComboBox(comboMonth);
        ComponentStyleUtil.styleComboBox(comboYear);
    }

    public void initMonthlyCardData() {
        setMonthlyCardData();
        stylePlainCardUI(monthCard1,monthCard2,monthCard3);
    }

    private void setMonthlyCardData() {
        double monthIncome = orderController.getTotalAmountByMonth(selectedMonth, selectedYear);
        double monthExpense = expenseController.getTotalExpenseByMonth(selectedMonth, selectedYear);
        double monthRevenue = monthIncome - monthExpense;
        monthCard1.setData(new ModelCard("Doanh thu", monthIncome, incomeIcon));
        monthCard2.setData(new ModelCard("Chi phí", monthExpense, expenseIcon));
        monthCard3.setData(new ModelCard("Lợi nhuận", monthRevenue, revenueIcon));
    }


    public void initMonthlyChartData() {
        LocalDate endDate = LocalDate.of(selectedYear, selectedMonth + 1 , 1); //vd chọn tháng 9 thì sql xét dk < 01/10
        LocalDate startDate = endDate.minusMonths(6).withDayOfMonth(1);
        Map<Integer, Double> expenseMap = expenseController.getTotalExpenseByMonths(startDate, endDate);
        Map<Integer, Double> incomeMap = orderController.getTotalAmountByMonths(startDate, endDate);

        for (int i = 0; i < 6; i++) {
            LocalDate chartDate = startDate.plusMonths(i);
            int chartMonth = chartDate.getMonthValue();
            int chartYear = chartDate.getYear();
            double monthExpense = expenseMap.getOrDefault(chartMonth, 0.0);
            double monthIncome = incomeMap.getOrDefault(chartMonth, 0.0);
            double monthRevenue = monthIncome - monthExpense;
            monthChart.addChartData(new ModelChart(chartMonth + "-" + chartYear, new double[]{monthIncome, monthExpense, monthRevenue}));
        }
    }

    private void updateMonthlyChartData() {
        monthChart.clearChartData();
        initMonthlyChartData();
    }

    public void initMonthlyDishesTableData() {
//        table1.addRowSelectionInterval(new ModelVoucher);
        monthDishesTableModel = setUpDishTable(monthTable2);
        List<TopDishes> dishes = topDishesController.getMonthTopDishes(selectedMonth, selectedYear);
        setMonthlyDishesTableData(monthDishesTableModel, dishes);
    }


    private void setMonthlyDishesTableData(DefaultTableModel tableModel, List<TopDishes> dishes) {
//        List<Order> orders = orderController.getMonthTopOrderByAmount(selectedMonth, selectedYear);
        tableModel.setRowCount(0);
        for (TopDishes dish: dishes) {
            tableModel.addRow(new Object[]{
                    dish.getId(),
                    dish.getName(),
                    dish.getTotalQuantity(),
            });
        }
    }

    public void initMonthlyOrderTableData() {
//        table1.addRowSelectionInterval(new ModelVoucher);
        monthOrderTableModel = setUpOrderTable(monthTable1);
        List<Order> orders = orderController.getMonthTopOrderByAmount(selectedMonth, selectedYear);
        setMonthlyOrderTableData(monthOrderTableModel,orders);
    }


    private void setMonthlyOrderTableData(DefaultTableModel tableModel, List<Order> orders) {
//        List<Order> orders = orderController.getMonthTopOrderByAmount(selectedMonth, selectedYear);
        tableModel.setRowCount(0);
        for (Order order : orders) {
            tableModel.addRow(new Object[]{
                    order.getId(),
                    order.getCreated_at(),
                    order.getTotal_amount(),
            });
        }
    }



    // Khởi tạo dữ liệu cho tab Annual Report
    private void initAnnualReportData() {
        // [TODO] - lấy dữ liệu cho Annual Report
        initComboYear();
        initYearCardData();
        initYearChartData();
        initYearOrderTableData();
        initYearDishesTableData();

        yearTable1.fixTable(YearScroll1);
        yearTable2.fixTable(yearScroll2);

        //Add change listener cho combobox tháng và năm
        comboYear1.addActionListener(e -> {
                  selectedYearAnnualReport = (Integer) comboYear1.getSelectedItem();
            refreshYearReportData(selectedYearAnnualReport);
        });

    }

    private void refreshYearReportData(int selectedYearAnnualReport) {
        setYearCardData();// Cập nhật dữ liệu cho Card

        List<Order> yearOrders = orderController.getYearTopOrderByAmount(selectedYearAnnualReport);
        setYearOrderTableData(yearOrderTableModel, yearOrders); // Cập nhật dữ

        List<TopDishes> yearDishes = topDishesController.getYearTopDishes(selectedYearAnnualReport);
        setYearDishesTableData(yearDishesTableModel, yearDishes); // Cập nhật dữ liệu cho Table 2

        updateYearChartData();// Cập nhật dữ liệu cho Chart
        stylePlainCardUI(yearCard1,yearCard2,yearCard3);   // format lại UI để Đổi màu tiền lãi/lỗ
    }

    private void initYearCardData() {
        setYearCardData();
        stylePlainCardUI(yearCard1,yearCard2,yearCard3);
    }

    private void setYearCardData() {
        double yearIncome = orderController.getTotalAmountByYear(selectedYearAnnualReport);
        double yearExpense = expenseController.getTotalExpenseByYear(selectedYearAnnualReport);
        double yearRevenue = yearIncome - yearExpense;
        yearCard1.setData(new ModelCard("Doanh thu", yearIncome, incomeIcon));
        yearCard2.setData(new ModelCard("Chi phí", yearExpense, expenseIcon));
        yearCard3.setData(new ModelCard("Lợi nhuận", yearRevenue, revenueIcon));
    }
    private void initYearChartData() {
        int endYear = selectedYearAnnualReport;
        int startYear = selectedYearAnnualReport - 5;

        Map<Integer, Double> expenseMap = expenseController.getTotalExpenseByYears(startYear, endYear);
        Map<Integer, Double> incomeMap = orderController.getTotalAmountByYears(startYear, endYear);

        for (int i = 0; i < 6; i++) {
            int chartYear = startYear + i;
            double yearExpense = expenseMap.getOrDefault(chartYear, 0.0);
            double yearIncome = incomeMap.getOrDefault(chartYear, 0.0);
            double yearRevenue = yearIncome - yearExpense;
            yearChart.addChartData(new ModelChart(String.valueOf(chartYear), new double[]{yearIncome, yearExpense, yearRevenue}));
        }
    }

    private void updateYearChartData() {
        yearChart.clearChartData();
        initYearChartData();
    }

    private void initYearDishesTableData() {
        yearDishesTableModel = setUpDishTable(yearTable2);
        List<TopDishes> dishes = topDishesController.getYearTopDishes(selectedYearAnnualReport);
        setYearDishesTableData(yearDishesTableModel, dishes);
    }

    private void setYearDishesTableData(DefaultTableModel tableModel, List<TopDishes> dishes) {
//        List<Order> orders = orderController.getYearTopOrderByAmount(selectedYearAnnualReport);
        tableModel.setRowCount(0);
        for (TopDishes dish: dishes) {
            tableModel.addRow(new Object[]{
                    dish.getId(),
                    dish.getName(),
                    dish.getTotalQuantity()
            });
        }
    }

    private void initYearOrderTableData() {
        yearOrderTableModel = setUpOrderTable(yearTable1);
        List<Order> orders = orderController.getYearTopOrderByAmount(selectedYearAnnualReport);
        setYearOrderTableData(yearOrderTableModel, orders);
    }

    private void setYearOrderTableData(DefaultTableModel tableModel, List<Order> orders) {
//        List<Order> orders = orderController.getYearTopOrderByAmount(selectedYearAnnualReport);
        tableModel.setRowCount(0);
        for (Order order : orders) {
            tableModel.addRow(new Object[]{
                    order.getId(),
                    order.getCreated_at(),
                    order.getTotal_amount()
            });
        }
    }
    private void initComboYear() {
        comboYear1.removeAllItems();
        for (int y = 2020; y <= 2050; y++) {
            comboYear1.addItem(y);
        }
        // Set default selected year to current
        LocalDate now = LocalDate.now();
        selectedYearAnnualReport = now.getYear();
        comboYear1.setSelectedItem(selectedYearAnnualReport);
        ComponentStyleUtil.styleComboBox(comboYear1);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        card1 = new com.mycompany.quanlyquanan.view.Reports.CardGradient();
        tabs = new javax.swing.JTabbedPane();
        dashboard = new javax.swing.JPanel();
        dashboardChart = new com.mycompany.quanlyquanan.view.Reports.ChartPanel();
        dashboardCard = new com.mycompany.quanlyquanan.view.Reports.CardPanel();
        dashboardChartTitle = new javax.swing.JLabel();
        dashboardTable1Title = new javax.swing.JLabel();
        todayScroll1 = new javax.swing.JScrollPane();
        todayTable1 = new com.mycompany.quanlyquanan.view.Reports.Table();
        todayScroll2 = new javax.swing.JScrollPane();
        todayTable2 = new com.mycompany.quanlyquanan.view.Reports.Table();
        dashboardTable2Title = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        dayTab = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        dayCard1 = new com.mycompany.quanlyquanan.view.Reports.Card();
        dayCard2 = new com.mycompany.quanlyquanan.view.Reports.Card();
        dayCard3 = new com.mycompany.quanlyquanan.view.Reports.Card();
        dayScroll1 = new javax.swing.JScrollPane();
        dayTable1 = new com.mycompany.quanlyquanan.view.Reports.Table();
        dayChart = new com.mycompany.quanlyquanan.view.Reports.ChartPanel();
        datePicker1 = new com.github.lgooddatepicker.components.DatePicker();
        dayChartTitle = new javax.swing.JLabel();
        dayTable1Title = new javax.swing.JLabel();
        dayTable2Title = new javax.swing.JLabel();
        dayScroll2 = new javax.swing.JScrollPane();
        dayTable2 = new com.mycompany.quanlyquanan.view.Reports.Table();
        monthTab = new javax.swing.JPanel();
        selectMonthLbl = new javax.swing.JLabel();
        comboMonth = new javax.swing.JComboBox<>();
        comboYear = new javax.swing.JComboBox<>();
        monthCard1 = new com.mycompany.quanlyquanan.view.Reports.Card();
        monthCard2 = new com.mycompany.quanlyquanan.view.Reports.Card();
        monthCard3 = new com.mycompany.quanlyquanan.view.Reports.Card();
        MonthChartTitle = new javax.swing.JLabel();
        monthChart = new com.mycompany.quanlyquanan.view.Reports.ChartPanel();
        MonthTable1Title = new javax.swing.JLabel();
        monthScroll1 = new javax.swing.JScrollPane();
        monthTable1 = new com.mycompany.quanlyquanan.view.Reports.Table();
        MonthTable2Title = new javax.swing.JLabel();
        monthScroll2 = new javax.swing.JScrollPane();
        monthTable2 = new com.mycompany.quanlyquanan.view.Reports.Table();
        yearTab = new javax.swing.JPanel();
        selectYearLbl = new javax.swing.JLabel();
        comboYear1 = new javax.swing.JComboBox<>();
        yearCard1 = new com.mycompany.quanlyquanan.view.Reports.Card();
        yearCard2 = new com.mycompany.quanlyquanan.view.Reports.Card();
        yearCard3 = new com.mycompany.quanlyquanan.view.Reports.Card();
        YearChartTitle = new javax.swing.JLabel();
        yearChart = new com.mycompany.quanlyquanan.view.Reports.ChartPanel();
        yearTable1Title = new javax.swing.JLabel();
        YearScroll1 = new javax.swing.JScrollPane();
        yearTable1 = new com.mycompany.quanlyquanan.view.Reports.Table();
        YearTable2Title = new javax.swing.JLabel();
        yearScroll2 = new javax.swing.JScrollPane();
        yearTable2 = new com.mycompany.quanlyquanan.view.Reports.Table();
        jLabel3 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(1050, 750));

        tabs.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setBackground(new java.awt.Color(244, 244, 244));
        tabs.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tabs.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        tabs.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        tabs.setPreferredSize(new java.awt.Dimension(1050, 940));

        dashboard.setBackground(new java.awt.Color(244, 244, 244));

        dashboardChart.setPreferredSize(new java.awt.Dimension(490, 510));

        dashboardChartTitle.setText("Biểu đồ Doanh số ");
        dashboardChartTitle.setBackground(new java.awt.Color(255, 255, 255));
        dashboardChartTitle.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        dashboardTable1Title.setText("Top các đơn hàng giá trị cao");
        dashboardTable1Title.setBackground(new java.awt.Color(255, 255, 255));
        dashboardTable1Title.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

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

        dashboardTable2Title.setText("Top các món ăn được ưa thích");
        dashboardTable2Title.setBackground(new java.awt.Color(255, 255, 255));
        dashboardTable2Title.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        refreshButton.setText("Refresh");
        refreshButton.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        refreshButton.setPreferredSize(new java.awt.Dimension(90, 30));
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dashboardLayout = new javax.swing.GroupLayout(dashboard);
        dashboard.setLayout(dashboardLayout);
        dashboardLayout.setHorizontalGroup(
            dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardLayout.createSequentialGroup()
                .addGroup(dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dashboardCard, javax.swing.GroupLayout.PREFERRED_SIZE, 678, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dashboardChart, javax.swing.GroupLayout.PREFERRED_SIZE, 678, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(dashboardLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dashboardChartTitle)
                            .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(todayScroll2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(dashboardLayout.createSequentialGroup()
                        .addComponent(dashboardTable2Title)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(dashboardLayout.createSequentialGroup()
                        .addComponent(dashboardTable1Title)
                        .addContainerGap(162, Short.MAX_VALUE))
                    .addComponent(todayScroll1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
        dashboardLayout.setVerticalGroup(
            dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dashboardTable1Title)
                    .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dashboardLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(todayScroll1, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dashboardTable2Title)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(todayScroll2, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dashboardLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(dashboardCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dashboardChartTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dashboardChart, javax.swing.GroupLayout.PREFERRED_SIZE, 471, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        tabs.addTab("Hôm nay", dashboard);

        dayTab.setBackground(new java.awt.Color(244, 244, 244));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("Chọn ngày");
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
        dayScroll1.setViewportView(dayTable1);
        if (dayTable1.getColumnModel().getColumnCount() > 0) {
            dayTable1.getColumnModel().getColumn(0).setResizable(false);
            dayTable1.getColumnModel().getColumn(1).setResizable(false);
            dayTable1.getColumnModel().getColumn(2).setResizable(false);
        }

        datePicker1.setBackground(new java.awt.Color(244, 244, 244));
        datePicker1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        datePicker1.setPreferredSize(new java.awt.Dimension(163, 30));

        dayChartTitle.setText("Biểu đồ Doanh số");
        dayChartTitle.setBackground(new java.awt.Color(255, 255, 255));
        dayChartTitle.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        dayTable1Title.setText("Top các đơn hàng giá trị cao");
        dayTable1Title.setBackground(new java.awt.Color(255, 255, 255));
        dayTable1Title.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        dayTable2Title.setText("Top các món ăn được ưa thích");
        dayTable2Title.setBackground(new java.awt.Color(255, 255, 255));
        dayTable2Title.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

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
        dayScroll2.setViewportView(dayTable2);
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
                .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(dayTabLayout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(dayTabLayout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(datePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(dayChartTitle)))
                    .addGroup(dayTabLayout.createSequentialGroup()
                        .addComponent(dayCard1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dayCard2, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dayCard3, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dayChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dayTabLayout.createSequentialGroup()
                        .addGroup(dayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dayTable1Title)
                            .addComponent(dayTable2Title)
                            .addComponent(dayScroll1, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE))
                        .addContainerGap())
                    .addComponent(dayScroll2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
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
                .addComponent(dayChartTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dayChart, javax.swing.GroupLayout.PREFERRED_SIZE, 505, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(dayTabLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(dayTable1Title)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dayScroll1, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dayTable2Title)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dayScroll2, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        tabs.addTab("Báo cáo ngày", dayTab);

        monthTab.setBackground(new java.awt.Color(244, 244, 244));

        selectMonthLbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        selectMonthLbl.setText("Chọn tháng");
        selectMonthLbl.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        selectMonthLbl.setPreferredSize(new java.awt.Dimension(90, 18));

        comboMonth.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboMonth.setMinimumSize(new java.awt.Dimension(72, 25));
        comboMonth.setPreferredSize(new java.awt.Dimension(72, 25));
        comboMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboMonthActionPerformed(evt);
            }
        });

        comboYear.setMinimumSize(new java.awt.Dimension(72, 25));
        comboYear.setPreferredSize(new java.awt.Dimension(100, 25));

        MonthChartTitle.setText("Biểu đồ Doanh số");
        MonthChartTitle.setBackground(new java.awt.Color(255, 255, 255));
        MonthChartTitle.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        MonthTable1Title.setText("Top các đơn hàng giá trị cao");
        MonthTable1Title.setBackground(new java.awt.Color(255, 255, 255));
        MonthTable1Title.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

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
        monthScroll1.setViewportView(monthTable1);
        if (monthTable1.getColumnModel().getColumnCount() > 0) {
            monthTable1.getColumnModel().getColumn(0).setResizable(false);
            monthTable1.getColumnModel().getColumn(1).setResizable(false);
            monthTable1.getColumnModel().getColumn(2).setResizable(false);
        }

        MonthTable2Title.setText("Top các món ăn được ưa thích");
        MonthTable2Title.setBackground(new java.awt.Color(255, 255, 255));
        MonthTable2Title.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

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
        monthScroll2.setViewportView(monthTable2);
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
                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(monthTabLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(MonthChartTitle))
                    .addGroup(monthTabLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(selectMonthLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(monthTabLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(monthCard1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(monthCard2, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(monthCard3, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(monthTabLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(monthChart, javax.swing.GroupLayout.PREFERRED_SIZE, 672, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(monthTabLayout.createSequentialGroup()
                        .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(MonthTable2Title)
                            .addComponent(MonthTable1Title))
                        .addContainerGap(146, Short.MAX_VALUE))
                    .addComponent(monthScroll1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(monthScroll2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
        monthTabLayout.setVerticalGroup(
            monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(monthTabLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectMonthLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MonthTable1Title))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(monthTabLayout.createSequentialGroup()
                        .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(monthScroll1, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(monthCard3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(monthTabLayout.createSequentialGroup()
                                .addGroup(monthTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(monthCard1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(monthCard2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(MonthChartTitle)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(MonthTable2Title)
                        .addGap(7, 7, 7)
                        .addComponent(monthScroll2, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(monthChart, javax.swing.GroupLayout.PREFERRED_SIZE, 491, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(468, 468, 468))
        );

        tabs.addTab("Báo cáo tháng", monthTab);

        yearTab.setBackground(new java.awt.Color(244, 244, 244));

        selectYearLbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        selectYearLbl.setText("Chọn năm");
        selectYearLbl.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        selectYearLbl.setPreferredSize(new java.awt.Dimension(90, 18));

        comboYear1.setMinimumSize(new java.awt.Dimension(72, 25));
        comboYear1.setPreferredSize(new java.awt.Dimension(100, 25));

        YearChartTitle.setText("Biểu đồ Doanh số");
        YearChartTitle.setBackground(new java.awt.Color(255, 255, 255));
        YearChartTitle.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        yearTable1Title.setText("Top các đơn hàng giá trị cao");
        yearTable1Title.setBackground(new java.awt.Color(255, 255, 255));
        yearTable1Title.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        yearTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        YearScroll1.setViewportView(yearTable1);
        if (yearTable1.getColumnModel().getColumnCount() > 0) {
            yearTable1.getColumnModel().getColumn(0).setResizable(false);
            yearTable1.getColumnModel().getColumn(1).setResizable(false);
            yearTable1.getColumnModel().getColumn(2).setResizable(false);
        }

        YearTable2Title.setText("Top các món ăn được ưa thích");
        YearTable2Title.setBackground(new java.awt.Color(255, 255, 255));
        YearTable2Title.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N

        yearTable2.setModel(new javax.swing.table.DefaultTableModel(
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
        yearScroll2.setViewportView(yearTable2);
        if (yearTable2.getColumnModel().getColumnCount() > 0) {
            yearTable2.getColumnModel().getColumn(0).setResizable(false);
            yearTable2.getColumnModel().getColumn(1).setResizable(false);
            yearTable2.getColumnModel().getColumn(2).setResizable(false);
        }

        javax.swing.GroupLayout yearTabLayout = new javax.swing.GroupLayout(yearTab);
        yearTab.setLayout(yearTabLayout);
        yearTabLayout.setHorizontalGroup(
            yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(yearTabLayout.createSequentialGroup()
                .addGroup(yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(yearTabLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(yearChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(yearTabLayout.createSequentialGroup()
                                .addGroup(yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(YearChartTitle)
                                    .addGroup(yearTabLayout.createSequentialGroup()
                                        .addComponent(yearCard1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(yearCard2, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(yearCard3, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(6, 6, 6)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(yearTabLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(selectYearLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboYear1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(yearTabLayout.createSequentialGroup()
                        .addGroup(yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(yearTable1Title)
                            .addComponent(YearTable2Title)
                            .addComponent(yearScroll2, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE))
                        .addContainerGap())
                    .addComponent(YearScroll1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
        yearTabLayout.setVerticalGroup(
            yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(yearTabLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yearTable1Title)
                    .addComponent(comboYear1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectYearLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(yearTabLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(yearTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(yearCard2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yearCard1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yearCard3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addComponent(YearChartTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yearChart, javax.swing.GroupLayout.PREFERRED_SIZE, 494, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(yearTabLayout.createSequentialGroup()
                        .addComponent(YearScroll1, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(YearTable2Title)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yearScroll2, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        tabs.addTab("Báo cáo năm", yearTab);

        jLabel3.setText("Tổng quan Doanh Thu");
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(200, 22));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 1044, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(tabs, javax.swing.GroupLayout.PREFERRED_SIZE, 872, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void comboMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboMonthActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboMonthActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {                                              
        // Reload all dashboard data: card, table, chart
        dashboardCard.reloadData(); // Nếu có hàm reloadData hoặc tương đương
        initTodayOrderTableData();
        initTodayDishesTableData();
        dashboardChart.clearChartData(); // Thêm dòng này để xóa dữ liệu cũ của chart
        initTodayChartData();            // Thêm dòng này để nạp lại dữ liệu mới cho chart
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel MonthChartTitle;
    private javax.swing.JLabel MonthTable1Title;
    private javax.swing.JLabel MonthTable2Title;
    private javax.swing.JLabel YearChartTitle;
    private javax.swing.JScrollPane YearScroll1;
    private javax.swing.JLabel YearTable2Title;
    private com.mycompany.quanlyquanan.view.Reports.CardGradient card1;
    private javax.swing.JComboBox<String> comboMonth;
    private javax.swing.JComboBox<Integer> comboYear;
    private javax.swing.JComboBox<Integer> comboYear1;
    private javax.swing.JPanel dashboard;
    private com.mycompany.quanlyquanan.view.Reports.CardPanel dashboardCard;
    private com.mycompany.quanlyquanan.view.Reports.ChartPanel dashboardChart;
    private javax.swing.JLabel dashboardChartTitle;
    private javax.swing.JLabel dashboardTable1Title;
    private javax.swing.JLabel dashboardTable2Title;
    private com.github.lgooddatepicker.components.DatePicker datePicker1;
    private com.mycompany.quanlyquanan.view.Reports.Card dayCard1;
    private com.mycompany.quanlyquanan.view.Reports.Card dayCard2;
    private com.mycompany.quanlyquanan.view.Reports.Card dayCard3;
    private com.mycompany.quanlyquanan.view.Reports.ChartPanel dayChart;
    private javax.swing.JLabel dayChartTitle;
    private javax.swing.JScrollPane dayScroll1;
    private javax.swing.JScrollPane dayScroll2;
    private javax.swing.JPanel dayTab;
    private com.mycompany.quanlyquanan.view.Reports.Table dayTable1;
    private javax.swing.JLabel dayTable1Title;
    private com.mycompany.quanlyquanan.view.Reports.Table dayTable2;
    private javax.swing.JLabel dayTable2Title;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private com.mycompany.quanlyquanan.view.Reports.Card monthCard1;
    private com.mycompany.quanlyquanan.view.Reports.Card monthCard2;
    private com.mycompany.quanlyquanan.view.Reports.Card monthCard3;
    private com.mycompany.quanlyquanan.view.Reports.ChartPanel monthChart;
    private javax.swing.JScrollPane monthScroll1;
    private javax.swing.JScrollPane monthScroll2;
    private javax.swing.JPanel monthTab;
    private com.mycompany.quanlyquanan.view.Reports.Table monthTable1;
    private com.mycompany.quanlyquanan.view.Reports.Table monthTable2;
    private javax.swing.JButton refreshButton;
    private javax.swing.JLabel selectMonthLbl;
    private javax.swing.JLabel selectYearLbl;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JScrollPane todayScroll1;
    private javax.swing.JScrollPane todayScroll2;
    private com.mycompany.quanlyquanan.view.Reports.Table todayTable1;
    private com.mycompany.quanlyquanan.view.Reports.Table todayTable2;
    private com.mycompany.quanlyquanan.view.Reports.Card yearCard1;
    private com.mycompany.quanlyquanan.view.Reports.Card yearCard2;
    private com.mycompany.quanlyquanan.view.Reports.Card yearCard3;
    private com.mycompany.quanlyquanan.view.Reports.ChartPanel yearChart;
    private javax.swing.JScrollPane yearScroll2;
    private javax.swing.JPanel yearTab;
    private com.mycompany.quanlyquanan.view.Reports.Table yearTable1;
    private javax.swing.JLabel yearTable1Title;
    private com.mycompany.quanlyquanan.view.Reports.Table yearTable2;
    // End of variables declaration//GEN-END:variables


}
