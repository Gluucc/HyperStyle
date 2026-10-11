package io.github.gluucc.hyperstyleMonumenta.client.source;

import io.github.gluucc.client.api.DamageRecord;
import io.github.gluucc.client.api.StyleCategory;
import io.github.gluucc.client.api.StyleEvent;
import io.github.gluucc.client.style.StyleMeter;
import io.github.gluucc.hyperstyleMonumenta.client.api.StyleEventsMonumenta;
import io.github.gluucc.hyperstyleMonumenta.client.mixins.AbilityUpdatePacketAccessor;
import net.minecraft.text.Text;


public class AbilityClassifier {
    public static StyleEvent resolveCastEvent(AbilityUpdatePacketAccessor ability) {

        if (ability.getName().equals("Dagger Throw")) {
            return StyleEventsMonumenta.DAGGER_THROW;
        }

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
}
