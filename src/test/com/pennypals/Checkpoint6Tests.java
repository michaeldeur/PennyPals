package com.pennypals;

import com.pennypals.model.Quest;
import com.pennypals.model.UnlockedItem;
import com.pennypals.model.User;
import com.pennypals.model.UserProfile;
import com.pennypals.repository.QuestRepository;
import com.pennypals.repository.UnlockedItemRepository;
import com.pennypals.repository.UserProfileRepository;
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
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class Checkpoint6Tests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UnlockedItemRepository unlockedItemRepository;

    @Autowired
    private QuestRepository questRepository;

    private User getDemoUser() {
        return userRepository.findByUsername("demo_user").orElseThrow();
    }

    private UserProfile getDemoProfile(User user) {
        return userProfileRepository.findByUserId(user.getId()).orElseThrow();
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("1. Test Shop Page (/shop) Loads With Fish and Decoration Catalog Model Attributes")
    @SuppressWarnings("unchecked")
    public void test1_ShopPageLoadsWithCatalogAndInventoryModel() throws Exception {
        var result = mockMvc.perform(get("/shop"))
                .andExpect(status().isOk())
                .andExpect(view().name("shop"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attributeExists("catalog"))
                .andExpect(model().attributeExists("itemCosts"))
                .andExpect(model().attributeExists("ownedItems"))
                .andExpect(model().attributeExists("ownedNames"))
                .andExpect(model().attributeExists("ownedByName"))
                .andExpect(model().attributeExists("equippedDecorCount"))
                .andExpect(model().attributeExists("decorSlotMap"))
                .andReturn();

        List<String[]> catalog = (List<String[]>) result.getModelAndView().getModel().get("catalog");
        assertNotNull(catalog);
        long fishCount = catalog.stream().filter(entry -> "FISH".equals(entry[1])).count();
        long decorCount = catalog.stream().filter(entry -> "DECOR".equals(entry[1])).count();
        assertEquals(7, fishCount, "Shop catalog should contain 7 fish items.");
        assertEquals(7, decorCount, "Shop catalog should contain 7 decoration items.");
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("2. Test Quests Page (/quests) Seeds 3 Daily and 2 Weekly Quests Sorted by Penny Reward")
    @SuppressWarnings("unchecked")
    public void test2_QuestsPageSeedsDailyAndWeeklyQuestsSortedByReward() throws Exception {
        User user = getDemoUser();
        questRepository.deleteAll(questRepository.findByUser(user));
        questRepository.flush();

        var result = mockMvc.perform(get("/quests"))
                .andExpect(status().isOk())
                .andExpect(view().name("quests"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attributeExists("dailyQuests"))
                .andExpect(model().attributeExists("weeklyQuests"))
                .andReturn();

        List<Quest> dailyQuests = (List<Quest>) result.getModelAndView().getModel().get("dailyQuests");
        List<Quest> weeklyQuests = (List<Quest>) result.getModelAndView().getModel().get("weeklyQuests");

        assertNotNull(dailyQuests);
        assertNotNull(weeklyQuests);
        assertEquals(3, dailyQuests.size(), "Expected exactly 3 daily quests.");
        assertEquals(2, weeklyQuests.size(), "Expected exactly 2 weekly quests.");

        for (int i = 1; i < dailyQuests.size(); i++) {
            assertTrue(dailyQuests.get(i - 1).getPennyReward() <= dailyQuests.get(i).getPennyReward(),
                    "Daily quests should be sorted in ascending order by penny reward.");
        }
        for (int i = 1; i < weeklyQuests.size(); i++) {
            assertTrue(weeklyQuests.get(i - 1).getPennyReward() <= weeklyQuests.get(i).getPennyReward(),
                    "Weekly quests should be sorted in ascending order by penny reward.");
        }
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("3. Test Store Penny Currency Check: Rejects Insufficient Balance and Negative Costs")
    public void test3_PurchaseRejectedWhenInsufficientPenniesOrNegativeCost() throws Exception {
        User user = getDemoUser();
        UserProfile profile = getDemoProfile(user);
        profile.setPennies(39);
        userProfileRepository.saveAndFlush(profile);

        int initialOwnedCount = unlockedItemRepository.findByUserId(user.getId()).size();

        // Insufficient pennies (39 < 40)
        mockMvc.perform(post("/shop/purchase")
                        .with(csrf())
                        .param("itemName", "Goldfish")
                        .param("itemType", "FISH")
                        .param("colorHex", "#F97316")
                        .param("cost", "40"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shop"))
                .andExpect(flash().attributeExists("errorMessage"));

        assertEquals(39, getDemoProfile(user).getPennies());
        assertEquals(initialOwnedCount, unlockedItemRepository.findByUserId(user.getId()).size(),
                "Unaffordable item must not be added to unlocked items.");
        assertFalse(unlockedItemRepository.findByUserId(user.getId()).stream()
                .anyMatch(i -> "Goldfish".equals(i.getItemName())));

        // Negative cost guard
        mockMvc.perform(post("/shop/purchase")
                        .with(csrf())
                        .param("itemName", "Blue Tang")
                        .param("itemType", "FISH")
                        .param("colorHex", "#2563EB")
                        .param("cost", "-15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shop"))
                .andExpect(flash().attributeExists("errorMessage"));

        assertEquals(39, getDemoProfile(user).getPennies());
        assertEquals(initialOwnedCount, unlockedItemRepository.findByUserId(user.getId()).size());
        assertFalse(unlockedItemRepository.findByUserId(user.getId()).stream()
                .anyMatch(i -> "Blue Tang".equals(i.getItemName())));
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("4. Test Store Penny Currency Check: Exact Balance Purchase Sets Pennies to Zero and Unlocks Item")
    public void test4_ExactBalancePurchaseSucceedsAndPersistsItemMetadata() throws Exception {
        User user = getDemoUser();
        UserProfile profile = getDemoProfile(user);
        profile.setPennies(50);
        userProfileRepository.saveAndFlush(profile);

        mockMvc.perform(post("/shop/purchase")
                        .with(csrf())
                        .param("itemName", "Clownfish")
                        .param("itemType", "FISH")
                        .param("colorHex", "#FF4500")
                        .param("cost", "50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shop"))
                .andExpect(flash().attributeExists("successMessage"));

        UserProfile updated = getDemoProfile(user);
        assertEquals(0, updated.getPennies(), "Exact cost purchase should leave 0 pennies.");

        UnlockedItem saved = unlockedItemRepository.findByUserId(user.getId()).stream()
                .filter(i -> "Clownfish".equals(i.getItemName()))
                .findFirst()
                .orElseThrow();

        assertEquals("FISH", saved.getItemType());
        assertEquals("#FF4500", saved.getColorHex());
        assertEquals("PENNIES", saved.getCurrencyType());
        assertEquals(50, saved.getCost());
        assertTrue(Boolean.TRUE.equals(saved.getIsPurchased()));
        assertTrue(Boolean.TRUE.equals(saved.getIsEquipped()));
        assertEquals(user.getId(), saved.getUser().getId());
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("5. Test Item Ownership Constraint: Case-Insensitive Duplicate Purchase Blocked")
    public void test5_DuplicateItemPurchaseBlockedCaseInsensitive() throws Exception {
        User user = getDemoUser();
        UserProfile profile = getDemoProfile(user);
        profile.setPennies(300);
        userProfileRepository.saveAndFlush(profile);

        UnlockedItem existing = new UnlockedItem(
                "Treasure Chest", "DECOR", "#5C3A21", "PENNIES", 80, true, false, user
        );
        unlockedItemRepository.saveAndFlush(existing);

        int penniesBefore = getDemoProfile(user).getPennies();
        int totalOwnedBefore = unlockedItemRepository.findByUserId(user.getId()).size();

        mockMvc.perform(post("/shop/purchase")
                        .with(csrf())
                        .param("itemName", "treasure chest")
                        .param("itemType", "DECOR")
                        .param("colorHex", "#5C3A21")
                        .param("cost", "80"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shop"))
                .andExpect(flash().attributeExists("errorMessage"));

        assertEquals(penniesBefore, getDemoProfile(user).getPennies(),
                "Duplicate item purchase must not deduct pennies.");
        assertEquals(totalOwnedBefore, unlockedItemRepository.findByUserId(user.getId()).size(),
                "Duplicate item purchase must not insert a second UnlockedItem row.");
        long matchingCount = unlockedItemRepository.findByUserId(user.getId()).stream()
                .filter(i -> "Treasure Chest".equalsIgnoreCase(i.getItemName()))
                .count();
        assertEquals(1, matchingCount);
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("6. Test Item Ownership Constraint: Cross-User Toggle Rejected Without Mutating Foreign Item")
    public void test6_CannotToggleItemOwnedByAnotherUser() throws Exception {
        User otherUser = new User();
        otherUser.setUsername("other_checkpoint6_user");
        otherUser.setPassword("encoded-password");
        otherUser.setEmail("other6@example.com");
        otherUser = userRepository.saveAndFlush(otherUser);

        UnlockedItem foreignItem = new UnlockedItem(
                "Iron Anchor", "DECOR", "#334155", "PENNIES", 70, true, true, 1, otherUser
        );
        foreignItem = unlockedItemRepository.saveAndFlush(foreignItem);

        mockMvc.perform(post("/shop/toggle/" + foreignItem.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shop"))
                .andExpect(flash().attributeExists("errorMessage"));

        UnlockedItem reloaded = unlockedItemRepository.findById(foreignItem.getId()).orElseThrow();
        assertTrue(Boolean.TRUE.equals(reloaded.getIsEquipped()),
                "Toggling another user's item must not alter its equipped state.");
        assertEquals(Integer.valueOf(1), reloaded.getEquipOrder(),
                "Toggling another user's item must not alter its equip slot order.");
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("7. Test Canvas State Toggle: Equip and Unequip Fish Updates Aquarium Canvas State")
    public void test7_EquipAndUnequipFishUpdatesCanvasState() throws Exception {
        User user = getDemoUser();

        UnlockedItem fish = new UnlockedItem(
                "Angelfish", "FISH", "#F59E0B", "PENNIES", 60, true, false, user
        );
        fish = unlockedItemRepository.saveAndFlush(fish);

        mockMvc.perform(post("/shop/toggle/" + fish.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shop"))
                .andExpect(flash().attributeExists("successMessage"));

        assertTrue(Boolean.TRUE.equals(
                unlockedItemRepository.findById(fish.getId()).orElseThrow().getIsEquipped()
        ));

        mockMvc.perform(get("/aquarium"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/shop/toggle/" + fish.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shop"))
                .andExpect(flash().attributeExists("successMessage"));

        assertFalse(Boolean.TRUE.equals(
                unlockedItemRepository.findById(fish.getId()).orElseThrow().getIsEquipped()
        ));
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("8. Test Canvas State Toggle: Enforces Max 3 Equipped Decorations and Normalizes Slots")
    @SuppressWarnings("unchecked")
    public void test8_DecorationCapThreeAndSlotNormalization() throws Exception {
        User user = getDemoUser();

        // Clear existing decors (including any starter decor like "Sunken Treasure Chest")
        unlockedItemRepository.findByUserId(user.getId()).stream()
                .filter(i -> "DECOR".equalsIgnoreCase(i.getItemType()))
                .forEach(unlockedItemRepository::delete);
        unlockedItemRepository.flush();

        UnlockedItem d1 = unlockedItemRepository.saveAndFlush(
                new UnlockedItem("Stone Column", "DECOR", "#94A3B8", "PENNIES", 65, true, true, 1, user)
        );
        UnlockedItem d2 = unlockedItemRepository.saveAndFlush(
                new UnlockedItem("Iron Anchor", "DECOR", "#334155", "PENNIES", 70, true, true, 2, user)
        );
        UnlockedItem d3 = unlockedItemRepository.saveAndFlush(
                new UnlockedItem("Treasure Chest", "DECOR", "#5C3A21", "PENNIES", 80, true, true, 3, user)
        );
        UnlockedItem d4 = unlockedItemRepository.saveAndFlush(
                new UnlockedItem("Reef Archway", "DECOR", "#DB2777", "PENNIES", 125, true, false, null, user)
        );

        mockMvc.perform(post("/shop/toggle/" + d4.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shop"))
                .andExpect(flash().attributeExists("errorMessage"));

        assertFalse(Boolean.TRUE.equals(
                unlockedItemRepository.findById(d4.getId()).orElseThrow().getIsEquipped()
        ));

        mockMvc.perform(post("/shop/toggle/" + d2.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shop"))
                .andExpect(flash().attributeExists("successMessage"));

        assertFalse(Boolean.TRUE.equals(
                unlockedItemRepository.findById(d2.getId()).orElseThrow().getIsEquipped()
        ));
        assertNull(unlockedItemRepository.findById(d2.getId()).orElseThrow().getEquipOrder());
        assertEquals(Integer.valueOf(1), unlockedItemRepository.findById(d1.getId()).orElseThrow().getEquipOrder());
        assertEquals(Integer.valueOf(2), unlockedItemRepository.findById(d3.getId()).orElseThrow().getEquipOrder());

        mockMvc.perform(post("/shop/toggle/" + d4.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shop"))
                .andExpect(flash().attributeExists("successMessage"));

        assertTrue(Boolean.TRUE.equals(
                unlockedItemRepository.findById(d4.getId()).orElseThrow().getIsEquipped()
        ));
        assertEquals(Integer.valueOf(3), unlockedItemRepository.findById(d4.getId()).orElseThrow().getEquipOrder());

        var shopResult = mockMvc.perform(get("/shop"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("equippedDecorCount", 3))
                .andReturn();
        Map<Long, Integer> slotMap = (Map<Long, Integer>) shopResult.getModelAndView().getModel().get("decorSlotMap");
        assertEquals(Set.of(1, 2, 3), Set.copyOf(slotMap.values()));
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("9. Test Quest Completion Reward: Claiming Daily and Weekly Quests Awards Exact Pennies")
    public void test9_ClaimingDailyAndWeeklyQuestsAwardsPennies() throws Exception {
        User user = getDemoUser();
        UserProfile profile = getDemoProfile(user);
        int startingPennies = profile.getPennies();

        Quest dailyQuest = new Quest();
        dailyQuest.setUser(user);
        dailyQuest.setTitle("Daily Habit Check");
        dailyQuest.setDescription("Review daily spending.");
        dailyQuest.setFrequency("DAILY");
        dailyQuest.setPennyReward(25);
        dailyQuest.setCompleted(false);
        dailyQuest.setClaimed(false);
        dailyQuest = questRepository.saveAndFlush(dailyQuest);

        Quest weeklyQuest = new Quest();
        weeklyQuest.setUser(user);
        weeklyQuest.setTitle("Weekly Budget Audit");
        weeklyQuest.setDescription("Stay within weekly budget.");
        weeklyQuest.setFrequency("WEEKLY");
        weeklyQuest.setPennyReward(75);
        weeklyQuest.setCompleted(false);
        weeklyQuest.setClaimed(false);
        weeklyQuest = questRepository.saveAndFlush(weeklyQuest);

        mockMvc.perform(post("/quests/complete/" + dailyQuest.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests"))
                .andExpect(flash().attributeExists("successMessage"));

        assertEquals(startingPennies + 25, getDemoProfile(user).getPennies());
        Quest claimedDaily = questRepository.findById(dailyQuest.getId()).orElseThrow();
        assertTrue(claimedDaily.isCompleted());
        assertTrue(claimedDaily.isClaimed());

        mockMvc.perform(post("/quests/complete/" + weeklyQuest.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests"))
                .andExpect(flash().attributeExists("successMessage"));

        assertEquals(startingPennies + 100, getDemoProfile(user).getPennies());
        Quest claimedWeekly = questRepository.findById(weeklyQuest.getId()).orElseThrow();
        assertTrue(claimedWeekly.isCompleted());
        assertTrue(claimedWeekly.isClaimed());
    }

    @Test
    @WithMockUser(username = "demo_user", roles = {"USER"})
    @DisplayName("10. Test Quest Reward Guard: Already Claimed Quest and Foreign User Quest Rejected")
    public void test10_DoubleClaimAndForeignUserQuestBlocked() throws Exception {
        User user = getDemoUser();
        int startingPennies = getDemoProfile(user).getPennies();

        Quest alreadyClaimed = new Quest();
        alreadyClaimed.setUser(user);
        alreadyClaimed.setTitle("Completed & Claimed Quest");
        alreadyClaimed.setDescription("Already rewarded.");
        alreadyClaimed.setFrequency("DAILY");
        alreadyClaimed.setPennyReward(40);
        alreadyClaimed.setCompleted(true);
        alreadyClaimed.setClaimed(true);
        alreadyClaimed = questRepository.saveAndFlush(alreadyClaimed);

        mockMvc.perform(post("/quests/complete/" + alreadyClaimed.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests"))
                .andExpect(flash().attributeExists("errorMessage"));

        assertEquals(startingPennies, getDemoProfile(user).getPennies(),
                "Already-claimed quest must not pay out again.");

        User otherUser = new User();
        otherUser.setUsername("quest_other_user");
        otherUser.setPassword("encoded-password");
        otherUser.setEmail("quest_other@example.com");
        otherUser = userRepository.saveAndFlush(otherUser);

        Quest foreignQuest = new Quest();
        foreignQuest.setUser(otherUser);
        foreignQuest.setTitle("Other User Quest");
        foreignQuest.setDescription("Belongs to another user.");
        foreignQuest.setFrequency("WEEKLY");
        foreignQuest.setPennyReward(60);
        foreignQuest.setCompleted(false);
        foreignQuest.setClaimed(false);
        foreignQuest = questRepository.saveAndFlush(foreignQuest);

        mockMvc.perform(post("/quests/complete/" + foreignQuest.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quests"))
                .andExpect(flash().attributeExists("errorMessage"));

        assertEquals(startingPennies, getDemoProfile(user).getPennies(),
                "Claiming another user's quest must not award pennies to current user.");
        Quest reloadedForeign = questRepository.findById(foreignQuest.getId()).orElseThrow();
        assertFalse(reloadedForeign.isClaimed());
    }
}