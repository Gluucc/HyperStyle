package io.github.gluucc.client;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.gluucc.HyperStyle;
import io.github.gluucc.client.config.ModConfig;
import io.github.gluucc.client.hud.StyleHud;
import io.github.gluucc.client.hud.StyleHud.HudMode;
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

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class HyperStyleClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModConfig.load();
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientTickEvents.END_CLIENT_TICK.register(client -> {StyleMeter.tick(); StyleHud.tick();});
		HudRenderCallback.EVENT.register(StyleHud::render);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			LiteralArgumentBuilder<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> builder =
					literal("hyperstyle")
							.then(literal("hud")
								.then(literal("default").executes(context -> {
									ModConfig.INSTANCE.hudMode = HudMode.DEFAULT;
									ModConfig.save();
									feedback("HUD mode set to default (hidden for UNRANKED)");
									return 1;
								}))
								.then(literal("on").executes(context -> {
									ModConfig.INSTANCE.hudMode = HudMode.ON;
									ModConfig.save();
									feedback("HUD mode set to on (always visible)");
									return 1;
								}))
								.then(literal("off").executes(context -> {
									ModConfig.INSTANCE.hudMode = HudMode.OFF;
									ModConfig.save();
									feedback("HUD mode set to off (always hidden)");
									return 1;
								}))
							)
							.then(literal("position")
								.then(literal("topoffset")
									.then(argument("value", IntegerArgumentType.integer()).executes(context -> {
										int value = IntegerArgumentType.getInteger(context, "value");
										ModConfig.INSTANCE.hudOffsetTop = value;
										ModConfig.save();
										return 1;
									})
								))
								.then(literal("rightoffset")
									.then(argument("value", IntegerArgumentType.integer()).executes(context -> {
										int value = IntegerArgumentType.getInteger(context, "value");
										ModConfig.INSTANCE.hudOffsetRight = value;
										ModConfig.save();
										return 1;
									})
								))
							);
			dispatcher.register(builder);
		});

		StyleEvents.init();
		StyleCategories.init();

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