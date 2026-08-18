package in.surventure.worldrelics.structure;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.RelicDefinition;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StructureManager {

    private final WorldRelicsPlugin plugin;
    private final BuiltInStructure builtInStructure;
    private Location activeStructureCenter;
    private Item activeRelicItemEntity;
    private TextDisplay activeHologram;
    private final List<LivingEntity> activeGuardians = new ArrayList<>();
    private final Map<Location, Material> originalBlockState = new HashMap<>();
    private final Map<UUID, BukkitTask> activeChannelingTasks = new HashMap<>();
    private final Map<UUID, Long> channelingCooldowns = new HashMap<>();

    public StructureManager(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
        this.builtInStructure = new BuiltInStructure(plugin);
    }

    public void generateRelicStructure(ActiveRelic activeRelic, RelicDefinition def, ItemStack relicItem) {
        Location spawnLoc = activeRelic.getLocation();
        if (spawnLoc == null) return;

        this.activeStructureCenter = spawnLoc.clone();

        boolean enabled = plugin.getConfig().getBoolean("structure.enabled", true);
        StructureTemplate template = StructureTemplate.ALTAR;
        try {
            template = StructureTemplate.valueOf(def.getStructureType().toUpperCase());
        } catch (Exception ignored) {}

        if (enabled) {
            Map<Location, Material> backup = builtInStructure.buildStructure(spawnLoc, template);
            originalBlockState.putAll(backup);
        }

        // Spawn Relic Guardian Mobs
        if (plugin.getConfig().getBoolean("guardians.enabled", true)) {
            int count = plugin.getConfig().getInt("guardians.count", 4);
            double health = plugin.getConfig().getDouble("guardians.health", 50.0);
            List<LivingEntity> mobs = builtInStructure.spawnGuardians(spawnLoc, template, count, health);
            activeGuardians.addAll(mobs);
        }

        // Floating Relic Item Entity perfectly centered on pedestal at (0.5, 3.2, 0.5)
        Location itemLoc = spawnLoc.clone().add(0.5, 3.2, 0.5);
        if (itemLoc.getWorld() != null) {
            activeRelicItemEntity = itemLoc.getWorld().dropItem(itemLoc, relicItem);
            activeRelicItemEntity.teleport(itemLoc);
            activeRelicItemEntity.setPersistent(true);
            activeRelicItemEntity.setUnlimitedLifetime(true);
            activeRelicItemEntity.setGlowing(true);
            activeRelicItemEntity.setGravity(false);
            activeRelicItemEntity.setCanPlayerPickup(false);
            activeRelicItemEntity.setPickupDelay(32767);
            activeRelicItemEntity.setVelocity(new org.bukkit.util.Vector(0, 0, 0));

            // Spawn Floating Hologram Display above pedestal
            try {
                Location holoLoc = itemLoc.clone().add(0, 0.8, 0);
                activeHologram = (TextDisplay) holoLoc.getWorld().spawnEntity(holoLoc, EntityType.TEXT_DISPLAY);
                activeHologram.setBillboard(TextDisplay.Billboard.CENTER);

                String name = def != null ? def.getDisplayName() : activeRelic.getRelicTypeId();
                String rarity = def != null ? def.getRarity().getDisplayName() : "LEGENDARY";

                activeHologram.text(MiniMessage.miniMessage().deserialize(
                        "⚡ <name> ⚡\n<rarity>\n<yellow>[ Right-Click Pedestal to Claim ]</yellow>",
                        Placeholder.parsed("name", name),
                        Placeholder.parsed("rarity", rarity)
                ));
            } catch (Exception e) {
                plugin.getLogger().warning("[WorldRelics] Could not spawn hologram text display: " + e.getMessage());
            }

            itemLoc.getWorld().playSound(itemLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.2f);
        }
    }

    public void tickBeaconBeam() {
        if (activeStructureCenter == null || activeStructureCenter.getWorld() == null) return;
        if (!plugin.getConfig().getBoolean("visuals.beacon-beam", true)) return;

        int height = plugin.getConfig().getInt("visuals.beam-height", 40);
        Location base = activeStructureCenter.clone().add(0.5, 2.5, 0.5);

        for (int y = 0; y < height; y += 2) {
            Location pLoc = base.clone().add(0, y, 0);
            pLoc.getWorld().spawnParticle(Particle.END_ROD, pLoc, 2, 0.1, 0.1, 0.1, 0.01);
        }
    }

    public boolean isChanneling(Player player) {
        return false;
    }

    public void cancelChanneling(Player player, String reason) {
    }

    public void claimPedestalRelic(Player player) {
        if (player == null) return;

        ActiveRelic currentRelic = plugin.getRelicManager().getActiveRelic();
        if (currentRelic == null || currentRelic.getStatus() != in.surventure.worldrelics.model.RelicState.AVAILABLE) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>No claimable relic at this pedestal.</red>"));
            return;
        }

        RelicDefinition currentDef = plugin.getConfigManager().getRelicDefinition(currentRelic.getRelicTypeId());
        if (currentDef != null) {
            ItemStack relicItem = plugin.getItemFactory().createRelicItem(currentRelic, currentDef);
            boolean success = plugin.getRelicManager().claimRelic(player, relicItem);
            if (success) {
                if (activeHologram != null && activeHologram.isValid()) {
                    activeHologram.remove();
                    activeHologram = null;
                }
                if (activeRelicItemEntity != null && activeRelicItemEntity.isValid()) {
                    activeRelicItemEntity.remove();
                    activeRelicItemEntity = null;
                }
                player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1.0f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
        }
    }

    public boolean isWithinProtectedArea(Location loc) {
        if (activeStructureCenter == null || loc == null) return false;
        if (!loc.getWorld().equals(activeStructureCenter.getWorld())) return false;
        boolean protectionEnabled = plugin.getConfig().getBoolean("structure.protection.enabled", true);
        if (!protectionEnabled) return false;
        double radius = plugin.getConfig().getDouble("structure.protection.radius", 15.0);
        return loc.distanceSquared(activeStructureCenter) <= (radius * radius);
    }

    public void cleanupStructure() {
        if (activeHologram != null && activeHologram.isValid()) {
            activeHologram.remove();
            activeHologram = null;
        }

        if (activeRelicItemEntity != null && activeRelicItemEntity.isValid()) {
            activeRelicItemEntity.remove();
            activeRelicItemEntity = null;
        }

        // Remove active guardians cleanly
        for (LivingEntity guardian : activeGuardians) {
            if (guardian != null && guardian.isValid()) {
                guardian.remove();
            }
        }
        activeGuardians.clear();

        // World entity sweep for any orphaned Guardians or TextDisplays
        if (activeStructureCenter != null && activeStructureCenter.getWorld() != null) {
            for (org.bukkit.entity.Entity entity : activeStructureCenter.getWorld().getNearbyEntities(activeStructureCenter, 30, 30, 30)) {
                if (entity instanceof TextDisplay) {
                    entity.remove();
                } else if (entity instanceof LivingEntity le) {
                    if (le.getCustomName() != null && le.getCustomName().contains("Relic Guardian")) {
                        le.remove();
                    }
                } else if (entity instanceof Item itemEnt) {
                    if (plugin.getItemFactory().isRelicItem(itemEnt.getItemStack())) {
                        itemEnt.remove();
                    }
                }
            }
        }

        // Restore blocks
        for (Map.Entry<Location, Material> entry : originalBlockState.entrySet()) {
            Block b = entry.getKey().getBlock();
            b.setType(entry.getValue());
        }
        originalBlockState.clear();
        activeStructureCenter = null;
    }

    public Location getActiveStructureCenter() {
        return activeStructureCenter;
    }

    public Item getActiveRelicItemEntity() {
        return activeRelicItemEntity;
    }
}
