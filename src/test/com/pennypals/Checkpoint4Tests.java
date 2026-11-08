package com.pennypals;

import com.pennypals.model.Transaction;
import com.pennypals.model.User;
import com.pennypals.model.UserProfile;
import com.pennypals.repository.BudgetRepository;
import com.pennypals.repository.TransactionRepository;
import com.pennypals.repository.UnlockedItemRepository;
import com.pennypals.repository.UserProfileRepository;
import com.pennypals.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class Checkpoint4Tests {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private com.pennypals.repository.UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UnlockedItemRepository unlockedItemRepository;

    private User testUser;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        // Clear all dependent child tables first to satisfy foreign key constraints
        transactionRepository.deleteAll();
        budgetRepository.deleteAll();
        unlockedItemRepository.deleteAll();
        userProfileRepository.deleteAll();

        // Now safe to delete parent users
        userRepository.deleteAll();

        // Setup test user
        testUser = new User();
        testUser.setUsername("checkpoint4user");
        testUser.setPassword("password123");
        testUser.setEmail("cp4@pennypals.com");
        testUser.setSecurityQuestion(1);
        testUser.setSecurityAnswer("Blue");
        testUser = userRepository.save(testUser);

        // Setup test profile linked via setUser()
        testProfile = new UserProfile();
        testProfile.setUser(testUser);
        testProfile.setPennies(50);
        testProfile = userProfileRepository.save(testProfile);
    }

    @Test
    void test1_TransactionUpdate() {
        // 1. Add initial transaction
        Transaction tx = new Transaction();
        tx.setCategory("Food");
        tx.setAmount(new BigDecimal("25.00"));
        tx.setNotes("Lunch");
        transactionService.addTransaction(testUser, tx);

        List<Transaction> userTransactions = transactionService.searchTransactions(
                testUser.getId(), null, "ALL", "ALL", null, null, null, null
        );
        assertEquals(1, userTransactions.size());
        Long txId = userTransactions.get(0).getId();

        // 2. Update transaction details
        transactionService.updateTransaction(
                txId, testUser.getId(), "Lifestyle", new BigDecimal("45.00"), "EXPENSE", "Updated Note"
        );

        // 3. Verify changes
        Transaction updated = transactionRepository.findById(txId).orElseThrow();
        assertEquals("Lifestyle", updated.getCategory());
        assertEquals(0, new BigDecimal("45.00").compareTo(updated.getAmount()));
        assertEquals("Updated Note", updated.getNotes());
    }

    @Test
    void test2_TransactionDeletionAndBalanceRecalculation() {
        // 1. Add transaction (adds +10 pennies -> total 60)
        Transaction tx = new Transaction();
        tx.setCategory("Transportation");
        tx.setAmount(new BigDecimal("15.00"));
        transactionService.addTransaction(testUser, tx);

        UserProfile profileAfterAdd = userProfileRepository.findByUserId(testUser.getId()).orElseThrow();
        assertEquals(60, profileAfterAdd.getPennies());

        List<Transaction> userTransactions = transactionService.searchTransactions(
                testUser.getId(), null, "ALL", "ALL", null, null, null, null
        );
        Long txId = userTransactions.get(0).getId();

        // 2. Delete transaction with balance recalculation (subtracts 10 pennies -> back to 50)
        transactionService.deleteTransactionWithBalanceRecalculation(txId, testUser.getId());

        UserProfile profileAfterDelete = userProfileRepository.findByUserId(testUser.getId()).orElseThrow();
        assertEquals(50, profileAfterDelete.getPennies());
        assertTrue(transactionRepository.findById(txId).isEmpty());
    }

    @Test
    void test3_DynamicKeywordSearch() {
        Transaction tx1 = new Transaction();
        tx1.setCategory("Food");
        tx1.setAmount(new BigDecimal("10.00"));
        tx1.setNotes("Groceries at Market");
        tx1.setDate(LocalDate.of(2026, 11, 1));
        transactionService.addTransaction(testUser, tx1);

        Transaction tx2 = new Transaction();
        tx2.setCategory("Utilities");
        tx2.setAmount(new BigDecimal("50.00"));
        tx2.setNotes("Electricity bill");
        tx2.setDate(LocalDate.of(2026, 11, 2));
        transactionService.addTransaction(testUser, tx2);

        // Test keyword search query
        List<Transaction> results = transactionService.searchTransactions(
                testUser.getId(), "Groceries", "ALL", "ALL", null, null, null, null
        );
        assertEquals(1, results.size());
        assertEquals("Food", results.get(0).getCategory());
    }

    @Test
    void test4_TransactionTypeFiltering() {
        Transaction tx1 = new Transaction();
        tx1.setType("EXPENSE");
        tx1.setCategory("Food");
        tx1.setAmount(new BigDecimal("20.00"));
        transactionService.addTransaction(testUser, tx1);

        Transaction tx2 = new Transaction();
        tx2.setType("INCOME");
        tx2.setCategory("Income");
        tx2.setAmount(new BigDecimal("500.00"));
        transactionService.addTransaction(testUser, tx2);

        // Filter strictly by INCOME type
        List<Transaction> incomeResults = transactionService.searchTransactions(
                testUser.getId(), null, "INCOME", "ALL", null, null, null, null
        );
        assertEquals(1, incomeResults.size());
        assertEquals("Income", incomeResults.get(0).getCategory());
    }

    @Test
    void test5_CategoryFiltering() {
        Transaction tx1 = new Transaction();
        tx1.setCategory("Food");
        tx1.setAmount(new BigDecimal("15.00"));
        transactionService.addTransaction(testUser, tx1);

        Transaction tx2 = new Transaction();
        tx2.setCategory("Entertainment");
        tx2.setAmount(new BigDecimal("30.00"));
        transactionService.addTransaction(testUser, tx2);

        // Filter strictly by Category
        List<Transaction> foodResults = transactionService.searchTransactions(
                testUser.getId(), null, "ALL", "Food", null, null, null, null
        );
        assertEquals(1, foodResults.size());
        assertEquals("Food", foodResults.get(0).getCategory());
    }

    @Test
    void test6_AmountRangeThresholdFiltering() {
        Transaction tx1 = new Transaction();
        tx1.setCategory("Food");
        tx1.setAmount(new BigDecimal("10.00"));
        transactionService.addTransaction(testUser, tx1);

        Transaction tx2 = new Transaction();
        tx2.setCategory("Rent");
        tx2.setAmount(new BigDecimal("750.00"));
        transactionService.addTransaction(testUser, tx2);

        // Filter by amount range threshold
        List<Transaction> results = transactionService.searchTransactions(
                testUser.getId(), null, "ALL", "ALL", null, null, new BigDecimal("500.00"), new BigDecimal("1000.00")
        );
        assertEquals(1, results.size());
        assertEquals(0, new BigDecimal("750.00").compareTo(results.get(0).getAmount()));
    }

    @Test
    void test7_DateRangeFiltering() {
        Transaction tx1 = new Transaction();
        tx1.setCategory("Food");
        tx1.setAmount(new BigDecimal("15.00"));
        tx1.setDate(LocalDate.of(2026, 11, 5));
        transactionService.addTransaction(testUser, tx1);

        Transaction tx2 = new Transaction();
        tx2.setCategory("Food");
        tx2.setAmount(new BigDecimal("25.00"));
        tx2.setDate(LocalDate.of(2026, 11, 20));
        transactionService.addTransaction(testUser, tx2);

        // Filter by specific date range
        List<Transaction> results = transactionService.searchTransactions(
                testUser.getId(), null, "ALL", "ALL", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10), null, null
        );
        assertEquals(1, results.size());
        assertEquals(0, new BigDecimal("15.00").compareTo(results.get(0).getAmount()));
    }

    @Test
    void test8_EmptySearchResults() {
        // Search with criteria that matches nothing
        List<Transaction> emptyResults = transactionService.searchTransactions(
                testUser.getId(), "NonExistentKeywordXYZ", "ALL", "ALL", null, null, null, null
        );

        assertNotNull(emptyResults);
        assertTrue(emptyResults.isEmpty());
    }

    @Test
    void test9_CategorySpentTotalsMap() {
        Transaction tx1 = new Transaction();
        tx1.setCategory("Food");
        tx1.setAmount(new BigDecimal("20.00"));
        transactionService.addTransaction(testUser, tx1);

        Transaction tx2 = new Transaction();
        tx2.setCategory("Food");
        tx2.setAmount(new BigDecimal("30.00"));
        transactionService.addTransaction(testUser, tx2);

        Map<String, BigDecimal> totals = transactionService.getCategorySpentTotals(testUser.getId());

        assertTrue(totals.containsKey("Food"));
        assertEquals(0, new BigDecimal("50.00").compareTo(totals.get("Food")));
    }

    @Test
    void test10_IncomeCategoryHandling() {
        Transaction incomeTx = new Transaction();
        incomeTx.setCategory("Income");
        incomeTx.setAmount(new BigDecimal("1200.00"));
        transactionService.addTransaction(testUser, incomeTx);

        Map<String, BigDecimal> totals = transactionService.getCategorySpentTotals(testUser.getId());

        // Income is defaulted to type Income with a positive sum in the totals map
        assertTrue(totals.containsKey("Income"));
        assertEquals(0, new BigDecimal("1200.00").compareTo(totals.get("Income")));
    }
}