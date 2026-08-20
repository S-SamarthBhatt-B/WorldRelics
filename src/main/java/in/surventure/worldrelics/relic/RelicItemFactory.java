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

    public List<Component> buildRelicLore(ActiveRelic activeRelic, RelicDefinition def) {
        List<Component> lore = new ArrayList<>();
        int tier = activeRelic != null ? activeRelic.getTier() : 1;

        String tierBadge = switch (tier) {
            case 2 -> plugin.getConfig().getString("evolution.tier-2.badge", "<gold>[Tier II Evolved]</gold>");
            case 3 -> plugin.getConfig().getString("evolution.tier-3.badge", "<light_purple><bold>[Tier III Mastered]</bold></light_purple>");
            default -> plugin.getConfig().getString("evolution.tier-1-badge", "<gray>[Tier I]</gray>");
        };

        lore.add(miniMessage.deserialize("<gray>Mastery Tier:</gray> " + tierBadge));
        if (activeRelic != null && activeRelic.getRelicKills() > 0) {
            lore.add(miniMessage.deserialize("<gray>Slayed Foes:</gray> <red>⚔ " + activeRelic.getRelicKills() + " Kills</red>"));
        }
        lore.add(Component.text(""));

        if (def != null) {
            for (String line : def.getLore()) {
                lore.add(miniMessage.deserialize(line));
            }
        }

        return lore;
    }

    public ItemStack createRelicItem(ActiveRelic activeRelic, RelicDefinition def) {
        ItemStack item = new ItemStack(def.getMaterial(), 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(miniMessage.deserialize(def.getDisplayName()));
            meta.lore(buildRelicLore(activeRelic, def));

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
            String nameStr = plugin.getConfig().getString("items.relic-locator.display-name", "<gradient:#FFD700:#FF8C00><bold>Relic Locator</bold></gradient>");
            meta.displayName(miniMessage.deserialize(nameStr));

            List<String> rawLore = plugin.getConfig().getStringList("items.relic-locator.lore");
            List<Component> lore = new ArrayList<>();
            if (rawLore != null && !rawLore.isEmpty()) {
                for (String line : rawLore) {
                    lore.add(miniMessage.deserialize(line));
                }
            } else {
                lore = List.of(
                        miniMessage.deserialize("<gray>An ancient mystical compass tuned to world relics.</gray>"),
                        Component.text(""),
                        miniMessage.deserialize("<yellow><bold>Right-Click</bold> to locate the active relic.</yellow>")
                );
            }
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
            String nameStr = plugin.getConfig().getString("items.owner-locator.display-name", "<gradient:#FF4500:#FFA500><bold>Relic Owner Tracker</bold></gradient>");
            meta.displayName(miniMessage.deserialize(nameStr));

            List<String> rawLore = plugin.getConfig().getStringList("items.owner-locator.lore");
            List<Component> lore = new ArrayList<>();
            if (rawLore != null && !rawLore.isEmpty()) {
                for (String line : rawLore) {
                    lore.add(miniMessage.deserialize(line));
                }
            } else {
                lore = List.of(
                        miniMessage.deserialize("<gray>An enchanted compass tuned to the current relic owner.</gray>"),
                        Component.text(""),
                        miniMessage.deserialize("<yellow><bold>Right-Click</bold> to track the live position of the owner.</yellow>")
                );
            }
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

    public ItemStack createGuideBookItem() {
        ItemStack book = new ItemStack(org.bukkit.Material.WRITTEN_BOOK, 1);
        if (book.getItemMeta() instanceof org.bukkit.inventory.meta.BookMeta bookMeta) {
            String titleStr = plugin.getConfig().getString("guide-book.title", "<aqua><bold>WorldRelics Guide</bold></aqua>");
            String authorStr = plugin.getConfig().getString("guide-book.author", "<dark_gray>The Ancients</dark_gray>");

            bookMeta.title(miniMessage.deserialize(titleStr));
            bookMeta.author(miniMessage.deserialize(authorStr));

            List<String> rawPages = plugin.getConfig().getStringList("guide-book.pages");
            List<Component> pages = new ArrayList<>();
            if (rawPages != null && !rawPages.isEmpty()) {
                for (String pageText : rawPages) {
                    pages.add(miniMessage.deserialize(pageText));
                }
            } else {
                pages = List.of(
                        miniMessage.deserialize("<dark_blue><bold>━━ WORLDRELICS ━━</dark_blue>\n\n<dark_gray><bold>ONE WORLD.\nONE RELIC.\nONE OWNER.</bold></dark_gray>\n\nOnly ONE relic exists at a time.\n\nFind it.\nClaim it.\nProtect it."),
                        miniMessage.deserialize("<dark_blue><bold>━━ FIND THE RELIC ━━</dark_blue>\n\nA relic awakens somewhere in the world.\n\nIt is hidden inside a special structure.\n\nBuy a <dark_gray><bold>Relic Locator</bold></dark_gray> from the shop to track it."),
                        miniMessage.deserialize("<dark_blue><bold>━━ RELIC LOCATOR ━━</dark_blue>\n\nUse the <dark_gray><bold>Relic Locator</bold></dark_gray> to discover the relic's approximate direction and distance.\n\n<dark_red>The exact location remains hidden.</dark_red>\n\nThe hunt is yours to complete."),
                        miniMessage.deserialize("<dark_blue><bold>━━ CLAIM THE RELIC ━━</dark_blue>\n\nFind the relic altar.\n\n<dark_gray><bold>Right-click the pedestal</bold></dark_gray> to claim it.\n\nThe first player to claim it becomes the <dark_blue><bold>OWNER.</bold></dark_blue>\n\n<dark_red>Only ONE owner can exist.</dark_red>"),
                        miniMessage.deserialize("<dark_blue><bold>━━ RELIC POWERS ━━</dark_blue>\n\nEvery relic has unique powers.\n\n<dark_aqua><bold>PASSIVE</bold></dark_aqua>\nAlways active.\n\n<dark_purple><bold>ACTIVE</bold></dark_purple>\nPowerful abilities with cooldowns."),
                        miniMessage.deserialize("<dark_blue><bold>━━ PROTECT IT ━━</dark_blue>\n\nThe relic makes you a target.\n\nOther players may hunt you.\n\n<dark_red><bold>IF YOU DIE, THE RELIC DROPS.</bold></dark_red>\n\nAnother player can claim it."),
                        miniMessage.deserialize("<dark_blue><bold>━━ RELIC LIFETIME ━━</dark_blue>\n\n<dark_blue><bold>10–15 MINECRAFT DAYS</bold></dark_blue>\n\nWhen the timer ends, the relic is <dark_red>destroyed</dark_red>.\n\nA new relic awakens."),
                        miniMessage.deserialize("<dark_blue><bold>━━ COMMANDS ━━</dark_blue>\n\n<dark_gray><bold>/wr status</bold></dark_gray>\nActive relic & owner\n\n<dark_gray><bold>/wr info</bold></dark_gray>\nRelic information\n\n<dark_gray><bold>/wr menu</bold></dark_gray>\nOpen relic menu"),
                        miniMessage.deserialize("<dark_blue><bold>━━ THE ANCIENTS' LAW ━━</dark_blue>\n\n<dark_blue><bold>ONE WORLD.</bold></dark_blue>\n\n<dark_gray><bold>ONE RELIC.</bold></dark_gray>\n\n<dark_aqua><bold>ONE OWNER.</bold></dark_aqua>\n\n<dark_red><bold>Claim it before someone else does.</bold></dark_red>")
                );
            }
            bookMeta.addPages(pages.toArray(new Component[0]));
            bookMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "item_type"), PersistentDataType.STRING, "guide_book");
            book.setItemMeta(bookMeta);
        }
        return book;
    }

    public boolean isGuideBookItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        NamespacedKey itemTypeKey = new NamespacedKey(plugin, "item_type");
        if (meta.getPersistentDataContainer().has(itemTypeKey, PersistentDataType.STRING)) {
            String val = meta.getPersistentDataContainer().get(itemTypeKey, PersistentDataType.STRING);
            return "guide_book".equalsIgnoreCase(val);
        }
        return false;
    }
}
