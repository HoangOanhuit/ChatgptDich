package com.mycompany.quanlyquanan.view.Reports;

import javax.swing.*;
import java.awt.*;

public class ReportView extends JFrame {
    private JTabbedPane tabbedPane;

    public ReportView() {
        setTitle("BÁO CÁO THỐNG KÊ");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        tabbedPane.add("Doanh Thu", createRevenuePanel());
        tabbedPane.add("Món Bán Chạy", createTopDishPanel());
        tabbedPane.add("Tồn Kho", createInventoryPanel());
        tabbedPane.add("Chi Phí", createExpensePanel());
        tabbedPane.add("Lợi Nhuận", createProfitPanel());

        add(tabbedPane);
    }

    private JPanel createRevenuePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Bộ lọc thời gian
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Từ ngày:"));
        filterPanel.add(new JTextField(10));
        filterPanel.add(new JLabel("Đến ngày:"));
        filterPanel.add(new JTextField(10));
        filterPanel.add(new JButton("Xem báo cáo"));

        // Kết quả
        JTable revenueTable = new JTable(); // dữ liệu sẽ từ controller/service đổ vào
        JScrollPane scrollPane = new JScrollPane(revenueTable);

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTopDishPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("TOP MÓN BÁN CHẠY", JLabel.CENTER), BorderLayout.NORTH);

        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("NGUYÊN LIỆU GẦN HẾT HÀNG", JLabel.CENTER), BorderLayout.NORTH);

        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createExpensePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Có thể lọc theo loại chi phí, khoảng thời gian
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Từ ngày:"));
        filterPanel.add(new JTextField(10));
        filterPanel.add(new JLabel("Đến ngày:"));
        filterPanel.add(new JTextField(10));
        filterPanel.add(new JButton("Xem báo cáo"));

        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProfitPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Có thể tổng hợp từ doanh thu - chi phí
        JTextArea summaryArea = new JTextArea("Lợi nhuận = Doanh thu - Chi phí\nChi tiết sẽ hiển thị ở đây...");
        summaryArea.setEditable(false);
        panel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReportView().setVisible(true));
    }
}
