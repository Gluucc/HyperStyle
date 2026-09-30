package io.github.gluucc.hyperstyleMonumenta.client.mixins;

import ch.njol.unofficialmonumentamod.AbilityHandler;
import ch.njol.unofficialmonumentamod.ChannelHandler;
import io.github.gluucc.hyperstyleMonumenta.client.source.AbilityCastHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.gluucc.hyperstyleMonumenta.HyperstyleMonumenta;

import java.util.List;

@Mixin(value = AbilityHandler.class, remap = false)
public abstract class AbilityHandlerMixin {

    @Shadow
    @Final
    public List<AbilityHandler.AbilityInfo> abilityData;

    @Inject(method = "updateAbilities", at = @At("HEAD"), remap = false)
    private void hyperstyle_monumenta$trackAbilities(ChannelHandler.ClassUpdatePacket packet, CallbackInfo ci) {
        var abilities = ((ClassUpdatePacketAccessor) packet).getAbilities();

        for (ChannelHandler.ClassUpdatePacket.AbilityInfo ability : abilities) {
            HyperstyleMonumenta.LOGGER.info(ability.name + " " + ability.className);
        }
    }

    @Inject(method = "updateAbility", at = @At("HEAD"), remap = false)
    private void hyperstyle_monumenta$trackAbility(ChannelHandler.AbilityUpdatePacket packet, CallbackInfo ci) {
        for (AbilityHandler.AbilityInfo ability : abilityData) {
            //HyperstyleMonumenta.LOGGER.info(ability.name + " " + ability.initialCooldown + " " + ability.remainingCooldown + " " + ability.mode);
        }

        AbilityCastHandler.onCast(packet, abilityData);
    }

}