package com.pennypals;

import com.pennypals.dto.UserRegistrationDto;
import com.pennypals.model.Transaction;
import com.pennypals.model.User;
import com.pennypals.model.UserProfile;
import com.pennypals.repository.TransactionRepository;
import com.pennypals.repository.UserProfileRepository;
import com.pennypals.repository.UserRepository;
import com.pennypals.service.TransactionService;
import com.pennypals.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class Checkpoint3Tests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    // Helper method to register users via UserRegistrationDto
    private User registerHelper(String username, String email) {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername(username);
        dto.setPassword("password123");
        dto.setEmail(email);
        dto.setSecurityQuestion(1);
        dto.setSecurityAnswer("answer");
        return userService.registerUser(dto);
    }

    @Test
    public void testUserRegistrationAndHashing() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("secureuser");
        dto.setPassword("rawPassword123");
        dto.setEmail("secure@test.com");
        dto.setSecurityQuestion(1);
        dto.setSecurityAnswer("fluffy");

        User savedUser = userService.registerUser(dto);

        assertNotNull(savedUser.getId());
        assertNotEquals("rawPassword123", savedUser.getPassword(), "Password must be hashed.");
        assertTrue(passwordEncoder.matches("rawPassword123", savedUser.getPassword()));

        UserProfile profile = userProfileRepository.findByUserId(savedUser.getId()).orElseThrow();
        assertEquals(100, profile.getPennies(), "New users must start with 100 pennies.");
    }

    @Test
    public void testDuplicateRegistrationRejection() {
        registerHelper("existinguser", "unique@test.com");

        UserRegistrationDto duplicateDto = new UserRegistrationDto();
        duplicateDto.setUsername("existinguser");
        duplicateDto.setPassword("password123");
        duplicateDto.setEmail("another@test.com");
        duplicateDto.setSecurityQuestion(1);
        duplicateDto.setSecurityAnswer("ans");

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(duplicateDto),
                "Service should reject duplicate usernames.");
    }

    @Test
    public void testUserAuthenticationFlow() {
        registerHelper("authuser", "auth@test.com");

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("authuser", "password123")
        );

        assertTrue(auth.isAuthenticated(), "Authentication token must be marked as authenticated.");
        assertEquals("authuser", auth.getName());
    }

    @Test
    public void testUnauthenticatedAccessControl() throws Exception {
        // Public landing page is accessible without login
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        // Aquarium requires authentication; unauthenticated requests should redirect
        mockMvc.perform(get("/aquarium"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testCurrencyRewardCalculation() {
        User user = registerHelper("rewarduser", "reward@test.com");
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElseThrow();
        int initialPennies = profile.getPennies();

        Transaction tx = new Transaction();
        tx.setType("INCOME");
        tx.setCategory("Salary");
        tx.setAmount(new BigDecimal("500.00"));
        tx.setDate(LocalDate.now());

        transactionService.addTransaction(user, tx);

        UserProfile updatedProfile = userProfileRepository.findByUserId(user.getId()).orElseThrow();
        assertEquals(initialPennies + 10, updatedProfile.getPennies(), "Reward engine should award 10 pennies per transaction.");
    }

    @Test
    public void testUserOwnershipIsolation() {
        User user1 = registerHelper("user1", "user1@test.com");
        User user2 = registerHelper("user2", "user2@test.com");

        Transaction tx1 = new Transaction();
        tx1.setType("EXPENSE");
        tx1.setCategory("Food");
        tx1.setAmount(new BigDecimal("10.00"));
        tx1.setDate(LocalDate.now());
        transactionService.addTransaction(user1, tx1);

        Transaction found = transactionRepository.findById(tx1.getId()).orElseThrow();
        assertEquals(user1.getId(), found.getUser().getId(), "Transaction must be linked to user1.");
        assertNotEquals(user2.getId(), found.getUser().getId(), "Transactions must remain isolated between users.");
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testSecurityPermissionsAccess() throws Exception {
        // Public landing page is accessible
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        // Secured aquarium page is accessible when authenticated
        mockMvc.perform(get("/aquarium"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "valuser")
    public void testFormValidationEnforcement() throws Exception {
        registerHelper("valuser", "val@test.com");

        // Submitting an invalid transaction with CSRF token included
        mockMvc.perform(post("/add-transaction")
                        .with(csrf())
                        .param("type", "EXPENSE")
                        .param("category", "Food")
                        .param("amount", "") // invalid empty amount
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(username = "formatuser")
    public void testInvalidAmountFormattingRejection() throws Exception {
        registerHelper("formatuser", "format@test.com");

        mockMvc.perform(post("/add-transaction")
                        .with(csrf())
                        .param("type", "INCOME")
                        .param("category", "Gift")
                        .param("amount", "not-a-number") // invalid number format
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());
    }

    @Test
    public void testPasswordResetVerificationFlow() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("resetuser");
        dto.setPassword("oldPassword123");
        dto.setEmail("reset@test.com");
        dto.setSecurityQuestion(1);
        dto.setSecurityAnswer("rover");
        userService.registerUser(dto);

        // Reset password with correct normalized security answer
        assertDoesNotThrow(() -> userService.resetPassword("reset@test.com", "ROVER", "newPassword456"));

        // Verify authentication succeeds with the new password
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("resetuser", "newPassword456")
        );
        assertTrue(auth.isAuthenticated());
    }
}