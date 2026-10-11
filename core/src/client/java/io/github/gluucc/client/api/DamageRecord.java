package io.github.gluucc.client.api;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;

public record DamageRecord(DamageSource source, int tick, int lastPlayerHitTick, float healthBefore, float maxHealth, Text mobName, double heightAboveGround, boolean isOnFire) {}
