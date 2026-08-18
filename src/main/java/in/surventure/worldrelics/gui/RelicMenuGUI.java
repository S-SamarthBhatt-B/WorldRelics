package in.surventure.worldrelics.gui;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.RelicDefinition;
import in.surventure.worldrelics.model.RelicState;
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

import java.util.ArrayList;
import java.util.List;

public class RelicMenuGUI implements Listener {

    private final WorldRelicsPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final String GUI_TITLE = "<gradient:#FF8C00:#FFD700><bold>WorldRelics Control Center</bold></gradient>";

    public RelicMenuGUI(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, miniMessage.deserialize(GUI_TITLE));

        // Fill background glass
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(Component.text(" "));
            glass.setItemMeta(glassMeta);
        }

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic != null && relic.getStatus() != RelicState.EXPIRED && relic.getStatus() != RelicState.NO_RELIC) {
            RelicDefinition def = plugin.getConfigManager().getRelicDefinition(relic.getRelicTypeId());
            Material mat = def != null ? def.getMaterial() : Material.NETHER_STAR;

            ItemStack relicSlot = new ItemStack(mat);
            ItemMeta meta = relicSlot.getItemMeta();
            if (meta != null) {
                meta.displayName(def != null ? miniMessage.deserialize(def.getDisplayName()) : Component.text(relic.getRelicTypeId()));
                List<Component> lore = new ArrayList<>();
                lore.add(miniMessage.deserialize("<gray>Rarity: </gray>" + relic.getRarity().getDisplayName()));
                lore.add(miniMessage.deserialize("<gray>Owner: </gray><yellow>" + (relic.getOwnerName() != null ? relic.getOwnerName() : "Unclaimed") + "</yellow>"));
                lore.add(miniMessage.deserialize("<gray>Status: </gray><green>" + relic.getStatus() + "</green>"));
                lore.add(miniMessage.deserialize("<gray>Time Remaining: </gray><aqua>" + plugin.getRelicManager().getDisplayManager().formatRemainingTime(relic.getRemainingMillis()) + "</aqua>"));
                meta.lore(lore);
                relicSlot.setItemMeta(meta);
            }
            inv.setItem(13, relicSlot);
        } else {
            ItemStack noRelicSlot = new ItemStack(Material.BARRIER);
            ItemMeta meta = noRelicSlot.getItemMeta();
            if (meta != null) {
                meta.displayName(miniMessage.deserialize("<red><bold>No Active Relic</bold></red>"));
                List<Component> lore = List.of(miniMessage.deserialize("<gray>The world is waiting for a new relic to awaken...</gray>"));
                meta.lore(lore);
                noRelicSlot.setItemMeta(meta);
            }
            inv.setItem(13, noRelicSlot);
        }

        // History Button
        ItemStack historySlot = new ItemStack(Material.BOOK);
        ItemMeta histMeta = historySlot.getItemMeta();
        if (histMeta != null) {
            histMeta.displayName(miniMessage.deserialize("<gold><bold>Relic History Log</bold></gold>"));
            histMeta.lore(List.of(miniMessage.deserialize("<gray>Click to view previous server relic claims.</gray>")));
            historySlot.setItemMeta(histMeta);
        }
        inv.setItem(22, historySlot);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().equals(miniMessage.deserialize(GUI_TITLE))) {
            event.setCancelled(true);
            if (event.getRawSlot() == 22 && event.getWhoClicked() instanceof Player player) {
                plugin.getHistoryGUI().openHistory(player);
            }
        }
    }
}
