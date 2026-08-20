package in.surventure.worldrelics.relic.ability;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;

public class InfernoBurstAbility implements RelicAbility {

    @Override
    public String getName() {
        return "inferno-burst";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        int tier = relic != null ? relic.getTier() : 1;

        double radius = config.containsKey("radius") ? ((Number) config.get("radius")).doubleValue() : (5.0 + (tier * 4.0));
        int fireSec = config.containsKey("fire-seconds") ? ((Number) config.get("fire-seconds")).intValue() : (4 + (tier * 4));
        double damage = 3.0 + (tier * 4.0);

        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(0, 1, 0), 60 * tier, radius / 2, 0.5, radius / 2, 0.2);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 0.9f);

        int count = 0;
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius)) {
            if (entity instanceof LivingEntity target && !entity.equals(player)) {
                target.setFireTicks(fireSec * 20);
                target.damage(damage, player);
                count++;
            }
        }

        if (tier >= 2) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE, fireSec * 40, 0));
        }

        String tierBadge = tier == 3 ? " [Tier III Mastered]" : (tier == 2 ? " [Tier II Evolved]" : "");
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#FF1493:#FF4500><bold>🌋 INFERNO BURST" + tierBadge + "!</bold></gradient> Ignited " + count + " targets."));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 50;
    }
}
