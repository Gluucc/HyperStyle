package io.github.gluucc.client.style;

import net.minecraft.util.Identifier;

public record StyleEvent(Identifier id, String label, int points, int color) {}
