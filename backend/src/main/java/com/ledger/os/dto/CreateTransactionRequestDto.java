package com.ledger.os.dto;

import com.ledger.os.factory.TransactionType;
import com.ledger.os.model.Category;
import com.ledger.os.model.Frequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequestDto(
        @NotNull(message = "Transaction type is required (EXPENSE, INCOME, RECURRING_EXPENSE)")
        TransactionType type,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be strictly positive")
        BigDecimal amount,

        String currency,

        @NotNull(message = "Transaction date is required")
        LocalDate date,

        String note,

        @NotNull(message = "Category is required")
        Category category,

        Frequency frequency
) {
}
