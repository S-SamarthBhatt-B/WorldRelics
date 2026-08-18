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

        // Floating Relic Item Entity perfectly centered on pedestal at (0.5, 2.5, 0.5)
        Location itemLoc = spawnLoc.clone().add(0.5, 2.5, 0.5);
        if (itemLoc.getWorld() != null) {
            activeRelicItemEntity = itemLoc.getWorld().dropItem(itemLoc, relicItem);
            activeRelicItemEntity.setPersistent(true);
            activeRelicItemEntity.setUnlimitedLifetime(true);
            activeRelicItemEntity.setGlowing(true);
            activeRelicItemEntity.setVelocity(new org.bukkit.util.Vector(0, 0, 0));

            // Spawn Floating Hologram Display above pedestal
            try {
                Location holoLoc = itemLoc.clone().add(0, 0.8, 0);
                activeHologram = (TextDisplay) holoLoc.getWorld().spawnEntity(holoLoc, EntityType.TEXT_DISPLAY);
                activeHologram.setBillboard(TextDisplay.Billboard.CENTER);

                String name = def != null ? def.getDisplayName() : activeRelic.getRelicTypeId();
                String rarity = def != null ? def.getRarity().getDisplayName() : "LEGENDARY";

                activeHologram.text(MiniMessage.miniMessage().deserialize(
                        "⚡ <name> ⚡\n<rarity>\n<yellow>[ Right-Click Pedestal to Channel ]</yellow>",
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
        return activeChannelingTasks.containsKey(player.getUniqueId());
    }

    public void cancelChanneling(Player player, String reason) {
        BukkitTask task = activeChannelingTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            channelingCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 10000L); // 10s cooldown
            if (reason != null && player.isOnline()) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(reason));
            }
        }
    }

    public void startChanneling(Player player) {
        if (player == null || isChanneling(player)) return;

        long now = System.currentTimeMillis();
        long cd = channelingCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (now < cd) {
            long remaining = (cd - now) / 1000 + 1;
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>❌ You must wait <yellow>" + remaining + "s</yellow> before attempting to channel again!</red>"));
            return;
        }

        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic == null || relic.getStatus() != in.surventure.worldrelics.model.RelicState.AVAILABLE) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>No claimable relic at this pedestal.</red>"));
            return;
        }

        RelicDefinition def = plugin.getConfigManager().getRelicDefinition(relic.getRelicTypeId());
        String relicName = def != null ? def.getDisplayName() : relic.getRelicTypeId();

        Location startLoc = player.getLocation().clone();
        long startTime = System.currentTimeMillis();
        long totalMs = 5000L;

        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<gold>⏳ Started channeling <name>... Hold steady for 5 seconds!</gold>",
                Placeholder.parsed("name", relicName)
        ));

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                cancelChanneling(player, null);
                return;
            }

            // Check movement
            if (player.getLocation().distanceSquared(startLoc) > 9.0) {
                cancelChanneling(player, "<red>❌ Channeling interrupted! You moved too far away.</red>");
                return;
            }

            long elapsed = System.currentTimeMillis() - startTime;
            long remainingSec = Math.max(1, (totalMs - elapsed) / 1000 + 1);

            // Relic Surge Defense Wave & Soundscapes
            Location pLoc = player.getLocation().add(0, 1, 0);
            player.getWorld().spawnParticle(Particle.DRAGON_BREATH, pLoc, 15, 0.5, 1.0, 0.5, 0.05);
            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, pLoc, 10, 0.5, 0.5, 0.5, 0.1);
            player.getWorld().playSound(pLoc, Sound.BLOCK_BEACON_AMBIENT, 1.0f, 1.5f);

            // Actionbar update
            player.sendActionBar(MiniMessage.miniMessage().deserialize(
                    "⏳ Channeling <name>... <yellow><time>s remaining</yellow>",
                    Placeholder.parsed("name", relicName),
                    Placeholder.unparsed("time", String.valueOf(remainingSec))
            ));

            if (elapsed >= totalMs) {
                cancelChanneling(player, null);
                if (activeRelicItemEntity != null && activeRelicItemEntity.isValid()) {
                    ItemStack itemStack = activeRelicItemEntity.getItemStack();
                    boolean success = plugin.getRelicManager().claimRelic(player, itemStack);
                    if (success) {
                        if (activeHologram != null && activeHologram.isValid()) {
                            activeHologram.remove();
                            activeHologram = null;
                        }
                        activeRelicItemEntity.remove();
                        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1.0f);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                    }
                }
            }
        }, 0L, 5L);

        activeChannelingTasks.put(player.getUniqueId(), task);
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
