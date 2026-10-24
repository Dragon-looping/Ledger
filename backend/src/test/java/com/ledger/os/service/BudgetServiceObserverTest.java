package com.ledger.os.service;

import com.ledger.os.event.BudgetThresholdEvent;
import com.ledger.os.model.Category;
import com.ledger.os.model.Expense;
import com.ledger.os.model.Money;
import com.ledger.os.model.Wallet;
import com.ledger.os.rule.BudgetLevel;
import com.ledger.os.rule.MonthlyCapRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Observer Pattern: BudgetService & BudgetThresholdEvent Tests")
class BudgetServiceObserverTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<BudgetThresholdEvent> eventCaptor;

    private BudgetService budgetService;
    private Wallet wallet;
    private MonthlyCapRule monthlyCapRule;

    private static final Currency USD = Currency.getInstance("USD");

    @BeforeEach
    void setUp() {
        budgetService = new BudgetService(eventPublisher);
        wallet = new Wallet("Main Wallet", USD);
        monthlyCapRule = new MonthlyCapRule(Money.usd(1000.00));
        budgetService.registerRule(monthlyCapRule);
    }

    @Test
    @DisplayName("Should publish WARNING when reaching 80%, EXCEEDED when reaching 100%, and fire each level only once per month")
    void shouldPublishThresholdEventsExactlyOncePerLevelPerMonth() {
        LocalDate oct5 = LocalDate.of(2026, 10, 5);
        LocalDate oct10 = LocalDate.of(2026, 10, 10);
        LocalDate oct15 = LocalDate.of(2026, 10, 15);
        LocalDate oct20 = LocalDate.of(2026, 10, 20);
        LocalDate oct25 = LocalDate.of(2026, 10, 25);

        // Step 1: Add $500 expense (50% of cap). No threshold passed.
        Expense tx1 = new Expense(Money.usd(500.00), oct5, "Groceries", Category.FOOD);
        budgetService.processTransaction(wallet, tx1);

        verify(eventPublisher, never()).publishEvent(any(BudgetThresholdEvent.class));

        // Step 2: Add $300 expense (Total $800 = 80.0%). Hits WARNING threshold!
        Expense tx2 = new Expense(Money.usd(300.00), oct10, "Gadget", Category.SHOPPING);
        budgetService.processTransaction(wallet, tx2);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        BudgetThresholdEvent warningEvent = eventCaptor.getValue();
        assertEquals(BudgetLevel.WARNING, warningEvent.getLevel());
        assertEquals(80.0, warningEvent.getStatus().percentUsed());
        assertEquals(YearMonth.of(2026, 10), warningEvent.getPeriod());
        assertEquals(tx2, warningEvent.getTriggeringTransaction());

        // Step 3: Add $50 expense (Total $850 = 85.0%). Still in WARNING, must NOT re-fire!
        reset(eventPublisher);
        Expense tx3 = new Expense(Money.usd(50.00), oct15, "Gas", Category.TRANSPORT);
        budgetService.processTransaction(wallet, tx3);

        verify(eventPublisher, never()).publishEvent(any(BudgetThresholdEvent.class));

        // Step 4: Add $200 expense (Total $1050 = 105.0%). Hits EXCEEDED threshold!
        Expense tx4 = new Expense(Money.usd(200.00), oct20, "Dinner", Category.FOOD);
        budgetService.processTransaction(wallet, tx4);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        BudgetThresholdEvent exceededEvent = eventCaptor.getValue();
        assertEquals(BudgetLevel.EXCEEDED, exceededEvent.getLevel());
        assertEquals(105.0, exceededEvent.getStatus().percentUsed());
        assertEquals(tx4, exceededEvent.getTriggeringTransaction());

        // Step 5: Add $100 expense (Total $1150 = 115.0%). Still EXCEEDED, must NOT re-fire!
        reset(eventPublisher);
        Expense tx5 = new Expense(Money.usd(100.00), oct25, "Shoes", Category.SHOPPING);
        budgetService.processTransaction(wallet, tx5);

        verify(eventPublisher, never()).publishEvent(any(BudgetThresholdEvent.class));

        // Step 6: Advance to next month (2026-11). Add $820 expense (82% of Nov cap).
        // Must fire WARNING for the new month!
        LocalDate nov2 = LocalDate.of(2026, 11, 2);
        Expense novTx = new Expense(Money.usd(820.00), nov2, "Nov Rent share", Category.RENT);
        budgetService.processTransaction(wallet, novTx);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        BudgetThresholdEvent novEvent = eventCaptor.getValue();
        assertEquals(BudgetLevel.WARNING, novEvent.getLevel());
        assertEquals(YearMonth.of(2026, 11), novEvent.getPeriod());
        assertEquals(82.0, novEvent.getStatus().percentUsed());
    }

    @Test
    @DisplayName("Direct jump past 100% in a single transaction publishes WARNING and EXCEEDED events")
    void shouldPublishBothWhenJumpingDirectlyPast100Percent() {
        // Single transaction jumping directly from 0% to 125%
        Expense largeExpense = new Expense(
                Money.usd(1250.00),
                LocalDate.of(2026, 10, 5),
                "Emergency laptop repair",
                Category.BILLS
        );

        budgetService.processTransaction(wallet, largeExpense);

        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());
        List<BudgetThresholdEvent> events = eventCaptor.getAllValues();

        assertEquals(2, events.size());
        assertEquals(BudgetLevel.WARNING, events.get(0).getLevel());
        assertEquals(BudgetLevel.EXCEEDED, events.get(1).getLevel());
        assertEquals(125.0, events.get(0).getStatus().percentUsed());
        assertEquals(125.0, events.get(1).getStatus().percentUsed());
    }

    @Test
    @DisplayName("BudgetThresholdEvent contains all necessary event metadata")
    void shouldContainCompleteEventMetadata() {
        Expense expense = new Expense(
                Money.usd(900.00),
                LocalDate.of(2026, 10, 1),
                "Health insurance",
                Category.HEALTH
        );

        budgetService.processTransaction(wallet, expense);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        BudgetThresholdEvent event = eventCaptor.getValue();

        assertSame(wallet, event.getWallet());
        assertSame(monthlyCapRule, event.getRule());
        assertEquals(YearMonth.of(2026, 10), event.getPeriod());
        assertEquals(BudgetLevel.WARNING, event.getLevel());
        assertEquals(Money.usd(1000.00), event.getStatus().limit());
        assertEquals(Money.usd(900.00), event.getStatus().spent());
        assertEquals(90.0, event.getStatus().percentUsed());
        assertSame(expense, event.getTriggeringTransaction());
        assertNotNull(event.toString());
    }
}
