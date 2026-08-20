package in.surventure.worldrelics.manager;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.api.event.*;
import in.surventure.worldrelics.model.*;
import in.surventure.worldrelics.structure.StructureManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RelicManager {

    private final WorldRelicsPlugin plugin;
    private final RelicLocationManager locationManager;
    private final StructureManager structureManager;
    private final RelicDisplayManager displayManager;
    private final RelicBountyManager bountyManager;
    private final in.surventure.worldrelics.manager.RelicDuelManager duelManager;
    private final RelicEvolutionManager evolutionManager;
    private final Random random = new Random();

    private final long serverStartTime = System.currentTimeMillis();
    private ActiveRelic activeRelic;

    public RelicManager(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
        this.locationManager = new RelicLocationManager(plugin);
        this.structureManager = new StructureManager(plugin);
        this.displayManager = new RelicDisplayManager(plugin);
        this.bountyManager = new RelicBountyManager(plugin);
        this.duelManager = new in.surventure.worldrelics.manager.RelicDuelManager(plugin);
        this.evolutionManager = new RelicEvolutionManager(plugin);
    }

    public boolean canSpawnRelic(boolean checkDelay, boolean checkPlayers) {
        if (checkDelay) {
            long initialDelayMs = plugin.getConfig().getLong("spawn.initial-delay-seconds", 300) * 1000L;
            if (System.currentTimeMillis() - serverStartTime < initialDelayMs) {
                return false;
            }
        }
        if (checkPlayers) {
            int minPlayers = plugin.getConfig().getInt("spawn.min-players", 2);
            if (Bukkit.getOnlinePlayers().size() < minPlayers) {
                return false;
            }
        }
        return true;
    }

    public void initializeStateOnStartup() {
        ActiveRelic loaded = plugin.getDatabaseManager().loadActiveRelicSync();
        if (loaded != null) {
            if (loaded.isExpired()) {
                plugin.getLogger().info("[WorldRelics] Startup check: Found expired relic (" + loaded.getRelicTypeId() + "). Expiring...");
                this.activeRelic = loaded;
                handleRelicExpiration(false);
            } else {
                plugin.getLogger().info("[WorldRelics] Startup recovery: Restored active relic " + loaded.getRelicTypeId() + " (Owner: " + loaded.getOwnerName() + ")");
                this.activeRelic = loaded;
                RelicDefinition def = plugin.getConfigManager().getRelicDefinition(loaded.getRelicTypeId());
                if (def != null && loaded.getStatus() == RelicState.AVAILABLE) {
                    ItemStack relicItem = plugin.getItemFactory().createRelicItem(loaded, def);
                    structureManager.generateRelicStructure(loaded, def, relicItem);
                }
            }
        } else {
            plugin.getLogger().info("[WorldRelics] Startup check: No active relic found. Relic spawn queued (waiting for startup delay & player requirements)...");
        }
    }

    public RelicDefinition selectRandomRelicDefinition() {
        Map<String, RelicDefinition> definitions = plugin.getConfigManager().getRelicDefinitions();
        if (definitions.isEmpty()) return null;

        Map<RelicRarity, Integer> weights = plugin.getConfigManager().getRarityWeights();
        List<RelicDefinition> pool = new ArrayList<>();

        for (RelicDefinition def : definitions.values()) {
            int rarityWeight = weights.getOrDefault(def.getRarity(), def.getRarity().getDefaultWeight());
            int totalWeight = rarityWeight * def.getWeight();
            for (int i = 0; i < Math.max(1, totalWeight); i++) {
                pool.add(def);
            }
        }

        if (pool.isEmpty()) return new ArrayList<>(definitions.values()).get(0);
        return pool.get(random.nextInt(pool.size()));
    }

    public void triggerNewRelicSpawnCycle() {
        triggerNewRelicSpawnCycle(false, null);
    }

    public void triggerNewRelicSpawnCycle(boolean force) {
        triggerNewRelicSpawnCycle(force, null);
    }

    public void triggerNewRelicSpawnCycle(boolean force, String forcedRelicTypeId) {
        if (activeRelic != null && activeRelic.getStatus() != RelicState.NO_RELIC && activeRelic.getStatus() != RelicState.EXPIRED) {
            plugin.getLogger().warning("[WorldRelics] Cannot spawn new relic: Active relic already exists!");
            return;
        }

        if (!force && !canSpawnRelic(true, true)) {
            plugin.getLogger().info("[WorldRelics] Postponing relic spawn: Server startup delay or min player requirement not met.");
            return;
        }

        RelicDefinition selectedDef = null;
        if (forcedRelicTypeId != null) {
            selectedDef = plugin.getConfigManager().getRelicDefinition(forcedRelicTypeId);
        }
        if (selectedDef == null) {
            selectedDef = selectRandomRelicDefinition();
        }

        if (selectedDef == null) {
            plugin.getLogger().severe("[WorldRelics] Failed to select relic: No definitions loaded!");
            return;
        }

        plugin.getLogger().info("[WorldRelics] Selected relic: " + selectedDef.getId() + " (" + selectedDef.getRarity() + ")");

        final RelicDefinition finalDef = selectedDef;

        locationManager.findSafeLocationAsync().thenAccept(loc -> {
            if (loc == null) {
                plugin.getLogger().severe("[WorldRelics] Failed to find safe spawn location!");
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                UUID relicUuid = UUID.randomUUID();
                int minDays = finalDef.getMinDays();
                int maxDays = finalDef.getMaxDays();
                int selectedMcDays = minDays + (minDays < maxDays ? random.nextInt(maxDays - minDays + 1) : 0);

                // 1 Minecraft Day = 20 real minutes = 1,200,000 milliseconds
                long lifetimeMillis = selectedMcDays * 20L * 60L * 1000L;
                long expiresAt = System.currentTimeMillis() + lifetimeMillis;

                activeRelic = new ActiveRelic(
                        relicUuid, finalDef.getId(), finalDef.getRarity(),
                        null, null, loc.getWorld().getName(),
                        loc.getX(), loc.getY(), loc.getZ(),
                        System.currentTimeMillis(), expiresAt, RelicState.AVAILABLE
                );

                plugin.getDatabaseManager().saveOrUpdateActiveRelic(activeRelic);

                ItemStack relicItem = plugin.getItemFactory().createRelicItem(activeRelic, finalDef);
                structureManager.generateRelicStructure(activeRelic, finalDef, relicItem);

                // Call Event
                Bukkit.getPluginManager().callEvent(new RelicSpawnEvent(activeRelic, loc));

                // Broadcast
                if (plugin.getConfig().getBoolean("broadcast.relic-spawned", true)) {
                    plugin.getMessageManager().broadcast("relic-spawned-broadcast",
                            Placeholder.parsed("relic_name", finalDef.getDisplayName())
                    );
                }

                plugin.getLogger().info("[WorldRelics] Relic spawned at " + loc.getWorld().getName() + " " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
            });
        });
    }

    public boolean claimRelic(Player player, ItemStack item) {
        if (activeRelic == null || activeRelic.getStatus() == RelicState.EXPIRED) return false;
        if (!plugin.getItemFactory().isRelicItem(item)) return false;

        UUID itemRelicUuid = plugin.getItemFactory().getRelicUuid(item);
        if (itemRelicUuid == null || !itemRelicUuid.equals(activeRelic.getRelicUuid())) {
            // Obsolete or duplicated relic item -> destroy
            item.setAmount(0);
            player.sendMessage(net.kyori.adventure.text.Component.text("This relic item is invalid or obsolete and has vanished.", net.kyori.adventure.text.format.NamedTextColor.RED));
            return false;
        }

        RelicClaimEvent claimEvent = new RelicClaimEvent(activeRelic, player);
        Bukkit.getPluginManager().callEvent(claimEvent);
        if (claimEvent.isCancelled()) return false;

        Player prevOwner = activeRelic.getOwnerUuid() != null ? Bukkit.getPlayer(activeRelic.getOwnerUuid()) : null;

        activeRelic.setOwnerUuid(player.getUniqueId());
        activeRelic.setOwnerName(player.getName());
        activeRelic.setStatus(RelicState.CLAIMED);

        bountyManager.applyBounty(player, activeRelic.getRarity());

        // Wipe Relic Locators from all online inventories since relic is now claimed
        wipeLocatorsFromInventories(true, false);

        // Schedule structure removal 30 seconds after claim
        if (structureManager.getActiveStructureCenter() != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                structureManager.cleanupStructure();
            }, 600L);
        }

        plugin.getItemFactory().updateOwnerData(item, player.getUniqueId());
        player.getInventory().addItem(item);
        plugin.getDatabaseManager().saveOrUpdateActiveRelic(activeRelic);

        RelicDefinition def = plugin.getConfigManager().getRelicDefinition(activeRelic.getRelicTypeId());
        String relicName = def != null ? def.getDisplayName() : activeRelic.getRelicTypeId();

        if (prevOwner != null && !prevOwner.equals(player)) {
            Bukkit.getPluginManager().callEvent(new RelicTransferEvent(activeRelic, prevOwner, player));
        }

        double bountyAmt = bountyManager.getActiveBountyAmount();

        if (plugin.getConfig().getBoolean("broadcast.relic-claimed", true)) {
            plugin.getMessageManager().broadcast("relic-claimed-broadcast",
                    Placeholder.parsed("relic_name", relicName),
                    Placeholder.unparsed("player_name", player.getName()),
                    Placeholder.unparsed("bounty_amount", String.format("%,.2f", bountyAmt))
            );
        }

        plugin.getLogger().info("[WorldRelics] Relic claimed by player " + player.getName() + " (" + player.getUniqueId() + ")");
        return true;
    }

    public void handleOwnerDeath(Player owner, Location deathLoc) {
        if (activeRelic == null || activeRelic.getOwnerUuid() == null || !activeRelic.getOwnerUuid().equals(owner.getUniqueId())) {
            return;
        }

        Player killer = owner.getKiller();
        if (killer != null) {
            bountyManager.handleOwnerKilled(owner, killer);
        }

        RelicDefinition def = plugin.getConfigManager().getRelicDefinition(activeRelic.getRelicTypeId());
        String relicName = def != null ? def.getDisplayName() : activeRelic.getRelicTypeId();

        boolean dropRelic = plugin.getConfig().getBoolean("death.drop-relic", true);

        if (dropRelic) {
            activeRelic.setOwnerUuid(null);
            activeRelic.setOwnerName(null);
            activeRelic.setStatus(RelicState.AVAILABLE);
            plugin.getDatabaseManager().saveOrUpdateActiveRelic(activeRelic);

            // Reset evolution back to Tier I on owner death
            evolutionManager.resetEvolutionOnDeath(activeRelic);

            // Wipe owner trackers on owner death
            wipeLocatorsFromInventories(false, true);

            Bukkit.getPluginManager().callEvent(new RelicDropEvent(activeRelic, owner, deathLoc));

            if (plugin.getConfig().getBoolean("broadcast.relic-dropped", true)) {
                plugin.getMessageManager().broadcast("relic-dropped-broadcast",
                        Placeholder.parsed("relic_name", relicName),
                        Placeholder.unparsed("player_name", owner.getName())
                );
            }
        }
    }

    public void handleRelicExpiration(boolean respawnAfter) {
        if (activeRelic == null) return;

        RelicDefinition def = plugin.getConfigManager().getRelicDefinition(activeRelic.getRelicTypeId());
        String relicName = def != null ? def.getDisplayName() : activeRelic.getRelicTypeId();

        // Broadcast Expiration
        if (plugin.getConfig().getBoolean("broadcast.relic-expired", true)) {
            plugin.getMessageManager().broadcast("relic-expired-broadcast",
                    Placeholder.parsed("relic_name", relicName)
            );
        }

        Bukkit.getPluginManager().callEvent(new RelicExpireEvent(activeRelic));

        // Archive to DB
        RelicHistoryEntry history = new RelicHistoryEntry(
                activeRelic.getRelicUuid(), activeRelic.getRelicTypeId(),
                activeRelic.getOwnerUuid(), activeRelic.getOwnerName(),
                activeRelic.getClaimedAt(), System.currentTimeMillis(), "EXPIRED"
        );
        plugin.getDatabaseManager().archiveHistory(history);
        plugin.getDatabaseManager().deleteActiveRelic(activeRelic.getRelicUuid());

        // Cleanup structure & entity
        structureManager.cleanupStructure();

        // Wipe relic items from current online owner inventory if applicable
        if (activeRelic.getOwnerUuid() != null) {
            Player owner = Bukkit.getPlayer(activeRelic.getOwnerUuid());
            if (owner != null && owner.isOnline()) {
                wipeRelicFromInventory(owner, activeRelic.getRelicUuid());
            }
        }

        bountyManager.clearActiveBounty();
        displayManager.clearOwnerEffects();
        displayManager.removeBossBar();
        wipeLocatorsFromInventories(true, true);

        activeRelic.setStatus(RelicState.EXPIRED);
        activeRelic = null;

        if (respawnAfter) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> triggerNewRelicSpawnCycle(false), 100L);
        }
    }

    public void wipeLocatorsFromInventories(boolean wipeRelicLocator, boolean wipeOwnerLocator) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (ItemStack item : p.getInventory().getContents()) {
                if (item == null) continue;
                if (wipeRelicLocator && plugin.getItemFactory().isRelicLocatorItem(item)) {
                    item.setAmount(0);
                }
                if (wipeOwnerLocator && plugin.getItemFactory().isOwnerLocatorItem(item)) {
                    item.setAmount(0);
                }
            }
        }
    }

    public void wipeRelicFromInventory(Player player, UUID relicUuid) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && plugin.getItemFactory().isRelicItem(item)) {
                UUID itemUuid = plugin.getItemFactory().getRelicUuid(item);
                if (relicUuid == null || relicUuid.equals(itemUuid)) {
                    item.setAmount(0);
                }
            }
        }
    }

    public void resetRelicSystem() {
        if (activeRelic != null) {
            structureManager.cleanupStructure();
            plugin.getDatabaseManager().deleteActiveRelic(activeRelic.getRelicUuid());
            activeRelic = null;
        }
        bountyManager.clearActiveBounty();
        displayManager.clearOwnerEffects();
        displayManager.removeBossBar();
    }

    public ActiveRelic getActiveRelic() {
        return activeRelic;
    }

    public RelicLocationManager getLocationManager() {
        return locationManager;
    }

    public StructureManager getStructureManager() {
        return structureManager;
    }

    public RelicDisplayManager getDisplayManager() {
        return displayManager;
    }

    public RelicBountyManager getBountyManager() {
        return bountyManager;
    }

    public in.surventure.worldrelics.manager.RelicDuelManager getDuelManager() {
        return duelManager;
    }

    public RelicEvolutionManager getEvolutionManager() {
        return evolutionManager;
    }
}
