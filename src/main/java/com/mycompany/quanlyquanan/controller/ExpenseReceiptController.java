package com.mycompany.quanlyquanan.controller;

import com.mycompany.quanlyquanan.service.ExpenseReceiptService;

import java.time.LocalDate;
import java.util.Map;

public class ExpenseReceiptController {
    private ExpenseReceiptService expenseService;

    public ExpenseReceiptController() {
        this.expenseService = new ExpenseReceiptService();
    }

    public double getTodayTotalExpense() {
        return expenseService.getTodayTotalExpense();
    }

    public double getTotalExpenseByDay(LocalDate date) {
        return expenseService.getTotalExpenseByDay(date);
    }

    public Map<LocalDate, Double> getTotalExpenseByDays(LocalDate startDate, LocalDate endDate) {
        return expenseService.getTotalExpenseByDays(startDate, endDate);
    }

    public double getTotalExpenseByMonth(int month, int year) {
        return expenseService.getTotalExpenseByMonth(month, year);
    }

    public Map<Integer, Double> getTotalExpenseByMonths(LocalDate startDate, LocalDate endDate) {
        return expenseService.getTotalExpenseByMonths(startDate, endDate);
    }

    public double getTotalExpenseByYear(int year) {
        return expenseService.getTotalExpenseByYear(year);
    }

    public Map<Integer, Double> getTotalExpenseByYears(int startYear, int endYear) {
        return expenseService.getTotalExpenseByYears(startYear, endYear);
    }
}
