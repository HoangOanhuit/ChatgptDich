package com.mycompany.quanlyquanan.service;

import com.mycompany.quanlyquanan.dao.ExpenseReceiptDAO;


import java.time.LocalDate;
import java.util.Map;

public class ExpenseReceiptService {
    private ExpenseReceiptDAO expenseDAO;

    public ExpenseReceiptService() {
        this.expenseDAO = new ExpenseReceiptDAO();
    }

    public double getTodayTotalExpense() {
        return expenseDAO.getTodayTotalExpense();
    }


    public double getTotalExpenseByDay(LocalDate date) {
        return expenseDAO.getTotalExpenseByDay(date);
    }

    public Map<LocalDate, Double> getTotalExpenseByDays(LocalDate startDate, LocalDate endDate) {
        return expenseDAO.getTotalExpenseByDays(startDate, endDate);
    }

    public double getTotalExpenseByMonth(int month, int year) {
        return expenseDAO.getTotalExpenseByMonth(month, year);
    }

    public Map<Integer, Double> getTotalExpenseByMonths(LocalDate startDate, LocalDate endDate) {
        return expenseDAO.getTotalExpenseByMonths(startDate, endDate);
    }


    public double getTotalExpenseByYear(int year) {
        return expenseDAO.getTotalExpenseByYear(year);
    }

    public Map<Integer, Double> getTotalExpenseByYears(int startYear, int endYear) {
        return expenseDAO.getTotalExpenseByYears(startYear, endYear);
    }
}
