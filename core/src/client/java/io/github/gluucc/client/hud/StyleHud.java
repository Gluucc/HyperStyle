package io.github.gluucc.client.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.gluucc.HyperStyle;
import io.github.gluucc.client.style.StyleEntry;
import io.github.gluucc.client.style.StyleMeter;
import io.github.gluucc.client.style.StyleRank;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.Tessellator;
import org.joml.Matrix4f;

import java.util.Collection;

public class StyleHud {
    public enum HudMode {
        DEFAULT,  // Auto: hidden for UNRANKED, shown otherwise
        ON,
        OFF
    }
    public static HudMode hudMode = HudMode.DEFAULT;

    private static SimpleFramebuffer customBuffer;

    static final Identifier TEXTURE = HyperStyle.id("textures/gui/test3.png");
    static final Identifier LARGE_FONT = HyperStyle.id("vcr_large");
    static final Identifier SMALL_FONT = HyperStyle.id("vcr_small");
    static final int TEXTURE_HEIGHT = 275;
    static final int TEXTURE_WIDTH = 192;

    static final int RANK_HEIGHT = 64;
    static final int RANK_WIDTH = 192;

    static int lastCreatedAt = -1;
    static int animationStartTick = -1;

    static final float GROW_PHASE = 0.4f;
    static final float HOLD_PHASE = 0.1f;
    static final float DECAY_PHASE = 0.5f;
    static final float RANK_SCALE_MAX = 1.3f;

    static final double RANK_SCALE_MAX_WIDTH_DIFF = Math.ceil(((RANK_WIDTH * RANK_SCALE_MAX) - TEXTURE_WIDTH) / 2);
    static final double RANK_SCALE_MAX_HEIGHT_DIFF = Math.ceil(((RANK_HEIGHT * RANK_SCALE_MAX) - RANK_HEIGHT) / 2);

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

        switch (hudMode) {
            case OFF:
                return;
            case ON:
                break;
            case DEFAULT:
            default:
                if (StyleMeter.getCurrentRank() == StyleRank.UNRANKED) {
                    return;
                }
                break;
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

        StyleRank currentRank = StyleMeter.getCurrentRank();
        Collection<StyleEntry> styleList = StyleMeter.getStyleList();

        TextRenderer renderer = client.textRenderer;

        int scaledWidth = context.getScaledWindowWidth();
        int scaledHeight = context.getScaledWindowHeight();

        int pixelWidth = client.getFramebuffer().textureWidth;
        int pixelHeight = client.getFramebuffer().textureHeight;

        if (customBuffer == null) {
            customBuffer = new SimpleFramebuffer(pixelWidth, pixelHeight, true, MinecraftClient.IS_SYSTEM_MAC);
        } else if (customBuffer.textureWidth != pixelWidth || customBuffer.textureHeight != pixelHeight) {
            customBuffer.resize(pixelWidth, pixelHeight, MinecraftClient.IS_SYSTEM_MAC);
        }

        var mainBuffer = client.getFramebuffer();

        customBuffer.setClearColor(0, 0, 0, 0);
        customBuffer.clear(MinecraftClient.IS_SYSTEM_MAC);

        customBuffer.beginWrite(true);

        int meterX = scaledWidth - TEXTURE_WIDTH - 20;
        int meterY = 20;

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        context.drawTexture(TEXTURE, meterX, meterY, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        int lineSpacing = renderer.fontHeight + 3;
        int offsetY = 52;

        int currentRankWidth = Math.round(RANK_WIDTH * rankScale);
        int currentRankHeight = Math.round(RANK_HEIGHT * rankScale);

        int rankX = meterX + (TEXTURE_WIDTH - currentRankWidth) / 2;
        int rankY = meterY + (RANK_HEIGHT - currentRankHeight) / 2;

        context.drawTexture(currentRank.getTexture(), rankX, rankY, 0, 0, currentRankWidth, currentRankHeight, currentRankWidth, currentRankHeight);
        offsetY += lineSpacing;

        RenderSystem.disableBlend();

        double percent = StyleMeter.getRankPercent();
        int filledWidth = (int)(TEXTURE_WIDTH * (percent / 100.0));

        context.fill(meterX, meterY+offsetY, meterX + TEXTURE_WIDTH, meterY + 10 + offsetY, 0xFF333333);

        if (filledWidth > 0) {
            context.fill(meterX, meterY+offsetY, meterX + filledWidth, meterY + 10 + offsetY, 0xFFFFFFFF);
        }

        offsetY += 20;
        Style customFontLarge = Style.EMPTY.withFont(LARGE_FONT);

        int largeLineSpacing = 22;

        for(StyleEntry entry : styleList) {
            String entryStr = "+ " + entry.event().label();
            if (entry.count() > 1) {
                entryStr += " x" + entry.count();
            }

            MutableText entryText = Text.literal(entryStr).setStyle(customFontLarge);

            context.drawText(renderer, entryText, meterX, meterY + offsetY, entry.event().color(), false);
            offsetY += largeLineSpacing;
        }

        Style customFontSmall = Style.EMPTY.withFont(SMALL_FONT);

        String freshnessStr = "FRESHNESS: " + String.format("%.2f", StyleMeter.getFreshness());
        MutableText freshnessText = Text.literal(freshnessStr).setStyle(customFontSmall);

        int freshnessTextWidth = renderer.getWidth(freshnessText.asOrderedText());
        context.drawText(renderer, freshnessText, meterX + (TEXTURE_WIDTH - freshnessTextWidth) / 2, meterY + TEXTURE_HEIGHT - renderer.fontHeight, 0xFFFFFFFF, false);

        customBuffer.endWrite();
        mainBuffer.beginWrite(true);

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, customBuffer.getColorAttachment());

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        // upper left and lower left
        float x1 = meterX - rankX * (float) RANK_SCALE_MAX_WIDTH_DIFF;
        // lower right and upper right
        float x2 = meterX + TEXTURE_WIDTH + (float) RANK_SCALE_MAX_WIDTH_DIFF;
        // upper left and upper right
        float y1 = meterY - (float) RANK_SCALE_MAX_HEIGHT_DIFF;
        // lower left and lower right
        float y2 = meterY + TEXTURE_HEIGHT;

        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        // upper left
        bufferBuilder.vertex(matrix4f, x1, y1, (float)1).texture(x1 / scaledWidth, 1 - y1 / scaledHeight).next();
        // lower left
        bufferBuilder.vertex(matrix4f, x1, y2, (float)1).texture(x1 / scaledWidth, 1 - y2 / scaledHeight).next();
        // lower right
        bufferBuilder.vertex(matrix4f, x2, y2, (float)1).texture(x2 / scaledWidth, 1 - y2 / scaledHeight).next();
        // upper right
        bufferBuilder.vertex(matrix4f, x2, y1, (float)1).texture(x2 / scaledWidth, 1 - y1 / scaledHeight).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
