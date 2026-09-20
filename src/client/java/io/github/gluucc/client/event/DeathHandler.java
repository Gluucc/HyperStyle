package io.github.gluucc.client.event;

import io.github.gluucc.client.style.StyleCategory;
import io.github.gluucc.client.style.StyleEvent;
import io.github.gluucc.client.style.StyleMeter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayDeque;

public class DeathHandler {
    private static final ArrayDeque<Integer> recentKills = new ArrayDeque<>();
    private static final int KILLSTREAK_WINDOW = 20;
    private static final int KILL_CREDIT_WINDOW = 1000;

    public static void onDeath(LivingEntity target) {
        DamageRecord record = DamageHandler.getLastDamage(target.getId());
        if (record == null) return;

        boolean isMine = record.source().getAttacker() == MinecraftClient.getInstance().player
                || (record.lastPlayerHitTick() >= 0
                && StyleMeter.getCurrentTick() - record.lastPlayerHitTick() < KILL_CREDIT_WINDOW);

        if (isMine) {
            StyleCategory category = DamageClassifier.classify(record);

            StyleMeter.addStyle(DamageClassifier.resolveKillEvent(record), category);

            recentKills.addLast(StyleMeter.getCurrentTick());


            while(!recentKills.isEmpty() && StyleMeter.getCurrentTick() - recentKills.peekFirst() > KILLSTREAK_WINDOW) {
                recentKills.pollFirst();
            }

            if (recentKills.size() == 2) {
                StyleMeter.addStyle(StyleEvent.DOUBLE_KILL, StyleCategory.NONE);
            }

            if (recentKills.size() == 3) {
                StyleMeter.addStyle(StyleEvent.TRIPLE_KILL, StyleCategory.NONE);
            }

            if (recentKills.size() >= 4) {
                StyleMeter.addStyle(StyleEvent.MULTIKILL, StyleCategory.NONE);
            }
        }
    }
}
