package com.pennypals.controller;

import com.pennypals.model.UnlockedItem;
import com.pennypals.model.User;
import com.pennypals.model.UserProfile;
import com.pennypals.repository.UnlockedItemRepository;
import com.pennypals.repository.UserProfileRepository;
import com.pennypals.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/shop")
public class ShopController {

    private static final int MAX_EQUIPPED_DECORS = 3;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UnlockedItemRepository unlockedItemRepository;

    // Catalog definition: {itemName, itemType, colorHex, cost, secondaryColorHex}
    private static final List<String[]> CATALOG = List.of(
            new String[]{"Goldfish", "FISH", "#F97316", "40", "#FDE047"},
            new String[]{"Clownfish", "FISH", "#FF4500", "50", "#FFFFFF"},
            new String[]{"Angelfish", "FISH", "#F59E0B", "60", "#FFFFFF"},
            new String[]{"Blue Tang", "FISH", "#2563EB", "70", "#FACC15"},
            new String[]{"Pufferfish", "FISH", "#8B5E34", "75", "#FDE68A"},
            new String[]{"Koi", "FISH", "#FB923C", "110", "#FFFFFF"},
            new String[]{"Shark", "FISH", "#94A3B8", "150", "#CBD5E1"},

            new String[]{"Stone Column", "DECOR", "#94A3B8", "65", "#94A3B8"},
            new String[]{"Iron Anchor", "DECOR", "#334155", "70", "#334155"},
            new String[]{"Treasure Chest", "DECOR", "#5C3A21", "80", "#5C3A21"},
            new String[]{"Shipwreck Helm", "DECOR", "#78350F", "85", "#78350F"},
            new String[]{"Tiki Monolith", "DECOR", "#475569", "95", "#475569"},
            new String[]{"Crystal Geode", "DECOR", "#57534E", "110", "#57534E"},
            new String[]{"Reef Archway", "DECOR", "#DB2777", "125", "#DB2777"}
    );

    @GetMapping
    public String viewShop(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        List<String[]> sortedCatalog = CATALOG.stream()
                .sorted(Comparator.comparingInt(item -> Integer.parseInt(item[3])))
                .collect(Collectors.toList());

        Map<String, Integer> itemCosts = sortedCatalog.stream()
                .collect(Collectors.toMap(item -> item[0], item -> Integer.parseInt(item[3]), (a, b) -> a));

        List<UnlockedItem> ownedItems = unlockedItemRepository.findByUserId(user.getId());
        Set<String> ownedNames = ownedItems.stream()
                .map(UnlockedItem::getItemName)
                .collect(Collectors.toSet());

        Map<String, UnlockedItem> ownedByName = ownedItems.stream()
                .collect(Collectors.toMap(UnlockedItem::getItemName, u -> u, (a, b) -> a));

        List<UnlockedItem> equippedDecors = getOrderedEquippedDecors(ownedItems);

        Map<Long, Integer> decorSlotMap = new HashMap<>();
        for (int i = 0; i < Math.min(equippedDecors.size(), MAX_EQUIPPED_DECORS); i++) {
            int slotNumber = (equippedDecors.get(i).getEquipOrder() != null)
                    ? equippedDecors.get(i).getEquipOrder()
                    : (i + 1);
            decorSlotMap.put(equippedDecors.get(i).getId(), slotNumber);
        }

        model.addAttribute("profile", profile);
        model.addAttribute("catalog", sortedCatalog);
        model.addAttribute("itemCosts", itemCosts);
        model.addAttribute("ownedItems", ownedItems);
        model.addAttribute("ownedNames", ownedNames);
        model.addAttribute("ownedByName", ownedByName);
        model.addAttribute("equippedDecorCount", equippedDecors.size());
        model.addAttribute("decorSlotMap", decorSlotMap);
        return "shop";
    }

    @PostMapping("/purchase")
    @Transactional
    public String purchaseItem(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam String itemName,
                               @RequestParam String itemType,
                               @RequestParam String colorHex,
                               @RequestParam int cost,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        List<UnlockedItem> currentOwned = unlockedItemRepository.findByUserId(user.getId());

        boolean alreadyOwned = currentOwned.stream()
                .anyMatch(item -> item.getItemName().equalsIgnoreCase(itemName));
        if (alreadyOwned) {
            redirectAttributes.addFlashAttribute("errorMessage", "You already own " + itemName + "!");
            return "redirect:/shop";
        }

        if (cost < 0 || profile.getPennies() < cost) {
            redirectAttributes.addFlashAttribute("errorMessage", "Not enough pennies to buy " + itemName + "!");
            return "redirect:/shop";
        }

        List<UnlockedItem> equippedDecors = getOrderedEquippedDecors(currentOwned);
        boolean isDecor = "DECOR".equalsIgnoreCase(itemType);
        boolean equipOnPurchase = !isDecor || equippedDecors.size() < MAX_EQUIPPED_DECORS;

        profile.setPennies(profile.getPennies() - cost);
        userProfileRepository.saveAndFlush(profile);

        UnlockedItem newItem = new UnlockedItem();
        newItem.setItemName(itemName);
        newItem.setItemType(itemType);
        newItem.setColorHex(colorHex);
        newItem.setCurrencyType("PENNIES");
        newItem.setCost(cost);
        newItem.setIsPurchased(true);
        newItem.setIsEquipped(equipOnPurchase);
        newItem.setEquipOrder(equipOnPurchase && isDecor ? equippedDecors.size() + 1 : null);
        newItem.setUser(user);
        unlockedItemRepository.saveAndFlush(newItem);

        if (equipOnPurchase) {
            redirectAttributes.addFlashAttribute("successMessage", "Purchased and equipped " + itemName + "!");
        } else {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Purchased " + itemName + "! Added to inventory (max 3 decorations can be equipped at once).");
        }
        return "redirect:/shop";
    }

    @PostMapping("/toggle/{id}")
    @Transactional
    public String toggleEquip(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UnlockedItem item = unlockedItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getUser() == null || !item.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized item action.");
            return "redirect:/shop";
        }

        boolean currentlyEquipped = Boolean.TRUE.equals(item.getIsEquipped());
        boolean nextEquippedState = !currentlyEquipped;
        boolean isDecor = "DECOR".equalsIgnoreCase(item.getItemType());

        List<UnlockedItem> userItems = unlockedItemRepository.findByUserId(user.getId());
        List<UnlockedItem> equippedDecors = getOrderedEquippedDecors(userItems);

        if (nextEquippedState && isDecor && equippedDecors.size() >= MAX_EQUIPPED_DECORS) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You can only equip up to 3 decorations at a time. Unequip one first.");
            return "redirect:/shop";
        }

        item.setIsEquipped(nextEquippedState);
        if (isDecor) {
            if (nextEquippedState) {
                item.setEquipOrder(equippedDecors.size() + 1);
                unlockedItemRepository.saveAndFlush(item);
            } else {
                item.setEquipOrder(null);
                unlockedItemRepository.saveAndFlush(item);
                normalizeDecorSlots(unlockedItemRepository.findByUserId(user.getId()));
            }
        } else {
            unlockedItemRepository.saveAndFlush(item);
        }

        redirectAttributes.addFlashAttribute("successMessage",
                item.getItemName() + (nextEquippedState ? " equipped to aquarium!" : " stored in inventory."));
        return "redirect:/shop";
    }

    private List<UnlockedItem> getOrderedEquippedDecors(List<UnlockedItem> items) {
        return items.stream()
                .filter(i -> "DECOR".equalsIgnoreCase(i.getItemType()) && Boolean.TRUE.equals(i.getIsEquipped()))
                .sorted(Comparator
                        .comparing((UnlockedItem i) -> i.getEquipOrder() == null ? Integer.MAX_VALUE : i.getEquipOrder())
                        .thenComparing(UnlockedItem::getId))
                .collect(Collectors.toList());
    }

    private void normalizeDecorSlots(List<UnlockedItem> userItems) {
        List<UnlockedItem> remaining = getOrderedEquippedDecors(userItems);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setEquipOrder(i + 1);
            unlockedItemRepository.saveAndFlush(remaining.get(i));
        }
    }
}