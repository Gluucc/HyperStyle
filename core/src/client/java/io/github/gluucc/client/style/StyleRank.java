package io.github.gluucc.client.style;

import io.github.gluucc.HyperStyle;
import net.minecraft.util.Identifier;

public enum StyleRank {
    UNRANKED("textures/styleranks/linux.png", 0, 1.0),
    D("textures/styleranks/d.png", 200, 1.0),
    C("textures/styleranks/c.png", 300, 1.25),
    B("textures/styleranks/b.png", 400, 1.5),
    A("textures/styleranks/a.png", 500, 2.0),
    S("textures/styleranks/s.png", 700, 3.0),
    SS("textures/styleranks/ss.png",  850, 4.0),
    SSS("textures/styleranks/sss.png" , 1000, 6.0),
    SSSS("textures/styleranks/ssss.png" , 1500, 8.0);

    private final Identifier texture;
    private final int reqPoints;
    private final double decayMultiplier;

    StyleRank(String texturePath, int reqPoints, double decayMultiplier) {
        this.texture = HyperStyle.id(texturePath);
        this.reqPoints = reqPoints;
        this.decayMultiplier = decayMultiplier;
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
}
