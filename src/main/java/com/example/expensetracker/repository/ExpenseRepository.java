package com.example.expensetracker.repository;

import com.example.expensetracker.entity.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    public void deleteAllByUserId(String userID);
    public long countByUserId(String userId);
}