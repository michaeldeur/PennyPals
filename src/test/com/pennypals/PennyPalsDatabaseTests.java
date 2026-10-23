package com.pennypals;

import com.pennypals.model.*;
import com.pennypals.repository.*;
import com.pennypals.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class PennyPalsDatabaseTests {

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
    void test1_DatabaseSchemaAndSeedDataLoading() {
        User user = userRepository.findByUsername("demo_user").orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("demo@pennypals.com");

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        assertThat(profile).isNotNull();
        assertThat(profile.getPennies()).isEqualTo(150);

        var transactions = transactionRepository.findByUserId(user.getId());
        assertThat(transactions).hasSize(2);

        var budgets = budgetRepository.findByUserId(user.getId());
        assertThat(budgets).hasSize(1);

        var items = unlockedItemRepository.findByUserId(user.getId());
        assertThat(items).hasSize(2);
    }

    @Test
    @Transactional
    void test2_CreateAndRetrieveNewTransaction() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();

        Transaction newTx = new Transaction();
        newTx.setType("EXPENSE");
        newTx.setCategory("Coffee");
        newTx.setAmount(new BigDecimal("4.50"));
        newTx.setDate(LocalDate.now());
        newTx.setNotes("Morning latte");
        newTx.setUser(user);

        Transaction savedTx = transactionRepository.save(newTx);
        assertThat(savedTx.getId()).isNotNull();

        var userTransactions = transactionRepository.findByUserId(user.getId());
        assertThat(userTransactions).hasSize(3);
    }

    @Test
    @Transactional
    void test3_UpdateUserProfilePennies() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElseThrow();

        int initialPennies = profile.getPennies();
        profile.setPennies(initialPennies + 50);
        userProfileRepository.save(profile);

        UserProfile updatedProfile = userProfileRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(updatedProfile.getPennies()).isEqualTo(200);
    }

    @Test
    @Transactional
    void test4_CreateAndRetrieveNewBudget() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();

        Budget newBudget = new Budget();
        newBudget.setType("SPENDING_LIMIT");
        newBudget.setCategory("Entertainment");
        newBudget.setMonthlyLimit(new BigDecimal("100.00"));
        newBudget.setCurrentProgress(new BigDecimal("20.00"));
        newBudget.setIsCompleted(false);
        newBudget.setUser(user);

        Budget savedBudget = budgetRepository.save(newBudget);
        assertThat(savedBudget.getId()).isNotNull();

        var budgets = budgetRepository.findByUserId(user.getId());
        assertThat(budgets).hasSize(2);
    }

    @Test
    @Transactional
    void test5_UnlockOrPurchaseNewShopItem() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();

        UnlockedItem newItem = new UnlockedItem();
        newItem.setItemName("Blue Tang");
        newItem.setItemType("FISH");
        newItem.setColorHex("#0000FF");
        newItem.setCurrencyType("PENNIES");
        newItem.setCost(75);
        newItem.setIsPurchased(true);
        newItem.setIsEquipped(false);
        newItem.setUser(user);

        UnlockedItem savedItem = unlockedItemRepository.save(newItem);
        assertThat(savedItem.getId()).isNotNull();

        var items = unlockedItemRepository.findByUserId(user.getId());
        assertThat(items).hasSize(3);
    }

    @Test
    @Transactional
    void test6_DeleteTransaction() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();
        var transactions = transactionRepository.findByUserId(user.getId());
        assertThat(transactions).isNotEmpty();

        Long txIdToDelete = transactions.getFirst().getId();
        transactionRepository.deleteById(txIdToDelete);

        var remainingTransactions = transactionRepository.findByUserId(user.getId());
        assertThat(remainingTransactions).hasSize(1);
    }

    @Test
    @Transactional
    void test7_EquipUnlockedItem() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();
        var items = unlockedItemRepository.findByUserId(user.getId());

        UnlockedItem chest = items.stream()
                .filter(i -> i.getItemName().equals("Treasure Chest"))
                .findFirst()
                .orElseThrow();

        assertThat(chest.getIsEquipped()).isFalse();

        chest.setIsEquipped(true);
        unlockedItemRepository.save(chest);

        UnlockedItem updatedChest = unlockedItemRepository.findById(chest.getId()).orElseThrow();
        assertThat(updatedChest.getIsEquipped()).isTrue();
    }

    @Test
    @Transactional
    void test8_UpdateBudgetProgress() {
        User user = userRepository.findByUsername("demo_user").orElseThrow();
        var budgets = budgetRepository.findByUserId(user.getId());
        Budget foodBudget = budgets.getFirst();

        foodBudget.setCurrentProgress(new BigDecimal("150.00"));
        budgetRepository.save(foodBudget);

        Budget updatedBudget = budgetRepository.findById(foodBudget.getId()).orElseThrow();
        assertThat(updatedBudget.getCurrentProgress()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @Transactional
    void test9_UserUniqueEmailConstraintViolation() {
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
    @Transactional
    void test10_FindUserByUsernameQuery() {
        User user = userRepository.findByUsername("demo_user").orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("demo_user");

        var nonExistent = userRepository.findByUsername("ghost_user");
        assertThat(nonExistent).isEmpty();
    }
}