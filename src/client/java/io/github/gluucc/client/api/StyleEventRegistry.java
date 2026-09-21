package io.github.gluucc.client.api;

import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class StyleEventRegistry {
    private static final Map<Identifier, StyleEvent> styleEvents = new LinkedHashMap<>();

    public static StyleEvent register(StyleEvent event) {
        if (styleEvents.containsKey(event.id())) {
            throw new IllegalStateException("Duplicate style event id: " + event.id());
        }

        styleEvents.put(event.id(), event);

        return event;
    }

    public static StyleEvent get(Identifier id) {
        return styleEvents.get(id);
    }

    public static Collection<StyleEvent> values() {
        return Collections.unmodifiableCollection(styleEvents.values());
    }
}
