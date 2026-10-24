package com.ledger.os.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Budget constraint set for a specific category and monthly period.
 */
@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private Category category;

    @Embedded
    private Money limit;

    @Column(name = "period_month", nullable = false)
    private int month;

    @Column(name = "period_year", nullable = false)
    private int year;

    // Protected default constructor for JPA
    protected Budget() {
    }

    public Budget(Long id, Category category, Money limit, YearMonth period) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (limit == null) {
            throw new InvalidAmountException("Budget limit cannot be null");
        }
        if (!limit.isPositive()) {
            throw new InvalidAmountException("Budget limit must be strictly positive, but was: " + limit);
        }
        if (period == null) {
            throw new IllegalArgumentException("Period cannot be null");
        }
        this.id = id;
        this.category = category;
        this.limit = limit;
        this.year = period.getYear();
        this.month = period.getMonthValue();
    }

    public Budget(Category category, Money limit, YearMonth period) {
        this(null, category, limit, period);
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public Money getLimit() {
        return limit;
    }

    public YearMonth getPeriod() {
        return YearMonth.of(year, month);
    }

    /**
     * Checks if a total spent amount exceeds this budget's limit.
     */
    public boolean isExceededBy(Money spent) {
        Objects.requireNonNull(spent, "Spent amount cannot be null");
        return spent.compareTo(this.limit) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Budget budget)) return false;
        return month == budget.month
                && year == budget.year
                && Objects.equals(id, budget.id)
                && category == budget.category
                && Objects.equals(limit, budget.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category, limit, month, year);
    }

    @Override
    public String toString() {
        return String.format("Budget[%s: %s limit for %04d-%02d]", category, limit, year, month);
    }
}
