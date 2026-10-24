package com.ledger.os.service;

import com.ledger.os.dto.BudgetDto;
import com.ledger.os.dto.BudgetStatusDto;
import com.ledger.os.dto.CategoryBreakdownDto;
import com.ledger.os.dto.CreateTransactionRequestDto;
import com.ledger.os.dto.HeatmapItemDto;
import com.ledger.os.dto.MonthlySummaryDto;
import com.ledger.os.dto.ParseResponseDto;
import com.ledger.os.dto.TransactionResponseDto;
import com.ledger.os.factory.TransactionFactory;
import com.ledger.os.factory.TransactionRequest;
import com.ledger.os.factory.TransactionType;
import com.ledger.os.model.Budget;
import com.ledger.os.model.Category;
import com.ledger.os.model.Expense;
import com.ledger.os.model.Income;
import com.ledger.os.model.Money;
import com.ledger.os.model.Transaction;
import com.ledger.os.model.Wallet;
import com.ledger.os.parser.TransactionTextParser;
import com.ledger.os.repository.BudgetRepository;
import com.ledger.os.repository.TransactionRepository;
import com.ledger.os.repository.WalletRepository;
import com.ledger.os.rule.BudgetRule;
import com.ledger.os.rule.BudgetStatus;
import com.ledger.os.rule.CategoryCapRule;
import com.ledger.os.rule.MonthlyCapRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Currency;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class LedgerService {

    private final TransactionRepository transactionRepo;
    private final BudgetRepository budgetRepo;
    private final WalletRepository walletRepo;
    private final BudgetService budgetService;
    private final TransactionTextParser textParser;

    private final Currency defaultCurrency;

    public LedgerService(
            TransactionRepository transactionRepo,
            BudgetRepository budgetRepo,
            WalletRepository walletRepo,
            BudgetService budgetService,
            TransactionTextParser textParser,
            @Value("${ledger.currency.default:INR}") String defaultCurrencyCode
    ) {
        this.transactionRepo = transactionRepo;
        this.budgetRepo = budgetRepo;
        this.walletRepo = walletRepo;
        this.budgetService = budgetService;
        this.textParser = textParser;
        this.defaultCurrency = Currency.getInstance(defaultCurrencyCode.trim().toUpperCase());
    }

    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    public Wallet getOrCreateDefaultWallet() {
        return walletRepo.findFirst().orElseGet(() -> {
            Wallet newWallet = new Wallet("Primary Ledger", defaultCurrency);
            return walletRepo.save(newWallet);
        });
    }

    public List<TransactionResponseDto> getTransactions(YearMonth period, Category category, TransactionType type) {
        List<Transaction> transactions;
        if (period != null) {
            transactions = transactionRepo.findByYearMonth(period);
        } else if (category != null) {
            transactions = transactionRepo.findByCategory(category);
        } else {
            transactions = transactionRepo.findAll();
        }

        return transactions.stream()
                .filter(t -> {
                    if (category != null && t.getCategory() != category) return false;
                    if (type != null) {
                        return switch (type) {
                            case INCOME -> t instanceof Income;
                            case EXPENSE -> t instanceof Expense;
                            case RECURRING_EXPENSE -> t instanceof com.ledger.os.model.RecurringExpense;
                        };
                    }
                    return true;
                })
                .map(TransactionResponseDto::fromEntity)
                .toList();
    }

    public TransactionResponseDto getTransactionById(Long id) {
        Transaction tx = transactionRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found with id: " + id));
        return TransactionResponseDto.fromEntity(tx);
    }

    public TransactionResponseDto createTransaction(CreateTransactionRequestDto dto) {
        Currency currency = dto.currency() != null && !dto.currency().isBlank()
                ? Currency.getInstance(dto.currency().trim().toUpperCase())
                : defaultCurrency;

        Money money = Money.of(dto.amount(), currency);

        TransactionRequest factoryRequest = new TransactionRequest(
                dto.type(),
                money,
                dto.date(),
                dto.note(),
                dto.category(),
                dto.frequency()
        );

        Transaction transaction = TransactionFactory.create(factoryRequest);
        Transaction saved = transactionRepo.save(transaction);

        // Evaluate against Wallet & Budget Rules to trigger threshold alerts
        Wallet wallet = getOrCreateDefaultWallet();
        List<BudgetRule> activeRules = buildActiveRulesForPeriod(YearMonth.from(dto.date()));
        budgetService.processTransaction(wallet, saved, activeRules);

        return TransactionResponseDto.fromEntity(saved);
    }

    public TransactionResponseDto updateTransaction(Long id, CreateTransactionRequestDto dto) {
        if (!transactionRepo.existsById(id)) {
            throw new NoSuchElementException("Transaction not found with id: " + id);
        }

        Currency currency = dto.currency() != null && !dto.currency().isBlank()
                ? Currency.getInstance(dto.currency().trim().toUpperCase())
                : defaultCurrency;

        Money money = Money.of(dto.amount(), currency);

        TransactionRequest factoryRequest = new TransactionRequest(
                id,
                dto.type(),
                money,
                dto.date(),
                dto.note(),
                dto.category(),
                dto.frequency()
        );

        Transaction updated = TransactionFactory.create(factoryRequest);
        Transaction saved = transactionRepo.save(updated);

        return TransactionResponseDto.fromEntity(saved);
    }

    public void deleteTransaction(Long id) {
        if (!transactionRepo.existsById(id)) {
            throw new NoSuchElementException("Transaction not found with id: " + id);
        }
        transactionRepo.deleteById(id);
    }

    public MonthlySummaryDto getMonthlySummary(YearMonth period) {
        YearMonth targetMonth = period != null ? period : YearMonth.now();
        List<Transaction> transactions = transactionRepo.findByYearMonth(targetMonth);

        Money totalIncome = Money.zero(defaultCurrency);
        Money totalSpent = Money.zero(defaultCurrency);
        Map<Category, Money> categoryExpenses = new EnumMap<>(Category.class);

        for (Transaction t : transactions) {
            if (t instanceof Income) {
                totalIncome = totalIncome.add(t.getAmount());
            } else if (t instanceof Expense) {
                totalSpent = totalSpent.add(t.getAmount());
                Category cat = t.getCategory();
                Money existing = categoryExpenses.getOrDefault(cat, Money.zero(defaultCurrency));
                categoryExpenses.put(cat, existing.add(t.getAmount()));
            }
        }

        Money netBalance = totalIncome.subtract(totalSpent);

        double savingsRate = 0.0;
        if (totalIncome.isPositive()) {
            savingsRate = netBalance.isPositive()
                    ? netBalance.getAmount()
                    .divide(totalIncome.getAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue()
                    : 0.0;
        }

        // Category breakdown
        List<CategoryBreakdownDto> breakdowns = new ArrayList<>();
        BigDecimal totalSpentVal = totalSpent.getAmount();

        for (Map.Entry<Category, Money> entry : categoryExpenses.entrySet()) {
            Category cat = entry.getKey();
            BigDecimal spentVal = entry.getValue().getAmount();
            double pct = totalSpentVal.compareTo(BigDecimal.ZERO) > 0
                    ? spentVal.divide(totalSpentVal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue()
                    : 0.0;

            breakdowns.add(new CategoryBreakdownDto(
                    cat,
                    cat.name(),
                    cat.getIcon(),
                    cat.getHexColor(),
                    spentVal,
                    pct
            ));
        }

        // Evaluate active rules for this month
        Wallet wallet = getOrCreateDefaultWallet();
        List<BudgetRule> rules = buildActiveRulesForPeriod(targetMonth);
        List<BudgetStatusDto> statusDtos = new ArrayList<>();

        for (BudgetRule rule : rules) {
            BudgetStatus status = rule.evaluate(wallet, targetMonth);
            Category ruleCat = rule instanceof CategoryCapRule ccr ? ccr.getCategory() : null;
            statusDtos.add(new BudgetStatusDto(
                    rule.getName(),
                    ruleCat,
                    status.limit().getAmount(),
                    status.spent().getAmount(),
                    status.percentUsed(),
                    status.level().name()
            ));
        }

        return new MonthlySummaryDto(
                targetMonth.toString(),
                defaultCurrency.getCurrencyCode(),
                totalIncome.getAmount(),
                totalSpent.getAmount(),
                netBalance.getAmount(),
                savingsRate,
                transactions.size(),
                breakdowns,
                statusDtos
        );
    }

    public List<HeatmapItemDto> getYearlyHeatmap(int year) {
        List<Transaction> transactions = transactionRepo.findByYear(year);

        Map<LocalDate, BigDecimal> dailyTotals = new HashMap<>();
        Map<LocalDate, Integer> dailyCounts = new HashMap<>();

        for (Transaction t : transactions) {
            if (t instanceof Expense) {
                LocalDate d = t.getDate();
                dailyTotals.put(d, dailyTotals.getOrDefault(d, BigDecimal.ZERO).add(t.getAmount().getAmount()));
                dailyCounts.put(d, dailyCounts.getOrDefault(d, 0) + 1);
            }
        }

        // Find max daily expense to normalize levels (0..4)
        BigDecimal maxDaily = dailyTotals.values().stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.valueOf(1000));

        if (maxDaily.compareTo(BigDecimal.ZERO) == 0) {
            maxDaily = BigDecimal.valueOf(1000);
        }

        List<HeatmapItemDto> result = new ArrayList<>();
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            BigDecimal amount = dailyTotals.getOrDefault(d, BigDecimal.ZERO);
            int count = dailyCounts.getOrDefault(d, 0);

            int level = 0;
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                double ratio = amount.divide(maxDaily, 4, RoundingMode.HALF_UP).doubleValue();
                if (ratio >= 0.75) level = 4;
                else if (ratio >= 0.50) level = 3;
                else if (ratio >= 0.25) level = 2;
                else level = 1;
            }

            result.add(new HeatmapItemDto(d.toString(), amount, count, level));
        }

        return result;
    }

    public List<BudgetDto> getBudgets(YearMonth period) {
        YearMonth target = period != null ? period : YearMonth.now();
        return budgetRepo.findByPeriod(target).stream()
                .map(BudgetDto::fromEntity)
                .toList();
    }

    public BudgetDto setBudget(BudgetDto dto) {
        Objects.requireNonNull(dto.category(), "Category cannot be null");
        Objects.requireNonNull(dto.amount(), "Budget amount cannot be null");

        YearMonth period = dto.month() != null && !dto.month().isBlank()
                ? YearMonth.parse(dto.month().trim())
                : YearMonth.now();

        Currency currency = dto.currency() != null && !dto.currency().isBlank()
                ? Currency.getInstance(dto.currency().trim().toUpperCase())
                : defaultCurrency;

        Money limit = Money.of(dto.amount(), currency);

        // Check if budget exists for this category and month
        Optional<Budget> existing = budgetRepo.findByCategoryAndPeriod(dto.category(), period);

        Budget toSave = existing
                .map(b -> new Budget(b.getId(), dto.category(), limit, period))
                .orElseGet(() -> new Budget(dto.category(), limit, period));

        Budget saved = budgetRepo.save(toSave);
        return BudgetDto.fromEntity(saved);
    }

    public ParseResponseDto parseNaturalText(String text) {
        TransactionTextParser.ParsedTransactionResult parsed = textParser.parse(text);

        return new ParseResponseDto(
                parsed.amount().getAmount(),
                parsed.amount().getCurrencyCode(),
                parsed.date(),
                parsed.note(),
                parsed.category(),
                parsed.type(),
                parsed.category().getIcon(),
                parsed.category().getHexColor(),
                parsed.rawInput()
        );
    }

    public List<BudgetRule> buildActiveRulesForPeriod(YearMonth period) {
        List<BudgetRule> rules = new ArrayList<>();

        // Add CategoryCapRules from stored budgets
        List<Budget> budgets = budgetRepo.findByPeriod(period);
        for (Budget b : budgets) {
            rules.add(new CategoryCapRule(b.getCategory(), b.getLimit()));
        }

        // Add overall MonthlyCapRule if any budgets exist, or default monthly cap
        Money totalCap = Money.zero(defaultCurrency);
        for (Budget b : budgets) {
            totalCap = totalCap.add(b.getLimit());
        }

        if (totalCap.isPositive()) {
            rules.add(new MonthlyCapRule(totalCap));
        }

        return rules;
    }
}
