package com.ledger.os.rule;

import com.ledger.os.model.Money;
import com.ledger.os.model.Wallet;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Strategy implementation evaluating whether actual spending preserves a defined savings goal.
 * The spending allowance is defined as: (monthly income - target savings goal).
 */
public class SavingsGoalRule implements BudgetRule {

    private final Money targetSavings;
    private final Money expectedIncome;

    public SavingsGoalRule(Money targetSavings) {
        this(targetSavings, null);
    }

    public SavingsGoalRule(Money targetSavings, Money expectedIncome) {
        if (targetSavings == null || !targetSavings.isPositive()) {
            throw new IllegalArgumentException("Target savings goal must be strictly positive");
        }
        if (expectedIncome != null && !expectedIncome.isPositive()) {
            throw new IllegalArgumentException("Expected income must be strictly positive when provided");
        }
        this.targetSavings = targetSavings;
        this.expectedIncome = expectedIncome;
    }

    @Override
    public BudgetStatus evaluate(Wallet wallet, YearMonth month) {
        Objects.requireNonNull(wallet, "Wallet cannot be null");
        Objects.requireNonNull(month, "YearMonth cannot be null");

        Money income = expectedIncome != null ? expectedIncome : wallet.totalIncome(month);

        // Allowable spending limit = income - targetSavings
        Money allowableSpending = income.compareTo(targetSavings) > 0
                ? income.subtract(targetSavings)
                : Money.zero(targetSavings.getCurrency());

        Money spent = wallet.totalSpent(month);
        return BudgetStatus.of(getName(), allowableSpending, spent);
    }

    @Override
    public String getName() {
        return "SavingsGoalRule[TargetSavings=" + targetSavings
                + (expectedIncome != null ? ",ExpectedIncome=" + expectedIncome : "") + "]";
    }

    public Money getTargetSavings() {
        return targetSavings;
    }

    public Money getExpectedIncome() {
        return expectedIncome;
    }
}
