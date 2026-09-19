package io.github.gluucc.client.style;

public enum StyleRank {
    UNRANKED("", 0, 1.0),
    D("DESTRUCTIVE", 1, 1.0),
    C("CHAOTIC", 300, 1.25),
    B("BRUTAL", 400, 1.5),
    A("ANARCHIC", 500, 2.0),
    S("SUPREME", 700, 3.0),
    SS("SSADISTIC", 850, 4.0),
    SSS("SSSHITSTORM", 1000, 6.0),
    SSSS("ULTRAKILL", 1500, 8.0);

    private final String styleLabel;
    private final int reqPoints;
    private final double decayMultiplier;

    StyleRank(String styleRank, int reqPoints, double decayMultiplier) {
        this.styleLabel = styleRank;
        this.reqPoints = reqPoints;
        this.decayMultiplier = decayMultiplier;
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
}
