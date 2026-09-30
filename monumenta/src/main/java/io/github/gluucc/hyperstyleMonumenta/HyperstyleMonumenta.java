package io.github.gluucc.hyperstyleMonumenta;

import io.github.gluucc.client.api.StyleCategoryRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HyperstyleMonumenta implements ModInitializer {
    public static final String MOD_ID = "hyperstyle_monumenta";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("hello (not client)");
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
