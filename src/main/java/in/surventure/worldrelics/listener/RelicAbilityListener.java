package in.surventure.worldrelics.listener;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.RelicDefinition;
import in.surventure.worldrelics.model.RelicState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class RelicAbilityListener implements Listener {

    private final WorldRelicsPlugin plugin;

    public RelicAbilityListener(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !plugin.getItemFactory().isRelicItem(item)) {
            return;
        }

        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic == null || relic.getStatus() != RelicState.CLAIMED || !player.getUniqueId().equals(relic.getOwnerUuid())) {
            return;
        }

        RelicDefinition def = plugin.getConfigManager().getRelicDefinition(relic.getRelicTypeId());
        if (def == null || def.getAbilityConfig().isEmpty()) {
            return;
        }

        // Trigger first configured active ability
        for (Map.Entry<String, Object> entry : def.getAbilityConfig().entrySet()) {
            String abilityName = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> cfg = (Map<String, Object>) entry.getValue();
            boolean enabled = cfg == null || !cfg.containsKey("enabled") || Boolean.TRUE.equals(cfg.get("enabled"));

            if (enabled) {
                event.setCancelled(true);
                plugin.getAbilityManager().triggerAbility(player, abilityName, cfg);
                break;
            }
        }
    }
}
