package io.github.gluucc.client;
import io.github.gluucc.client.api.CoreEvents;
import io.github.gluucc.client.hud.StyleHud;
import io.github.gluucc.client.hud.StyleHud.HudMode;
import io.github.gluucc.client.source.DamageClassifier;
import io.github.gluucc.client.source.DamageHandler;
import io.github.gluucc.client.source.DeathHandler;
import io.github.gluucc.client.api.StyleCategories;
import io.github.gluucc.client.api.StyleEvents;
import io.github.gluucc.client.source.SpawnerBreakHandler;
import io.github.gluucc.client.style.StyleMeter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class HyperStyleClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientTickEvents.END_CLIENT_TICK.register(client -> {StyleMeter.tick(); StyleHud.tick();});
		HudRenderCallback.EVENT.register(StyleHud::render);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			LiteralArgumentBuilder<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> builder =
					literal("hyperstylehud")
							.then(literal("default").executes(context -> {
								StyleHud.hudMode = HudMode.DEFAULT;
								feedback("HUD mode set to default (hidden for UNRANKED)");
								return 1;
							}))
							.then(literal("on").executes(context -> {
								StyleHud.hudMode = HudMode.ON;
								feedback("HUD mode set to on (always visible)");
								return 1;
							}))
							.then(literal("off").executes(context -> {
								StyleHud.hudMode = HudMode.OFF;
								feedback("HUD mode set to off (always hidden)");
								return 1;
							}));
			dispatcher.register(builder);
		});

		StyleEvents.init();
		StyleCategories.init();

		CoreEvents.ADD_KILL_EVENT.register((record, category) -> {
			DamageClassifier.addKill(record, category);
		});

		ClientPlayerBlockBreakEvents.AFTER.register(((world, player, pos, state) -> {
			if (state.isOf(Blocks.SPAWNER)) {
				SpawnerBreakHandler.returnBirthControl();
			}
		}));

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

	private void feedback(String message) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			client.player.sendMessage(Text.literal("[HyperStyle] " + message), false);
		}
	}
}