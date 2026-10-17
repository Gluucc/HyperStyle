package io.github.gluucc.hyperstyle.client.source;

import io.github.gluucc.hyperstyle.client.api.DamageRecord;
import io.github.gluucc.hyperstyle.client.api.StyleEvent;
import io.github.gluucc.hyperstyle.client.style.DamageEntry;
import io.github.gluucc.hyperstyle.client.style.StyleMeter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

public class DamageHandler {
    private static final Map<Integer, TargetState> lastDamage = new HashMap<>();
    private static final int MAX_RAYCAST = 5;
    private static float lastHealth = -1;
    private static final ArrayDeque<DamageEntry> damageEntries = StyleMeter.getDamageEntries();

    public static void onDamaged(LivingEntity target, DamageSource source) {
        TargetState state = lastDamage.get(target.getId());
        DamageRecord previous = (state == null) ? null : state.record;
        int lastPlayerHit = -1;
        if (source.getAttacker() == MinecraftClient.getInstance().player) {
            lastPlayerHit = StyleMeter.getCurrentTick();
        } else if (previous != null) {
            lastPlayerHit = previous.lastPlayerHitTick();
        }

        DamageRecord record = new DamageRecord(
                source,
                StyleMeter.getCurrentTick(),
                lastPlayerHit,
                target.getHealth(),
                target.getMaxHealth(),
                target.getCustomName(),
                heightAboveGround(target),
                target.isOnFire()
        );

        if (state == null) {
            lastDamage.put(target.getId(), new TargetState(record, target.getHealth() + target.getAbsorptionAmount()));
        } else {
            state.record = record;
        }

        if (source.getAttacker() == MinecraftClient.getInstance().player) {
            StyleEvent hit = DamageClassifier.resolveHitEvent(record);
            if (hit != null) {
                StyleMeter.addStyle(hit, DamageClassifier.classify(record));
            }
        }
    }

    public static void onHealthChanged(LivingEntity entity) {
        LivingEntity player = MinecraftClient.getInstance().player;

        if (entity == player) {
            float currentHealth = entity.getHealth() + entity.getAbsorptionAmount();

            if (lastHealth < 0) {
                lastHealth = currentHealth;
                return;
            }

            if (currentHealth < lastHealth) {
                float damage = lastHealth - currentHealth;
                StyleMeter.penalize(damage * 10);
            }

            lastHealth = currentHealth;
        }

        TargetState state = lastDamage.get(entity.getId());
        if (state != null) {
            float currentHealth = entity.getHealth() + entity.getAbsorptionAmount();
            if (entity != player && state.record.source().getAttacker() == player) {

                if (currentHealth < state.currentHealth) {
                    float damage = currentHealth - state.currentHealth;
                    damageEntries.addFirst(new DamageEntry(damage, StyleMeter.getCurrentTick()));
                }

            }
            state.currentHealth = currentHealth;
        }
    }

    public static DamageRecord getLastDamage(int entityId) {
        TargetState state = lastDamage.get(entityId);
        return (state == null) ? null : state.record;
    }

    public static void removeRecord(int entityId) {
        lastDamage.remove(entityId);
    }

    public static void reset() {
        lastDamage.clear();
    }

    private static double heightAboveGround(LivingEntity target) {
        Vec3d start = target.getPos();
        Vec3d end = start.subtract(0, MAX_RAYCAST, 0);
        BlockHitResult hit = target.getWorld().raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                target
        ));

        if (hit.getType() == HitResult.Type.MISS) {
            return MAX_RAYCAST;
        }

        return start.y - hit.getPos().y;
    }
}

class TargetState {
    public DamageRecord record;
    public float currentHealth;

    public TargetState(DamageRecord record, float currentHealth) {
        this.record = record;
        this.currentHealth = currentHealth;
    }
}
