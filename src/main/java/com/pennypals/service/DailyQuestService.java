package com.pennypals.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class DailyQuestService {

    public record QuestTemplate(
            String code,
            String title,
            String description,
            int pennyReward,
            String frequency
    ) {}

    public static final List<QuestTemplate> QUEST_CATALOG = List.of(
            new QuestTemplate("LOGIN_TODAY", "Log in today", "Check in with your Penny Pals account today.", 15, "DAILY"),
            new QuestTemplate("REBALANCE_BUDGET", "Rebalance Budget", "Review and adjust your category allocations.", 25, "DAILY"),
            new QuestTemplate("UPLOAD_1_TX", "Upload a transaction", "Record at least 1 new income or expense entry.", 20, "DAILY"),
            new QuestTemplate("UPLOAD_3_TX", "Upload 3 transactions", "Record 3 transactions in a single day.", 45, "DAILY"),
            new QuestTemplate("CHECK_AQUARIUM", "Visit your Aquarium", "Open your aquarium and check on your fish and flora.", 15, "DAILY"),
            new QuestTemplate("FEED_FISH_5", "Feed your fish", "Drop pellets into your tank to feed your swimming pals.", 20, "DAILY"),
            new QuestTemplate("VISIT_SHOP", "Browse the Shop", "Visit the shop to check out fish, flora, and decor.", 10, "DAILY"),
            new QuestTemplate("LOG_EXPENSE", "Log an expense", "Add a spending entry with a clear category.", 15, "DAILY"),
            new QuestTemplate("LOG_INCOME", "Log an income entry", "Record incoming funds or deposits.", 25, "DAILY"),
            new QuestTemplate("NO_IMPULSE_BUY", "Skip an impulse buy", "Hold off on an unplanned purchase and log your win.", 35, "DAILY"),
            new QuestTemplate("REVIEW_TRANSACTIONS", "Review recent transactions", "Check your transaction history for accuracy.", 15, "DAILY"),
            new QuestTemplate("ADD_NOTE_TX", "Add a note to a transaction", "Include helpful context on a logged transaction.", 15, "DAILY"),
            new QuestTemplate("STAY_UNDER_DAILY_CAP", "Stay under daily spending cap", "Keep today's spending within your target limit.", 40, "DAILY"),
            new QuestTemplate("CHECK_SAVINGS_GOAL", "Check your savings progress", "Review your progress toward a savings target.", 20, "DAILY"),
            new QuestTemplate("CATEGORIZE_ALL", "Categorize every entry", "Ensure all recent transactions have categories assigned.", 25, "DAILY"),
            new QuestTemplate("EQUIP_DECOR_OR_FISH", "Customize tank lineup", "Update your equipped fish or decor arrangement.", 25, "DAILY"),
            new QuestTemplate("TRIM_SMALL_SPEND", "Trim a small expense", "Cut back on a small recurring or snack expense.", 30, "DAILY"),
            new QuestTemplate("PLAN_TOMORROW_SPEND", "Plan tomorrow's budget", "Set a spending intention before tomorrow begins.", 20, "DAILY"),
            new QuestTemplate("COMPLETE_ALL_DAILY", "Complete all daily quests", "Finish every daily quest on your list today.", 60, "DAILY"),

            new QuestTemplate("WEEKLY_BUDGET_CHECK", "Do a weekly budget check-in", "Compare your weekly spending against your plan.", 50, "WEEKLY"),
            new QuestTemplate("COMPARE_WEEKS", "Compare weekly spending", "Look at how this week's totals compare to last week.", 35, "WEEKLY"),
            new QuestTemplate("WEEKLY_SAVINGS_GOAL", "Weekly savings transfer", "Set aside funds toward your savings goal this week.", 65, "WEEKLY"),
            new QuestTemplate("WEEKLY_UNDER_CAP", "Stay under weekly cap", "Keep your weekly spending inside your target limit.", 75, "WEEKLY")
    );

    public List<QuestTemplate> getDailyQuestsForDate(LocalDate date, String username) {
        return selectQuestsForFrequency(date, username, "DAILY", 3, 31L);
    }

    public List<QuestTemplate> getWeeklyQuestsForWeek(LocalDate date, String username) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return selectQuestsForFrequency(weekStart, username, "WEEKLY", 2, 97L);
    }

    private List<QuestTemplate> selectQuestsForFrequency(
            LocalDate anchorDate,
            String username,
            String frequency,
            int maxCount,
            long multiplier
    ) {
        List<QuestTemplate> pool = QUEST_CATALOG.stream()
                .filter(q -> frequency.equalsIgnoreCase(q.frequency()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        int count = Math.min(maxCount, pool.size());
        long seed = anchorDate.toEpochDay() * multiplier + (username != null ? username.hashCode() : 0);

        Collections.shuffle(pool, new Random(seed));
        return List.copyOf(pool.subList(0, count));
    }
}