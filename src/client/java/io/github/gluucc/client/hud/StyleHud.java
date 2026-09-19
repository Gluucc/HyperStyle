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
    final static int TEXTURE_RESOLUTION = 32;

    public static void render(DrawContext context, float tickDelta){
        double stylePoints = StyleMeter.getStylePoints();
        StyleRank currentRank = StyleMeter.getCurrentRank();
        Collection<StyleEntry> styleList = StyleMeter.getStyleList();

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer renderer = client.textRenderer;

        int pointsGaugeWidth = renderer.getWidth(Integer.toString((int) stylePoints));
        int rankGaugeWidth = renderer.getWidth(currentRank.getStyleLabel());

        int width = context.getScaledWindowWidth();

        context.drawTexture(TEXTURE, width-TEXTURE_RESOLUTION-10, 10, 0, 0, TEXTURE_RESOLUTION, TEXTURE_RESOLUTION, TEXTURE_RESOLUTION, TEXTURE_RESOLUTION);
        context.drawText(renderer, Integer.toString((int) stylePoints), width-pointsGaugeWidth-10, 30, 0xFF0000FF, false);
        context.drawText(renderer, currentRank.getStyleLabel(), width-rankGaugeWidth-10, 20, 0xFF0000FF, false);

        int entryOffsetY = 0;

        for(StyleEntry entry : styleList) {
            String entryText = "+ " + entry.text();
            if (entry.count() > 1) {
                entryText += " x" + entry.count();
            }
            int entryWidth = renderer.getWidth(entryText);
            context.drawText(renderer, entryText, width-entryWidth-10, 45+entryOffsetY, 0xFF0000FF, false);
            entryOffsetY += renderer.fontHeight + 5;
        }
    }
}
