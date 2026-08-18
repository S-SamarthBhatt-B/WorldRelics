package in.surventure.worldrelics.gui;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.RelicDefinition;
import in.surventure.worldrelics.model.RelicHistoryEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RelicHistoryGUI implements Listener {

    private final WorldRelicsPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final String GUI_TITLE = "<gradient:#FF8C00:#FFD700><bold>Relic History Archive</bold></gradient>";

    public RelicHistoryGUI(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    public void openHistory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, miniMessage.deserialize(GUI_TITLE));

        plugin.getDatabaseManager().getHistoryAsync().thenAccept(history -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                int slot = 0;
                for (RelicHistoryEntry entry : history) {
                    if (slot >= 27) break;
                    RelicDefinition def = plugin.getConfigManager().getRelicDefinition(entry.getRelicTypeId());
                    Material mat = def != null ? def.getMaterial() : Material.PAPER;

                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.displayName(def != null ? miniMessage.deserialize(def.getDisplayName()) : Component.text(entry.getRelicTypeId()));
                        List<Component> lore = new ArrayList<>();
                        lore.add(miniMessage.deserialize("<gray>Owner: </gray><yellow>" + (entry.getOwnerName() != null ? entry.getOwnerName() : "Unclaimed") + "</yellow>"));
                        lore.add(miniMessage.deserialize("<gray>Claimed: </gray><dark_gray>" + sdf.format(new Date(entry.getClaimedAt())) + "</dark_gray>"));
                        lore.add(miniMessage.deserialize("<gray>Expired: </gray><dark_gray>" + sdf.format(new Date(entry.getExpiredAt())) + "</dark_gray>"));
                        lore.add(miniMessage.deserialize("<gray>Outcome: </gray><red>" + entry.getFinalStatus() + "</red>"));
                        meta.lore(lore);
                        item.setItemMeta(meta);
                    }
                    inv.setItem(slot++, item);
                }

                // Back Button
                ItemStack back = new ItemStack(Material.ARROW);
                ItemMeta backMeta = back.getItemMeta();
                if (backMeta != null) {
                    backMeta.displayName(miniMessage.deserialize("<yellow>« Back to Control Center</yellow>"));
                    back.setItemMeta(backMeta);
                }
                inv.setItem(31, back);

                player.openInventory(inv);
            });
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().equals(miniMessage.deserialize(GUI_TITLE))) {
            event.setCancelled(true);
            if (event.getRawSlot() == 31 && event.getWhoClicked() instanceof Player player) {
                plugin.getMenuGUI().openMenu(player);
            }
        }
    }
}
