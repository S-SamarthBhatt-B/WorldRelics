package in.surventure.worldrelics.manager;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.RelicDefinition;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class RelicEvolutionManager {

    private final WorldRelicsPlugin plugin;

    public RelicEvolutionManager(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkEvolutionProgress(ActiveRelic relic, Player owner) {
        if (relic == null || owner == null || !owner.isOnline()) return;

        if (!plugin.getConfig().getBoolean("evolution.enabled", true)) return;

        int currentTier = relic.getTier();
        if (currentTier >= 3) return;

        long heldTimeMs = System.currentTimeMillis() - relic.getClaimedAt();
        long heldMinutes = heldTimeMs / (1000L * 60L);
        int kills = relic.getRelicKills();

        int targetTier = currentTier;

        int t2Kills = plugin.getConfig().getInt("evolution.tier-2.kills", 3);
        long t2Mins = plugin.getConfig().getLong("evolution.tier-2.minutes", 30);
        int t3Kills = plugin.getConfig().getInt("evolution.tier-3.kills", 10);
        long t3Mins = plugin.getConfig().getLong("evolution.tier-3.minutes", 120);

        // Tier 2 Requirement
        if (currentTier == 1 && (kills >= t2Kills || heldMinutes >= t2Mins)) {
            targetTier = 2;
        }

        // Tier 3 Requirement
        if (currentTier == 2 && (kills >= t3Kills || heldMinutes >= t3Mins)) {
            targetTier = 3;
        }

        if (targetTier > currentTier) {
            relic.setTier(targetTier);
            plugin.getDatabaseManager().saveOrUpdateActiveRelic(relic);

            // Update item lore in owner inventory
            updateOwnerRelicItemLore(owner, relic);

            RelicDefinition def = plugin.getConfigManager().getRelicDefinition(relic.getRelicTypeId());
            String relicName = def != null ? def.getDisplayName() : relic.getRelicTypeId();

            String tierRoman = targetTier == 2 ? "II" : "III";

            String broadcastFormat = plugin.getConfig().getString(
                    "evolution.broadcast-message",
                    "<gradient:#FFD700:#FF8C00><bold>⚡ RELIC EVOLVED! ⚡</bold></gradient>\n<yellow><player_name></yellow>'s <relic_name> has evolved to <gold><bold>TIER <tier></bold></gold>!"
            );

            // Broadcast Evolution
            Bukkit.broadcast(plugin.getMessageManager().getMiniMessage().deserialize(
                    broadcastFormat,
                    Placeholder.unparsed("player_name", owner.getName()),
                    Placeholder.parsed("relic_name", relicName),
                    Placeholder.unparsed("tier", tierRoman)
            ));

            owner.getWorld().playSound(owner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 0.8f);
            owner.getWorld().playSound(owner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        }
    }

    public void recordOwnerKill(Player owner) {
        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic != null && relic.getOwnerUuid() != null && relic.getOwnerUuid().equals(owner.getUniqueId())) {
            relic.setRelicKills(relic.getRelicKills() + 1);
            updateOwnerRelicItemLore(owner, relic);
            checkEvolutionProgress(relic, owner);
        }
    }

    public void resetEvolutionOnDeath(ActiveRelic relic) {
        if (relic != null) {
            relic.setTier(1);
            relic.setRelicKills(0);
        }
    }

    public void updateOwnerRelicItemLore(Player owner, ActiveRelic relic) {
        if (owner == null || relic == null || !owner.isOnline()) return;
        RelicDefinition def = plugin.getConfigManager().getRelicDefinition(relic.getRelicTypeId());
        if (def == null) return;

        for (org.bukkit.inventory.ItemStack item : owner.getInventory().getContents()) {
            if (item != null && plugin.getItemFactory().isRelicItem(item)) {
                java.util.UUID itemUuid = plugin.getItemFactory().getRelicUuid(item);
                if (relic.getRelicUuid().equals(itemUuid)) {
                    org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.lore(plugin.getItemFactory().buildRelicLore(relic, def));
                        item.setItemMeta(meta);
                    }
                }
            }
        }
    }
}
