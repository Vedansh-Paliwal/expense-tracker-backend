package com.example.expensetracker.controller;

import com.example.expensetracker.dto.*;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.service.CsvExportService;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.util.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@Validated
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private CsvExportService csvExportService;

    private String getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    @GetMapping("/test")
    public String testParams(@RequestParam String name, @RequestParam int age) {
        return "Hello " + name + ", you are "  + age + " years old!";
    }

    @GetMapping
    public ResponseEntity<PaginatedExpensesResponse> getAllExpenses(
            @Valid ExpenseFilterRequest filterRequest
    )   {
        String userID = getCurrentUserId();
        PaginatedExpensesResponse response = expenseService.getFilteredExpensesPaginated(userID,filterRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics/by-category")
    public ResponseEntity<List<CategoryTotal>> getExpensesByCategory(
            @RequestParam(required = false) @Min(1970) Integer year,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month
    ) {
        String userID = getCurrentUserId();
        List<CategoryTotal> expensesByCategory = expenseService.getCategoryTotals(userID, year, month);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(expensesByCategory);
    }

    @GetMapping("/analytics/summary")
    public ResponseEntity<ExpenseSummary> getExpensesSummary(){
        String userId = getCurrentUserId();
        ExpenseSummary expenseSummary = expenseService.getExpenseSummary(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(expenseSummary);
    }

    @GetMapping("/analytics/monthly")
    public ResponseEntity<List<MonthlyExpense>> getMonthlyExpensesByYear(@RequestParam @Min(1970) Integer year){
        String userID = getCurrentUserId();
        List<MonthlyExpense> monthlyExpenses = expenseService.getMonthlyExpensesByYear(userID, year);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(monthlyExpenses);
    }

    @GetMapping("/analytics/monthly-by-category")
    public ResponseEntity<List<MonthlyExpenseByCategory>> getMonthlyExpensesByCategory(@RequestParam @Min(1970) Integer year){
        String userID = getCurrentUserId();
        List<MonthlyExpenseByCategory> result = expenseService.getMonthlyExpensesByCategory(userID, year);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExpenses(ExpenseFilterRequest filter){
        String userID = getCurrentUserId();
        List<Expense> expenses = expenseService.getFilteredExpensesWithoutPagination(userID,filter);

        byte[] csvBytes = csvExportService.convertToCSV(expenses);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("expenses.csv")
                        .build()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(csvBytes);
    }

    @GetMapping("/view-budget")
    public ResponseEntity<BudgetViewResponse> viewBudget(){
        String userID = getCurrentUserId();
        BudgetViewResponse budgetViewResponse = expenseService.viewCurrentMonthBudget(userID);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(budgetViewResponse);
    }

    @PostMapping("/set-budget")
    public ResponseEntity<String> setBudget(@Valid @RequestBody SetBudgetRequest budget){
        String userId = getCurrentUserId();
        expenseService.setBudgetForUser(userId, budget.getBudgetAmount());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Budget set successfully.");
    }

    @PostMapping
    public ResponseEntity<?> addExpense(@Valid @RequestBody CreateExpenseRequest request) {
        String userID = getCurrentUserId();
        expenseService.addUserExpense(request, userID);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Expense added successfully");
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<?> updateExpense(@PathVariable @NotBlank String expenseId, @Valid @RequestBody CreateExpenseRequest request){
        String userId = getCurrentUserId();
        expenseService.updateUserExpense(expenseId, userId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Expense updated successfully");
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable @NotBlank String expenseId){
        String userID = getCurrentUserId();
        expenseService.deleteExpense(expenseId, userID);

        return  ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllExpenses(
            @RequestParam(defaultValue = "false") boolean confirm
    ){
        String userID = getCurrentUserId();
        expenseService.deleteAllExpenses(userID, confirm);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
