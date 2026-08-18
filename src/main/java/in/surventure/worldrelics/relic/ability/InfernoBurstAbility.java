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
        double radius = config.containsKey("radius") ? ((Number) config.get("radius")).doubleValue() : 6.0;
        int fireSec = config.containsKey("fire-seconds") ? ((Number) config.get("fire-seconds")).intValue() : 5;

        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(0, 1, 0), 60, radius / 2, 0.5, radius / 2, 0.2);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 0.9f);

        int count = 0;
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius)) {
            if (entity instanceof LivingEntity target && !entity.equals(player)) {
                target.setFireTicks(fireSec * 20);
                target.damage(4.0, player);
                count++;
            }
        }

        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#FF1493:#FF4500><bold>🌋 INFERNO BURST!</bold></gradient> Ignited " + count + " targets."));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 50;
    }
}
