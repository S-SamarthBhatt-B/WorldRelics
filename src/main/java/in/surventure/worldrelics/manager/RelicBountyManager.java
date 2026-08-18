package in.surventure.worldrelics.manager;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.RelicRarity;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RelicBountyManager {

    private final WorldRelicsPlugin plugin;
    private UUID activeBountyOwner;
    private double activeBountyAmount = 0.0;

    public RelicBountyManager(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    public double calculateBountyAmount(RelicRarity rarity) {
        if (rarity == null) return 1000.0;
        String key = rarity.name().toLowerCase();
        return plugin.getConfig().getDouble("bounty.rewards." + key, rarity.getDefaultWeight() * 100.0);
    }

    public void applyBounty(Player player, RelicRarity rarity) {
        if (!plugin.getConfig().getBoolean("bounty.enabled", true) || player == null) return;

        double amount = calculateBountyAmount(rarity);
        this.activeBountyOwner = player.getUniqueId();
        this.activeBountyAmount = amount;

        plugin.getLogger().info("[WorldRelics] Relic bounty of $" + amount + " set on player " + player.getName());
    }

    public void handleOwnerKilled(Player owner, Player killer) {
        if (activeBountyOwner == null || !activeBountyOwner.equals(owner.getUniqueId()) || activeBountyAmount <= 0) {
            return;
        }

        double reward = activeBountyAmount;
        this.activeBountyOwner = null;
        this.activeBountyAmount = 0.0;

        if (killer != null) {
            // Reward killer via Vault
            plugin.getHookManager().getVaultHook().deposit(killer, reward);

            plugin.getMessageManager().broadcast("relic-bounty-claimed-broadcast",
                    Placeholder.unparsed("killer_name", killer.getName()),
                    Placeholder.unparsed("owner_name", owner.getName()),
                    Placeholder.unparsed("bounty_amount", String.format("%,.2f", reward))
            );
        }
    }

    public void clearActiveBounty() {
        this.activeBountyOwner = null;
        this.activeBountyAmount = 0.0;
    }

    public UUID getActiveBountyOwner() {
        return activeBountyOwner;
    }

    public double getActiveBountyAmount() {
        return activeBountyAmount;
    }
}
