package com.ledger.os.factory;

/**
 * Discriminator enum indicating which Transaction subclass to instantiate.
 */
public enum TransactionType {
    EXPENSE,
    INCOME,
    RECURRING_EXPENSE
}
