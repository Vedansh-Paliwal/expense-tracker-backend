package com.example.expensetracker.dto;

import java.math.BigDecimal;

public class MonthlyExpense {

    private int month;
    private BigDecimal totalAmount;

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
