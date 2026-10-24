package com.ledger.os.factory;

import com.ledger.os.model.Category;
import com.ledger.os.model.Frequency;
import com.ledger.os.model.Money;

import java.time.LocalDate;

/**
 * Data Transfer Object providing parameters for transaction instantiation.
 */
public record TransactionRequest(
        Long id,
        TransactionType type,
        Money amount,
        LocalDate date,
        String note,
        Category category,
        Frequency frequency
) {

    public TransactionRequest(
            TransactionType type,
            Money amount,
            LocalDate date,
            String note,
            Category category
    ) {
        this(null, type, amount, date, note, category, null);
    }

    public TransactionRequest(
            TransactionType type,
            Money amount,
            LocalDate date,
            String note,
            Category category,
            Frequency frequency
    ) {
        this(null, type, amount, date, note, category, frequency);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private TransactionType type;
        private Money amount;
        private LocalDate date;
        private String note;
        private Category category;
        private Frequency frequency;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder amount(Money amount) {
            this.amount = amount;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Builder frequency(Frequency frequency) {
            this.frequency = frequency;
            return this;
        }

        public TransactionRequest build() {
            return new TransactionRequest(id, type, amount, date, note, category, frequency);
        }
    }
}
