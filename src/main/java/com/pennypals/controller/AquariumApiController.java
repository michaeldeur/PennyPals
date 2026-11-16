package com.pennypals.controller;

import com.pennypals.model.UnlockedItem;
import com.pennypals.model.User;
import com.pennypals.repository.UnlockedItemRepository;
import com.pennypals.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/aquarium")
public class AquariumApiController {

    @Autowired
    private UnlockedItemRepository unlockedItemRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/equipped")
    public ResponseEntity<List<UnlockedItem>> getEquippedItems(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Fetch items that belong to the user and are currently equipped
        List<UnlockedItem> equippedItems = unlockedItemRepository.findByUserAndIsEquipped(user, true);
        return ResponseEntity.ok(equippedItems);
    }
}