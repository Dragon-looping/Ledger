package com.ledger.os.rule;

import com.ledger.os.model.Money;
import com.ledger.os.model.Wallet;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Strategy implementation evaluating total monthly wallet spending against a fixed cap.
 */
public class MonthlyCapRule implements BudgetRule {

    private final Money monthlyCap;

    public MonthlyCapRule(Money monthlyCap) {
        if (monthlyCap == null || !monthlyCap.isPositive()) {
            throw new IllegalArgumentException("Monthly cap must be strictly positive");
        }
        this.monthlyCap = monthlyCap;
    }

    @Override
    public BudgetStatus evaluate(Wallet wallet, YearMonth month) {
        Objects.requireNonNull(wallet, "Wallet cannot be null");
        Objects.requireNonNull(month, "YearMonth cannot be null");

        Money spent = wallet.totalSpent(month);
        return BudgetStatus.of(getName(), monthlyCap, spent);
    }

    @Override
    public String getName() {
        return "MonthlyCapRule[" + monthlyCap + "]";
    }

    public Money getMonthlyCap() {
        return monthlyCap;
    }
}
