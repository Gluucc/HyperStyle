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
    final static Identifier TEXTURE = HyperStyle.id("textures/gui/test1.png");
    final static int TEXTURE_HEIGHT = 128;
    final static int TEXTURE_WIDTH = 96;

    public static void render(DrawContext context, float tickDelta){
        double stylePoints = StyleMeter.getStylePoints();
        StyleRank currentRank = StyleMeter.getCurrentRank();
        Collection<StyleEntry> styleList = StyleMeter.getStyleList();

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer renderer = client.textRenderer;

        int pointsGaugeWidth = renderer.getWidth(Integer.toString((int) stylePoints));
        int rankGaugeWidth = renderer.getWidth(currentRank.getStyleLabel());

        int width = context.getScaledWindowWidth();

        int meterX = width - TEXTURE_WIDTH - 10;
        int meterY = 10;

        context.drawTexture(TEXTURE, meterX, meterY, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        context.drawText(renderer, Integer.toString((int) stylePoints), meterX, 30, 0xFFFFFFFF, false);
        context.drawText(renderer, currentRank.getStyleLabel(), meterX, 20, currentRank.getRankColor(), false);

        int entryOffsetY = 0;

        for(StyleEntry entry : styleList) {
            String entryText = "+ " + entry.text();
            if (entry.count() > 1) {
                entryText += " x" + entry.count();
            }
            int entryWidth = renderer.getWidth(entryText);
            context.drawText(renderer, entryText, meterX, 45+entryOffsetY, entry.color(), false);
            entryOffsetY += renderer.fontHeight + 5;
        }
    }
}
