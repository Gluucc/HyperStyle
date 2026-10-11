package io.github.gluucc.hyperstyle.monumenta.client.api;

import io.github.gluucc.hyperstyle.client.api.StyleEvent;
import io.github.gluucc.hyperstyle.client.api.StyleEventRegistry;
import io.github.gluucc.hyperstyle.monumenta.HyperstyleMonumenta;

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

    public static final StyleEvent BIG_KILL = register("big_kill", "BIG KILL", 150,0xFFFFFFFF);
    public static final StyleEvent GROUND_SLAM = register("ground_slam", "GROUND SLAM", 70, 0xFFFFFFFF);
    public static final StyleEvent AIR_SLAM = register("air_slam", "AIR SLAM", 160, 0xFF00FFFF);
}
