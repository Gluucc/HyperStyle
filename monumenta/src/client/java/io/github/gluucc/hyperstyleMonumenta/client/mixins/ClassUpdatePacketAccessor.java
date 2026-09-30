package io.github.gluucc.hyperstyleMonumenta.client.mixins;

import ch.njol.unofficialmonumentamod.ChannelHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ChannelHandler.ClassUpdatePacket.class, remap = false)
public interface ClassUpdatePacketAccessor {
    @Accessor("abilities")
    ChannelHandler.ClassUpdatePacket.AbilityInfo[] getAbilities();
}
