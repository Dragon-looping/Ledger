package com.ledger.os.model;

public class InsufficientBudgetException extends RuntimeException {
    public InsufficientBudgetException(String message) {
        super(message);
    }

    public InsufficientBudgetException(String message, Throwable cause) {
        super(message, cause);
    }
}
