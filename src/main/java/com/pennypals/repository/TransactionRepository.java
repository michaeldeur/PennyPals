package com.pennypals.repository;

import com.pennypals.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    List<Transaction> findByUserId(@Param("userId") Long userId);
    List<Transaction> findByUserIdOrderByDateDescIdDesc(Long userId);
    List<Transaction> findTop5ByUserIdOrderByDateDescIdDesc(Long userId);
}