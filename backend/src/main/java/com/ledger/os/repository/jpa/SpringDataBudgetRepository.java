package com.ledger.os.repository.jpa;

import com.ledger.os.model.Budget;
import com.ledger.os.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataBudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByCategoryAndYearAndMonth(Category category, int year, int month);

    List<Budget> findByYearAndMonth(int year, int month);
}
