package com.ledger.os.repository;

import com.ledger.os.model.Category;
import com.ledger.os.model.Transaction;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface TransactionRepository extends Repository<Transaction, Long> {

    List<Transaction> findByDateBetween(LocalDate start, LocalDate end);

    List<Transaction> findByCategory(Category category);

    List<Transaction> findByYearMonth(YearMonth period);

    List<Transaction> findByYear(int year);
}
