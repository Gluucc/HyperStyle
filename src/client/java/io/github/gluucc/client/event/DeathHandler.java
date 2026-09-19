package io.github.gluucc.client.event;

import io.github.gluucc.client.style.StyleMeter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

import java.util.ArrayDeque;

public class DeathHandler {
    private static final ArrayDeque<Integer> recentKills = new ArrayDeque<>();
    private static final int KILLSTREAK_WINDOW = 20;

    public static void onDeath(LivingEntity target) {
        DamageSource source = DamageHandler.getLastDamage(target.getId());

        if (source != null && source.getAttacker() == MinecraftClient.getInstance().player) {
            StyleMeter.addStyle("KILL", 45, 0xFFFFFFFF);

            recentKills.addLast(StyleMeter.getCurrentTick());

            while(!recentKills.isEmpty() && StyleMeter.getCurrentTick() - recentKills.peekFirst() > KILLSTREAK_WINDOW) {
                recentKills.pollFirst();
            }

            if (recentKills.size() == 2) {
                StyleMeter.addStyle("DOUBLE KILL", 25, 0xFFFFA500);
            }

            if (recentKills.size() == 3) {
                StyleMeter.addStyle("TRIPLE KILL", 50, 0xFFFFA500);
            }

            if (recentKills.size() >= 4) {
                StyleMeter.addStyle("MULTIKILL", 100, 0xFFFFA500);
            }
        }


    }
}
