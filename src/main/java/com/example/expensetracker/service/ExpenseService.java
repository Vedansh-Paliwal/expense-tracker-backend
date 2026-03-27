package com.example.expensetracker.service;

import com.example.expensetracker.dto.*;
import com.example.expensetracker.entity.Budget;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.exception.AccessDeniedException;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.ExpenseRepository;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final Set<String> VALID_CATEGORIES =
            Set.of("food", "travel", "rent", "shopping", "utilities", "health", "entertainment" ,"other");

    private static final Set<String> VALID_PAYMENT_METHODS =
            Set.of("cash", "upi", "card", "netbanking");

    private Query buildExpenseFilterQuery(
            String userId,
            ExpenseFilterRequest filter
    ){
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));

        if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
            String normalizedCategory =
                    filter.getCategory().trim().toLowerCase();
            if (!VALID_CATEGORIES.contains(normalizedCategory)) {
                throw new IllegalArgumentException("Invalid category filter");
            }
            query.addCriteria(Criteria.where("category").is(normalizedCategory));
        }

        if(filter.getPaymentMethod() != null && !filter.getPaymentMethod().isBlank()) {
            String normalizedPaymentMethod =
                    filter.getPaymentMethod().trim().toLowerCase();
            if (!VALID_PAYMENT_METHODS.contains(normalizedPaymentMethod)) {
                throw new IllegalArgumentException("Invalid payment method filter");
            }
            query.addCriteria(Criteria.where("paymentMethod").is(normalizedPaymentMethod));
        }

        BigDecimal min = filter.getMinAmount();
        BigDecimal max = filter.getMaxAmount();

        if(min != null && max != null){
            if(min.compareTo(max) > 0){
                throw new IllegalArgumentException("Min amount cannot exceed Max amount");
            }
            query.addCriteria(Criteria.where("amount")
                    .gte(new Decimal128(min))
                    .lte(new Decimal128(max)));
        }
        else if(min != null) {
            query.addCriteria(Criteria.where("amount").gte(new Decimal128(min)));
        }
        else if(max != null) {
            query.addCriteria(Criteria.where("amount").lte(new Decimal128(max)));
        }

        LocalDate start = filter.getStartDate();
        LocalDate end = filter.getEndDate();

        if (start != null && end != null) {
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
            query.addCriteria(Criteria.where("date").gte(start.toString()).lte(end.toString()));
        }
        else if (start != null) {
            query.addCriteria(Criteria.where("date").gte(start.toString()));
        }
        else if (end != null) {
            query.addCriteria(Criteria.where("date").lte(end.toString()));
        }

        return query;
    }

    public void addUserExpense(CreateExpenseRequest request, String userID) {
        String category = request.getCategory().trim().toLowerCase();
        if (!VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Invalid category");
        }

        String paymentMethod = request.getPaymentMethod().trim().toLowerCase();
        if (!VALID_PAYMENT_METHODS.contains(paymentMethod)) {
            throw new IllegalArgumentException("Invalid payment method");
        }

        LocalDateTime currentDateTime = LocalDateTime.now();

        Expense expense = new Expense();
        expense.setTitle(request.getTitle().trim());
        expense.setAmount(request.getAmount());
        expense.setCategory(category);
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription().trim());
        expense.setPaymentMethod(paymentMethod);
        expense.setUserId(userID);
        expense.setCreatedAt(currentDateTime);
        expense.setUpdatedAt(currentDateTime);

        expenseRepository.save(expense);
    }

    public List<Expense> getFilteredExpensesWithoutPagination(
            String userId,
            ExpenseFilterRequest filter
    ) {
        Query baseQuery = buildExpenseFilterQuery(userId, filter);
        return mongoTemplate.find(baseQuery, Expense.class);
    }

    public PaginatedExpensesResponse getFilteredExpensesPaginated(
            String userId,
            ExpenseFilterRequest filter
    ){
        int page = filter.getPage();
        int size = filter.getSize();

        Set<String> validSorts = Set.of("date", "amount", "title");
        Set<String> validOrders = Set.of("asc", "desc");
        String sortBy = filter.getSortBy();
        String order = filter.getOrder();
        String normalizedSortBy = (sortBy != null) ? sortBy.toLowerCase() : null;
        String normalizedOrder = (order != null) ? order.toLowerCase() : null;
        if(normalizedSortBy != null && !validSorts.contains(normalizedSortBy)){
            throw  new IllegalArgumentException("Invalid sort parameter");
        }
        if(normalizedOrder != null && !validOrders.contains(normalizedOrder)){
            throw  new IllegalArgumentException("Invalid order parameter");
        }

        Query query = buildExpenseFilterQuery(userId, filter);

        String finalSortBy = (normalizedSortBy != null) ? normalizedSortBy : "date";
        String finalOrder = (normalizedOrder != null) ? normalizedOrder : "desc";
        Sort.Direction direction = finalOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        query.with(
                Sort.by(direction, finalSortBy)
                        .and(Sort.by(direction, "_id"))
        );
        int skip = page * size;
        query.skip(skip);
        query.limit(size);
        Query countQuery = buildExpenseFilterQuery(userId, filter);

        long totalElements = mongoTemplate.count(countQuery, Expense.class);
        List<Expense> expenses = mongoTemplate.find(query, Expense.class);

        return new PaginatedExpensesResponse(expenses,page,size,totalElements);
    }

    public void updateUserExpense(String expenseId, String userId, CreateExpenseRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));
        if(!expense.getUserId().equals(userId)) {
            throw new AccessDeniedException("Not allowed to update this expense");
        }

        String category = request.getCategory().trim().toLowerCase();
        if (!VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Invalid category");
        }

        String paymentMethod = request.getPaymentMethod().trim().toLowerCase();
        if (!VALID_PAYMENT_METHODS.contains(paymentMethod)) {
            throw new IllegalArgumentException("Invalid payment method");
        }
        expense.setTitle(request.getTitle().trim());
        expense.setAmount(request.getAmount());
        expense.setCategory(category);
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription().trim());
        expense.setPaymentMethod(paymentMethod);
        expense.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense);
    }

    public void deleteExpense(String expenseId, String userId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));
        if (!expense.getUserId().equals(userId)) {
            throw new AccessDeniedException("Not allowed to delete this expense");
        }
        expenseRepository.deleteById(expenseId);
    }

    public void deleteAllExpenses(String userId, boolean confirm) {
        if(!confirm) {
            throw  new IllegalArgumentException("Confirmation required to delete all expenses");
        }
        long count = expenseRepository.countByUserId(userId);
        if(count == 0) {
            throw new ResourceNotFoundException("No expenses found to delete");
        }
        expenseRepository.deleteAllByUserId(userId);
    }

    public ExpenseSummary getExpenseSummary (String userId) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        LocalDate yearStart = LocalDate.of(currentYear, 1, 1);
        LocalDate nextYearStart = yearStart.plusYears(1);

        LocalDate monthStart = LocalDate.of(currentYear, currentMonth, 1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(userId)),
                Aggregation.group()
                        .count().as("totalExpenses")
                        .sum("amount").as("totalAmount")
                        .sum(
                                ConditionalOperators.when(
                                        Criteria.where("date")
                                                .gte(yearStart)
                                                .lt(nextYearStart)
                                )
                                .thenValueOf("amount")
                                .otherwise(BigDecimal.ZERO)
                        ).as("totalThisYear")
                        .sum(
                                ConditionalOperators.when(
                                        Criteria.where("date")
                                                .gte(monthStart)
                                                .lt(nextMonthStart)
                                )
                                .thenValueOf("amount")
                                .otherwise(BigDecimal.ZERO)
                        ).as("totalThisMonth")
        );

        AggregationResults<ExpenseSummary> results = mongoTemplate.aggregate(
                aggregation,
                Expense.class,
                ExpenseSummary.class
        );
        List<ExpenseSummary> expenseSummary = results.getMappedResults();
        if(expenseSummary.isEmpty()){
            ExpenseSummary emptyExpenseSummary = new ExpenseSummary();
            emptyExpenseSummary.setTotalExpenses(0);
            emptyExpenseSummary.setTotalAmount(BigDecimal.ZERO);
            emptyExpenseSummary.setAverageAmount(BigDecimal.ZERO);
            emptyExpenseSummary.setTotalThisYear(BigDecimal.ZERO);
            emptyExpenseSummary.setTotalThisMonth(BigDecimal.ZERO);
            return emptyExpenseSummary;
        }
        ExpenseSummary summary = expenseSummary.getFirst();
        BigDecimal avg = summary.getTotalAmount()
                .divide(BigDecimal.valueOf(summary.getTotalExpenses()), 2, RoundingMode.HALF_UP);
        summary.setAverageAmount(avg);

        return summary;
    }

    public List<MonthlyExpense> getMonthlyExpensesByYear (String userId, Integer year) {
        int currentYear = Year.now().getValue();
        if(year > currentYear){
            throw new IllegalArgumentException("Invalid year. You can't look into the future");
        }
        LocalDate start = LocalDate.of(year,1,1);
        LocalDate nextStart = start.plusYears(1);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("userId").is(userId)
                                .and("date").gte(start.toString()).lt(nextStart.toString())
                ),
                Aggregation.project("amount")
                        .and(StringOperators.valueOf("date").substring(5, 2)).as("month"),
                Aggregation.group("month")
                        .sum("amount").as("totalAmount"),
                Aggregation.project("totalAmount")
                        .and("_id").as("month"),
                Aggregation.sort(Sort.by(Sort.Order.asc("month")))
        );
        AggregationResults<MonthlyExpense> results = mongoTemplate.aggregate(
                aggregation,
                Expense.class,
                MonthlyExpense.class
        );
        return results.getMappedResults();
    }

    public List<MonthlyExpenseByCategory> getMonthlyExpensesByCategory (String userId, Integer year) {
        int currentYear = Year.now().getValue();
        if(year > currentYear){
            throw new IllegalArgumentException("Invalid year. You can't look into the future");
        }
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate nextStart = start.plusYears(1);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("userId").is(userId)
                                .and("date").gte(start.toString()).lt(nextStart.toString())
                ),
                Aggregation.project("amount","category")
                        .and(StringOperators.valueOf("date").substring(5, 2)).as("month"),
                Aggregation.group("month","category")
                        .sum("amount").as("totalAmount"),
                Aggregation.project("totalAmount")
                        .and("_id.month").as("month")
                        .and("_id.category").as("category"),
                Aggregation.sort(
                        Sort.by(Sort.Order.asc("month"),
                                Sort.Order.asc("category"))
                )
        );

        AggregationResults<MonthlyExpenseByCategory> results = mongoTemplate.aggregate(
                aggregation,
                Expense.class,
                MonthlyExpenseByCategory.class
        );
        return results.getMappedResults();
    }

    public void setBudgetForUser(String userId, BigDecimal budgetAmount){

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        Query query = new Query();
        query.addCriteria(
                Criteria.where("userId").is(userId)
                        .and("year").is(currentYear)
                        .and("month").is(currentMonth)
        );
        Budget existingBudget = mongoTemplate.findOne(query, Budget.class); // 1. Returns null if no document exists 2. Returns exactly one if it exists
        if(existingBudget != null){
            existingBudget.setBudget(budgetAmount);
            existingBudget.setUpdatedAt(LocalDateTime.now());
            mongoTemplate.save(existingBudget);
        }
        else {
            Budget newBudget = new Budget();
            newBudget.setUserId(userId);
            newBudget.setYear(currentYear);
            newBudget.setMonth(currentMonth);
            newBudget.setBudget(budgetAmount);
            newBudget.setCreatedAt(LocalDateTime.now());
            newBudget.setUpdatedAt(LocalDateTime.now());
            mongoTemplate.save(newBudget);
        }
    }

    public BudgetViewResponse viewCurrentMonthBudget(String userId){
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        Query query = new Query();
        query.addCriteria(Criteria
                .where("userId").is(userId)
                .and("year").is(currentYear)
                .and("month").is(currentMonth)
        );
        Budget existingBudget = mongoTemplate.findOne(query, Budget.class);
        if(existingBudget ==  null){
            throw new ResourceNotFoundException("Budget not found");
        }
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate nextStartOfMonth = startOfMonth.plusMonths(1);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("userId").is(userId)
                                .and("date").gte(startOfMonth).lt(nextStartOfMonth)
                ),
                Aggregation.group()
                        .sum("amount").as("totalSpent")
        );
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Expense.class, Document.class);
        BigDecimal totalSpent = BigDecimal.ZERO;
        if(!results.getMappedResults().isEmpty()){
            Object value = results.getMappedResults().getFirst().get("totalSpent");
            if(value != null){
                totalSpent = new BigDecimal(value.toString());
            }
        }
        BigDecimal budgetAmount = existingBudget.getBudget();
        BigDecimal remaining =  budgetAmount.subtract(totalSpent);
        String status = remaining.compareTo(BigDecimal.ZERO) >= 0 ? "SAFE":"EXCEEDED";

        return new BudgetViewResponse(budgetAmount, totalSpent, remaining, status);
    }

    public List<CategoryTotal> getCategoryTotals (String userId, Integer year, Integer month) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        if(month != null && year == null){
            throw new IllegalArgumentException("Month requires year");
        }
        if(year != null && year > currentYear){
            throw new IllegalArgumentException("Can't look into the future year");
        }
        if(year != null && year == currentYear && month != null && month > currentMonth){
            throw new IllegalArgumentException("Can't look into the future month");
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate;
        LocalDate nextStart;

        if(month == null && year == null){
            startDate = now.withDayOfMonth(1);
            nextStart = startDate.plusMonths(1);
        }
        else if(month == null) {
            startDate = LocalDate.of(year, 1, 1);
            nextStart = startDate.plusYears(1);
        }
        else {
            startDate = LocalDate.of(year, month, 1);
            nextStart = startDate.plusMonths(1);
        }
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("userId").is(userId)
                                .and("date").gte(startDate.toString()).lt(nextStart.toString())
                ),
                Aggregation.group("category")
                        .sum("amount").as("totalAmount"),
                Aggregation.project("totalAmount")
                        .and("_id").as("category"),
                Aggregation.sort(
                        Sort.by(Sort.Order.desc("totalAmount"))
                )
        );

        AggregationResults<CategoryTotal> results = mongoTemplate.aggregate(
                aggregation,
                Expense.class,
                CategoryTotal.class
        );
        return results.getMappedResults();
    }
}
