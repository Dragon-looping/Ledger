package com.ledger.os.parser;

import com.ledger.os.factory.TransactionType;
import com.ledger.os.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionTextParser Tests")
class TransactionTextParserTest {

    private TransactionTextParser parser;

    @BeforeEach
    void setUp() {
        parser = new TransactionTextParser();
    }

    @Test
    @DisplayName("Should parse 'chai 40 yesterday' into FOOD expense on yesterday's date")
    void shouldParseChaiYesterday() {
        var result = parser.parse("chai 40 yesterday");

        assertEquals(new BigDecimal("40.00"), result.amount().getAmount());
        assertEquals("INR", result.amount().getCurrencyCode());
        assertEquals(Category.FOOD, result.category());
        assertEquals(TransactionType.EXPENSE, result.type());
        assertEquals(LocalDate.now().minusDays(1), result.date());
        assertTrue(result.note().toLowerCase().contains("chai"));
    }

    @Test
    @DisplayName("Should parse 'rent 12000 on 1st' into RENT expense on the 1st of this month")
    void shouldParseRentOnFirst() {
        var result = parser.parse("rent 12000 on 1st");

        assertEquals(new BigDecimal("12000.00"), result.amount().getAmount());
        assertEquals("INR", result.amount().getCurrencyCode());
        assertEquals(Category.RENT, result.category());
        assertEquals(TransactionType.EXPENSE, result.type());

        LocalDate expected = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1);
        assertEquals(expected, result.date());
    }

    @Test
    @DisplayName("Should recognize Hindi/Hinglish keywords: auto, kirana, bijli, tankhwah/salary")
    void shouldRecognizeHinglishKeywords() {
        // Auto -> TRANSPORT
        var autoResult = parser.parse("auto 150 today");
        assertEquals(Category.TRANSPORT, autoResult.category());
        assertEquals(new BigDecimal("150.00"), autoResult.amount().getAmount());
        assertEquals(LocalDate.now(), autoResult.date());

        // Kirana -> SHOPPING
        var kiranaResult = parser.parse("kirana 2450 parso");
        assertEquals(Category.SHOPPING, kiranaResult.category());
        assertEquals(new BigDecimal("2450.00"), kiranaResult.amount().getAmount());
        assertEquals(LocalDate.now().minusDays(2), kiranaResult.date());

        // Bijli -> BILLS
        var bijliResult = parser.parse("bijli bill 1850");
        assertEquals(Category.BILLS, bijliResult.category());
        assertEquals(new BigDecimal("1850.00"), bijliResult.amount().getAmount());

        // Salary -> SALARY (INCOME)
        var salaryResult = parser.parse("salary 95000 on 1st");
        assertEquals(Category.SALARY, salaryResult.category());
        assertEquals(TransactionType.INCOME, salaryResult.type());
        assertEquals(new BigDecimal("95000.00"), salaryResult.amount().getAmount());
    }

    @Test
    @DisplayName("Should support 'k' suffix for thousands (e.g. '25k')")
    void shouldSupportKSuffix() {
        var result = parser.parse("flat rent 25k on 2nd");
        assertEquals(new BigDecimal("25000.00"), result.amount().getAmount());
        assertEquals(Category.RENT, result.category());
    }

    @Test
    @DisplayName("Should resolve 'N days ago'")
    void shouldResolveDaysAgo() {
        var result = parser.parse("dinner with friends 1200 3 days ago");
        assertEquals(new BigDecimal("1200.00"), result.amount().getAmount());
        assertEquals(Category.FOOD, result.category());
        assertEquals(LocalDate.now().minusDays(3), result.date());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when input is blank or has no positive amount")
    void shouldThrowOnInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(""));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("   "));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("just some text without numbers"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("coffee 0 today"));
    }
}
