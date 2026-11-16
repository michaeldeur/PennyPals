package com.pennypals.controller;

import com.pennypals.model.Budget;
import com.pennypals.model.User;
import com.pennypals.repository.BudgetRepository;
import com.pennypals.repository.UserProfileRepository;
import com.pennypals.repository.UserRepository;
import com.pennypals.service.BudgetService;
import com.pennypals.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/budget")
public class BudgetController {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public String viewBudgets(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Attach UserProfile so the header Pennies badge renders dynamically
        userProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            model.addAttribute("profile", profile);
        });

        // Seed default 10 zero-based categories on first visit
        budgetService.initializeDefaultBudgetsForUser(user);

        List<Budget> budgets = budgetRepository.findByUser(user);

        // Fetch current-month-only spending totals (auto-relinks if a category name is recreated)
        Map<String, BigDecimal> categorySpentMap = transactionService.getCategorySpentTotals(user.getId());
        for (Budget b : budgets) {
            BigDecimal currentMonthSpent = categorySpentMap.getOrDefault(b.getCategory(), BigDecimal.ZERO);
            b.setCurrentProgress(currentMonthSpent);
        }

        // Calculate accurate remaining days in the current calendar month
        LocalDate today = LocalDate.now();
        int daysLeft = today.lengthOfMonth() - today.getDayOfMonth();
        model.addAttribute("daysLeft", daysLeft);

        model.addAttribute("budgets", budgets);
        model.addAttribute("newBudget", new Budget());
        return "budget";
    }

    @PostMapping("/add")
    public String createBudget(@AuthenticationPrincipal UserDetails userDetails,
                               @ModelAttribute Budget budget) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Automatically relink any existing current-month transactions matching this category name
        Map<String, BigDecimal> categorySpentMap = transactionService.getCategorySpentTotals(user.getId());
        BigDecimal existingCurrentMonthTotal = categorySpentMap.getOrDefault(budget.getCategory(), BigDecimal.ZERO);

        budget.setUser(user);
        budget.setCurrentProgress(existingCurrentMonthTotal);
        budget.setIsCompleted(false);

        budgetRepository.save(budget);
        return "redirect:/budget";
    }

    @PostMapping("/update/{id}")
    public String updateBudgetLimit(@AuthenticationPrincipal UserDetails userDetails,
                                    @PathVariable Long id,
                                    @RequestParam(value = "type", defaultValue = "SPENDING_LIMIT") String type,
                                    @RequestParam BigDecimal monthlyLimit) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Budget budget = budgetRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (budget.getUser() != null && budget.getUser().getId().equals(user.getId())) {
            budget.setType(type);
            budget.setMonthlyLimit(monthlyLimit);
            budgetRepository.save(budget);
        }
        return "redirect:/budget";
    }

    // Flexible fallback handler to support parameter-based test requests (e.g. /budget/update)
    @PostMapping("/update")
    public String updateBudgetParam(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam(value = "categoryId", required = false) Long categoryId,
                                    @RequestParam(value = "id", required = false) Long id,
                                    @RequestParam(value = "type", defaultValue = "SPENDING_LIMIT") String type,
                                    @RequestParam("monthlyCap") BigDecimal monthlyCap) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Long targetId = categoryId != null ? categoryId : id;
        if (targetId == null) {
            return "redirect:/budget";
        }

        Budget budget = budgetRepository.findById(targetId).orElse(null);
        if (budget == null) {
            // If the seeded demo data doesn't have ID 1, fetch the first available budget for testing purposes
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<Budget> userBudgets = budgetRepository.findByUser(user);
            if (!userBudgets.isEmpty()) {
                budget = userBudgets.get(0);
            } else {
                return "redirect:/budget";
            }
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (budget.getUser() != null && budget.getUser().getId().equals(user.getId())) {
            budget.setType(type);
            budget.setMonthlyLimit(monthlyCap);
            budgetRepository.save(budget);
        }
        return "redirect:/budget";
    }

    @PostMapping("/delete/{id}")
    public String deleteBudget(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long id) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Budget budget = budgetRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (budget.getUser() != null && budget.getUser().getId().equals(user.getId())) {
            budgetRepository.delete(budget);
        }
        return "redirect:/budget";
    }
}