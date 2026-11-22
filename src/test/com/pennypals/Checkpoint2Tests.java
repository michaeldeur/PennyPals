package com.pennypals;

import com.pennypals.model.Budget;
import com.pennypals.model.Transaction;
import com.pennypals.model.UnlockedItem;
import com.pennypals.model.User;
import com.pennypals.model.UserProfile;
import com.pennypals.repository.BudgetRepository;
import com.pennypals.repository.TransactionRepository;
import com.pennypals.repository.TransactionSpecifications;
import com.pennypals.repository.UnlockedItemRepository;
import com.pennypals.repository.UserProfileRepository;
import com.pennypals.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class Checkpoint2Tests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UnlockedItemRepository unlockedItemRepository;

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("1. Test Database Schema and Seed Data Loading")
    public void test1_DatabaseSchemaAndSeedDataLoading() {
        User user = userRepository.findByUsername("demo_user").orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("demo@pennypals.com");

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        assertThat(profile).isNotNull();
        assertThat(profile.getPennies()).isNotNull();

        assertThat(transactionRepository.findByUserId(user.getId())).isNotNull();
        assertThat(budgetRepository.findByUser(user)).isNotNull();
        assertThat(unlockedItemRepository.findByUserId(user.getId())).isNotNull();
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("2. Test Create and Retrieve New Transaction")
    public void test2_CreateAndRetrieveNewTransaction() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();
        int initialCount = transactionRepository.findByUserId(user.getId()).size();

        Transaction newTx = new Transaction();
        newTx.setType("EXPENSE");
        newTx.setCategory("Coffee");
        newTx.setAmount(new BigDecimal("4.50"));
        newTx.setDate(LocalDate.now());
        newTx.setNotes("Morning latte");
        newTx.setUser(user);

        Transaction savedTx = transactionRepository.saveAndFlush(newTx);
        assertThat(savedTx.getId()).isNotNull();

        var userTransactions = transactionRepository.findAll(
                Specification.where(TransactionSpecifications.belongsToUser(user.getId()))
        );
        assertThat(userTransactions).hasSize(initialCount + 1);
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("3. Test Update User Profile Pennies")
    public void test3_UpdateUserProfilePennies() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElseThrow();

        int initialPennies = profile.getPennies();
        profile.setPennies(initialPennies + 50);
        userProfileRepository.saveAndFlush(profile);

        UserProfile updatedProfile = userProfileRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(updatedProfile.getPennies()).isEqualTo(initialPennies + 50);
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("4. Test Create and Retrieve New Budget")
    public void test4_CreateAndRetrieveNewBudget() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();
        int initialCount = budgetRepository.findByUser(user).size();

        Budget newBudget = new Budget();
        newBudget.setType("SPENDING_LIMIT");
        newBudget.setCategory("Entertainment");
        newBudget.setMonthlyLimit(new BigDecimal("100.00"));
        newBudget.setCurrentProgress(new BigDecimal("20.00"));
        newBudget.setIsCompleted(false);
        newBudget.setUser(user);

        Budget savedBudget = budgetRepository.saveAndFlush(newBudget);
        assertThat(savedBudget.getId()).isNotNull();

        var budgets = budgetRepository.findByUser(user);
        assertThat(budgets).hasSize(initialCount + 1);
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("5. Test Unlock or Purchase New Shop Item")
    public void test5_UnlockOrPurchaseNewShopItem() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();
        int initialCount = unlockedItemRepository.findByUserId(user.getId()).size();

        UnlockedItem newItem = new UnlockedItem();
        newItem.setItemName("Blue Tang");
        newItem.setItemType("FISH");
        newItem.setColorHex("#0000FF");
        newItem.setCurrencyType("PENNIES");
        newItem.setCost(75);
        newItem.setIsPurchased(true);
        newItem.setIsEquipped(false);
        newItem.setUser(user);

        UnlockedItem savedItem = unlockedItemRepository.saveAndFlush(newItem);
        assertThat(savedItem.getId()).isNotNull();

        var items = unlockedItemRepository.findByUserId(user.getId());
        assertThat(items).hasSize(initialCount + 1);
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("6. Test Delete Transaction")
    public void test6_DeleteTransaction() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();

        Transaction tx = new Transaction();
        tx.setType("EXPENSE");
        tx.setCategory("Snacks");
        tx.setAmount(new BigDecimal("3.25"));
        tx.setDate(LocalDate.now());
        tx.setNotes("Temporary transaction");
        tx.setUser(user);
        Transaction savedTx = transactionRepository.saveAndFlush(tx);

        int countBeforeDelete = transactionRepository.findByUserId(user.getId()).size();
        transactionRepository.deleteById(savedTx.getId());
        transactionRepository.flush();

        var remainingTransactions = transactionRepository.findByUserId(user.getId());
        assertThat(remainingTransactions).hasSize(countBeforeDelete - 1);
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("7. Test Equip Unlocked Item")
    public void test7_EquipUnlockedItem() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();

        UnlockedItem chest = new UnlockedItem(
                "Treasure Chest", "DECOR", "#5C3A21", "PENNIES", 80, true, false, user
        );
        chest = unlockedItemRepository.saveAndFlush(chest);

        assertThat(chest.getIsEquipped()).isFalse();

        chest.setIsEquipped(true);
        unlockedItemRepository.saveAndFlush(chest);

        UnlockedItem updatedChest = unlockedItemRepository.findById(chest.getId()).orElseThrow();
        assertThat(updatedChest.getIsEquipped()).isTrue();
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("8. Test Update Budget Progress")
    public void test8_UpdateBudgetProgress() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();

        Budget budget = new Budget();
        budget.setType("SPENDING_LIMIT");
        budget.setCategory("Food");
        budget.setMonthlyLimit(new BigDecimal("200.00"));
        budget.setCurrentProgress(new BigDecimal("25.00"));
        budget.setIsCompleted(false);
        budget.setUser(user);
        budget = budgetRepository.saveAndFlush(budget);

        budget.setCurrentProgress(new BigDecimal("150.00"));
        budgetRepository.saveAndFlush(budget);

        Budget updatedBudget = budgetRepository.findById(budget.getId()).orElseThrow();
        assertThat(updatedBudget.getCurrentProgress()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("9. Test User Unique Email Constraint Violation")
    public void test9_UserUniqueEmailConstraintViolation() {
        User duplicateUser = new User();
        duplicateUser.setUsername("another_user");
        duplicateUser.setPassword("password123");
        duplicateUser.setEmail("demo@pennypals.com");
        duplicateUser.setSecurityQuestion(1);
        duplicateUser.setSecurityAnswer("Red");

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(duplicateUser);
        });
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("10. Test Find User By Username Query")
    public void test10_FindUserByUsernameQuery() {
        User user = userRepository.findByUsername("demo_user").orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("demo_user");

        var nonExistent = userRepository.findByUsername("ghost_user");
        assertThat(nonExistent).isEmpty();
    }
}