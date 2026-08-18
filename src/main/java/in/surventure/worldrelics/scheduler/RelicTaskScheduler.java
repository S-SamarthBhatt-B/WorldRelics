package in.surventure.worldrelics.scheduler;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.RelicDefinition;
import in.surventure.worldrelics.model.RelicState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class RelicTaskScheduler {

    private final WorldRelicsPlugin plugin;
    private BukkitTask mainTickerTask;

    public RelicTaskScheduler(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    public void startTasks() {
        cancelTasks();

        mainTickerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
            if (relic == null || relic.getStatus() == RelicState.NO_RELIC || relic.getStatus() == RelicState.EXPIRED) {
                if (plugin.getRelicManager().canSpawnRelic(true, true)) {
                    plugin.getRelicManager().triggerNewRelicSpawnCycle(false);
                }
                return;
            }

            // Check Expiration
            if (relic.isExpired()) {
                plugin.getLogger().info("[WorldRelics] Relic timer expired! Triggering expiration cycle...");
                plugin.getRelicManager().handleRelicExpiration(true);
                return;
            }

            // Update ActionBar & BossBar
            plugin.getRelicManager().getDisplayManager().updateDisplays();

            // Apply Passive Potion Effects to current owner if online
            if (relic.getOwnerUuid() != null) {
                Player owner = Bukkit.getPlayer(relic.getOwnerUuid());
                if (owner != null && owner.isOnline()) {
                    RelicDefinition def = plugin.getConfigManager().getRelicDefinition(relic.getRelicTypeId());
                    if (def != null) {
                        plugin.getAbilityManager().applyPassiveEffects(owner, def);
                    }
                }
            }
        }, 20L, 40L);
    }

    public void cancelTasks() {
        if (mainTickerTask != null) {
            mainTickerTask.cancel();
            mainTickerTask = null;
        }
    }
}
