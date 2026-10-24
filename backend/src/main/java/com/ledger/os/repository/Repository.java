package com.ledger.os.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository abstraction for domain persistence.
 *
 * @param <T>  Entity type
 * @param <ID> Identifier type
 */
public interface Repository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    void deleteById(ID id);

    boolean existsById(ID id);

    long count();
}
