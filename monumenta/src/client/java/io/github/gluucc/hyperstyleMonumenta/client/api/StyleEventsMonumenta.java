package io.github.gluucc.hyperstyleMonumenta.client.api;

import io.github.gluucc.client.api.StyleEvent;
import io.github.gluucc.client.api.StyleEventRegistry;
import io.github.gluucc.hyperstyleMonumenta.HyperstyleMonumenta;

public class StyleEventsMonumenta {
    private static StyleEvent register(String path, String label, int points, int color) {
        StyleEvent styleEvent = new StyleEvent(
                HyperstyleMonumenta.id(path),
                label,
                points,
                color
        );

        StyleEventRegistry.register(styleEvent);
        return styleEvent;
    }

    public static void init() {}

    public static final StyleEvent DAGGER_THROW = register("dagger_throw", "DAGGER THROW", 45, 0xFFFFFFFF);
    public static final StyleEvent BIG_KILL = register("big_kill", "BIG KILL", 150,0xFFFFFFFF);
}
