package io.github.gluucc.hyperstyleMonumenta.client.source;

import ch.njol.unofficialmonumentamod.ChannelHandler;
import io.github.gluucc.client.api.StyleEvent;
import io.github.gluucc.hyperstyleMonumenta.client.api.StyleEventsMonumenta;
import io.github.gluucc.hyperstyleMonumenta.client.mixins.AbilityUpdatePacketAccessor;

public class AbilityClassifier {
    public static StyleEvent resolveCastEvent(AbilityUpdatePacketAccessor ability) {

        if (ability.getName().equals("Dagger Throw")) {
            return StyleEventsMonumenta.DAGGER_THROW;
        }

        return null;
    }
}
