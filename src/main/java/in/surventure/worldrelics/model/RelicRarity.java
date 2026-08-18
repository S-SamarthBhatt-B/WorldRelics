package in.surventure.worldrelics.model;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum RelicRarity {
    COMMON("Common", NamedTextColor.GRAY, 60),
    RARE("Rare", NamedTextColor.BLUE, 25),
    EPIC("Epic", NamedTextColor.LIGHT_PURPLE, 10),
    LEGENDARY("Legendary", NamedTextColor.GOLD, 5),
    MYTHIC("Mythic", TextColor.color(0xFF1493), 1);

    private final String displayName;
    private final TextColor color;
    private final int defaultWeight;

    RelicRarity(String displayName, TextColor color, int defaultWeight) {
        this.displayName = displayName;
        this.color = color;
        this.defaultWeight = defaultWeight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TextColor getColor() {
        return color;
    }

    public int getDefaultWeight() {
        return defaultWeight;
    }

    public static RelicRarity fromString(String name) {
        for (RelicRarity rarity : values()) {
            if (rarity.name().equalsIgnoreCase(name)) {
                return rarity;
            }
        }
        return COMMON;
    }
}
