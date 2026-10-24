package com.ledger.os.repository.jpa;

import com.ledger.os.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataWalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByName(String name);
}
