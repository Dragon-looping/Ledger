package com.ledger.os.rule;

import com.ledger.os.model.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Result of evaluating a BudgetRule against wallet transactions for a given period.
 *
 * @param ruleName    Identifier of the evaluated rule
 * @param limit       Allocated spending limit for the period
 * @param spent       Actual spending incurred in the period
 * @param percentUsed Percentage of the limit consumed (e.g. 80.0, 100.0)
 * @param level       Status level (OK < 80%, WARNING >= 80%, EXCEEDED >= 100%)
 */
public record BudgetStatus(
        String ruleName,
        Money limit,
        Money spent,
        double percentUsed,
        BudgetLevel level
) {

    public BudgetStatus {
        Objects.requireNonNull(ruleName, "Rule name cannot be null");
        Objects.requireNonNull(limit, "Limit cannot be null");
        Objects.requireNonNull(spent, "Spent cannot be null");
        Objects.requireNonNull(level, "BudgetLevel cannot be null");
    }

    /**
     * Factory constructor calculating percentUsed and assigning the appropriate BudgetLevel.
     */
    public static BudgetStatus of(String ruleName, Money limit, Money spent) {
        if (limit == null || spent == null) {
            throw new IllegalArgumentException("Limit and spent Money must not be null");
        }

        double percent;
        if (limit.isZero() || limit.isNegative()) {
            percent = spent.isPositive() ? 100.0 : 0.0;
        } else {
            percent = spent.getAmount()
                    .divide(limit.getAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        BudgetLevel level;
        if (percent >= 100.0) {
            level = BudgetLevel.EXCEEDED;
        } else if (percent >= 80.0) {
            level = BudgetLevel.WARNING;
        } else {
            level = BudgetLevel.OK;
        }

        return new BudgetStatus(ruleName, limit, spent, percent, level);
    }

    public Money getLimit() {
        return limit;
    }

    public Money getSpent() {
        return spent;
    }

    public double getPercentUsed() {
        return percentUsed;
    }

    public BudgetLevel getLevel() {
        return level;
    }

    public String getRuleName() {
        return ruleName;
    }
}
