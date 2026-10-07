package com.devsu.hackerearth.backend.account.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devsu.hackerearth.backend.account.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findFirstByAccountIdOrderByDateDescIdDesc(Long accountId);

    List<Transaction> findByAccountIdInAndDateBetweenOrderByDateAscIdAsc(Collection<Long> accountIds, Date from,Date to);

    boolean existsByAccountId(Long accountId);
}
