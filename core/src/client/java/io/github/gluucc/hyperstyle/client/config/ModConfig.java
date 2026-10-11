package io.github.gluucc.hyperstyle.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.gluucc.hyperstyle.HyperStyle;
import io.github.gluucc.hyperstyle.client.hud.StyleHud.HudMode;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("hyperstyle.json");

    public static ModConfig INSTANCE = new ModConfig();
    public HudMode hudMode = HudMode.DEFAULT;

    public int hudOffsetRight = 30;
    public int hudOffsetTop = 30;

    public float hudScale = 1.0f;
    public float hudRotation = -55f;

    public float hudSwayIntensity = 0.5f;
    public float hudSwayCatchupDelay = 0.5f;

    public static void load() {
        try {
            if(Files.exists(PATH)) {
                ModConfig loaded = GSON.fromJson(Files.readString(PATH), ModConfig.class);
                if(loaded != null) INSTANCE = loaded;
            }
        } catch(Exception e) {
            HyperStyle.LOGGER.error("Failed to load config, using defaults", e);
        }
        if(INSTANCE.hudMode == null) INSTANCE.hudMode = HudMode.DEFAULT;
        save();
    }

    public static void save() {
        try {
            Files.writeString(PATH, GSON.toJson(INSTANCE));
        } catch(Exception e) {
            HyperStyle.LOGGER.error("Failed to save config", e);
        }
    }
}
