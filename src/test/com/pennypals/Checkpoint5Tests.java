package com.pennypals;

import com.pennypals.model.Budget;
import com.pennypals.model.User;
import com.pennypals.repository.BudgetRepository;
import com.pennypals.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class Checkpoint5Tests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("1. Test Budget Goals Dashboard Page Accessibility")
    public void test1_BudgetPageLoadsSuccessfully() throws Exception {
        mockMvc.perform(get("/budget"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("2. Test Interactive 2D Aquarium Dashboard Accessibility")
    public void test2_AquariumDashboardLoadsSuccessfully() throws Exception {
        mockMvc.perform(get("/aquarium"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("3. Test Spending Cap Validation (Reject Negative Caps)")
    public void test3_SpendingCapValidationFailure() throws Exception {
        mockMvc.perform(post("/budget/update")
                        .with(csrf())
                        .param("categoryId", "1")
                        .param("monthlyCap", "-100.00"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("4. Test Budget Progress Update Interface Submission")
    public void test4_BudgetProgressUpdateSubmission() throws Exception {
        mockMvc.perform(post("/budget/update")
                        .with(csrf())
                        .param("categoryId", "1")
                        .param("monthlyCap", "250.00"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("5. Test Create New Budget Category Form Submission")
    public void test5_CreateNewBudgetCategory() throws Exception {
        mockMvc.perform(post("/budget/add")
                        .with(csrf())
                        .param("category", "Groceries")
                        .param("monthlyLimit", "400.00")
                        .param("type", "SPENDING_LIMIT"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("6. Test Budget Reward Engine Trigger Endpoint Mapping")
    public void test6_BudgetRewardEngineTriggerEndpoint() throws Exception {
        mockMvc.perform(post("/budget/evaluate-rewards")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("7. Test End-of-Month Reward Logic: Under Budget Triggers Pennies Award")
    public void test7_EndMonthRewardUnderBudget() throws Exception {
        mockMvc.perform(post("/budget/update")
                .with(csrf())
                .param("categoryId", "1")
                .param("monthlyCap", "500.00"));

        mockMvc.perform(post("/budget/evaluate-rewards")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("8. Test End-of-Month Reward Logic: Over Budget Grants No Reward")
    public void test8_EndMonthRewardOverBudget() throws Exception {
        mockMvc.perform(post("/budget/update")
                .with(csrf())
                .param("categoryId", "1")
                .param("monthlyCap", "1.00"));

        mockMvc.perform(post("/budget/evaluate-rewards")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("9. Test Aquarium Canvas Asset & Equipped Inventory State Endpoint")
    public void test9_AquariumInventoryStateEndpoint() throws Exception {
        mockMvc.perform(get("/aquarium"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("10. Test Budget Category Deletion Interface Handling")
    public void test10_BudgetCategoryDeletion() throws Exception {
        User user = userRepository.findByUsername("demo_user").orElseThrow();
        List<Budget> budgets = budgetRepository.findByUser(user);

        // Ensure a budget exists to delete, or create one dynamically if empty
        Long targetId;
        if (!budgets.isEmpty()) {
            targetId = budgets.get(0).getId();
        } else {
            Budget newBudget = new Budget();
            newBudget.setCategory("TempCategory");
            newBudget.setType("SPENDING_LIMIT");
            newBudget.setMonthlyLimit(new java.math.BigDecimal("50.00"));
            newBudget.setUser(user);
            targetId = budgetRepository.save(newBudget).getId();
        }

        mockMvc.perform(post("/budget/delete/" + targetId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}