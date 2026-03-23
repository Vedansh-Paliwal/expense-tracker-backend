package com.example.expensetracker.dto;

import java.util.List;

public class FinancialAnalysisResponse {

    private Double financialHealthScore;
    private String overallSummary;
    private List<String> strengths;
    private List<String> risks;
    private List<String> recommendations;
    private String spendingBehaviour;
    private Boolean budgetExceededThisMonth;
    private Double savingsRate;

    public Double getFinancialHealthScore() {
        return financialHealthScore;
    }

    public void setFinancialHealthScore(Double financialHealthScore) {
        this.financialHealthScore = financialHealthScore;
    }

    public String getOverallSummary() {
        return overallSummary;
    }

    public void setOverallSummary(String overallSummary) {
        this.overallSummary = overallSummary;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public String getSpendingBehaviour() {
        return spendingBehaviour;
    }

    public void setSpendingBehaviour(String spendingBehaviour) {
        this.spendingBehaviour = spendingBehaviour;
    }

    public Boolean getBudgetExceededThisMonth() {
        return budgetExceededThisMonth;
    }

    public void setBudgetExceededThisMonth(Boolean budgetExceededThisMonth) {
        this.budgetExceededThisMonth = budgetExceededThisMonth;
    }

    public Double getSavingsRate() {
        return savingsRate;
    }

    public void setSavingsRate(Double savingsRate) {
        this.savingsRate = savingsRate;
    }
}
