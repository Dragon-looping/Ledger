package com.ledger.os.repository.jpa;

import com.ledger.os.model.Wallet;
import com.ledger.os.repository.WalletRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JpaWalletRepository implements WalletRepository {

    private final SpringDataWalletRepository springDataRepo;

    public JpaWalletRepository(SpringDataWalletRepository springDataRepo) {
        this.springDataRepo = Objects.requireNonNull(springDataRepo);
    }

    @Override
    public Wallet save(Wallet entity) {
        return springDataRepo.save(entity);
    }

    @Override
    public Optional<Wallet> findById(Long id) {
        return springDataRepo.findById(id);
    }

    @Override
    public List<Wallet> findAll() {
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
    public Optional<Wallet> findByName(String name) {
        return springDataRepo.findByName(name);
    }

    @Override
    public Optional<Wallet> findFirst() {
        List<Wallet> wallets = springDataRepo.findAll();
        return wallets.isEmpty() ? Optional.empty() : Optional.of(wallets.get(0));
    }
}
