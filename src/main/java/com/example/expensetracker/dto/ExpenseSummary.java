package com.example.expensetracker.dto;

import java.math.BigDecimal;

public class ExpenseSummary {

    private int totalExpenses;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
    private BigDecimal totalThisYear;
    private BigDecimal totalThisMonth;

    public BigDecimal getTotalThisYear() {
        return totalThisYear;
    }

    public void setTotalThisYear(BigDecimal totalThisYear) {
        this.totalThisYear = totalThisYear;
    }

    public BigDecimal getTotalThisMonth() {
        return totalThisMonth;
    }

    public void setTotalThisMonth(BigDecimal totalThisMonth) {
        this.totalThisMonth = totalThisMonth;
    }

    public int getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(int totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(BigDecimal averageAmount) {
        this.averageAmount = averageAmount;
    }
}
