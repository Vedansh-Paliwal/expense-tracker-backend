package com.example.expensetracker.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "ai_reports")
@CompoundIndex(
        name = "user_unique_report",
        def = "{'userId': 1}",
        unique = true
)
public class AiReport {

    @Id
    private String id;
    private String userId;
    private LocalDateTime generatedAt;
    private LocalDateTime basedOnExpenseLastUpdatedAt;
    private BigDecimal monthlyIncome;
    private BigDecimal savingsGoal;
    private String financialPriority;
    private String aiResultJson;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDateTime getBasedOnExpenseLastUpdatedAt() {
        return basedOnExpenseLastUpdatedAt;
    }

    public void setBasedOnExpenseLastUpdatedAt(LocalDateTime basedOnExpenseLastUpdatedAt) {
        this.basedOnExpenseLastUpdatedAt = basedOnExpenseLastUpdatedAt;
    }

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

    public String getAiResultJSON() {
        return aiResultJson;
    }

    public void setAiResultJSON(String aiResultJSON) {
        this.aiResultJson = aiResultJSON;
    }
}
