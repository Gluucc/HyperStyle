package io.github.gluucc.hyperstyle.client.api;

import net.minecraft.util.Identifier;

public record StyleEvent(Identifier id, String label, int points, int color) {}
