package com.example.expensetracker.controller;

import com.example.expensetracker.dto.FinancialAnalysisRequest;
import com.example.expensetracker.dto.FinancialAnalysisResponse;
import com.example.expensetracker.security.UserDetailsImpl;
import com.example.expensetracker.service.AiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
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
