package io.github.gluucc.client.config;

import io.github.gluucc.client.hud.StyleHud.HudMode;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
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
        return builder.build();
    }
}
