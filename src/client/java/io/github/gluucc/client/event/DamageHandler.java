package io.github.gluucc.client.event;

import io.github.gluucc.client.style.StyleMeter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

import java.util.HashMap;
import java.util.Map;

public class DamageHandler {
    private static final Map<Integer, DamageSource> lastDamage = new HashMap<>();

    public static void onDamaged(LivingEntity target, DamageSource source) {
        lastDamage.put(target.getId(), source);
    }

    public static DamageSource getLastDamage(int entityId) {
        return lastDamage.get(entityId);
    }
}
