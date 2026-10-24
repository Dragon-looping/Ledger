package com.ledger.os.factory;

import com.ledger.os.model.Expense;
import com.ledger.os.model.Income;
import com.ledger.os.model.RecurringExpense;
import com.ledger.os.model.Transaction;

import java.util.Objects;

/**
 * Factory creating specific Transaction subclasses based on TransactionRequest parameters.
 */
public final class TransactionFactory {

    private TransactionFactory() {
    }

    /**
     * Creates and returns the appropriate Transaction subclass.
     *
     * @param dto the request payload
     * @return an Expense, Income, or RecurringExpense
     * @throws IllegalArgumentException if the DTO or required type parameters are missing
     */
    public static Transaction create(TransactionRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("TransactionRequest DTO cannot be null");
        }
        if (dto.type() == null) {
            throw new IllegalArgumentException("TransactionType must be provided");
        }

        return switch (dto.type()) {
            case INCOME -> new Income(
                    dto.id(),
                    dto.amount(),
                    dto.date(),
                    dto.note(),
                    dto.category()
            );
            case EXPENSE -> new Expense(
                    dto.id(),
                    dto.amount(),
                    dto.date(),
                    dto.note(),
                    dto.category()
            );
            case RECURRING_EXPENSE -> {
                if (dto.frequency() == null) {
                    throw new IllegalArgumentException(
                            "Frequency must be specified when creating a RECURRING_EXPENSE"
                    );
                }
                yield new RecurringExpense(
                        dto.id(),
                        dto.amount(),
                        dto.date(),
                        dto.note(),
                        dto.category(),
                        dto.frequency()
                );
            }
        };
    }
}
