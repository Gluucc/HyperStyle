package io.github.gluucc.client.style;

import io.github.gluucc.client.api.StyleCategories;
import io.github.gluucc.client.api.StyleCategory;
import io.github.gluucc.client.api.StyleCategoryRegistry;
import io.github.gluucc.client.api.StyleEvent;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Collections;

public class StyleMeter {
    private static double stylePoints;
    private static int currentTick;
    private static final double DECAY = 0.75;
    private static final int MAX_STYLE_LIST_AGE = 60;
    private static final ArrayDeque<QueuedEvent> styleQueue = new ArrayDeque<>();
    private static final ArrayDeque<StyleEntry> styleList = new ArrayDeque<>();
    private static StyleRank currentRank = StyleRank.UNRANKED;
    private static final ArrayDeque<FreshnessEntry> freshnessWindow = new ArrayDeque<>();
    private static final int MAX_FRESHNESS_ENTRIES = 8;
    private static final int MAX_FRESHNESS_LIST_AGE = 80;
    private static double freshness = 1;
    private static final double MAX_FRESHNESS_BONUS = 0.75;
    private static double peakPoints = 0;

    public static void tick() {
        currentTick++;

        QueuedEvent pending = styleQueue.pollFirst();
        if(pending != null) {
            StyleEntry top = styleList.peekFirst();
            if (top != null && top.event() == pending.event()) {
                styleList.pollFirst();
                styleList.addFirst(new StyleEntry(pending.event(), top.count() + 1, currentTick));
            } else {
                if (styleList.size() >= 5) {
                    styleList.removeLast();
                }

                styleList.addFirst(new StyleEntry(pending.event(), 1, currentTick));

            }

            if (pending.category() != StyleCategories.NONE) {
                freshnessWindow.addFirst(new FreshnessEntry(pending.category(), currentTick));
                if (freshnessWindow.size() > MAX_FRESHNESS_ENTRIES) {
                    freshnessWindow.removeLast();
                }
            }



            int categoryCount = StyleCategoryRegistry.values().size() - 1;
            int count = countDistinctCategories();
            if (count == 0) {
                freshness = 1;
            } else {
                freshness = 1 + (count - 1) / (categoryCount - 1.0) * MAX_FRESHNESS_BONUS;
            }

            stylePoints += pending.event().points() * freshness;
        }

        if (stylePoints > 0) {
            stylePoints -= DECAY * currentRank.getDecayMultiplier();
        }

        if (stylePoints < 0) {
            stylePoints = 0;
        }

        for (StyleRank rank : StyleRank.values()) {
            if (rank.getReqPoints() <= stylePoints) {
                currentRank = rank;
            }
        }

        while(!styleList.isEmpty() && currentTick - styleList.getLast().createdAt() >= MAX_STYLE_LIST_AGE) {
            styleList.removeLast();
        }

        while(!freshnessWindow.isEmpty() && currentTick - freshnessWindow.getLast().createdAt() >= MAX_FRESHNESS_LIST_AGE) {
            freshnessWindow.removeLast();
        }
    }

    public static void addStyle(StyleEvent event, StyleCategory category) {
        styleQueue.addLast(new QueuedEvent(event, category));
    }

    public static void penalize(float amount) {
        if (amount <= 0) return;

        double basePenalty = amount * 0.5;
        double rankMultiplier = getCurrentRank().getDecayMultiplier();
        double actualPenalty = Math.min(basePenalty * rankMultiplier, stylePoints * 0.8);

        if (actualPenalty > 0) {
            stylePoints -= actualPenalty;
            if (stylePoints < 0) stylePoints = 0;
        }
    }

    public static StyleRank getCurrentRank() {
        return currentRank;
    }

    public static Collection<StyleEntry> getStyleList() {
        return Collections.unmodifiableCollection(styleList);
    }

    public static int getCurrentTick() {
        return currentTick;
    }

    public static double getFreshness() {
        return freshness;
    }

    public static double getRankPercent() {

        double percent;
        if (currentRank.getNextRank() == currentRank) {
            peakPoints = Math.max(peakPoints, stylePoints);

            double range = peakPoints - currentRank.getReqPoints();
            if(range <= 0) {
                percent = 100.0;
            } else {
                percent = (stylePoints - currentRank.getReqPoints()) / range * 100;
            }
        } else {
            peakPoints = 0;

            int rangeSize = (currentRank.getNextRank().getReqPoints()) - currentRank.getReqPoints();
            double currentPointsFromZero = stylePoints - currentRank.getReqPoints();
            percent = (currentPointsFromZero / rangeSize) * 100;
        }

        return Math.max(0, Math.min(100, percent));
    }

    private static int countDistinctCategories() {
        HashSet<StyleCategory> distinct = new HashSet<>();
        for (FreshnessEntry entry : freshnessWindow) {
            distinct.add(entry.category());
        }
        return distinct.size();
    }

    public static void reset() {
        currentRank = StyleRank.UNRANKED;
        currentTick = 0;
        stylePoints = 0;
        styleQueue.clear();
        styleList.clear();
        freshnessWindow.clear();
        freshness = 1;
        peakPoints = 0;
    }
}
