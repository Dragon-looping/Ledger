package com.ledger.os.factory;

import com.ledger.os.model.Category;
import com.ledger.os.model.Expense;
import com.ledger.os.model.Frequency;
import com.ledger.os.model.Income;
import com.ledger.os.model.Money;
import com.ledger.os.model.RecurringExpense;
import com.ledger.os.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Factory Pattern: TransactionFactory Tests")
class TransactionFactoryTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 10, 24);

    @Test
    @DisplayName("Should create Income subclass when type is INCOME")
    void shouldCreateIncome() {
        TransactionRequest request = TransactionRequest.builder()
                .id(101L)
                .type(TransactionType.INCOME)
                .amount(Money.usd(3500.00))
                .date(TEST_DATE)
                .note("Bi-weekly paycheck")
                .category(Category.SALARY)
                .build();

        Transaction tx = TransactionFactory.create(request);

        assertNotNull(tx);
        assertInstanceOf(Income.class, tx);
        assertEquals(101L, tx.getId());
        assertEquals(Money.usd(3500.00), tx.getAmount());
        assertEquals(Money.usd(3500.00), tx.getSignedAmount());
        assertEquals(Category.SALARY, tx.getCategory());
        assertEquals("Bi-weekly paycheck", tx.getNote());
    }

    @Test
    @DisplayName("Should create Expense subclass when type is EXPENSE")
    void shouldCreateExpense() {
        TransactionRequest request = new TransactionRequest(
                TransactionType.EXPENSE,
                Money.usd(65.40),
                TEST_DATE,
                "Dinner",
                Category.FOOD
        );

        Transaction tx = TransactionFactory.create(request);

        assertNotNull(tx);
        assertInstanceOf(Expense.class, tx);
        assertEquals(Money.usd(65.40), tx.getAmount());
        assertEquals(Money.usd(-65.40), tx.getSignedAmount());
        assertEquals(Category.FOOD, tx.getCategory());
    }

    @Test
    @DisplayName("Should create RecurringExpense subclass when type is RECURRING_EXPENSE with valid frequency")
    void shouldCreateRecurringExpense() {
        TransactionRequest request = TransactionRequest.builder()
                .id(202L)
                .type(TransactionType.RECURRING_EXPENSE)
                .amount(Money.usd(12.99))
                .date(TEST_DATE)
                .note("Cloud storage subscription")
                .category(Category.BILLS)
                .frequency(Frequency.MONTHLY)
                .build();

        Transaction tx = TransactionFactory.create(request);

        assertNotNull(tx);
        assertInstanceOf(RecurringExpense.class, tx);
        RecurringExpense recurring = (RecurringExpense) tx;
        assertEquals(Frequency.MONTHLY, recurring.getFrequency());
        assertEquals(Money.usd(-12.99), recurring.getSignedAmount());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when creating RECURRING_EXPENSE without frequency")
    void shouldThrowWhenRecurringExpenseLacksFrequency() {
        TransactionRequest request = new TransactionRequest(
                TransactionType.RECURRING_EXPENSE,
                Money.usd(100.00),
                TEST_DATE,
                "Gym membership",
                Category.HEALTH,
                null // missing frequency
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionFactory.create(request)
        );
        assertTrue(ex.getMessage().contains("Frequency must be specified"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when request DTO or type is null")
    void shouldThrowWhenDtoOrTypeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> TransactionFactory.create(null));

        TransactionRequest missingType = TransactionRequest.builder()
                .amount(Money.usd(50.00))
                .date(TEST_DATE)
                .category(Category.OTHER)
                .build();

        assertThrows(IllegalArgumentException.class, () -> TransactionFactory.create(missingType));
    }
}
