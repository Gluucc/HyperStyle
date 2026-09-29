package io.github.gluucc.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
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

    static int lastCreatedAt = -1;
    static int animationStartTick = -1;

    static final float GROW_PHASE = 0.4f;
    static final float HOLD_PHASE = 0.1f;
    static final float DECAY_PHASE = 0.5f;
    static final float RANK_SCALE_MAX = 1.3f;


    public static void tick() {
        Collection<StyleEntry> styleList = StyleMeter.getStyleList();
        StyleEntry headEntry = styleList.stream().findFirst().orElse(null);

        if (headEntry != null && headEntry.createdAt() != lastCreatedAt) {
            lastCreatedAt = headEntry.createdAt();
            animationStartTick = StyleMeter.getCurrentTick();
        }
    }


    public static void render(DrawContext context, float tickDelta){
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null || client.options.hudHidden) {
            return;
        }


        float rankScale = 1.0f;
        if (animationStartTick != -1L) {
            long currentTick = StyleMeter.getCurrentTick();
            float currentTickFloat = currentTick + tickDelta;
            float duration = 6.0f; // tick
            float progress = (currentTickFloat - animationStartTick) / duration;

            if (progress >= 1.0f) {
                rankScale = 1.0f;
                animationStartTick = -1;
            } else {
                if (progress < GROW_PHASE) {
                    float growProgress = progress / GROW_PHASE;
                    rankScale = 1.0f + 0.3f * growProgress;
                } else if (progress < GROW_PHASE + HOLD_PHASE) {
                    rankScale = RANK_SCALE_MAX;
                } else {
                    float decayProgress = (progress - (GROW_PHASE + HOLD_PHASE)) / DECAY_PHASE;
                    float easedDecay = (float)(1.0f - Math.pow(1.0f - decayProgress, 3.0f));
                    rankScale = RANK_SCALE_MAX - 0.3f * easedDecay;
                }
            }
        }

        double stylePoints = StyleMeter.getStylePoints();
        StyleRank currentRank = StyleMeter.getCurrentRank();
        Collection<StyleEntry> styleList = StyleMeter.getStyleList();

        TextRenderer renderer = client.textRenderer;

        int width = context.getScaledWindowWidth();

        int meterX = width - TEXTURE_WIDTH - 10;
        int meterY = 10;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        context.drawTexture(TEXTURE, meterX, meterY, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        int lineSpacing = renderer.fontHeight + 3;
        int offsetY = 18;

        int currentRankWidth = Math.round(RANK_WIDTH * rankScale);
        int currentRankHeight = Math.round(RANK_HEIGHT * rankScale);

        int rankX = meterX + (TEXTURE_WIDTH - currentRankWidth) / 2;
        int rankY = meterY + (RANK_HEIGHT - currentRankHeight) / 2;

        context.drawTexture(currentRank.getTexture(), rankX, rankY, 0, 0, currentRankWidth, currentRankHeight, currentRankWidth, currentRankHeight);
        offsetY += lineSpacing;


        RenderSystem.disableBlend();

        double percent;
        if (currentRank.getNextRank() == currentRank) {
            percent = 100.0;
        } else {
            int rangeSize = (currentRank.getNextRank().getReqPoints()) - currentRank.getReqPoints();
            double currentPointsFromZero = stylePoints - currentRank.getReqPoints();
            percent = (currentPointsFromZero / rangeSize) * 100;
        }

        int filledWidth = (int)(TEXTURE_WIDTH * (percent / 100.0));

        context.fill(meterX, meterY+offsetY, meterX + TEXTURE_WIDTH, meterY + 6 + offsetY, 0xFF333333);

        if (filledWidth > 0) {
            context.fill(meterX, meterY+offsetY, meterX + filledWidth, meterY + 6 + offsetY, 0xFFFFFFFF);
        }

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
