package com.ledger.os.repository.jpa;

import com.ledger.os.model.Budget;
import com.ledger.os.model.Category;
import com.ledger.os.repository.BudgetRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JpaBudgetRepository implements BudgetRepository {

    private final SpringDataBudgetRepository springDataRepo;

    public JpaBudgetRepository(SpringDataBudgetRepository springDataRepo) {
        this.springDataRepo = Objects.requireNonNull(springDataRepo);
    }

    @Override
    public Budget save(Budget entity) {
        return springDataRepo.save(entity);
    }

    @Override
    public Optional<Budget> findById(Long id) {
        return springDataRepo.findById(id);
    }

    @Override
    public List<Budget> findAll() {
        return springDataRepo.findAll();
    }

    @Override
    public void deleteById(Long id) {
        springDataRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataRepo.existsById(id);
    }

    @Override
    public long count() {
        return springDataRepo.count();
    }

    @Override
    public Optional<Budget> findByCategoryAndPeriod(Category category, YearMonth period) {
        return springDataRepo.findByCategoryAndYearAndMonth(category, period.getYear(), period.getMonthValue());
    }

    @Override
    public List<Budget> findByPeriod(YearMonth period) {
        return springDataRepo.findByYearAndMonth(period.getYear(), period.getMonthValue());
    }
}
