package io.github.gluucc.hyperstyleMonumenta.client;

import io.github.gluucc.client.api.CoreEvents;
import io.github.gluucc.client.api.StyleCategoryRegistry;
import io.github.gluucc.client.api.StyleEvents;
import io.github.gluucc.client.source.SpawnerBreakHandler;
import io.github.gluucc.hyperstyleMonumenta.HyperstyleMonumenta;
import io.github.gluucc.hyperstyleMonumenta.client.api.StyleCategoriesMonumenta;
import io.github.gluucc.hyperstyleMonumenta.client.api.StyleEventsMonumenta;
import io.github.gluucc.hyperstyleMonumenta.client.source.AbilityClassifier;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.entity.damage.DamageSource;

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
