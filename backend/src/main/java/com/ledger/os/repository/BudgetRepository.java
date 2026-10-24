package com.ledger.os.repository;

import com.ledger.os.model.Budget;
import com.ledger.os.model.Category;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends Repository<Budget, Long> {

    Optional<Budget> findByCategoryAndPeriod(Category category, YearMonth period);

    List<Budget> findByPeriod(YearMonth period);
}
