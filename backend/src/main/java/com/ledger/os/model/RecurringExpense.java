package com.ledger.os.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a recurring outflow of money occurring at fixed intervals.
 */
@Entity
@DiscriminatorValue("RECURRING_EXPENSE")
public class RecurringExpense extends Expense {

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 20)
    private Frequency frequency;

    // Protected default constructor for JPA
    protected RecurringExpense() {
        super();
    }

    public RecurringExpense(Long id, Money amount, LocalDate date, String note, Category category, Frequency frequency) {
        super(id, amount, date, note, category);
        this.frequency = Objects.requireNonNull(frequency, "Frequency cannot be null");
    }

    public RecurringExpense(Money amount, LocalDate date, String note, Category category, Frequency frequency) {
        this(null, amount, date, note, category, frequency);
    }

    public Frequency getFrequency() {
        return frequency;
    }

    @Override
    public String describe() {
        String noteSuffix = getNote().isBlank() ? "" : " (" + getNote() + ")";
        return String.format("[RECURRING EXPENSE - %s] %s: -%s on %s%s",
                frequency,
                getCategory(),
                getAmount(),
                getDate(),
                noteSuffix);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecurringExpense that)) return false;
        if (!super.equals(o)) return false;
        return frequency == that.frequency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), frequency);
    }
}
