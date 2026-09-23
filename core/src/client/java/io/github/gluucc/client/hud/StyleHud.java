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
        context.drawText(renderer, Integer.toString((int) stylePoints), meterX, meterY+renderer.fontHeight+10, 0xFFFFFFFF, false);
        context.drawText(renderer, currentRank.getStyleLabel(), meterX, meterY+renderer.fontHeight, currentRank.getRankColor(), false);

        int entryOffsetY = 0;

        for(StyleEntry entry : styleList) {
            String entryText = "+ " + entry.event().label();
            if (entry.count() > 1) {
                entryText += " x" + entry.count();
            }
            context.drawText(renderer, entryText, meterX, 45+entryOffsetY, entry.event().color(), false);
            entryOffsetY += renderer.fontHeight + 5;
        }

        context.drawText(renderer, "FRESHNESS " + String.format("%.2f", StyleMeter.getFreshness()), meterX, meterY + 100 + renderer.fontHeight, 0xFFFFFFFF, false);
    }
}
