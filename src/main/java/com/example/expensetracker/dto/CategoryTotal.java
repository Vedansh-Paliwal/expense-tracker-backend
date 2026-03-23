package com.example.expensetracker.dto;

import java.math.BigDecimal;

public class CategoryTotal {

    private String category;
    private BigDecimal totalAmount;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
