package in.surventure.worldrelics.listener;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.RelicState;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class RelicItemListener implements Listener {

    private final WorldRelicsPlugin plugin;

    public RelicItemListener(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Item itemEntity = event.getItem();
        ItemStack item = itemEntity.getItemStack();

        if (!plugin.getItemFactory().isRelicItem(item)) return;

        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic == null) {
            itemEntity.remove();
            event.setCancelled(true);
            return;
        }

        UUID itemUuid = plugin.getItemFactory().getRelicUuid(item);
        if (itemUuid == null || !itemUuid.equals(relic.getRelicUuid())) {
            itemEntity.remove();
            event.setCancelled(true);
            player.sendMessage(net.kyori.adventure.text.Component.text("This relic item is invalid and has dissolved.", net.kyori.adventure.text.format.NamedTextColor.RED));
            return;
        }

        // Claim relic if unclaimed or transferred
        if (relic.getStatus() == RelicState.AVAILABLE || relic.getOwnerUuid() == null || !relic.getOwnerUuid().equals(player.getUniqueId())) {
            boolean success = plugin.getRelicManager().claimRelic(player, item);
            if (!success) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (plugin.getItemFactory().isRelicItem(event.getEntity().getItemStack())) {
            // Never allow relic item entities in wild to despawn
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        for (ItemStack matrix : event.getInventory().getMatrix()) {
            if (matrix != null && plugin.getItemFactory().isRelicItem(matrix)) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player p) {
                    p.sendMessage(net.kyori.adventure.text.Component.text("Relic items cannot be used in crafting recipes!", net.kyori.adventure.text.format.NamedTextColor.RED));
                }
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (event.getInventory().getType() == InventoryType.ANVIL ||
                event.getInventory().getType() == InventoryType.GRINDSTONE ||
                event.getInventory().getType() == InventoryType.ENCHANTING) {
            if ((current != null && plugin.getItemFactory().isRelicItem(current)) ||
                    (cursor != null && plugin.getItemFactory().isRelicItem(cursor))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.getRelicManager().getStructureManager().isChanneling(player)) {
                plugin.getRelicManager().getStructureManager().cancelChanneling(player, "❌ Channeling interrupted by damage!");
            }
        }
    }

    @EventHandler
    public void onPedestalInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Location clickedLoc = event.getClickedBlock().getLocation();
        Location structureCenter = plugin.getRelicManager().getStructureManager().getActiveStructureCenter();
        if (structureCenter == null) return;

        // Check if clicked block is the pedestal (center at Y+2)
        Location pedestalLoc = structureCenter.clone().add(0, 2, 0);
        if (clickedLoc.getBlockX() == pedestalLoc.getBlockX() &&
                clickedLoc.getBlockY() == pedestalLoc.getBlockY() &&
                clickedLoc.getBlockZ() == pedestalLoc.getBlockZ()) {

            event.setCancelled(true);
            Player player = event.getPlayer();
            plugin.getRelicManager().getStructureManager().startChanneling(player);
        }
    }

    @EventHandler
    public void onPlayerInteractLocator(org.bukkit.event.player.PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // 1. Relic Locator Compass Handling
        if (plugin.getItemFactory().isRelicLocatorItem(item)) {
            event.setCancelled(true);

            ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
            if (relic == null || relic.getStatus() == RelicState.NO_RELIC || relic.getStatus() == RelicState.EXPIRED) {
                plugin.getMessageManager().sendMessage(player, "relic-locate-no-relic");
                return;
            }

            // Dissolve Relic Locator if relic has already been claimed!
            if (relic.getOwnerUuid() != null || relic.getStatus() == RelicState.CLAIMED) {
                item.setAmount(0);
                player.sendMessage(net.kyori.adventure.text.Component.text("The relic has already been claimed by a player! The Relic Locator dissolves into dust.", net.kyori.adventure.text.format.NamedTextColor.RED));
                return;
            }

            Location relicLoc = relic.getLocation();
            if (relicLoc == null) {
                plugin.getMessageManager().sendMessage(player, "relic-locate-no-relic");
                return;
            }

            if (item.getItemMeta() instanceof org.bukkit.inventory.meta.CompassMeta compassMeta) {
                compassMeta.setLodestone(relicLoc);
                compassMeta.setLodestoneTracked(false);
                item.setItemMeta(compassMeta);
            }

            in.surventure.worldrelics.model.RelicDefinition def = plugin.getConfigManager().getRelicDefinition(relic.getRelicTypeId());
            String relicName = def != null ? def.getDisplayName() : relic.getRelicTypeId();

            Location from = player.getLocation();
            double dist = from.distance(relicLoc);
            String direction = in.surventure.worldrelics.util.LocationUtils.getFuzzyDirectionString(from, relicLoc);
            String formattedDist = String.format("%,d", (int) dist);

            plugin.getMessageManager().sendMessage(player, "relic-locate-unclaimed",
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed("relic_name", relicName),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("x", String.valueOf(relicLoc.getBlockX())),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("y", String.valueOf(relicLoc.getBlockY())),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("z", String.valueOf(relicLoc.getBlockZ())),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("world", relicLoc.getWorld().getName()),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("direction", direction),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("distance", formattedDist)
            );
            return;
        }

        // 2. Owner Locator Compass Handling
        if (plugin.getItemFactory().isOwnerLocatorItem(item)) {
            event.setCancelled(true);

            ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
            if (relic == null || relic.getStatus() != RelicState.CLAIMED || relic.getOwnerUuid() == null) {
                item.setAmount(0);
                player.sendMessage(net.kyori.adventure.text.Component.text("There is currently no active relic owner! The Owner Tracker dissolves into dust.", net.kyori.adventure.text.format.NamedTextColor.RED));
                return;
            }

            Player owner = org.bukkit.Bukkit.getPlayer(relic.getOwnerUuid());
            if (owner == null || !owner.isOnline()) {
                player.sendMessage(net.kyori.adventure.text.Component.text("The relic owner is currently offline.", net.kyori.adventure.text.format.NamedTextColor.RED));
                return;
            }

            Location ownerLoc = owner.getLocation();
            if (item.getItemMeta() instanceof org.bukkit.inventory.meta.CompassMeta compassMeta) {
                compassMeta.setLodestone(ownerLoc);
                compassMeta.setLodestoneTracked(false);
                item.setItemMeta(compassMeta);
            }

            Location from = player.getLocation();
            double dist = from.distance(ownerLoc);
            String direction = in.surventure.worldrelics.util.LocationUtils.getFuzzyDirectionString(from, ownerLoc);
            String formattedDist = String.format("%,d", (int) dist);

            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(
                    "<gold>🎯 Relic Owner <yellow><owner></yellow> is at <yellow>X: <x>, Y: <y>, Z: <z></yellow> in <aqua><world></aqua> (<gold><direction></gold>, <yellow><distance> blocks away</yellow>).</gold>",
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("owner", owner.getName()),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("x", String.valueOf(ownerLoc.getBlockX())),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("y", String.valueOf(ownerLoc.getBlockY())),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("z", String.valueOf(ownerLoc.getBlockZ())),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("world", ownerLoc.getWorld().getName()),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("direction", direction),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("distance", formattedDist)
            ));
        }
    }
}
