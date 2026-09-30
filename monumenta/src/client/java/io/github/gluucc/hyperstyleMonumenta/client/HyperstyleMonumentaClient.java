package io.github.gluucc.hyperstyleMonumenta.client;

import io.github.gluucc.client.api.StyleCategoryRegistry;
import io.github.gluucc.client.api.StyleEvents;
import io.github.gluucc.hyperstyleMonumenta.HyperstyleMonumenta;
import io.github.gluucc.hyperstyleMonumenta.client.api.StyleCategoriesMonumenta;
import io.github.gluucc.hyperstyleMonumenta.client.api.StyleEventsMonumenta;
import net.fabricmc.api.ClientModInitializer;

public class HyperstyleMonumentaClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        StyleCategoriesMonumenta.init();
        StyleEventsMonumenta.init();

        HyperstyleMonumenta.LOGGER.info(StyleCategoryRegistry.values() + " ");
    }
}
