package in.surventure.worldrelics.relic.ability;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;

public class WindBurstAbility implements RelicAbility {

    @Override
    public String getName() {
        return "wind-burst";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 0.5, 0), 60, 1.5, 0.5, 1.5, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.5f, 1.2f);

        // Launch player upward
        player.setVelocity(new Vector(0, 1.4, 0));

        // Blast nearby enemies
        for (Entity e : player.getNearbyEntities(6.0, 6.0, 6.0)) {
            if (e instanceof LivingEntity target && !e.equals(player)) {
                Vector push = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(2.0).setY(0.4);
                target.setVelocity(push);
            }
        }

        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#F0FFFF:#ADD8E6><bold>🪶 WIND BURST!</bold></gradient> Launched into the sky with a gust of wind."));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 25;
    }
}
