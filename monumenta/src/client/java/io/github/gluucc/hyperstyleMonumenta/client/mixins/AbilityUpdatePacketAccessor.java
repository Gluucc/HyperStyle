package io.github.gluucc.hyperstyleMonumenta.client.mixins;

import ch.njol.unofficialmonumentamod.ChannelHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ChannelHandler.AbilityUpdatePacket.class, remap = false)
public interface AbilityUpdatePacketAccessor {
    @Accessor("name")
    String getName();

    @Accessor("remainingCooldown")
    int getRemainingCooldown();

    @Accessor("remainingCharges")
    int getRemainingCharges();

    @Accessor("initialDuration")
    Integer getInitialDuration();

    @Accessor("remainingDuration")
    Integer getRemainingDuration();
}
