package com.ledger.os.rule;

import com.ledger.os.model.Wallet;

import java.time.YearMonth;

/**
 * Strategy interface for evaluating budget policies over wallet transactions.
 */
public interface BudgetRule {

    /**
     * Evaluates this rule against the wallet's activity for the specified YearMonth.
     */
    BudgetStatus evaluate(Wallet w, YearMonth m);

    /**
     * Unique or human-readable identifier for this rule instance.
     */
    String getName();
}
