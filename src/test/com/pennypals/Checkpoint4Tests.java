package com.pennypals;

import com.pennypals.model.Transaction;
import com.pennypals.model.User;
import com.pennypals.model.UserProfile;
import com.pennypals.repository.BudgetRepository;
import com.pennypals.repository.TransactionRepository;
import com.pennypals.repository.UnlockedItemRepository;
import com.pennypals.repository.UserProfileRepository;
import com.pennypals.repository.UserRepository;
import com.pennypals.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class Checkpoint4Tests {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UnlockedItemRepository unlockedItemRepository;

    private User testUser;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);

        testUser = new User();
        testUser.setUsername("cp4user_" + suffix);
        testUser.setPassword("password123");
        testUser.setEmail("cp4_" + suffix + "@pennypals.com");
        testUser.setSecurityQuestion(1);
        testUser.setSecurityAnswer("Blue");
        testUser = userRepository.saveAndFlush(testUser);

        testProfile = new UserProfile();
        testProfile.setUser(testUser);
        testProfile.setPennies(50);
        testProfile = userProfileRepository.saveAndFlush(testProfile);
    }

    @Test
    @WithMockUser(username = "checkpoint4user", roles = {"USER"})
    @DisplayName("1. Test Transaction Update")
    public void test1_TransactionUpdate() {
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

        transactionService.updateTransaction(
                txId, testUser.getId(), "Lifestyle", new BigDecimal("45.00"), "EXPENSE", "Updated Note"
        );

        Transaction updated = transactionRepository.findById(txId).orElseThrow();
        assertEquals("Lifestyle", updated.getCategory());
        assertEquals(0, new BigDecimal("45.00").compareTo(updated.getAmount()));
        assertEquals("Updated Note", updated.getNotes());
    }

    @Test
    @WithMockUser(username = "checkpoint4user", roles = {"USER"})
    @DisplayName("2. Test Transaction Deletion and Balance Recalculation")
    public void test2_TransactionDeletionAndBalanceRecalculation() {
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

        transactionService.deleteTransactionWithBalanceRecalculation(txId, testUser.getId());

        UserProfile profileAfterDelete = userProfileRepository.findByUserId(testUser.getId()).orElseThrow();
        assertEquals(50, profileAfterDelete.getPennies());
        assertTrue(transactionRepository.findById(txId).isEmpty());
    }

    @Test
    @WithMockUser(username = "checkpoint4user", roles = {"USER"})
    @DisplayName("3. Test Dynamic Keyword Search")
    public void test3_DynamicKeywordSearch() {
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

        List<Transaction> results = transactionService.searchTransactions(
                testUser.getId(), "Groceries", "ALL", "ALL", null, null, null, null
        );
        assertEquals(1, results.size());
        assertEquals("Food", results.get(0).getCategory());
    }

    @Test
    @WithMockUser(username = "checkpoint4user", roles = {"USER"})
    @DisplayName("4. Test Transaction Type Filtering")
    public void test4_TransactionTypeFiltering() {
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

        List<Transaction> incomeResults = transactionService.searchTransactions(
                testUser.getId(), null, "INCOME", "ALL", null, null, null, null
        );
        assertEquals(1, incomeResults.size());
        assertEquals("Income", incomeResults.get(0).getCategory());
    }

    @Test
    @WithMockUser(username = "checkpoint4user", roles = {"USER"})
    @DisplayName("5. Test Category Filtering")
    public void test5_CategoryFiltering() {
        Transaction tx1 = new Transaction();
        tx1.setCategory("Food");
        tx1.setAmount(new BigDecimal("15.00"));
        transactionService.addTransaction(testUser, tx1);

        Transaction tx2 = new Transaction();
        tx2.setCategory("Entertainment");
        tx2.setAmount(new BigDecimal("30.00"));
        transactionService.addTransaction(testUser, tx2);

        List<Transaction> foodResults = transactionService.searchTransactions(
                testUser.getId(), null, "ALL", "Food", null, null, null, null
        );
        assertEquals(1, foodResults.size());
        assertEquals("Food", foodResults.get(0).getCategory());
    }

    @Test
    @WithMockUser(username = "checkpoint4user", roles = {"USER"})
    @DisplayName("6. Test Amount Range Threshold Filtering")
    public void test6_AmountRangeThresholdFiltering() {
        Transaction tx1 = new Transaction();
        tx1.setCategory("Food");
        tx1.setAmount(new BigDecimal("10.00"));
        transactionService.addTransaction(testUser, tx1);

        Transaction tx2 = new Transaction();
        tx2.setCategory("Rent");
        tx2.setAmount(new BigDecimal("750.00"));
        transactionService.addTransaction(testUser, tx2);

        List<Transaction> results = transactionService.searchTransactions(
                testUser.getId(), null, "ALL", "ALL", null, null, new BigDecimal("500.00"), new BigDecimal("1000.00")
        );
        assertEquals(1, results.size());
        assertEquals(0, new BigDecimal("750.00").compareTo(results.get(0).getAmount()));
    }

    @Test
    @WithMockUser(username = "checkpoint4user", roles = {"USER"})
    @DisplayName("7. Test Date Range Filtering")
    public void test7_DateRangeFiltering() {
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

        List<Transaction> results = transactionService.searchTransactions(
                testUser.getId(), null, "ALL", "ALL", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10), null, null
        );
        assertEquals(1, results.size());
        assertEquals(0, new BigDecimal("15.00").compareTo(results.get(0).getAmount()));
    }

    @Test
    @WithMockUser(username = "checkpoint4user", roles = {"USER"})
    @DisplayName("8. Test Empty Search Results")
    public void test8_EmptySearchResults() {
        List<Transaction> emptyResults = transactionService.searchTransactions(
                testUser.getId(), "NonExistentKeywordXYZ", "ALL", "ALL", null, null, null, null
        );

        assertNotNull(emptyResults);
        assertTrue(emptyResults.isEmpty());
    }

    @Test
    @WithMockUser(username = "checkpoint4user", roles = {"USER"})
    @DisplayName("9. Test Category Spent Totals Map")
    public void test9_CategorySpentTotalsMap() {
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
    @WithMockUser(username = "checkpoint4user", roles = {"USER"})
    @DisplayName("10. Test Income Category Handling")
    public void test10_IncomeCategoryHandling() {
        Transaction incomeTx = new Transaction();
        incomeTx.setCategory("Income");
        incomeTx.setAmount(new BigDecimal("1200.00"));
        transactionService.addTransaction(testUser, incomeTx);

        Map<String, BigDecimal> totals = transactionService.getCategorySpentTotals(testUser.getId());

        assertTrue(totals.containsKey("Income"));
        assertEquals(0, new BigDecimal("1200.00").compareTo(totals.get("Income")));
    }
}