package in.surventure.worldrelics.relic.ability;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;

public class StormStrikeAbility implements RelicAbility {

    @Override
    public String getName() {
        return "storm-strike";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        int tier = relic != null ? relic.getTier() : 1;

        double radius = config.containsKey("radius") ? ((Number) config.get("radius")).doubleValue() : (7.0 + (tier * 3.0));
        double damage = config.containsKey("damage") ? ((Number) config.get("damage")).doubleValue() : (6.0 + (tier * 6.0));
        int strikeCount = tier;

        Location targetLoc = player.getTargetBlockExact(30) != null ?
                player.getTargetBlockExact(30).getLocation() : player.getLocation();

        for (int i = 0; i < strikeCount; i++) {
            player.getWorld().strikeLightning(targetLoc);
        }

        int struck = 0;
        for (Entity entity : player.getWorld().getNearbyEntities(targetLoc, radius, radius, radius)) {
            if (entity instanceof LivingEntity target && !entity.equals(player)) {
                target.damage(damage, player);
                if (tier >= 2) {
                    target.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOW, 100 * tier, tier - 1));
                }
                struck++;
            }
        }

        player.getWorld().playSound(targetLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);

        String tierBadge = tier == 3 ? " [Tier III Mastered]" : (tier == 2 ? " [Tier II Evolved]" : "");
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#00FFFF:#1E90FF><bold>⚡ STORM STRIKE" + tierBadge + "!</bold></gradient> Struck " + struck + " targets."));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 60;
    }
}
