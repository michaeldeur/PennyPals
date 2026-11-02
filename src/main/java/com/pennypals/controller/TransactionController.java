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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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
        addProfileToModel(userDetails, model); // Supplies profile data to the header badge
        return "add-transaction";
    }

    @PostMapping("/add-transaction")
    public String addTransaction(@Valid @ModelAttribute("transaction") Transaction transaction,
                                 BindingResult result,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {

        // Manually assign type based on category so validation passes
        if (transaction.getCategory() != null && transaction.getCategory().equalsIgnoreCase("Income")) {
            transaction.setType("INCOME");
        } else {
            transaction.setType("EXPENSE");
        }

        // Clear previous type errors if any were recorded before setting it
        if (result.hasFieldErrors("type")) {
            // Re-validate or process if type was the only error
        }

        if (result.hasErrors()) {
            System.out.println("=== VALIDATION ERRORS: " + result.getAllErrors() + " ===");
            addProfileToModel(userDetails, model);
            return "add-transaction";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        transactionService.addTransaction(user, transaction);

        return "redirect:/transactions";
    }
}