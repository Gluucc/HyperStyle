package io.github.gluucc.hyperstyleMonumenta.client.source;

import io.github.gluucc.client.api.DamageRecord;
import io.github.gluucc.client.api.StyleCategory;
import io.github.gluucc.client.api.StyleEvent;
import io.github.gluucc.client.style.StyleMeter;
import io.github.gluucc.hyperstyleMonumenta.client.api.StyleEventsMonumenta;
import io.github.gluucc.hyperstyleMonumenta.client.mixins.AbilityUpdatePacketAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;


public class AbilityClassifier {
    private static final int AIRBORNE_MIN_HEIGHT = 2;

    public static StyleEvent resolveCastEvent(AbilityUpdatePacketAccessor ability) {
        return null;
    }

    public static void addBigKill(DamageRecord record, StyleCategory category) {
        Text name = record.mobName();
        if (name != null) {
            String rawString = name.getString();

            if (rawString.contains("§")) {
                int index = rawString.indexOf("§");
                if (index + 1 < rawString.length()) {
                    char colorCode = rawString.charAt(index + 1);
                    if (colorCode == '6') {
                        StyleMeter.addStyle(StyleEventsMonumenta.BIG_KILL, category);
                    }
                }
            }
        }
    }

    public static void addGroundSlam(DamageRecord record, StyleCategory category) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        DamageSource source = record.source();

        if (player != null && !source.isIndirect() && player.getVelocity().y < -0.6) {
            StyleMeter.addStyle(StyleEventsMonumenta.GROUND_SLAM, category);
        }

        if (player != null && !source.isIndirect() && record.heightAboveGround() > AIRBORNE_MIN_HEIGHT && player.getVelocity().y < -0.6) {
            StyleMeter.addStyle(StyleEventsMonumenta.AIR_SLAM, category);
        }
    }
}
