package io.github.gluucc.client.mixin;

import io.github.gluucc.client.event.DamageHandler;
import io.github.gluucc.client.event.DeathHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "onDamaged", at = @At("TAIL"))
    private void hyperstyle$trackDamage(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        DamageHandler.onDamaged(entity, source);
    }

    @Inject(method = "handleStatus", at = @At("TAIL"))
    private void hyperstyle$trackDeath(byte status, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (status == 3) {
            DeathHandler.onDeath(entity);
        }
    }
}
