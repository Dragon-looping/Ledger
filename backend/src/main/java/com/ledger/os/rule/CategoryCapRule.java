package com.ledger.os.rule;

import com.ledger.os.model.Category;
import com.ledger.os.model.Money;
import com.ledger.os.model.Wallet;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Strategy implementation evaluating spending for a specific Category against a category cap.
 */
public class CategoryCapRule implements BudgetRule {

    private final Category category;
    private final Money categoryCap;

    public CategoryCapRule(Category category, Money categoryCap) {
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        if (categoryCap == null || !categoryCap.isPositive()) {
            throw new IllegalArgumentException("Category cap must be strictly positive");
        }
        this.categoryCap = categoryCap;
    }

    @Override
    public BudgetStatus evaluate(Wallet wallet, YearMonth month) {
        Objects.requireNonNull(wallet, "Wallet cannot be null");
        Objects.requireNonNull(month, "YearMonth cannot be null");

        Money spent = wallet.spentByCategory(month)
                .getOrDefault(category, Money.zero(categoryCap.getCurrency()));

        return BudgetStatus.of(getName(), categoryCap, spent);
    }

    @Override
    public String getName() {
        return "CategoryCapRule[" + category + "=" + categoryCap + "]";
    }

    public Category getCategory() {
        return category;
    }

    public Money getCategoryCap() {
        return categoryCap;
    }
}
