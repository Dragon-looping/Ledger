package com.ledger.os.dto;

import com.ledger.os.model.Budget;
import com.ledger.os.model.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetDto(
        Long id,

        @NotNull(message = "Category is required")
        Category category,

        @NotNull(message = "Budget limit amount is required")
        @DecimalMin(value = "0.01", message = "Budget limit must be strictly positive")
        BigDecimal amount,

        String currency,

        String month
) {

    public static BudgetDto fromEntity(Budget b) {
        return new BudgetDto(
                b.getId(),
                b.getCategory(),
                b.getLimit().getAmount(),
                b.getLimit().getCurrencyCode(),
                b.getPeriod().toString()
        );
    }
}
