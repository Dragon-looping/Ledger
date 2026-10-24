package com.ledger.os.dto;

import com.ledger.os.factory.TransactionType;
import com.ledger.os.model.Category;
import com.ledger.os.model.Expense;
import com.ledger.os.model.Income;
import com.ledger.os.model.RecurringExpense;
import com.ledger.os.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponseDto(
        Long id,
        TransactionType type,
        BigDecimal amount,
        String currency,
        BigDecimal signedAmount,
        LocalDate date,
        String note,
        Category category,
        String categoryIcon,
        String categoryHexColor,
        String frequency,
        String description
) {

    public static TransactionResponseDto fromEntity(Transaction tx) {
        TransactionType type;
        String frequency = null;

        if (tx instanceof RecurringExpense re) {
            type = TransactionType.RECURRING_EXPENSE;
            frequency = re.getFrequency().name();
        } else if (tx instanceof Expense) {
            type = TransactionType.EXPENSE;
        } else if (tx instanceof Income) {
            type = TransactionType.INCOME;
        } else {
            type = TransactionType.EXPENSE;
        }

        return new TransactionResponseDto(
                tx.getId(),
                type,
                tx.getAmount().getAmount(),
                tx.getAmount().getCurrencyCode(),
                tx.getSignedAmount().getAmount(),
                tx.getDate(),
                tx.getNote(),
                tx.getCategory(),
                tx.getCategory().getIcon(),
                tx.getCategory().getHexColor(),
                frequency,
                tx.describe()
        );
    }
}
