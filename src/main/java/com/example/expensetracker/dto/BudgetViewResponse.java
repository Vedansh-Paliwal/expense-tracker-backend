package com.example.expensetracker.dto;

import com.example.expensetracker.entity.Budget;

import java.math.BigDecimal;

public class BudgetViewResponse {

    private BigDecimal budget;
    private BigDecimal totalSpent;
    private BigDecimal remaining;
    private String status;

    public BudgetViewResponse(BigDecimal budgetAmount, BigDecimal totalSpent, BigDecimal remaining, String status) {
        this.budget = budgetAmount;
        this.totalSpent = totalSpent;
        this.remaining = remaining;
        this.status = status;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public String getStatus() {
        return status;
    }
}
