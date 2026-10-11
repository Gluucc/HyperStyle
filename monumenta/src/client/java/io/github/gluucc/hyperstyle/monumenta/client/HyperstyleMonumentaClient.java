package io.github.gluucc.hyperstyle.monumenta.client;

import io.github.gluucc.hyperstyle.client.api.CoreEvents;
import io.github.gluucc.hyperstyle.client.api.StyleCategoryRegistry;
import io.github.gluucc.hyperstyle.monumenta.HyperstyleMonumenta;
import io.github.gluucc.hyperstyle.monumenta.client.api.StyleCategoriesMonumenta;
import io.github.gluucc.hyperstyle.monumenta.client.api.StyleEventsMonumenta;
import io.github.gluucc.hyperstyle.monumenta.client.source.AbilityClassifier;
import net.fabricmc.api.ClientModInitializer;

public class HyperstyleMonumentaClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        StyleCategoriesMonumenta.init();
        StyleEventsMonumenta.init();

        CoreEvents.ADD_KILL_EVENT.register((record, category) -> {
            AbilityClassifier.addBigKill(record, category);
        });

        HyperstyleMonumenta.LOGGER.info(StyleCategoryRegistry.values() + " ");
    }
}
