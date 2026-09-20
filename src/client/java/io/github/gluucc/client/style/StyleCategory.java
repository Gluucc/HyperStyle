package io.github.gluucc.client.style;

public enum StyleCategory {
    MELEE("MELEE"),
    RANGED("RANGED"),
    ENVIRONMENT("ENVIRONMENT"),
    NONE("NONE");

    private final String categoryLabel;

    StyleCategory(String categoryLabel) {
        this.categoryLabel = categoryLabel;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }


}
