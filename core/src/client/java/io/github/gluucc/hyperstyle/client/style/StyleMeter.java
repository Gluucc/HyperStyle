package io.github.gluucc.hyperstyle.client.style;

import io.github.gluucc.hyperstyle.client.api.StyleCategories;
import io.github.gluucc.hyperstyle.client.api.StyleCategory;
import io.github.gluucc.hyperstyle.client.api.StyleCategoryRegistry;
import io.github.gluucc.hyperstyle.client.api.StyleEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

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
    private static float prevAirborneMultiplier = 1.00f;
    private static float currentAirborneMultiplier = 1.00f;
    private static int rankShakeTicks = 0;
    private static final int MAX_RANK_SHAKE_TICKS = 20;

    private static DpsLevel currentDpsLevel = DpsLevel.BLACK;
    private static final int MAX_DPS_LIST_AGE = 60;
    private static final ArrayDeque<DamageEntry> damageEntries = new ArrayDeque<>();
    private static float currentDps;
    private static float prevDps;
    private static float prevEffectiveDps;
    private static float effectiveDps;
    private static float dpsMultiplier = 1.00f;

    public static void tick() {
        currentTick++;

        if (rankShakeTicks > 0) {
            rankShakeTicks--;
        }

        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        while (!styleList.isEmpty() && currentTick - styleList.getLast().createdAt() >= MAX_STYLE_LIST_AGE) {
            styleList.removeLast();
        }

        while (!freshnessWindow.isEmpty() && currentTick - freshnessWindow.getLast().createdAt() >= MAX_FRESHNESS_LIST_AGE) {
            freshnessWindow.removeLast();
        }

        while (!damageEntries.isEmpty() && currentTick - damageEntries.getLast().createdAt() >= MAX_DPS_LIST_AGE) {
            damageEntries.removeLast();
        }

        if (player != null) {
            prevAirborneMultiplier = currentAirborneMultiplier;

            if (!player.isOnGround() && !player.isInFluid()) {
                currentAirborneMultiplier += 0.10f;
            }

            if (player.isOnGround()) {
                currentAirborneMultiplier -= 0.30f;
            }

            currentAirborneMultiplier = Math.max(1.00f, Math.min(currentAirborneMultiplier, 3.00f));
        }

        QueuedEvent pending = styleQueue.pollFirst();
        if (pending != null) {
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

            stylePoints += pending.event().points() * (1 + (freshness - 1) + (currentAirborneMultiplier - 1) + (dpsMultiplier - 1));
        }

        float totalDamage = 0;
        for (DamageEntry entry : damageEntries) {
            float damage = entry.damage();
            totalDamage += damage;
        }

        prevDps = currentDps;
        currentDps = Math.abs(totalDamage / ((float) MAX_DPS_LIST_AGE / 20));

        prevEffectiveDps = effectiveDps;
        if (currentDps == effectiveDps || currentDps > effectiveDps) {
            effectiveDps = MathHelper.lerp(0.3f, currentDps, effectiveDps);
        }

        if (currentDps < effectiveDps) {
            effectiveDps = MathHelper.lerp(0.02f, effectiveDps, currentDps);
        }

        for (DpsLevel level : DpsLevel.values()) {
            if (level.getLevelThreshold() <= effectiveDps) {
                currentDpsLevel = level;
                dpsMultiplier = currentDpsLevel.getPointsMultiplier();
            }
        }

        System.out.println("DPS: " + currentDps + " |DPS MULT: " + dpsMultiplier + "| DPS LEVEL: " + currentDpsLevel);

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

        rankShakeTicks = MAX_RANK_SHAKE_TICKS;
    }

    public static int getRankShakeTicks() {
        return rankShakeTicks;
    }

    public static int getMaxRankShakeTicks() {
        return MAX_RANK_SHAKE_TICKS;
    }

    public static StyleRank getCurrentRank() {
        return currentRank;
    }

    public static Collection<StyleEntry> getStyleList() {
        return Collections.unmodifiableCollection(styleList);
    }

    public static ArrayDeque<DamageEntry> getDamageEntries() {
        return damageEntries;
    }

    public static float getCurrentDps() {
        return currentDps;
    }

    public static float getEffectiveDps() {
        return effectiveDps;
    }

    public static float getPrevEffectiveDps() {
        return prevEffectiveDps;
    }

    public static float getPrevDps() {
        return prevDps;
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
            if (range <= 0) {
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

    public static double getDpsLevelPercent(float dps) {
        double percent;

        float rangeSize = (currentDpsLevel.getNextLevelThreshold() - currentDpsLevel.getLevelThreshold());
        double currentDpsFromZero = dps - currentDpsLevel.getLevelThreshold();
        percent = (currentDpsFromZero / rangeSize) * 100;

        return Math.max(0, Math.min(100, percent));
    }

    public static DpsLevel getCurrentDpsLevel() {
        return currentDpsLevel;
    }

    private static int countDistinctCategories() {
        HashSet<StyleCategory> distinct = new HashSet<>();
        for (FreshnessEntry entry : freshnessWindow) {
            distinct.add(entry.category());
        }
        return distinct.size();
    }

    public static float getCurrentAirborneMultiplier() {
        return currentAirborneMultiplier;
    }

    public static float getPrevAirborneMultiplier() {
        return prevAirborneMultiplier;
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
        damageEntries.clear();
        currentDps = 0;
        dpsMultiplier = DpsLevel.BLACK.getPointsMultiplier();
        currentDpsLevel = DpsLevel.BLACK;
        DpsLevel.setThresholdMultiplier(DpsLevel.getDefaultThresholdMultiplier());
    }
}
