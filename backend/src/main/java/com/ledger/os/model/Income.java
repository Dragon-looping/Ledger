package com.ledger.os.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;

/**
 * Represents an inflow of money for a given income category.
 */
@Entity
@DiscriminatorValue("INCOME")
public class Income extends Transaction {

    // Protected default constructor for JPA
    protected Income() {
        super();
    }

    public Income(Long id, Money amount, LocalDate date, String note, Category category) {
        super(id, amount, date, note, category);
    }

    public Income(Money amount, LocalDate date, String note, Category category) {
        super(amount, date, note, category);
    }

    @Override
    public Money getSignedAmount() {
        return getAmount();
    }

    @Override
    public String describe() {
        String noteSuffix = getNote().isBlank() ? "" : " (" + getNote() + ")";
        return String.format("[INCOME] %s: +%s on %s%s",
                getCategory(),
                getAmount(),
                getDate(),
                noteSuffix);
    }
}
