package io.github.gluucc.client.hud;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import io.github.gluucc.HyperStyle;
import io.github.gluucc.client.config.ModConfig;
import io.github.gluucc.client.style.StyleEntry;
import io.github.gluucc.client.style.StyleMeter;
import io.github.gluucc.client.style.StyleRank;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.Collection;

public class StyleHud {
    public enum HudMode {
        DEFAULT,  // Auto: hidden for UNRANKED, shown otherwise
        ON,
        OFF
    }

    private static SimpleFramebuffer customBuffer;

    static final Identifier TEXTURE = HyperStyle.id("textures/gui/test3.png");
    static final Identifier DEFAULT_FONT = HyperStyle.id("vcr_default");
    static final Identifier MEDIUM_FONT = HyperStyle.id("vcr_medium");
    static final Identifier LARGE_FONT = HyperStyle.id("vcr_large");
    static final Identifier MASSIVE_FONT = HyperStyle.id("vcr_massive");
    static final Identifier SMALL_FONT = HyperStyle.id("vcr_small");

    static final int DEFAULT_FONT_HEIGHT = 14;
    static final int MEDIUM_FONT_HEIGHT = 10;
    static final int LARGE_FONT_HEIGHT = 17;
    static final int MASSIVE_FONT_HEIGHT = 20;
    static final int SMALL_FONT_HEIGHT = 8;
    public static final int TEXTURE_HEIGHT = 275;
    public static final int TEXTURE_WIDTH = 192;

    static final int RANK_HEIGHT = 84;
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


    public static void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null || client.options.hudHidden) {
            return;
        }

        switch (ModConfig.INSTANCE.hudMode) {
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
                    float easedDecay = (float) (1.0f - Math.pow(1.0f - decayProgress, 3.0f));
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

        int meterX = scaledWidth - TEXTURE_WIDTH - ModConfig.INSTANCE.hudOffsetRight;
        int meterY = ModConfig.INSTANCE.hudOffsetTop;

        float hudScale = ModConfig.INSTANCE.hudScale;
        float rightEdge = meterX + TEXTURE_WIDTH;
        MatrixStack matrices = context.getMatrices();


        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        matrices.push();
        matrices.translate(rightEdge, meterY, 0);
        matrices.scale(hudScale, hudScale, 1f);
        matrices.translate(-rightEdge, -meterY, 0);

        context.drawTexture(TEXTURE, meterX, meterY, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        int lineSpacing = renderer.fontHeight + 3;
        int offsetY = 72;

        int currentRankWidth = Math.round(RANK_WIDTH * rankScale);
        int currentRankHeight = Math.round(RANK_HEIGHT * rankScale);

        int rankX = meterX + (TEXTURE_WIDTH - currentRankWidth) / 2;
        int rankY = meterY + (RANK_HEIGHT - currentRankHeight) / 2;

        context.drawTexture(currentRank.getTexture(), rankX, rankY, 0, 0, currentRankWidth, currentRankHeight, currentRankWidth, currentRankHeight);
        offsetY += lineSpacing;

        RenderSystem.disableBlend();

        double percent = StyleMeter.getRankPercent();
        int filledWidth = (int) (TEXTURE_WIDTH * (percent / 100.0));

        context.fill(meterX, meterY + offsetY, meterX + TEXTURE_WIDTH, meterY + 10 + offsetY, 0xFF000000);

        if (filledWidth > 0) {
            context.fill(meterX, meterY + offsetY, meterX + filledWidth, meterY + 10 + offsetY, 0xFFFFFFFF);
        }

        offsetY += 20;
        Style customFontDefault = Style.EMPTY.withFont(DEFAULT_FONT);

        int largeLineSpacing = 22;

        for (StyleEntry entry : styleList) {
            MutableText entryText = Text.empty().fillStyle(customFontDefault);
            entryText.append(Text.literal("+  ").formatted(Formatting.WHITE));
            entryText.append(Text.literal(entry.event().label()));
            if (entry.count() > 1) {
                entryText.append(Text.literal(" x" + entry.count()).formatted(Formatting.WHITE));
            }

            context.drawText(renderer, entryText, meterX, meterY + offsetY, entry.event().color(), false);
            offsetY += largeLineSpacing;
        }

        int offsetBottom = 5;

        Style customFontSmall = Style.EMPTY.withFont(SMALL_FONT);

        String freshnessStr = "FRESHNESS: " + String.format("%.2f", StyleMeter.getFreshness());
        MutableText freshnessText = Text.literal(freshnessStr).setStyle(customFontSmall);

        int freshnessTextWidth = renderer.getWidth(freshnessText.asOrderedText());
        context.drawText(renderer, freshnessText, meterX + (TEXTURE_WIDTH - freshnessTextWidth) / 2, meterY + TEXTURE_HEIGHT - SMALL_FONT_HEIGHT - offsetBottom, 0xFFFFFFFF, false);

        offsetBottom += 10;

        Style customFontMedium = Style.EMPTY.withFont(MEDIUM_FONT);
        Style customFontLarge = Style.EMPTY.withFont(LARGE_FONT);
        Style customFontMassive = Style.EMPTY.withFont(MASSIVE_FONT);

        MutableText multiplierText = Text.empty().fillStyle(customFontMedium);
        multiplierText.append(Text.literal("MULTIPLIER").setStyle(customFontMedium));

        float smoothMultiplier = MathHelper.lerp(tickDelta, StyleMeter.getPrevMultiplier(), StyleMeter.getCurrentMultiplier());
        float steppedMultiplier = Math.round(smoothMultiplier * 100.0f) / 100.0f;
        String multiplierToDisplay = String.format(java.util.Locale.US, "%.2f", steppedMultiplier);

        int currentMultiplierFontHeight = DEFAULT_FONT_HEIGHT;

        MutableText multiplierMultiplierText = Text.empty().fillStyle(customFontDefault);


        if (steppedMultiplier >= 2.00f) {
            multiplierMultiplierText = Text.empty().fillStyle(customFontLarge).withColor(0xFFFF8C00);
            currentMultiplierFontHeight = LARGE_FONT_HEIGHT;
            HudRenderUtil.applyHudShakeRand(matrices, 2.0f);
        }

        if (steppedMultiplier >= 3.00f) {
            multiplierMultiplierText = Text.empty().fillStyle(customFontMassive).withColor(0xFFFF0000);
            currentMultiplierFontHeight = MASSIVE_FONT_HEIGHT;
            HudRenderUtil.applyHudShakeRand(matrices, 3.0f);
        }
        multiplierMultiplierText.append(Text.literal("x" + multiplierToDisplay));

        int multiplierMultiplierTextWidth = renderer.getWidth(multiplierMultiplierText.asOrderedText());

        context.drawText(renderer, multiplierText, meterX, meterY + TEXTURE_HEIGHT - DEFAULT_FONT_HEIGHT - SMALL_FONT_HEIGHT - offsetBottom + DEFAULT_FONT_HEIGHT - MEDIUM_FONT_HEIGHT, 0xFFFFFFFF, false);
        context.drawText(renderer, multiplierMultiplierText, meterX + TEXTURE_WIDTH - multiplierMultiplierTextWidth, meterY + TEXTURE_HEIGHT - currentMultiplierFontHeight - SMALL_FONT_HEIGHT - offsetBottom, 0xFFFFFFFF, false);

        matrices.pop();

        customBuffer.endWrite();
        mainBuffer.beginWrite(true);

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, customBuffer.getColorAttachment());

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.backupProjectionMatrix();
        customBuffer.setTexFilter(GlConst.GL_LINEAR);
        Matrix4f projection = new Matrix4f();
        float fov = (float) Math.toRadians(40);
        Matrix4f perspectiveMatrix = projection.perspective(fov, (float) pixelWidth / pixelHeight, 0.1f, 1000);

        float ndcX = 2f * (30 - ModConfig.INSTANCE.hudOffsetRight) / scaledWidth;
        float ndcY = -2f * (meterY - 30) / scaledHeight;
        perspectiveMatrix.m20(perspectiveMatrix.m20() - ndcX);
        perspectiveMatrix.m21(perspectiveMatrix.m21() - ndcY);

        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        RenderSystem.setProjectionMatrix(perspectiveMatrix, VertexSorter.BY_Z);
        matrices.push();
        matrixStack.push();

        // upper left and lower left
        float x1 = rightEdge - (TEXTURE_WIDTH + (float) RANK_SCALE_MAX_WIDTH_DIFF) * hudScale;
        // lower right and upper right
        float x2 = rightEdge + (float) RANK_SCALE_MAX_WIDTH_DIFF * hudScale;
        // upper left and upper right
        float y1 = meterY - (float) RANK_SCALE_MAX_HEIGHT_DIFF * hudScale;
        // lower left and lower right
        float y2 = meterY + (TEXTURE_HEIGHT + (float) RANK_SCALE_MAX_HEIGHT_DIFF) * hudScale;

        float centerX = (x1 + x2) / 2;
        float centerY = (y1 + y2) / 2;

        float refRightEdge = scaledWidth - 30f;
        float refCenterY = 30f + TEXTURE_HEIGHT * hudScale / 2f;

        matrixStack.loadIdentity();
        float z = scaledHeight / (2 * (float) Math.tan(fov / 2));
        matrices.translate(refRightEdge - ((double) scaledWidth / 2), ((double) scaledHeight / 2) - refCenterY, -z);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ModConfig.INSTANCE.hudRotation));

        matrices.translate(centerX - rightEdge, 0, 0);

        RenderSystem.applyModelViewMatrix();

        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        // upper left
        bufferBuilder.vertex(matrix4f, x1 - centerX, centerY - y1, (float) 0).texture(x1 / scaledWidth, 1 - y1 / scaledHeight).next();
        // lower left
        bufferBuilder.vertex(matrix4f, x1 - centerX, centerY - y2, (float) 0).texture(x1 / scaledWidth, 1 - y2 / scaledHeight).next();
        // lower right
        bufferBuilder.vertex(matrix4f, x2 - centerX, centerY - y2, (float) 0).texture(x2 / scaledWidth, 1 - y2 / scaledHeight).next();
        // upper right
        bufferBuilder.vertex(matrix4f, x2 - centerX, centerY - y1, (float) 0).texture(x2 / scaledWidth, 1 - y1 / scaledHeight).next();
        RenderSystem.disableDepthTest();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        matrices.pop();
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();

        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
