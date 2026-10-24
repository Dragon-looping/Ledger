package com.ledger.os.model;

/**
 * Ledger transaction categories with UI icons and color tokens.
 */
public enum Category {
    FOOD("utensils", "#EF4444"),
    TRANSPORT("car", "#3B82F6"),
    RENT("home", "#8B5CF6"),
    SHOPPING("shopping-bag", "#EC4899"),
    BILLS("file-text", "#F59E0B"),
    HEALTH("heart-pulse", "#10B981"),
    ENTERTAINMENT("film", "#6366F1"),
    SALARY("briefcase", "#14B8A6"),
    OTHER("tag", "#6B7280");

    private final String icon;
    private final String hexColor;

    Category(String icon, String hexColor) {
        this.icon = icon;
        this.hexColor = hexColor;
    }

    public String getIcon() {
        return icon;
    }

    public String getHexColor() {
        return hexColor;
    }
}
