package io.github.gluucc.client;
import io.github.gluucc.client.hud.StyleHud;
import io.github.gluucc.client.style.StyleMeter;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class HyperStyleClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientTickEvents.END_CLIENT_TICK.register(client -> StyleMeter.tick());
		HudRenderCallback.EVENT.register(StyleHud::render);

	}
}