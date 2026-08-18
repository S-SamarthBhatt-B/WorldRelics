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

public class GravitySlamAbility implements RelicAbility {

    @Override
    public String getName() {
        return "gravity-slam";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 100, 3.0, 1.0, 3.0, 0.2);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.5f);

        // Pull nearby entities inward then damage & slam down
        for (Entity e : player.getNearbyEntities(12.0, 12.0, 12.0)) {
            if (e instanceof LivingEntity target && !e.equals(player)) {
                Vector pull = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(1.5).setY(0.3);
                target.setVelocity(pull);
                target.damage(10.0, player);
            }
        }

        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#2F4F4F:#000000><bold>⚓ GRAVITY SLAM!</bold></gradient> Dragged foes in and slammed the ground."));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 60;
    }
}
