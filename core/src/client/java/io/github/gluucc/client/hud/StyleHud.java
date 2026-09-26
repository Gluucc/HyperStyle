package io.github.gluucc.client.hud;

import io.github.gluucc.HyperStyle;
import io.github.gluucc.client.style.StyleEntry;
import io.github.gluucc.client.style.StyleMeter;
import io.github.gluucc.client.style.StyleRank;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.Collection;

public class StyleHud {
    static final Identifier TEXTURE = HyperStyle.id("textures/gui/test1.png");
    static final int TEXTURE_HEIGHT = 128;
    static final int TEXTURE_WIDTH = 96;

    static final int RANK_HEIGHT = 32;
    static final int RANK_WIDTH = 92;

    private static float rankScale = 1.0f;
    private static float rankScaleVelocity = 0.0f;

    static int lastCreatedAt = -1;

    public static void render(DrawContext context, float tickDelta){
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null || client.options.hudHidden) {
            return;
        }
        double stylePoints = StyleMeter.getStylePoints();
        StyleRank currentRank = StyleMeter.getCurrentRank();
        Collection<StyleEntry> styleList = StyleMeter.getStyleList();

        TextRenderer renderer = client.textRenderer;

        int width = context.getScaledWindowWidth();

        int meterX = width - TEXTURE_WIDTH - 10;
        int meterY = 10;

        context.drawTexture(TEXTURE, meterX, meterY, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        int lineSpacing = renderer.fontHeight + 3;
        int offsetY = 18;


        StyleEntry headEntry = styleList.stream().findFirst().orElse(null);
        if (headEntry != null && headEntry.createdAt() != lastCreatedAt) {
            rankScaleVelocity = 0.03f;
            lastCreatedAt = headEntry.createdAt();
        }

        if (rankScaleVelocity != 0.0f) {
            rankScale += rankScaleVelocity;

            if (rankScale >= 1.2f) {
                rankScale = 1.2f;
                rankScaleVelocity = -0.02f;
            }

            if (rankScale <= 1.0f) {
                rankScale = 1.0f;
                rankScaleVelocity = 0.0f;
            }
        }


        int currentRankWidth = Math.round(RANK_WIDTH * rankScale);
        int currentRankHeight = Math.round(RANK_HEIGHT * rankScale);

        int rankX = meterX + (TEXTURE_WIDTH - currentRankWidth) / 2;
        int rankY = meterY + (RANK_HEIGHT - currentRankHeight) / 2;

        context.drawTexture(HyperStyle.id(currentRank.getTexturePath()), rankX, rankY, 0, 0, currentRankWidth, currentRankHeight, currentRankWidth, currentRankHeight);
        offsetY += lineSpacing;

        context.drawText(renderer, Integer.toString((int) stylePoints), meterX, meterY+offsetY, 0xFFFFFFFF, false);
        offsetY += lineSpacing;


        for(StyleEntry entry : styleList) {
            String entryText = "+ " + entry.event().label();
            if (entry.count() > 1) {
                entryText += " x" + entry.count();
            }
            context.drawText(renderer, entryText, meterX, meterY + offsetY, entry.event().color(), false);
            offsetY += lineSpacing;
        }

        String freshnessText = "FRESHNESS " + String.format("%.2f", StyleMeter.getFreshness());
        int freshnessTextWidth = renderer.getWidth(freshnessText);
        context.drawText(renderer, freshnessText, meterX + (TEXTURE_WIDTH - freshnessTextWidth) / 2, meterY + TEXTURE_HEIGHT - renderer.fontHeight, 0xFFFFFFFF, false);
    }
}
