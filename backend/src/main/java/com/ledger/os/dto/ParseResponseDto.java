package com.ledger.os.dto;

import com.ledger.os.factory.TransactionType;
import com.ledger.os.model.Category;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParseResponseDto(
        BigDecimal amount,
        String currency,
        LocalDate date,
        String note,
        Category category,
        TransactionType type,
        String categoryIcon,
        String categoryHexColor,
        String rawInput
) {
}
