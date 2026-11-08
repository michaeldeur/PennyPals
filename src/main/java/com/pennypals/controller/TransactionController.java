package com.pennypals.controller;


import com.pennypals.model.Transaction;
import com.pennypals.model.User;
import com.pennypals.repository.UserProfileRepository;
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
import java.util.List;

@Controller
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;
    private final UserProfileRepository userProfileRepository;

    public TransactionController(TransactionService transactionService,
                                 UserService userService,
                                 UserProfileRepository userProfileRepository) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.userProfileRepository = userProfileRepository;
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
                List<Transaction> transactions = transactionService.searchTransactions(
                        user.getId(), keyword, type, category, startDate, endDate, minAmount, maxAmount
                );


                // --- ADD THIS LINE ---
                java.util.Map<String, BigDecimal> categorySpentMap = transactionService.getCategorySpentTotals(user.getId());
                model.addAttribute("categorySpentMap", categorySpentMap);
                // ---------------------


                model.addAttribute("transactions", transactions);
                model.addAttribute("keyword", keyword);
                model.addAttribute("selectedType", type);
                model.addAttribute("selectedCategory", category);
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
                model.addAttribute("minAmount", minAmount);
                model.addAttribute("maxAmount", maxAmount);


                LocalDate today = LocalDate.now();
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