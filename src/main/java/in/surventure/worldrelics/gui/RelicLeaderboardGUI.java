package in.surventure.worldrelics.gui;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.PlayerStats;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class RelicLeaderboardGUI implements Listener {

    private final WorldRelicsPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public RelicLeaderboardGUI(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize("<gradient:#FFD700:#FF8C00><bold>🏆 Top Relic Hunters</bold></gradient>"));

        // Fill background
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(Component.text(" "));
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, glass);
        }

        // Fetch top players from SQLite DB
        List<PlayerStats> topPlayers = plugin.getDatabaseManager().getTopPlayers(10);
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};

        for (int i = 0; i < Math.min(topPlayers.size(), slots.length); i++) {
            PlayerStats stats = topPlayers.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            if (headMeta != null) {
                headMeta.setOwningPlayer(Bukkit.getOfflinePlayer(stats.getPlayerUuid()));
                headMeta.displayName(miniMessage.deserialize("<gold>#" + (i + 1) + " <yellow>" + stats.getPlayerName() + "</yellow></gold>"));
                List<Component> lore = new ArrayList<>();
                lore.add(miniMessage.deserialize("<gray>Relics Claimed: </gray><gold>" + stats.getRelicsClaimed() + "</gold>"));
                lore.add(miniMessage.deserialize("<gray>Relics Lost: </gray><red>" + stats.getRelicsLost() + "</red>"));
                lore.add(miniMessage.deserialize("<gray>Relic Kills: </gray><green>" + stats.getRelicKills() + "</green>"));
                headMeta.lore(lore);
                head.setItemMeta(headMeta);
            }
            inv.setItem(slots[i], head);
        }

        // Close Button
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta bMeta = barrier.getItemMeta();
        if (bMeta != null) {
            bMeta.displayName(miniMessage.deserialize("<red><bold>Close</bold></red>"));
            barrier.setItemMeta(bMeta);
        }
        inv.setItem(49, barrier);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().equals(miniMessage.deserialize("<gradient:#FFD700:#FF8C00><bold>🏆 Top Relic Hunters</bold></gradient>"))) {
            event.setCancelled(true);
            if (event.getSlot() == 49) {
                event.getWhoClicked().closeInventory();
            }
        }
    }
}
