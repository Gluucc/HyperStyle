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
}
