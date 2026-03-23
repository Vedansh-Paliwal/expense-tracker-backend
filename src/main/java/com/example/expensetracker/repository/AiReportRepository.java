package com.example.expensetracker.repository;

import com.example.expensetracker.entity.AiReport;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AiReportRepository extends MongoRepository<AiReport, String> {
    Optional<AiReport> findByUserId(String userId);
    public void deleteByUserId(String userId);
}
