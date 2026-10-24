package com.ledger.os.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Nested
    @DisplayName("Constructor & Factory Validation")
    class ConstructorValidationTests {

        @Test
        @DisplayName("Should create Money with normalized 2 decimal places")
        void shouldCreateMoneyWithNormalizedScale() {
            Money money = Money.of(new BigDecimal("19.999"), USD);
            assertEquals(new BigDecimal("20.00"), money.getAmount());
            assertEquals("USD", money.getCurrencyCode());
            assertEquals(USD, money.getCurrency());
        }

        @Test
        @DisplayName("Should throw InvalidAmountException when amount is null")
        void shouldThrowWhenAmountIsNull() {
            assertThrows(InvalidAmountException.class, () -> new Money(null, USD));
            assertThrows(InvalidAmountException.class, () -> Money.of((BigDecimal) null, "USD"));
        }

        @Test
        @DisplayName("Should throw InvalidAmountException when currency is null or blank")
        void shouldThrowWhenCurrencyIsNull() {
            assertThrows(InvalidAmountException.class, () -> new Money(BigDecimal.TEN, (Currency) null));
            assertThrows(InvalidAmountException.class, () -> Money.of(BigDecimal.TEN, (String) null));
            assertThrows(InvalidAmountException.class, () -> Money.of(BigDecimal.TEN, "   "));
        }

        @Test
        @DisplayName("Should throw InvalidAmountException for invalid ISO currency string")
        void shouldThrowForInvalidCurrencyCode() {
            assertThrows(InvalidAmountException.class, () -> Money.of(BigDecimal.TEN, "INVALID_CODE"));
        }

        @Test
        @DisplayName("Should throw InvalidAmountException for malformed numeric string")
        void shouldThrowForMalformedNumericString() {
            assertThrows(InvalidAmountException.class, () -> Money.of("not-a-number", "USD"));
            assertThrows(InvalidAmountException.class, () -> Money.of("", "USD"));
        }
    }

    @Nested
    @DisplayName("Arithmetic Operations & Immutability")
    class ArithmeticTests {

        @Test
        @DisplayName("Should add amounts with same currency immutably")
        void shouldAddAmountsCorrectly() {
            Money a = Money.usd(100.50);
            Money b = Money.usd(49.50);

            Money result = a.add(b);

            assertEquals(Money.usd(150.00), result);
            // Verify immutability of inputs
            assertEquals(Money.usd(100.50), a);
            assertEquals(Money.usd(49.50), b);
        }

        @Test
        @DisplayName("Should subtract amounts with same currency immutably")
        void shouldSubtractAmountsCorrectly() {
            Money a = Money.usd(100.00);
            Money b = Money.usd(35.25);

            Money result = a.subtract(b);

            assertEquals(Money.usd(64.75), result);
            assertEquals(Money.usd(100.00), a);
        }

        @Test
        @DisplayName("Should support subtraction resulting in negative Money")
        void shouldSupportNegativeResult() {
            Money a = Money.usd(50.00);
            Money b = Money.usd(80.00);

            Money result = a.subtract(b);

            assertEquals(Money.usd(-30.00), result);
            assertTrue(result.isNegative());
            assertFalse(result.isPositive());
            assertFalse(result.isZero());
        }

        @Test
        @DisplayName("Should negate and abs money correctly")
        void shouldNegateAndAbs() {
            Money pos = Money.usd(75.50);
            Money neg = pos.negate();

            assertEquals(Money.usd(-75.50), neg);
            assertTrue(neg.isNegative());
            assertEquals(pos, neg.abs());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when adding different currencies")
        void shouldThrowOnCurrencyMismatchDuringAdd() {
            Money usd = Money.usd(100.00);
            Money eur = Money.of(100.00, EUR);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> usd.add(eur));
            assertTrue(ex.getMessage().contains("Currency mismatch"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when subtracting different currencies")
        void shouldThrowOnCurrencyMismatchDuringSubtract() {
            Money usd = Money.usd(100.00);
            Money eur = Money.of(50.00, EUR);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> usd.subtract(eur));
            assertTrue(ex.getMessage().contains("Currency mismatch"));
        }
    }

    @Nested
    @DisplayName("Comparisons, Equality & HashCode")
    class ComparisonAndEqualityTests {

        @Test
        @DisplayName("Should correctly compare Money objects")
        void shouldCompareMoneyObjects() {
            Money small = Money.usd(25.00);
            Money medium = Money.usd(50.00);
            Money large = Money.usd(100.00);

            assertTrue(small.compareTo(medium) < 0);
            assertTrue(large.compareTo(medium) > 0);
            assertEquals(0, medium.compareTo(Money.usd(50.00)));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException on compareTo with different currencies or null")
        void shouldThrowOnCompareMismatch() {
            Money usd = Money.usd(50.00);
            Money eur = Money.of(50.00, EUR);

            assertThrows(IllegalArgumentException.class, () -> usd.compareTo(eur));
            assertThrows(IllegalArgumentException.class, () -> usd.compareTo(null));
        }

        @Test
        @DisplayName("Should satisfy equals and hashCode contract regardless of input scale representation")
        void shouldSatisfyEqualsAndHashCode() {
            Money m1 = Money.of(new BigDecimal("42.5"), "USD");
            Money m2 = Money.of(new BigDecimal("42.50"), "USD");
            Money m3 = Money.of(new BigDecimal("42.500"), "USD");
            Money diffAmount = Money.usd(42.51);
            Money diffCurrency = Money.of(42.50, EUR);

            assertEquals(m1, m2);
            assertEquals(m2, m3);
            assertEquals(m1.hashCode(), m2.hashCode());
            assertEquals(m2.hashCode(), m3.hashCode());

            assertNotEquals(m1, diffAmount);
            assertNotEquals(m1, diffCurrency);
            assertNotEquals(m1, null);
            assertNotEquals(m1, "not-a-money-object");
        }

        @Test
        @DisplayName("Should format string representation cleanly")
        void shouldFormatToString() {
            Money money = Money.usd(1234.56);
            assertEquals("USD 1234.56", money.toString());
        }
    }
}
