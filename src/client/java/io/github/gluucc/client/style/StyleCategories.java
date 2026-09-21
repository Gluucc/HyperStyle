package io.github.gluucc.client.style;

import io.github.gluucc.HyperStyle;

public class StyleCategories {
    private static StyleCategory register(String path, String label) {
        StyleCategory styleCategory = new StyleCategory(
                HyperStyle.id(path),
                label
        );

        StyleCategoryRegistry.register(styleCategory);
        return styleCategory;
    }

    public static void init() {}

    public static final StyleCategory MELEE = register("melee", "MELEE");
    public static final StyleCategory RANGED = register("ranged", "RANGED");
    public static final StyleCategory ENVIRONMENT = register("environment", "ENVIRONMENT");
    public static final StyleCategory NONE = register("none", "NONE");

}
