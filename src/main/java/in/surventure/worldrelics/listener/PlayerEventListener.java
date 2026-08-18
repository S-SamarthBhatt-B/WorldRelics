package in.surventure.worldrelics.listener;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.RelicState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerEventListener implements Listener {

    private final WorldRelicsPlugin plugin;

    public PlayerEventListener(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic == null || relic.getOwnerUuid() == null) return;

        if (relic.getOwnerUuid().equals(player.getUniqueId())) {
            // Keep relic item in drops so natural drop occurs
            plugin.getRelicManager().handleOwnerDeath(player, player.getLocation());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic != null) {
            // Scan player inventory for duplicate or obsolete relics
            validatePlayerRelics(player, relic);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic != null && relic.getOwnerUuid() != null && relic.getOwnerUuid().equals(player.getUniqueId())) {
            boolean dropOnLogout = plugin.getConfig().getBoolean("disconnect.drop-if-logout", false);
            if (dropOnLogout) {
                plugin.getRelicManager().handleOwnerDeath(player, player.getLocation());
            }
        }
    }

    private void validatePlayerRelics(Player player, ActiveRelic relic) {
        UUID validUuid = relic.getRelicUuid();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && plugin.getItemFactory().isRelicItem(item)) {
                UUID itemUuid = plugin.getItemFactory().getRelicUuid(item);
                if (itemUuid == null || !itemUuid.equals(validUuid)) {
                    // Invalid duplicate item -> remove
                    item.setAmount(0);
                    player.sendMessage(net.kyori.adventure.text.Component.text("An invalid/duplicate Relic item was removed from your inventory.", net.kyori.adventure.text.format.NamedTextColor.RED));
                }
            }
        }
    }
}
