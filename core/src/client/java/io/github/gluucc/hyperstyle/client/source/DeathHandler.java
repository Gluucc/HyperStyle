package io.github.gluucc.hyperstyle.client.source;

import io.github.gluucc.hyperstyle.client.api.*;
import io.github.gluucc.hyperstyle.client.style.*;
import io.github.gluucc.hyperstyle.client.api.*;
import io.github.gluucc.hyperstyle.client.style.StyleMeter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayDeque;

public class DeathHandler {
    private static final ArrayDeque<Integer> recentKills = new ArrayDeque<>();
    private static final int KILLSTREAK_WINDOW = 10;
    private static final int KILL_CREDIT_WINDOW = 200;

    public static void onDeath(LivingEntity target) {
        DamageRecord record = DamageHandler.getLastDamage(target.getId());
        if (record == null) return;

        boolean isMine = record.source().getAttacker() == MinecraftClient.getInstance().player
                || (record.lastPlayerHitTick() >= 0
                && StyleMeter.getCurrentTick() - record.lastPlayerHitTick() < KILL_CREDIT_WINDOW);

        if (isMine) {
            StyleCategory category = DamageClassifier.classify(record);

            if (DamageClassifier.resolveKillEvent(record) != null) {
                StyleMeter.addStyle(DamageClassifier.resolveKillEvent(record), category);
            }

            recentKills.addLast(StyleMeter.getCurrentTick());


            while (!recentKills.isEmpty() && StyleMeter.getCurrentTick() - recentKills.peekFirst() > KILLSTREAK_WINDOW) {
                recentKills.pollFirst();
            }

            if (recentKills.size() == 2) {
                StyleMeter.addStyle(StyleEvents.DOUBLE_KILL, StyleCategories.NONE);
            }

            if (recentKills.size() == 3) {
                StyleMeter.addStyle(StyleEvents.TRIPLE_KILL, StyleCategories.NONE);
            }

            if (recentKills.size() >= 4) {
                StyleMeter.addStyle(StyleEvents.MULTIKILL, StyleCategories.NONE);
            }

            CoreEvents.ADD_KILL_EVENT.invoker().addKillEvent(record, category);
        }
        DamageHandler.removeRecord(target.getId());
    }

    public static void reset() {
        recentKills.clear();
    }
}
