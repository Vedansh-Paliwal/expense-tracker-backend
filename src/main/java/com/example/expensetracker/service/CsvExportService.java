package com.example.expensetracker.service;

import com.example.expensetracker.entity.Expense;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CsvExportService {

    private String escape(String value) {
        if (value == null) {
            return "\"\"";
        }

        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
    private String formatText(String value) {
        if(value == null || value.isBlank()) {
            return "";
        }
        return value.substring(0,1).toUpperCase() + value.substring(1).toLowerCase();
    }
    public byte[] convertToCSV(List<Expense> expenses) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title,Amount,Category,Description,Payment Method,Date\n");
        for (Expense expense : expenses) {
            sb.append(escape(expense.getTitle())).append(',');
            sb.append(expense.getAmount()).append(',');
            sb.append(escape(formatText(expense.getCategory()))).append(',');
            sb.append(escape(expense.getDescription())).append(',');
            sb.append(escape(formatText(expense.getPaymentMethod()))).append(',');
            sb.append(expense.getDate()).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
