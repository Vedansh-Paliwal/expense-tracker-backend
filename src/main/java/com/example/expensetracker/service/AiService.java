package com.example.expensetracker.service;

import com.example.expensetracker.ai.LlmClient;
import com.example.expensetracker.dto.CategoryTotal;
import com.example.expensetracker.dto.ExpenseSummary;
import com.example.expensetracker.dto.FinancialAnalysisRequest;
import com.example.expensetracker.dto.FinancialAnalysisResponse;
import com.example.expensetracker.entity.AiReport;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.repository.AiReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AiService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AiReportRepository aiReportRepository;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LlmClient  llmClient;

    private LocalDateTime getLatestExpenseUpdatedAt(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.with(Sort.by(Sort.Direction.DESC, "updatedAt"));
        query.limit(1);
        query.fields().include("updatedAt");

        Expense latestExpense = mongoTemplate.findOne(query, Expense.class);

        return latestExpense != null
                ? latestExpense.getUpdatedAt()
                : null;
    }

    private boolean bigDecimalEquals(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;
    }

    private boolean isCacheValid(FinancialAnalysisRequest request, AiReport existingReport, LocalDateTime latestExpenseUpdatedAt) {
        boolean expensesSame = Objects.equals(existingReport.getBasedOnExpenseLastUpdatedAt(), latestExpenseUpdatedAt);
        boolean incomeSame = existingReport.getMonthlyIncome().compareTo(request.getMonthlyIncome()) == 0;
        boolean savingsGoalSame = bigDecimalEquals(existingReport.getSavingsGoal(), request.getSavingsGoal());
        boolean prioritySame = Objects.equals(existingReport.getFinancialPriority(), request.getFinancialPriority());

        return expensesSame && incomeSame && savingsGoalSame && prioritySame;
    }

    private FinancialSnapshot buildSnapshot(
            String userId,
            FinancialAnalysisRequest request
    ) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        ExpenseSummary expenseSummary = expenseService.getExpenseSummary(userId);
        BigDecimal thisMonthExpense = expenseSummary.getTotalThisMonth();
        BigDecimal thisYearExpense = expenseSummary.getTotalThisYear();

        List<CategoryTotal> categories = expenseService.getCategoryTotals(userId, currentYear, currentMonth);
        Map<String, BigDecimal> categoryBreakdownThisMonth = new HashMap<>();
        for (CategoryTotal categoryTotal : categories) {
            categoryBreakdownThisMonth.put(categoryTotal.getCategory(), categoryTotal.getTotalAmount());
        }
        BigDecimal monthlyIncome = request.getMonthlyIncome();
        BigDecimal savings = monthlyIncome.subtract(thisMonthExpense);
        BigDecimal rate = savings.divide(monthlyIncome, 4,  RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        BigDecimal savingsGoal = request.getSavingsGoal();
        String financialPriority = request.getFinancialPriority();

        FinancialSnapshot snapshot = new FinancialSnapshot();

        snapshot.setMonthlyIncome(monthlyIncome);
        snapshot.setTotalSpentThisMonth(thisMonthExpense);
        snapshot.setTotalSpentThisYear(thisYearExpense);
        snapshot.setSavingsGoal(savingsGoal);
        snapshot.setSavingsRate(rate.doubleValue());
        snapshot.setCategoryBreakdownThisMonth(categoryBreakdownThisMonth);
        snapshot.setFinancialPriority(financialPriority);

        return snapshot;
    }

    private String buildPrompt(FinancialSnapshot snapshot) throws JsonProcessingException {
        String snapshotJson = objectMapper.writeValueAsString(snapshot);
        return """
            You are a financial advisor AI.
            
            Analyze the following structured financial data:
            
            %s
            
            Respond strictly in this JSON format:
            {
              "financialHealthScore": number,
              "overallSummary": string,
              "strengths": [string],
              "risks": [string],
              "recommendations": [string],
              "spendingBehaviour": string,
              "budgetExceededThisMonth": boolean,
              "savingsRate": number
            }
            
            Do NOT include any text outside JSON.
            """.formatted(snapshotJson);
    }

    public FinancialAnalysisResponse generateAnalysis(String userId, FinancialAnalysisRequest request) {
        LocalDateTime latestExpenseUpdatedAt = getLatestExpenseUpdatedAt(userId);
        Optional<AiReport> optionalReport = aiReportRepository.findByUserId(userId);
        if(optionalReport.isPresent()){
            AiReport existingReport = optionalReport.get();
            boolean shouldUseCache = isCacheValid(request, existingReport, latestExpenseUpdatedAt);

            if(shouldUseCache){
                String oldReport = existingReport.getAiResultJSON();
                try {
                    return objectMapper.readValue(oldReport, FinancialAnalysisResponse.class);
                }
                catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to parse old report", e);
                }
            }
        }
        FinancialSnapshot snapshot = buildSnapshot(userId, request);
        String prompt;
        try {
            prompt = buildPrompt(snapshot);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build AI prompt", e);
        }
        String llmResponse = llmClient.callLlm(prompt);
        llmResponse = llmResponse.strip();
        if (llmResponse.startsWith("```json")) {
            llmResponse = llmResponse.substring(7);
        }
        if (llmResponse.startsWith("```")) {
            llmResponse = llmResponse.substring(3);
        }
        if (llmResponse.endsWith("```")) {
            llmResponse = llmResponse.substring(0, llmResponse.length() - 3);
        }
        llmResponse = llmResponse.strip();
        if (llmResponse == null || llmResponse.isBlank()) {
            throw new RuntimeException("LLM returned empty response");
        }
        FinancialAnalysisResponse response;
        try {
            response = objectMapper.readValue(llmResponse, FinancialAnalysisResponse.class);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse llm response", e);
        }
        AiReport report = new AiReport();
        report.setUserId(userId);
        report.setGeneratedAt(LocalDateTime.now());
        report.setBasedOnExpenseLastUpdatedAt(latestExpenseUpdatedAt);
        report.setMonthlyIncome(request.getMonthlyIncome());
        report.setSavingsGoal(request.getSavingsGoal());
        report.setFinancialPriority(request.getFinancialPriority());
        report.setAiResultJSON(llmResponse);
        optionalReport.ifPresent(aiReport -> report.setId(aiReport.getId()));
        aiReportRepository.save(report);

        return response;
    }

    private static class FinancialSnapshot {

        private BigDecimal totalSpentThisMonth;
        private BigDecimal totalSpentThisYear;
        private BigDecimal monthlyIncome;
        private BigDecimal savingsGoal;
        private Double savingsRate;
        private Map<String, BigDecimal> categoryBreakdownThisMonth;
        private String financialPriority;

        public BigDecimal getTotalSpentThisMonth() {
            return totalSpentThisMonth;
        }

        public void setTotalSpentThisMonth(BigDecimal totalSpentThisMonth) {
            this.totalSpentThisMonth = totalSpentThisMonth;
        }

        public BigDecimal getTotalSpentThisYear() {
            return totalSpentThisYear;
        }

        public void setTotalSpentThisYear(BigDecimal totalSpentThisYear) {
            this.totalSpentThisYear = totalSpentThisYear;
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

        public Double getSavingsRate() {
            return savingsRate;
        }

        public void setSavingsRate(Double savingsRate) {
            this.savingsRate = savingsRate;
        }

        public Map<String, BigDecimal> getCategoryBreakdownThisMonth() {
            return categoryBreakdownThisMonth;
        }

        public void setCategoryBreakdownThisMonth(Map<String, BigDecimal> categoryBreakdownThisMonth) {
            this.categoryBreakdownThisMonth = categoryBreakdownThisMonth;
        }

        public String getFinancialPriority() {
            return financialPriority;
        }

        public void setFinancialPriority(String financialPriority) {
            this.financialPriority = financialPriority;
        }
    }
}
