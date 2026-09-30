package io.github.gluucc.hyperstyleMonumenta.client.api;

import io.github.gluucc.client.api.StyleCategory;
import io.github.gluucc.client.api.StyleCategoryRegistry;
import io.github.gluucc.hyperstyleMonumenta.HyperstyleMonumenta;

public class StyleCategoriesMonumenta {
    private static StyleCategory register(String path, String label) {
        StyleCategory styleCategory = new StyleCategory(
                HyperstyleMonumenta.id(path),
                label
        );

        StyleCategoryRegistry.register(styleCategory);
        return styleCategory;
    }

    public static void init() {}

    public static final StyleCategory ABILITY = register("ability", "ABILITY");
}
