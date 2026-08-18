package in.surventure.worldrelics.listener;

import in.surventure.worldrelics.WorldRelicsPlugin;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class RelicProtectionListener implements Listener {

    private final WorldRelicsPlugin plugin;

    public RelicProtectionListener(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getConfig().getBoolean("structure.protection.prevent-block-break", true)) {
            if (plugin.getRelicManager().getStructureManager().isWithinProtectedArea(event.getBlock().getLocation())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(net.kyori.adventure.text.Component.text("This structure is protected by ancient relic energy!", net.kyori.adventure.text.format.NamedTextColor.RED));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getConfig().getBoolean("structure.protection.prevent-block-place", true)) {
            if (plugin.getRelicManager().getStructureManager().isWithinProtectedArea(event.getBlock().getLocation())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(net.kyori.adventure.text.Component.text("You cannot place blocks near an active relic structure!", net.kyori.adventure.text.format.NamedTextColor.RED));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        if (plugin.getConfig().getBoolean("structure.protection.prevent-explosions", true)) {
            event.blockList().removeIf(block -> plugin.getRelicManager().getStructureManager().isWithinProtectedArea(block.getLocation()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireIgnite(BlockIgniteEvent event) {
        if (plugin.getConfig().getBoolean("structure.protection.prevent-fire", true)) {
            if (plugin.getRelicManager().getStructureManager().isWithinProtectedArea(event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (plugin.getConfig().getBoolean("structure.protection.prevent-fire", true)) {
            if (plugin.getRelicManager().getStructureManager().isWithinProtectedArea(event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
        }
    }
}
