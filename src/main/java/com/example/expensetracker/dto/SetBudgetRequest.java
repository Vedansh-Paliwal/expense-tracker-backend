package com.example.expensetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class SetBudgetRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal budgetAmount;

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }
}
