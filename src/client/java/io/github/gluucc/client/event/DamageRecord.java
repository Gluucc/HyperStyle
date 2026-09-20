package io.github.gluucc.client.event;

import net.minecraft.entity.damage.DamageSource;

public record DamageRecord(DamageSource source, int tick, int lastPlayerHitTick, float healthBefore, float maxHealth, double heightAboveGround) {}
