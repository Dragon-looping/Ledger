package com.ledger.os.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wallet Aggregate & Budget Verification Tests")
class WalletTest {

    private Wallet wallet;
    private final YearMonth currentMonth = YearMonth.of(2026, 10);
    private final YearMonth previousMonth = YearMonth.of(2026, 9);
    private static final Currency USD = Currency.getInstance("USD");

    @BeforeEach
    void setUp() {
        wallet = new Wallet("Personal Checking", USD);
    }

    @Test
    @DisplayName("Should correctly calculate total spent for a specific YearMonth")
    void shouldCalculateTotalSpentPerMonth() {
        // Current month expenses
        wallet.addTransaction(new Expense(Money.usd(50.00), LocalDate.of(2026, 10, 5), "Dinner", Category.FOOD));
        wallet.addTransaction(new Expense(Money.usd(30.00), LocalDate.of(2026, 10, 12), "Subway pass", Category.TRANSPORT));
        wallet.addTransaction(new RecurringExpense(Money.usd(1200.00), LocalDate.of(2026, 10, 1), "Rent", Category.RENT, Frequency.MONTHLY));

        // Income in current month (should not be counted in totalSpent)
        wallet.addTransaction(new Income(Money.usd(4000.00), LocalDate.of(2026, 10, 1), "Salary", Category.SALARY));

        // Expense in previous month (should not be counted in current month)
        wallet.addTransaction(new Expense(Money.usd(100.00), LocalDate.of(2026, 9, 20), "Old groceries", Category.FOOD));

        Money spentCurrentMonth = wallet.totalSpent(currentMonth);
        Money spentPreviousMonth = wallet.totalSpent(previousMonth);

        // 50 + 30 + 1200 = 1280.00
        assertEquals(Money.usd(1280.00), spentCurrentMonth);
        assertEquals(Money.usd(100.00), spentPreviousMonth);
    }

    @Test
    @DisplayName("Should aggregate spent amounts grouped by category for a specific month")
    void shouldAggregateSpentByCategory() {
        wallet.addTransaction(new Expense(Money.usd(45.00), LocalDate.of(2026, 10, 2), "Groceries", Category.FOOD));
        wallet.addTransaction(new Expense(Money.usd(35.50), LocalDate.of(2026, 10, 8), "Restaurant", Category.FOOD));
        wallet.addTransaction(new Expense(Money.usd(25.00), LocalDate.of(2026, 10, 15), "Taxi", Category.TRANSPORT));
        wallet.addTransaction(new Income(Money.usd(3500.00), LocalDate.of(2026, 10, 1), "Paycheck", Category.SALARY));

        Map<Category, Money> categoryMap = wallet.spentByCategory(currentMonth);

        assertEquals(2, categoryMap.size());
        assertEquals(Money.usd(80.50), categoryMap.get(Category.FOOD));
        assertEquals(Money.usd(25.00), categoryMap.get(Category.TRANSPORT));
        assertNull(categoryMap.get(Category.SALARY));
    }

    @Test
    @DisplayName("Should calculate total income and net balance for a specific month")
    void shouldCalculateTotalIncomeAndNetBalance() {
        wallet.addTransaction(new Income(Money.usd(4500.00), LocalDate.of(2026, 10, 1), "Primary Job", Category.SALARY));
        wallet.addTransaction(new Income(Money.usd(500.00), LocalDate.of(2026, 10, 15), "Freelance", Category.OTHER));
        wallet.addTransaction(new Expense(Money.usd(1200.00), LocalDate.of(2026, 10, 1), "Rent", Category.RENT));
        wallet.addTransaction(new Expense(Money.usd(300.00), LocalDate.of(2026, 10, 10), "Groceries", Category.FOOD));

        assertEquals(Money.usd(5000.00), wallet.totalIncome(currentMonth));
        assertEquals(Money.usd(1500.00), wallet.totalSpent(currentMonth));
        // Net balance: 5000.00 - 1500.00 = 3500.00
        assertEquals(Money.usd(3500.00), wallet.netBalance(currentMonth));
    }

    @Test
    @DisplayName("checkBudget should pass when spending is below or equal to budget limit")
    void shouldPassWhenSpendingWithinBudget() {
        wallet.addBudget(new Budget(Category.FOOD, Money.usd(300.00), currentMonth));
        wallet.addTransaction(new Expense(Money.usd(120.00), LocalDate.of(2026, 10, 5), "Market", Category.FOOD));
        wallet.addTransaction(new Expense(Money.usd(80.00), LocalDate.of(2026, 10, 10), "Dinner", Category.FOOD));

        // 200.00 spent <= 300.00 limit -> no exception
        assertDoesNotThrow(() -> wallet.checkBudget(Category.FOOD, currentMonth));
    }

    @Test
    @DisplayName("checkBudget should throw InsufficientBudgetException when spending exceeds limit")
    void shouldThrowWhenBudgetExceeded() {
        wallet.addBudget(new Budget(Category.SHOPPING, Money.usd(150.00), currentMonth));
        wallet.addTransaction(new Expense(Money.usd(100.00), LocalDate.of(2026, 10, 3), "Shoes", Category.SHOPPING));
        wallet.addTransaction(new Expense(Money.usd(80.00), LocalDate.of(2026, 10, 14), "Jacket", Category.SHOPPING));

        // 180.00 spent > 150.00 limit -> must throw
        InsufficientBudgetException ex = assertThrows(
                InsufficientBudgetException.class,
                () -> wallet.checkBudget(Category.SHOPPING, currentMonth)
        );

        assertTrue(ex.getMessage().contains("Budget exceeded for SHOPPING"));
        assertTrue(ex.getMessage().contains("Limit: USD 150.00"));
        assertTrue(ex.getMessage().contains("Current Spent: USD 180.00"));
    }

    @Test
    @DisplayName("Budget validation throws InvalidAmountException for non-positive limits")
    void shouldValidateBudgetLimit() {
        assertThrows(InvalidAmountException.class, () ->
                new Budget(Category.FOOD, Money.usd(0.00), currentMonth)
        );
        assertThrows(InvalidAmountException.class, () ->
                new Budget(Category.FOOD, Money.usd(-50.00), currentMonth)
        );
    }
}
