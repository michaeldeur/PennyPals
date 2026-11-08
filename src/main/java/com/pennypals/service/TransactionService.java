package com.pennypals.service;


import com.pennypals.model.Transaction;
import com.pennypals.model.User;
import com.pennypals.model.UserProfile;
import com.pennypals.repository.TransactionRepository;
import com.pennypals.repository.UserProfileRepository;
import com.pennypals.repository.TransactionSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Transaction> searchTransactions(
            Long userId, String keyword, String type, String category,
            LocalDate startDate, LocalDate endDate,
            BigDecimal minAmount, BigDecimal maxAmount) {


        Specification<Transaction> spec = Specification.where(TransactionSpecifications.belongsToUser(userId))
                .and(TransactionSpecifications.hasKeyword(keyword))
                .and(TransactionSpecifications.hasType(type))
                .and(TransactionSpecifications.hasCategory(category))
                .and(TransactionSpecifications.inDateRange(startDate, endDate))
                .and(TransactionSpecifications.inAmountRange(minAmount, maxAmount));


        return transactionRepository.findAll(spec);
    }


    public Map<String, BigDecimal> getCategorySpentTotals(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());


        List<Transaction> monthTransactions = transactionRepository.findAll(
                Specification.where(TransactionSpecifications.belongsToUser(userId))
                        .and(TransactionSpecifications.inDateRange(startOfMonth, endOfMonth))
        );


        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        for (Transaction tx : monthTransactions) {
            BigDecimal currentTotal = categoryTotals.getOrDefault(tx.getCategory(), BigDecimal.ZERO);
            currentTotal = currentTotal.add(tx.getAmount());
            categoryTotals.put(tx.getCategory(), currentTotal);
        }
        return categoryTotals;
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

        // Reverse penny reward logic upon deletion (subtract 10 pennies, keeping floor at 0)
        UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow();
        profile.setPennies(Math.max(0, profile.getPennies() - 10));
        userProfileRepository.save(profile);

        transactionRepository.delete(tx);
    }
}