package com.ledger.os.config;

import com.ledger.os.factory.TransactionFactory;
import com.ledger.os.factory.TransactionRequest;
import com.ledger.os.factory.TransactionType;
import com.ledger.os.model.Budget;
import com.ledger.os.model.Category;
import com.ledger.os.model.Frequency;
import com.ledger.os.model.Money;
import com.ledger.os.model.Transaction;
import com.ledger.os.model.Wallet;
import com.ledger.os.repository.BudgetRepository;
import com.ledger.os.repository.TransactionRepository;
import com.ledger.os.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;

/**
 * Data seeder triggered only when the "demo" profile is active.
 * Populates 3 months of realistic Indian transactions and budgets.
 */
@Component
@Profile("demo")
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private static final Currency INR = Currency.getInstance("INR");

    private final TransactionRepository transactionRepo;
    private final BudgetRepository budgetRepo;
    private final WalletRepository walletRepo;

    public DemoDataInitializer(
            TransactionRepository transactionRepo,
            BudgetRepository budgetRepo,
            WalletRepository walletRepo
    ) {
        this.transactionRepo = transactionRepo;
        this.budgetRepo = budgetRepo;
        this.walletRepo = walletRepo;
    }

    @Override
    public void run(String... args) {
        log.info("--- Initializing Demo Profile: Seeding 3 months of Ledger OS data ---");

        // Clear existing demo data
        for (Transaction t : transactionRepo.findAll()) {
            transactionRepo.deleteById(t.getId());
        }
        for (Budget b : budgetRepo.findAll()) {
            budgetRepo.deleteById(b.getId());
        }

        Wallet wallet = walletRepo.findFirst().orElseGet(() -> {
            Wallet w = new Wallet("Demo Personal Ledger", INR);
            return walletRepo.save(w);
        });

        LocalDate today = LocalDate.now();
        YearMonth m0 = YearMonth.from(today);               // Current month (e.g. 2026-10)
        YearMonth m1 = m0.minusMonths(1);                   // Previous month (e.g. 2026-09)
        YearMonth m2 = m0.minusMonths(2);                   // 2 months ago (e.g. 2026-08)

        // Seed Budgets for all 3 months
        seedBudgetsForMonth(m2);
        seedBudgetsForMonth(m1);
        seedBudgetsForMonth(m0);

        // Seed 3 months of transactions
        seedMonthTransactions(m2, false);
        seedMonthTransactions(m1, false);
        seedMonthTransactions(m0, true);

        log.info("--- Demo Data Seeded Successfully: {} total transactions, {} budgets ---",
                transactionRepo.count(), budgetRepo.count());
    }

    private void seedBudgetsForMonth(YearMonth month) {
        List<Budget> budgets = List.of(
                new Budget(Category.RENT, Money.of(new BigDecimal("30000.00"), INR), month),
                new Budget(Category.FOOD, Money.of(new BigDecimal("15000.00"), INR), month),
                new Budget(Category.SHOPPING, Money.of(new BigDecimal("12000.00"), INR), month),
                new Budget(Category.BILLS, Money.of(new BigDecimal("8000.00"), INR), month),
                new Budget(Category.TRANSPORT, Money.of(new BigDecimal("6000.00"), INR), month),
                new Budget(Category.ENTERTAINMENT, Money.of(new BigDecimal("5000.00"), INR), month)
        );
        for (Budget b : budgets) {
            budgetRepo.save(b);
        }
    }

    private void seedMonthTransactions(YearMonth month, boolean isCurrentMonth) {
        int year = month.getYear();
        int m = month.getMonthValue();

        // 1. Primary Monthly Salary (Income)
        createTx(TransactionType.INCOME, "135000.00", LocalDate.of(year, m, 1), "Tech Lead Salary (Direct Deposit)", Category.SALARY, null);

        // 2. Freelance UI consulting
        createTx(TransactionType.INCOME, "25000.00", LocalDate.of(year, m, 18), "Mobile App Design Consultation", Category.SALARY, null);

        // 3. Apartment Rent (Recurring Expense)
        createTx(TransactionType.RECURRING_EXPENSE, "28000.00", LocalDate.of(year, m, 2), "2BHK Flat Rent & Maintenance", Category.RENT, Frequency.MONTHLY);

        // 4. Utilities & Bills
        createTx(TransactionType.EXPENSE, "2850.00", LocalDate.of(year, m, 5), "Bijli Bill (Electricity board)", Category.BILLS, null);
        createTx(TransactionType.EXPENSE, "1199.00", LocalDate.of(year, m, 6), "Airtel Fiber Gigabit Wifi", Category.BILLS, null);
        createTx(TransactionType.EXPENSE, "950.00", LocalDate.of(year, m, 12), "Indane Gas Cylinder refill", Category.BILLS, null);
        createTx(TransactionType.EXPENSE, "719.00", LocalDate.of(year, m, 20), "Jio Mobile 5G quarterly recharge", Category.BILLS, null);

        // 5. Groceries & Kirana
        createTx(TransactionType.EXPENSE, "2850.00", LocalDate.of(year, m, 3), "Monthly Kirana staples (Atta, Dal, Oil)", Category.SHOPPING, null);
        createTx(TransactionType.EXPENSE, "1420.00", LocalDate.of(year, m, 9), "Zepto fresh veggies & dairy", Category.SHOPPING, null);
        createTx(TransactionType.EXPENSE, "1890.00", LocalDate.of(year, m, 16), "Blinkit weekend snacks & fruits", Category.SHOPPING, null);
        createTx(TransactionType.EXPENSE, "3200.00", LocalDate.of(year, m, 22), "Instamart household cleaning & laundry", Category.SHOPPING, null);

        // 6. Food & Dining
        createTx(TransactionType.EXPENSE, "40.00", LocalDate.of(year, m, 4), "Chai tapri & bun maska", Category.FOOD, null);
        createTx(TransactionType.EXPENSE, "120.00", LocalDate.of(year, m, 7), "Irani chai & samosa nashta", Category.FOOD, null);
        createTx(TransactionType.EXPENSE, "680.00", LocalDate.of(year, m, 8), "Swiggy lunch bowl delivery", Category.FOOD, null);
        createTx(TransactionType.EXPENSE, "1450.00", LocalDate.of(year, m, 13), "Zomato Hyderabadi Dum Biryani party", Category.FOOD, null);
        createTx(TransactionType.EXPENSE, "60.00", LocalDate.of(year, m, 17), "Evening cutting chai & biscuits", Category.FOOD, null);
        createTx(TransactionType.EXPENSE, "2100.00", LocalDate.of(year, m, 21), "Family weekend dinner at Sagar Ratna", Category.FOOD, null);

        // 7. Transport
        createTx(TransactionType.EXPENSE, "150.00", LocalDate.of(year, m, 4), "Auto to Metro station", Category.TRANSPORT, null);
        createTx(TransactionType.EXPENSE, "80.00", LocalDate.of(year, m, 7), "Metro smart card auto-topup", Category.TRANSPORT, null);
        createTx(TransactionType.EXPENSE, "1800.00", LocalDate.of(year, m, 11), "HPCL Petrol station full tank", Category.TRANSPORT, null);
        createTx(TransactionType.EXPENSE, "420.00", LocalDate.of(year, m, 15), "Uber Premier ride to client office", Category.TRANSPORT, null);
        createTx(TransactionType.EXPENSE, "95.00", LocalDate.of(year, m, 19), "Rapido bike taxi", Category.TRANSPORT, null);

        // 8. Health & Fitness
        createTx(TransactionType.EXPENSE, "2500.00", LocalDate.of(year, m, 5), "Cult.fit Elite Monthly Gym pass", Category.HEALTH, null);
        createTx(TransactionType.EXPENSE, "640.00", LocalDate.of(year, m, 14), "Apollo Pharmacy multivitamins & omega-3", Category.HEALTH, null);

        // 9. Entertainment & Subscriptions
        createTx(TransactionType.EXPENSE, "649.00", LocalDate.of(year, m, 6), "Netflix 4K Ultra Premium", Category.ENTERTAINMENT, null);
        createTx(TransactionType.EXPENSE, "119.00", LocalDate.of(year, m, 6), "Spotify Premium Individual", Category.ENTERTAINMENT, null);
        createTx(TransactionType.EXPENSE, "1100.00", LocalDate.of(year, m, 18), "PVR IMAX movie tickets & popcorn", Category.ENTERTAINMENT, null);

        // 10. Occasional Shopping
        createTx(TransactionType.EXPENSE, "2899.00", LocalDate.of(year, m, 24), "Myntra casual sneaker sale", Category.SHOPPING, null);

        if (isCurrentMonth) {
            // Add a few more recent micro-transactions for the current month
            createTx(TransactionType.EXPENSE, "80.00", LocalDate.now(), "Morning kulhad chai & rusk", Category.FOOD, null);
            createTx(TransactionType.EXPENSE, "220.00", LocalDate.now(), "Auto ride from tech park", Category.TRANSPORT, null);
        }
    }

    private void createTx(
            TransactionType type,
            String amount,
            LocalDate date,
            String note,
            Category category,
            Frequency frequency
    ) {
        TransactionRequest request = new TransactionRequest(
                type,
                Money.of(new BigDecimal(amount), INR),
                date,
                note,
                category,
                frequency
        );
        Transaction tx = TransactionFactory.create(request);
        transactionRepo.save(tx);
    }
}
