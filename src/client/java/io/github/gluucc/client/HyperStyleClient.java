package io.github.gluucc.client;
import io.github.gluucc.HyperStyle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.realms.dto.PendingInvite;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.Queue;


public class HyperStyleClient implements ClientModInitializer {
	private static final Identifier TEXTURE = HyperStyle.id("textures/gui/test1.png");
	private static double stylePoints;
	private static final int TEXTURE_RESOLUTION = 32;
	private static StyleRanks currentRank = StyleRanks.UNRANKED;
	private static final double DECAY = 0.75;
	private static final ArrayDeque<StyleEntry> styleList = new ArrayDeque<>();
	private static final ArrayDeque<QueuedEvent> styleQueue = new ArrayDeque<>();
	private static int currentTick;
	private static final int MAX_STYLE_LIST_AGE = 60;
	private static String prevEntry;

	private record StyleEntry(String text, int count, int createdAt){}
	private record QueuedEvent(String text, int points){}

	public enum StyleRanks {
		UNRANKED("", 0, 0.0),
		D("DESTRUCTIVE", 200, 1.0),
		C("CHAOTIC", 300, 1.25),
		B("BRUTAL", 400, 1.5),
		A("ANARCHIC", 500, 2.0),
		S("SUPREME", 700, 3.0),
		SS("SSADISTIC", 850, 4.0),
		SSS("SSSHITSTORM", 1000, 6.0),
		SSSS("ULTRAKILL", 1500, 8.0);

		private final String styleLabel;
		private final int reqPoints;
		private final double decayMultiplier;

		StyleRanks(String styleRank, int reqPoints, double decayMultiplier) {
			this.styleLabel = styleRank;
			this.reqPoints = reqPoints;
			this.decayMultiplier = decayMultiplier;
		}

		public String getStyleLabel() {
			return this.styleLabel;
		}

		public int getReqPoints() {
			return this.reqPoints;
		}

		public double getDecayMultiplier() {
			return this.decayMultiplier;
		}
	}

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if(world.isClient) {
				styleQueue.addLast(new QueuedEvent("HIT", 20));
			}
			return ActionResult.PASS;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			currentTick++;

			QueuedEvent pending = styleQueue.pollFirst();
			if(pending != null) {
				StyleEntry top = styleList.peekFirst();
				if (top != null && top.text().equals(pending.text())) {
					styleList.pollFirst();
					styleList.addFirst(new StyleEntry(pending.text(), top.count() + 1, currentTick));
				} else {
					if (styleList.size() >= 5) {
						styleList.removeLast();
					}

					styleList.addFirst(new StyleEntry(pending.text(), 1, currentTick));

				}
				stylePoints += pending.points();
			}

			if (stylePoints > 0) {
				stylePoints -= DECAY * currentRank.getDecayMultiplier();
			}

			if (stylePoints < 0) {
				stylePoints = 0;
			}

			for (StyleRanks rank : StyleRanks.values()) {
				if (rank.getReqPoints() <= stylePoints) {
					currentRank = rank;
				}
			}

			while(!styleList.isEmpty() && currentTick - styleList.getLast().createdAt() >= MAX_STYLE_LIST_AGE) {
				styleList.removeLast();
			}
		});

		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			TextRenderer renderer = client.textRenderer;

			int pointsGaugeWidth = renderer.getWidth(Integer.toString((int) stylePoints));
			int rankGaugeWidth = renderer.getWidth(currentRank.getStyleLabel());

			int width = context.getScaledWindowWidth();

			context.drawTexture(TEXTURE, width-TEXTURE_RESOLUTION-10, 10, 0, 0, TEXTURE_RESOLUTION, TEXTURE_RESOLUTION, TEXTURE_RESOLUTION, TEXTURE_RESOLUTION);
			context.drawText(renderer, Integer.toString((int) stylePoints), width-pointsGaugeWidth-10, 30, 0xFF0000FF, false);
			context.drawText(renderer, currentRank.getStyleLabel(), width-rankGaugeWidth-10, 20, 0xFF0000FF, false);

			int entryOffsetY = 0;

			for(StyleEntry entry : styleList) {
				String entryText = "+ " + entry.text();
				if (entry.count() > 1) {
					entryText += " x" + entry.count();
				}
				int entryWidth = renderer.getWidth(entryText);
				context.drawText(renderer, entryText, width-entryWidth-10, 45+entryOffsetY, 0xFF0000FF, false);
				entryOffsetY += renderer.fontHeight + 5;
			}
		});
	}
}