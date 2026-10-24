package com.ledger.os.dto;

import com.ledger.os.model.Category;

import java.math.BigDecimal;

public record BudgetStatusDto(
        String ruleName,
        Category category,
        BigDecimal limit,
        BigDecimal spent,
        double percentUsed,
        String level
) {
}
