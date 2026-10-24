package com.ledger.os.repository.jpa;

import com.ledger.os.model.Category;
import com.ledger.os.model.Transaction;
import com.ledger.os.repository.TransactionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JpaTransactionRepository implements TransactionRepository {

    private final SpringDataTransactionRepository springDataRepo;

    public JpaTransactionRepository(SpringDataTransactionRepository springDataRepo) {
        this.springDataRepo = Objects.requireNonNull(springDataRepo);
    }

    @Override
    public Transaction save(Transaction entity) {
        return springDataRepo.save(entity);
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return springDataRepo.findById(id);
    }

    @Override
    public List<Transaction> findAll() {
        return springDataRepo.findAll(Sort.by(Sort.Direction.DESC, "date", "id"));
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
    public List<Transaction> findByDateBetween(LocalDate start, LocalDate end) {
        return springDataRepo.findByDateBetweenOrderByDateDescIdDesc(start, end);
    }

    @Override
    public List<Transaction> findByCategory(Category category) {
        return springDataRepo.findByCategoryOrderByDateDesc(category);
    }

    @Override
    public List<Transaction> findByYearMonth(YearMonth period) {
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();
        return springDataRepo.findByDateRange(start, end);
    }

    @Override
    public List<Transaction> findByYear(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        return springDataRepo.findByDateRange(start, end);
    }
}
