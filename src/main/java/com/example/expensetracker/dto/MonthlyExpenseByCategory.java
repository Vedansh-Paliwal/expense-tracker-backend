package com.example.expensetracker.dto;

import java.math.BigDecimal;

public class MonthlyExpenseByCategory {

    private int month;
    private String category;
    private BigDecimal totalAmount;

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
