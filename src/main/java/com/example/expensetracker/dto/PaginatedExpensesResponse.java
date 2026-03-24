package com.example.expensetracker.dto;

import com.example.expensetracker.entity.Expense;

import java.util.List;

public class PaginatedExpensesResponse {

    private List<Expense> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PaginatedExpensesResponse(
            List<Expense> content,
            int page,
            int size,
            long totalElements
    ) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
    }
    public List<Expense> getContent() {
        return content;
    }
    public int getPage() {
        return page;
    }
    public int getSize() {
        return size;
    }
    public long getTotalElements() {
        return totalElements;
    }
    public int getTotalPages() {
        return totalPages;
    }
}