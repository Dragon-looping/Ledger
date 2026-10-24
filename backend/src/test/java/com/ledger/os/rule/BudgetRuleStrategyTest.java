package com.ledger.os.rule;

import com.ledger.os.model.Category;
import com.ledger.os.model.Expense;
import com.ledger.os.model.Income;
import com.ledger.os.model.Money;
import com.ledger.os.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Strategy Pattern: BudgetRule Implementations")
class BudgetRuleStrategyTest {

    private Wallet wallet;
    private final YearMonth currentMonth = YearMonth.of(2026, 10);
    private static final Currency USD = Currency.getInstance("USD");

    @BeforeEach
    void setUp() {
        wallet = new Wallet("Checking", USD);
    }

    @Nested
    @DisplayName("MonthlyCapRule Tests")
    class MonthlyCapRuleTests {

        @Test
        @DisplayName("Should evaluate to OK when spending is below 80%")
        void shouldEvaluateToOkBelow80Percent() {
            MonthlyCapRule rule = new MonthlyCapRule(Money.usd(1000.00));
            // Spend $500 (50.0%)
            wallet.addTransaction(new Expense(Money.usd(500.00), LocalDate.of(2026, 10, 5), "Bills", Category.BILLS));

            BudgetStatus status = rule.evaluate(wallet, currentMonth);

            assertEquals(BudgetLevel.OK, status.level());
            assertEquals(50.0, status.percentUsed());
            assertEquals(Money.usd(1000.00), status.limit());
            assertEquals(Money.usd(500.00), status.spent());
        }

        @Test
        @DisplayName("Should evaluate to WARNING when spending is between 80% and 99.9%")
        void shouldEvaluateToWarningAt80Percent() {
            MonthlyCapRule rule = new MonthlyCapRule(Money.usd(1000.00));
            // Spend $800 (exactly 80.0%)
            wallet.addTransaction(new Expense(Money.usd(800.00), LocalDate.of(2026, 10, 10), "Equipment", Category.SHOPPING));

            BudgetStatus status = rule.evaluate(wallet, currentMonth);

            assertEquals(BudgetLevel.WARNING, status.level());
            assertEquals(80.0, status.percentUsed());
        }

        @Test
        @DisplayName("Should evaluate to EXCEEDED when spending reaches or exceeds 100%")
        void shouldEvaluateToExceededAt100PercentOrMore() {
            MonthlyCapRule rule = new MonthlyCapRule(Money.usd(1000.00));
            // Spend $1050 (105.0%)
            wallet.addTransaction(new Expense(Money.usd(1050.00), LocalDate.of(2026, 10, 15), "Rent", Category.RENT));

            BudgetStatus status = rule.evaluate(wallet, currentMonth);

            assertEquals(BudgetLevel.EXCEEDED, status.level());
            assertEquals(105.0, status.percentUsed());
        }

        @Test
        @DisplayName("Should evaluate to 0% and OK when there is zero spending")
        void shouldEvaluateZeroSpending() {
            MonthlyCapRule rule = new MonthlyCapRule(Money.usd(500.00));
            BudgetStatus status = rule.evaluate(wallet, currentMonth);

            assertEquals(BudgetLevel.OK, status.level());
            assertEquals(0.0, status.percentUsed());
            assertEquals(Money.usd(0.00), status.spent());
        }

        @Test
        @DisplayName("Should reject null or non-positive cap in constructor")
        void shouldValidateMonthlyCap() {
            assertThrows(IllegalArgumentException.class, () -> new MonthlyCapRule(null));
            assertThrows(IllegalArgumentException.class, () -> new MonthlyCapRule(Money.usd(0.00)));
            assertThrows(IllegalArgumentException.class, () -> new MonthlyCapRule(Money.usd(-100.00)));
        }
    }

    @Nested
    @DisplayName("CategoryCapRule Tests")
    class CategoryCapRuleTests {

        @Test
        @DisplayName("Should evaluate spending exclusively for the specified Category")
        void shouldIsolateCategorySpending() {
            CategoryCapRule foodCap = new CategoryCapRule(Category.FOOD, Money.usd(400.00));

            // $320 on FOOD (80%)
            wallet.addTransaction(new Expense(Money.usd(320.00), LocalDate.of(2026, 10, 2), "Groceries", Category.FOOD));
            // $1500 on RENT (different category, should be ignored by food rule)
            wallet.addTransaction(new Expense(Money.usd(1500.00), LocalDate.of(2026, 10, 1), "Apartment", Category.RENT));

            BudgetStatus status = foodCap.evaluate(wallet, currentMonth);

            assertEquals(Category.FOOD, foodCap.getCategory());
            assertEquals(BudgetLevel.WARNING, status.level());
            assertEquals(80.0, status.percentUsed());
            assertEquals(Money.usd(320.00), status.spent());
            assertEquals(Money.usd(400.00), status.limit());
        }

        @Test
        @DisplayName("Should evaluate to OK with 0% when target category has no expenses")
        void shouldHandleNoCategoryExpenses() {
            CategoryCapRule entertainmentCap = new CategoryCapRule(Category.ENTERTAINMENT, Money.usd(200.00));
            wallet.addTransaction(new Expense(Money.usd(100.00), LocalDate.of(2026, 10, 5), "Gas", Category.TRANSPORT));

            BudgetStatus status = entertainmentCap.evaluate(wallet, currentMonth);

            assertEquals(BudgetLevel.OK, status.level());
            assertEquals(0.0, status.percentUsed());
            assertEquals(Money.usd(0.00), status.spent());
        }

        @Test
        @DisplayName("Should reject null category or non-positive cap in constructor")
        void shouldValidateCategoryCap() {
            assertThrows(NullPointerException.class, () -> new CategoryCapRule(null, Money.usd(100.00)));
            assertThrows(IllegalArgumentException.class, () -> new CategoryCapRule(Category.FOOD, null));
            assertThrows(IllegalArgumentException.class, () -> new CategoryCapRule(Category.FOOD, Money.usd(0.00)));
        }
    }

    @Nested
    @DisplayName("SavingsGoalRule Tests")
    class SavingsGoalRuleTests {

        @Test
        @DisplayName("Should calculate allowable spending as (income - savingsGoal)")
        void shouldCalculateAllowableSpendingFromIncome() {
            // Target savings: $1,000. Income: $5,000 -> Allowable spending limit = $4,000.
            SavingsGoalRule rule = new SavingsGoalRule(Money.usd(1000.00));

            wallet.addTransaction(new Income(Money.usd(5000.00), LocalDate.of(2026, 10, 1), "Salary", Category.SALARY));

            // Case 1: Spend $2000 of $4000 limit -> 50% -> OK
            wallet.addTransaction(new Expense(Money.usd(2000.00), LocalDate.of(2026, 10, 5), "Lifestyle", Category.SHOPPING));
            BudgetStatus status1 = rule.evaluate(wallet, currentMonth);
            assertEquals(BudgetLevel.OK, status1.level());
            assertEquals(50.0, status1.percentUsed());
            assertEquals(Money.usd(4000.00), status1.limit());

            // Case 2: Spend another $1200 -> Total $3200 of $4000 limit -> 80% -> WARNING
            wallet.addTransaction(new Expense(Money.usd(1200.00), LocalDate.of(2026, 10, 15), "Rent", Category.RENT));
            BudgetStatus status2 = rule.evaluate(wallet, currentMonth);
            assertEquals(BudgetLevel.WARNING, status2.level());
            assertEquals(80.0, status2.percentUsed());

            // Case 3: Spend another $900 -> Total $4100 of $4000 limit -> 102.5% -> EXCEEDED
            wallet.addTransaction(new Expense(Money.usd(900.00), LocalDate.of(2026, 10, 20), "Electronics", Category.SHOPPING));
            BudgetStatus status3 = rule.evaluate(wallet, currentMonth);
            assertEquals(BudgetLevel.EXCEEDED, status3.level());
            assertEquals(102.5, status3.percentUsed());
        }

        @Test
        @DisplayName("Should support explicit expected income baseline")
        void shouldSupportExpectedIncome() {
            // Target savings $1,500 with fixed expected income $4,500 -> Allowable limit = $3,000.
            SavingsGoalRule rule = new SavingsGoalRule(Money.usd(1500.00), Money.usd(4500.00));

            wallet.addTransaction(new Expense(Money.usd(2400.00), LocalDate.of(2026, 10, 8), "Misc", Category.OTHER));

            BudgetStatus status = rule.evaluate(wallet, currentMonth);

            assertEquals(Money.usd(3000.00), status.limit());
            assertEquals(Money.usd(2400.00), status.spent());
            assertEquals(80.0, status.percentUsed());
            assertEquals(BudgetLevel.WARNING, status.level());
        }

        @Test
        @DisplayName("Should mark EXCEEDED immediately if spending occurs when income is below savings goal")
        void shouldHandleZeroOrNegativeAllowance() {
            SavingsGoalRule rule = new SavingsGoalRule(Money.usd(2000.00));
            // Income is only $1000, savings target is $2000 -> allowance is $0.00
            wallet.addTransaction(new Income(Money.usd(1000.00), LocalDate.of(2026, 10, 1), "Part-time", Category.SALARY));
            wallet.addTransaction(new Expense(Money.usd(50.00), LocalDate.of(2026, 10, 2), "Coffee", Category.FOOD));

            BudgetStatus status = rule.evaluate(wallet, currentMonth);

            assertEquals(BudgetLevel.EXCEEDED, status.level());
            assertEquals(100.0, status.percentUsed());
        }
    }
}
