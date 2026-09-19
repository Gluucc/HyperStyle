package io.github.gluucc.client.style;

import java.util.ArrayDeque;
import java.util.Collection;

public class StyleMeter {
    private static double stylePoints;
    private static int currentTick;
    private static final double DECAY = 0.75;
    private static final int MAX_STYLE_LIST_AGE = 60;
    private static final ArrayDeque<QueuedEvent> styleQueue = new ArrayDeque<>();
    private static final ArrayDeque<StyleEntry> styleList = new ArrayDeque<>();
    private static StyleRank currentRank = StyleRank.UNRANKED;

    public static void tick() {
        currentTick++;

        QueuedEvent pending = styleQueue.pollFirst();
        if(pending != null) {
            StyleEntry top = styleList.peekFirst();
            if (top != null && top.text().equals(pending.text())) {
                styleList.pollFirst();
                styleList.addFirst(new StyleEntry(pending.text(), top.count() + 1, currentTick));
            } else {
                if (styleList.size() >= 5) {
                    styleList.removeLast();
                }

                styleList.addFirst(new StyleEntry(pending.text(), 1, currentTick));

            }
            stylePoints += pending.points();
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
    }

    public static void addStyle(String text, int points) {
        styleQueue.addLast(new QueuedEvent(text, points));
    }

    public static double getStylePoints() {
        return stylePoints;
    }

    public static StyleRank getCurrentRank() {
        return currentRank;
    }

    public static Collection<StyleEntry> getStyleList() {
        return styleList;
    }

    public static int getCurrentTick() {
        return currentTick;
    }
}
