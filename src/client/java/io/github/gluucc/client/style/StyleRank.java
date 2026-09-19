package io.github.gluucc.client.style;

public enum StyleRank {
    UNRANKED("", 0, 1.0, 0x00000000),
    D("DESTRUCTIVE", 1, 1.0, 0xFF0000FF),
    C("CHAOTIC", 300, 1.25, 0xFF00FF00),
    B("BRUTAL", 400, 1.5, 0xFFFFFF00),
    A("ANARCHIC", 500, 2.0, 0xFFFFA500),
    S("SUPREME", 700, 3.0, 0xFFFF0000),
    SS("SSADISTIC", 850, 4.0, 0xFFFF0000),
    SSS("SSSHITSTORM", 1000, 6.0,0xFFFF0000),
    SSSS("ULTRAKILL", 1500, 8.0, 0xFFFFD700);

    private final String styleLabel;
    private final int reqPoints;
    private final double decayMultiplier;
    private final int rankColor;

    StyleRank(String styleRank, int reqPoints, double decayMultiplier, int rankColor) {
        this.styleLabel = styleRank;
        this.reqPoints = reqPoints;
        this.decayMultiplier = decayMultiplier;
        this.rankColor = rankColor;
    }

    public String getStyleLabel() {
        return this.styleLabel;
    }

    public int getReqPoints() {
        return this.reqPoints;
    }

    public double getDecayMultiplier() {
        return this.decayMultiplier;
    }

    public int getRankColor() {
        return this.rankColor;
    }
}
