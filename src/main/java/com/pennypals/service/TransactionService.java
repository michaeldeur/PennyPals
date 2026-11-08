package com.pennypals.service;

import com.pennypals.model.Transaction;
import com.pennypals.model.User;
import com.pennypals.model.UserProfile;
import com.pennypals.repository.TransactionRepository;
import com.pennypals.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserProfileRepository userProfileRepository;

    public TransactionService(TransactionRepository transactionRepository, UserProfileRepository userProfileRepository) {
        this.transactionRepository = transactionRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByDateDescIdDesc(userId);
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

    @Transactional
    public void updateTransaction(Long transactionId, Long userId, String category, BigDecimal amount, String type, String notes) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (!tx.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to transaction");
        }

        tx.setCategory(category);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setNotes(notes);

        transactionRepository.save(tx);
    }

    @Transactional
    public void deleteTransactionWithBalanceRecalculation(Long transactionId, Long userId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (!tx.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to transaction");
        }

        UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow();
        profile.setPennies(Math.max(0, profile.getPennies() - 10));
        userProfileRepository.save(profile);

        transactionRepository.delete(tx);
    }
}