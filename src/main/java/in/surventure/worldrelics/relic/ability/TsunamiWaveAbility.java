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

public class TsunamiWaveAbility implements RelicAbility {

    @Override
    public String getName() {
        return "tsunami-wave";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        player.getWorld().spawnParticle(Particle.WATER_WAKE, player.getLocation().add(0, 1, 0), 100, 3.0, 1.0, 3.0, 0.2);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.5f, 0.8f);

        for (Entity e : player.getNearbyEntities(7.0, 7.0, 7.0)) {
            if (e instanceof LivingEntity target && !e.equals(player)) {
                Vector push = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.8).setY(0.6);
                target.setVelocity(push);
                target.damage(6.0, player);
            }
        }

        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#00F5FF:#008B8B><bold>🔱 TSUNAMI WAVE!</bold></gradient> Released crushing water surge."));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 30;
    }
}
