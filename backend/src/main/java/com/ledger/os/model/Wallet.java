package com.ledger.os.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Root domain aggregate representing an account / wallet composing transactions and budgets.
 */
@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrencyCode;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets = new ArrayList<>();

    // Protected default constructor for JPA
    protected Wallet() {
    }

    public Wallet(Long id, String name, Currency defaultCurrency) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Wallet name cannot be null or blank");
        }
        if (defaultCurrency == null) {
            throw new IllegalArgumentException("Default currency cannot be null");
        }
        this.id = id;
        this.name = name.trim();
        this.defaultCurrencyCode = defaultCurrency.getCurrencyCode();
    }

    public Wallet(String name, Currency defaultCurrency) {
        this(null, name, defaultCurrency);
    }

    public Wallet(String name, String currencyCode) {
        this(null, name, Currency.getInstance(currencyCode));
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Currency getDefaultCurrency() {
        return Currency.getInstance(defaultCurrencyCode);
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public List<Budget> getBudgets() {
        return Collections.unmodifiableList(budgets);
    }

    public void addTransaction(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");
        this.transactions.add(transaction);
    }

    public void addBudget(Budget budget) {
        Objects.requireNonNull(budget, "Budget cannot be null");
        this.budgets.add(budget);
    }

    /**
     * Calculates the total spent (expenses) for a specified YearMonth period.
     */
    public Money totalSpent(YearMonth period) {
        Objects.requireNonNull(period, "Period cannot be null");
        Money total = Money.zero(getDefaultCurrency());
        for (Transaction transaction : transactions) {
            if (transaction instanceof Expense && YearMonth.from(transaction.getDate()).equals(period)) {
                total = total.add(transaction.getAmount());
            }
        }
        return total;
    }

    /**
     * Calculates total income for a specified YearMonth period.
     */
    public Money totalIncome(YearMonth period) {
        Objects.requireNonNull(period, "Period cannot be null");
        Money total = Money.zero(getDefaultCurrency());
        for (Transaction transaction : transactions) {
            if (transaction instanceof Income && YearMonth.from(transaction.getDate()).equals(period)) {
                total = total.add(transaction.getAmount());
            }
        }
        return total;
    }

    /**
     * Calculates net balance (income minus expenses) for a specified YearMonth period.
     */
    public Money netBalance(YearMonth period) {
        return totalIncome(period).subtract(totalSpent(period));
    }

    /**
     * Returns an aggregation map of total expenses grouped by category for a given month.
     */
    public Map<Category, Money> spentByCategory(YearMonth period) {
        Objects.requireNonNull(period, "Period cannot be null");
        Map<Category, Money> map = new EnumMap<>(Category.class);

        for (Transaction transaction : transactions) {
            if (transaction instanceof Expense && YearMonth.from(transaction.getDate()).equals(period)) {
                Category cat = transaction.getCategory();
                Money current = map.getOrDefault(cat, Money.zero(getDefaultCurrency()));
                map.put(cat, current.add(transaction.getAmount()));
            }
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Validates if spending for a specific category and period is within budget limits.
     * Throws InsufficientBudgetException if the budget exists and has been exceeded.
     */
    public void checkBudget(Category category, YearMonth period) {
        Objects.requireNonNull(category, "Category cannot be null");
        Objects.requireNonNull(period, "Period cannot be null");

        Optional<Budget> budgetOpt = budgets.stream()
                .filter(b -> b.getCategory() == category && b.getPeriod().equals(period))
                .findFirst();

        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            Money spent = spentByCategory(period).getOrDefault(category, Money.zero(getDefaultCurrency()));
            if (budget.isExceededBy(spent)) {
                throw new InsufficientBudgetException(String.format(
                        "Budget exceeded for %s in %s! Limit: %s, Current Spent: %s",
                        category, period, budget.getLimit(), spent
                ));
            }
        }
    }
}
