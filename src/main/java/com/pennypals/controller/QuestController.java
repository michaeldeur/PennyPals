package com.pennypals.controller;

import com.pennypals.model.Quest;
import com.pennypals.model.User;
import com.pennypals.model.UserProfile;
import com.pennypals.repository.QuestRepository;
import com.pennypals.repository.UserProfileRepository;
import com.pennypals.repository.UserRepository;
import com.pennypals.service.DailyQuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/quests")
public class QuestController {

    private static final Comparator<Quest> BY_REWARD_ASC = Comparator
            .comparingInt(Quest::getPennyReward)
            .thenComparing(Quest::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private QuestRepository questRepository;

    @Autowired
    private DailyQuestService dailyQuestService;

    @GetMapping
    @Transactional
    public String viewQuests(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        List<Quest> quests = questRepository.findByUser(user);
        if (quests.isEmpty()) {
            seedQuestsForUser(user);
            quests = questRepository.findByUser(user);
        }

        List<Quest> dailyQuests = quests.stream()
                .filter(q -> "DAILY".equalsIgnoreCase(q.getFrequency()))
                .sorted(BY_REWARD_ASC)
                .limit(3)
                .toList();

        List<Quest> weeklyQuests = quests.stream()
                .filter(q -> "WEEKLY".equalsIgnoreCase(q.getFrequency()))
                .sorted(BY_REWARD_ASC)
                .limit(2)
                .toList();

        model.addAttribute("profile", profile);
        model.addAttribute("dailyQuests", dailyQuests);
        model.addAttribute("weeklyQuests", weeklyQuests);
        return "quests";
    }

    @PostMapping("/complete/{id}")
    @Transactional
    public String completeAndClaimQuest(@AuthenticationPrincipal UserDetails userDetails,
                                        @PathVariable Long id,
                                        RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Quest quest = questRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quest not found"));

        if (quest.getUser() == null || !quest.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized quest action.");
            return "redirect:/quests";
        }

        if (quest.isClaimed()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Reward already claimed!");
            return "redirect:/quests";
        }

        quest.setCompleted(true);
        quest.setClaimed(true);
        questRepository.save(quest);

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        profile.setPennies(profile.getPennies() + quest.getPennyReward());
        userProfileRepository.save(profile);

        redirectAttributes.addFlashAttribute("successMessage",
                "Claimed +" + quest.getPennyReward() + " Pennies for: " + quest.getTitle());
        return "redirect:/quests";
    }

    private void seedQuestsForUser(User user) {
        List<DailyQuestService.QuestTemplate> dailyTemplates =
                dailyQuestService.getDailyQuestsForDate(LocalDate.now(), user.getUsername());

        List<Quest> seeded = new java.util.ArrayList<>();

        dailyTemplates.stream()
                .filter(tpl -> "DAILY".equalsIgnoreCase(tpl.frequency()))
                .sorted(Comparator.comparingInt(DailyQuestService.QuestTemplate::pennyReward))
                .limit(3)
                .forEach(tpl -> seeded.add(createQuest(
                        user,
                        tpl.title(),
                        tpl.description(),
                        "DAILY",
                        tpl.pennyReward()
                )));

        seeded.add(createQuest(
                user,
                "Weekly Savings Check-In",
                "Review your weekly budget and keep spending on track.",
                "WEEKLY",
                50
        ));
        seeded.add(createQuest(
                user,
                "Under-Cap Weekly Goal",
                "Stay within your weekly budget cap across categories.",
                "WEEKLY",
                75
        ));

        questRepository.saveAll(seeded);
    }

    private Quest createQuest(User user, String title, String desc, String freq, int reward) {
        Quest q = new Quest();
        q.setUser(user);
        q.setTitle(title);
        q.setDescription(desc);
        q.setFrequency(freq);
        q.setPennyReward(reward);
        q.setCompleted(false);
        q.setClaimed(false);
        return q;
    }
}