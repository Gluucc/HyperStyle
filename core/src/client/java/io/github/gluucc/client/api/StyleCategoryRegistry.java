package io.github.gluucc.client.api;

import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class StyleCategoryRegistry {
    private static final Map<Identifier, StyleCategory> styleCategories = new LinkedHashMap<>();

    public static StyleCategory register(StyleCategory category) {
        if (styleCategories.containsKey(category.id())) {
            throw new IllegalStateException("Duplicate style event id: " + category.id());
        }

        styleCategories.put(category.id(), category);

        return category;
    }

    public static StyleCategory get(Identifier id) {
        return styleCategories.get(id);
    }

    public static Collection<StyleCategory> values() {
        return Collections.unmodifiableCollection(styleCategories.values());
    }
}
