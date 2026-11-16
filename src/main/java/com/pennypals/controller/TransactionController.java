package com.pennypals.controller;

import com.pennypals.model.Budget;
import com.pennypals.model.Transaction;
import com.pennypals.model.User;
import com.pennypals.repository.BudgetRepository;
import com.pennypals.repository.UserProfileRepository;
import com.pennypals.service.BudgetService;
import com.pennypals.service.TransactionService;
import com.pennypals.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;
    private final UserProfileRepository userProfileRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetService budgetService;

    public TransactionController(TransactionService transactionService,
                                 UserService userService,
                                 UserProfileRepository userProfileRepository,
                                 BudgetRepository budgetRepository,
                                 BudgetService budgetService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.userProfileRepository = userProfileRepository;
        this.budgetRepository = budgetRepository;
        this.budgetService = budgetService;
    }

    private void addProfileToModel(UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            if (user != null) {
                userProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                    model.addAttribute("profile", profile);
                });
            }
        }
    }

    @GetMapping("/add-transaction")
    public String showAddTransactionForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("transaction", new Transaction());
        addProfileToModel(userDetails, model);

        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            if (user != null) {
                budgetService.initializeDefaultBudgetsForUser(user);
                List<Budget> budgets = budgetRepository.findByUser(user);
                model.addAttribute("budgets", budgets);
            }
        }

        return "add-transaction";
    }

    @PostMapping("/add-transaction")
    public String addTransaction(@Valid @ModelAttribute("transaction") Transaction transaction,
                                 BindingResult result,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {

        if (transaction.getCategory() != null && transaction.getCategory().equalsIgnoreCase("Income")) {
            transaction.setType("INCOME");
        } else {
            transaction.setType("EXPENSE");
        }

        if (result.hasErrors()) {
            addProfileToModel(userDetails, model);
            if (userDetails != null) {
                User user = userService.findByUsername(userDetails.getUsername());
                if (user != null) {
                    model.addAttribute("budgets", budgetRepository.findByUser(user));
                }
            }
            return "add-transaction";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        transactionService.addTransaction(user, transaction);

        return "redirect:/transactions";
    }

    @GetMapping("/transactions")
    public String transactionsPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @RequestParam(required = false, defaultValue = "ALL") String category,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        addProfileToModel(userDetails, model);

        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            if (user != null) {
                budgetService.initializeDefaultBudgetsForUser(user);
                List<Budget> budgets = budgetRepository.findByUser(user);
                model.addAttribute("budgets", budgets);

                // Build category limit map for O(1) Thymeleaf rendering
                Map<String, BigDecimal> categoryLimitMap = new HashMap<>();
                for (Budget b : budgets) {
                    categoryLimitMap.put(b.getCategory(), b.getMonthlyLimit());
                }
                model.addAttribute("categoryLimitMap", categoryLimitMap);

                // Filtered transactions for display in the table
                List<Transaction> transactions = transactionService.searchTransactions(
                        user.getId(), keyword, type, category, startDate, endDate, minAmount, maxAmount
                );

                // Compute current-month-only spending totals across all user transactions
                // so out-of-month transactions do not count toward this month's available budget
                LocalDate today = LocalDate.now();
                List<Transaction> allUserTransactions = transactionService.searchTransactions(
                        user.getId(), null, "ALL", "ALL", null, null, null, null
                );

                Map<String, BigDecimal> categorySpentMap = new HashMap<>();
                for (Transaction tx : allUserTransactions) {
                    if (tx.getDate() != null
                            && tx.getDate().getYear() == today.getYear()
                            && tx.getDate().getMonth() == today.getMonth()
                            && "EXPENSE".equalsIgnoreCase(tx.getType())
                            && tx.getCategory() != null) {
                        categorySpentMap.merge(tx.getCategory(), tx.getAmount(), BigDecimal::add);
                    }
                }
                model.addAttribute("categorySpentMap", categorySpentMap);

                model.addAttribute("transactions", transactions);
                model.addAttribute("keyword", keyword);
                model.addAttribute("selectedType", type);
                model.addAttribute("selectedCategory", category);
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
                model.addAttribute("minAmount", minAmount);
                model.addAttribute("maxAmount", maxAmount);

                int daysLeft = today.lengthOfMonth() - today.getDayOfMonth();
                model.addAttribute("daysLeft", daysLeft);
            }
        }
        return "transactions";
    }

    @PostMapping("/transactions/update/{id}")
    public String updateTransaction(
            @PathVariable Long id,
            @RequestParam String category,
            @RequestParam BigDecimal amount,
            @RequestParam String type,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            transactionService.updateTransaction(id, user.getId(), category, amount, type, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/transactions";
    }

    @PostMapping("/transactions/delete/{id}")
    public String deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            transactionService.deleteTransactionWithBalanceRecalculation(id, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Transaction removed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/transactions";
    }
}