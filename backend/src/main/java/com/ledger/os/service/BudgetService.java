package com.ledger.os.service;

import com.ledger.os.event.BudgetThresholdEvent;
import com.ledger.os.model.Transaction;
import com.ledger.os.model.Wallet;
import com.ledger.os.rule.BudgetLevel;
import com.ledger.os.rule.BudgetRule;
import com.ledger.os.rule.BudgetStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service managing budget evaluation and threshold alert publication.
 * Uses Spring's ApplicationEventPublisher to dispatch BudgetThresholdEvents when
 * spending crosses 80% (WARNING) or 100% (EXCEEDED).
 * Guarantees that each level is fired strictly once per rule per month.
 */
@Service
public class BudgetService {

    private final ApplicationEventPublisher eventPublisher;

    // Registry of active budget rules
    private final List<BudgetRule> registeredRules = new CopyOnWriteArrayList<>();

    // Tracks fired alerts: "walletId/name::ruleName::YYYY-MM::LEVEL"
    private final Set<String> firedAlerts = ConcurrentHashMap.newKeySet();

    public BudgetService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    public void registerRule(BudgetRule rule) {
        Objects.requireNonNull(rule, "BudgetRule cannot be null");
        this.registeredRules.add(rule);
    }

    public void clearRegisteredRules() {
        this.registeredRules.clear();
    }

    public List<BudgetRule> getRegisteredRules() {
        return Collections.unmodifiableList(registeredRules);
    }

    /**
     * Adds the transaction to the wallet and evaluates registered budget rules.
     */
    public void processTransaction(Wallet wallet, Transaction transaction) {
        processTransaction(wallet, transaction, this.registeredRules);
    }

    /**
     * Adds the transaction to the wallet and evaluates the specified budget rules.
     */
    public void processTransaction(Wallet wallet, Transaction transaction, List<BudgetRule> rules) {
        Objects.requireNonNull(wallet, "Wallet cannot be null");
        Objects.requireNonNull(transaction, "Transaction cannot be null");
        Objects.requireNonNull(rules, "Rules list cannot be null");

        // Add transaction to wallet aggregate
        wallet.addTransaction(transaction);

        YearMonth period = YearMonth.from(transaction.getDate());

        for (BudgetRule rule : rules) {
            BudgetStatus status = rule.evaluate(wallet, period);
            evaluateAndPublishThresholds(wallet, rule, period, status, transaction);
        }
    }

    private void evaluateAndPublishThresholds(
            Wallet wallet,
            BudgetRule rule,
            YearMonth period,
            BudgetStatus status,
            Transaction transaction
    ) {
        if (status.level() == BudgetLevel.WARNING) {
            // Passed 80% threshold
            if (!hasFired(wallet, rule, period, BudgetLevel.WARNING)) {
                publishEvent(wallet, rule, period, BudgetLevel.WARNING, status, transaction);
            }
        } else if (status.level() == BudgetLevel.EXCEEDED) {
            // Passed 100% threshold: also fire WARNING if skipped directly
            if (!hasFired(wallet, rule, period, BudgetLevel.WARNING)) {
                publishEvent(wallet, rule, period, BudgetLevel.WARNING, status, transaction);
            }
            if (!hasFired(wallet, rule, period, BudgetLevel.EXCEEDED)) {
                publishEvent(wallet, rule, period, BudgetLevel.EXCEEDED, status, transaction);
            }
        }
    }

    private void publishEvent(
            Wallet wallet,
            BudgetRule rule,
            YearMonth period,
            BudgetLevel level,
            BudgetStatus status,
            Transaction transaction
    ) {
        markFired(wallet, rule, period, level);
        BudgetThresholdEvent event = new BudgetThresholdEvent(
                this,
                wallet,
                rule,
                period,
                level,
                status,
                transaction
        );
        eventPublisher.publishEvent(event);
    }

    public boolean hasFired(Wallet wallet, BudgetRule rule, YearMonth period, BudgetLevel level) {
        return firedAlerts.contains(buildAlertKey(wallet, rule, period, level));
    }

    public void markFired(Wallet wallet, BudgetRule rule, YearMonth period, BudgetLevel level) {
        firedAlerts.add(buildAlertKey(wallet, rule, period, level));
    }

    public void resetFiredThresholds() {
        firedAlerts.clear();
    }

    private String buildAlertKey(Wallet wallet, BudgetRule rule, YearMonth period, BudgetLevel level) {
        String walletIdentifier = wallet.getId() != null ? String.valueOf(wallet.getId()) : wallet.getName();
        return walletIdentifier + "::" + rule.getName() + "::" + period + "::" + level.name();
    }
}
