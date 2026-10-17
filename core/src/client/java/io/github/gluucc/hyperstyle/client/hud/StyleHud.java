package io.github.gluucc.hyperstyle.client.hud;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import io.github.gluucc.hyperstyle.HyperStyle;
import io.github.gluucc.hyperstyle.client.config.ModConfig;
import io.github.gluucc.hyperstyle.client.style.DpsLevel;
import io.github.gluucc.hyperstyle.client.style.StyleEntry;
import io.github.gluucc.hyperstyle.client.style.StyleMeter;
import io.github.gluucc.hyperstyle.client.style.StyleRank;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Collection;

public class StyleHud {
    public enum HudMode {
        DEFAULT,  // Auto: hidden for UNRANKED, shown otherwise
        ON,
        OFF
    }

    private static SimpleFramebuffer customBuffer;

    static final Identifier TEXTURE = HyperStyle.id("textures/gui/test3.png");

    static final Identifier FONT_12 = HyperStyle.id("vcr_12");
    static final Identifier FONT_14 = HyperStyle.id("vcr_14");
    static final Identifier FONT_16 = HyperStyle.id("vcr_16");
    static final Identifier FONT_18 = HyperStyle.id("vcr_18");
    static final Identifier FONT_22 = HyperStyle.id("vcr_22");
    static final Identifier FONT_26 = HyperStyle.id("vcr_26");

    static final int FONT_12_HEIGHT = 9;
    static final int FONT_16_HEIGHT = 12;
    static final int FONT_18_HEIGHT = 14;
    static final int FONT_14_HEIGHT = 10;
    static final int FONT_22_HEIGHT = 17;
    static final int FONT_26_HEIGHT = 20;

    public static final int TEXTURE_HEIGHT = 275;
    public static final int TEXTURE_WIDTH = 192;

    static final int PARALLELOGRAM_BAR_HEIGHT = 19;
    static final int PARALLELOGRAM_BAR_GAP = 1;
    static final int PARALLELOGRAM_SKEW = 8;

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

        int currentRankWidth = Math.round(RANK_WIDTH * rankScale);
        int currentRankHeight = Math.round(RANK_HEIGHT * rankScale);

        int rankX = meterX + (TEXTURE_WIDTH - currentRankWidth) / 2;
        int rankY = meterY + (RANK_HEIGHT - currentRankHeight) / 2;

        MatrixStack rankMatrices = context.getMatrices();
        rankMatrices.push();

        if (StyleMeter.getRankShakeTicks() > 0) {
            HudRenderUtil.applyHudShakeSin(rankMatrices, tickDelta, 10.0f);
        }

        context.drawTexture(currentRank.getTexture(), rankX, rankY, 0, 0, currentRankWidth, currentRankHeight, currentRankWidth, currentRankHeight);

        rankMatrices.pop();

        int offsetY = RANK_HEIGHT;

        RenderSystem.disableBlend();

        double percent = StyleMeter.getRankPercent();
        int filledWidth = (int) (TEXTURE_WIDTH * (percent / 100.0));

        context.fill(meterX, meterY + offsetY, meterX + TEXTURE_WIDTH, meterY + 10 + offsetY, 0xFF000000);

        if (filledWidth > 0) {
            context.fill(meterX, meterY + offsetY, meterX + filledWidth, meterY + 10 + offsetY, 0xFFFFFFFF);
        }

        float parallelogramBarX1 = (meterX + (float) TEXTURE_WIDTH / 2) + 1;
        float parallelogramBarY1 = meterY + TEXTURE_HEIGHT + PARALLELOGRAM_BAR_GAP * 2;
        float parallelogramBarX2 = meterX + TEXTURE_WIDTH;
        float parallelogramBarY2 = meterY + PARALLELOGRAM_BAR_HEIGHT + TEXTURE_HEIGHT + PARALLELOGRAM_BAR_GAP * 2;

        float parallelogramBarLeftEdge = parallelogramBarX1 + PARALLELOGRAM_BAR_GAP;
        float parallelogramWidth = parallelogramBarX2 - parallelogramBarLeftEdge;
        float rightParallelogramCenter = ((parallelogramBarX2 + parallelogramBarX1 + PARALLELOGRAM_BAR_GAP) / 2);

        offsetY += 20;

        Style customFont12 = Style.EMPTY.withFont(FONT_12);
        Style customFont14 = Style.EMPTY.withFont(FONT_14);
        Style customFont16 = Style.EMPTY.withFont(FONT_16);
        Style customFont18 = Style.EMPTY.withFont(FONT_18);
        Style customFont22 = Style.EMPTY.withFont(FONT_22);
        Style customFont26 = Style.EMPTY.withFont(FONT_26);

        int largeLineSpacing = 22;

        for (StyleEntry entry : styleList) {
            MutableText entryText = Text.empty().fillStyle(customFont18);
            entryText.append(Text.literal("+ ").formatted(Formatting.WHITE));
            entryText.append(Text.literal(entry.event().label()));
            if (entry.count() > 1) {
                entryText.append(Text.literal(" x" + entry.count()).formatted(Formatting.WHITE));
            }

            context.drawText(renderer, entryText, meterX, meterY + offsetY, entry.event().color(), false);
            offsetY += largeLineSpacing;
        }

        int offsetBottom = 5;

        HudRenderUtil.drawParallelogram(context, meterX, parallelogramBarY1, parallelogramBarX1 - PARALLELOGRAM_BAR_GAP, parallelogramBarY2, -PARALLELOGRAM_SKEW, 0, DpsLevel.BLACK.getColor());

        String freshnessStr = "FRESH: " + String.format("%.2f", StyleMeter.getFreshness());
        MutableText freshnessText = Text.literal(freshnessStr).setStyle(customFont12);
        int freshnessTextWidth = renderer.getWidth(freshnessText.asOrderedText());
        context.drawText(renderer, freshnessText, (((meterX + (int) parallelogramBarX1 - PARALLELOGRAM_BAR_GAP) / 2) - freshnessTextWidth / 2) - PARALLELOGRAM_SKEW / 4, (int) parallelogramBarY1 + (PARALLELOGRAM_BAR_HEIGHT - FONT_12_HEIGHT) / 2, 0xFFFFFFFF, false);

        DpsLevel dpsLevel = StyleMeter.getCurrentDpsLevel();

        float smoothEffectiveDps = MathHelper.lerp(tickDelta, StyleMeter.getPrevEffectiveDps(), StyleMeter.getEffectiveDps());

        percent = StyleMeter.getDpsLevelPercent(smoothEffectiveDps);
        float filledDpsWidth = (float) (parallelogramBarX2 - (parallelogramWidth + PARALLELOGRAM_SKEW) * (percent / 100.0)) + PARALLELOGRAM_SKEW;

        Matrix4f pos = matrices.peek().getPositionMatrix();

        Vector3f dpsBarBottomLeftPoint = new Vector3f(parallelogramBarLeftEdge - PARALLELOGRAM_SKEW - 10, parallelogramBarY2 + PARALLELOGRAM_SKEW, 0);
        Vector3f dpsBarUpperRightPoint = new Vector3f(parallelogramBarX2, parallelogramBarY1 - PARALLELOGRAM_SKEW, 0);

        dpsBarBottomLeftPoint = pos.transformPosition(dpsBarBottomLeftPoint);
        dpsBarUpperRightPoint = pos.transformPosition(dpsBarUpperRightPoint);

        context.enableScissor((int) dpsBarBottomLeftPoint.x, (int) dpsBarUpperRightPoint.y, (int) dpsBarUpperRightPoint.x, (int) dpsBarBottomLeftPoint.y);

        HudRenderUtil.drawParallelogram(context, parallelogramBarLeftEdge, parallelogramBarY1, parallelogramBarX2 + PARALLELOGRAM_SKEW, parallelogramBarY2, 0, -PARALLELOGRAM_SKEW, dpsLevel.getColor());
        HudRenderUtil.drawParallelogram(context, filledDpsWidth, parallelogramBarY1, parallelogramBarX2 + PARALLELOGRAM_SKEW, parallelogramBarY2, 0, -PARALLELOGRAM_SKEW, dpsLevel.getNextLevel().getColor());
        context.disableScissor();

        float smoothDps = MathHelper.lerp(tickDelta, StyleMeter.getPrevDps(), StyleMeter.getCurrentDps());
        float steppedDps = Math.round(smoothDps * 100.0f) / 100.0f;

        String dpsStr = "DPS: " + (int) steppedDps;
        MutableText dpsText = Text.literal(dpsStr).setStyle(customFont16);
        int dpsTextWidth = renderer.getWidth(dpsText.asOrderedText());

        context.drawText(renderer, dpsText, (((int) rightParallelogramCenter - dpsTextWidth / 2) - PARALLELOGRAM_SKEW / 4) + 1, ((int) parallelogramBarY1 + (PARALLELOGRAM_BAR_HEIGHT - FONT_16_HEIGHT) / 2) + 1, 0xCC000000, false);
        context.drawText(renderer, dpsText, ((int) rightParallelogramCenter - dpsTextWidth / 2) - PARALLELOGRAM_SKEW / 4, (int) parallelogramBarY1 + (PARALLELOGRAM_BAR_HEIGHT - FONT_16_HEIGHT) / 2, 0xFFFFFFFF, false);

        MutableText multiplierText = Text.empty().fillStyle(customFont14);
        multiplierText.append(Text.literal("MULTIPLIER").setStyle(customFont14));

        float smoothMultiplier = MathHelper.lerp(tickDelta, StyleMeter.getPrevAirborneMultiplier(), StyleMeter.getCurrentAirborneMultiplier());
        float steppedMultiplier = Math.round(smoothMultiplier * 100.0f) / 100.0f;
        String multiplierToDisplay = String.format(java.util.Locale.US, "%.2f", steppedMultiplier);

        int currentMultiplierFontHeight = FONT_18_HEIGHT;

        MutableText multiplierMultiplierText = Text.empty().fillStyle(customFont18);

        if (steppedMultiplier >= 2.00f) {
            multiplierMultiplierText = Text.empty().fillStyle(customFont22).withColor(0xFFFF8C00);
            currentMultiplierFontHeight = FONT_22_HEIGHT;
            HudRenderUtil.applyHudShakeRand(matrices, 2.0f);
        }

        if (steppedMultiplier >= 3.00f) {
            multiplierMultiplierText = Text.empty().fillStyle(customFont26).withColor(0xFFFF0000);
            currentMultiplierFontHeight = FONT_26_HEIGHT;
            HudRenderUtil.applyHudShakeRand(matrices, 3.0f);
        }
        multiplierMultiplierText.append(Text.literal("x" + multiplierToDisplay));

        int multiplierMultiplierTextWidth = renderer.getWidth(multiplierMultiplierText.asOrderedText());

        context.drawText(renderer, multiplierText, meterX, meterY + TEXTURE_HEIGHT - FONT_14_HEIGHT - offsetBottom, 0xFFFFFFFF, false);
        context.drawText(renderer, multiplierMultiplierText, meterX + TEXTURE_WIDTH - multiplierMultiplierTextWidth, meterY + TEXTURE_HEIGHT - currentMultiplierFontHeight - offsetBottom, 0xFFFFFFFF, false);

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
        HudRenderUtil.applyHudSway(perspectiveMatrix);

        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        RenderSystem.setProjectionMatrix(perspectiveMatrix, VertexSorter.BY_Z);
        matrices.push();
        matrixStack.push();

        // upper left and lower left
        float x1 = rightEdge - (TEXTURE_WIDTH + (float) RANK_SCALE_MAX_WIDTH_DIFF) * hudScale;
        // lower right and upper right
        float x2 = rightEdge + (float) RANK_SCALE_MAX_WIDTH_DIFF * hudScale;
        // upper left and upper right
        float y1 = meterY - ((float) RANK_SCALE_MAX_HEIGHT_DIFF + PARALLELOGRAM_BAR_HEIGHT + PARALLELOGRAM_BAR_GAP) * hudScale;
        // lower left and lower right
        float y2 = meterY + (TEXTURE_HEIGHT + (float) RANK_SCALE_MAX_HEIGHT_DIFF + PARALLELOGRAM_BAR_HEIGHT + PARALLELOGRAM_BAR_GAP) * hudScale;

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
