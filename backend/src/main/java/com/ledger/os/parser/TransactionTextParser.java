package com.ledger.os.parser;

import com.ledger.os.factory.TransactionType;
import com.ledger.os.model.Category;
import com.ledger.os.model.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Natural language parser converting colloquial expense strings into draft transactions.
 * Supports relative date resolution and English + Hindi/Hinglish keywords.
 */
@Component
public class TransactionTextParser {

    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("INR");

    private static final Map<String, Category> KEYWORD_MAP = new HashMap<>();

    static {
        // FOOD
        for (String kw : new String[]{
                "chai", "tea", "coffee", "samosa", "nashta", "khana", "lunch", "dinner",
                "breakfast", "biryani", "roti", "subzi", "pizza", "burger", "swiggy",
                "zomato", "restaurant", "cafe", "dhaba", "food", "maggi", "snacks", "groceries"}) {
            KEYWORD_MAP.put(kw, Category.FOOD);
        }

        // TRANSPORT
        for (String kw : new String[]{
                "auto", "ola", "uber", "metro", "bus", "train", "flight", "petrol",
                "diesel", "fuel", "cab", "taxi", "rickshaw", "toll", "parking",
                "rapido", "transport"}) {
            KEYWORD_MAP.put(kw, Category.TRANSPORT);
        }

        // RENT
        for (String kw : new String[]{
                "rent", "kiraya", "pg", "flat", "maintenance", "brokerage", "house"}) {
            KEYWORD_MAP.put(kw, Category.RENT);
        }

        // SHOPPING
        for (String kw : new String[]{
                "kirana", "zepto", "blinkit", "instamart", "amazon", "flipkart",
                "clothes", "kapde", "shopping", "shoes", "myntra", "meesho", "mall", "market"}) {
            KEYWORD_MAP.put(kw, Category.SHOPPING);
        }

        // BILLS
        for (String kw : new String[]{
                "bijli", "electricity", "wifi", "broadband", "water", "paani", "gas",
                "cylinder", "recharge", "jio", "airtel", "bill", "bills", "current", "dth"}) {
            KEYWORD_MAP.put(kw, Category.BILLS);
        }

        // HEALTH
        for (String kw : new String[]{
                "doctor", "medicine", "dawa", "dawakhana", "pharmacy", "apollo",
                "hospital", "clinic", "gym", "tablet", "health", "test", "lab"}) {
            KEYWORD_MAP.put(kw, Category.HEALTH);
        }

        // ENTERTAINMENT
        for (String kw : new String[]{
                "movie", "cinema", "film", "netflix", "spotify", "prime", "hotstar",
                "game", "entertainment", "concert", "outing", "trip", "party"}) {
            KEYWORD_MAP.put(kw, Category.ENTERTAINMENT);
        }

        // SALARY (Income)
        for (String kw : new String[]{
                "salary", "tankhwah", "stipend", "bonus", "freelance", "dividend",
                "interest", "cashback", "refund", "payout", "income"}) {
            KEYWORD_MAP.put(kw, Category.SALARY);
        }
    }

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?:₹|rs\\.?|inr)?\\s*(\\d+(?:\\.\\d+)?)\\s*(k|lakh)?\\b",
            Pattern.CASE_INSENSITIVE
    );

    public ParsedTransactionResult parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input text cannot be null or empty");
        }

        String cleaned = input.trim();
        LocalDate resolvedDate = LocalDate.now();
        BigDecimal amount = null;
        String remainingText = cleaned;

        // 1. Resolve relative dates
        String lower = remainingText.toLowerCase(Locale.ROOT);

        if (lower.contains("day before yesterday") || lower.contains("parso")) {
            resolvedDate = LocalDate.now().minusDays(2);
            remainingText = remainingText.replaceAll("(?i)day before yesterday|parso", " ");
        } else if (lower.contains("yesterday") || lower.contains("kal")) {
            resolvedDate = LocalDate.now().minusDays(1);
            remainingText = remainingText.replaceAll("(?i)yesterday|kal", " ");
        } else if (lower.contains("today") || lower.contains("aaj")) {
            resolvedDate = LocalDate.now();
            remainingText = remainingText.replaceAll("(?i)today|aaj", " ");
        } else {
            // Check for "on Nth" / "on N" (e.g., "on 1st", "on 15th", "on 1")
            Pattern onDayPattern = Pattern.compile("\\bon\\s+(\\d{1,2})(?:st|nd|rd|th)?\\b", Pattern.CASE_INSENSITIVE);
            Matcher dayMatcher = onDayPattern.matcher(remainingText);
            if (dayMatcher.find()) {
                int day = Integer.parseInt(dayMatcher.group(1));
                LocalDate now = LocalDate.now();
                int safeDay = Math.min(day, now.lengthOfMonth());
                resolvedDate = LocalDate.of(now.getYear(), now.getMonth(), safeDay);
                remainingText = remainingText.replace(dayMatcher.group(0), " ");
            } else {
                // Check "N days ago"
                Pattern daysAgoPattern = Pattern.compile("(\\d+)\\s+days?\\s+ago", Pattern.CASE_INSENSITIVE);
                Matcher daysAgoMatcher = daysAgoPattern.matcher(remainingText);
                if (daysAgoMatcher.find()) {
                    int days = Integer.parseInt(daysAgoMatcher.group(1));
                    resolvedDate = LocalDate.now().minusDays(days);
                    remainingText = remainingText.replace(daysAgoMatcher.group(0), " ");
                }
            }
        }

        // 2. Extract numeric amount
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(remainingText);
        if (amountMatcher.find()) {
            String numStr = amountMatcher.group(1);
            String unit = amountMatcher.group(2);
            BigDecimal val = new BigDecimal(numStr);
            if (unit != null) {
                if (unit.equalsIgnoreCase("k")) {
                    val = val.multiply(BigDecimal.valueOf(1000));
                } else if (unit.equalsIgnoreCase("lakh")) {
                    val = val.multiply(BigDecimal.valueOf(100000));
                }
            }
            amount = val;
            remainingText = remainingText.substring(0, amountMatcher.start()) + " " + remainingText.substring(amountMatcher.end());
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Could not extract a positive monetary amount from: \"" + input + "\"");
        }

        // 3. Clean up note and extract words
        String cleanedNote = remainingText.replaceAll("\\s+", " ").trim();
        Category guessedCategory = Category.OTHER;

        String[] tokens = cleanedNote.toLowerCase(Locale.ROOT).split("[\\s,;.]+");
        for (String token : tokens) {
            if (KEYWORD_MAP.containsKey(token)) {
                guessedCategory = KEYWORD_MAP.get(token);
                break;
            }
        }

        // If note is empty, give a clean default
        if (cleanedNote.isBlank()) {
            cleanedNote = guessedCategory.name().charAt(0) + guessedCategory.name().substring(1).toLowerCase();
        }

        // 4. Determine transaction type: SALARY implies INCOME, others default to EXPENSE
        TransactionType type = guessedCategory == Category.SALARY
                ? TransactionType.INCOME
                : TransactionType.EXPENSE;

        Money money = Money.of(amount, DEFAULT_CURRENCY);

        return new ParsedTransactionResult(
                money,
                resolvedDate,
                cleanedNote,
                guessedCategory,
                type,
                input
        );
    }

    public record ParsedTransactionResult(
            Money amount,
            LocalDate date,
            String note,
            Category category,
            TransactionType type,
            String rawInput
    ) {
    }
}
