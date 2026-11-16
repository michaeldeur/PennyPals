package com.pennypals.service;

import com.pennypals.model.Budget;
import com.pennypals.model.User;
import com.pennypals.model.UserProfile;
import com.pennypals.repository.BudgetRepository;
import com.pennypals.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    /**
     * Checks if a user has budgets. If empty, seeds the 10 default categories
     * balanced so that total expenses/savings equal the $3,000.00 income.
     */
    @Transactional
    public void initializeDefaultBudgetsForUser(User user) {
        List<Budget> existingBudgets = budgetRepository.findByUser(user);
        if (existingBudgets.isEmpty()) {
            String[][] defaultCategories = {
                    {"Income", "SAVINGS_GOAL", "3000.00"},
                    {"Giving", "SPENDING_LIMIT", "150.00"},
                    {"Savings", "SAVINGS_GOAL", "500.00"},
                    {"Housing", "SPENDING_LIMIT", "1200.00"},
                    {"Transportation", "SPENDING_LIMIT", "250.00"},
                    {"Food", "SPENDING_LIMIT", "450.00"},
                    {"Personal", "SPENDING_LIMIT", "100.00"},
                    {"Health", "SPENDING_LIMIT", "100.00"},
                    {"Insurance", "SPENDING_LIMIT", "150.00"},
                    {"Debts", "SPENDING_LIMIT", "100.00"}
            };

            for (String[] cat : defaultCategories) {
                Budget budget = new Budget();
                budget.setCategory(cat[0]);
                budget.setType(cat[1]);
                budget.setMonthlyLimit(new BigDecimal(cat[2]));
                budget.setCurrentProgress(BigDecimal.ZERO);
                budget.setIsCompleted(false);
                budget.setUser(user);

                budgetRepository.save(budget);
            }
        }
    }

    /**
     * Scheduled month-end evaluation task (Runs at midnight on the last day of every month).
     * Rewards users with 50 bonus pennies if they stayed under their monthly spending limits.
     */
    @Scheduled(cron = "0 0 0 L * ?")
    @Transactional
    public void evaluateMonthEndBudgets() {
        List<Budget> allBudgets = budgetRepository.findAll();

        for (Budget budget : allBudgets) {
            if (budget.getUser() != null && "SPENDING_LIMIT".equals(budget.getType()) && !Boolean.TRUE.equals(budget.getIsCompleted())) {
                if (budget.getCurrentProgress().compareTo(budget.getMonthlyLimit()) <= 0) {
                    budget.setIsCompleted(true);
                    budgetRepository.save(budget);

                    UserProfile profile = userProfileRepository.findByUserId(budget.getUser().getId()).orElse(null);
                    if (profile != null) {
                        profile.setPennies(profile.getPennies() + 50);
                        userProfileRepository.save(profile);
                    }
                }
            }
        }
    }
}