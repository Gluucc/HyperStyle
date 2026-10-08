package io.github.gluucc.client.source;

import io.github.gluucc.client.api.StyleCategories;
import io.github.gluucc.client.api.StyleEvents;
import io.github.gluucc.client.style.StyleMeter;

public class SpawnerBreakHandler {
    public static void returnBirthControl() {
        StyleMeter.addStyle(StyleEvents.BIRTH_CONTROL, StyleCategories.NONE);
    }
}
