package com.ledger.os.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Immutable value object representing a monetary amount with an associated ISO currency.
 */
@Embeddable
public final class Money implements Comparable<Money>, Serializable {

    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currencyCode;

    // Protected default constructor for JPA
    protected Money() {
    }

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new InvalidAmountException("Monetary amount cannot be null");
        }
        if (currency == null) {
            throw new InvalidAmountException("Monetary currency cannot be null");
        }
        this.amount = amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
        this.currencyCode = currency.getCurrencyCode();
    }

    public Money(BigDecimal amount, String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new InvalidAmountException("Currency code cannot be null or blank");
        }
        try {
            Currency parsedCurrency = Currency.getInstance(currencyCode.trim().toUpperCase());
            if (amount == null) {
                throw new InvalidAmountException("Monetary amount cannot be null");
            }
            this.amount = amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
            this.currencyCode = parsedCurrency.getCurrencyCode();
        } catch (IllegalArgumentException e) {
            throw new InvalidAmountException("Invalid ISO currency code: " + currencyCode, e);
        }
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, currencyCode);
    }

    public static Money of(String amount, String currencyCode) {
        if (amount == null || amount.isBlank()) {
            throw new InvalidAmountException("Amount string cannot be null or blank");
        }
        try {
            return new Money(new BigDecimal(amount.trim()), currencyCode);
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Invalid numeric format for monetary amount: " + amount, e);
        }
    }

    public static Money of(double amount, String currencyCode) {
        return new Money(BigDecimal.valueOf(amount), currencyCode);
    }

    public static Money of(double amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public static Money zero(String currencyCode) {
        return new Money(BigDecimal.ZERO, currencyCode);
    }

    public static Money usd(double amount) {
        return of(amount, "USD");
    }

    public static Money usd(BigDecimal amount) {
        return of(amount, "USD");
    }

    public static Money usd(String amount) {
        return of(amount, "USD");
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return Currency.getInstance(currencyCode);
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currencyCode);
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currencyCode);
    }

    public Money negate() {
        return new Money(this.amount.negate(), this.currencyCode);
    }

    public Money abs() {
        return new Money(this.amount.abs(), this.currencyCode);
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public int compareTo(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare Money with null");
        }
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return this.currencyCode.equals(other.currencyCode)
                && this.amount.compareTo(other.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.amount, this.currencyCode);
    }

    @Override
    public String toString() {
        return String.format("%s %s", currencyCode, amount.toPlainString());
    }

    private void validateSameCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Target Money must not be null");
        }
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new IllegalArgumentException(String.format(
                    "Currency mismatch: cannot operate on %s and %s",
                    this.currencyCode, other.currencyCode
            ));
        }
    }
}
