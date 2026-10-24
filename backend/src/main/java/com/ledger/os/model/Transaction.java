package com.ledger.os.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Base domain model for all financial transactions in Ledger OS.
 */
@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "transaction_type")
public abstract class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Money amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate date;

    @Column(name = "note", length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private Category category;

    // Protected default constructor for JPA
    protected Transaction() {
    }

    protected Transaction(Long id, Money amount, LocalDate date, String note, Category category) {
        if (amount == null) {
            throw new InvalidAmountException("Transaction amount cannot be null");
        }
        if (!amount.isPositive()) {
            throw new InvalidAmountException(
                    "Transaction amount must be strictly positive, but was: " + amount
            );
        }
        this.id = id;
        this.amount = amount;
        this.date = Objects.requireNonNull(date, "Transaction date cannot be null");
        this.category = Objects.requireNonNull(category, "Transaction category cannot be null");
        this.note = note != null ? note.trim() : "";
    }

    protected Transaction(Money amount, LocalDate date, String note, Category category) {
        this(null, amount, date, note, category);
    }

    /**
     * Returns the signed monetary amount: negative for expenses and positive for incomes.
     */
    public abstract Money getSignedAmount();

    /**
     * Returns a human-readable description of the transaction.
     */
    public abstract String describe();

    public Long getId() {
        return id;
    }

    public Money getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(id, that.id)
                && Objects.equals(amount, that.amount)
                && Objects.equals(date, that.date)
                && Objects.equals(note, that.note)
                && category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, date, note, category);
    }

    @Override
    public String toString() {
        return describe();
    }
}
