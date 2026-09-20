package io.github.gluucc.client.event;

import io.github.gluucc.client.style.StyleEvent;
import io.github.gluucc.client.style.StyleMeter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.HashMap;
import java.util.Map;

public class DamageHandler {
    private static final Map<Integer, DamageRecord> lastDamage = new HashMap<>();
    private static final int MAX_RAYCAST = 5;

    public static void onDamaged(LivingEntity target, DamageSource source) {
        DamageRecord previous = lastDamage.get(target.getId());
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
                heightAboveGround(target)
        );
        lastDamage.put(target.getId(), record);

        if (source.getAttacker() == MinecraftClient.getInstance().player) {
            StyleEvent hit = DamageClassifier.resolveHitEvent(record);
            if (hit != null) {
                StyleMeter.addStyle(hit, DamageClassifier.classify(record));
            }
        }
    }

    public static DamageRecord getLastDamage(int entityId) {
        return lastDamage.get(entityId);
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
