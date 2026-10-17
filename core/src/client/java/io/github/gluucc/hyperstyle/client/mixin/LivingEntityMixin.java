package io.github.gluucc.hyperstyle.client.mixin;

import io.github.gluucc.hyperstyle.client.source.DamageHandler;
import io.github.gluucc.hyperstyle.client.source.DeathHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Shadow
    @Final
    private static TrackedData<Float> HEALTH;


    @Inject(method = "onDamaged", at = @At("HEAD"))
    private void hyperstyle$trackDamage(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        DamageHandler.onDamaged(entity, source);
    }

    @Inject(method = "onTrackedDataSet", at = @At("HEAD"))
    private void hyperstyle$trackHealth(TrackedData<?> data, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (data == HEALTH) {
            DamageHandler.onHealthChanged(entity);
        }

        // to-do: make an accessor for the ABSORPTION field in PlayerEntity and
        // add the 'data == ABSORPTION' to the upper condition while removing the bottom one
        if (entity == MinecraftClient.getInstance().player) {
            DamageHandler.onHealthChanged(entity);
        }
    }

    @Inject(method = "handleStatus", at = @At("TAIL"))
    private void hyperstyle$trackDeath(byte status, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (status == 3) {
            DeathHandler.onDeath(entity);
        }
    }
}
