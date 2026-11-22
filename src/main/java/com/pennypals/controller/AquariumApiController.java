package com.pennypals.controller;

import com.pennypals.model.UnlockedItem;
import com.pennypals.model.User;
import com.pennypals.repository.UnlockedItemRepository;
import com.pennypals.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/aquarium")
public class AquariumApiController {

    @Autowired
    private UnlockedItemRepository unlockedItemRepository;

    @Autowired
    private UserRepository userRepository;

    public record EquippedItemDto(
            Long id,
            String itemName,
            String itemType,
            String colorHex,
            Integer equipOrder,
            Boolean isPurchased,
            Boolean isEquipped
    ) {}

    @GetMapping("/equipped")
    public ResponseEntity<List<EquippedItemDto>> getEquippedItems(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<EquippedItemDto> equippedItems = unlockedItemRepository.findByUserAndIsEquipped(user, true)
                .stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsPurchased()))
                .sorted(Comparator
                        .comparing((UnlockedItem item) -> item.getEquipOrder() == null ? Integer.MAX_VALUE : item.getEquipOrder())
                        .thenComparing(UnlockedItem::getId))
                .map(item -> new EquippedItemDto(
                        item.getId(),
                        item.getItemName(),
                        item.getItemType(),
                        item.getColorHex(),
                        item.getEquipOrder(),
                        item.getIsPurchased(),
                        item.getIsEquipped()
                ))
                .toList();

        return ResponseEntity.ok(equippedItems);
    }
}