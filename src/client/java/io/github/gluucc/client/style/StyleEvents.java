package io.github.gluucc.client.style;

import io.github.gluucc.HyperStyle;

public class StyleEvents {
    private static StyleEvent register(String path, String label, int points, int color) {
        StyleEvent styleEvent = new StyleEvent(
                HyperStyle.id(path),
                label,
                points,
                color
        );

        StyleEventRegistry.register(styleEvent);
        return styleEvent;
    }

    public static void init() {}

    public static final StyleEvent KILL = register("kill", "KILL", 45, 0xFFFFFFFF);
    public static final StyleEvent DOUBLE_KILL = register("double_kill", "DOUBLE KILL", 25, 0xFFFFA500);
    public static final StyleEvent TRIPLE_KILL = register("triple_kill", "TRIPLE KILL", 50, 0xFFFFA500);
    public static final StyleEvent MULTIKILL = register("multikill", "MULTIKILL", 100, 0xFFFFA500);
    public static final StyleEvent INSTAKILL = register("instakill", "INSTAKILL", 50,0xFF00FF00);
    public static final StyleEvent FRIED = register("fried", "FRIED", 30, 0xFFFFFFFF);
    public static final StyleEvent EXPLODED = register("exploded", "EXPLODED", 50, 0xFFFFFFFF);
    public static final StyleEvent FIREWORKS =  register("fireworks", "FIREWORKS", 120, 0xFF00FFFF);
    public static final StyleEvent AIRSHOT = register("airshot", "AIRSHOT", 50, 0xFF00FFFF);
    public static final StyleEvent JUMPSHOT = register("jumpshot", "JUMPSHOT", 25, 0xFF00FFFF);

}
