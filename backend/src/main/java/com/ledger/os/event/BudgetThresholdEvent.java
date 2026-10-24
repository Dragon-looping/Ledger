package com.ledger.os.event;

import com.ledger.os.model.Transaction;
import com.ledger.os.model.Wallet;
import com.ledger.os.rule.BudgetLevel;
import com.ledger.os.rule.BudgetRule;
import com.ledger.os.rule.BudgetStatus;
import org.springframework.context.ApplicationEvent;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Domain application event published whenever a transaction causes a budget rule
 * to breach the 80% (WARNING) or 100% (EXCEEDED) threshold.
 */
public class BudgetThresholdEvent extends ApplicationEvent {

    private final Wallet wallet;
    private final BudgetRule rule;
    private final YearMonth period;
    private final BudgetLevel level;
    private final BudgetStatus status;
    private final Transaction triggeringTransaction;

    public BudgetThresholdEvent(
            Object source,
            Wallet wallet,
            BudgetRule rule,
            YearMonth period,
            BudgetLevel level,
            BudgetStatus status,
            Transaction triggeringTransaction
    ) {
        super(source);
        this.wallet = Objects.requireNonNull(wallet, "Wallet cannot be null");
        this.rule = Objects.requireNonNull(rule, "Rule cannot be null");
        this.period = Objects.requireNonNull(period, "Period cannot be null");
        this.level = Objects.requireNonNull(level, "Level cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.triggeringTransaction = Objects.requireNonNull(triggeringTransaction, "Triggering transaction cannot be null");
    }

    public Wallet getWallet() {
        return wallet;
    }

    public BudgetRule getRule() {
        return rule;
    }

    public YearMonth getPeriod() {
        return period;
    }

    public BudgetLevel getLevel() {
        return level;
    }

    public BudgetStatus getStatus() {
        return status;
    }

    public Transaction getTriggeringTransaction() {
        return triggeringTransaction;
    }

    @Override
    public String toString() {
        return String.format("BudgetThresholdEvent[%s on %s - %s (spent=%s/limit=%s - %.1f%%) by tx=%s]",
                rule.getName(), period, level, status.spent(), status.limit(), status.percentUsed(),
                triggeringTransaction.describe());
    }
}
