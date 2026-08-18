package in.surventure.worldrelics.config;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.RelicDefinition;
import in.surventure.worldrelics.model.RelicRarity;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.io.File;

import java.util.*;

public class ConfigManager {

    private final WorldRelicsPlugin plugin;
    private final Map<String, RelicDefinition> relicDefinitions = new HashMap<>();
    private final Map<RelicRarity, Integer> rarityWeights = new HashMap<>();

    public ConfigManager(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Load Rarity weights
        rarityWeights.clear();
        ConfigurationSection raritySection = config.getConfigurationSection("relic-selection");
        for (RelicRarity rarity : RelicRarity.values()) {
            int weight = raritySection != null ? raritySection.getInt(rarity.name().toLowerCase(), rarity.getDefaultWeight()) : rarity.getDefaultWeight();
            rarityWeights.put(rarity, weight);
        }

        // Load Relic Definitions from relics/ directory
        loadRelics();
    }

    private void loadRelics() {
        relicDefinitions.clear();
        File relicsDir = new File(plugin.getDataFolder(), "relics");
        if (!relicsDir.exists()) {
            relicsDir.mkdirs();
            // Save bundled default relic files
            saveDefaultRelicFiles();
        }

        File[] files = relicsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            saveDefaultRelicFiles();
            files = relicsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        }

        if (files != null) {
            for (File file : files) {
                try {
                    YamlConfiguration relicConfig = YamlConfiguration.loadConfiguration(file);
                    String id = relicConfig.getString("id", file.getName().replace(".yml", ""));
                    String displayName = relicConfig.getString("display-name", "<gold>" + id + "</gold>");
                    Material material = Material.matchMaterial(relicConfig.getString("material", "HEART_OF_THE_SEA"));
                    if (material == null) material = Material.HEART_OF_THE_SEA;
                    int customModelData = relicConfig.getInt("custom-model-data", 0);
                    RelicRarity rarity = RelicRarity.fromString(relicConfig.getString("rarity", "COMMON"));
                    int weight = relicConfig.getInt("weight", rarity.getDefaultWeight());
                    String structureType = relicConfig.getString("structure-type", "ALTAR");
                    int minDays = relicConfig.getInt("lifetime.min-days", plugin.getConfig().getInt("lifetime.min-days", 10));
                    int maxDays = relicConfig.getInt("lifetime.max-days", plugin.getConfig().getInt("lifetime.max-days", 15));
                    List<String> lore = relicConfig.getStringList("lore");

                    // Parse Passives
                    Map<PotionEffectType, Integer> passives = new HashMap<>();
                    ConfigurationSection passiveSec = relicConfig.getConfigurationSection("passive");
                    if (passiveSec != null) {
                        for (String key : passiveSec.getKeys(false)) {
                            boolean enabled = passiveSec.getBoolean(key + ".enabled", true);
                            if (enabled) {
                                PotionEffectType type = PotionEffectType.getByName(key.toUpperCase().replace("-", "_"));
                                if (type != null) {
                                    int amp = passiveSec.getInt(key + ".amplifier", 0);
                                    passives.put(type, amp);
                                }
                            }
                        }
                    }

                    // Parse Ability Config
                    Map<String, Object> abilityConfig = new HashMap<>();
                    ConfigurationSection abilitySec = relicConfig.getConfigurationSection("abilities");
                    if (abilitySec != null) {
                        for (String key : abilitySec.getKeys(false)) {
                            ConfigurationSection sub = abilitySec.getConfigurationSection(key);
                            if (sub != null) {
                                abilityConfig.put(key, sub.getValues(deepValues(sub)));
                            }
                        }
                    }

                    RelicDefinition def = new RelicDefinition(id, displayName, material, customModelData,
                            rarity, weight, structureType, minDays, maxDays, lore, passives, abilityConfig);
                    relicDefinitions.put(id.toLowerCase(), def);
                    plugin.getLogger().info("[WorldRelics] Loaded relic definition: " + id);
                } catch (Exception e) {
                    plugin.getLogger().severe("[WorldRelics] Failed to load relic file: " + file.getName() + " - " + e.getMessage());
                }
            }
        }
    }

    private boolean deepValues(ConfigurationSection sec) {
        return true;
    }

    private void saveDefaultRelicFiles() {
        String[] defaults = {
                "phoenix_heart.yml", "thunder_core.yml", "frost_crown.yml", "void_eye.yml",
                "guardian_heart.yml", "blood_relic.yml", "phantom_mask.yml", "inferno_core.yml"
        };
        for (String def : defaults) {
            File dest = new File(plugin.getDataFolder(), "relics/" + def);
            if (!dest.exists()) {
                plugin.saveResource("relics/" + def, false);
            }
        }
    }

    public Map<String, RelicDefinition> getRelicDefinitions() {
        return Collections.unmodifiableMap(relicDefinitions);
    }

    public RelicDefinition getRelicDefinition(String id) {
        return relicDefinitions.get(id.toLowerCase());
    }

    public Map<RelicRarity, Integer> getRarityWeights() {
        return rarityWeights;
    }
}
