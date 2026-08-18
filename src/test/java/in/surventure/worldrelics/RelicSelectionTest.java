package in.surventure.worldrelics;

import in.surventure.worldrelics.model.RelicDefinition;
import in.surventure.worldrelics.model.RelicRarity;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class RelicSelectionTest {

    @Test
    public void testWeightedSelection() {
        Map<String, RelicDefinition> defs = new HashMap<>();
        defs.put("common_relic", new RelicDefinition("common_relic", "Common", Material.STONE, 0, RelicRarity.COMMON, 60, "ALTAR", 10, 15, List.of(), Map.of(), Map.of()));
        defs.put("mythic_relic", new RelicDefinition("mythic_relic", "Mythic", Material.DIAMOND, 0, RelicRarity.MYTHIC, 1, "ALTAR", 10, 15, List.of(), Map.of(), Map.of()));

        Map<RelicRarity, Integer> weights = Map.of(
                RelicRarity.COMMON, 60,
                RelicRarity.MYTHIC, 1
        );

        List<RelicDefinition> pool = new ArrayList<>();
        for (RelicDefinition def : defs.values()) {
            int rarityWeight = weights.getOrDefault(def.getRarity(), def.getRarity().getDefaultWeight());
            int totalWeight = rarityWeight * def.getWeight();
            for (int i = 0; i < totalWeight; i++) {
                pool.add(def);
            }
        }

        Assertions.assertFalse(pool.isEmpty());
        Assertions.assertTrue(pool.size() > 3600); // 60*60 = 3600 common entries vs 1 mythic entry
    }
}
