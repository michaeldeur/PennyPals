package com.pennypals.service;

import com.pennypals.model.Transaction;
import com.pennypals.model.User;
import com.pennypals.model.UserProfile;
import com.pennypals.repository.TransactionRepository;
import com.pennypals.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserProfileRepository userProfileRepository;

    public TransactionService(TransactionRepository transactionRepository, UserProfileRepository userProfileRepository) {
        this.transactionRepository = transactionRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public void addTransaction(User user, Transaction transaction) {
        transaction.setUser(user);
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDate.now());
        }

        // Automatically determine type based on category
        if ("Income".equalsIgnoreCase(transaction.getCategory())) {
            transaction.setType("INCOME");
        } else {
            transaction.setType("EXPENSE");
        }

        // Save transaction
        transactionRepository.save(transaction);

        // Add 10 pennies to the user profile
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile != null) {
            profile.setPennies(profile.getPennies() + 10);
            userProfileRepository.save(profile);
        }
    }
}