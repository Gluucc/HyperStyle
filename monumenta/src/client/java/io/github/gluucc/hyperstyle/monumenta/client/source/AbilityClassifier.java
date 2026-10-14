package io.github.gluucc.hyperstyle.monumenta.client.source;

import io.github.gluucc.hyperstyle.client.api.DamageRecord;
import io.github.gluucc.hyperstyle.client.api.StyleCategory;
import io.github.gluucc.hyperstyle.client.api.StyleEvent;
import io.github.gluucc.hyperstyle.client.style.StyleMeter;
import io.github.gluucc.hyperstyle.monumenta.client.api.StyleEventsMonumenta;
import io.github.gluucc.hyperstyle.monumenta.client.mixins.AbilityUpdatePacketAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import java.util.Set;


public class AbilityClassifier {
    private static final int AIRBORNE_MIN_HEIGHT = 2;
    private static final Set<Integer> blacklist = (Set.of(0xAAAAAA, 0x555555, 0xFFFFFF));

    public static StyleEvent resolveCastEvent(AbilityUpdatePacketAccessor ability) {
        return null;
    }

    public static void addBigKill(DamageRecord record, StyleCategory category) {
        Text name = record.mobName();

        if (name == null) return;

        boolean[] isWhitelisted = {false};

        name.asOrderedText().accept(((index, style, codePoint) -> {
            TextColor color = style.getColor();

            if (color == null || blacklist.contains(color.getRgb())) {
                return false;
            }
            isWhitelisted[0] = true;
            return false;
        }));

        if (isWhitelisted[0]) {
            StyleMeter.addStyle(StyleEventsMonumenta.BIG_KILL, category);
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
