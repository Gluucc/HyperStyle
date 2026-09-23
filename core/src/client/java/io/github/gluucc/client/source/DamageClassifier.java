package io.github.gluucc.client.source;

import io.github.gluucc.client.api.StyleCategories;
import io.github.gluucc.client.api.StyleCategory;
import io.github.gluucc.client.api.StyleEvent;
import io.github.gluucc.client.api.StyleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;

public class DamageClassifier {
    private static final double AIRBORNE_MIN_HEIGHT = 2;

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

        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            return StyleEvents.FRIED;
        }

        if (source.isIn(DamageTypeTags.IS_EXPLOSION) && record.heightAboveGround() > AIRBORNE_MIN_HEIGHT) {
            return StyleEvents.FIREWORKS;
        }

        if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
            return StyleEvents.EXPLODED;
        }

        if (record.healthBefore() >= record.maxHealth() && record.maxHealth() >= 4){
            return StyleEvents.INSTAKILL;
        }

        return StyleEvents.KILL;
    }

    public static StyleEvent resolveHitEvent(DamageRecord record) {
        DamageSource source = record.source();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        // Temporary solution, will make a map with projectile and player state later
        if (player != null && !source.isIndirect() && !player.isOnGround() && player.getVelocity().y < -0.6) {
            return StyleEvents.JUMPSHOT;
        }

        if (record.heightAboveGround() > AIRBORNE_MIN_HEIGHT) {
            return StyleEvents.AIRSHOT;
        }

        return null;

    }
}
