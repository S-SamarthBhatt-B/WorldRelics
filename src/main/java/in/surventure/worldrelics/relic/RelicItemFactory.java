package in.surventure.worldrelics.relic;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.RelicDefinition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RelicItemFactory {

    private final WorldRelicsPlugin plugin;
    private final NamespacedKey relicIdKey;
    private final NamespacedKey relicTypeKey;
    private final NamespacedKey relicOwnerKey;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public RelicItemFactory(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
        this.relicIdKey = new NamespacedKey(plugin, "relic_id");
        this.relicTypeKey = new NamespacedKey(plugin, "relic_type");
        this.relicOwnerKey = new NamespacedKey(plugin, "relic_owner");
    }

    public ItemStack createRelicItem(ActiveRelic activeRelic, RelicDefinition def) {
        ItemStack item = new ItemStack(def.getMaterial(), 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(miniMessage.deserialize(def.getDisplayName()));

            List<Component> lore = new ArrayList<>();
            for (String line : def.getLore()) {
                lore.add(miniMessage.deserialize(line));
            }
            meta.lore(lore);

            if (def.getCustomModelData() > 0) {
                meta.setCustomModelData(def.getCustomModelData());
            }

            // Set Persistent Data Container
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(relicIdKey, PersistentDataType.STRING, activeRelic.getRelicUuid().toString());
            pdc.set(relicTypeKey, PersistentDataType.STRING, def.getId());
            if (activeRelic.getOwnerUuid() != null) {
                pdc.set(relicOwnerKey, PersistentDataType.STRING, activeRelic.getOwnerUuid().toString());
            } else {
                pdc.set(relicOwnerKey, PersistentDataType.STRING, "UNCLAIMED");
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isRelicItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(relicIdKey, PersistentDataType.STRING);
    }

    public UUID getRelicUuid(ItemStack item) {
        if (!isRelicItem(item)) return null;
        ItemMeta meta = item.getItemMeta();
        String uuidStr = meta.getPersistentDataContainer().get(relicIdKey, PersistentDataType.STRING);
        try {
            return uuidStr != null ? UUID.fromString(uuidStr) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getRelicTypeId(ItemStack item) {
        if (!isRelicItem(item)) return null;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(relicTypeKey, PersistentDataType.STRING);
    }

    public void updateOwnerData(ItemStack item, UUID ownerUuid) {
        if (!isRelicItem(item)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (ownerUuid != null) {
                pdc.set(relicOwnerKey, PersistentDataType.STRING, ownerUuid.toString());
            } else {
                pdc.set(relicOwnerKey, PersistentDataType.STRING, "UNCLAIMED");
            }
            item.setItemMeta(meta);
        }
    }

    public NamespacedKey getRelicIdKey() {
        return relicIdKey;
    }

    public NamespacedKey getRelicTypeKey() {
        return relicTypeKey;
    }

    public NamespacedKey getRelicOwnerKey() {
        return relicOwnerKey;
    }

    public ItemStack createRelicLocatorItem() {
        ItemStack item = new ItemStack(org.bukkit.Material.COMPASS, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<gradient:#FFD700:#FF8C00><bold>Relic Locator</bold></gradient>"));
            List<Component> lore = List.of(
                    miniMessage.deserialize("<gray>An ancient mystical compass tuned to world relics.</gray>"),
                    Component.text(""),
                    miniMessage.deserialize("<yellow><bold>Right-Click</bold> to locate the active relic.</yellow>")
            );
            meta.lore(lore);

            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "item_type"), PersistentDataType.STRING, "relic_locator");
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isRelicLocatorItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        NamespacedKey itemTypeKey = new NamespacedKey(plugin, "item_type");
        if (meta.getPersistentDataContainer().has(itemTypeKey, PersistentDataType.STRING)) {
            String val = meta.getPersistentDataContainer().get(itemTypeKey, PersistentDataType.STRING);
            return "relic_locator".equalsIgnoreCase(val);
        }
        return false;
    }

    public ItemStack createOwnerLocatorItem() {
        ItemStack item = new ItemStack(org.bukkit.Material.COMPASS, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<gradient:#FF4500:#FFA500><bold>Relic Owner Tracker</bold></gradient>"));
            List<Component> lore = List.of(
                    miniMessage.deserialize("<gray>An enchanted compass tuned to the current relic owner.</gray>"),
                    Component.text(""),
                    miniMessage.deserialize("<yellow><bold>Right-Click</bold> to track the live position of the owner.</yellow>")
            );
            meta.lore(lore);

            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "item_type"), PersistentDataType.STRING, "owner_locator");
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isOwnerLocatorItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        NamespacedKey itemTypeKey = new NamespacedKey(plugin, "item_type");
        if (meta.getPersistentDataContainer().has(itemTypeKey, PersistentDataType.STRING)) {
            String val = meta.getPersistentDataContainer().get(itemTypeKey, PersistentDataType.STRING);
            return "owner_locator".equalsIgnoreCase(val);
        }
        return false;
    }
}
