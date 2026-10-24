package com.ledger.os.repository;

import com.ledger.os.model.Wallet;

import java.util.Optional;

public interface WalletRepository extends Repository<Wallet, Long> {

    Optional<Wallet> findByName(String name);

    Optional<Wallet> findFirst();
}
