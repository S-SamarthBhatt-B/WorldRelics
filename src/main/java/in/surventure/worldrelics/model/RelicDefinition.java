package in.surventure.worldrelics.model;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

public class RelicDefinition {

    private final String id;
    private final String displayName;
    private final Material material;
    private final int customModelData;
    private final RelicRarity rarity;
    private final int weight;
    private final String structureType;
    private final int minDays;
    private final int maxDays;
    private final List<String> lore;
    private final Map<PotionEffectType, Integer> passiveEffects;
    private final Map<String, Object> abilityConfig;

    public RelicDefinition(String id, String displayName, Material material, int customModelData,
                           RelicRarity rarity, int weight, String structureType, int minDays, int maxDays,
                           List<String> lore, Map<PotionEffectType, Integer> passiveEffects,
                           Map<String, Object> abilityConfig) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.customModelData = customModelData;
        this.rarity = rarity;
        this.weight = weight;
        this.structureType = structureType;
        this.minDays = minDays;
        this.maxDays = maxDays;
        this.lore = lore;
        this.passiveEffects = passiveEffects;
        this.abilityConfig = abilityConfig;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public RelicRarity getRarity() {
        return rarity;
    }

    public int getWeight() {
        return weight;
    }

    public String getStructureType() {
        return structureType;
    }

    public int getMinDays() {
        return minDays;
    }

    public int getMaxDays() {
        return maxDays;
    }

    public List<String> getLore() {
        return lore;
    }

    public Map<PotionEffectType, Integer> getPassiveEffects() {
        return passiveEffects;
    }

    public Map<String, Object> getAbilityConfig() {
        return abilityConfig;
    }
}
