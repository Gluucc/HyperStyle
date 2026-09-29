package io.github.gluucc.client.style;

import io.github.gluucc.HyperStyle;
import net.minecraft.util.Identifier;

public enum StyleRank {
    UNRANKED("UNRANKED", "textures/styleranks/linux.png", 0, 1.0, 0x00000000),
    D("DESTRUCTIVE", "textures/styleranks/d.png", 200, 1.0, 0xFF0000FF),
    C("CHAOTIC", "textures/styleranks/c.png", 300, 1.25, 0xFF00FF00),
    B("BRUTAL", "textures/styleranks/b.png", 400, 1.5, 0xFFFFFF00),
    A("ANARCHIC", "textures/styleranks/a.png", 500, 2.0, 0xFFFFA500),
    S("SUPREME", "textures/styleranks/s.png", 700, 3.0, 0xFFFF0000),
    SS("SSADISTIC", "textures/styleranks/ss.png",  850, 4.0, 0xFFFF0000),
    SSS("SSSHITSTORM", "textures/styleranks/sss.png" , 1000, 6.0,0xFFFF0000),
    SSSS("ULTRAKILL", "textures/styleranks/ssss.png" , 1500, 8.0, 0xFFFFD700);

    private final String styleLabel;
    private final Identifier texture;
    private final int reqPoints;
    private final double decayMultiplier;
    private final int rankColor;

    StyleRank(String styleRank, String texturePath, int reqPoints, double decayMultiplier, int rankColor) {
        this.styleLabel = styleRank;
        this.texture = HyperStyle.id(texturePath);
        this.reqPoints = reqPoints;
        this.decayMultiplier = decayMultiplier;
        this.rankColor = rankColor;
    }

    public String getStyleLabel() {
        return this.styleLabel;
    }

    public Identifier getTexture() { return this.texture; }

    public int getReqPoints() {
        return this.reqPoints;
    }

    public StyleRank getNextRank() {
        StyleRank[] all = values();
        int nextOrdinal = this.ordinal() + 1;
        if(nextOrdinal >= all.length) {
            return this;
        }
        return all[nextOrdinal];
    }

    public double getDecayMultiplier() {
        return this.decayMultiplier;
    }

    public int getRankColor() {
        return this.rankColor;
    }
}
