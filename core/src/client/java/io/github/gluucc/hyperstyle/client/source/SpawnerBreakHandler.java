package io.github.gluucc.hyperstyle.client.source;

import io.github.gluucc.hyperstyle.client.api.StyleCategories;
import io.github.gluucc.hyperstyle.client.api.StyleEvents;
import io.github.gluucc.hyperstyle.client.style.StyleMeter;

public class SpawnerBreakHandler {
    public static void returnBirthControl() {
        StyleMeter.addStyle(StyleEvents.BIRTH_CONTROL, StyleCategories.NONE);
    }
}
