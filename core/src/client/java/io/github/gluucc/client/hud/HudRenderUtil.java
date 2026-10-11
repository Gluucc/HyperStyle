package io.github.gluucc.client.hud;

import io.github.gluucc.client.style.StyleMeter;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Random;

public class HudRenderUtil {
    private static final Random RANDOM = new Random();

    public static void applyHudShakeRand(MatrixStack matrices, float intensity) {

        float offsetX = ((RANDOM.nextFloat()) * 2.0f - 1.0f) * intensity;
        float offsetY = ((RANDOM.nextFloat()) * 2.0f - 1.0f) * intensity;

        matrices.translate(offsetX, offsetY, 0.0f);
    }

    public static void applyHudShakeSin(MatrixStack matrices, float tickDelta, float intensity) {
        int time = StyleMeter.getCurrentTick();
        float shakeTime = (float) time + tickDelta;

        float frequencyMultiplier = 2.5f;

        float offsetX = (float) Math.sin(shakeTime * frequencyMultiplier) * intensity;
        float offsetY = (float) Math.sin(shakeTime * frequencyMultiplier) * intensity;

        matrices.translate(offsetX, offsetY, 0.0f);
    }
}
