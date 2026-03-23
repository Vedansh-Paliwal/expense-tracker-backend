package com.example.expensetracker.util;

import com.example.expensetracker.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ExpenseCSVExporter {
    public static String convertToCSV(List<Expense> expense) {
        StringBuilder csv = new StringBuilder();
        csv.append("Date,Title,Category,Amount,Payment Method,Description\n");
        for (Expense e : expense) {
            LocalDate date = e.getDate();
            String description = e.getDescription();
            String category = e.getCategory();
            BigDecimal amount = e.getAmount();
            String payment = e.getPaymentMethod();
            String title = e.getTitle();

            String dateString = date.toString();
            String amountStr = amount.toPlainString();
            String titleEscaped = escapeCSVValue(title);
            String categoryEscaped = escapeCSVValue(category);
            String paymentEscaped = escapeCSVValue(payment);
            String descriptionEscaped = escapeCSVValue(description);

            String row = String.join(",",
                    dateString,
                    titleEscaped,
                    categoryEscaped,
                    amountStr,
                    paymentEscaped,
                    descriptionEscaped
            );
            csv.append(row).append("\n");
        }
        return csv.toString();
    }

    private static String escapeCSVValue(String value) {
        if(value==null){
            return "\"\"";
        }
        value = value.replace("\"", "\"\"");
        return "\"" + value + "\"";
    }
}
