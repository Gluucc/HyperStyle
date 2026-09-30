package io.github.gluucc.hyperstyleMonumenta.client.source;

import ch.njol.unofficialmonumentamod.AbilityHandler;
import ch.njol.unofficialmonumentamod.ChannelHandler;
import io.github.gluucc.client.api.StyleEvent;
import io.github.gluucc.client.style.StyleMeter;
import io.github.gluucc.hyperstyleMonumenta.HyperstyleMonumenta;
import io.github.gluucc.hyperstyleMonumenta.client.api.StyleCategoriesMonumenta;
import io.github.gluucc.hyperstyleMonumenta.client.mixins.AbilityUpdatePacketAccessor;

import java.util.List;

public class AbilityCastHandler {
    public static void onCast(ChannelHandler.AbilityUpdatePacket packet, List<AbilityHandler.AbilityInfo> abilityData) {
        var ability = ((AbilityUpdatePacketAccessor) packet);

        for (AbilityHandler.AbilityInfo prevAbility : abilityData) {
            if (ability.getName().equals(prevAbility.name)) {
                if (ability.getRemainingCharges() < prevAbility.charges || prevAbility.remainingCooldown == 0 && ability.getRemainingCooldown() != 0)  {
                    HyperstyleMonumenta.LOGGER.info(ability.getName() + " WAS USED." + " REM COOLDOWN: " + ability.getRemainingCooldown() + " | REM CHARGES: " + ability.getRemainingCharges() + " | REM DURATION: " + ability.getRemainingDuration() + " | INIT DURATION: " + ability.getInitialDuration());

                    StyleEvent cast = AbilityClassifier.resolveCastEvent(ability);
                    if (cast != null) {
                        StyleMeter.addStyle(cast, StyleCategoriesMonumenta.ABILITY);
                    }
                }
            }
        }
    }
}
