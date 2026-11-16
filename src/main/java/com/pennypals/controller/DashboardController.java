package com.pennypals.controller;

import com.pennypals.dto.UserRegistrationDto;
import com.pennypals.repository.TransactionRepository;
import com.pennypals.repository.UserProfileRepository;
import com.pennypals.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final TransactionRepository transactionRepository;

    public DashboardController(UserRepository userRepository,
                               UserProfileRepository userProfileRepository,
                               TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.transactionRepository = transactionRepository;
    }

    private void addProfileToModel(Principal principal, Model model) {
        if (principal != null) {
            String username = principal.getName();
            userRepository.findByUsername(username).ifPresent(user -> {
                userProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                    model.addAttribute("profile", profile);
                });
            });
        }
    }

    @GetMapping("/")
    public String landingPage(Model model) {
        if (!model.containsAttribute("registrationDto")) {
            model.addAttribute("registrationDto", new UserRegistrationDto());
        }
        return "index";
    }

    @GetMapping("/aquarium")
    public String aquariumPage(Principal principal, Model model) {
        addProfileToModel(principal, model);
        return "aquarium";
    }

    @GetMapping("/shop")
    public String shopPage(Principal principal, Model model) {
        addProfileToModel(principal, model);
        return "shop";
    }

    @GetMapping("/quests")
    public String questsPage(Principal principal, Model model) {
        addProfileToModel(principal, model);
        return "quests";
    }
}