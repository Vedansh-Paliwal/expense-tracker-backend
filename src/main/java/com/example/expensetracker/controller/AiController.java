package com.example.expensetracker.controller;

import com.example.expensetracker.dto.FinancialAnalysisRequest;
import com.example.expensetracker.dto.FinancialAnalysisResponse;
import com.example.expensetracker.service.AiService;
import com.example.expensetracker.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    private String getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    @PostMapping("/financial-analysis")
    public ResponseEntity<FinancialAnalysisResponse> generateFinancialAnalysis(
            @Valid @RequestBody FinancialAnalysisRequest request
            ) {
            String userId = getCurrentUserId();
            FinancialAnalysisResponse response = aiService.generateAnalysis(userId, request);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
    }
}
