package com.ledger.os.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transaction Polymorphism & Domain Tests")
class TransactionPolymorphismTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 10, 24);

    @Nested
    @DisplayName("Validation in Constructors")
    class ConstructorValidationTests {

        @Test
        @DisplayName("Should throw InvalidAmountException when transaction amount is null")
        void shouldThrowWhenAmountIsNull() {
            assertThrows(InvalidAmountException.class, () ->
                    new Expense(null, TEST_DATE, "Groceries", Category.FOOD)
            );
        }

        @Test
        @DisplayName("Should throw InvalidAmountException when transaction amount is zero or negative")
        void shouldThrowWhenAmountIsNonPositive() {
            assertThrows(InvalidAmountException.class, () ->
                    new Expense(Money.usd(0.00), TEST_DATE, "Free item", Category.FOOD)
            );
            assertThrows(InvalidAmountException.class, () ->
                    new Expense(Money.usd(-15.00), TEST_DATE, "Negative expense", Category.SHOPPING)
            );
            assertThrows(InvalidAmountException.class, () ->
                    new Income(Money.usd(0.00), TEST_DATE, "Zero salary", Category.SALARY)
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when date or category is null")
        void shouldThrowWhenRequiredFieldsAreNull() {
            assertThrows(NullPointerException.class, () ->
                    new Expense(Money.usd(25.00), null, "Lunch", Category.FOOD)
            );
            assertThrows(NullPointerException.class, () ->
                    new Expense(Money.usd(25.00), TEST_DATE, "Lunch", null)
            );
        }

        @Test
        @DisplayName("RecurringExpense should throw NullPointerException when frequency is null")
        void shouldThrowWhenRecurringFrequencyIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new RecurringExpense(Money.usd(1200.00), TEST_DATE, "Apartment", Category.RENT, null)
            );
        }
    }

    @Nested
    @DisplayName("Polymorphic getSignedAmount() Behavior")
    class PolymorphicSignedAmountTests {

        @Test
        @DisplayName("Expense.getSignedAmount() must return a negative Money amount")
        void expenseShouldReturnNegativeSignedAmount() {
            Transaction expense = new Expense(
                    1L,
                    Money.usd(45.50),
                    TEST_DATE,
                    "Dinner with friends",
                    Category.FOOD
            );

            Money signedAmount = expense.getSignedAmount();

            assertEquals(Money.usd(-45.50), signedAmount);
            assertTrue(signedAmount.isNegative());
            assertEquals(Money.usd(45.50), expense.getAmount());
        }

        @Test
        @DisplayName("Income.getSignedAmount() must return a positive Money amount")
        void incomeShouldReturnPositiveSignedAmount() {
            Transaction income = new Income(
                    2L,
                    Money.usd(5000.00),
                    TEST_DATE,
                    "Monthly Salary",
                    Category.SALARY
            );

            Money signedAmount = income.getSignedAmount();

            assertEquals(Money.usd(5000.00), signedAmount);
            assertTrue(signedAmount.isPositive());
            assertEquals(Money.usd(5000.00), income.getAmount());
        }

        @Test
        @DisplayName("RecurringExpense.getSignedAmount() must inherit and return a negative Money amount")
        void recurringExpenseShouldReturnNegativeSignedAmount() {
            Transaction recurringExpense = new RecurringExpense(
                    3L,
                    Money.usd(14.99),
                    TEST_DATE,
                    "Streaming service",
                    Category.ENTERTAINMENT,
                    Frequency.MONTHLY
            );

            Money signedAmount = recurringExpense.getSignedAmount();

            assertEquals(Money.usd(-14.99), signedAmount);
            assertTrue(signedAmount.isNegative());
            assertEquals(Money.usd(14.99), recurringExpense.getAmount());
        }

        @Test
        @DisplayName("Polymorphic list processing computes correct net ledger balance")
        void shouldPolymorphicallyCalculateNetBalance() {
            List<Transaction> ledger = List.of(
                    new Income(Money.usd(3000.00), TEST_DATE, "Salary", Category.SALARY),
                    new Expense(Money.usd(150.00), TEST_DATE, "Groceries", Category.FOOD),
                    new Expense(Money.usd(60.00), TEST_DATE, "Gas", Category.TRANSPORT),
                    new RecurringExpense(Money.usd(1200.00), TEST_DATE, "Apartment rent", Category.RENT, Frequency.MONTHLY)
            );

            // Polymorphic dispatch to getSignedAmount()
            Money netBalance = Money.usd(0.00);
            for (Transaction tx : ledger) {
                netBalance = netBalance.add(tx.getSignedAmount());
            }

            // 3000.00 - 150.00 - 60.00 - 1200.00 = 1590.00
            assertEquals(Money.usd(1590.00), netBalance);
        }
    }

    @Nested
    @DisplayName("Polymorphic describe() Behavior")
    class DescribeMethodTests {

        @Test
        @DisplayName("Expense.describe() should include category, negative format, and note")
        void expenseShouldDescribeCorrectly() {
            Transaction expense = new Expense(
                    Money.usd(82.50),
                    TEST_DATE,
                    "Weekly groceries",
                    Category.FOOD
            );

            String description = expense.describe();

            assertTrue(description.startsWith("[EXPENSE] FOOD: -USD 82.50 on 2026-10-24"));
            assertTrue(description.contains("Weekly groceries"));
        }

        @Test
        @DisplayName("Income.describe() should include category, positive format, and note")
        void incomeShouldDescribeCorrectly() {
            Transaction income = new Income(
                    Money.usd(4200.00),
                    TEST_DATE,
                    "October Payroll",
                    Category.SALARY
            );

            String description = income.describe();

            assertTrue(description.startsWith("[INCOME] SALARY: +USD 4200.00 on 2026-10-24"));
            assertTrue(description.contains("October Payroll"));
        }

        @Test
        @DisplayName("RecurringExpense.describe() should include frequency tag")
        void recurringExpenseShouldDescribeCorrectly() {
            RecurringExpense recurring = new RecurringExpense(
                    Money.usd(1500.00),
                    TEST_DATE,
                    "Mortgage",
                    Category.RENT,
                    Frequency.MONTHLY
            );

            String description = recurring.describe();

            assertTrue(description.startsWith("[RECURRING EXPENSE - MONTHLY] RENT: -USD 1500.00 on 2026-10-24"));
            assertTrue(description.contains("Mortgage"));
        }
    }

    @Nested
    @DisplayName("Category Enum Metadata")
    class CategoryEnumTests {

        @Test
        @DisplayName("All categories must have non-empty icon and valid hex color format")
        void allCategoriesMustHaveValidMetadata() {
            for (Category category : Category.values()) {
                assertNotNull(category.getIcon(), "Icon should not be null for " + category);
                assertFalse(category.getIcon().isBlank(), "Icon should not be blank for " + category);

                assertNotNull(category.getHexColor(), "Hex color should not be null for " + category);
                assertTrue(category.getHexColor().matches("^#[0-9A-Fa-f]{6}$"),
                        "Hex color should match #RRGGBB for " + category + " but was " + category.getHexColor());
            }
        }
    }
}
