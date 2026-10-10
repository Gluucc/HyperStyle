package io.github.gluucc.client.config;

import io.github.gluucc.client.hud.StyleHud;
import io.github.gluucc.client.hud.StyleHud.HudMode;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClothScreen {
    static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent)
                .setTitle(Text.translatable("title.hyperstyle.config"))
                .setSavingRunnable(ModConfig::save);

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.hyperstyle.general"));
        ConfigEntryBuilder eb = builder.entryBuilder();

        general.addEntry(eb.startEnumSelector(
                Text.translatable("option.hyperstyle.hud_mode"),
                HudMode.class, ModConfig.INSTANCE.hudMode).setDefaultValue(HudMode.DEFAULT)
                .setTooltip(Text.translatable("option.hyperstyle.hud_mode.tooltip"))
                .setSaveConsumer(v -> ModConfig.INSTANCE.hudMode = v)
                .build());

        int screenW = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int screenH = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int maxRight = Math.max(0, screenW - StyleHud.TEXTURE_WIDTH);
        int maxTop = Math.max(0, screenH - StyleHud.TEXTURE_HEIGHT);

        general.addEntry(eb.startIntSlider(
                Text.translatable("option.hyperstyle.hud_offset_right"),
                Math.min(ModConfig.INSTANCE.hudOffsetRight, maxRight), 0, maxRight)
                .setDefaultValue(30)
                .setTooltip(Text.translatable("option.hyperstyle.hud_offset_right.tooltip"))
                .setSaveConsumer(v -> ModConfig.INSTANCE.hudOffsetRight = v)
                .build());

        general.addEntry(eb.startIntSlider(
                        Text.translatable("option.hyperstyle.hud_offset_top"),
                        Math.min(ModConfig.INSTANCE.hudOffsetTop, maxTop), 0, maxTop)
                .setDefaultValue(30)
                .setTooltip(Text.translatable("option.hyperstyle.hud_offset_top.tooltip"))
                .setSaveConsumer(v -> ModConfig.INSTANCE.hudOffsetTop = v)
                .build());

        return builder.build();
    }
}
