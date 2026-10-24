package com.ledger.os.dto;

import com.ledger.os.model.Category;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummaryDto(
        String period,
        String currency,
        BigDecimal totalIncome,
        BigDecimal totalSpent,
        BigDecimal netBalance,
        double savingsRate,
        int transactionCount,
        List<CategoryBreakdownDto> categoryBreakdown,
        List<BudgetStatusDto> budgetStatuses
) {
}
