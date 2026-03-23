package com.example.expensetracker.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class FinancialAnalysisRequest {

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal monthlyIncome;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal savingsGoal;

    @NotBlank
    @Size(max = 50)
    private String financialPriority;

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getSavingsGoal() {
        return savingsGoal;
    }

    public void setSavingsGoal(BigDecimal savingsGoal) {
        this.savingsGoal = savingsGoal;
    }

    public String getFinancialPriority() {
        return financialPriority;
    }

    public void setFinancialPriority(String financialPriority) {
        this.financialPriority = financialPriority;
    }
}
