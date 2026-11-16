package com.pennypals.repository;

import com.pennypals.model.Budget;
import com.pennypals.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser(User user);
}