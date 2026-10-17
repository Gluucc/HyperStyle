package io.github.gluucc.hyperstyle.client.style;

public enum DpsLevel {
    BLACK(0.0f, 1.00f, 0xFF000000),
    GRAY(2.0f, 1.25f, 0xFF483938),
    RED(4.0f, 1.50f, 0xFFEE0602),
    GOLD(7.0f, 1.75f, 0xFFFFD700);

    private static final float LAST_LEVEL_THRESHOLD_MULTIPLIER = 1.5f;
    private static final float DEFAULT_THRESHOLD_MULTIPLIER = 2.0f;

    private final float reqDps;
    private final float pointsMultiplier;
    private final int color;

    private static float thresholdMultiplier = DEFAULT_THRESHOLD_MULTIPLIER;

    DpsLevel(float reqDps, float pointsMultiplier, int color) {
        this.reqDps = reqDps;
        this.pointsMultiplier = pointsMultiplier;
        this.color = color;
    }

    public float getPointsMultiplier() {
        return pointsMultiplier;
    }

    public int getColor() {
        return color;
    }

    public static float getDefaultThresholdMultiplier() {
        return DEFAULT_THRESHOLD_MULTIPLIER;
    }

    public float getLevelThreshold() {
        return this.reqDps * thresholdMultiplier;
    }

    public float getNextLevelThreshold() {
        if (this.getNextLevel() == this) {
            return this.getLevelThreshold() * LAST_LEVEL_THRESHOLD_MULTIPLIER;
        } else {
            return this.getNextLevel().getLevelThreshold();
        }
    }

    public DpsLevel getNextLevel() {
        DpsLevel[] all = values();
        int nextOrdinal = this.ordinal() + 1;
        if(nextOrdinal >= all.length) {
            return this;
        }
        return all[nextOrdinal];
    }

    public static void setThresholdMultiplier(float newThresholdMultiplier) {
        thresholdMultiplier = newThresholdMultiplier;
    }
}
