package com.ledger.os.dto;

import com.ledger.os.model.Category;

import java.math.BigDecimal;

public record CategoryBreakdownDto(
        Category category,
        String categoryName,
        String icon,
        String hexColor,
        BigDecimal spent,
        double percentage
) {
}
