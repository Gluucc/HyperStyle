package io.github.gluucc.hyperstyle.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.gluucc.hyperstyle.client.config.ModConfig;
import io.github.gluucc.hyperstyle.client.style.StyleMeter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Random;

public class HudRenderUtil {
    private static final Random RANDOM = new Random();
    private static float renderX;
    private static float renderY;
    private static float renderZ;

    public static void applyHudShakeRand(MatrixStack matrices, float intensity) {

        float offsetX = ((RANDOM.nextFloat()) * 2.0f - 1.0f) * intensity;
        float offsetY = ((RANDOM.nextFloat()) * 2.0f - 1.0f) * intensity;

        matrices.translate(offsetX, offsetY, 0.0f);
    }

    public static void applyHudShakeSin(MatrixStack matrices, float tickDelta, float intensity) {
        float currentTicks = (float) StyleMeter.getRankShakeTicks();
        float maxTicks = (float) StyleMeter.getMaxRankShakeTicks();

        float smoothRankShakeTicks = Math.max(0, currentTicks - tickDelta);
        float linearProgress = smoothRankShakeTicks / maxTicks;

        float fadeMultiplier = (float) Math.pow(linearProgress, 3);

        float dynamicIntensity = intensity * fadeMultiplier;

        int time = StyleMeter.getCurrentTick();
        float shakeTime = (float) time + tickDelta;
        float frequencyMultiplier = 3.5f;

        float offsetX = (float) Math.sin(shakeTime * frequencyMultiplier) * dynamicIntensity;
        float offsetY = (float) Math.sin((shakeTime + 5.0f) * (frequencyMultiplier * 1.2f)) * dynamicIntensity;

        matrices.translate(offsetX, offsetY, 0.0f);
    }

    public static void applyHudSway(Matrix4f matrices) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        MinecraftClient client = MinecraftClient.getInstance();
        if (player != null) {
            float prevRenderX = renderX;
            float prevRenderY = renderY;
            float prevRenderZ = renderZ;

            float dt = client.getLastFrameDuration();
            float lerpFrameIndependent = 1 - (float) Math.pow(2.0f, -dt / (4 * ModConfig.INSTANCE.hudSwayCatchupDelay));
            Vec3d velocity = player.getVelocity();
            Vec3d localVelocity = velocity.rotateY((float) Math.toRadians(player.getYaw()));

            float x = MathHelper.clamp((float) localVelocity.x * 200 * ModConfig.INSTANCE.hudSwayIntensity, -50f, 50f);
            float y = MathHelper.clamp((float) localVelocity.y * 40 * ModConfig.INSTANCE.hudSwayIntensity, -50f, 50f);
            float z = MathHelper.clamp((float) localVelocity.z * 200 * ModConfig.INSTANCE.hudSwayIntensity, -50f, 50f);

            renderX = MathHelper.lerp(lerpFrameIndependent, prevRenderX, x);
            renderY = MathHelper.lerp(lerpFrameIndependent, prevRenderY, y);
            renderZ = MathHelper.lerp(lerpFrameIndependent, prevRenderZ, z);

            if (!Float.isFinite(renderX) || !Float.isFinite(renderY) || !Float.isFinite(renderZ)) {
                renderX = 0;
                renderY = 0;
                renderZ = 0;
            }

            matrices.translate(renderX, -renderY, renderZ);
        } else {
            renderX = 0;
            renderY = 0;
            renderZ = 0;
        }
    }

    public static void drawParallelogram(DrawContext context, float x1, float y1, float x2, float y2, int bottomRightVertexOffset, int bottomLeftVertexOffset, int color) {
        Matrix4f matrices = context.getMatrices().peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrices, x1, y1, 0).color(color).next();
        buffer.vertex(matrices, x1 + bottomLeftVertexOffset, y2, 0).color(color).next();
        buffer.vertex(matrices, x2 + bottomRightVertexOffset, y2, 0).color(color).next();
        buffer.vertex(matrices, x2, y1, 0).color(color).next();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        tessellator.draw();
    }
}
