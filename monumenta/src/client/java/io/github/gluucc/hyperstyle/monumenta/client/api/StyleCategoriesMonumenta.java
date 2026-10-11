package io.github.gluucc.hyperstyle.monumenta.client.api;

import io.github.gluucc.hyperstyle.client.api.StyleCategory;
import io.github.gluucc.hyperstyle.client.api.StyleCategoryRegistry;
import io.github.gluucc.hyperstyle.monumenta.HyperstyleMonumenta;

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
