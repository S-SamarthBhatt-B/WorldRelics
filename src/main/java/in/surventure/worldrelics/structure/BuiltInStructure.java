package in.surventure.worldrelics.structure;

import in.surventure.worldrelics.WorldRelicsPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuiltInStructure {

    private final WorldRelicsPlugin plugin;

    public BuiltInStructure(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    public Map<Location, Material> buildStructure(Location centerLoc, StructureTemplate template) {
        Map<Location, Material> originalBlocks = new HashMap<>();
        Material baseMat;
        Material pillarMat;
        Material capMat;
        Material accentMat;

        switch (template) {
            case VOLCANIC_ALTAR -> {
                baseMat = Material.BASALT;
                pillarMat = Material.MAGMA_BLOCK;
                capMat = Material.OBSIDIAN;
                accentMat = Material.CRYING_OBSIDIAN;
            }
            case STORM_ALTAR -> {
                baseMat = Material.CHISELED_QUARTZ_BLOCK;
                pillarMat = Material.QUARTZ_PILLAR;
                capMat = Material.COPPER_BLOCK;
                accentMat = Material.LIGHTNING_ROD;
            }
            case FROST_TEMPLE -> {
                baseMat = Material.PACKED_ICE;
                pillarMat = Material.BLUE_ICE;
                capMat = Material.CHISELED_QUARTZ_BLOCK;
                accentMat = Material.SNOW_BLOCK;
            }
            case VOID_CHAMBER -> {
                baseMat = Material.CRYING_OBSIDIAN;
                pillarMat = Material.PURPUR_PILLAR;
                capMat = Material.END_STONE_BRICKS;
                accentMat = Material.RESPAWN_ANCHOR;
            }
            case GUARDIAN_FORTRESS -> {
                baseMat = Material.DARK_PRISMARINE;
                pillarMat = Material.PRISMARINE_BRICKS;
                capMat = Material.SEA_LANTERN;
                accentMat = Material.PRISMARINE;
            }
            case BLOOD_ALTAR -> {
                baseMat = Material.NETHER_BRICKS;
                pillarMat = Material.RED_NETHER_BRICKS;
                capMat = Material.REDSTONE_BLOCK;
                accentMat = Material.NETHER_WART_BLOCK;
            }
            case PHANTOM_TEMPLE -> {
                baseMat = Material.DEEPSLATE_BRICKS;
                pillarMat = Material.CHISELED_DEEPSLATE;
                capMat = Material.SMOOTH_BASALT;
                accentMat = Material.SOUL_LANTERN;
            }
            default -> {
                baseMat = Material.SMOOTH_STONE;
                pillarMat = Material.STONE_BRICKS;
                capMat = Material.GOLD_BLOCK;
                accentMat = Material.CHISELED_STONE_BRICKS;
            }
        }

        Location base = centerLoc.getBlock().getLocation();

        // 1. Clear 9x9x15 air volume above structure floor so structure and pedestal item are never buried/pushed by trees
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = 1; y <= 15; y++) {
                    Block airBlock = base.clone().add(x, y, z).getBlock();
                    if (!airBlock.getType().isAir()) {
                        originalBlocks.put(airBlock.getLocation(), airBlock.getType());
                        airBlock.setType(Material.AIR);
                    }
                }
            }
        }

        // 2. Build 9x9 outer foundation platform
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                Block b = base.clone().add(x, 0, z).getBlock();
                originalBlocks.put(b.getLocation(), b.getType());
                b.setType(baseMat);
            }
        }

        // 3. Build 5x5 elevated inner stage
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Block b = base.clone().add(x, 1, z).getBlock();
                originalBlocks.put(b.getLocation(), b.getType());
                b.setType(accentMat);
            }
        }

        // 4. Build 4 tall corner columns at (-3, -3), (3, -3), (-3, 3), (3, 3)
        int[][] corners = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] corner : corners) {
            for (int h = 1; h <= 4; h++) {
                Block b = base.clone().add(corner[0], h, corner[1]).getBlock();
                originalBlocks.put(b.getLocation(), b.getType());
                b.setType(h == 4 ? capMat : pillarMat);
            }
        }

        // 5. Central Relic Pedestal at (0, 2, 0)
        Block pedestalBlock = base.clone().add(0, 2, 0).getBlock();
        originalBlocks.put(pedestalBlock.getLocation(), pedestalBlock.getType());
        pedestalBlock.setType(capMat);

        return originalBlocks;
    }

    public List<LivingEntity> spawnGuardians(Location centerLoc, StructureTemplate template, int count, double maxHealth) {
        List<LivingEntity> guardians = new ArrayList<>();
        if (centerLoc == null || centerLoc.getWorld() == null || count <= 0) return guardians;

        EntityType type;
        switch (template) {
            case VOLCANIC_ALTAR -> type = EntityType.BLAZE;
            case STORM_ALTAR -> type = EntityType.PHANTOM;
            case FROST_TEMPLE -> type = EntityType.STRAY;
            case VOID_CHAMBER -> type = EntityType.WITHER_SKELETON;
            case GUARDIAN_FORTRESS -> type = EntityType.GUARDIAN;
            case BLOOD_ALTAR -> type = EntityType.VINDICATOR;
            case PHANTOM_TEMPLE -> type = EntityType.PHANTOM;
            default -> type = EntityType.SKELETON;
        }

        // Safe guardian spawn offsets on outer platform at radius 4 (clear of pillars)
        int[][] safeOffsets = {{-4, 0}, {4, 0}, {0, -4}, {0, 4}, {-3, 0}, {3, 0}};
        for (int i = 0; i < Math.min(count, safeOffsets.length); i++) {
            Location spawnAt = centerLoc.clone().add(safeOffsets[i][0] + 0.5, 1.0, safeOffsets[i][1] + 0.5);
            try {
                LivingEntity entity = (LivingEntity) centerLoc.getWorld().spawnEntity(spawnAt, type);
                entity.customName(MiniMessage.miniMessage().deserialize("<gold><bold>Relic Guardian</bold></gold>"));
                entity.setCustomNameVisible(true);
                entity.setPersistent(true);
                entity.setGlowing(true);

                if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                    entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
                    entity.setHealth(maxHealth);
                }

                guardians.add(entity);
            } catch (Exception e) {
                plugin.getLogger().warning("[WorldRelics] Failed to spawn guardian entity: " + e.getMessage());
            }
        }
        return guardians;
    }
}
