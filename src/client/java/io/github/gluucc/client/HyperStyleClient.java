package io.github.gluucc.client;
import io.github.gluucc.client.source.DamageHandler;
import io.github.gluucc.client.source.DeathHandler;
import io.github.gluucc.client.hud.StyleHud;
import io.github.gluucc.client.api.StyleCategories;
import io.github.gluucc.client.api.StyleEvents;
import io.github.gluucc.client.style.StyleMeter;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class HyperStyleClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientTickEvents.END_CLIENT_TICK.register(client -> StyleMeter.tick());
		HudRenderCallback.EVENT.register(StyleHud::render);

		StyleEvents.init();
		StyleCategories.init();

		ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) ->
				DamageHandler.removeRecord(entity.getId()));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
				StyleMeter.reset();
				DamageHandler.reset();
				DeathHandler.reset();
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			StyleMeter.reset();
			DamageHandler.reset();
			DeathHandler.reset();
		});
	}
}