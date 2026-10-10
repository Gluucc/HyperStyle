package io.github.gluucc.client.source;

import io.github.gluucc.client.api.*;
import io.github.gluucc.client.style.StyleMeter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.DamageTypeTags;

public class DamageClassifier {
    private static final double AIRBORNE_MIN_HEIGHT = 2;
    private static final double INSTAKILL_MIN_HEALTH = 4;

    public static StyleCategory classify(DamageRecord record) {
        DamageSource source = record.source();

        if (source.isIn(DamageTypeTags.IS_FIRE) || source.isIn(DamageTypeTags.IS_EXPLOSION) || source.isIn(DamageTypeTags.IS_LIGHTNING) || source.isIn(DamageTypeTags.IS_FALL) || source.isIn(DamageTypeTags.IS_FREEZING) || source.isIn(DamageTypeTags.IS_DROWNING)) {
            return StyleCategories.ENVIRONMENT;
        }

        if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
            return StyleCategories.RANGED;
        }

        // This method will get changed to isDirect() in 1.21+
        return source.isIndirect() ? StyleCategories.NONE : StyleCategories.MELEE;
    }

    public static StyleEvent resolveKillEvent(DamageRecord record) {
        DamageSource source = record.source();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (source.getAttacker() instanceof MobEntity) {
            return StyleEvents.FRIENDLY_FIRE;
        }

        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            return StyleEvents.FRIED;
        }

        if (source.isIn(DamageTypeTags.IS_EXPLOSION) && record.heightAboveGround() > AIRBORNE_MIN_HEIGHT) {
            return StyleEvents.FIREWORKS;
        }

        if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
            return StyleEvents.EXPLODED;
        }

        if (source.isIn(DamageTypeTags.IS_FALL)) {
            return StyleEvents.SPLATTERED;
        }

        // Temporary solution, will make a map with projectile and player state later
        if (player != null && source.isIndirect() && !player.isOnGround() && player.getVelocity().y < -0.6) {
            return StyleEvents.JUMPSHOT;
        }

        if (source.isIndirect() && record.heightAboveGround() > AIRBORNE_MIN_HEIGHT) {
            return StyleEvents.AIRSHOT;
        }

        return null;
    }

    public static StyleEvent resolveHitEvent(DamageRecord record) {
        return null;
    }

    public static void addKill(DamageRecord record, StyleCategory category) {
        if (record.healthBefore() >= record.maxHealth() && record.maxHealth() >= INSTAKILL_MIN_HEALTH){
            StyleMeter.addStyle(StyleEvents.INSTAKILL, category);
        } else {
            StyleMeter.addStyle(StyleEvents.KILL, category);
        }
    }
}
