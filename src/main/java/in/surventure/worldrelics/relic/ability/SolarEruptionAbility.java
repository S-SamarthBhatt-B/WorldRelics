package in.surventure.worldrelics.relic.ability;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class SolarEruptionAbility implements RelicAbility {

    @Override
    public String getName() {
        return "solar-eruption";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        Location origin = player.getEyeLocation();
        player.getWorld().playSound(origin, Sound.ITEM_FIRECHARGE_USE, 1.5f, 0.8f);

        for (int i = 1; i <= 15; i++) {
            Location pLoc = origin.clone().add(origin.getDirection().multiply(i));
            player.getWorld().spawnParticle(Particle.FLAME, pLoc, 10, 0.3, 0.3, 0.3, 0.05);
            player.getWorld().spawnParticle(Particle.FLASH, pLoc, 2, 0.1, 0.1, 0.1, 0.01);
        }

        for (Entity e : player.getNearbyEntities(10.0, 10.0, 10.0)) {
            if (e instanceof LivingEntity target && !e.equals(player)) {
                target.setFireTicks(120);
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                target.damage(8.0, player);
            }
        }

        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#FF8C00:#FFD700><bold>☀️ SOLAR ERUPTION!</bold></gradient> Scorched target area with sunfire."));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 50;
    }
}
