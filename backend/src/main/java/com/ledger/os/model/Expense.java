package com.ledger.os.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;

/**
 * Represents an outflow of money for a given expense category.
 */
@Entity
@DiscriminatorValue("EXPENSE")
public class Expense extends Transaction {

    // Protected default constructor for JPA
    protected Expense() {
        super();
    }

    public Expense(Long id, Money amount, LocalDate date, String note, Category category) {
        super(id, amount, date, note, category);
    }

    public Expense(Money amount, LocalDate date, String note, Category category) {
        super(amount, date, note, category);
    }

    @Override
    public Money getSignedAmount() {
        return getAmount().negate();
    }

    @Override
    public String describe() {
        String noteSuffix = getNote().isBlank() ? "" : " (" + getNote() + ")";
        return String.format("[EXPENSE] %s: -%s on %s%s",
                getCategory(),
                getAmount(),
                getDate(),
                noteSuffix);
    }
}
