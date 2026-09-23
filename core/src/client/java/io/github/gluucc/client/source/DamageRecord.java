package io.github.gluucc.client.source;

import net.minecraft.entity.damage.DamageSource;

public record DamageRecord(DamageSource source, int tick, int lastPlayerHitTick, float healthBefore, float maxHealth, double heightAboveGround) {}
