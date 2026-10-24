package com.ledger.os.repository.jpa;

import com.ledger.os.model.Category;
import com.ledger.os.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataTransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDateBetweenOrderByDateDescIdDesc(LocalDate start, LocalDate end);

    List<Transaction> findByCategoryOrderByDateDesc(Category category);

    @Query("SELECT t FROM Transaction t WHERE t.date >= :start AND t.date <= :end ORDER BY t.date DESC, t.id DESC")
    List<Transaction> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
